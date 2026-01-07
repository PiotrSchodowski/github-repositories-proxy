package io.github.piotrschodowski.githubproxy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubControllerIT {

    private static final String EXISTING_USER = "octocat";
    private static final String MISSING_USER = "not-existing-user";

    private static final String NON_FORK_REPO = "my-repo";
    private static final String FORK_REPO = "forked-repo";

    @LocalServerPort
    int port;

    private RestClient appClient;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", wireMock::baseUrl);
    }

    @BeforeEach
    void setUp() {
        appClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldReturnOnlyNonForkRepositoriesWithBranches() {
        // given
        stubGithubUserReposExisting();
        stubGithubRepoBranchesExisting();

        // when
        ResponseEntity<RepositoryResponse[]> response = appClient.get()
                .uri("/users/{username}/repositories", EXISTING_USER)
                .retrieve()
                .toEntity(RepositoryResponse[].class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<RepositoryResponse> repos = asList(response.getBody());
        assertThat(repos).hasSize(1);

        RepositoryResponse repo = repos.getFirst();
        assertThat(repo.repositoryName()).isEqualTo(NON_FORK_REPO);
        assertThat(repo.ownerLogin()).isEqualTo(EXISTING_USER);

        assertThat(repo.branches()).hasSize(2);
        assertThat(branchNames(repo.branches())).containsExactly("main", "dev");
        assertThat(branchShas(repo.branches())).containsExactly("aaa111", "bbb222");

        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/" + EXISTING_USER + "/repos")));
        wireMock.verify(1, getRequestedFor(urlEqualTo("/repos/" + EXISTING_USER + "/" + NON_FORK_REPO + "/branches")));
        wireMock.verify(0, getRequestedFor(urlEqualTo("/repos/" + EXISTING_USER + "/" + FORK_REPO + "/branches")));
    }

    @Test
    void shouldReturn404WithExpectedBodyWhenGithubUserDoesNotExist() {
        // given
        stubGithubUserReposNotFound();

        // when
        HttpClientErrorException ex = callAndExpectClientError();

        // then
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ErrorResponse body = ex.getResponseBodyAs(ErrorResponse.class);
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.message()).contains("GitHub user '" + MISSING_USER + "' not found");
    }

    private static void stubGithubUserReposExisting() {
        wireMock.stubFor(get(urlEqualTo("/users/" + EXISTING_USER + "/repos"))
                .willReturn(okJson("""
                        [
                          { "name": "%s", "fork": true,  "owner": { "login": "%s" } },
                          { "name": "%s", "fork": false, "owner": { "login": "%s" } }
                        ]
                        """.formatted(FORK_REPO, EXISTING_USER, NON_FORK_REPO, EXISTING_USER))));
    }

    private static void stubGithubRepoBranchesExisting() {
        wireMock.stubFor(get(urlEqualTo("/repos/" + EXISTING_USER + "/" + NON_FORK_REPO + "/branches"))
                .willReturn(okJson("""
                        [
                          { "name": "main", "commit": { "sha": "aaa111" } },
                          { "name": "dev",  "commit": { "sha": "bbb222" } }
                        ]
                        """)));
    }

    private static void stubGithubUserReposNotFound() {
        wireMock.stubFor(get(urlEqualTo("/users/" + MISSING_USER + "/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"message\": \"Not Found\" }")));
    }

    private HttpClientErrorException callAndExpectClientError() {
        try {
            appClient.get()
                    .uri("/users/{username}/repositories", MISSING_USER)
                    .retrieve()
                    .toEntity(ErrorResponse.class);

            assertWithMessage("Expected 4xx for user '%s'", MISSING_USER).fail();
            throw new IllegalStateException("unreachable");
        } catch (HttpClientErrorException ex) {
            return ex;
        }
    }

    private static List<RepositoryResponse> asList(RepositoryResponse[] body) {
        return body == null ? List.of() : List.of(body);
    }

    private static List<String> branchNames(List<BranchResponse> branches) {
        return branches.stream().map(BranchResponse::name).toList();
    }

    private static List<String> branchShas(List<BranchResponse> branches) {
        return branches.stream().map(BranchResponse::lastCommitSha).toList();
    }
}