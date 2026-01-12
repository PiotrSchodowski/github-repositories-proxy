package dev.piotrschodowski.recruitment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GithubController {

    private static final String USER_REPOSITORIES_ENDPOINT = "/users/{username}/repositories";

    private final GithubService githubService;

    public GithubController(final GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping(USER_REPOSITORIES_ENDPOINT)
    public List<RepositoryResponse> listUserNonForkRepositories(@PathVariable final String username) {
        return githubService.getUserRepositoriesWithBranches(username);
    }
}