package dev.piotrschodowski.recruitment;

public record BranchResponse(
        String name,
        String lastCommitSha
) {
}
