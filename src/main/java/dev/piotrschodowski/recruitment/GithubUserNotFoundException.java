package dev.piotrschodowski.recruitment;

final class GithubUserNotFoundException extends RuntimeException {
    GithubUserNotFoundException(final String username) {
        super("GitHub user '%s' not found".formatted(username));
    }
}
