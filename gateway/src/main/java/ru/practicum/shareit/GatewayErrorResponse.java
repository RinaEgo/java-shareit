package ru.practicum.shareit;

public class GatewayErrorResponse extends RuntimeException {
    private final String error;

    public GatewayErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}