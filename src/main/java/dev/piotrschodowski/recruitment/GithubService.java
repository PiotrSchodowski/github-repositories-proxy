package dev.piotrschodowski.recruitment;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Service
final class GithubService {

    private final GithubClient githubClient;

    GithubService(final GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> getUserRepositoriesWithBranches(final String username) {
        final var nonForkRepos = githubClient.fetchUserRepositories(username).stream()
                .filter(repo -> !repo.fork())
                .toList();

        if (nonForkRepos.isEmpty()) {
            return List.of();
        }

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<RepositoryResponse>allSuccessfulOrThrow()
        )) {
            final var tasks = nonForkRepos.stream()
                    .map(repo -> scope.fork(() -> mapToRepositoryResponse(repo)))
                    .toList();

            scope.join();

            return tasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .toList();

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching branches from GitHub", e);
        }
    }

    private RepositoryResponse mapToRepositoryResponse(final GithubRepo repository) {
        final var ownerLogin = repository.owner().login();

        final var branches = githubClient.fetchRepositoryBranches(ownerLogin, repository.name()).stream()
                .map(this::mapToBranchResponse)
                .toList();

        return new RepositoryResponse(repository.name(), ownerLogin, branches);
    }

    private BranchResponse mapToBranchResponse(final GithubBranch branch) {
        return new BranchResponse(branch.name(), branch.commit().sha());
    }
}