# Step 11 — NFR-MAIN-02: SonarQube Integration

**NFR IDs:** NFR-MAIN-02
**Effort:** ~2 hrs | **Dependencies:** None
**Commit:** `feat(quality): add SonarQube integration (NFR-MAIN-02)`

---

## SRS Requirement

> SonarQube quality gate: A rating; no critical or blocker issues.

## Current State

No SonarQube integration. No `sonar-project.properties`. No quality gate configuration.

## Implementation

### New File: `sonar-project.properties`

**Path:** `taskhive-backend/sonar-project.properties`

```properties
sonar.projectKey=taskhive-backend
sonar.projectName=TaskHive Backend
sonar.projectVersion=1.0

sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.source=21

sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes

sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

sonar.exclusions=\
  **/config/**,\
  **/dto/**,\
  **/model/**,\
  **/exception/**,\
  **/enums/**,\
  **/*Application.java

sonar.issue.ignore.multicriteria=e1
sonar.issue.ignore.multicriteria.e1.ruleKey=java:S6813
sonar.issue.ignore.multicriteria.e1.resourceKey=**/*.java
```

### Modify: `pom.xml`

Add SonarQube + JaCoCo plugins:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>

<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.11.0.3922</version>
</plugin>
```

### Run SonarQube Locally

```bash
# Start SonarQube via Docker
docker run -d --name sonarqube -p 9000:9000 sonarqube:community

# Wait for it to start, then run analysis
cd taskhive-backend
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<your-sonar-token>

# View results at http://localhost:9000
```

### Quality Gate Configuration

In SonarQube UI, set quality gate:
- Coverage ≥ 80%
- Duplicated Lines ≤ 3%
- Maintainability Rating = A
- Reliability Rating = A
- Security Rating = A
- No Critical or Blocker issues

## Verification

```bash
# Run analysis and check results
mvn clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000
# Open http://localhost:9000/dashboard?id=taskhive-backend
# Quality gate should show current status
```
