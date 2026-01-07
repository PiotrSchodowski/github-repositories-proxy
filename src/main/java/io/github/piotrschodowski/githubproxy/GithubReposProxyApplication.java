package io.github.piotrschodowski.githubproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class GithubReposProxyApplication {

	static void main(String[] args) {
		SpringApplication.run(GithubReposProxyApplication.class, args);
	}
}
