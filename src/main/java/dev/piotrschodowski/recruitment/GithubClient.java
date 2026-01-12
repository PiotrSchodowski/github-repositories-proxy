package dev.piotrschodowski.recruitment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.List;

@Component
class GithubClient {

    private static final String USERS_REPOS_PATH = "/users/{username}/repos";
    private static final String REPO_BRANCHES_PATH = "/repos/{owner}/{repo}/branches";

    private final RestClient restClient;

    GithubClient(@Value("${github.api.base-url}") final String githubApiBaseUrl) {
        this.restClient = buildGithubRestClient(githubApiBaseUrl);
    }

    List<GithubRepo> fetchUserRepositories(final String username) {
        try {
            GithubRepo[] repos = restClient.get()
                    .uri(USERS_REPOS_PATH, username)
                    .retrieve()
                    .body(GithubRepo[].class);

            return toListOrEmpty(repos);
        } catch (RestClientResponseException ex) {
            throw handleClientError(ex, username);
        }
    }

    List<GithubBranch> fetchRepositoryBranches(final String ownerLogin, final String repositoryName) {
        GithubBranch[] branches = restClient.get()
                .uri(REPO_BRANCHES_PATH, ownerLogin, repositoryName)
                .retrieve()
                .body(GithubBranch[].class);

        return toListOrEmpty(branches);
    }

    private static RestClient buildGithubRestClient(final String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private static RuntimeException handleClientError(final RestClientResponseException ex, final String username) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new GithubUserNotFoundException(username);
        }
        return ex;
    }

    private static <T> List<T> toListOrEmpty(T[] array) {
        return array != null ? Arrays.asList(array) : List.of();
    }
}