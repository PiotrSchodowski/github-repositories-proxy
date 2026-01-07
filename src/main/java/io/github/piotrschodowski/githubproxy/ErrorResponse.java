package io.github.piotrschodowski.githubproxy;

record ErrorResponse(
        int status,
        String message
) {
}
