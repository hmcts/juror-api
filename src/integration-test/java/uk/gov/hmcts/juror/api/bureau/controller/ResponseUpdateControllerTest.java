package uk.gov.hmcts.juror.api.bureau.controller;

import jakarta.validation.ValidationException;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauResponseStatusUpdateDto;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogType;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.bureau.service.ResponseNotesService;
import uk.gov.hmcts.juror.api.bureau.service.ResponsePhoneLogService;
import uk.gov.hmcts.juror.api.bureau.service.ResponseUpdateServiceImpl;
import uk.gov.hmcts.juror.api.bureau.service.ResponseUpdateServiceImpl.CjsEmployment;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ResponseUpdateController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResponseUpdateControllerTest extends AbstractIntegrationTest {
    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"
    })
    public void jurorNoteByJurorNumber_happy() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ResponseEntity<ResponseUpdateController.JurorNoteDto> responseEntity =
            template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/bureau/juror" +
                "/209092530/notes")), ResponseUpdateController.JurorNoteDto.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getNotes())
            .as("Notes column is expected")
            .isEqualTo("Juror 209092530 notes");
        assertThat(responseEntity.getBody().getVersion()).as("Hashing function has unchanged")
            .isEqualTo("eec6b39e8b7c52e0662220a8f05d98d0");
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"
    })
    public void jurorNoteByJurorNumber_unhappyNonExistentJuror() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ResponseEntity<SpringBootErrorResponse> responseEntity =
            template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/bureau/juror" +
                "/111222333/notes")), SpringBootErrorResponse.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getException())
            .as("Error is correct")
            .isEqualTo(ResponseNotesService.NoteNotFoundException.class.getTypeName());
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"
    })
    public void updateNoteByJurorNumber_happy() throws Exception {
        final String LOGIN_NAME = "testlogin";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(LOGIN_NAME)
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '209092530'", String.class)).isNull();

        final String UPDATED_NOTES = "New improved note for juror 209092530";
        final String VERSION_HASH = "eec6b39e8b7c52e0662220a8f05d98d0";
        final ResponseUpdateController.JurorNoteDto noteDto = ResponseUpdateController.JurorNoteDto.builder()
            .notes(UPDATED_NOTES)
            .version(VERSION_HASH)
            .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + "209092530" + "/notes");
        final RequestEntity<ResponseUpdateController.JurorNoteDto> requestEntity = new RequestEntity<>(noteDto,
            httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR.POOL WHERE PART_NO = '209092530'",
            String.class)).isEqualTo(UPDATED_NOTES);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT USER_ID FROM JUROR.PART_HIST WHERE PART_NO = '209092530' AND " +
            "OWNER = '400'", String.class)).isEqualTo(LOGIN_NAME);
        assertThat(jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM JUROR.PART_HIST WHERE PART_NO = " +
            "'209092530' AND OWNER = '400'", String.class))
            .as("Other information set to for note update")
            .isEqualTo(ResponseUpdateServiceImpl.UPDATED_NOTES);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '209092530'", String.class)).isEqualTo(LOGIN_NAME);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"
    })
    public void updateNoteByJurorNumber_unhappyNonExistentJuror() throws Exception {
        final String LOGIN_NAME = "testlogin";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(LOGIN_NAME + " Mc" + LOGIN_NAME)
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);

        final String UPDATED_NOTES = "New improved note for juror 123123123";
        final String VERSION_HASH = "eec6b39e8b7c52e0662220a8f05d98d0";
        final ResponseUpdateController.JurorNoteDto noteDto = ResponseUpdateController.JurorNoteDto.builder()
            .notes(UPDATED_NOTES)
            .version(VERSION_HASH)
            .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + "123123123" + "/notes");
        final RequestEntity<ResponseUpdateController.JurorNoteDto> requestEntity = new RequestEntity<>(noteDto,
            httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getBody().getException())
            .as("Error is correct")
            .isEqualTo(ResponseNotesService.NoteNotFoundException.class.getTypeName());

        // assert the DB change was NOT applied
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR.POOL WHERE PART_NO = '209092530'",
            String.class)).isNotEqualTo(UPDATED_NOTES);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '209092530'", String.class)).isNull();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"
    })
    public void updateNoteByJurorNumber_unhappy_hashMismatch() throws Exception {
        final String LOGIN_NAME = "testlogin";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(LOGIN_NAME + " Mc" + LOGIN_NAME)
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);

        final String UPDATED_NOTES = "New improved note for juror 209092530";
        final String BAD_VERSION_HASH = "abcdef1234567890abcdef1234567890";
        final ResponseUpdateController.JurorNoteDto noteDto = ResponseUpdateController.JurorNoteDto.builder()
            .notes(UPDATED_NOTES)
            .version(BAD_VERSION_HASH)
            .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + "209092530" + "/notes");
        final RequestEntity<ResponseUpdateController.JurorNoteDto> requestEntity = new RequestEntity<>(noteDto,
            httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody().getException())
            .as("Error is correct")
            .isEqualTo(ResponseNotesService.NoteComparisonFailureException.class.getTypeName());

        // assert the DB change was NOT applied
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR.POOL WHERE PART_NO = '209092530'",
            String.class)).isNotEqualTo(UPDATED_NOTES);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_phoneLogs.sql")
    public void updatePhoneLogByJurorNumber_happy() throws Exception {
        final String LOGIN_NAME = "testlogin";
        final String JUROR_NUMBER = "644892530";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(LOGIN_NAME)
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);

        final String UPDATED_NOTES = "New phone log entry for juror 644892530";
        final ResponseUpdateController.JurorPhoneLogDto noteDto = ResponseUpdateController.JurorPhoneLogDto.builder()
            .notes(UPDATED_NOTES)
            .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/phone");
        final RequestEntity<ResponseUpdateController.JurorPhoneLogDto> requestEntity = new RequestEntity<>(noteDto,
            httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT * FROM (SELECT NOTES FROM JUROR.PHONE_LOG ORDER BY START_CALL DESC) AS TEMP LIMIT 1", String.class))
                .as("Newest phone log notes are as expected")
                .isEqualTo(UPDATED_NOTES);
        assertThat(jdbcTemplate.queryForObject("SELECT * FROM (SELECT USER_ID FROM JUROR.PHONE_LOG ORDER BY START_CALL DESC) AS TEMP LIMIT 1", String.class))
                .as("Newest phone log user id is as expected")
                .isEqualTo(LOGIN_NAME);
        assertThat(jdbcTemplate.queryForObject("SELECT * FROM (SELECT PHONE_CODE FROM JUROR.PHONE_LOG ORDER BY START_CALL DESC) AS TEMP LIMIT 1", String.class))
                .as("Newest phone log phone code is as expected")
                .isEqualTo(ResponsePhoneLogService.DEFAULT_PHONE_CODE);
        assertThat(jdbcTemplate.queryForObject("SELECT * FROM (SELECT PART_NO FROM JUROR.PHONE_LOG ORDER BY START_CALL DESC) AS TEMP LIMIT 1", String.class))
                .as("Newest phone log juror number is as expected")
                .isEqualTo(JUROR_NUMBER);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo(LOGIN_NAME);
    }

    @Test
    public void validateJurorNumberPathVariable_happy_validationPass() throws Exception {
        final String VALID_JUROR_NUMBER = "123456789";
        ResponseUpdateController.validateJurorNumberPathVariable(VALID_JUROR_NUMBER);
        // no exception = pass
    }

    @Test
    public void validateJurorNumberPathVariable_unhappy_validationFail() throws Exception {
        final String INVALID_JUROR_NUMBER_LONG = "1234567890";// too long
        final String INVALID_JUROR_NUMBER_SHORT = "12345678";// too short
        final String INVALID_JUROR_NUMBER_ALPHA = "1234S6789";// alpha char
        final String INVALID_JUROR_NUMBER_SPECIAL = "!23456789";// special char

        try {
            ResponseUpdateController.validateJurorNumberPathVariable(INVALID_JUROR_NUMBER_LONG);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.validateJurorNumberPathVariable(INVALID_JUROR_NUMBER_SHORT);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.validateJurorNumberPathVariable(INVALID_JUROR_NUMBER_ALPHA);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.validateJurorNumberPathVariable(INVALID_JUROR_NUMBER_SPECIAL);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_firstPerson.sql")
    public void updateJurorDetailsFirstPerson_happy() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool = jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = " +
            "'352004504'");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isNull();

        final String UPDATED_TITLE = "Sir";//"Rev";
        final String UPDATED_FIRST_NAME = "James";//"Jose";
        final String UPDATED_LAST_NAME = "Rivers";//"Rivera";
        final String UPDATED_ADDRESS1 = "22177 Bluewing Way";//"22177 Redwing Way";
        final String UPDATED_ADDRESS2 = "London";//"England";
        final String UPDATED_ADDRESS3 = "England";//"London";
        final String UPDATED_ADDRESS4 = "Lonely Island";//"United Kingdom";
        final String UPDATED_ADDRESS5 = "Planet Earth";//null;
        final String UPDATED_POSTCODE = "E17 2NY";//"EC3M 2NY";
        // Not updating DOB!
        final String UPDATED_MAIN_PHONE = "01415555559";//"01415555557";
        final String UPDATED_ALT_PHONE = "07415555558";//"01415555558";
        final String UPDATED_EMAIL_ADDRESS = "james.rivers@email.com";//"jose.rivera@email.com";

        final ResponseUpdateController.FirstPersonJurorDetailsDto tpDto =
            ResponseUpdateController.FirstPersonJurorDetailsDto.builder()
                .title(UPDATED_TITLE)
                .firstName(UPDATED_FIRST_NAME)
                .lastName(UPDATED_LAST_NAME)
                .address(UPDATED_ADDRESS1)
                .address2(UPDATED_ADDRESS2)
                .address3(UPDATED_ADDRESS3)
                .address4(UPDATED_ADDRESS4)
                .address5(UPDATED_ADDRESS5)
                .postcode(UPDATED_POSTCODE)
                .dob(Date.from(Instant.from(ZonedDateTime.of(
                    LocalDate.of(1985, 8, 8), LocalTime.MIDNIGHT, ZoneId.systemDefault())
                )))// no change!
                .mainPhone(UPDATED_MAIN_PHONE)
                .altPhone(UPDATED_ALT_PHONE)
                .emailAddress(UPDATED_EMAIL_ADDRESS)
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/first-person");
        final RequestEntity<ResponseUpdateController.FirstPersonJurorDetailsDto> requestEntity =
            new RequestEntity<>(tpDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = '352004504'"))
            .containsAllEntriesOf(originalPool);
        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TITLE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed title column updated").isEqualTo(UPDATED_TITLE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT FIRST_NAME FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed first name column updated").isEqualTo(UPDATED_FIRST_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed last name column updated").isEqualTo(UPDATED_LAST_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address1 column updated").isEqualTo(UPDATED_ADDRESS1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS2 FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address2 column updated").isEqualTo(UPDATED_ADDRESS2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS3 FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address3 column updated").isEqualTo(UPDATED_ADDRESS3);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS4 FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address4 column updated").isEqualTo(UPDATED_ADDRESS4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS5 FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address5 column updated").isEqualTo(UPDATED_ADDRESS5);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ZIP FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed postcode column updated").isEqualTo(UPDATED_POSTCODE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PHONE_NUMBER FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed main phone column updated").isEqualTo(UPDATED_MAIN_PHONE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ALT_PHONE_NUMBER FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed alt phone column updated").isEqualTo(UPDATED_ALT_PHONE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EMAIL FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed email column updated").isEqualTo(UPDATED_EMAIL_ADDRESS);

        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isEqualTo(LOGIN_NAME);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.JUROR_DETAILS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE " +
            "CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items " +
            "were " +
            "created").isEqualTo(13);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM",
            Integer.class)).as("Correct TOTAL number of change log items were created").isEqualTo(13);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_thirdParty.sql")
    public void updateJurorDetailsThirdParty_happy() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool = jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = " +
            "'352004504'");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isNull();

        final boolean USE_JUROR_PHONE = true;
        final boolean USE_JUROR_EMAIL = true;
        final String RELATIONSHIP = "Brother";
        final String REASON = "other";
        final String OTHER_REASON_TEXT = "This is a test third party update!";
        // otherReason = null
        final String TP_FIRST_NAME = "John";
        final String TP_LAST_NAME = "Doe";
        final String TP_MAIN_PHONE = "01415555555";
        final String TP_ALT_PHONE = "01415555556";
        final String TP_EMAIL = "john.doe@email.com";
        final String UPDATED_TITLE = "Sir";
        final ResponseUpdateController.ThirdPartyJurorDetailsDto tpDto =
            ResponseUpdateController.ThirdPartyJurorDetailsDto.builder()
                .useJurorPhone(USE_JUROR_PHONE)
                .useJurorEmail(USE_JUROR_EMAIL)
                .relationship(RELATIONSHIP)
                .thirdPartyReason(REASON)
                .thirdPartyOtherReason(OTHER_REASON_TEXT)
                .thirdPartyFirstName(TP_FIRST_NAME)
                .thirdPartyLastName(TP_LAST_NAME)
                .thirdPartyMainPhone(TP_MAIN_PHONE)
                .thirdPartyAltPhone(TP_ALT_PHONE)
                .thirdPartyEmail(TP_EMAIL)
                .title(UPDATED_TITLE)
                .firstName("Jose")
                .lastName("Rivera")
                .address("22177 Redwing Way")
                .address2("England")
                .address3("London")
                .address4("United Kingdom")
                .address5(null)
                .postcode("EC3M 2NY")
                .dob(Date.from(Instant.from(ZonedDateTime.of(
                    LocalDate.of(1995, 8, 8), LocalTime.MIDNIGHT, ZoneId.systemDefault())
                )))
                .mainPhone("01415555557")
                .altPhone("01415555558")
                .emailAddress("jose.rivera@email.com")
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/third-party");
        final RequestEntity<ResponseUpdateController.ThirdPartyJurorDetailsDto> requestEntity =
            new RequestEntity<>(tpDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM",
            Integer.class)).isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = '352004504'"))
            .containsAllEntriesOf(originalPool);

        softly.assertThat(jdbcTemplate.queryForObject("SELECT TITLE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed title column updated").isEqualTo(UPDATED_TITLE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isEqualTo(LOGIN_NAME);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.JUROR_DETAILS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE " +
            "CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items " +
            "were " +
            "created").isEqualTo(3);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_firstPerson.sql")
    public void updateJurorDetailsFirstPerson_unhappy_optimisticLocking() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String UPDATED_TITLE = "Sir";//"Rev";
        final String UPDATED_FIRST_NAME = "James";//"Jose";
        final String UPDATED_LAST_NAME = "Rivers";//"Rivera";
        final String UPDATED_ADDRESS1 = "22177 Bluewing Way";//"22177 Redwing Way";
        final String UPDATED_ADDRESS2 = "London";//"England";
        final String UPDATED_ADDRESS3 = "England";//"London";
        final String UPDATED_ADDRESS4 = "Lonely Island";//"United Kingdom";
        final String UPDATED_ADDRESS5 = "Planet Earth";//null;
        final String UPDATED_POSTCODE = "E17 2NY";//"EC3M 2NY";
        // Not updating DOB!
        final String UPDATED_MAIN_PHONE = "01415555559";//"01415555557";
        final String UPDATED_ALT_PHONE = "07415555558";//"01415555558";
        final String UPDATED_EMAIL_ADDRESS = "james.rivers@email.com";//"jose.rivera@email.com";
        final Integer INVALID_VERSION = -1;// stale version

        final ResponseUpdateController.FirstPersonJurorDetailsDto tpDto =
            ResponseUpdateController.FirstPersonJurorDetailsDto.builder()
                .title(UPDATED_TITLE)
                .firstName(UPDATED_FIRST_NAME)
                .lastName(UPDATED_LAST_NAME)
                .address(UPDATED_ADDRESS1)
                .address2(UPDATED_ADDRESS2)
                .address3(UPDATED_ADDRESS3)
                .address4(UPDATED_ADDRESS4)
                .address5(UPDATED_ADDRESS5)
                .postcode(UPDATED_POSTCODE)
                .dob(Date.from(Instant.from(ZonedDateTime.of(
                    LocalDate.of(1995, 8, 8), LocalTime.MIDNIGHT, ZoneId.systemDefault())
                )))// no change!
                .mainPhone(UPDATED_MAIN_PHONE)
                .altPhone(UPDATED_ALT_PHONE)
                .emailAddress(UPDATED_EMAIL_ADDRESS)
                .notes(CHANGE_LOG_NOTES)
                .version(INVALID_VERSION)
                .build();

        // expecting an optimistic lock error response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/first-person");
        final RequestEntity<ResponseUpdateController.FirstPersonJurorDetailsDto> requestEntity =
            new RequestEntity<>(tpDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        softly.assertThat(exchange.getBody()).isNotNull();
        softly.assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TITLE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed title column updated").isNotEqualTo(UPDATED_TITLE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT FIRST_NAME FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed first name column updated").isNotEqualTo(UPDATED_FIRST_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed last name column updated").isNotEqualTo(UPDATED_LAST_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address1 column updated").isNotEqualTo(UPDATED_ADDRESS1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS2 FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address2 column updated").isNotEqualTo(UPDATED_ADDRESS2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS3 FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address3 column updated").isNotEqualTo(UPDATED_ADDRESS3);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS4 FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address4 column updated").isNotEqualTo(UPDATED_ADDRESS4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ADDRESS5 FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed address5 column updated").isNotEqualTo(UPDATED_ADDRESS5);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ZIP FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed postcode column updated").isNotEqualTo(UPDATED_POSTCODE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PHONE_NUMBER FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed main phone column updated").isNotEqualTo(UPDATED_MAIN_PHONE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT ALT_PHONE_NUMBER FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed alt phone column updated").isNotEqualTo(UPDATED_ALT_PHONE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EMAIL FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed email column updated").isNotEqualTo(UPDATED_EMAIL_ADDRESS);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_thirdParty.sql")
    public void updateJurorDetailsThirdParty_unhappy_optimisticLocking() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool = jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = " +
            "'352004504'");

        final boolean USE_JUROR_PHONE = true;
        final boolean USE_JUROR_EMAIL = true;
        final String RELATIONSHIP = "Brother";
        final String REASON = "nothere";
        // otherReason = null
        final String TP_FIRST_NAME = "John";
        final String TP_LAST_NAME = "Doe";
        final String TP_MAIN_PHONE = "01415555555";
        final String TP_ALT_PHONE = "01415555556";
        final String TP_EMAIL = "john.doe@email.com";
        final String UPDATED_TITLE = "Sir";
        final Integer INVALID_VERSION = -1;
        final ResponseUpdateController.ThirdPartyJurorDetailsDto tpDto =
            ResponseUpdateController.ThirdPartyJurorDetailsDto.builder()
                .useJurorPhone(USE_JUROR_PHONE)
                .useJurorEmail(USE_JUROR_EMAIL)
                .relationship(RELATIONSHIP)
                .thirdPartyReason(REASON)
                .thirdPartyFirstName(TP_FIRST_NAME)
                .thirdPartyLastName(TP_LAST_NAME)
                .thirdPartyMainPhone(TP_MAIN_PHONE)
                .thirdPartyAltPhone(TP_ALT_PHONE)
                .thirdPartyEmail(TP_EMAIL)
                .title(UPDATED_TITLE)
                .firstName("Jose")
                .lastName("Rivera")
                .address("22177 Redwing Way")
                .address2("England")
                .address3("London")
                .address4("United Kingdom")
                .address5(null)
                .postcode("EC3M 2NY")
                .dob(Date.from(Instant.from(ZonedDateTime.of(
                    LocalDate.of(1995, 8, 8), LocalTime.MIDNIGHT, ZoneId.systemDefault())
                )))
                .mainPhone("01415555557")
                .altPhone("01415555558")
                .emailAddress("jose.rivera@email.com")
                .notes(CHANGE_LOG_NOTES)
                .version(INVALID_VERSION)
                .build();

        // expecting an optimistic lock error response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/third-party");
        final RequestEntity<ResponseUpdateController.ThirdPartyJurorDetailsDto> requestEntity =
            new RequestEntity<>(tpDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        softly.assertThat(exchange.getBody()).isNotNull();
        softly.assertThat(exchange.getBody().getException())
            .isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert the DB change was NOT applied
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'", Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = '352004504'")).containsAllEntriesOf(originalPool);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TITLE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Changed title column updated").isNotEqualTo(UPDATED_TITLE);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateDeferralExcusal_confirmation_happy.sql")
    public void updateDeferralExcusal_confirmation_happy() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isNull();

        final ResponseUpdateController.DeferralExcusalDto confirmationDto =
            ResponseUpdateController.DeferralExcusalDto.builder()
                .excusal(ResponseUpdateController.DeferralExcusalUpdateType.CONFIRMATION)
                .reason(null)
                .deferralDates(null)
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/excusal");
        final RequestEntity<ResponseUpdateController.DeferralExcusalDto> requestEntity =
            new RequestEntity<>(confirmationDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        softly.assertThat(jdbcTemplate.queryForObject("SELECT DEFERRAL_REASON FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Deferral reason column blanked out").isNullOrEmpty();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DEFERRAL_DATE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Deferral dates column blanked out").isNullOrEmpty();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EXCUSAL_REASON FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Excusal reason column blanked out").isNullOrEmpty();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isEqualTo(LOGIN_NAME);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.DEFERRAL_EXCUSAL.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE " +
            "CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items " +
            "were " +
            "created").isEqualTo(2);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateDeferralExcusal_confirmation_happy.sql")
    public void updateDeferralExcusal_deferral_happy() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isNull();

        final String DEFERRAL_REASON = "Deferral reason text.";
        final String DEFERRAL_DATES = "Updated dates.";

        final ResponseUpdateController.DeferralExcusalDto confirmationDto =
            ResponseUpdateController.DeferralExcusalDto.builder()
                .excusal(ResponseUpdateController.DeferralExcusalUpdateType.DEFERRAL)
                .reason(DEFERRAL_REASON)
                .deferralDates(DEFERRAL_DATES)
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/excusal");
        final RequestEntity<ResponseUpdateController.DeferralExcusalDto> requestEntity =
            new RequestEntity<>(confirmationDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        softly.assertThat(jdbcTemplate.queryForObject("SELECT DEFERRAL_REASON FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Deferral reason column updated").isEqualTo(DEFERRAL_REASON);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DEFERRAL_DATE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Deferral dates column updated").isEqualTo(DEFERRAL_DATES);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EXCUSAL_REASON FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Excusal reason column blanked out").isNullOrEmpty();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isEqualTo(LOGIN_NAME);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.DEFERRAL_EXCUSAL.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE " +
            "CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items " +
            "were " +
            "created").isEqualTo(2);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateDeferralExcusal_confirmation_happy.sql")
    public void updateDeferralExcusal_excusal_happy() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isNull();

        final String EXCUSAL_REASON = "Excusal reason text.";

        final ResponseUpdateController.DeferralExcusalDto confirmationDto =
            ResponseUpdateController.DeferralExcusalDto.builder()
                .excusal(ResponseUpdateController.DeferralExcusalUpdateType.EXCUSAL)
                .reason(EXCUSAL_REASON)
                .deferralDates(null)
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/excusal");
        final RequestEntity<ResponseUpdateController.DeferralExcusalDto> requestEntity =
            new RequestEntity<>(confirmationDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        softly.assertThat(jdbcTemplate.queryForObject("SELECT DEFERRAL_REASON FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Deferral reason removed").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DEFERRAL_DATE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Deferral dates column removed").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EXCUSAL_REASON FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Excusal reason column blanked out").isEqualTo(EXCUSAL_REASON);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isEqualTo(LOGIN_NAME);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.DEFERRAL_EXCUSAL.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE " +
            "CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items " +
            "were " +
            "created").isEqualTo(3);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateDeferralExcusal_confirmation_happy.sql")
    public void updateDeferralExcusal_excusal_unhappy_optimisticLock() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String EXCUSAL_REASON = "Excusal reason text.";

        final Integer INVALID_VERSION = -1;
        final ResponseUpdateController.DeferralExcusalDto confirmationDto =
            ResponseUpdateController.DeferralExcusalDto.builder()
                .excusal(ResponseUpdateController.DeferralExcusalUpdateType.EXCUSAL)
                .reason(EXCUSAL_REASON)
                .deferralDates(null)
                .notes(CHANGE_LOG_NOTES)
                .version(INVALID_VERSION)
                .build();

        // expecting a conflict response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/excusal");
        final RequestEntity<ResponseUpdateController.DeferralExcusalDto> requestEntity =
            new RequestEntity<>(confirmationDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert the DB change was not applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM",
            Integer.class)).isEqualTo(0);
        softly.assertAll();
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_specialNeeds.sql")
    public void updateSpecialNeeds_happy() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isNull();

        final String SPECIAL_NEEDS_REQUIREMENTS = "Special needs requirements text.";

        final String INSERT_LIMITED_MOBILITY = "I have limited mobility";// new row
        final String UPDATE_VISUAL_IMPAIRMENT = "I am blind in one eye";// change row
        final String DELETE_DIABETES = null;// posting null value should delete the existing special need row
        final String NOOP_HEARING_IMPAIRMENT = null; // no existing row therefore no changes
        final String INSERT_LEARNING_DISABILITY = "I am dyslexic";// new row

        final ResponseUpdateController.ReasonableAdjustmentsDto dto =
            ResponseUpdateController.ReasonableAdjustmentsDto.builder()
                .specialArrangements(SPECIAL_NEEDS_REQUIREMENTS)
                .diabetes(DELETE_DIABETES)
                .hearingImpairment(NOOP_HEARING_IMPAIRMENT)
                .learningDisability(INSERT_LEARNING_DISABILITY)
                .limitedMobility(INSERT_LIMITED_MOBILITY)
                .sightImpairment(UPDATE_VISUAL_IMPAIRMENT)
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/special-needs");
        final RequestEntity<ResponseUpdateController.ReasonableAdjustmentsDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);
        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject("SELECT SPECIAL_NEEDS_ARRANGEMENTS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class)).as("Special need arrangements").isEqualTo(SPECIAL_NEEDS_REQUIREMENTS);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Correct number of special needs present").isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isEqualTo(LOGIN_NAME);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.REASONABLE_ADJUSTMENTS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items were created").isEqualTo(5);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).as("Correct TOTAL number of change log items were created").isEqualTo(5);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_specialNeeds.sql")
    public void updateSpecialNeeds_unhappy_optimisticLock() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String SPECIAL_NEEDS_REQUIREMENTS = "Special needs requirements text.";

        final String INSERT_LIMITED_MOBILITY = "I have limited mobility";// new row
        final String UPDATE_VISUAL_IMPAIRMENT = "I am blind in one eye";// change row
        final String DELETE_DIABETES = null;// posting null value should delete the existing special need row
        final String NOOP_HEARING_IMPAIRMENT = null; // no existing row therefore no changes
        final String INSERT_LEARNING_DISABILITY = "I am dyslexic";// new row

        final ResponseUpdateController.ReasonableAdjustmentsDto dto =
            ResponseUpdateController.ReasonableAdjustmentsDto.builder()
                .specialArrangements(SPECIAL_NEEDS_REQUIREMENTS)
                .diabetes(DELETE_DIABETES)
                .hearingImpairment(NOOP_HEARING_IMPAIRMENT)
                .learningDisability(INSERT_LEARNING_DISABILITY)
                .limitedMobility(INSERT_LIMITED_MOBILITY)
                .sightImpairment(UPDATE_VISUAL_IMPAIRMENT)
                .notes(CHANGE_LOG_NOTES)
                .version(-1)
                .build();

        // expecting an conflict response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/special-needs");
        final RequestEntity<ResponseUpdateController.ReasonableAdjustmentsDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert db state is unchanged.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_eligibility.sql")
    public void updateJurorEligibility_happy() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool = jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = " +
            "'352004504'");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isNull();

        final Boolean RESIDENCY = false;
        final String RESIDENCY_DETAIL = "I lived in an airship until last year";
        final Boolean MENTAL_HEALTH_ACT = true;
        final String MENTAL_HEALTH_ACT_DETAILS = "I have altitude-induced insanity";
        final Boolean BAIL = true;
        final String BAIL_DETAILS = "I am on bail";
        final Boolean CONVICTIONS = true;
        final String CONVICTIONS_DETAILS = "I was convicted for a total misunderstanding no big deal";

        final ResponseUpdateController.JurorEligibilityDto dto = ResponseUpdateController.JurorEligibilityDto.builder()
            .version(0)
            .notes(CHANGE_LOG_NOTES)
            .residency(RESIDENCY)
            .residencyDetails(RESIDENCY_DETAIL)
            .mentalHealthAct(MENTAL_HEALTH_ACT)
            .mentalHealthActDetails(MENTAL_HEALTH_ACT_DETAILS)
            .bail(BAIL)
            .bailDetails(BAIL_DETAILS)
            .convictions(CONVICTIONS)
            .convictionsDetails(CONVICTIONS_DETAILS)
            .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/eligibility");
        final RequestEntity<ResponseUpdateController.JurorEligibilityDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert database changes were made
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM",
            Integer.class)).isEqualTo(8);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = '352004504'"))
            .containsAllEntriesOf(originalPool);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isEqualTo(LOGIN_NAME);

        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESIDENCY FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Residency column should be updated").isEqualTo("N");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESIDENCY_DETAIL FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Residency detail column should be updated").isEqualTo(RESIDENCY_DETAIL);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT MENTAL_HEALTH_ACT FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Mental health act column should updated").isEqualTo("Y");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT MENTAL_HEALTH_ACT_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Mental health act details column should updated").isEqualTo(MENTAL_HEALTH_ACT_DETAILS);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT BAIL FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Bail column should be updated").isEqualTo("Y");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT BAIL_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Bail details column should be updated").isEqualTo(BAIL_DETAILS);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CONVICTIONS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Convictions column should be updated").isEqualTo("Y");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CONVICTIONS_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Convictions details column should be updated").isEqualTo(CONVICTIONS_DETAILS);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("Version has been zeroed").isEqualTo(0);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.ELIGIBILITY.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE " +
            "CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items " +
            "were " +
            "created").isEqualTo(8);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_eligibility.sql")
    public void updateJurorEligibility_unhappy_optimisticLocking() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool = jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = " +
            "'352004504'");

        final Boolean RESIDENCY = false;
        final String RESIDENCY_DETAIL = "I lived in an airship until last year";
        final Boolean MENTAL_HEALTH_ACT = true;
        final String MENTAL_HEALTH_ACT_DETAILS = "I have altitude-induced insanity";
        final Boolean BAIL = true;
        final String BAIL_DETAILS = "I am on bail";
        final Boolean CONVICTIONS = true;
        final String CONVICTIONS_DETAILS = "I was convicted for a total misunderstanding no big deal";

        final int INVALID_VERSION = -1;

        final ResponseUpdateController.JurorEligibilityDto dto = ResponseUpdateController.JurorEligibilityDto.builder()
            .version(INVALID_VERSION)
            .notes(CHANGE_LOG_NOTES)
            .residency(RESIDENCY)
            .residencyDetails(RESIDENCY_DETAIL)
            .mentalHealthAct(MENTAL_HEALTH_ACT)
            .mentalHealthActDetails(MENTAL_HEALTH_ACT_DETAILS)
            .bail(BAIL)
            .bailDetails(BAIL_DETAILS)
            .convictions(CONVICTIONS)
            .convictionsDetails(CONVICTIONS_DETAILS)
            .build();

        // expecting an optimistic lock error response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/eligibility");
        final RequestEntity<ResponseUpdateController.JurorEligibilityDto> requestEntity = new RequestEntity<>(dto,
            httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        softly.assertThat(exchange.getBody()).isNotNull();
        softly.assertThat(exchange.getBody().getException())
            .isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert database changes were not made
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM JUROR.POOL WHERE PART_NO = '352004504'"))
            .containsAllEntriesOf(originalPool);

        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESIDENCY FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Residency column should not be updated").isEqualTo("Y");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESIDENCY_DETAIL FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Residency detail column should not be updated").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT MENTAL_HEALTH_ACT FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Mental health act column should not updated").isEqualTo("N");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT MENTAL_HEALTH_ACT_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Mental health act details column should not updated").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT BAIL FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Bail column should not be updated").isEqualTo("N");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT BAIL_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Bail details column should not be updated").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CONVICTIONS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Convictions column should not be updated").isEqualTo("N");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CONVICTIONS_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Convictions details column should not be updated").isEqualTo(null);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .as("No change log entry should be created").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM",
                Integer.class))
            .as("No change log items should be created").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateCjsEmployment.sql")
    public void updateCjsEmployment_happy_updateAll() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isNull();

        final String POLICE_DETAILS = "I worked for the police";
        final String PRISON_DETAILS = "I worked in a prison";
        final Boolean NCA_EMPLOYMENT = true;
        final Boolean JUDICIARY_EMPLOYMENT = true;
        final Boolean HMCTS_EMPLOYMENT = true;
        final String OTHER_DETAILS = "I wear a fake sheriffs badge on the weekends and hassle loiterers by the KFC";

        final ResponseUpdateController.CJSEmploymentDetailsDto dto =
            ResponseUpdateController.CJSEmploymentDetailsDto.builder()
                .policeForceDetails(POLICE_DETAILS)
                .prisonServiceDetails(PRISON_DETAILS)
                .ncaEmployment(NCA_EMPLOYMENT)
                .judiciaryEmployment(JUDICIARY_EMPLOYMENT)
                .hmctsEmployment(HMCTS_EMPLOYMENT)
                .otherDetails(OTHER_DETAILS)
                .jurorNumber(JUROR_NUMBER)
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/cjs");
        final RequestEntity<ResponseUpdateController.CJSEmploymentDetailsDto> requestEntity = new RequestEntity<>(dto
            , httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(6);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CJS_EMPLOYER_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Police Force' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Police employer details should be updated").isEqualTo(POLICE_DETAILS);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CJS_EMPLOYER_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'HM Prison Service' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Prison service details should be added").isEqualTo(PRISON_DETAILS);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CJS_EMPLOYER_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'National Crime Agency' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("NCA details should be added").isEqualTo(CjsEmployment.NCA.getDescription());

        softly.assertThat(jdbcTemplate.queryForObject("SELECT CJS_EMPLOYER_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Judiciary' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Judiciary details should be added").isEqualTo(CjsEmployment.JUDICIARY.getDescription());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CJS_EMPLOYER_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'HMCTS' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("HMCTS details should be added").isEqualTo(CjsEmployment.HMCTS.getDescription());

        softly.assertThat(jdbcTemplate.queryForObject("SELECT CJS_EMPLOYER_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Other' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Other CJS employer details should be added").isEqualTo(OTHER_DETAILS);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '352004504'", String.class)).isEqualTo(LOGIN_NAME);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.CJS_EMPLOYMENTS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items were created").isEqualTo(6);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateCjsEmploymentIncludingNca.sql")
    public void updateCjsEmployment_happy_removeTwoAndUpdateOne() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String POLICE_DETAILS = null;
        final String PRISON_DETAILS = null;
        final Boolean NCA_EMPLOYMENT = false;
        final Boolean JUDICIARY_EMPLOYMENT = false;
        final Boolean HMCTS_EMPLOYMENT = false;
        final String OTHER_DETAILS = "I wear a fake sheriffs badge on the weekends and hassle loiterers by the KFC";

        final ResponseUpdateController.CJSEmploymentDetailsDto dto =
            ResponseUpdateController.CJSEmploymentDetailsDto.builder()
                .policeForceDetails(POLICE_DETAILS)
                .prisonServiceDetails(PRISON_DETAILS)
                .ncaEmployment(NCA_EMPLOYMENT)
                .judiciaryEmployment(JUDICIARY_EMPLOYMENT)
                .hmctsEmployment(HMCTS_EMPLOYMENT)
                .otherDetails(OTHER_DETAILS)
                .jurorNumber(JUROR_NUMBER)
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/cjs");
        final RequestEntity<ResponseUpdateController.CJSEmploymentDetailsDto> requestEntity = new RequestEntity<>(dto
            , httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Police Force' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("Police employer details should be removed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'HM Prison Service' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("Prison service employer details should be removed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'National Crime Agency' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("NCA details should be updated").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CJS_EMPLOYER_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Other' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Other CJS employer details should be updated").isEqualTo(OTHER_DETAILS);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.CJS_EMPLOYMENTS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items were created").isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).as("Correct TOTAL number of change log items were created").isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateCjsEmploymentIncludingNca.sql")
    public void updateCjsEmployment_happy_removeAllWithNull() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String POLICE_DETAILS = null;
        final String PRISON_DETAILS = null;
        final Boolean NCA_EMPLOYMENT = null; // NCA has different logic to others, RE: null, as it's a boolean
        final Boolean JUDICIARY_EMPLOYMENT = false;
        final Boolean HMCTS_EMPLOYMENT = false;
        final String OTHER_DETAILS = null;

        final ResponseUpdateController.CJSEmploymentDetailsDto dto =
            ResponseUpdateController.CJSEmploymentDetailsDto.builder()
                .policeForceDetails(POLICE_DETAILS)
                .prisonServiceDetails(PRISON_DETAILS)
                .ncaEmployment(NCA_EMPLOYMENT)
                .judiciaryEmployment(JUDICIARY_EMPLOYMENT)
                .hmctsEmployment(HMCTS_EMPLOYMENT)
                .otherDetails(OTHER_DETAILS)
                .jurorNumber(JUROR_NUMBER)
                .notes(CHANGE_LOG_NOTES)
                .version(0)
                .build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/cjs");
        final RequestEntity<ResponseUpdateController.CJSEmploymentDetailsDto> requestEntity = new RequestEntity<>(dto
            , httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Police Force' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("Police employer details should be removed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'HM Prison Service' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("Prison service employer details should be removed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'National Crime Agency' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("NCA details should be updated").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Other' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("Other CJS employer details should be updated").isEqualTo(0);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG",
            String.class)).isEqualTo(JUROR_NUMBER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.CJS_EMPLOYMENTS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(CHANGE_LOG_NOTES);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId, Integer.class)).as("Correct number of change log items were created").isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).as("Correct TOTAL number of change log items were created").isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class)).as("Version has been zeroed").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateCjsEmployment.sql")
    public void updateCjsEmployment_unhappy_optimisticLocking() throws Exception {
        final String LOGIN_NAME = "BUREAULADY9";
        final String STAFF_BUREAU_LADY = "Bureau Lady";
        final String JUROR_NUMBER = "352004504";
        final String CHANGE_LOG_NOTES = "Some change log notes.";
        final Integer INVALID_VERSION = -1;

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name(STAFF_BUREAU_LADY)
                .courts(Collections.singletonList("448"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String POLICE_DETAILS = "I worked for the police";
        final String PRISON_DETAILS = "I worked in a prison";
        final Boolean NCA_EMPLOYMENT = true;
        final Boolean JUDICIARY_EMPLOYMENT = true;
        final Boolean HMCTS_EMPLOYMENT = true;
        final String OTHER_DETAILS = "I wear a fake sheriffs badge on the weekends and hassle loiterers by the KFC";

        final ResponseUpdateController.CJSEmploymentDetailsDto dto =
            ResponseUpdateController.CJSEmploymentDetailsDto.builder()
                .policeForceDetails(POLICE_DETAILS)
                .prisonServiceDetails(PRISON_DETAILS)
                .ncaEmployment(NCA_EMPLOYMENT)
                .judiciaryEmployment(JUDICIARY_EMPLOYMENT)
                .hmctsEmployment(HMCTS_EMPLOYMENT)
                .otherDetails(OTHER_DETAILS)
                .jurorNumber(JUROR_NUMBER)
                .notes(CHANGE_LOG_NOTES)
                .version(INVALID_VERSION)
                .build();

        // expecting a conflict response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + JUROR_NUMBER + "/details/cjs");
        final RequestEntity<ResponseUpdateController.CJSEmploymentDetailsDto> requestEntity = new RequestEntity<>(dto
            , httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert the DB change was not applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.POOL WHERE PART_NO = '352004504'",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PHONE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_CJS_EMPLOYMENT", Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL" +
            ".JUROR_RESPONSE_SPECIAL_NEEDS", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_HIST", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.PART_AMENDMENTS", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM",
            Integer.class)).isEqualTo(0);

        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CJS_EMPLOYER_DETAILS FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Police Force' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", String.class))
                .as("Police employer details should not be updated").isNotEqualTo(POLICE_DETAILS);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'HM Prison Service' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("Prison service details should not be added").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'National Crime Agency' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("NCA details should not be added").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT" +
                " WHERE CJS_EMPLOYER = 'Other' AND JUROR_NUMBER = '" + JUROR_NUMBER + "'", Integer.class))
                .as("Other CJS employer details should not be added").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateStatusControllerTest_legacy_status_2_changed.sql")
    public void updateResponseStatus_legacy_status_2_changed() throws Exception {

        //final Date beforeTest = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
        final String description = "Update juror response if legacy status changed.";

        final URI uri = URI.create("/api/v1/bureau/juror/644892530/response/status");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("testlogin")
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        // assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(1);


        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(ProcessingStatus.CLOSED)
            .version(2)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after update the status.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(1);


        // assert the change to DOB was applied and audited
        assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM JUROR.POOL WHERE PART_NO = '644892530'",
            String.class)).isEqualTo("2");
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_STATUS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("CLOSED");

        assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD " +
            "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("CLOSED");

    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateStatusControllerTest_legacy_status_11_changed.sql")
    public void updateResponseStatus_legacy_status_11_changed() throws Exception {

        //final Date beforeTest = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
        final String description = "Update juror response if legacy status changed.";

        final URI uri = URI.create("/api/v1/bureau/juror/644892530/response/status");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("testlogin")
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        // assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.PART_HIST", Integer.class)).isEqualTo(1);


        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(ProcessingStatus.AWAITING_TRANSLATION)
            .version(2)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after update the status.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(1);


        // assert the change to DOB was applied and audited
        assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM JUROR.POOL WHERE PART_NO = '644892530'",
            String.class)).isEqualTo("11");
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_STATUS FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("AWAITING_TRANSLATION");

        assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD " +
            "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("AWAITING_TRANSLATION");

    }


}
