# GitHub Repositories Proxy

Simple Spring Boot application acting as a proxy to GitHub API v3.

The application exposes an endpoint that returns all **non-fork** repositories
for a given GitHub user, along with their branches and last commit SHA.

---

## Tech stack

- Java 25
- Spring Boot 4.0.1
- Maven
- WireMock (integration tests)

---

## How to run the application

### Prerequisites
- **JDK 25**

### Run the application

#### Windows

```bash
.\mvnw.cmd spring-boot:run
```

#### Linux
```bash
./mvnw spring-boot:run
```
The application will start on port 8080.

---

## API Endpoint
### Get user repositories (non-fork)
 `GET /users/{username}/repositories`



### Example request
```bash
curl http://localhost:8080/users/octocat/repositories
```

### Example response
```json
[
  {
    "repositoryName": "my-repo",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "aaa111"
      }
    ]
  }
]
```

---


## Error handling

If the GitHub user does not exist, the API returns 404 with the following format:
```json
{
  "status": 404,
  "message": "GitHub user 'username' not found"
}
```

---

## Running tests

Integration tests use WireMock to simulate GitHub API.
```bash
./mvnw test
```
