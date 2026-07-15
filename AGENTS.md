# Repository Guidelines

## Project Structure & Module Organization

`juror-api` is a Spring Boot application for the Juror Modernisation project.

- `src/main/java`: application code under `uk.gov.hmcts.juror.api`
- `src/main/resources`: application config and SQL resources
- `src/test/java`: unit tests
- `src/integration-test/java`: integration tests
- `src/functionalTest/java`: functional tests
- `src/smokeTest/java`: smoke tests
- `src/integration-test/resources/db`: SQL fixtures used by integration tests
- `config/checkstyle` and `config/pmd`: static analysis rules
- `infrastructure/`: Terraform and deployment-related assets

The main code follows standard Spring layering. Prefer keeping responsibilities explicit:

- `**/controller`: HTTP endpoints and request/response mapping
- `**/service`: business logic and orchestration
- `**/repository`: persistence and query code
- `**/domain`: JPA entities and domain models
- `**/controller/request` and `**/controller/response`: API DTOs

## Build, Test, and Development Commands

- `./gradlew build`: compiles the project and runs the default verification flow
- `./gradlew test`: runs unit tests
- `./gradlew integration`: runs integration tests from `src/integration-test/java`
- `./gradlew functional`: runs functional tests
- `./gradlew smoke`: runs smoke tests
- `./gradlew check`: runs static analysis and integration verification; in this repo it depends on `integration`
- `./gradlew jacocoTestReport`: generates coverage output for unit + integration tests
- `./gradlew runAllStyleChecks`: runs Checkstyle and PMD across all configured source sets
- `./gradlew dependencyCheckAnalyze`: runs OWASP dependency analysis
- `./gradlew bootRun`: runs the service locally with the `development` profile
- `./gradlew flywayMigrate`: applies Flyway migrations to the configured database
- `./gradlew migratePostgresDatabase -Pdburl=<host:port/db>`: migrates a specific Postgres database

Local prerequisites documented in `README.md` are Java 17, Docker, and Postgres.

## Coding Style & Naming Conventions

This project builds with Java 17 and Spring Boot 3.5.4.

- Use 4-space indentation.
- Respect the Checkstyle max line length of 120 characters.
- Avoid wildcard imports.
- Prefer constructor injection over field injection.
- Use descriptive names; avoid abbreviations unless they are already established in the domain.
- Keep controller methods thin and move business logic into services.
- Keep repository code focused on persistence concerns.

Before generating or refactoring code, consult:

- `config/checkstyle/checkstyle.xml`
- `config/pmd/ruleset.xml`
- `config/editorConfig/Project.xml`

If IntelliJ formatting guidance conflicts with build-enforced rules, treat the build rules as the source of truth.

## Persistence & Query Guidance

- Default JPA relationships to lazy loading unless there is a strong reason not to.
- Prefer targeted repository queries, projections, or QueryDSL expressions over loading large object graphs into memory.
- Keep transaction boundaries in service methods, not controllers.
- Use `@Transactional(readOnly = true)` for read-only service operations where appropriate.
- When changing query behavior, check existing integration tests and SQL fixtures before assuming semantics.
- Do not modify existing flyway migrations. Instead, create new migrations for any schema changes.
- If adding Flyway migrations, ensure they are idempotent and compatible with existing test fixtures.
- When adding new database fields, consider how they will be populated in both runtime and test contexts, and whether they require updates to existing fixtures or new ones.
- When modifying existing queries, consider the impact on both runtime behavior and integration tests that rely on specific query results. If necessary, update or add SQL fixtures to ensure tests remain valid and reflect the new query semantics.

## QueryDSL & Injection Safety


- Prefer QueryDSL's typed predicates (Q-class expressions, BooleanBuilder, CaseBuilder) over building queries with string concatenation.
- Never concatenate user-supplied input directly into .where() clauses, native @Query strings, or raw JDBC/EntityManager.createNativeQuery calls. Use bind parameters (?1, named parameters, or QueryDSL's own parameter binding) in all cases.
- If a native or JPQL query is unavoidable, use parameterized placeholders and verify at review time that no request/DTO field is interpolated into the query text.
- Be especially cautious with dynamic sort/filter fields (e.g., report or search endpoints that accept a column name or filter key from the client) — validate against an allow-list of known QueryDSL paths rather than resolving field names from raw request input.
- Treat any reflection-based or dynamically-constructed QueryDSL path resolution as a review flag; confirm the source of the field name cannot be influenced by an untrusted caller.


## Data Handling & PII

- This system handles juror, elector, and Local Authority data, including names, addresses, and other personal data. Treat this as sensitive by default.


- Never log full request/response bodies, entities, or DTOs that may contain PII (names, addresses, dates of birth, contact details, electoral data). Log identifiers (e.g., juror number, case ID) instead of personal fields.
- Review toString(), Lombok @Data/@ToString usage, and exception messages on entities/DTOs containing PII — these can leak personal data into logs indirectly. Prefer excluding sensitive fields (@ToString.Exclude) or using targeted, explicit logging statements instead of logging whole objects.
- Do not include PII in log statements at INFO level or above used for routine tracing; if personal data must appear for debugging, keep it at DEBUG/TRACE and ensure it is not enabled in shared or production-like environments.
- Be cautious with PII in error messages surfaced to clients — prefer generic messages over ones that echo back submitted personal data.
- Test data (including generated electoral register test data) must be synthetic/fabricated, not derived from real elector or juror records.
- Apply the same care to Application Insights/telemetry queries and any exported CSV/report data (e.g., StreamingResponseBody exports) — confirm exports only include the fields the endpoint's contract requires, not full entity graphs.

## Testing Guidelines

This repository uses a mix of JUnit 4 and JUnit 5, so follow the style already used in the area you are changing.

- Mirror the source package structure in tests.
- Name test classes `*Test` or `*ITest` consistently with nearby code.
- Add unit tests for decision-heavy logic: branching, validation, calculations, mapping, and error handling.
- Add integration tests when behavior depends on database queries, Spring wiring, security rules, or serialized API responses.
- Reuse existing SQL fixture patterns under `src/integration-test/resources/db`.
- Prefer focused tests over broad fixture-heavy tests when both are possible.

At minimum, run the most relevant local checks before asking for review:

- `./gradlew test` for isolated logic changes
- `./gradlew integration` for repository, controller, or query changes
- `./gradlew check` for broader cross-cutting changes

## Code Review Guidance for Agents

Prioritize correctness, regression risk, and missing coverage.

- `High`: security flaws, data corruption risks, broken behavior, contract regressions, or changes likely to fail in production
- `Medium`: logic ambiguities, performance concerns, query inefficiencies, missing validation, or incomplete test coverage
- `Low`: naming, duplication, readability, or documentation issues

Review comments should be specific about:

- the problem
- why it matters
- what part of the code or contract is affected
- what change or test would resolve it

For this repo, pay particular attention to:

- Spring security and role-based behavior
- database query semantics, especially QueryDSL and grouped counts
- DTO field naming and serialized JSON contracts
- Flyway compatibility and SQL fixture stability

## Definition of Done

- The change is consistent with existing package and layering conventions.
- New logic is covered by the appropriate level of tests.
- Static analysis impact has been considered.
- Public API changes are reflected in DTOs, tests, and documentation where needed.
- Database-related changes account for both runtime behavior and integration test fixtures.
- No secrets, environment-specific values, or destructive local-only assumptions are committed.

## Security & Configuration Tips

- Never commit real secrets or tokens.
- Prefer environment variables or local config overrides described in `README.md`.
- Be careful when changing authentication, authorization, or JWT-related code paths.
- Treat schema, migration, and fixture changes as high-impact because they affect both runtime and tests.
