package io.github.piotrschodowski.githubproxy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class GithubController {

    private static final String USER_REPOSITORIES_ENDPOINT = "/users/{username}/repositories";

    private final GithubService githubService;

    GithubController(final GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping(USER_REPOSITORIES_ENDPOINT)
    List<RepositoryResponse> listUserNonForkRepositories(@PathVariable final String username) {
        return githubService.getUserRepositoriesWithBranches(username);
    }
}