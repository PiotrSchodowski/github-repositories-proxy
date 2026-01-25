package dev.piotrschodowski.recruitment;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class GithubControllerIT {

    private static final String EXISTING_USER = "octocat";
    private static final String MISSING_USER = "missing-user";

    private static final String NON_FORK_REPO = "my-repo";
    private static final String FORK_REPO = "forked-repo";

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.http.serviceclient.github.base-url", wireMock::baseUrl);
    }

    @Test
    void givenForksExist_whenListingRepositories_thenReturnsOnlyNonForksWithBranches() {
        stubGithubUserReposWithForksAndNonForks();
        stubGithubRepoBranches();

        restTestClient.get()
                .uri("/users/{username}/repositories", EXISTING_USER)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RepositoryResponse[].class)
                .consumeWith(result -> {
                    assertThat(result.getResponseBody())
                            .as("Response body must not be null")
                            .isNotNull();

                    final var repos = result.getResponseBody();
                    assertThat(repos).hasSize(1);

                    final var repo = repos[0];
                    assertThat(repo.repositoryName()).isEqualTo(NON_FORK_REPO);
                    assertThat(repo.ownerLogin()).isEqualTo(EXISTING_USER);

                    assertThat(repo.branches()).hasSize(2);
                    assertThat(repo.branches()).anySatisfy(b -> {
                        assertThat(b.name()).isEqualTo("main");
                        assertThat(b.lastCommitSha()).isEqualTo("aaa111");
                    });
                    assertThat(repo.branches()).anySatisfy(b -> {
                        assertThat(b.name()).isEqualTo("dev");
                        assertThat(b.lastCommitSha()).isEqualTo("bbb222");
                    });
                });

        verifyWireMockRequestsForHappyPath();
    }

    @Test
    void givenNoRepositories_whenListingRepositories_thenReturnsEmptyArray() {
        stubGithubUserReposEmpty();

        restTestClient.get()
                .uri("/users/{username}/repositories", EXISTING_USER)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RepositoryResponse[].class)
                .consumeWith(result -> {
                    assertThat(result.getResponseBody())
                            .as("Response body must not be null")
                            .isNotNull();

                    final var repos = result.getResponseBody();
                    assertThat(repos).isEmpty();
                });

        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/" + EXISTING_USER + "/repos")));
    }

    @Test
    void givenMissingGithubUser_whenListingRepositories_thenReturns404WithErrorBody() {
        stubGithubUserReposNotFound();

        restTestClient.get()
                .uri("/users/{username}/repositories", MISSING_USER)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .consumeWith(result -> {
                    assertThat(result.getResponseBody())
                            .as("Error body must not be null")
                            .isNotNull();
                    final var body = result.getResponseBody();
                    assertThat(body.status()).isEqualTo(404);
                    assertThat(body.message()).contains("GitHub user '" + MISSING_USER + "' not found");
                });

        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/" + MISSING_USER + "/repos")));
    }

    @Test
    void givenApiVersion2_whenListingRepositories_thenReturnsV2Wrapper() {
        stubGithubUserReposWithForksAndNonForks();
        stubGithubRepoBranches();

        restTestClient.get()
                .uri("/users/{username}/repositories", EXISTING_USER)
                .header("API-Version", "2.0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RepositoriesResponseV2.class)
                .consumeWith(result -> {
                    assertThat(result.getResponseBody())
                            .as("Response body must not be null")
                            .isNotNull();
                    final var body = result.getResponseBody();
                    assertThat(body.count()).isEqualTo(1);
                    assertThat(body.repositories()).hasSize(1);

                    final var repos = body.repositories();
                    assertThat(repos).hasSize(1);

                    final var repo = repos.getFirst();
                    assertThat(repo.repositoryName()).isEqualTo(NON_FORK_REPO);
                    assertThat(repo.ownerLogin()).isEqualTo(EXISTING_USER);

                    assertThat(repo.branches()).hasSize(2);
                    assertThat(repo.branches()).anySatisfy(b -> {
                        assertThat(b.name()).isEqualTo("main");
                        assertThat(b.lastCommitSha()).isEqualTo("aaa111");
                    });
                    assertThat(repo.branches()).anySatisfy(b -> {
                        assertThat(b.name()).isEqualTo("dev");
                        assertThat(b.lastCommitSha()).isEqualTo("bbb222");
                    });
                });

        verifyWireMockRequestsForHappyPath();
    }

    // ---------- WireMock stubs ----------

    private void stubGithubUserReposWithForksAndNonForks() {
        final var githubRepos = List.of(
                new GithubRepo(FORK_REPO, new GithubRepo.Owner(EXISTING_USER), true),
                new GithubRepo(NON_FORK_REPO, new GithubRepo.Owner(EXISTING_USER), false),
                new GithubRepo("another-fork", new GithubRepo.Owner(EXISTING_USER), true)
        );

        wireMock.stubFor(get(urlEqualTo("/users/" + EXISTING_USER + "/repos"))
                .willReturn(okJson(objectMapper.writeValueAsString(githubRepos))));
    }

    private void stubGithubRepoBranches() {
        final var branches = List.of(
                new GithubBranch("main", new GithubBranch.Commit("aaa111")),
                new GithubBranch("dev", new GithubBranch.Commit("bbb222"))
        );

        wireMock.stubFor(get(urlEqualTo("/repos/" + EXISTING_USER + "/" + NON_FORK_REPO + "/branches"))
                .willReturn(okJson(objectMapper.writeValueAsString(branches))));
    }

    private void stubGithubUserReposNotFound() {
        wireMock.stubFor(get(urlEqualTo("/users/" + MISSING_USER + "/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Not Found\",\"documentation_url\":\"https://docs.github.com/rest\"}")));
    }

    private void stubGithubUserReposEmpty() {
        wireMock.stubFor(get(urlEqualTo("/users/" + EXISTING_USER + "/repos"))
                .willReturn(okJson("[]")));
    }

    // ---------- Verifications ----------

    private void verifyWireMockRequestsForHappyPath() {
        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/" + EXISTING_USER + "/repos")));
        wireMock.verify(1, getRequestedFor(urlEqualTo("/repos/" + EXISTING_USER + "/" + NON_FORK_REPO + "/branches")));
        wireMock.verify(0, getRequestedFor(urlEqualTo("/repos/" + EXISTING_USER + "/" + FORK_REPO + "/branches")));
    }
}