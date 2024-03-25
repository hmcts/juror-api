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
import java.time.LocalDate;
import java.util.Collections;
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
    @Sql({"/db/truncate.sql", "/db/mod/truncate.sql", "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"})
    public void jurorNoteByJurorNumber_happy() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner("400").build()));

        ResponseEntity<ResponseUpdateController.JurorNoteDto> responseEntity = template.exchange(
            new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/bureau/juror/209092530/notes")),
            ResponseUpdateController.JurorNoteDto.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getNotes()).as("Notes column is expected")
            .isEqualTo("Juror 209092530 notes");
        assertThat(responseEntity.getBody().getVersion()).as("Hashing function has unchanged")
            .isEqualTo("eec6b39e8b7c52e0662220a8f05d98d0");
    }

    @Test
    @Sql({"/db/truncate.sql", "/db/mod/truncate.sql", "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"})
    public void jurorNoteByJurorNumber_unhappyNonExistentJuror() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner("400").build()));

        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(
            new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/bureau/juror/111222333/notes")), SpringBootErrorResponse.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getException()).as("Error is correct")
            .isEqualTo(ResponseNotesService.NoteNotFoundException.class.getTypeName());
    }

    @Test
    @Sql({"/db/truncate.sql", "/db/mod/truncate.sql", "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"})
    public void updateNoteByJurorNumber_happy() throws Exception {
        final String loginName = "testlogin";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("5").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner("400").staff(BureauJWTPayload.Staff.builder().active(1).rank(1).name(loginName)
                    .courts(Collections.singletonList("123")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '209092530'",
            String.class)).isNull();

        final String updatedNotes = "New improved note for juror 209092530";
        final String versionHash = "eec6b39e8b7c52e0662220a8f05d98d0";
        final ResponseUpdateController.JurorNoteDto noteDto =
            ResponseUpdateController.JurorNoteDto.builder().notes(updatedNotes).version(versionHash).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/209092530/notes");
        final RequestEntity<ResponseUpdateController.JurorNoteDto> requestEntity =
            new RequestEntity<>(noteDto, httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM juror_mod.juror WHERE juror_number = '209092530'",
            String.class)).isEqualTo(updatedNotes);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT USER_ID FROM juror_mod.juror_history WHERE juror_number = '209092530'",
            String.class)).isEqualTo(loginName);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT OTHER_INFORMATION FROM juror_mod.juror_history WHERE juror_number = "
                + "'209092530'", String.class)).as("Other information set to for note update")
            .isEqualTo(ResponseUpdateServiceImpl.UPDATED_NOTES);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '209092530'",
            String.class)).isEqualTo(loginName);
    }

    @Test
    @Sql({"/db/truncate.sql", "/db/mod/truncate.sql", "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"})
    public void updateNoteByJurorNumber_unhappyNonExistentJuror() throws Exception {
        final String loginName = "testlogin";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("5").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner("400").staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(loginName + " Mc" + loginName)
                        .courts(Collections.singletonList("123")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);

        final String updatedNotes = "New improved note for juror 123123123";
        final String versionHash = "eec6b39e8b7c52e0662220a8f05d98d0";
        final ResponseUpdateController.JurorNoteDto noteDto =
            ResponseUpdateController.JurorNoteDto.builder().notes(updatedNotes).version(versionHash).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/123123123/notes");
        final RequestEntity<ResponseUpdateController.JurorNoteDto> requestEntity =
            new RequestEntity<>(noteDto, httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getBody().getException()).as("Error is correct")
            .isEqualTo(ResponseNotesService.NoteNotFoundException.class.getTypeName());

        // assert the DB change was NOT applied
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM juror_mod.juror WHERE juror_number = '209092530'",
            String.class)).isNotEqualTo(updatedNotes);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '209092530'",
            String.class)).isNull();
    }

    @Test
    @Sql({"/db/truncate.sql", "/db/mod/truncate.sql", "/db/standing_data.sql",
        "/db/ResponseUpdateControllerTest_jurorNote.sql"})
    public void updateNoteByJurorNumber_unhappy_hashMismatch() throws Exception {
        final String loginName = "testlogin";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("5").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner("400").staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(loginName + " Mc" + loginName)
                        .courts(Collections.singletonList("123")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);

        final String updatedNotes = "New improved note for juror 209092530";
        final String badVersionHash = "abcdef1234567890abcdef1234567890";
        final ResponseUpdateController.JurorNoteDto noteDto =
            ResponseUpdateController.JurorNoteDto.builder().notes(updatedNotes).version(badVersionHash).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/209092530/notes");
        final RequestEntity<ResponseUpdateController.JurorNoteDto> requestEntity =
            new RequestEntity<>(noteDto, httpHeaders, HttpMethod.PUT, uri);
        ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody().getException()).as("Error is correct")
            .isEqualTo(ResponseNotesService.NoteComparisonFailureException.class.getTypeName());

        // assert the DB change was NOT applied
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM juror_mod.juror WHERE juror_number = '209092530'",
            String.class)).isNotEqualTo(updatedNotes);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_phoneLogs.sql")
    public void updatePhoneLogByJurorNumber_happy() throws Exception {
        final String loginName = "testlogin";
        final String jurorNumber = "644892530";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("5").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner("400").staff(BureauJWTPayload.Staff.builder().active(1).rank(1).name(loginName)
                    .courts(Collections.singletonList("123")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);

        final String updatedNotes = "New phone log entry for juror 644892530";
        final ResponseUpdateController.JurorPhoneLogDto noteDto =
            ResponseUpdateController.JurorPhoneLogDto.builder().notes(updatedNotes).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/phone");
        final RequestEntity<ResponseUpdateController.JurorPhoneLogDto> requestEntity =
            new RequestEntity<>(noteDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            3);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT * FROM (SELECT NOTES FROM juror_mod.contact_log ORDER BY START_CALL DESC) AS TEMP LIMIT 1",
            String.class)).as("Newest phone log notes are as expected").isEqualTo(updatedNotes);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT * FROM (SELECT USER_ID FROM juror_mod.contact_log ORDER BY START_CALL DESC) AS TEMP LIMIT 1",
            String.class)).as("Newest phone log user id is as expected").isEqualTo(loginName);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT * FROM (SELECT enquiry_type FROM juror_mod.contact_log ORDER BY START_CALL DESC) AS TEMP LIMIT 1",
            String.class)).as("Newest phone log phone code is as expected")
            .isEqualTo(ResponsePhoneLogService.DEFAULT_PHONE_CODE);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT * FROM (SELECT juror_number FROM juror_mod.contact_log ORDER BY START_CALL DESC) AS TEMP LIMIT 1",
            String.class)).as("Newest phone log juror number is as expected").isEqualTo(jurorNumber);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo(loginName);
    }

    @Test
    public void validateJurorNumberPathVariable_happy_validationPass() throws Exception {
        final String validJurorNumber = "123456789";
        ResponseUpdateController.validateJurorNumberPathVariable(validJurorNumber);
        // no exception = pass
    }

    @Test
    public void validateJurorNumberPathVariable_unhappy_validationFail() throws Exception {
        final String invalidJurorNumberLong = "1234567890";// too long
        final String invalidJurorNumberShort = "12345678";// too short
        final String invalidJurorNumberAlpha = "1234S6789";// alpha char
        final String invalidJurorNumberSpecial = "!23456789";// special char

        try {
            ResponseUpdateController.validateJurorNumberPathVariable(invalidJurorNumberLong);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.validateJurorNumberPathVariable(invalidJurorNumberShort);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.validateJurorNumberPathVariable(invalidJurorNumberAlpha);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }

        try {
            ResponseUpdateController.validateJurorNumberPathVariable(invalidJurorNumberSpecial);
            Assert.fail("Did not throw validation exception");
        } catch (ValidationException ve) {
            assertThat(ve.getMessage()).contains("Juror number must be exactly 9 digits");
        }
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_firstPerson.sql")
    public void updateJurorDetailsFirstPerson_happy() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool =
            jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'",
            String.class)).isNull();

        final String updatedTitle = "Sir";//"Rev";
        final String updatedFirstName = "James";//"Jose";
        final String updatedLastName = "Rivers";//"Rivera";
        final String updatedAddress1 = "22177 Bluewing Way";//"22177 Redwing Way";
        final String updatedAddress2 = "London";//"England";
        final String updatedAddress3 = "England";//"London";
        final String updatedAddress4 = "Lonely Island";//"United Kingdom";
        final String updatedAddress5 = "Planet Earth";//null;
        final String updatedPostcode = "E17 2NY";//"EC3M 2NY";
        // Not updating DOB!
        final String updatedMainPhone = "01415555559";//"01415555557";
        final String updatedAltPhone = "07415555558";//"01415555558";
        final String updatedEmailAddress = "james.rivers@email.com";//"jose.rivera@email.com";

        final ResponseUpdateController.FirstPersonJurorDetailsDto tpDto =
            ResponseUpdateController.FirstPersonJurorDetailsDto.builder().title(updatedTitle)
                .firstName(updatedFirstName).lastName(updatedLastName).address(updatedAddress1)
                .address2(updatedAddress2).address3(updatedAddress3).address4(updatedAddress4)
                .address5(updatedAddress5).postcode(updatedPostcode).dob(LocalDate.of(1985, 8, 8))
                // change!
                .mainPhone(updatedMainPhone).altPhone(updatedAltPhone).emailAddress(updatedEmailAddress)
                .notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/first-person");
        final RequestEntity<ResponseUpdateController.FirstPersonJurorDetailsDto> requestEntity =
            new RequestEntity<>(tpDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'"))
            .containsAllEntriesOf(originalPool);
        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT TITLE FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Changed title column updated").isEqualTo(updatedTitle);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT FIRST_NAME FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Changed first name column updated").isEqualTo(updatedFirstName);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT LAST_NAME FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed last name column updated").isEqualTo(updatedLastName);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_1 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address1 column updated").isEqualTo(updatedAddress1);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_2 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address2 column updated").isEqualTo(updatedAddress2);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_3 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address3 column updated").isEqualTo(updatedAddress3);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_4 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address4 column updated").isEqualTo(updatedAddress4);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_5 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address5 column updated").isEqualTo(updatedAddress5);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT postcode FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed postcode column updated").isEqualTo(updatedPostcode);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT PHONE_NUMBER FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Changed main phone column updated").isEqualTo(updatedMainPhone);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT ALT_PHONE_NUMBER FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Changed alt phone column updated").isEqualTo(updatedAltPhone);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT EMAIL FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Changed email column updated").isEqualTo(updatedEmailAddress);

        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'", String.class))
            .isEqualTo(loginName);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.JUROR_DETAILS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(13);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .as("Correct TOTAL number of change log items were created").isEqualTo(13);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_thirdParty.sql")
    public void updateJurorDetailsThirdParty_happy() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool =
            jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'",
            String.class)).isNull();

        final boolean useJurorPhone = true;
        final boolean useJurorEmail = true;
        final String relationship = "Brother";
        final String reason = "other";
        final String otherReasonText = "This is a test third party update!";
        // otherReason = null
        final String tpFirstName = "John";
        final String tpLastName = "Doe";
        final String tpMainPhone = "01415555555";
        final String tpAltPhone = "01415555556";
        final String tpEmail = "john.doe@email.com";
        final String updatedTitle = "Sir";
        final ResponseUpdateController.ThirdPartyJurorDetailsDto tpDto =
            ResponseUpdateController.ThirdPartyJurorDetailsDto.builder().useJurorPhone(useJurorPhone)
                .useJurorEmail(useJurorEmail).relationship(relationship).thirdPartyReason(reason)
                .thirdPartyOtherReason(otherReasonText).thirdPartyFirstName(tpFirstName)
                .thirdPartyLastName(tpLastName).thirdPartyMainPhone(tpMainPhone).thirdPartyAltPhone(tpAltPhone)
                .thirdPartyEmail(tpEmail).title(updatedTitle).firstName("Jose").lastName("Rivera")
                .address("22177 Redwing Way").address2("England").address3("London").address4("United Kingdom")
                .address5(null).postcode("EC3M 2NY").dob(LocalDate.of(1995, 8, 8))
                .mainPhone("01415555557").altPhone("01415555558").emailAddress("jose.rivera@email.com")
                .notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/third-party");
        final RequestEntity<ResponseUpdateController.ThirdPartyJurorDetailsDto> requestEntity =
            new RequestEntity<>(tpDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'"))
            .containsAllEntriesOf(originalPool);

        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT TITLE FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Changed title column updated").isEqualTo(updatedTitle);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'", String.class))
            .isEqualTo(loginName);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.JUROR_DETAILS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(3);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_firstPerson.sql")
    public void updateJurorDetailsFirstPerson_unhappy_optimisticLocking() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt =
            mintBureauJwt(BureauJWTPayload.builder().userLevel("1").login(loginName).daysToExpire(89).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String updatedTitle = "Sir";//"Rev";
        final String updatedFirstName = "James";//"Jose";
        final String updatedLastName = "Rivers";//"Rivera";
        final String updatedAddress1 = "22177 Bluewing Way";//"22177 Redwing Way";
        final String updatedAddress2 = "London";//"England";
        final String updatedAddress3 = "England";//"London";
        final String updatedAddress4 = "Lonely Island";//"United Kingdom";
        final String updatedAddress5 = "Planet Earth";//null;
        final String updatedPostcode = "E17 2NY";//"EC3M 2NY";
        // Not updating DOB!
        final String updatedMainPhone = "01415555559";//"01415555557";
        final String updatedAltPhone = "07415555558";//"01415555558";
        final String updatedEmailAddress = "james.rivers@email.com";//"jose.rivera@email.com";
        final Integer invalidVersion = -1;// stale version

        final ResponseUpdateController.FirstPersonJurorDetailsDto tpDto =
            ResponseUpdateController.FirstPersonJurorDetailsDto.builder().title(updatedTitle)
                .firstName(updatedFirstName).lastName(updatedLastName).address(updatedAddress1)
                .address2(updatedAddress2).address3(updatedAddress3).address4(updatedAddress4)
                .address5(updatedAddress5).postcode(updatedPostcode).dob(LocalDate.of(1995, 8, 8))// no change!
                .mainPhone(updatedMainPhone).altPhone(updatedAltPhone).emailAddress(updatedEmailAddress)
                .notes(changeLogNotes).version(invalidVersion).build();

        // expecting an optimistic lock error response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/first-person");
        final RequestEntity<ResponseUpdateController.FirstPersonJurorDetailsDto> requestEntity =
            new RequestEntity<>(tpDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        softly.assertThat(exchange.getBody()).isNotNull();
        softly.assertThat(exchange.getBody().getException())
            .isEqualTo(BureauOptimisticLockingException.class.getTypeName());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT TITLE FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Changed title column updated").isNotEqualTo(updatedTitle);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT FIRST_NAME FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Changed first name column updated").isNotEqualTo(updatedFirstName);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT LAST_NAME FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed last name column updated").isNotEqualTo(updatedLastName);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_1 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address1 column updated").isNotEqualTo(updatedAddress1);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_2 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address2 column updated").isNotEqualTo(updatedAddress2);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_3 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address3 column updated").isNotEqualTo(updatedAddress3);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_4 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address4 column updated").isNotEqualTo(updatedAddress4);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT address_line_5 FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed address5 column updated").isNotEqualTo(updatedAddress5);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT postcode FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Changed postcode column updated").isNotEqualTo(updatedPostcode);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT PHONE_NUMBER FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Changed main phone column updated").isNotEqualTo(updatedMainPhone);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT ALT_PHONE_NUMBER FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Changed alt phone column updated").isNotEqualTo(updatedAltPhone);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT EMAIL FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Changed email column updated").isNotEqualTo(updatedEmailAddress);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_thirdParty.sql")
    public void updateJurorDetailsThirdParty_unhappy_optimisticLocking() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool =
            jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'");

        final boolean useJurorPhone = true;
        final boolean useJurorEmail = true;
        final String relationship = "Brother";
        final String reason = "nothere";
        // otherReason = null
        final String tpFirstName = "John";
        final String tpLastName = "Doe";
        final String tpMainPhone = "01415555555";
        final String tpAltPhone = "01415555556";
        final String tpEmail = "john.doe@email.com";
        final String updatedTitle = "Sir";
        final Integer invalidVersion = -1;
        final ResponseUpdateController.ThirdPartyJurorDetailsDto tpDto =
            ResponseUpdateController.ThirdPartyJurorDetailsDto.builder().useJurorPhone(useJurorPhone)
                .useJurorEmail(useJurorEmail).relationship(relationship).thirdPartyReason(reason)
                .thirdPartyFirstName(tpFirstName).thirdPartyLastName(tpLastName).thirdPartyMainPhone(tpMainPhone)
                .thirdPartyAltPhone(tpAltPhone).thirdPartyEmail(tpEmail).title(updatedTitle).firstName("Jose")
                .lastName("Rivera").address("22177 Redwing Way").address2("England").address3("London")
                .address4("United Kingdom").address5(null).postcode("EC3M 2NY").dob(LocalDate.of(1995, 8, 8))
                .mainPhone("01415555557").altPhone("01415555558").emailAddress("jose.rivera@email.com")
                .notes(changeLogNotes).version(invalidVersion).build();

        // expecting an optimistic lock error response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/third-party");
        final RequestEntity<ResponseUpdateController.ThirdPartyJurorDetailsDto> requestEntity =
            new RequestEntity<>(tpDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        softly.assertThat(exchange.getBody()).isNotNull();
        softly.assertThat(exchange.getBody().getException())
            .isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert the DB change was NOT applied
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
                    Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                    Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'"))
            .containsAllEntriesOf(originalPool);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT TITLE FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Changed title column updated").isNotEqualTo(updatedTitle);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateDeferralExcusal_confirmation_happy.sql")
    public void updateDeferralExcusal_confirmation_happy() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'",
            String.class)).isNull();

        final ResponseUpdateController.DeferralExcusalDto confirmationDto =
            ResponseUpdateController.DeferralExcusalDto.builder()
                .excusal(ResponseUpdateController.DeferralExcusalUpdateType.CONFIRMATION).reason(null)
                .deferralDates(null).notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/excusal");
        final RequestEntity<ResponseUpdateController.DeferralExcusalDto> requestEntity =
            new RequestEntity<>(confirmationDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT DEFERRAL_REASON FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Deferral reason column blanked out").isNullOrEmpty();
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT DEFERRAL_DATE FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Deferral dates column blanked out").isNullOrEmpty();
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT EXCUSAL_REASON FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Excusal reason column blanked out").isNullOrEmpty();
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'", String.class))
            .isEqualTo(loginName);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.DEFERRAL_EXCUSAL.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(2);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateDeferralExcusal_confirmation_happy.sql")
    public void updateDeferralExcusal_deferral_happy() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'",
            String.class)).isNull();

        final String deferralReason = "Deferral reason text.";
        final String deferralDates = "Updated dates.";

        final ResponseUpdateController.DeferralExcusalDto confirmationDto =
            ResponseUpdateController.DeferralExcusalDto.builder()
                .excusal(ResponseUpdateController.DeferralExcusalUpdateType.DEFERRAL).reason(deferralReason)
                .deferralDates(deferralDates).notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/excusal");
        final RequestEntity<ResponseUpdateController.DeferralExcusalDto> requestEntity =
            new RequestEntity<>(confirmationDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT DEFERRAL_REASON FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Deferral reason column updated").isEqualTo(deferralReason);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT DEFERRAL_DATE FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Deferral dates column updated").isEqualTo(deferralDates);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT EXCUSAL_REASON FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Excusal reason column blanked out").isNullOrEmpty();
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'", String.class))
            .isEqualTo(loginName);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.DEFERRAL_EXCUSAL.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(2);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateDeferralExcusal_confirmation_happy.sql")
    public void updateDeferralExcusal_excusal_happy() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'",
            String.class)).isNull();

        final String excusalReason = "Excusal reason text.";

        final ResponseUpdateController.DeferralExcusalDto confirmationDto =
            ResponseUpdateController.DeferralExcusalDto.builder()
                .excusal(ResponseUpdateController.DeferralExcusalUpdateType.EXCUSAL).reason(excusalReason)
                .deferralDates(null).notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/excusal");
        final RequestEntity<ResponseUpdateController.DeferralExcusalDto> requestEntity =
            new RequestEntity<>(confirmationDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT DEFERRAL_REASON FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Deferral reason removed").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT DEFERRAL_DATE FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Deferral dates column removed").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT EXCUSAL_REASON FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Excusal reason column blanked out").isEqualTo(excusalReason);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'", String.class))
            .isEqualTo(loginName);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.DEFERRAL_EXCUSAL.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(3);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateDeferralExcusal_confirmation_happy.sql")
    public void updateDeferralExcusal_excusal_unhappy_optimisticLock() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String excusalReason = "Excusal reason text.";

        final Integer invalidVersion = -1;
        final ResponseUpdateController.DeferralExcusalDto confirmationDto =
            ResponseUpdateController.DeferralExcusalDto.builder()
                .excusal(ResponseUpdateController.DeferralExcusalUpdateType.EXCUSAL).reason(excusalReason)
                .deferralDates(null).notes(changeLogNotes).version(invalidVersion).build();

        // expecting a conflict response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/excusal");
        final RequestEntity<ResponseUpdateController.DeferralExcusalDto> requestEntity =
            new RequestEntity<>(confirmationDto, httpHeaders, HttpMethod.POST, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert the DB change was not applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .isEqualTo(0);
        softly.assertAll();
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_specialNeeds.sql")
    public void updateSpecialNeeds_happy() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'",
            String.class)).isNull();

        final String specialNeedsRequirements = "Special needs requirements text.";

        final String insertLimitedMobility = "I have limited mobility";// new row
        final String updateVisualImpairment = "I am blind in one eye";// change row
        final String deleteDiabetes = null;// posting null value should delete the existing special need row
        final String noopHearingImpairment = null; // no existing row therefore no changes
        final String insertLearningDisability = "I am dyslexic";// new row

        final ResponseUpdateController.ReasonableAdjustmentsDto dto =
            ResponseUpdateController.ReasonableAdjustmentsDto.builder().specialArrangements(specialNeedsRequirements)
                .diabetes(deleteDiabetes).hearingImpairment(noopHearingImpairment)
                .learningDisability(insertLearningDisability).limitedMobility(insertLimitedMobility)
                .sightImpairment(updateVisualImpairment).notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/special-needs");
        final RequestEntity<ResponseUpdateController.ReasonableAdjustmentsDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);
        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT reasonable_adjustments_arrangements FROM juror_mod.juror_response WHERE JUROR_NUMBER = '"
                + jurorNumber
                + "'", String.class)).as("Special need arrangements").isEqualTo(specialNeedsRequirements);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            Integer.class)).as("Correct number of special needs present").isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'", String.class))
            .isEqualTo(loginName);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.REASONABLE_ADJUSTMENTS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(5);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .as("Correct TOTAL number of change log items were created").isEqualTo(5);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_specialNeeds.sql")
    public void updateSpecialNeeds_unhappy_optimisticLock() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String specialNeedsRequirements = "Special needs requirements text.";

        final String insertLimitedMobility = "I have limited mobility";// new row
        final String updateVisualImpairment = "I am blind in one eye";// change row
        final String deleteDiabetes = null;// posting null value should delete the existing special need row
        final String noopHearingImpairment = null; // no existing row therefore no changes
        final String insertLearningDisability = "I am dyslexic";// new row

        final ResponseUpdateController.ReasonableAdjustmentsDto dto =
            ResponseUpdateController.ReasonableAdjustmentsDto.builder().specialArrangements(specialNeedsRequirements)
                .diabetes(deleteDiabetes).hearingImpairment(noopHearingImpairment)
                .learningDisability(insertLearningDisability).limitedMobility(insertLimitedMobility)
                .sightImpairment(updateVisualImpairment).notes(changeLogNotes).version(-1).build();

        // expecting an conflict response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/special-needs");
        final RequestEntity<ResponseUpdateController.ReasonableAdjustmentsDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert db state is unchanged.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_eligibility.sql")
    public void updateJurorEligibility_happy() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool =
            jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'",
            String.class)).isNull();

        final Boolean residency = false;
        final String residencyDetail = "I lived in an airship until last year";
        final Boolean mentalHealthAct = true;
        final String mentalHealthActDetails = "I have altitude-induced insanity";
        final Boolean bail = true;
        final String bailDetails = "I am on bail";
        final Boolean convictions = true;
        final String convictionsDetails = "I was convicted for a total misunderstanding no big deal";

        final ResponseUpdateController.JurorEligibilityDto dto =
            ResponseUpdateController.JurorEligibilityDto.builder().version(0).notes(changeLogNotes)
                .residency(residency).residencyDetails(residencyDetail).mentalHealthAct(mentalHealthAct)
                .mentalHealthActDetails(mentalHealthActDetails).bail(bail).bailDetails(bailDetails)
                .convictions(convictions).convictionsDetails(convictionsDetails).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/eligibility");
        final RequestEntity<ResponseUpdateController.JurorEligibilityDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert database changes were made
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .isEqualTo(8);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'"))
            .containsAllEntriesOf(originalPool);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'", String.class))
            .isEqualTo(loginName);

        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT RESIDENCY FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Boolean.class))
            .as("Residency column should be updated").isEqualTo(false);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT RESIDENCY_DETAIL FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Residency detail column should be updated").isEqualTo(residencyDetail);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT MENTAL_HEALTH_ACT FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            Boolean.class)).as("Mental health act column should updated").isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT MENTAL_HEALTH_ACT_DETAILS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber
                    + "'", String.class)).as("Mental health act details column should updated")
            .isEqualTo(mentalHealthActDetails);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT BAIL FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'", Boolean.class))
            .as("Bail column should be updated").isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT BAIL_DETAILS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Bail details column should be updated").isEqualTo(bailDetails);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT CONVICTIONS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            Boolean.class)).as("Convictions column should be updated").isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT CONVICTIONS_DETAILS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Convictions details column should be updated").isEqualTo(convictionsDetails);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.ELIGIBILITY.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(8);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_jurorResponse_eligibility.sql")
    public void updateJurorEligibility_unhappy_optimisticLocking() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        final Map<String, Object> originalPool =
            jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'");

        final Boolean residency = false;
        final String residencyDetail = "I lived in an airship until last year";
        final Boolean mentalHealthAct = true;
        final String mentalHealthActDetails = "I have altitude-induced insanity";
        final Boolean bail = true;
        final String bailDetails = "I am on bail";
        final Boolean convictions = true;
        final String convictionsDetails = "I was convicted for a total misunderstanding no big deal";

        final int invalidVersion = -1;

        final ResponseUpdateController.JurorEligibilityDto dto =
            ResponseUpdateController.JurorEligibilityDto.builder().version(invalidVersion).notes(changeLogNotes)
                .residency(residency).residencyDetails(residencyDetail).mentalHealthAct(mentalHealthAct)
                .mentalHealthActDetails(mentalHealthActDetails).bail(bail).bailDetails(bailDetails)
                .convictions(convictions).convictionsDetails(convictionsDetails).build();

        // expecting an optimistic lock error response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/eligibility");
        final RequestEntity<ResponseUpdateController.JurorEligibilityDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        softly.assertThat(exchange.getBody()).isNotNull();
        softly.assertThat(exchange.getBody().getException())
            .isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert database changes were not made
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForMap("SELECT * FROM juror_mod.juror WHERE juror_number = '352004504'"))
            .containsAllEntriesOf(originalPool);

        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT RESIDENCY FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Boolean.class))
            .as("Residency column should not be updated").isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT RESIDENCY_DETAIL FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Residency detail column should not be updated").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT MENTAL_HEALTH_ACT FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            Boolean.class)).as("Mental health act column should not updated").isEqualTo(false);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT MENTAL_HEALTH_ACT_DETAILS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber
                + "'", String.class)).as("Mental health act details column should not updated").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT BAIL FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'", Boolean.class))
            .as("Bail column should not be updated").isEqualTo(false);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT BAIL_DETAILS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Bail details column should not be updated").isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT CONVICTIONS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            Boolean.class)).as("Convictions column should not be updated").isEqualTo(false);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT CONVICTIONS_DETAILS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("Convictions details column should not be updated").isEqualTo(null);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .as("No change log entry should be created").isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .as("No change log items should be created").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateCjsEmployment.sql")
    public void updateCjsEmployment_happy_updateAll() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'",
            String.class)).isNull();

        final String policeDetails = "I worked for the police";
        final String prisonDetails = "I worked in a prison";
        final Boolean ncaEmployment = true;
        final Boolean judiciaryEmployment = true;
        final Boolean hmctsEmployment = true;
        final String otherDetails = "I wear a fake sheriffs badge on the weekends and hassle loiterers by the KFC";

        final ResponseUpdateController.CJSEmploymentDetailsDto dto =
            ResponseUpdateController.CJSEmploymentDetailsDto.builder().policeForceDetails(policeDetails)
                .prisonServiceDetails(prisonDetails).ncaEmployment(ncaEmployment)
                .judiciaryEmployment(judiciaryEmployment).hmctsEmployment(hmctsEmployment).otherDetails(otherDetails)
                .jurorNumber(jurorNumber).notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/cjs");
        final RequestEntity<ResponseUpdateController.CJSEmploymentDetailsDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(6);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT CJS_EMPLOYER_DETAILS FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'Police Force' AND JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Police employer details should be updated").isEqualTo(policeDetails);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT CJS_EMPLOYER_DETAILS FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'HM Prison Service' AND JUROR_NUMBER = '" + jurorNumber + "'",
                String.class))
            .as("Prison service details should be added").isEqualTo(prisonDetails);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT CJS_EMPLOYER_DETAILS FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                + " WHERE CJS_EMPLOYER = 'National Crime Agency' AND JUROR_NUMBER = '" + jurorNumber + "'",
            String.class)).as("NCA details should be added").isEqualTo(CjsEmployment.NCA.getDescription());

        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT CJS_EMPLOYER_DETAILS FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'Judiciary' AND JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Judiciary details should be added").isEqualTo(CjsEmployment.JUDICIARY.getDescription());
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT CJS_EMPLOYER_DETAILS FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'HMCTS' AND JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("HMCTS details should be added").isEqualTo(CjsEmployment.HMCTS.getDescription());

        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT CJS_EMPLOYER_DETAILS FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'Other' AND JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Other CJS employer details should be added").isEqualTo(otherDetails);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE JUROR_NUMBER = '352004504'", String.class))
            .isEqualTo(loginName);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.CJS_EMPLOYMENTS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(6);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateCjsEmploymentIncludingNca.sql")
    public void updateCjsEmployment_happy_removeTwoAndUpdateOne() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String policeDetails = null;
        final String prisonDetails = null;
        final Boolean ncaEmployment = false;
        final Boolean judiciaryEmployment = false;
        final Boolean hmctsEmployment = false;
        final String otherDetails = "I wear a fake sheriffs badge on the weekends and hassle loiterers by the KFC";

        final ResponseUpdateController.CJSEmploymentDetailsDto dto =
            ResponseUpdateController.CJSEmploymentDetailsDto.builder().policeForceDetails(policeDetails)
                .prisonServiceDetails(prisonDetails).ncaEmployment(ncaEmployment)
                .judiciaryEmployment(judiciaryEmployment).hmctsEmployment(hmctsEmployment).otherDetails(otherDetails)
                .jurorNumber(jurorNumber).notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/cjs");
        final RequestEntity<ResponseUpdateController.CJSEmploymentDetailsDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(1);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                + " WHERE CJS_EMPLOYER = 'Police Force' AND JUROR_NUMBER = '" + jurorNumber + "'", Integer.class))
            .as("Police employer details should be removed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                + " WHERE CJS_EMPLOYER = 'HM Prison Service' AND JUROR_NUMBER = '" + jurorNumber + "'", Integer.class))
            .as("Prison service employer details should be removed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'National Crime Agency' AND JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("NCA details should be updated").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT CJS_EMPLOYER_DETAILS FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'Other' AND JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Other CJS employer details should be updated").isEqualTo(otherDetails);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.CJS_EMPLOYMENTS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(3);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .as("Correct TOTAL number of change log items were created").isEqualTo(3);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateCjsEmploymentIncludingNca.sql")
    public void updateCjsEmployment_happy_removeAllWithNull() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String policeDetails = null;
        final String prisonDetails = null;
        final Boolean ncaEmployment = null; // NCA has different logic to others, RE: null, as it's a boolean
        final Boolean judiciaryEmployment = false;
        final Boolean hmctsEmployment = false;
        final String otherDetails = null;

        final ResponseUpdateController.CJSEmploymentDetailsDto dto =
            ResponseUpdateController.CJSEmploymentDetailsDto.builder().policeForceDetails(policeDetails)
                .prisonServiceDetails(prisonDetails).ncaEmployment(ncaEmployment)
                .judiciaryEmployment(judiciaryEmployment).hmctsEmployment(hmctsEmployment).otherDetails(otherDetails)
                .jurorNumber(jurorNumber).notes(changeLogNotes).version(0).build();

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/cjs");
        final RequestEntity<ResponseUpdateController.CJSEmploymentDetailsDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert the DB change was applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(1);

        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                + " WHERE CJS_EMPLOYER = 'Police Force' AND JUROR_NUMBER = '" + jurorNumber + "'", Integer.class))
            .as("Police employer details should be removed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                + " WHERE CJS_EMPLOYER = 'HM Prison Service' AND JUROR_NUMBER = '" + jurorNumber + "'", Integer.class))
            .as("Prison service employer details should be removed").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'National Crime Agency' AND JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("NCA details should be updated").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                + " WHERE CJS_EMPLOYER = 'Other' AND JUROR_NUMBER = '" + jurorNumber + "'", Integer.class))
            .as("Other CJS employer details should be updated").isEqualTo(0);

        // assert changelog
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT JUROR_NUMBER FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(jurorNumber);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TYPE FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(ChangeLogType.CJS_EMPLOYMENTS.name());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NOTES FROM JUROR_DIGITAL.CHANGE_LOG", String.class))
            .isEqualTo(changeLogNotes);
        final Long changeLogId = jdbcTemplate.queryForObject("SELECT ID FROM JUROR_DIGITAL.CHANGE_LOG", Long.class);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM WHERE CHANGE_LOG_ITEM.CHANGE_LOG = " + changeLogId,
            Integer.class)).as("Correct number of change log items were created").isEqualTo(2);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .as("Correct TOTAL number of change log items were created").isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT VERSION FROM JUROR_DIGITAL.CHANGE_LOG WHERE JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("Version has been zeroed").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateControllerTest_updateCjsEmployment.sql")
    public void updateCjsEmployment_unhappy_optimisticLocking() throws Exception {
        final String loginName = "BUREAULADY9";
        final String staffBureauLady = "Bureau Lady";
        final String jurorNumber = "352004504";
        final String changeLogNotes = "Some change log notes.";
        final Integer invalidVersion = -1;

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("1").passwordWarning(false).login(loginName).daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).staff(
                    BureauJWTPayload.Staff.builder().active(1).rank(1).name(staffBureauLady)
                        .courts(Collections.singletonList("448")).build()).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class)).isEqualTo(
            0);

        final String policeDetails = "I worked for the police";
        final String prisonDetails = "I worked in a prison";
        final Boolean ncaEmployment = true;
        final Boolean judiciaryEmployment = true;
        final Boolean hmctsEmployment = true;
        final String otherDetails = "I wear a fake sheriffs badge on the weekends and hassle loiterers by the KFC";

        final ResponseUpdateController.CJSEmploymentDetailsDto dto =
            ResponseUpdateController.CJSEmploymentDetailsDto.builder().policeForceDetails(policeDetails)
                .prisonServiceDetails(prisonDetails).ncaEmployment(ncaEmployment)
                .judiciaryEmployment(judiciaryEmployment).hmctsEmployment(hmctsEmployment).otherDetails(otherDetails)
                .jurorNumber(jurorNumber).notes(changeLogNotes).version(invalidVersion).build();

        // expecting a conflict response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/juror/" + jurorNumber + "/details/cjs");
        final RequestEntity<ResponseUpdateController.CJSEmploymentDetailsDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);

        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert the DB change was not applied
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number = '352004504'",
                Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.contact_log", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_cjs_employment",
                Integer.class)).isEqualTo(1);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_reasonable_adjustment",
                Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG", Integer.class))
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.CHANGE_LOG_ITEM", Integer.class))
            .isEqualTo(0);

        //updated fields
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT CJS_EMPLOYER_DETAILS FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'Police Force' AND JUROR_NUMBER = '" + jurorNumber + "'", String.class))
            .as("Police employer details should not be updated").isNotEqualTo(policeDetails);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                + " WHERE CJS_EMPLOYER = 'HM Prison Service' AND JUROR_NUMBER = '" + jurorNumber + "'", Integer.class))
            .as("Prison service details should not be added").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                    + " WHERE CJS_EMPLOYER = 'National Crime Agency' AND JUROR_NUMBER = '" + jurorNumber + "'",
                Integer.class))
            .as("NCA details should not be added").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT"
                + " WHERE CJS_EMPLOYER = 'Other' AND JUROR_NUMBER = '" + jurorNumber + "'", Integer.class))
            .as("Other CJS employer details should not be added").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateStatusControllerTest_legacy_status_2_changed.sql")
    public void updateResponseStatus_legacy_status_2_changed() throws Exception {

        //final Date beforeTest = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
        final String description = "Update juror response if legacy status changed.";

        final URI uri = URI.create("/api/v1/bureau/juror/644892530/response/status");

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("99").passwordWarning(false).login("testlogin").daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        // assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class))
        // .isEqualTo(1);


        final BureauResponseStatusUpdateDto dto =
            BureauResponseStatusUpdateDto.builder().status(ProcessingStatus.CLOSED).version(2).build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after update the status.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            1);


        // assert the change to DOB was applied and audited
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM juror_mod.juror_pool WHERE juror_number = "
                + "'644892530'",
            String.class)).isEqualTo("2");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT PROCESSING_STATUS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("CLOSED");

        assertThat(jdbcTemplate.queryForObject(
            "SELECT NEW_PROCESSING_STATUS FROM juror_mod.juror_response_AUD WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("CLOSED");

    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/ResponseUpdateStatusControllerTest_legacy_status_11_changed.sql")
    public void updateResponseStatus_legacy_status_11_changed() throws Exception {

        //final Date beforeTest = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
        final String description = "Update juror response if legacy status changed.";

        final URI uri = URI.create("/api/v1/bureau/juror/644892530/response/status");

        final String bureauJwt = mintBureauJwt(
            BureauJWTPayload.builder().userLevel("99").passwordWarning(false).login("testlogin").daysToExpire(89)
                .owner(JurorDigitalApplication.JUROR_OWNER).build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            0);
        // assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class))
        // .isEqualTo(1);


        final BureauResponseStatusUpdateDto dto =
            BureauResponseStatusUpdateDto.builder().status(ProcessingStatus.AWAITING_TRANSLATION).version(2).build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after update the status.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_AUD", Integer.class)).isEqualTo(
            1);


        // assert the change to DOB was applied and audited
        assertThat(
            jdbcTemplate.queryForObject("SELECT status FROM juror_mod.juror_pool WHERE juror_number = '644892530'",
                String.class)).isEqualTo("11");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT PROCESSING_STATUS FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("AWAITING_TRANSLATION");

        assertThat(jdbcTemplate.queryForObject(
            "SELECT NEW_PROCESSING_STATUS FROM juror_mod.juror_response_AUD WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("AWAITING_TRANSLATION");

    }


}
