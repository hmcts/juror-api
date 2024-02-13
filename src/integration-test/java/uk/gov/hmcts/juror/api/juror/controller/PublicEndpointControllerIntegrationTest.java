package uk.gov.hmcts.juror.api.juror.controller;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.TestUtil;
import uk.gov.hmcts.juror.api.bureau.domain.QSystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.config.public_.PublicJWTPayload;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.juror.service.StraightThroughType;
import uk.gov.service.notify.NotificationClientApi;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Public endpoint integration tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "notify.disabled=false"
)
@SuppressWarnings("PMD.LawOfDemeter")
public class PublicEndpointControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private TestRestTemplate template;
    private HttpHeaders httpHeaders;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemParameterRepository systemParameterRepository;

    @SpyBean
    private NotificationClientApi notificationClientApi;

    @Value("${jwt.secret.public}")
    private String publicSecret;

    private Date DOB_40_YEARS_OLD;
    private JurorResponseDto.Qualify VALID_QUALIFY;

    private int youngestJurorAgeAllowed;
    private int tooOldJurorAge;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        //create a valid DOB
        DOB_40_YEARS_OLD = Date.from(LocalDate.now().minusYears(40L).atStartOfDay().toInstant(ZoneOffset.UTC));

        //create a valid Qualify
        VALID_QUALIFY = JurorResponseDto.Qualify.builder()
            .convicted(JurorResponseDto.Answerable.builder().answer(false).build())
            .livedConsecutive(JurorResponseDto.Answerable.builder().answer(true).build())
            .mentalHealthAct(JurorResponseDto.Answerable.builder().answer(false).build())
            .onBail(JurorResponseDto.Answerable.builder().answer(false).build())
            .build();
    }

    //@Test(expected = InvalidJwtAuthenticationException.class)
    @Test
    public void retrieveJurorById_unhappy_header1() {
        final String description = "Authentication header is not present";

        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(
            new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/public/juror/123456789")),
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(InvalidJwtAuthenticationException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Authentication header may not be empty!");
    }

    @Test
    public void retrieveJurorById_unhappy_header2() throws Exception {
        final String description = "Authentication header is empty";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, null);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(
            new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/public/juror/123456789")),
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(InvalidJwtAuthenticationException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Authentication header may not be empty!");
    }

    @Test
    public void retrieveJurorById_unhappy_header3() throws Exception {
        final String description = "Authentication header is invalid";

        final String publicJwt = mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("123456789")
            .postcode("")
            .surname("")
            .roles(new String[]{"juror"})
            .id("")
            .build());

        final String[] jwtSections = publicJwt.split("\\.");
        final String invalidPublicJwt = String.join(".", jwtSections[0], "eyJhZG1pbiI6ICJ0cnVlIn0", jwtSections[2]);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, invalidPublicJwt);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(
            new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/public/juror/123456789")),
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(InvalidJwtAuthenticationException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Failed to parse JWT");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/PublicEndpointControllerTest_retrieveJurorById.sql")
    public void retrieveJurorById_RequestWithValidNumber_ReturnsJurorDetails() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("209092530")
            .postcode("AB3 9RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        ResponseEntity<JurorDetailDto> exchange = template.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/public/juror/209092530")), JurorDetailDto.class);
        assertThat(exchange.getBody()).extracting("jurorNumber", "title", "firstName", "lastName", "postcode")
            .contains("209092530", "Dr", "Jane", "CASTILLO", "AB39RY");
    }

    /**
     * A JUROR.UNIQUE_POOL entry with ATTEND_TIME set overrides the LOC_ATTEND_TIME column in JUROR_MOD.COURT_LOCATION
     *
     * @throws Exception if the test falls over
     * @since JDB-2042
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/PublicEndpointControllerTest_retrieveJurorById_poolAttendTime.sql")
    public void retrieveJurorById_alternatePath_poolAttendTime() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("209092530")
            .postcode("AB3 9RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        ResponseEntity<JurorDetailDto> exchange = template.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/public/juror/209092530")), JurorDetailDto.class);
        assertThat(exchange.getBody()).extracting("jurorNumber", "title", "firstName", "lastName", "postcode")
            .contains("209092530", "Dr", "Jane", "CASTILLO", "AB39RY");

        final String courtAttendTime = jdbcTemplate.queryForObject("SELECT LOC_ATTEND_TIME FROM JUROR_MOD.COURT_LOCATION WHERE LOC_CODE='407'",String.class);
        final String poolAttendTime = jdbcTemplate.queryForObject("SELECT ATTEND_TIME FROM JUROR.UNIQUE_POOL",String.class);

        assertThat(exchange.getBody().getCourtAttendTime()).isNotNull();
        assertThat(exchange.getBody().getCourtAttendTime()).isNotEqualTo(courtAttendTime);
        assertThat(exchange.getBody().getCourtAttendTime()).isEqualTo(poolAttendTime);

        assertThat(exchange.getBody().getCourtAttendTime()).contains("10:30").doesNotContain("09:30");
    }

    @Test
    public void retrieveJurorById_InvalidNumberRequest_ReturnsUnauthorizedErrorMessage() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("209092530")
            .postcode("AB3 9RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        ResponseEntity<String> exchange = template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/public/juror/12345")), String.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getBody()).contains("Unauthorized");
        assertThat(exchange.getBody()).contains("InvalidJwtAuthenticationException");

    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_happy() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "Joseph", "Dredd", "123456 Pleasant Walk",
                "Cube Four",
                "Block 871",
                "M1 1AB", DOB_40_YEARS_OLD,
                "012341234567", "dredd@megaone.web", VALID_QUALIFY, null)
            .title("Judge")
            .specialNeeds(Collections.singletonList(JurorResponseDto.SpecialNeed.builder()
                .assistanceType("V")
                .assistanceTypeDetails("Helmet visor tinted and cannot remove even indoors")
                .build())
            )
            .cjsEmployment(Collections.singletonList(JurorResponseDto.CJSEmployment.builder()
                .cjsEmployer("Mega City 1 Hall of Justice")
                .cjsEmployerDetails("I am the law.")
                .build())
            )
            .build();

        //assert response tables are in known state
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(exchange.getBody()).isNotBlank().asString().isEqualTo("Saved");

        //assert database has response
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(1);

        assertThat(jdbcTemplate.queryForObject("select LAST_NAME from JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("Dredd");
        assertThat(jdbcTemplate.queryForObject("select CJS_EMPLOYER from JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT WHERE JUROR_NUMBER = '644892530'", String.class)).contains("Mega City 1");
        assertThat(jdbcTemplate.queryForObject("select SPEC_NEED from JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("V");
    }

    /**
     * Backend test for the first-party aspect of the JDB-1937 bug
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    public void respondToSummons_unhappy_tooOld() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("352004504")
            .postcode("AC3M 2NY")
            .surname("RIVERA")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        Date dob = Date.from(LocalDateTime.now().minusYears(90).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.realBuilder()
            .jurorNumber("352004504")
            .firstName("Jose")
            .lastName("Rivera")
            .title("Rev")
            .addressLineOne("22177 Redwing Way")
            .addressLineTwo("England")
            .addressLineThree("London")
            .addressTown("United Kingdom")
            .addressCounty("")
            .addressPostcode("EC3M 2NY")
            .dateOfBirth(dob)
            .primaryPhone("11111111111")
            .secondaryPhone("00000000000")
            .emailAddress("email@email.com")
            .build();

        //assert response tables are in known state
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(exchange.getBody()).isNotBlank().asString().isEqualTo("Saved");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_straightThroughTooYoung_unhappy.sql")
    public void respondToSummons_unhappy_failedAgeCheckOnStraightThrough() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        Date dob = Date.from(LocalDateTime.now().minusYears(17).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .build();

        //assert response tables are in known state
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(exchange.getBody()).isNotBlank().asString().isEqualTo("Saved");

        //assert database has response
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE POOL_NO= '644892530'", Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_isSuperUrgentFailedStraightThrough_unhappy.sql")
    public void respondToSummons_unhappy_failedSuperUrgentCheckOnStraightThrough() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        Date dob = Date.from(LocalDateTime.now().minusYears(36).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .build();

        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        template.exchange(requestEntity, String.class);

        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE POOL_NO='644892530'", Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_noEmailOrPhoneNumber() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "Joseph", "Dredd", "123456 Pleasant Walk",
                "Cube Four",
                "Block 871",
                "M1 1AB", DOB_40_YEARS_OLD,
                null, null, VALID_QUALIFY, null)
            .title("Judge")
            .build();

        //assert response tables are in known state
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //assert no changes made to response tables
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_happy_successfulStraightThroughAcceptance() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        Date dob = Date.from(LocalDateTime.now().minusYears(36).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.ACCEPTANCE.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("CLOSED");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("Y");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select HISTORY_CODE from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", String.class)).isEqualTo("RESP");
        assertThat(jdbcTemplate.queryForObject("select USER_ID from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", String.class)).isEqualTo(JurorDigitalApplication.AUTO_USER);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).as("Only the DOB audits a change.").isEqualTo(1);
        assertNullExcusalDate();

        Mockito.verify(this.notificationClientApi).sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_happy_disabledStraightThroughAcceptance() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        Date dob = Date.from(LocalDateTime.now().minusYears(36).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .qualify(JurorResponseDto.Qualify.builder()
                .livedConsecutive(JurorResponseDto.Answerable.builder().answer(true).build()).build())
            .build();

        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.ACCEPTANCE.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(1);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        template.exchange(requestEntity, String.class);

        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
    }


    /**
     * Asserts that the excusal date is null when there isn't an excusal
     *
     * @since JDB-1902
     */
    private void assertNullExcusalDate() {
        assertThat(jdbcTemplate.queryForObject("select DATE_EXCUS FROM JUROR.POOL WHERE PART_NO='644892530'", Date.class)).isNull();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_straightThroughAcceptance_cjsEmployed() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        Date dob = Date.from(LocalDateTime.now().minusYears(36).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .cjsEmployment(Collections.singletonList(
                    JurorResponseDto.CJSEmployment.builder()
                        .cjsEmployer("police")
                        .cjsEmployerDetails("I am invalid because I am a forensic examiner")
                        .build()
                )
            ).build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.ACCEPTANCE.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);//summoned
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);//summoned
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(HISTORY_CODE) from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", Integer.class))
                .describedAs("No code history audit as straight through did not happen").isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(USER_ID) from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", Integer.class))
                .describedAs("No user id history audit as straight through did not happen").isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD", Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_straightThroughAcceptance_specialNeed() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        Date dob = Date.from(LocalDateTime.now().minusYears(36).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .specialNeeds(Collections.singletonList(
                JurorResponseDto.SpecialNeed.builder()
                    .assistanceType("I")
                    .assistanceTypeDetails("I have a nut allergy")
                    .build()
            ))
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.ACCEPTANCE.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);//summoned
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);//summoned
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(HISTORY_CODE) from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", Integer.class))
                .describedAs("No code history audit as straight through did not happen").isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(USER_ID) from JUROR.PART_HIST WHERE OTHER_INFORMATION='Responded' and PART_NO='644892530'", Integer.class))
                .describedAs("No user id history audit as straight through did not happen").isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD", Integer.class)).isEqualTo(0);
    }

//     @Test
//     @Sql("/db/truncate.sql")
//     @Sql("/db/standing_data.sql")
//     @Sql("/db/app_settings.sql")
//     @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
//     public void respondToSummons_happy_thirdPartyDeceased_successfulStraightThrough() throws Exception {

//         final URI uri = URI.create("/api/v1/public/juror/respond");
//         httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
//                 .jurorNumber("644892530")
//                 .postcode("AB39RY")
//                 .surname("CASTILLO")
//                 .roles(new String[]{"juror"})
//                 .id("")
//                 .build())
//         );

//         JurorResponseDto.ThirdParty thirdParty = new JurorResponseDto.ThirdParty();

//         // note the contact preference fields are absent in a deceased flow
//         thirdParty.setThirdPartyFName("Joe");
//         thirdParty.setThirdPartyLName("Bloggs");
//         thirdParty.setRelationship("Brother");
//         thirdParty.setThirdPartyReason("Deceased");
//         thirdParty.setThirdPartyOtherReason("");
//         thirdParty.setMainPhone("01234123456");
//         thirdParty.setOtherPhone("07890654321");
//         thirdParty.setEmailAddress("thirdparty@deceased.flow");

//         final JurorResponseDto dto = JurorResponseDto.builderThirdPartyDeceased("644892530", thirdParty)
//                 .build();

//         assertThat(jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)
//         ).isEqualTo(0);

//         RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
//         ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

//         assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//         assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)
//         ).isEqualTo(1);
//         assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
//         String.class)).isEqualTo("CLOSED");
//         assertThat(jdbcTemplate.queryForObject("select THIRDPARTY_REASON from JUROR_DIGITAL.JUROR_RESPONSE",
//         String.class)).isEqualToIgnoringCase("deceased");
//         assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("Y");
//         assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(5);
//         assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE PART_NO='644892530'", Integer.class)).isEqualTo(1);
//         assertThat(jdbcTemplate.queryForObject("select r.ZIP from JUROR_DIGITAL.JUROR_RESPONSE r WHERE JUROR_NUMBER=644892530", String.class))
//                 .describedAs("(JDB-1879)Postcode was loaded correctly from POOL during response persistence")
//                 .isEqualTo("AB3 9RY");

//         Mockito.verify(this.notificationClientApi).sendEmail(anyString(), anyString(), anyMap(), anyString());
//     }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/straight_through_deceased_excusal_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_happy_thirdPartyDeceased_disabledStraightThrough() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        JurorResponseDto.ThirdParty thirdParty = new JurorResponseDto.ThirdParty();

        // note the contact preference fields are absent in a deceased flow
        thirdParty.setThirdPartyFName("Joe");
        thirdParty.setThirdPartyLName("Bloggs");
        thirdParty.setRelationship("Brother");
        thirdParty.setThirdPartyReason("Deceased");
        thirdParty.setThirdPartyOtherReason("");
        thirdParty.setMainPhone("01234123456");
        thirdParty.setOtherPhone("07890654321");
        thirdParty.setEmailAddress("thirdparty@deceased.flow");

        final JurorResponseDto dto = JurorResponseDto.builderThirdPartyDeceased("644892530", thirdParty).build();

        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        // there is no straight through enabled, so expect TO-DO
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select THIRDPARTY_REASON from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualToIgnoringCase("deceased");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE POOL_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select r.ZIP from JUROR_DIGITAL.JUROR_RESPONSE r WHERE JUROR_NUMBER='644892530'", String.class))
                .describedAs("(JDB-1879)Postcode was loaded correctly from POOL during response persistence")
                .isEqualTo("AB3 9RY");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_thirdPartyDeceased_validationFail() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        JurorResponseDto.ThirdParty thirdParty = new JurorResponseDto.ThirdParty();

        // note the contact preference fields are absent in a deceased flow
        thirdParty.setThirdPartyFName(null);
        thirdParty.setThirdPartyLName(null);
        thirdParty.setRelationship(null);
        thirdParty.setThirdPartyReason("Deceased");
        thirdParty.setThirdPartyOtherReason("");
        thirdParty.setMainPhone(null);
        thirdParty.setOtherPhone(null);
        thirdParty.setEmailAddress(null);

        final JurorResponseDto dto = JurorResponseDto.builderThirdPartyDeceased("644892530", thirdParty).build();

        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exchange.getBody().getErrors())
            .hasSize(4)
            .extracting("field")
            .containsExactlyInAnyOrder("thirdParty.thirdPartyFName", "thirdParty.thirdPartyLName", "thirdParty",
                "thirdParty.relationship");
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE POOL_NO='644892530'", Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.ExcusalDeceasedStraightThrough_unhappy_superurgent.sql")
    public void respondToSummons_unhappy_thirdPartyDeceased_validationFail_superUrgent() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        JurorResponseDto.ThirdParty thirdParty = new JurorResponseDto.ThirdParty();
        thirdParty.setMainPhone("01234123456");
        thirdParty.setOtherPhone("07654888999");
        thirdParty.setEmailAddress("alpha.fox@thirdparty.test");
        thirdParty.setRelationship("Brother");
        thirdParty.setThirdPartyFName("Alpha");
        thirdParty.setThirdPartyLName("Fox");
        thirdParty.setThirdPartyReason("The person has died");
        thirdParty.setThirdPartyReason("Deceased");

        final JurorResponseDto dto = JurorResponseDto.builderThirdPartyDeceased("644892530", thirdParty).build();

        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE OTHER_INFORMATION='ADD Excuse - D' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    public void respondToSummons_happy_ageExcusal_successfulStraightThrough_young() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        // Hearing Date is set to (via sql): relative to current day (60 days ahead)
        LocalDateTime hearingDate = LocalDateTime.now().plus(60, ChronoUnit.DAYS);

        // set Juror to be one day too young on first day of hearing
        String youngestJurorAgeAllowedString = systemParameterRepository.findOne(
            QSystemParameter.systemParameter.spId.eq(101)).get().getSpValue();
        youngestJurorAgeAllowed = Integer.parseInt(youngestJurorAgeAllowedString);
        Date dob =
            Date.from(hearingDate.minusYears(youngestJurorAgeAllowed - 1L).minusDays(364).atZone(ZoneId.systemDefault())
                .toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.AGE_EXCUSAL.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE", String.class)).isEqualTo("CLOSED");
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE RESPONDED='Y' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE DISQ_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE STATUS='6' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='PDIS' and OTHER_INFORMATION='Disqualify Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='RDIS' and OTHER_INFORMATION='Disqualify Letter Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.DISQ_LETT WHERE DISQ_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
    }

    @Test
    @Ignore
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    public void respondToSummons_happy_ageExcusal_successfulStraightThrough_old() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        // Hearing Date is set to (via sql): relative to current day (60 days ahead)
        LocalDateTime hearingDate = LocalDateTime.now().plus(60, ChronoUnit.DAYS);

        // set Juror to be too old on first day of hearing
        //String tooOldJurorAgeString = systemParameterRepository.findOne(100).getSpValue();
        String tooOldJurorAgeString = systemParameterRepository.findOne(
            QSystemParameter.systemParameter.spId.eq(100)).get().getSpValue();

        tooOldJurorAge = Integer.parseInt(tooOldJurorAgeString);
        Date dob =
            Date.from(hearingDate.minusYears(tooOldJurorAge).minusDays(0).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.AGE_EXCUSAL.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE", String.class)).isEqualTo("CLOSED");
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE RESPONDED='Y' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE DISQ_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE STATUS='6' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='PDIS' and OTHER_INFORMATION='Disqualify Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='RDIS' and OTHER_INFORMATION='Disqualify Letter Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.DISQ_LETT WHERE DISQ_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(1);

        Mockito.verify(this.notificationClientApi).sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/straight_through_deceased_excusal_disabled.sql")
    @Sql("/db/straight_through_age_excusal_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    public void respondToSummons_happy_ageExcusal_straightThroughDisabled() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        // Hearing Date is set to (via sql): relative to current day (60 days ahead)
        LocalDateTime hearingDate = LocalDateTime.now().plus(60, ChronoUnit.DAYS);

        // set Juror to be too young on first day of hearing
        String youngestJurorAgeAllowedString = systemParameterRepository.findOne(
            QSystemParameter.systemParameter.spId.eq(101)).get().getSpValue();
        youngestJurorAgeAllowed = Integer.parseInt(youngestJurorAgeAllowedString);
        Date dob =
            Date.from(hearingDate.minusYears(youngestJurorAgeAllowed - 1L).minusDays(0).atZone(ZoneId.systemDefault())
                .toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.AGE_EXCUSAL.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE POOL_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE", String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE RESPONDED='Y' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE EXC_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE STATUS='6' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='PDIS' and OTHER_INFORMATION='Disqualify Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='RDIS' and OTHER_INFORMATION='Disqualify Letter Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.DISQ_LETT WHERE DISQ_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
    }

    @Test
    @Ignore
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/straight_through_deceased_excusal_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    public void respondToSummons_unhappy_ageExcusal_notExcused_exactlyMinimumAge() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        // Hearing Date is set to (via sql): relative to current day (60 days ahead)
        LocalDateTime hearingDate = LocalDateTime.now().plus(60, ChronoUnit.DAYS);

        // set Juror to be the minimum age allowed on first day of hearing
        String youngestJurorAgeAllowedString = systemParameterRepository.findOne(
            QSystemParameter.systemParameter.spId.eq(101)).get().getSpValue();
        youngestJurorAgeAllowed = Integer.parseInt(youngestJurorAgeAllowedString);
        Date dob =
            Date.from(hearingDate.minusYears(youngestJurorAgeAllowed).minusDays(0).atZone(ZoneId.systemDefault())
                .toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.AGE_EXCUSAL.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE POOL_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE", String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE RESPONDED='Y' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE EXC_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE STATUS='6' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='PDIS' and OTHER_INFORMATION='Disqualify Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='RDIS' and OTHER_INFORMATION='Disqualify Letter Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.DISQ_LETT WHERE DISQ_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/straight_through_deceased_excusal_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    public void respondToSummons_unhappy_ageExcusal_notExcused_exactlyMaximumAge() throws Exception {


        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        // Hearing Date is set to (via sql): relative to current day (60 days ahead)
        LocalDateTime hearingDate = LocalDateTime.now().plus(60, ChronoUnit.DAYS);

        //     set Juror to be 1 day off from excusal age
        String tooOldJurorAgeString = systemParameterRepository.findOne(
            QSystemParameter.systemParameter.spId.eq(100)).get().getSpValue();
        tooOldJurorAge = Integer.parseInt(tooOldJurorAgeString);
        Date dob =
            Date.from(
                hearingDate.minusYears(tooOldJurorAge - 1L).minusDays(364).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.APP_SETTINGS where SETTING='" +
            StraightThroughType.AGE_EXCUSAL.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE POOL_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE", String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE RESPONDED='Y' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE EXC_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.POOL WHERE STATUS='6' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='PDIS' and OTHER_INFORMATION='Disqualify Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE HISTORY_CODE='RDIS' and OTHER_INFORMATION='Disqualify Letter Code A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.DISQ_LETT WHERE DISQ_CODE='A' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
     }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_EmploymentsValidation() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        Date dob = Date.from(LocalDateTime.now().minusYears(36).atZone(ZoneId.systemDefault()).toInstant());

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", VALID_QUALIFY, null)
            .title("DR")
            .cjsEmployment(Collections.singletonList(JurorResponseDto.CJSEmployment.builder()
                .cjsEmployer("Mega City 1 Hall of Justice")
                .cjsEmployerDetails("I am the law.")
                .cjsEmployer("Police Force")
                .cjsEmployerDetails("In a Police force, Since 5 years ")
                .build()))
            .build();

        assertThat(
            jdbcTemplate.queryForObject("select count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from JUROR_DIGITAL.JUROR_RESPONSE",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from JUROR.POOL", String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject("select STATUS from JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from JUROR.PART_HIST WHERE OTHER_INFORMATION='ADD Excuse - D' and PART_NO='644892530'", Integer.class)).isEqualTo(0);
    }

    /**
     * The endpoint should reject a response with no phone numbers populated
     *
     * @throws Exception if the test falls over
     * @since JDB-1968
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappy_noPhoneNumbersProvided() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .addressLineOne("3 Some Street")
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .emailAddress("t.castillo@email.com")
                        .build()
                ).build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Should reject response if 'use juror phone' is false but no third party phone is provided
     *
     * @throws Exception if the test falls over
     * @since JDB-2165
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_useThirdPartyPhoneNumberButNoneSupplied() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .primaryPhone("01234567890")
                .addressLineOne("3 Some Street")
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .emailAddress("t.castillo@email.com")
                        .useJurorPhoneDetails(false)
                        .build()
                )
                .build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Should accept response if 'use juror phone' is set and no third party phone is provided
     *
     * @throws Exception if the test falls over
     * @since JDB-2165
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_happyPath_useJurorPhoneNumber() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .primaryPhone("01234567890")
                .addressLineOne("3 Some Street")
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .emailAddress("t.castillo@email.com")
                        .useJurorPhoneDetails(true)
                        .mainPhone(null)
                        .otherPhone(null)
                        .build()
                )
                .build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    /**
     * Should not accept response if 'use juror phone' is set and no juror number is provided
     *
     * @throws Exception if the test falls over
     * @since JDB-2165
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappyPath_useJurorPhoneNumberButNoneProvided() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        JurorResponseDto.Qualify qualify = new JurorResponseDto.Qualify(); // juror must not be age disqualified
        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .primaryPhone(null)
                .addressLineOne("3 Some Street")
                .qualify(qualify)
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .emailAddress("t.castillo@email.com")
                        .useJurorPhoneDetails(true)
                        .mainPhone("12345678")
                        .build()
                )
                .build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * The endpoint should reject a third-party response containing an invalid phone number
     *
     * @throws Exception if the test falls over
     * @since JDB-2137
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappy_invalidJurorPhoneNumber() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .addressLineOne("3 Some Street")
                .primaryPhone("999999999a9")
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .emailAddress("t.castillo@email.com")
                        .useJurorPhoneDetails(true)
                        .build()
                ).build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
    }

    /**
     * The endpoint should reject a response with no emails populated
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappy_noEmailsProvided() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .addressLineOne("3 Some Street")
                .primaryPhone("123456789")
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .build()
                ).build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Should reject response if 'use juror email' is false but no third party email is provided
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappyPath_useThirdPartyEmailButNoneSupplied() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .primaryPhone("01234567890")
                .addressLineOne("3 Some Street")
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .mainPhone("123456789")
                        .useJurorEmailDetails(false)
                        .build()
                )
                .build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Should accept response if 'use juror email' is set and no third party email is provided
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_happyPath_useJurorEmail() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .primaryPhone("01234567890")
                .emailAddress("email@domain.com")
                .addressLineOne("3 Some Street")
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .mainPhone("0123456789")
                        .useJurorEmailDetails(true)
                        .build()
                )
                .build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    /**
     * Should not accept response if 'use juror email' is set and no juror email is provided
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappyPath_useJurorEmailButNoneProvided() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        JurorResponseDto.Qualify qualify = new JurorResponseDto.Qualify(); // juror must not be age disqualified
        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .primaryPhone("0123456789")
                .addressLineOne("3 Some Street")
                .qualify(qualify)
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .emailAddress("t.castillo@email.com")
                        .useJurorEmailDetails(true)
                        .mainPhone("12345678")
                        .build()
                )
                .build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * The endpoint should reject a third-party response containing an invalid email
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappy_invalidJurorEmail() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJWTPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(new SimpleDateFormat("d-MM-yyyy").parse("24-07-1984"))
                .firstName("Jane")
                .lastName("Castillo")
                .addressLineOne("3 Some Street")
                .primaryPhone("9999999999")
                .emailAddress("x")
                .thirdParty(
                    JurorResponseDto.ThirdParty.builder().thirdPartyFName("Timothy").thirdPartyLName("Castillo")
                        .thirdPartyReason("Other")
                        .thirdPartyOtherReason("My sister is on holiday")
                        .relationship("Brother")
                        .useJurorEmailDetails(true)
                        .build()
                ).build();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(jurorResponse, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            0);
    }

    private String mintPublicJwt(final PublicJWTPayload payload) throws Exception {
        return TestUtil.mintPublicJwt(payload, SignatureAlgorithm.HS256, publicSecret, Instant.now().plus(100L * 365L
            , ChronoUnit.DAYS));
    }
}
