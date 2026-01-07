package io.github.piotrschodowski.githubproxy;

record GithubBranch(
        String name,
        Commit commit
) {
    record Commit(String sha) {
    }
}
