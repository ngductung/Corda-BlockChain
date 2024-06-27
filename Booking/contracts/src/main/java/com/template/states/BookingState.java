package com.template.states;

import com.template.contracts.BookingContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;

import java.util.List;

@BelongsToContract(BookingContract.class)
public class BookingState implements LinearState {
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
    private final List<AbstractParty> participants;
    private final UniqueIdentifier linearId;

    public BookingState(String hotelName, int roomNumber, String checkInDate, String checkOutDate, String guestName, String guestEmail, int guestAge, String roomType, double originalRoomPrice, String creditCardNumber, String creditCardExpiryDate, String bookingReference, List<AbstractParty> participants, UniqueIdentifier linearId) {
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
        this.participants = participants;
        this.linearId = linearId;
    }

    public String getHotelName() { return hotelName; }
    public int getRoomNumber() { return roomNumber; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public int getGuestAge() { return guestAge; }
    public String getRoomType() { return roomType; }
    public double getOriginalRoomPrice() { return originalRoomPrice; }
    public String getCreditCardNumber() { return creditCardNumber; }
    public String getCreditCardExpiryDate() { return creditCardExpiryDate; }
    public String getBookingReference() { return bookingReference; }
    @Override public List<AbstractParty> getParticipants() { return participants; }
    @Override public UniqueIdentifier getLinearId() { return linearId; }
}
