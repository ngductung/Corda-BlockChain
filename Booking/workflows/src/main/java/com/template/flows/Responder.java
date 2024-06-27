package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.BookingState;
import net.corda.core.contracts.ContractState;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.WireTransaction;
import net.corda.core.utilities.ProgressTracker;

@InitiatedBy(Initiator.class)
public class Responder extends FlowLogic<Void> {
    private final FlowSession counterpartySession;

    public Responder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    private final ProgressTracker.Step RECEIVING = new ProgressTracker.Step("Receiving the transaction.");
    private final ProgressTracker.Step SIGNING = new ProgressTracker.Step("Signing the transaction.");
    private final ProgressTracker.Step FINALISING = new ProgressTracker.Step("Finalising the transaction.");

    private final ProgressTracker progressTracker = new ProgressTracker(
            RECEIVING,
            SIGNING,
            FINALISING
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        progressTracker.setCurrentStep(RECEIVING);

        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession counterpartySession) {
                super(counterpartySession);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {
                WireTransaction tx = stx.getTx();
                ContractState output = tx.getOutputs().get(0).getData();
                if (!(output instanceof BookingState)) {
                    throw new IllegalArgumentException("This must be a Booking transaction.");
                }
                BookingState bookingState = (BookingState) output;
            }
        }

        progressTracker.setCurrentStep(SIGNING);
        SignedTransaction fullySignedTx = subFlow(new SignTxFlow(counterpartySession));

        progressTracker.setCurrentStep(FINALISING);
        subFlow(new ReceiveFinalityFlow(counterpartySession, fullySignedTx.getId()));

        counterpartySession.send("Booking request has been successfully recorded.");

        String roomNumberMessage = counterpartySession.receive(String.class).unwrap(data -> data);
        getLogger().info(roomNumberMessage);

        displayBookingInformation(roomNumberMessage);

        return null;
    }

    private void displayBookingInformation(String message) {
        System.out.println("Notice: " + message + "!!!!!!!!!!!!!");
    }
}

