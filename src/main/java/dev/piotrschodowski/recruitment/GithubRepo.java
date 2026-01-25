package dev.piotrschodowski.recruitment;

public record GithubRepo(
        String name,
        Owner owner,
        boolean fork
) {
    public record Owner(String login) {}
}
