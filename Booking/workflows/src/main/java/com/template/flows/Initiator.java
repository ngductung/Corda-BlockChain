package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.BookingContract;
import com.template.states.BookingState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.contracts.UniqueIdentifier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class Initiator extends FlowLogic<SignedTransaction> {
    private final String hotelName;
    private final int roomNumber;
    private final String checkInDate;
    private final String checkOutDate;
    private final String guestName;
    private final String guestEmail;
    private final int guestAge;
    private final String roomType;
    private final double originalRoomPrice;
    private final String creditCardNumber;
    private final String creditCardExpiryDate;
    private final String bookingReference;
    private final Party counterparty;

    public Initiator(String hotelName, int roomNumber, String checkInDate, String checkOutDate, String guestName, String guestEmail, int guestAge, String roomType, double originalRoomPrice, String creditCardNumber, String creditCardExpiryDate, String bookingReference, Party counterparty) {
        this.hotelName = hotelName;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestAge = guestAge;
        this.roomType = roomType;
        this.originalRoomPrice = originalRoomPrice;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiryDate = creditCardExpiryDate;
        this.bookingReference = bookingReference;
        this.counterparty = counterparty;
    }

    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new Booking.");
    private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the counterparty's signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.tracker();
        }
    };
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.tracker();
        }
    };

    private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate checkIn = LocalDate.parse(checkInDate, formatter);
        LocalDate checkOut = LocalDate.parse(checkOutDate, formatter);

        List<StateAndRef<BookingState>> bookingStates = getServiceHub().getVaultService().queryBy(BookingState.class).getStates();

        for (StateAndRef<BookingState> state : bookingStates) {
            BookingState booking = state.getState().getData();
            if (booking.getHotelName().equals(hotelName) && booking.getRoomNumber() == roomNumber) {
                LocalDate existingCheckIn = LocalDate.parse(booking.getCheckInDate(), formatter);
                LocalDate existingCheckOut = LocalDate.parse(booking.getCheckOutDate(), formatter);

                boolean isOverlapping = checkIn.isBefore(existingCheckOut) && checkOut.isAfter(existingCheckIn);
                if (isOverlapping) {
                    System.out.println("Room " + roomNumber + " at " + hotelName + " is already booked for the given dates.");
                    throw new FlowException("Room " + roomNumber + " at " + hotelName + " is already booked for the given dates.");
                }
            }
        }

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        BookingState outputState = new BookingState(
                hotelName,
                roomNumber,
                checkInDate,
                checkOutDate,
                guestName,
                guestEmail,
                guestAge,
                roomType,
                originalRoomPrice,
                creditCardNumber,
                creditCardExpiryDate,
                bookingReference,
                Arrays.asList(getOurIdentity(), counterparty),
                new UniqueIdentifier()
        );
        Command<BookingContract.Commands.Create> txCommand = new Command<>(
                new BookingContract.Commands.Create(),
                Arrays.asList(outputState.getParticipants().get(0).getOwningKey(), outputState.getParticipants().get(1).getOwningKey())
        );
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(outputState, BookingContract.ID)
                .addCommand(txCommand);

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        try {
            txBuilder.verify(getServiceHub());
        }
        catch (TransactionVerificationException e) {
            getLogger().error("Transaction verification failed: " + e.getMessage());
            throw new FlowException();
        }

        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

        progressTracker.setCurrentStep(GATHERING_SIGS);
        FlowSession counterpartySession = initiateFlow(counterparty);
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partSignedTx, Arrays.asList(counterpartySession), CollectSignaturesFlow.tracker()));


        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        SignedTransaction finalisedTx = subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(counterpartySession), FINALISING_TRANSACTION.childProgressTracker()));

        String confirmationMessage = counterpartySession.receive(String.class).unwrap(data -> data);
        getLogger().info(confirmationMessage);

        counterpartySession.send("Room number " + roomNumber + " has been successfully booked by " + guestName + " :)  !!!!!!!!!");

        return finalisedTx;
    }
}
