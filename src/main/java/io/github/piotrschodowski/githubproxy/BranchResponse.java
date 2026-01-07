package io.github.piotrschodowski.githubproxy;

record BranchResponse(
        String name,
        String lastCommitSha
) {
}
