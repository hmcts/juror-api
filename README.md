# Juror Backend API

This is the API for the Juror Modernisation project. It is a Spring Boot application that provides RESTful endpoints for
managing jurors. 

# Building and deploying the application

## Prerequisites

- [Java 17](https://www.oracle.com/java)
- [Docker](https://www.docker.com)

### Environment variables

The following application settings are required to run the application. They can be set in the
`application-local.yaml`file or as environment variables.The application will look for the environment variables first.

```
DB_PASSWORD=postgres
DB_URL=jdbc:postgresql://localhost:5432/juror
DB_USERNAME=juror
JWT_SECRET_BUREAU=
JWT_SECRET_HMAC=
JWT_SECRET_PUBLIC=
NOTIFY_KEY=
NOTIFY_REGION_KEY_1=
NOTIFY_REGION_KEY_2=
NOTIFY_REGION_KEY_3=
NOTIFY_REGION_KEY_4=
NOTIFY_REGION_KEY_5=
NOTIFY_REGION_KEY_6=
NOTIFY_REGION_KEY_7=
PNC_CHECK_SERVICE_HOST=localhost
PNC_CHECK_SERVICE_PORT=8081
PNC_CHECK_SERVICE_SECRET=
PNC_CHECK_SERVICE_SUBJECT=juror-back-end
PROXY_HOST=10.100.1.4
PROXY_PORT=3182
SMART_SURVEY_SECRET=
SMART_SURVEY_TOKEN=
```

It is possible to configure IntelliJ to use these environment variables when running the application by setting up
default configurations. This can be done by going to `Run -> Edit Configurations` and setting the environment variables

Alternatively, its possible export the environment variables in the terminal before running the application. For example:

```bash
export DB_PASSWORD=secret
export DB_URL=jdbc:postgresql://localhost:5432/juror
export DB_USERNAME=juror
......
```

## Database setup

The application requires a Postgres database.

The latest official image of Postgres can be run from Docker Hub (https://hub.docker.com/_/postgres). To install, run the following command

```bash
docker pull postgres
```

create a local data folder, e.g. in the home directory
```bash
mkdir ~/postgres
cd ~/postgres
mkdir data
```
Then to spin up a container run the following command:
```bash
docker run -d --name juror-postgres -e POSTGRES_USER=juror -e POSTGRES_PASSWORD=postgres -v ~/postgres/data:/var/lib/postgresql/data -p 5432:5432 postgres
```
From the root of the project, run the following command to create the database schema:
```bash
./gradlew flywayMigrate
```

## Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Jacoco Coverage Report

A local jacoco coverage report can be generated using the following command:-

```bash
  ./gradlew jacocoTestReport
```

The report will be available under ./build/jacocoHtml/index.html. The report incorporates both unit test
and integration test coverage

### Running the application locally via the terminal

The application can be run locally using the following command:

```bash
  ./gradlew bootRun
```

This will start the API exposing the application's port
(set to `8080` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8080/health
```

You should get a response similar to this:

```
  {"status":"UP","components":{"db":{"status":"UP","details":{"database":"PostgreSQL","validationQuery":"isValid()"}}
  ,"diskSpace":{"status":"UP","details":{"total":250685575168,"free":108429410304,"threshold":10485760,
  "path":"/Users/<user_name>/github/juror-api/.","exists":true}},"livenessState":{"status":"UP"},"ping":{"status":"UP"},
  "readinessState":{"status":"UP"}},"groups":["liveness","readiness"]}%
```

It is possible to debug the application through Intellij selecting the appropriate run configuration or through gradle.

### Swagger UI

The application has Swagger enabled. To access the Swagger UI, navigate to `http://localhost:8080/swagger-ui.html`

## Spring Profiles

The following Spring Profiles are defined. "External Components" are defined as any service upon which the application
is dependent, such as web services, Notify etc.

| Profile       | Purpose                                                            | External Components                                                                                          |
|---------------|--------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `development` | For running the application in the Pull Request (dev) environment. | Interaction permitted with "real" components, which may be services deployed to a test environment.          |
| `test`        | For running application in build pipelines                         | No interaction required or permitted, all external calls are mocked , an embedded database (for db queries)  |
| `production`  | For running application in a live environment                      | Connected to real external interfaces as per live environments                                               |


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
