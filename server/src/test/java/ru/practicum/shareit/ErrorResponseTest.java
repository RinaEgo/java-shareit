package ru.practicum.shareit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {
    @Test
    void shouldCreateErrorResponse() {
        String errorMessage = "Error";
        ErrorResponse error = new ErrorResponse(errorMessage);

        assertEquals(errorMessage, error.getError());
    }
}