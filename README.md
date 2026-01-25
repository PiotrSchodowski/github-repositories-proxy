# GitHub Repositories Proxy

Simple Spring Boot application acting as a proxy to GitHub API v3.

The application exposes an endpoint that returns all **non-fork** repositories
for a given GitHub user, along with their branches and last commit SHA.

---
## Table of Contents
* [Tech Stack](#tech-stack)
* [How to Run](#how-to-run)
* [API Endpoint](#api-endpoint)
* [Notes](#notes)
* [Error Handling](#error-handling)
* [Running Tests](#running-tests)
* [Project Status](#project-status)
* [License](#license)


## Tech stack

* Java 25
* Spring Boot 4.0.1
* Maven
* WireMock (for integration testing)

---

## How to run

### Prerequisites
* **JDK 25+**
* **Maven 3.6+** (optional, wrapper is provided)

### Run the application

#### Windows

```bash
.\mvnw.cmd spring-boot:run
```

#### Linux
```bash
./mvnw spring-boot:run
```
The application will start on http://localhost:8080.

---

## API Endpoint
### Headers
- `API-Version` (optional, default: `1.0`)
### Get user repositories (non-fork)
 `GET /users/{username}/repositories`



### Example request
```bash
curl -H "API-Version: 1.0" http://localhost:8080/users/octocat/repositories
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
### Example response (user exists, 0 repositories)

```json
[]
```
---

## Notes
GitHub API Rate Limiting: This proxy inherits the standard GitHub API rate limits 
(60 requests per hour for unauthenticated requests).
Backing API: GitHub REST API v3 (`/users/{username}/repos`, `/repos/{owner}/{repo}/branches`).

---

## Preview features (Java 25)

This project uses Java 25 **Structured Concurrency** (`StructuredTaskScope`) to fetch branch data concurrently.  
Structured Concurrency is a **preview feature** in Java 25, therefore the application and tests require `--enable-preview`.

The Maven build is configured to pass `--enable-preview` for compilation, tests and `spring-boot:run`.


---


## Error handling

The API returns user-friendly error messages in JSON format:

404 Not Found: GitHub user does not exist.
```json
{
  "status": 404,
  "message": "GitHub user 'username' not found"
}
```

---

## Running tests

Integration tests use WireMock to simulate GitHub API responses, 
ensuring no real calls are made during testing.
```bash
./mvnw test
```
---

## Project Status
This project was created as a coding exercise/assignment.
It is considered complete for its initial scope.

---

## License
This project is provided for educational and demonstration purposes.
