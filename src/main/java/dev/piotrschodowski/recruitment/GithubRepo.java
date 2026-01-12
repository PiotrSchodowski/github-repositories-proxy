package dev.piotrschodowski.recruitment;

record GithubRepo(
        String name,
        Owner owner,
        boolean fork
) {
    record Owner(String login) {}
}
