package dev.piotrschodowski.recruitment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
class GithubRestClientConfig {

    @Bean
    RestClient githubRestClient(
            RestClient.Builder builder,
            @Value("${github.api.base-url}") String githubApiBaseUrl
    ) {
        return builder
                .baseUrl(githubApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}