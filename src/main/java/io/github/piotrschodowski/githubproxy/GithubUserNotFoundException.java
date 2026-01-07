package io.github.piotrschodowski.githubproxy;

class GithubUserNotFoundException extends RuntimeException {

    GithubUserNotFoundException(String username) {
        super("GitHub user '%s' not found".formatted(username));
    }
}
