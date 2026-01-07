package io.github.piotrschodowski.githubproxy;

record GithubRepo(
        String name,
        Owner owner,
        boolean fork
) {
    record Owner(String login) {}
}
