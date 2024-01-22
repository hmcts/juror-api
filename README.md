
# Juror Digital - API

This is the API server for the MoJ Juror Digital project. It exposes RESTful webservices which the front end calls.


This document is work in progress and will need updating as part of the Java 17 / SpringBoot template updates.
## Technologies
* Spring Boot
* Gradle
* Maven
* Flyway
* Docker
* Oracle 10g/11g

## Pre-requisitess

### Configure Maven, Gradle and Docker proxies (GROUPINFRA-C network)

If you're running on GROUPINFRA network, you need the following proxy configurations:-

```
$ cat ~/.m2/settings.xml
<settings>
  <proxies>
    <proxy>
      <id>Logica</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>cache1.uk.logica.com</host>
      <port>80</port>
    </proxy>
  </proxies>
</settings>

$ cat ~/.gradle/gradle.properties
systemProp.http.proxyHost=cache1.uk.logica.com
systemProp.http.proxyPort=80
systemProp.https.proxyHost=cache1.uk.logica.com
systemProp.https.proxyPort=80

$ cat /etc/systemd/system/docker.service.d/http-proxy.conf
[Service]
Environment="HTTP_PROXY=http://cache1.uk.logica.com:80/"
```

(note that for Docker, the directory didn't exist. Once created, do a `systemctl daemon-reload && systemctl docker restart`
to get the changes applied to Docker.)

### Configure Gradle JDK

I also recommend you explicitly set the JDK for Gradle by adding the following to `~/.gradle/gradle.properties`:-
```
org.gradle.java.home=/usr/lib/jvm/java-1.8.0-openjdk-amd64/
```

otherwise you may get `Cannot find symbol ... JurorResponseDtoBuilder`.


### Install Oracle Driver

Install the Oracle driver to local maven (`~/.m2`) with

```
mvn install:install-file \
    -Dfile=libraries/commercial/ojdbc6.jar  \
    -DgroupId=com.oracle \
    -DartifactId=ojdbc6 \
    -Dversion=11.2.0.4 \
    -Dpackaging=jar
```

### Setting up Oracle Docker container

`docker pull wnameless/oracle-xe-11g`

Run a container and name it `juror_oracle_11g`.  Note this also mounts the dump file from the local file system
`/data/juror-dump`.

`docker run -d -v /data/juror-dump:/data/juror-dump:ro -p 1521:1521 --name juror_oracle_11g -t wnameless/oracle-xe-11g`

To import the dump:
1. Copy the `data/juror_system.dmp` file to `/data/juror-dump/juror_system.dmp`
1. Manually create the `JUROR` (not in CTC, NLE or PROD), `JUROR_DIGITAL` and `JUROR_DIGITAL_USER` accounts in the
database (default credentials are system/oracle on SID 'xe'). Example SQL can be found at [users_dev.sql](./data/users_dev.sql).
1. `docker exec -it juror_oracle_11g /bin/bash`
1. In the container as root
`imp system/oracle FROMUSER=juror TOUSER=juror file=/data/juror-dump/juror_system.dmp log=juror_system.log` (this will
take a while - on mine, it takes 1m18s)
1. You should see `Import terminated successfully with warnings` at the end of the import log.
1. Grant access to the Juror views for `JUROR_DIGITAL_USER'. Example SQL can be found at [grants.sql](./data/grants.sql).
1. Add the following stored procedure for tests into the JUROR_DIGITAL schema using Sql Developer or SQLPlus *(this is
required in the CI database too)*.
```
create or replace PROCEDURE JUROR_DIGITAL.reset_seq(p_name IN VARCHAR2, p_val IN NUMBER)
AS
  l_num NUMBER;
  BEGIN
    EXECUTE IMMEDIATE 'select ' || p_name || '.nextval from dual' INTO l_num;
    IF (p_val - l_num - 1) != 0
      THEN
        EXECUTE IMMEDIATE 'alter sequence ' || p_name || ' increment by ' || (p_val - l_num - 1) || ' minvalue 0';
    END IF;
    EXECUTE IMMEDIATE 'select ' || p_name || '.nextval from dual' INTO l_num;
    EXECUTE IMMEDIATE 'alter sequence ' || p_name || ' increment by 1 ';
  END;
```

### GOV.uk Notify configuration

In production Notify **is enabled by default**.  In development and test it is disabled by default to prevent spam.  This
can be toggled by setting the `notify.disabled=true` Spring property.

To integrate correctly with the remote Notify API an API key must be set for development and CI use.

For development place `notify.key=<<YOUR_API_KEY>>` in `~/.spring-boot-devtools.properties`.

For CI the environment variable `NOTIFY_KEY` should contain the API key value.

notify keys are in .yaml file

## Building the project

NOTE: The `flywayClean flywayMigrate` tasks are optional and set up the local database.  These tasks **are destructive!**
See the `flyway` closure in `build.gradle` for details.

`./gradlew clean flywayClean flywayMigrate build`

To run/debug locally (it doesn't detach from the console so use ctrl-c to kill when you're done):-

```bash
$ ./gradlew clean flywayClean flywayMigrate bootRun
<lots of messages snipped>
2017-06-14 16:36:00.207  INFO 4857 --- [  restartedMain] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
2017-06-14 16:36:00.250  INFO 4857 --- [  restartedMain] c.c.o.j.JurorDigitalApplication          : Started JurorDigitalApplication in 32.772 seconds (JVM running for 34.077)
```

You can check if the application is working and database connectivity by doing the following in another console:-

```bash
$ curl -k http://localhost:8080/health
{
  "status" : "UP",
  "diskSpace" : {
    "status" : "UP",
    "total" : 27345022976,
    "free" : 14594895872,
    "threshold" : 10485760
  },
  "db" : {
    "status" : "UP",
    "database" : "Oracle",
    "hello" : "Hello"
  }
```

## Application Configuration
Aspects of the application's behaviour can be configured using the `JUROR_DIGITAL` user's `APP_SETTING` table

The following environment properties must be defined:
SMART_SURVEY_TOKEN=6R1ur77SGDu8x00I6w2grAhzCL72FEQs
SMART_SURVEY_SECRET=Ou7nhRKb45uznFx
PROXY_HOST=10.100.1.4
PROXY_PORT=3182

JWT_SECRET_BUREAU=W3N1cGVyLXNlY3JldC1rZXktYnVyZWF1XVtzdXBlci1zZWNyZXQta2V5LWJ1cmVhdV1bc3VwZXItc2VjcmV0LWtleS1idXJlYXVd
JWT_SECRET_PUBLIC=W3N1cGVyLXNlY3JldC1rZXldW3N1cGVyLXNlY3JldC1rZXldW3N1cGVyLXNlY3JldC1rZXld
JWT_SECRET_HMAC=W3N1cGVyLXNlY3JldC1rZXktbG9naW5dW3N1cGVyLXNlY3JldC1rZXktbG9naW5dW3N1cGVyLXNlY3JldC1rZXktbG9naW5d

NOTIFY_KEY=juror_digital_dev_test-908b4d45-0c1f-4c2b-8e22-55e1e87808bd-9305b8a2-2d6b-4267-93d6-5e7c03a2e1f9
NOTIFY_REGION_KEY_1=dev_test_lo-aeb1d870-cd95-46e6-8f98-81c49919aadd-1b97e795-51d9-4cda-ad83-189be5d40762
NOTIFY_REGION_KEY_2=dev_test_mi-c87797e8-adba-4311-8468-9ed9d9ccab73-56801c5e-9ea1-4efb-a563-c1a3c6a5417c
NOTIFY_REGION_KEY_3=dev_test_ne-ee184a0d-473c-408c-bdc2-3cd4d0424394-f8349fd0-ee9b-40df-9092-c4bd4d7eeb89
NOTIFY_REGION_KEY_4=dev_test_nw-94551373-937b-476a-95cc-1819795ac369-7bb7aea9-e25f-489f-ae12-fbcba641d53b
NOTIFY_REGION_KEY_5=dev_test_se-ae27516d-e763-4655-80fc-ea7eb840666f-e22ba384-a7f6-4a2a-8c82-3bd17b2d384d
NOTIFY_REGION_KEY_6=dev_test_sw-939fbf31-8372-439d-80c4-ada5e441f5da-b283516f-4786-482d-8e56-d529a52b8347
NOTIFY_REGION_KEY_7=dev_test_wa-5096bac1-72ff-46c3-b5d3-72148e31a8f6-0eb54b9e-6ce5-4063-b12f-896d9d631774

DB_USERNAME=system
DB_PASSWORD=oracle
DB_URL=jdbc:oracle:thin:@localhost:1521:xe


SCHEDULER_SERVICE_SECRET=WW91clZlcnlWZXJ5VmVyeVNlY3JldEtleVRoYXRJc1NvU2VjcmV0SURvbnRFdmVuS25vd0l0
SCHEDULER_SERVICE_SUBJECT=juror-back-end
SCHEDULER_SERVICE_HOST=localhost
SCHEDULER_SERVICE_PORT=8080

PNC_CHECK_SERVICE_SECRET=WW91clZlcnlWZXJ5VmVyeVNlY3JldEtleVRoYXRJc1NvU2VjcmV0SURvbnRFdmVuS25vd0l0
PNC_CHECK_SERVICE_SUBJECT=juror-back-end
PNC_CHECK_SERVICE_HOST=localhost
PNC_CHECK_SERVICE_PORT=8081
The values above are for the development environment.

### Notify templates
* `NOTIFY_1ST_STRAIGHT_THROUGH
   NOTIFY_1ST_STRAIGHT_THROUGH_ADJ
   NOTIFY_1ST_DEFERRAL
   NOTIFY_1ST_DEFERRAL_ADJ
   NOTIFY_1ST_EXCUSAL
   NOTIFY_1ST_EXCUSAL_ADJ
   NOTIFY_1ST_DISQUALIFICATION_AGE
   NOTIFY_1ST_DISQUALIFICATION
   NOTIFY_3RD_STRAIGHT_THROUGH
   NOTIFY_3RD_STRAIGHT_THROUGH_ADJ
   NOTIFY_3RD_DEFERRAL
   NOTIFY_3RD_DEFERRAL_ADJ
   NOTIFY_3RD_EXCUSAL
   NOTIFY_3RD_EXCUSAL_ADJ
   NOTIFY_3RD_EXCUSAL_DECEASED
   NOTIFY_3RD_DISQUALIFICATION_AGE
   NOTIFY_3RD_DISQUALIFICATION
   NOTIFY_CY_1ST_STRAIGHT_THROUGH
   NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ
   NOTIFY_CY_1ST_DEFERRAL
   NOTIFY_CY_1ST_DEFERRAL_ADJ
   NOTIFY_CY_1ST_EXCUSAL
   NOTIFY_CY_1ST_EXCUSAL_ADJ
   NOTIFY_CY_1ST_DISQUALIFICATION_AGE
   NOTIFY_CY_1ST_DISQUALIFICATION
   NOTIFY_CY_3RD_STRAIGHT_THROUGH
   NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ
   NOTIFY_CY_3RD_DEFERRAL
   NOTIFY_CY_3RD_DEFERRAL_ADJ
   NOTIFY_CY_3RD_EXCUSAL
   NOTIFY_CY_3RD_EXCUSAL_ADJ
   NOTIFY_CY_3RD_EXCUSAL_DECEASED
   NOTIFY_CY_3RD_DISQUALIFICATION_AGE
   NOTIFY_CY_3RD_DISQUALIFICATION`
   Vaues should be the UUID template ID supplied in the Notify.gov admin web UI.

### Processing flows
* `STRAIGHT_THROUGH_ACCEPTANCE_DISABLED` = `TRUE` (Omission of the row will enable non-excusal straight-through processing flows)
* `STRAIGHT_THROUGH_DECEASED_EXCUSAL_DISABLED` = `TRUE` (Omission of the row will enable 3rd party deceased excusal straight-through processing flows)
* `STRAIGHT_THROUGH_AGE_EXCUSAL_DISABLED` = `TRUE` (Omission of the row will enable age based excusal straight-through processing flows)

### Search
* `SEARCH_RESULT_LIMIT_BUREAU_OFFICER` = `100`
    * Sets the maximum number of search results returned when the logged-in user is a bureau officer. Omission of the
    row results in the default value of 100 being used.
* `SEARCH_RESULT_LIMIT_TEAM_LEADER` = `250`
    * Sets the maximum number of search results returned when the logged-in user is a team leader. Omission of the row
    results in the default value of 250 being used.

### Auto-assignment
* `AUTO_ASSIGNMENT_DEFAULT_CAPACITY` = `60`
    * This sets the initial 'capacity' value displayed for each bureau officer in the backlog auto-assignment screen.
    Omission of the row results in the default value of 60 being used.

### Welsh Language support
* `WELSH_LANGUAGE_ENABLED` = `TRUE`
    * This enables responses flagged as Welsh language to receive Notify emails using the Welsh templates.
    * This enables the front end application support for switching to welsh language responses.

### System Parameters
The table `JUROR`.`SYSTEM_PARAMETERS` also features some settings that are required. The values listed below are required for the age-excusal processing to execute and define the upper and lower age constraints for excusal (the actual ages are subject to change).

| SP_ID | SP_DESC         | SP_VALUE |
|:-----:|:---------------:|:--------:|
| 100   | Upper Age Limit | 76       |
| 101   | Lower Age Limit | 18       |

### Auto processing user
The `JUROR_DIGITAL`.`STAFF` table should have a user dedicated for auto processing
`INSERT INTO JUROR_DIGITAL.STAFF (LOGIN, NAME, ACTIVE, RANK, VERSION) VALUES ('AUTO', 'AUTO', 1, -1, 0);`.

## Api Docs

Start the API application.

`http://localhost:8080/swagger-ui.html`

Static documentation can be generated by running the `asciidoctor` task.  To generate only the documents `./gradlew clean test --tests *Swagger2* asciidoctor`

## DevOps

> ### Using the release plugin in CI build
>
> Ensure Jenkins environment provides the property `JENKINS_CI_BUILD` with any value to enable CI specific settings.
>
> `release.useAutomaticVersion` is used to ensure the versioning process is non-interactive in the terminal.
>
> E.g: `./gradlew release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=1.0.0 -Prelease.newVersion=1.1.0-SNAPSHOT`
> See https://github.com/researchgate/gradle-release

> ### Inspection endpoints
> `/health` - health check
>
> `/info` - git and version information

> ### Docker deployments
>
> Overriding Spring Boot configuration can be done by appending command line arguments to be passed to the container.
> Use the dot syntax notation - E.g. `-Dspring.datasource.username=system`
>
> ### Development mode:
>
> Remote debug of the Java application is available on port 5005.
>
> ```
> docker run -p 8080:8080 -p 5005:5005 --name juror_test_container --rm juror-test \
>   -Dspring.profiles.active=development \
>   -Dspring.datasource.url=jdbc:oracle:thin:@172.17.0.1:1521:xe \
>   -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n
>```
>
> ### Production mode (profile is implied):
>
> `docker run -p 8080:8080 --name juror_test_container juror-test`
>
> Overrides to provide datasource, secrets and other configuration options can be supplied as
> system or environment variables using the dot syntax.
>
> ### Flyway versioning
>
> - `V1` is database state for BETA Private
> - `V2` is database state for BETA Public Trial
> - `V3` is database state for BETA Public Trial + Work Allocation
> - `V4` to `V6` is BETA Public 2.1
> - `V7` is BETA Public 2.2

> ### Runtime Configuration
>
> Set the following variables.
> (These can be configured using the following syntax: Property, YAML, linux environment underscored).  See
https://docs.spring.io/spring-boot/docs/1.5.2.RELEASE/reference/html/boot-features-external-config.html
for detailed property setting options (SPRING_APPLICATION_JSON env var is useful to set values that cannot be expressed
in UNIX env var format).
>
> |Property|Type|Default|Example|
> |:-------|:---|:------|-------|
> |spring.profiles.active|String|*null*|`production`|
> |logging.file|String|`${HOME}/juror-digital.log`|`/opt/juror-digital/juror-digital.log`
> |jwt.secret.hmac|Base64|*null*|`ZXhhbXBsZSB0ZXh0`|
> |jwt.secret.private|Base64|*null*|`ZXhhbXBsZSB0ZXh0`|
> |jwt.secret.public|Base64|*null*|`ZXhhbXBsZSB0ZXh0`|
> |spring.datasource.url|String|*null*|`jdbc:oracle:thin:@localhost:1521:xe`|
> |spring.datasource.username|String|*null*|`username`|
> |spring.datasource.password|String|*null*|`Passw0rd1!`|
> |notify.disabled|boolean|*false*|`true`|
> |notify.key|String|*null*|`somekey-12345678-90ab-cdef-1234-567890abcdef-12345678-90ab-cdef-1234-567890abcdef`|
> |notify.proxy.enabled|boolean|*false*|`true`|
> |notify.proxy.host|String|*null*|`http://proxy-domain.com`|
> |notify.proxy.port|Integer|0|12345|
> |notify.proxy.type|String|*null*|One of `DIRECT`,`HTTP`,`SOCKS`|
>
> Example command line. (Refer to the Spring Boot external config doc above for options).
> `java -jar -Djava.security.egd=file:/dev/./urandom jarfilename.jar`
>
>|notify keys in .yml file|
>
