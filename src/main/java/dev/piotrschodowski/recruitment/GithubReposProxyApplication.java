package dev.piotrschodowski.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@ImportHttpServices(group = "github", types = GithubHttpApi.class)
public class GithubReposProxyApplication {

    static void main(String[] args) {
        SpringApplication.run(GithubReposProxyApplication.class, args);
    }
}