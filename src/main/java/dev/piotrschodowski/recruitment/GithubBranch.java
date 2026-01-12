package dev.piotrschodowski.recruitment;

record GithubBranch(
        String name,
        Commit commit
) {
    record Commit(String sha) {
    }
}
