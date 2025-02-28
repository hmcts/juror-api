package uk.gov.hmcts.juror.api.testsupport;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

public class PostgresqlContainer extends PostgreSQLContainer<PostgresqlContainer> {

    private static final DockerImageName dockerImageName = DockerImageName
        .parse("hmctspublic.azurecr.io/imported/postgres:16-alpine")
        .asCompatibleSubstituteFor("postgres");
    private static PostgresqlContainer container;

    private PostgresqlContainer() {
        super(dockerImageName);
        setup();
    }

    public void setup() {
        withDatabaseName("juror");
        withUsername("system");
        withPassword("postgres");
        withExposedPorts(5432);
        setPortBindings(List.of(
            "5433:5432"
        ));
    }

    public static PostgresqlContainer getInstance() {
        if (container == null) {
            container = new PostgresqlContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
        String jdbcUrl = getJdbcUrl();
        String username = getUsername();
        String password = getPassword();
        Flyway flyway = Flyway.configure()
            .defaultSchema("juror_mod")
            .table("schema_history")
            .dataSource(jdbcUrl, username, password)
            .schemas("juror_dashboard", "juror_mod", "juror_eric")
            .load();
        flyway.migrate();
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
