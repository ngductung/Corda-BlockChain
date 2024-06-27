package com.template.contracts;

import com.template.states.BookingState;
import org.junit.Test;

public class StateTests {

    //Mock State test check for if the state has correct parameters type
    @Test
    public void hasFieldOfCorrectType() throws NoSuchFieldException {
        BookingState.class.getDeclaredField("msg");
        assert (BookingState.class.getDeclaredField("msg").getType().equals(String.class));
    }
}