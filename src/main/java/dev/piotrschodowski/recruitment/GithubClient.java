// src/main/java/dev/piotrschodowski/recruitment/GithubClient.java
package dev.piotrschodowski.recruitment;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
final class GithubClient {

    private final GithubHttpApi githubHttpApi;

    GithubClient(final GithubHttpApi githubHttpApi) {
        this.githubHttpApi = githubHttpApi;
    }

    List<GithubRepo> fetchUserRepositories(final String username) {
        try {
            final var repos = Objects.requireNonNull(
                    githubHttpApi.userRepos(username),
                    "GitHub returned null body for repositories"
            );

            return Arrays.stream(repos).toList();
        } catch (RestClientResponseException ex) {
            throw handleClientError(ex, username);
        }
    }

    List<GithubBranch> fetchRepositoryBranches(final String ownerLogin, final String repositoryName) {
        final var branches = Objects.requireNonNull(
                githubHttpApi.repoBranches(ownerLogin, repositoryName),
                "GitHub returned null body for branches"
        );

        return Arrays.stream(branches).toList();
    }

    private static RuntimeException handleClientError(final RestClientResponseException ex, final String username) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new GithubUserNotFoundException(username);
        }
        return ex;
    }
}