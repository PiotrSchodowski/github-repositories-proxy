package dev.piotrschodowski.recruitment;

public record ErrorResponse(
        int status,
        String message
) {
}
