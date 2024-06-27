package com.template.contracts;

import com.template.states.BookingState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Arrays;

public class BookingContract implements Contract {
    public static final String ID = "com.template.contracts.BookingContract";

    @Override
    public void verify(LedgerTransaction tx) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Dieu kien co ban
        if (tx.getInputs().size() != 0) throw new IllegalArgumentException("No inputs should be consumed.");
        if (tx.getOutputs().size() != 1) throw new IllegalArgumentException("There should be one output state.");

        ContractState output = tx.getOutput(0);
        if (!(output instanceof BookingState)) throw new IllegalArgumentException("Output must be a BookingState.");

        BookingState bookingState = (BookingState) output;

        // Tuoi lon hon 18
        if (bookingState.getGuestAge() <= 18) {
            throw new IllegalArgumentException("Guest must be over 18 years old.");
        }

        // Ngay nhan va tra phong phai lon hon hien tai
        LocalDate checkInDate = LocalDate.parse(bookingState.getCheckInDate(), formatter);
        LocalDate checkOutDate = LocalDate.parse(bookingState.getCheckOutDate(), formatter);
        LocalDate currentDate = LocalDate.now();
        if (!checkInDate.isAfter(currentDate) || !checkOutDate.isAfter(currentDate)) {
            throw new IllegalArgumentException("Check-in and check-out dates must be in the future.");
        }

        // Ngay tra phong phai lon hon ngay nhan phong
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        // Dinh dang loai phong: K, NK, DD, NDD
        List<String> validRoomTypes = Arrays.asList("K", "NK", "DD", "NDD");
        if (!validRoomTypes.contains(bookingState.getRoomType())) {
            throw new IllegalArgumentException("Room type must be one of the following: K, NK, DD, NDD.");
        }

        // Gia hoa hong
        double commissionPrice = bookingState.getOriginalRoomPrice() * 0.85;
        if (bookingState.getOriginalRoomPrice() * 0.85 != commissionPrice) {
            throw new IllegalArgumentException("Commission price must be 85% of the original room price.");
        }

        // Chieu dai the tin dung la 16
        if (bookingState.getCreditCardNumber().length() != 16) {
            throw new IllegalArgumentException("Credit card number must be 16 digits long.");
        }

        // Ngay het han cua the tin dung phai lon hon ngay nhan phong
        LocalDate creditCardExpiryDate = LocalDate.parse(bookingState.getCreditCardExpiryDate(), formatter);
        if (creditCardExpiryDate.isBefore(checkInDate)) {
            throw new IllegalArgumentException("Credit card expiry date must be on or after the check-in date.");
        }
    }

    public interface Commands extends CommandData {
        class Create implements Commands {}
    }
}
