package dev.piotrschodowski.recruitment;

import org.jspecify.annotations.Nullable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.PathVariable;

@HttpExchange(accept = "application/json")
public interface GithubHttpApi {

    @GetExchange("/users/{username}/repos")
    @Nullable GithubRepo[] userRepos(@PathVariable("username") String username);

    @GetExchange("/repos/{owner}/{repo}/branches")
    @Nullable GithubBranch[] repoBranches(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo
    );
}