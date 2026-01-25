// src/main/java/dev/piotrschodowski/recruitment/RepositoriesResponseV2.java
package dev.piotrschodowski.recruitment;

import java.util.List;

public record RepositoriesResponseV2(
        int count,
        List<RepositoryResponse> repositories
) {}
