package io.github.piotrschodowski.githubproxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GithubService {

    private final GithubClient githubClient;

    GithubService(final GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> getUserRepositoriesWithBranches(final String username) {
        return githubClient.fetchUserRepositories(username).stream()
                .filter(this::isNotFork)
                .map(this::mapToRepositoryResponse)
                .toList();
    }

    private boolean isNotFork(final GithubRepo repository) {
        return !repository.fork();
    }

    private RepositoryResponse mapToRepositoryResponse(final GithubRepo repository) {
        final String ownerLogin = repository.owner().login();
        final List<BranchResponse> branches =
                fetchRepositoryBranches(ownerLogin, repository.name());

        return new RepositoryResponse(
                repository.name(),
                ownerLogin,
                branches
        );
    }

    private List<BranchResponse> fetchRepositoryBranches(
            final String ownerLogin,
            final String repositoryName
    ) {
        return githubClient.fetchRepositoryBranches(ownerLogin, repositoryName).stream()
                .map(this::mapToBranchResponse)
                .toList();
    }

    private BranchResponse mapToBranchResponse(final GithubBranch branch) {
        return new BranchResponse(
                branch.name(),
                branch.commit().sha()
        );
    }
}