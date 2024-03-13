package uk.gov.hmcts.juror.api.testsupport;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ContainerTest {

    @Container
    private static final PostgresqlContainer SQL_CONTAINER = PostgresqlContainer.getInstance();
}
