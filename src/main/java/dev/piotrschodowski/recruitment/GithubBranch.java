package dev.piotrschodowski.recruitment;

public record GithubBranch(
        String name,
        Commit commit
) {
    public record Commit(String sha) {
    }
}
