package uk.gov.hmcts.juror.api.bureau.controller;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController.ExcusalCodeDto;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController.ExcusalReasonsDto;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ResponseExcusalController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResponseExcusalControllerTest extends AbstractIntegrationTest {

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
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    public void getExcusalReasons_happy() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ResponseEntity<ExcusalReasonsDto> responseEntity = template.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/bureau/juror/excuse")), ExcusalReasonsDto.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseEntity.getBody()).isNotNull();

        List<ExcusalCodeDto> data = responseEntity.getBody().getData();
        assertThat(data.size()).isGreaterThan(0);
        assertThat(data.size())
            .as("Excusal code list retrieved should match count of excusal codes in db")
            .isEqualTo(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.t_exc_code WHERE enabled=true",
                Integer.class));
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void excuseJuror_happy() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );
        ExcusalCodeDto excusalDto = new ExcusalCodeDto(ExcusalCodeEnum.B);
        excusalDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/excuse/644892530");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // database assertions before changes
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER = '644892530'", String.class))
            .isNull();

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        softly.assertThat(responseEntity.getBody()).isNull();

        // assert differences were merged from JUROR_RESPONSE to POOL
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT TITLE FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's title should be updated to be MS")
            .isEqualToIgnoringCase("MS");
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT first_name FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's named should be updated to be JUNE")
            .isEqualToIgnoringCase("JUNE");
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's named should be updated to be CASSILLO")
            .isEqualToIgnoringCase("CASSILLO");

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should be set to CLOSED")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response "
                + "WHERE JUROR_NUMBER='644892530'", Boolean.class))
            .as("Juror Response should have PROCESSING_COMPLETE flag set")
            .isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COMPLETED_AT FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530'", Date.class))
            .as("Juror Response should have COMPLETED_AT time set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD WHERE "
                    + "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Juror Response status in audit table should be set to CLOSED")
            .isEqualTo(1);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry should be marked as RESPONDED")
            .isEqualTo(true);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Jurors pool entry should have the appropriate excusal code set")
            .isEqualTo(excusalDto.getExcusalCode());
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM juror_mod.juror_pool WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's pool entry should have USER_EDTQ set to user login")
            .isEqualTo(loginName);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 5, meaning Excused")
            .isEqualTo(IPoolStatus.EXCUSED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should not have a hearing date set, as they're excused")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", Integer.class))
            .as("Juror's PART_HIST entry should have PEXC set as HISTORY_CODE")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_ID FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST entry should have user login set as USER_ID")
            .isEqualTo(loginName);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM juror_mod.juror_history "
                + "WHERE juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST entry should have the appropriate code set as OTHER_INFORMATION")
            .isEqualTo("Add Excuse - " + excusalDto.getExcusalCode());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST entry should have the appropriate pool code set as POOL_NO")
            .isEqualTo("555");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EXC_CODE FROM JUROR.EXC_LETT WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's EXC_LETT entry should have the appropriate excusal code set")
            .isEqualTo(excusalDto.getExcusalCode());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER = '644892530'", String.class))
            .as("As response was in the backlog, it needs assigned to the logged in user changing it.")
            .isEqualTo(loginName);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void excuseJuror_happy_deceasedExcusal() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ExcusalCodeDto excusalDto = new ExcusalCodeDto(ExcusalCodeEnum.D);
        excusalDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/excuse/644892530");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        softly.assertThat(responseEntity.getBody()).isNull();

        // assert differences were merged from JUROR_RESPONSE to POOL
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT TITLE FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's title should be updated to be MS")
            .isEqualToIgnoringCase("MS");
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT first_name FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's named should be updated to be JUNE")
            .isEqualToIgnoringCase("JUNE");
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's named should be updated to be CASSILLO")
            .isEqualToIgnoringCase("CASSILLO");

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should be set to CLOSED")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response "
                + "WHERE JUROR_NUMBER='644892530'", Boolean.class))
            .as("Juror Response should have PROCESSING_COMPLETE flag set")
            .isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COMPLETED_AT FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530'", Date.class))
            .as("Juror Response should have COMPLETED_AT time set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD WHERE "
                    + "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Juror Response status in audit table should be set to CLOSED")
            .isEqualTo(1);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry should be marked as RESPONDED")
            .isEqualTo(true);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Jurors pool entry should have the appropriate excusal code set")
            .isEqualTo(excusalDto.getExcusalCode());
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM juror_mod.juror_pool WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's pool entry should have USER_EDTQ set to user login")
            .isEqualTo(loginName);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 5, meaning Excused")
            .isEqualTo(IPoolStatus.EXCUSED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should not have a hearing date set, as they're excused")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", Integer.class))
            .as("Juror's PART_HIST entry should have PEXC set as HISTORY_CODE")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_ID FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST entry should have user login set as USER_ID")
            .isEqualTo(loginName);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM juror_mod.juror_history "
                + "WHERE juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST entry should have the appropriate code set as OTHER_INFORMATION")
            .isEqualTo("Add Excuse - " + excusalDto.getExcusalCode());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST entry should have the appropriate pool code set as POOL_NO")
            .isEqualTo("555");

        // Deceased disqualifications should not have an excusal letter created
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.EXC_LETT WHERE PART_NO='644892530'",
                Integer.class))
            .as("Juror should not have an EXC_LETT entry as they are deceased")
            .isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void excuseJuror_unhappy_invalidExcusalCode() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ExcusalCodeDto excusalDto = new ExcusalCodeDto(555, "ZINVALID", "Invalid Excusal");

        URI uri = URI.create("/api/v1/bureau/juror/excuse/644892530");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // assertions on database state before request
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response "
            + "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        softly.assertThat(responseEntity.getBody()).isNotNull();

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should not be set to CLOSED")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response "
                + "WHERE JUROR_NUMBER='644892530'", Boolean.class))
            .as("Juror Response should not have PROCESSING_COMPLETE flag set")
            .isEqualTo(false);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COMPLETED_AT FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530'", Date.class))
            .as("Juror Response should not have a COMPLETED_AT time set")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD WHERE "
                    + "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Juror Response status in audit table should not be set to CLOSED")
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isEqualTo(false);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Jurors pool entry should not have an excusal code set")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 1, meaning Summoned")
            .isEqualTo(IPoolStatus.SUMMONED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should still have a hearing date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.EXC_LETT WHERE PART_NO='644892530'",
                Integer.class))
            .as("Juror should not have an EXC_LETT entry")
            .isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response "
            + "WHERE JUROR_NUMBER='644892530'", String.class))
            .as("Response's staff assignment should not have been updated")
            .isNull();
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void excuseJuror_unhappy_JurorNotFound() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ExcusalCodeDto excusalDto = new ExcusalCodeDto(ExcusalCodeEnum.B);
        excusalDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/excuse/123456789");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.NOT_FOUND.value());
        softly.assertThat(responseEntity.getBody()).isNotNull();

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='123456789' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror should not have a Response entry")
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror WHERE juror_number='123456789'",
                    Integer.class))
            .as("Juror should not have a Pool entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='123456789'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.EXC_LETT WHERE PART_NO='123456789'",
                Integer.class))
            .as("Juror should not have an EXC_LETT entry")
            .isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void excuseJuror_unhappy_incorrectVersion() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ExcusalCodeDto excusalDto = new ExcusalCodeDto(ExcusalCodeEnum.B);
        excusalDto.setVersion(554);

        URI uri = URI.create("/api/v1/bureau/juror/excuse/644892530");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CONFLICT.value());
        softly.assertThat(responseEntity.getBody()).isNotNull();

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should not be set to CLOSED")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response "
                + "WHERE JUROR_NUMBER='644892530'", Boolean.class))
            .as("Juror Response should not have PROCESSING_COMPLETE flag set")
            .isEqualTo(false);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COMPLETED_AT FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530'", Date.class))
            .as("Juror Response should not have a COMPLETED_AT time set")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD WHERE "
                    + "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Juror Response status in audit table should not be set to CLOSED")
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isEqualTo(false);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Jurors pool entry should not have an excusal code set")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 1, meaning Summoned")
            .isEqualTo(IPoolStatus.SUMMONED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should still have a hearing date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.EXC_LETT WHERE PART_NO='644892530'",
                Integer.class))
            .as("Juror should not have an EXC_LETT entry")
            .isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void excuseJuror_unhappy_nullVersion() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ExcusalCodeDto excusalDto = new ExcusalCodeDto(ExcusalCodeEnum.B);
        excusalDto.setVersion(null);

        URI uri = URI.create("/api/v1/bureau/juror/excuse/644892530");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        softly.assertThat(responseEntity.getBody()).isNotNull();

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should not be set to CLOSED")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response "
                + "WHERE JUROR_NUMBER='644892530'", Boolean.class))
            .as("Juror Response should not have PROCESSING_COMPLETE flag set")
            .isEqualTo(false);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COMPLETED_AT FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530'", Date.class))
            .as("Juror Response should not have a COMPLETED_AT time set")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response_AUD WHERE "
                    + "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Juror Response status in audit table should not be set to CLOSED")
            .isEqualTo(0);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isEqualTo(false);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Jurors pool entry should not have an excusal code set")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 1, meaning Summoned")
            .isEqualTo(IPoolStatus.SUMMONED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should still have a hearing date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.EXC_LETT WHERE PART_NO='644892530'",
                Integer.class))
            .as("Juror should not have an EXC_LETT entry")
            .isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void rejectExcusalRequest_happy() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ExcusalCodeDto excusalDto = new ExcusalCodeDto(ExcusalCodeEnum.B);
        excusalDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/excuse/reject/644892530");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // database assertions before changes
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER = '644892530'", String.class))
            .isNull();

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        softly.assertThat(responseEntity.getBody()).isNull();

        // assert differences were merged from JUROR_RESPONSE to POOL
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT TITLE FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's title should be updated to be MS")
            .isEqualToIgnoringCase("MS");
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT first_name FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's named should be updated to be JUNE")
            .isEqualToIgnoringCase("JUNE");
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's named should be updated to be CASSILLO")
            .isEqualToIgnoringCase("CASSILLO");

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should be set to CLOSED")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response "
                + "WHERE JUROR_NUMBER='644892530'", Boolean.class))
            .as("Juror Response should have PROCESSING_COMPLETE flag set")
            .isEqualTo(true);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry should be marked as RESPONDED")
            .isEqualTo(true);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Jurors pool entry should have the appropriate excusal code set")
            .isEqualTo(excusalDto.getExcusalCode());
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM juror_mod.juror_pool WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's pool entry should have USER_EDTQ set to user login")
            .isEqualTo(loginName);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT ACC_EXC FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry ACC_EXC set to Y, meaning jurors request was rejected")
            .isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should still have a hearing date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM juror_mod.juror_pool WHERE "
                    + "juror_number='644892530'",
                Long.class))
            .as("Juror's pool entry status should be set to 2, meaning responded")
            .isEqualTo(IPoolStatus.RESPONDED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530'", Integer.class))
            .as("Juror should have 2 PART_HIST entries (plus 3 for the merges)")
            .isEqualTo(5);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", Integer.class))
            .as("Juror should have a PART_HIST entry with PEXC set as HISTORY_CODE")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='RESP'", Integer.class))
            .as("Juror should have a PART_HIST entry with RESP set as HISTORY_CODE")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_ID FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST entry should have user login set as USER_ID")
            .isEqualTo(loginName);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM juror_mod.juror_history "
                + "WHERE juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST PEXC entry should have Refuse Excuse set as OTHER_INFORMATION")
            .isEqualTo("Refuse Excuse");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM juror_mod.juror_history "
                + "WHERE juror_number='644892530' AND HISTORY_CODE='RESP'", String.class))
            .as("Juror's PART_HIST RESP entry should have 'Responded' set as OTHER_INFORMATION")
            .isEqualTo(PartHist.RESPONDED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530' AND HISTORY_CODE='PEXC'", String.class))
            .as("Juror's PART_HIST entry should have the appropriate pool code set as POOL_NO")
            .isEqualTo("555");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EXC_CODE FROM JUROR.EXC_DENIED_LETT WHERE "
                + "PART_NO='644892530'", String.class))
            .as("Juror's EXC_DENIED_LETT entry should have the appropriate excusal code set")
            .isEqualTo(excusalDto.getExcusalCode());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DATE_EXCUSED FROM JUROR.EXC_DENIED_LETT WHERE "
                + "PART_NO='644892530'", String.class))
            .as("Juror's EXC_DENIED_LETT entry should have a date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER = '644892530'", String.class))
            .as("As response was in the backlog, it needs assigned to the logged in user changing it.")
            .isEqualTo(loginName);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void rejectExcusalRequest_unhappy_invalidExcusalCode() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ExcusalCodeDto excusalDto = new ExcusalCodeDto(ExcusalCodeEnum.B);

        URI uri = URI.create("/api/v1/bureau/juror/excuse/reject/644892530");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        softly.assertThat(responseEntity.getBody()).isNotNull();

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should not be set to CLOSED")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response "
                + "WHERE JUROR_NUMBER='644892530'", Boolean.class))
            .as("Juror Response should not have PROCESSING_COMPLETE flag set")
            .isNotEqualTo(true);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isNotEqualTo(true);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Jurors pool entry should not have an excusal code set")
            .isNull();
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT ACC_EXC FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's pool entry should not have ACC_EXC flag set")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.EXC_DENIED_LETT WHERE "
                + "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a EXC_DENIED_LETT entry")
            .isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void rejectExcusalRequest_unhappy_JurorNotFound() throws Exception {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(loginName)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ExcusalCodeDto excusalDto = new ExcusalCodeDto(ExcusalCodeEnum.B);
        excusalDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/excuse/reject/123456789");
        RequestEntity<ExcusalCodeDto> requestEntity =
            new RequestEntity<>(excusalDto, httpHeaders, HttpMethod.POST, uri);

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.NOT_FOUND.value());
        softly.assertThat(responseEntity.getBody()).isNotNull();

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE "
                + "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should not be set to CLOSED")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response "
                + "WHERE JUROR_NUMBER='644892530'", Boolean.class))
            .as("Juror Response should not have PROCESSING_COMPLETE flag set")
            .isNotEqualTo(true);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number='644892530'",
                    Boolean.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isNotEqualTo(true);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Jurors pool entry should not have an excusal code set")
            .isNull();
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT ACC_EXC FROM juror_mod.juror WHERE juror_number='644892530'",
                    String.class))
            .as("Juror's pool entry should not have ACC_EXC flag set")
            .isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_history WHERE "
                + "juror_number='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.EXC_DENIED_LETT WHERE "
                + "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a EXC_DENIED_LETT entry")
            .isEqualTo(0);
        softly.assertAll();
    }
}
