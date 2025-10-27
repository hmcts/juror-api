package uk.gov.hmcts.juror.api.juror.controller;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
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
import uk.gov.hmcts.juror.api.config.public1.PublicJwtPayload;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.service.StraightThroughType;
import uk.gov.hmcts.juror.api.moj.domain.AppSetting;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCode;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.repository.AppSettingRepository;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;
import uk.gov.service.notify.NotificationClientApi;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Public endpoint integration tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "notify.disabled=false")
@SuppressWarnings({"PMD.ExcessiveImports","PMD.TooManyMethods", "PMD.TooManyFields"})
public class PublicEndpointControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private TestRestTemplate template;
    private HttpHeaders httpHeaders;

    @Autowired
    private JurorRepository jurorRepository;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    @Autowired
    private CourtLocationRepository courtLocationRepository;

    @Autowired
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepositoryMod;

    @Autowired
    private JurorResponseService jurorResponseService;

    @Autowired
    private JurorResponseCjsEmploymentRepositoryMod jurorResponseCjsEmploymentRepositoryMod;

    @Autowired
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;

    @Autowired
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepositoryMod;

    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;

    @Autowired
    private AppSettingRepository appSettingRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemParameterRepository systemParameterRepository;

    @SpyBean
    private NotificationClientApi notificationClientApi;

    @Value("${jwt.secret.public}")
    private String publicSecret;

    private LocalDate dob40YearsOld;
    private JurorResponseDto.Qualify validQualify;

    private int youngestJurorAgeAllowed;
    private int tooOldJurorAge;

    @Before
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        //create a valid DOB
        dob40YearsOld = LocalDate.now().minusYears(40L);

        //create a valid Qualify
        validQualify = JurorResponseDto.Qualify.builder()
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

        final String publicJwt = mintPublicJwt(PublicJwtPayload.builder()
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
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getBody().getStatus()).isEqualTo(500);
        assertThat(exchange.getBody().getException()).isEqualTo(InvalidJwtAuthenticationException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Failed to parse JWT");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/PublicEndpointControllerTest_retrieveJurorById.sql")
    public void retrieveJurorById_RequestWithValidNumber_ReturnsJurorDetails() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
     * A JUROR_MOD.POOL entry with ATTEND_TIME set overrides the LOC_ATTEND_TIME column in JUROR_MOD.COURT_LOCATION
     *
     * @throws Exception if the test falls over
     * @since JDB-2042
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/PublicEndpointControllerTest_retrieveJurorById_poolAttendTime.sql")
    public void retrieveJurorById_alternatePath_poolAttendTime() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("209092530")
            .postcode("AB3 9RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        ResponseEntity<JurorDetailDto> exchange = template.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/public/juror/209092530")), JurorDetailDto.class);

        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody()).extracting("jurorNumber", "title", "firstName", "lastName", "postcode")
            .contains("209092530", "Dr", "Jane", "CASTILLO", "AB39RY");

        executeInTransaction(() -> {
            Optional<CourtLocation> courtLocation = courtLocationRepository.findByLocCode("407");
            assertThat(courtLocation).isPresent();
            final LocalTime courtAttendTime = courtLocation.get().getCourtAttendTime();

            Optional<PoolRequest> poolRequest = poolRequestRepository.findByPoolNumber("101000000");
            assertThat(poolRequest).isPresent();
            final LocalDateTime poolAttendTime = poolRequest.get().getAttendTime();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String attendTime = exchange.getBody().getCourtAttendTime();
            assertThat(attendTime).isNotNull();
            assertThat(attendTime).isNotEqualTo(dateTimeFormatter.format(courtAttendTime));
            assertThat(attendTime).isEqualTo(dateTimeFormatter.format(poolAttendTime));

            assertThat(attendTime).contains("10:30").doesNotContain("09:30");
        });
    }

    @Test
    public void retrieveJurorById_InvalidNumberRequest_ReturnsUnauthorizedErrorMessage() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_happy() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
                "M1 1AB", dob40YearsOld,
                "012341234567", "dredd@megaone.web", validQualify, null, ReplyMethod.DIGITAL)
            .title("Judge")
            .reasonableAdjustments(Collections.singletonList(JurorResponseDto.ReasonableAdjustment.builder()
                .assistanceType("V")
                .assistanceTypeDetails("Helmet visor tinted and cannot remove even indoors")
                .build())
            )
            .cjsEmployment(Collections.singletonList(JurorResponseDto.CjsEmployment.builder()
                .cjsEmployer("Mega City 1 Hall of Justice")
                .cjsEmployerDetails("I am the law.")
                .build())
            )
            .build();

        //assert response tables are in known state
        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(exchange.getBody()).isNotBlank().asString().isEqualTo("Saved");

        executeInTransaction(() -> {
            //assert database has response
            checkSingleResponseCreated();

            // get the first response and check fields
            Iterable<DigitalResponse> responseList = jurorDigitalResponseRepositoryMod.findAll();
            DigitalResponse response = responseList.iterator().next();
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());
            assertThat(response.getJurorNumber()).isEqualTo("644892530");
            assertThat(response.getFirstName()).isEqualTo("Joseph");
            assertThat(response.getLastName()).isEqualTo("Dredd");

            // check cjs employment and reasonable adjustment entries
            List<JurorResponseCjsEmployment> cjsEmploymentList = jurorResponseCjsEmploymentRepositoryMod.findAll();
            assertThat(cjsEmploymentList).hasSize(1);
            Iterable<JurorReasonableAdjustment> reasonableAdjustmentList = jurorReasonableAdjustmentRepository
                                                                                                        .findAll();
            assertThat(reasonableAdjustmentList).hasSize(1);
            JurorResponseCjsEmployment cjsEmployment = cjsEmploymentList.get(0);
            assertThat(cjsEmployment.getCjsEmployer()).isEqualTo("Mega City 1 Hall of Justice");
            JurorReasonableAdjustment reasonableAdjustment = reasonableAdjustmentList.iterator().next();
            assertThat(reasonableAdjustment.getReasonableAdjustment().getCode()).isEqualTo("V");

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            assertThat(juror.get().isResponseEntered()).isTrue();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            // not a straight through so status should be SUMMONED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

            // check the response submitted history is present for juror
            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                h.getHistoryCode().equals(HistoryCodeMod.RESPONSE_SUBMITTED)).findFirst();
            assertThat(historyRecord).isPresent();
            assertThat(historyRecord.get().getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(historyRecord.get().getOtherInformation()).isEqualTo("Digital");
        });
    }

    /**
     * Backend test for the first-party aspect of the JDB-1937 bug.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_tooOld() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final LocalDate dob = LocalDate.now().minusYears(90);

        final JurorResponseDto dto = JurorResponseDto.realBuilder()
            .jurorNumber("644892530")
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
            .primaryPhone("07112233445")
            .secondaryPhone("02334455667")
            .emailAddress("email@email.com")
            .build();

        //assert response tables are in known state
        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(exchange.getBody()).isNotBlank().asString().isEqualTo("Saved");

        executeInTransaction(() -> {
            // verify the response was saved and auto-processed
            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);

            // check all the juror details posted were saved correctly in response
            checkJurorDetailsInResponse(response, dob);

            // verify the juror record was updated
            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.isResponded()).isTrue();
            assertThat(jurorRecord.isResponseEntered()).isTrue();
            assertThat(jurorRecord.getDisqualifyCode()).isEqualTo(DisqualifyCode.A.getCode());
            assertThat(jurorRecord.getDisqualifyDate()).isEqualTo(LocalDate.now());

            // check juror details were saved correctly in juror record
            checkJurorDetailsInJurorRecord(jurorRecord, dob);

        });
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_straightThroughTooYoung_unhappy.sql")
    public void respondToSummons_unhappy_failedAgeCheckOnStraightThrough() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        LocalDate dob = LocalDate.now().minusYears(17);

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .build();

        //assert response tables are in known state
        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(exchange.getBody()).isNotBlank().asString().isEqualTo("Saved");

        executeInTransaction(() -> {
            //assert database has response
            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());
            assertThat(response.getDateOfBirth()).isEqualTo(dob);
            // processing status should be "to do" as the straight through failed
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            final Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isEqualTo(dob);
            assertThat(jurorRecord.isResponded()).isFalse();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555111111");
            assertThat(jurorPool).isNotNull();
            // not a straight through so status should be SUMMONED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

            // check the response submitted history is present for juror
            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                                        h.getHistoryCode().equals(HistoryCodeMod.RESPONSE_SUBMITTED)).findFirst();
            assertThat(historyRecord).isPresent();
            assertThat(historyRecord.get().getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(historyRecord.get().getOtherInformation()).isEqualTo("Digital");
        });
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_isSuperUrgentFailedStraightThrough_unhappy.sql")
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void respondToSummons_unhappy_failedSuperUrgentCheckOnStraightThrough() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        LocalDate dob = LocalDate.now().minusYears(36);

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .build();

        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        template.exchange(requestEntity, String.class);

        executeInTransaction(() -> {
            //assert database has response
            checkSingleResponseCreated();

            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());
            assertThat(response.getDateOfBirth()).isEqualTo(dob);
            // processing status should be "to do" as the straight through failed
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isNotEqualTo(dob); // juror DOB should not have been updated
            assertThat(jurorRecord.isResponded()).isFalse();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555222222");
            assertThat(jurorPool).isNotNull();
            // not a straight through so status should be SUMMONED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

            // check the response submitted history is present for juror
            List<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            assertThat(history.size()).isEqualTo(1);
            Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                                           h.getHistoryCode().equals(HistoryCodeMod.RESPONSE_SUBMITTED)).findFirst();
            assertThat(historyRecord).isPresent();
            assertThat(historyRecord.get().getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(historyRecord.get().getOtherInformation()).isEqualTo("Digital");
        });
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_noEmailOrPhoneNumber() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
                "M1 1AB", dob40YearsOld,
                null, null, validQualify, null, ReplyMethod.DIGITAL)
            .title("Judge")
            .build();

        //assert response tables are in known state
        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //assert no changes made to response tables
        checkResponseTablesEmpty();
        // check the response submitted history is empty for juror
        List<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
        assertThat(history).isEmpty();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_happy_successfulStraightThroughAcceptance() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        LocalDate dob = LocalDate.now().minusYears(36);

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .build();

        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {
            checkSingleResponseCreated();

            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            // processing status should be closed as the straight through succeeded
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isEqualTo(dob); // juror DOB should have been updated
            assertThat(jurorRecord.isResponded()).isTrue();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            // not a straight through so status should be SUMMONED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.RESPONDED.getStatus());

            checkResponseChildTablesEmpty();

            // check the response submitted history is present for juror
            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            // change of DOB, response submitted and responded histories should be present
            assertThat(history.size()).isEqualTo(3);
            checkResponseSubmittedHistory(history);
            checkRespondedHistory(history);

            Iterable<JurorResponseAuditMod> audits = jurorResponseAuditRepositoryMod.findAll();
            assertThat(audits.iterator().hasNext()).isTrue();
            assertNullExcusalDate();
        });

        Mockito.verify(this.notificationClientApi).sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_happy_disabledStraightThroughAcceptance() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final LocalDate dob = LocalDate.now().minusYears(36);

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .qualify(JurorResponseDto.Qualify.builder()
                .livedConsecutive(JurorResponseDto.Answerable.builder().answer(true).build()).build())
            .build();

        checkResponseTablesEmpty();

        Iterable<JurorResponseAuditMod> jurorResponseAuditMod = jurorResponseAuditRepositoryMod.findAll();
        assertThat(jurorResponseAuditMod.iterator().hasNext()).isFalse();

        Optional<AppSetting> applicationSettings = appSettingRepository
            .findById(StraightThroughType.ACCEPTANCE.getDbName());
        assertThat(applicationSettings).map(AppSetting::getValue).hasValue("TRUE");

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        template.exchange(requestEntity, String.class);

        checkSingleResponseCreated();

        DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
        assertThat(response).isNotNull();
        // processing status should be closed as the straight through succeeded
        assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);
        assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());

        Optional<Juror> juror = jurorRepository.findById("644892530");
        assertThat(juror).isPresent();
        Juror jurorRecord = juror.get();
        assertThat(jurorRecord.getDateOfBirth()).isNotEqualTo(dob); // juror DOB should not have been updated
        assertThat(jurorRecord.isResponded()).isFalse();

        JurorPool jurorPool = jurorPoolRepository
            .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
        assertThat(jurorPool).isNotNull();
        // not a straight through so status should be SUMMONED
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

        // check the response submitted history is present for juror
        Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
        assertThat(history).isNotEmpty();
        assertThat(history.size()).isEqualTo(1);
        checkResponseSubmittedHistory(history);

        // check no audit records were created
        jurorResponseAuditMod = jurorResponseAuditRepositoryMod.findAll();
        assertThat(jurorResponseAuditMod.iterator().hasNext()).isFalse();
    }


    /**
     * Asserts that the excusal date is null when there isn't an excusal.
     *
     * @since JDB-1902
     */
    private void assertNullExcusalDate() {
        assertThat(
            jdbcTemplate.queryForObject("select date_excused from juror_mod.juror WHERE juror_number='644892530'",
                Date.class)).isNull();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_straightThroughAcceptance_cjsEmployed() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        LocalDate dob = LocalDate.now().minusYears(36);

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .cjsEmployment(Collections.singletonList(
                    JurorResponseDto.CjsEmployment.builder()
                        .cjsEmployer("police")
                        .cjsEmployerDetails("I am invalid because I am a forensic examiner")
                        .build()
                )
            ).build();

        Optional<AppSetting> applicationSettings = appSettingRepository
            .findById(StraightThroughType.ACCEPTANCE.getDbName());
        assertThat(applicationSettings).isEmpty();

        executeInTransaction(() -> {
            checkResponseTablesEmpty();

            Iterable<JurorResponseAuditMod> jurorResponseAuditMod = jurorResponseAuditRepositoryMod.findAll();
            assertThat(jurorResponseAuditMod.iterator().hasNext()).isFalse();

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isNotEqualTo(dob); // juror DOB should not match DOB in response
            assertThat(jurorRecord.isResponded()).isFalse();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

        });

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {
            //assert database has response
            checkSingleResponseCreated();

            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            // processing status should be closed as the straight through succeeded
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isNotEqualTo(dob); // juror DOB should not have been updated
            assertThat(jurorRecord.isResponded()).isFalse();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            // not a straight through so status should be SUMMONED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

            List<JurorResponseCjsEmployment> cjsEmploymentList = jurorResponseCjsEmploymentRepositoryMod.findAll();
            assertThat(cjsEmploymentList.size()).isEqualTo(1);
            assertThat(cjsEmploymentList.get(0).getCjsEmployer()).isEqualTo("police");
            assertThat(cjsEmploymentList.get(0).getCjsEmployerDetails())
                .isEqualTo("I am invalid because I am a forensic examiner");

            // check the response submitted history is present for juror
            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                h.getHistoryCode().equals(HistoryCodeMod.RESPONSE_SUBMITTED)).findFirst();
            assertThat(historyRecord).isPresent();
            assertThat(historyRecord.get().getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(historyRecord.get().getOtherInformation()).isEqualTo("Digital");

            historyRecord = history.stream().filter(h ->
                h.getHistoryCode().equals(HistoryCodeMod.RESPONDED_POSITIVELY)).findFirst();
            assertThat(historyRecord.isPresent()).isFalse();

            // check no audit records were created
            Iterable<JurorResponseAuditMod> jurorResponseAuditMod = jurorResponseAuditRepositoryMod.findAll();
            assertThat(jurorResponseAuditMod.iterator().hasNext()).isFalse();

        });
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_straightThroughAcceptance_specialNeed() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        LocalDate dob = LocalDate.now().minusYears(36);

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .reasonableAdjustments(Collections.singletonList(
                JurorResponseDto.ReasonableAdjustment.builder()
                    .assistanceType("I")
                    .assistanceTypeDetails("I have a nut allergy")
                    .build()
            ))
            .build();

        Optional<AppSetting> applicationSettings = appSettingRepository
            .findById(StraightThroughType.ACCEPTANCE.getDbName());
        assertThat(applicationSettings).isEmpty();

        executeInTransaction(() -> {
            //assert database has response
            checkResponseTablesEmpty();

            Iterable<JurorResponseAuditMod> jurorResponseAuditMod = jurorResponseAuditRepositoryMod.findAll();
            assertThat(jurorResponseAuditMod.iterator().hasNext()).isFalse();

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isNotEqualTo(dob); // juror DOB should not match DOB in response
            assertThat(jurorRecord.isResponded()).isFalse();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

        });

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {

            checkSingleResponseCreated();

            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            // processing status should be closed as the straight through succeeded
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isNotEqualTo(dob); // juror DOB should not have been updated
            assertThat(jurorRecord.isResponded()).isFalse();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            // not a straight through so status should be SUMMONED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

            Iterable<JurorReasonableAdjustment> reasonableAdjustmentList = jurorReasonableAdjustmentRepository
                                                                                                        .findAll();
            assertThat(reasonableAdjustmentList.iterator().hasNext()).isTrue();
            JurorReasonableAdjustment adjustment = reasonableAdjustmentList.iterator().next();
            assertThat(adjustment.getReasonableAdjustment().getCode()).isEqualTo("I");
            assertThat(adjustment.getReasonableAdjustmentDetail()).isEqualTo("I have a nut allergy");

            // check the response submitted history is present for juror
            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                                            h.getHistoryCode().equals(HistoryCodeMod.RESPONSE_SUBMITTED)).findFirst();
            assertThat(historyRecord).isPresent();
            assertThat(historyRecord.get().getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(historyRecord.get().getOtherInformation()).isEqualTo("Digital");

            historyRecord = history.stream().filter(h ->
                h.getHistoryCode().equals(HistoryCodeMod.RESPONDED_POSITIVELY)).findFirst();
            assertThat(historyRecord.isPresent()).isFalse();

            Iterable<JurorResponseAuditMod> jurorResponseAuditMod = jurorResponseAuditRepositoryMod.findAll();
            assertThat(jurorResponseAuditMod.iterator().hasNext()).isFalse();

        });

    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/straight_through_deceased_excusal_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_happy_thirdPartyDeceased_disabledStraightThrough() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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

        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {

            checkSingleResponseCreated();

            // there is no straight through enabled, so expect TO-DO
            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            // processing status should be closed as the straight through succeeded
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());
            assertThat(response.getThirdPartyReason()).isEqualTo("Deceased");
            assertThat(response.getPostcode()).isEqualTo("AB3 9RY");

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.isResponded()).isFalse();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            // not a straight through so status should be SUMMONED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

            // check the response submitted history is present for juror
            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                                        h.getHistoryCode().equals(HistoryCodeMod.RESPONSE_SUBMITTED)).findFirst();
            assertThat(historyRecord).isPresent();
            assertThat(historyRecord.get().getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(historyRecord.get().getOtherInformation()).isEqualTo("Digital");

        });
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_unhappy_thirdPartyDeceased_validationFail() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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

        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(exchange.getBody().getErrors())
            .hasSize(4)
            .extracting("field")
            .containsExactlyInAnyOrder("thirdParty.thirdPartyFName", "thirdParty.thirdPartyLName", "thirdParty",
                "thirdParty.relationship");

        // response was not saved
        checkResponseTablesEmpty();

        Optional<Juror> juror = jurorRepository.findById("644892530");
        assertThat(juror).isPresent();
        Juror jurorRecord = juror.get();
        assertThat(jurorRecord.isResponded()).isFalse();

        JurorPool jurorPool = jurorPoolRepository
            .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
        assertThat(jurorPool).isNotNull();
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

        Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
        assertThat(history).isEmpty();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.ExcusalDeceasedStraightThrough_unhappy_superurgent.sql")
    public void respondToSummons_unhappy_thirdPartyDeceased_validationFail_superUrgent() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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

        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {
            //assert database has response
            checkSingleResponseCreated();

            // there is no straight through enabled, so expect TO-DO
            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            // processing status should be closed as the straight through succeeded
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());

            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.isResponded()).isFalse();

            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555444444");
            assertThat(jurorPool).isNotNull();
            // not a straight through so status should be SUMMONED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.SUMMONED.getStatus());

            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            assertThat(history.size()).isEqualTo(1);
            checkResponseSubmittedHistory(history);

        });
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
    public void respondToSummons_happy_ageExcusal_successfulStraightThrough_young() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
        LocalDate dob = hearingDate.minusYears(youngestJurorAgeAllowed - 1L).minusDays(364).toLocalDate();

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .build();


        Optional<AppSetting> applicationSettings = appSettingRepository
            .findById(StraightThroughType.AGE_EXCUSAL.getDbName());
        assertThat(applicationSettings).isEmpty();

        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {

            checkSingleResponseCreated();

            // get the juror response and check the processing status
            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            // processing status should be closed as the straight through succeeded
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());

            // get juror record and check responded and DOB
            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isEqualTo(dob); // juror DOB should have been updated
            assertThat(jurorRecord.isResponded()).isTrue();
            assertThat(jurorRecord.getDisqualifyCode()).isEqualTo("A");

            // get the juror pool record and check status
            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            // status should be DISQUALIFIED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.DISQUALIFIED.getStatus());

            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            assertThat(history.size()).isEqualTo(5);

            checkResponseSubmittedHistory(history);
            checkDisqualifyPoolMemberHistory(history);
            checkWithdrawalLetterHistory(history);
            // there are two juror details updated history records too for dob and postcode

            List<BulkPrintData> bulkPrintDataList = bulkPrintDataRepository.findByJurorNo("644892530");
            assertThat(bulkPrintDataList.size()).isEqualTo(1);
            assertThat(bulkPrintDataList.get(0).getFormAttribute().getFormType())
                .as("form code should be withdrawal codes").isIn("5224", "5224C");

        });
    }

    private static void checkWithdrawalLetterHistory(Collection<JurorHistory> history) {
        Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                               h.getHistoryCode().equals(HistoryCodeMod.WITHDRAWAL_LETTER)).findFirst();
        assertThat(historyRecord).isPresent();
        assertThat(historyRecord.get().getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(historyRecord.get().getOtherInformationRef()).isEqualTo("A");
    }

    private static void checkDisqualifyPoolMemberHistory(Collection<JurorHistory> history) {
        Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                        h.getHistoryCode().equals(HistoryCodeMod.DISQUALIFY_POOL_MEMBER)).findFirst();
        assertThat(historyRecord).isPresent();
        assertThat(historyRecord.get().getCreatedBy()).isEqualTo("AUTO");
        assertThat(historyRecord.get().getOtherInformationRef()).isEqualTo("A");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
    public void respondToSummons_happy_ageExcusal_successfulStraightThrough_old() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
        LocalDate dob =
            hearingDate.minusYears(tooOldJurorAge).minusDays(0).toLocalDate();

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .build();

        Optional<AppSetting> applicationSettings = appSettingRepository
            .findById(StraightThroughType.AGE_EXCUSAL.getDbName());
        assertThat(applicationSettings).isEmpty();

        checkResponseTablesEmpty();

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {

            checkSingleResponseCreated();

            // get the juror response and check the processing status
            DigitalResponse response = jurorDigitalResponseRepositoryMod.findByJurorNumber("644892530");
            assertThat(response).isNotNull();
            // processing status should be closed as the straight through succeeded
            assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
            assertThat(response.getReplyType().getType()).isEqualTo(ReplyMethod.DIGITAL.getDescription());

            // get juror record and check responded and DOB
            Optional<Juror> juror = jurorRepository.findById("644892530");
            assertThat(juror).isPresent();
            Juror jurorRecord = juror.get();
            assertThat(jurorRecord.getDateOfBirth()).isEqualTo(dob); // juror DOB should have been updated
            assertThat(jurorRecord.isResponded()).isTrue();
            assertThat(jurorRecord.getDisqualifyCode()).isEqualTo("A");

            // get the juror pool record and check status
            JurorPool jurorPool = jurorPoolRepository
                .findByJurorJurorNumberAndPoolPoolNumber("644892530", "555000000");
            assertThat(jurorPool).isNotNull();
            // status should be DISQUALIFIED
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(JurorStatusEnum.DISQUALIFIED.getStatus());

            Collection<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById("644892530");
            assertThat(history).isNotEmpty();
            assertThat(history.size()).isEqualTo(4);

            checkResponseSubmittedHistory(history);
            checkDisqualifyPoolMemberHistory(history);
            checkWithdrawalLetterHistory(history);
            // there is also a juror details updated history record for dob

            List<BulkPrintData> bulkPrintDataList = bulkPrintDataRepository.findByJurorNo("644892530");
            assertThat(bulkPrintDataList.size()).isEqualTo(1);
            assertThat(bulkPrintDataList.get(0).getFormAttribute().getFormType())
                .as("form code should be withdrawal codes").isIn("5224", "5224C").isIn("5224", "5224C");
        });

        Mockito.verify(this.notificationClientApi).sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/straight_through_deceased_excusal_disabled.sql")
    @Sql("/db/straight_through_age_excusal_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    public void respondToSummons_happy_ageExcusal_straightThroughDisabled() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
        LocalDate dob =
            hearingDate.minusYears(youngestJurorAgeAllowed - 1L).minusDays(0).toLocalDate();

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from juror_mod.app_setting where SETTING='"
            + StraightThroughType.AGE_EXCUSAL.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from juror_mod.juror", Boolean.class))
            .isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("select STATUS from juror_mod.juror_pool", Integer.class))
            .isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_history WHERE pool_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from juror_mod.juror_response",
            String.class)).isEqualTo("TODO");
        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from juror_mod.juror WHERE RESPONDED='Y' and juror_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror WHERE excusal_code='A' and "
                    + "juror_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_pool WHERE STATUS='6' and "
                    + "juror_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.juror_history WHERE HISTORY_CODE='PDIS' and OTHER_INFO_REFERENCE='A'"
                + " and juror_number='644892530'",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.juror_history WHERE HISTORY_CODE='RDIS' and OTHER_INFO_REFERENCE='A'"
                + " and juror_number='644892530'",
            Integer.class)).isEqualTo(0);

        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.bulk_print_data WHERE juror_no='644892530' and form_type in ('5224',"
                + "'5224C')",
            Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/straight_through_deceased_excusal_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    public void respondToSummons_unhappy_ageExcusal_notExcused_exactlyMinimumAge() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
        LocalDate dob =
            hearingDate.minusYears(youngestJurorAgeAllowed).minusDays(0).toLocalDate();

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from juror_mod.app_setting where SETTING='"
            + StraightThroughType.AGE_EXCUSAL.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from juror_mod.juror", Boolean.class))
            .isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("select STATUS from juror_mod.juror_pool", Integer.class))
            .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_history "
                + "WHERE pool_number='644892530'",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from juror_mod.juror_response",
            String.class)).isEqualTo("TODO");
        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from juror_mod.juror WHERE RESPONDED='Y' and juror_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from juror_mod.juror WHERE excusal_code='A' and juror_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from juror_mod.juror_pool WHERE STATUS='6' and juror_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.juror_history WHERE HISTORY_CODE='PDIS' and OTHER_INFO_REFERENCE='A'"
                + " and juror_number='644892530'",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.juror_history WHERE HISTORY_CODE='RDIS' and OTHER_INFO_REFERENCE='A'"
                + " and juror_number='644892530'",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from JUROR_MOD.BULK_PRINT_DATA WHERE FORM_TYPE='5225' and JUROR_NO='644892530'",
            Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/straight_through_acceptance_disabled.sql")
    @Sql("/db/straight_through_deceased_excusal_disabled.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_ageExcusal.sql")
    public void respondToSummons_unhappy_ageExcusal_notExcused_exactlyMaximumAge() throws Exception {


        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
        LocalDate dob =
            hearingDate.minusYears(tooOldJurorAge - 1L).minusDays(364).toLocalDate();

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB3 9RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .build();

        assertThat(jdbcTemplate.queryForObject("select count(*) from juror_mod.app_setting where SETTING='"
            + StraightThroughType.AGE_EXCUSAL.getDbName() + "' AND VALUE='TRUE'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response_AUD",
            Integer.class)).isEqualTo(0);

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from juror_mod.juror", Boolean.class))
            .isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("select STATUS from juror_mod.juror_pool", Integer.class))
            .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_history "
                + "WHERE pool_number='644892530'",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from juror_mod.juror_response",
            String.class)).isEqualTo("TODO");
        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from juror_mod.juror WHERE RESPONDED='Y' and juror_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from juror_mod.juror WHERE excusal_code='A' and juror_number='644892530'",
                Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from juror_mod.juror_pool WHERE STATUS='6' and juror_number='644892530'",
                Integer.class)).isEqualTo(0);

        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.juror_history WHERE HISTORY_CODE='PDIS' and OTHER_INFO_REFERENCE='A'"
                + " and juror_number='644892530'",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.juror_history WHERE HISTORY_CODE='RDIS' and OTHER_INFO_REFERENCE='A'"
                + " and juror_number='644892530'",
            Integer.class)).isEqualTo(0);

        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.bulk_print_data WHERE form_type in ('5224','5224C') "
                + "and juror_no='644892530'",
            Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_EmploymentsValidation() throws Exception {

        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        LocalDate dob = LocalDate.now().minusYears(36);

        final JurorResponseDto dto = JurorResponseDto.builder(
                "644892530", "JANE", "CASTILLO", "4 Knutson Trail",
                "Scotland",
                "Aberdeen",
                "AB39RY", dob,
                "012341234567", "jcastillo0@ed.gov", validQualify, null, ReplyMethod.DIGITAL)
            .title("DR")
            .cjsEmployment(Collections.singletonList(JurorResponseDto.CjsEmployment.builder()
                .cjsEmployer("Mega City 1 Hall of Justice")
                .cjsEmployerDetails("I am the law.")
                .cjsEmployer("Police Force")
                .cjsEmployerDetails("In a Police force, Since 5 years ")
                .build()))
            .build();

        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class)).isEqualTo(
            0);
        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("select PROCESSING_STATUS from juror_mod.juror_response",
            String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("select RESPONDED from juror_mod.juror", Boolean.class))
            .isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("select STATUS from juror_mod.juror_pool", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from juror_mod.juror_history WHERE OTHER_INFORMATION='ADD Excuse - D' and "
                + "juror_number='644892530'",
            Integer.class)).isEqualTo(0);
    }

    /**
     * The endpoint should reject a response with no phone numbers populated.
     *
     * @throws Exception if the test falls over
     * @since JDB-1968
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappy_noPhoneNumbersProvided() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
     * Should reject response if 'use juror phone' is false but no third party phone is provided.
     *
     * @throws Exception if the test falls over
     * @since JDB-2165
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_useThirdPartyPhoneNumberButNoneSupplied() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
     * Should accept response if 'use juror phone' is set and no third party phone is provided.
     *
     * @throws Exception if the test falls over
     * @since JDB-2165
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_happyPath_useJurorPhoneNumber() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
     * Should not accept response if 'use juror phone' is set and no juror number is provided.
     *
     * @throws Exception if the test falls over
     * @since JDB-2165
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappyPath_useJurorPhoneNumberButNoneProvided() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
     * The endpoint should reject a third-party response containing an invalid phone number.
     *
     * @throws Exception if the test falls over
     * @since JDB-2137
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappy_invalidJurorPhoneNumber() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class)).isEqualTo(
            0);
    }

    /**
     * The endpoint should reject a response with no emails populated.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappy_noEmailsProvided() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
     * Should reject response if 'use juror email' is false but no third party email is provided.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappyPath_useThirdPartyEmailButNoneSupplied() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
     * Should accept response if 'use juror email' is set and no third party email is provided.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_happyPath_useJurorEmail() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
     * Should not accept response if 'use juror email' is set and no juror email is provided.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappyPath_useJurorEmailButNoneProvided() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
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
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
     * The endpoint should reject a third-party response containing an invalid email.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void thirdPartyResponse_unhappy_invalidJurorEmail() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("644892530")
            .postcode("AB39RY")
            .surname("CASTILLO")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto jurorResponse =
            JurorResponseDto.realBuilder().jurorNumber("644892530").addressPostcode("AB39RY")
                .dateOfBirth(LocalDate.of(1984, 7, 24))
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
            jdbcTemplate.queryForObject("select count(*) from juror_mod.juror_response", Integer.class)).isEqualTo(
            0);
    }

    private void checkSingleResponseCreated() {
        Iterable<DigitalResponse> responses = jurorDigitalResponseRepositoryMod.findAll();
        assertThat(responses).hasSize(1);
    }

    private void checkJurorDetailsInJurorRecord(Juror juror, LocalDate dob) {
        assertThat(juror.getFirstName()).isEqualTo("Jose");
        assertThat(juror.getLastName()).isEqualTo("Rivera");
        assertThat(juror.getTitle()).isEqualTo("Rev");
        assertThat(juror.getDateOfBirth()).isEqualTo(dob);
        assertThat(juror.getAddressLine1()).isEqualTo("22177 Redwing Way");
        assertThat(juror.getAddressLine2()).isEqualTo("England");
        assertThat(juror.getAddressLine3()).isEqualTo("London");
        assertThat(juror.getAddressLine4()).isEqualTo("United Kingdom");
        assertThat(juror.getAddressLine5()).isEqualTo("");
        assertThat(juror.getPostcode()).isEqualTo("EC3M 2NY");
        assertThat(juror.getPhoneNumber()).isEqualTo("07112233445");
        assertThat(juror.getAltPhoneNumber()).isEqualTo("02334455667");
        assertThat(juror.getEmail()).isEqualTo("email@email.com");

    }

    private void checkJurorDetailsInResponse(DigitalResponse response, LocalDate dob) {
        assertThat(response.getDateOfBirth()).isEqualTo(dob);
        assertThat(response.getFirstName()).isEqualTo("Jose");
        assertThat(response.getLastName()).isEqualTo("Rivera");
        assertThat(response.getTitle()).isEqualTo("Rev");
        assertThat(response.getAddressLine1()).isEqualTo("22177 Redwing Way");
        assertThat(response.getAddressLine2()).isEqualTo("England");
        assertThat(response.getAddressLine3()).isEqualTo("London");
        assertThat(response.getAddressLine4()).isEqualTo("United Kingdom");
        assertThat(response.getAddressLine5()).isEqualTo("");
        assertThat(response.getPostcode()).isEqualTo("EC3M 2NY");
        assertThat(response.getPhoneNumber()).isEqualTo("07112233445");
        assertThat(response.getAltPhoneNumber()).isEqualTo("02334455667");
        assertThat(response.getEmail()).isEqualTo("email@email.com");
    }

    private void checkResponseChildTablesEmpty() {
        List<JurorResponseCjsEmployment> cjsEmploymentList = jurorResponseCjsEmploymentRepositoryMod.findAll();
        assertThat(cjsEmploymentList).isEmpty();
        Iterable<JurorReasonableAdjustment> reasonableAdjustmentList = jurorReasonableAdjustmentRepository.findAll();
        assertThat(reasonableAdjustmentList.iterator().hasNext()).isFalse();
    }

    private void checkResponseTablesEmpty() {
        Iterable<DigitalResponse> responseList = jurorDigitalResponseRepositoryMod.findAll();
        assertThat(responseList.iterator().hasNext()).isFalse();
        List<JurorResponseCjsEmployment> cjsEmploymentList = jurorResponseCjsEmploymentRepositoryMod.findAll();
        assertThat(cjsEmploymentList).isEmpty();
        Iterable<JurorReasonableAdjustment> reasonableAdjustmentList = jurorReasonableAdjustmentRepository.findAll();
        assertThat(reasonableAdjustmentList.iterator().hasNext()).isFalse();
    }

    private void checkResponseSubmittedHistory(Collection<JurorHistory> history) {
        Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                                            h.getHistoryCode().equals(HistoryCodeMod.RESPONSE_SUBMITTED)).findFirst();
        assertThat(historyRecord).isPresent();
        assertThat(historyRecord.get().getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(historyRecord.get().getOtherInformation()).isEqualTo("Digital");
    }

    private void checkRespondedHistory(Collection<JurorHistory> history) {
        Optional<JurorHistory> historyRecord = history.stream().filter(h ->
                                        h.getHistoryCode().equals(HistoryCodeMod.RESPONDED_POSITIVELY)).findFirst();
        assertThat(historyRecord).isPresent();
        assertThat(historyRecord.get().getCreatedBy()).isEqualTo(JurorDigitalApplication.AUTO_USER);
        assertThat(historyRecord.get().getOtherInformation()).isEqualTo("Responded");
    }

    private String mintPublicJwt(final PublicJwtPayload payload) throws Exception {
        return TestUtil.mintPublicJwt(payload, SignatureAlgorithm.HS256, publicSecret,
            Instant.now().plus(100L * 365L, ChronoUnit.DAYS));
    }
}
