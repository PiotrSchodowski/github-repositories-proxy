package io.github.piotrschodowski.githubproxy;

import java.util.List;

record RepositoryResponse(
        String repositoryName,
        String ownerLogin,
        List<BranchResponse> branches
) {}