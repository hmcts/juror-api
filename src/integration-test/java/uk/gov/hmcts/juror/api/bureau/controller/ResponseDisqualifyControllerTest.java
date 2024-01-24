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
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyCodeDto;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.domain.DisqualifyCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ResponseDisqualifyController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResponseDisqualifyControllerTest extends AbstractIntegrationTest {

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
    @Sql("/db/standing_data.sql")
    public void getDisqualifyReasons_happy() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        ResponseEntity<DisqualifyReasonsDto> responseEntity = template.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/bureau/juror/disqualify")), DisqualifyReasonsDto.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseEntity.getBody()).isNotNull();

        List<DisqualifyCodeDto> data = responseEntity.getBody().getData();
        assertThat(data.size()).isGreaterThan(0);

        // JDB-1458: unable to assert actual list of reasons as they may change, but we know 'E' shouldn't be in it
        assertThat(data)
            .filteredOn("disqualifyCode", DisCode.ELECTRONIC_POLICE_CHECK_FAILURE)
            .isEmpty();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void disqualifyJuror_happy() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        DisqualifyCodeEntity disqualifyCodeEntity = new DisqualifyCodeEntity("B", "On Bail");
        DisqualifyCodeDto disqualifyDto =
            new DisqualifyCodeDto(disqualifyCodeEntity);
        disqualifyDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/disqualify/644892530");
        RequestEntity<DisqualifyCodeDto> requestEntity =
            new RequestEntity<>(disqualifyDto, httpHeaders, HttpMethod.POST, uri);

        // database assertions before changes
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
                "JUROR_NUMBER = '644892530'", String.class))
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
        softly.assertThat(jdbcTemplate.queryForObject("SELECT TITLE FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's title should be updated to be MS")
            .isEqualToIgnoringCase("MS");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT FNAME FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's named should be updated to be JUNE")
            .isEqualToIgnoringCase("JUNE");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT LNAME FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's named should be updated to be CASSILLO")
            .isEqualToIgnoringCase("CASSILLO");

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
                "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should be set to CLOSED").isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD WHERE " +
                    "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Juror Response status in audit table should be set to CLOSED")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESPONDED FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should be marked as RESPONDED")
            .isEqualTo("Y");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DISQ_CODE FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Jurors pool entry should have the appropriate disqualify code set")
            .isEqualTo(disqualifyCodeEntity.getDisqualifyCode());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should have USER_EDTQ set to user login")
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM JUROR.POOL WHERE PART_NO='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 6, meaning Disqualified")
            .isEqualTo(IPoolStatus.DISQUALIFIED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM JUROR.POOL WHERE PART_NO='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should not have a hearing date set, as they're disqualified")
            .isEqualTo((String) null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RET_DATE FROM JUROR.POOL WHERE PART_NO='644892530'",
                Date.class))
            .as("Juror's RET_DATE entry should not be wiped")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("There should be 1 entry in PART_HIST for Juror (plus 3 for TITLE, LNAME, and FNAME changes)")
            .isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530' AND HISTORY_CODE='PDIS'", Integer.class))
            .as("Juror's PART_HIST entry should have PDIS set as HISTORY_CODE")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OWNER FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530' AND HISTORY_CODE='PDIS'", String.class))
            .as("Juror's PART_HIST entries should have 400 set as the owner")
            .isEqualTo("400");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_ID FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530' AND HISTORY_CODE='PDIS'", String.class))
            .as("Juror's PART_HIST entry should have user login set as USER_ID")
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM JUROR_DIGITAL_USER.PART_HIST " +
                "WHERE PART_NO='644892530' AND HISTORY_CODE='PDIS'", String.class))
            .as("Juror's PART_HIST entry should have the appropriate code set as OTHER_INFORMATION")
            .isEqualTo("Code " + disqualifyCodeEntity.getDisqualifyCode());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT POOL_NO FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530' AND HISTORY_CODE='PDIS'", String.class))
            .as("Juror's PART_HIST entry should have the appropriate pool code set as POOL_NO")
            .isEqualTo("555");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DISQ_CODE FROM JUROR.DISQ_LETT WHERE " +
                "PART_NO='644892530'", String.class))
            .as("Juror's DISQ_LETT entry should have the appropriate disqualify code set as DISQ_CODE")
            .isEqualTo(disqualifyCodeEntity.getDisqualifyCode());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
                "JUROR_NUMBER = '644892530'", String.class))
            .as("As response was in the backlog, it needs assigned to the logged in user changing it.")
            .isEqualTo(LOGIN_NAME);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void disqualifyJuror_happy_ageDisqualification() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        DisqualifyCodeEntity disqualifyCodeEntity = new DisqualifyCodeEntity("A", "Less Than Eighteen Years of Age or" +
            " Over 70");
        DisqualifyCodeDto disqualifyDto = new DisqualifyCodeDto(disqualifyCodeEntity);
        disqualifyDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/disqualify/644892530");
        RequestEntity<DisqualifyCodeDto> requestEntity =
            new RequestEntity<>(disqualifyDto, httpHeaders, HttpMethod.POST, uri);

        // make request
        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        softly.assertThat(responseEntity.getBody()).isNull();

        // database assertions
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
                "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Juror Response status should be set to CLOSED").isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD WHERE " +
                    "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Juror Response status in audit table should be set to CLOSED")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESPONDED FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should be marked as RESPONDED")
            .isEqualTo("Y");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DISQ_CODE FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Jurors pool entry should have the appropriate disqualify code set")
            .isEqualTo(disqualifyCodeEntity.getDisqualifyCode());
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should have USER_EDTQ set to user login")
            .isEqualTo(LOGIN_NAME);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM JUROR.POOL WHERE PART_NO='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 6, meaning Disqualified")
            .isEqualTo(IPoolStatus.DISQUALIFIED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM JUROR.POOL WHERE PART_NO='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should not have a hearing date set, as they're disqualified")
            .isEqualTo((String) null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("There should be 2 entries in PART_HIST for Juror due to being an age disqualification (plus 3 for " +
                "TITLE, LNAME, and FNAME changes)")
            .isEqualTo(5);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530' AND HISTORY_CODE='PDIS' AND OTHER_INFORMATION='Disqualify Code A'", Integer.class))
            .as("The first entry in PART_HIST should say PDIS and 'Disqualify Code A'")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                    "PART_NO='644892530' AND HISTORY_CODE='RDIS' AND OTHER_INFORMATION='Disqualify Letter Code A'",
                Integer.class))
            .as("The first entry in PART_HIST should say RDIS and 'Disqualify Letter Code A'")
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DISQ_CODE FROM JUROR.DISQ_LETT WHERE " +
                "PART_NO='644892530'", String.class))
            .as("Juror's DISQ_LETT entry should have the appropriate disqualify code set as DISQ_CODE")
            .isEqualTo(disqualifyCodeEntity.getDisqualifyCode());
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void disqualifyJuror_unhappy_invalidDisqualifyCode() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        DisqualifyCodeEntity disqualifyCodeEntity = new DisqualifyCodeEntity("Z", "Zinvalid code");
        DisqualifyCodeDto disqualifyDto = new DisqualifyCodeDto(disqualifyCodeEntity);
        disqualifyDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/disqualify/644892530");
        RequestEntity<DisqualifyCodeDto> requestEntity =
            new RequestEntity<>(disqualifyDto, httpHeaders, HttpMethod.POST, uri);

        // database assertions before request
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE " +
            "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();

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
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
                "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='TODO'", Integer.class))
            .as("Juror Response status should be set to TODO").isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD WHERE " +
                    "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Should not be a relevant entry in the audit table")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESPONDED FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isEqualTo("N");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DISQ_CODE FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Jurors pool entry should not have a disqualify code set")
            .isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should not have a USER_EDTQ set")
            .isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM JUROR.POOL WHERE PART_NO='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 1, meaning Summoned")
            .isEqualTo(IPoolStatus.SUMMONED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM JUROR.POOL WHERE PART_NO='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should still have a hearing date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.DISQ_LETT WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a DISQ_LETT entry")
            .isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE " +
            "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void disqualifyJuror_unhappy_jurorNotFound() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        DisqualifyCodeEntity disqualifyCodeEntity = new DisqualifyCodeEntity("B", "On Bail");
        DisqualifyCodeDto disqualifyDto = new DisqualifyCodeDto(disqualifyCodeEntity);
        disqualifyDto.setVersion(555);

        URI uri = URI.create("/api/v1/bureau/juror/disqualify/123456789");
        RequestEntity<DisqualifyCodeDto> requestEntity =
            new RequestEntity<>(disqualifyDto, httpHeaders, HttpMethod.POST, uri);

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
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
                "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='TODO'", Integer.class))
            .as("Juror Response status should be set to TODO").isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD WHERE " +
                    "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Should not be a relevant entry in the audit table")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESPONDED FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isEqualTo("N");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DISQ_CODE FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Jurors pool entry should not have a disqualify code set")
            .isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should not have a USER_EDTQ set")
            .isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM JUROR.POOL WHERE PART_NO='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 1, meaning Summoned")
            .isEqualTo(IPoolStatus.SUMMONED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM JUROR.POOL WHERE PART_NO='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should still have a hearing date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.DISQ_LETT WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a DISQ_LETT entry")
            .isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void disqualifyJuror_unhappy_incorrectVersion() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        DisqualifyCodeEntity disqualifyCodeEntity = new DisqualifyCodeEntity("B", "On Bail");
        DisqualifyCodeDto disqualifyDto = new DisqualifyCodeDto(disqualifyCodeEntity);
        disqualifyDto.setVersion(554);

        URI uri = URI.create("/api/v1/bureau/juror/disqualify/644892530");
        RequestEntity<DisqualifyCodeDto> requestEntity =
            new RequestEntity<>(disqualifyDto, httpHeaders, HttpMethod.POST, uri);

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
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
                "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='TODO'", Integer.class))
            .as("Juror Response status should be set to TODO").isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD WHERE " +
                    "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Should not be a relevant entry in the audit table")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESPONDED FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isEqualTo("N");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DISQ_CODE FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Jurors pool entry should not have a disqualify code set")
            .isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should not have a USER_EDTQ set")
            .isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM JUROR.POOL WHERE PART_NO='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 1, meaning Summoned")
            .isEqualTo(IPoolStatus.SUMMONED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM JUROR.POOL WHERE PART_NO='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should still have a hearing date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.DISQ_LETT WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a DISQ_LETT entry")
            .isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDisqualifyAndExcusalControllerTestData.sql"
    })
    public void disqualifyJuror_unhappy_nullVersion() throws Exception {
        final String LOGIN_NAME = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(LOGIN_NAME)
            .daysToExpire(89)
            .owner("400")
            .build())
        );

        DisqualifyCodeEntity disqualifyCodeEntity = new DisqualifyCodeEntity("B", "On Bail");
        DisqualifyCodeDto disqualifyDto = new DisqualifyCodeDto(disqualifyCodeEntity);
        disqualifyDto.setVersion(null);

        URI uri = URI.create("/api/v1/bureau/juror/disqualify/644892530");
        RequestEntity<DisqualifyCodeDto> requestEntity =
            new RequestEntity<>(disqualifyDto, httpHeaders, HttpMethod.POST, uri);

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
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
                "JUROR_NUMBER='644892530' AND PROCESSING_STATUS='TODO'", Integer.class))
            .as("Juror Response status should be set to TODO").isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD WHERE " +
                    "JUROR_NUMBER='644892530' AND OLD_PROCESSING_STATUS='TODO' AND NEW_PROCESSING_STATUS='CLOSED'",
                Integer.class))
            .as("Should not be a relevant entry in the audit table")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT RESPONDED FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should not be marked as RESPONDED")
            .isEqualTo("N");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DISQ_CODE FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Jurors pool entry should not have a disqualify code set")
            .isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM JUROR.POOL WHERE PART_NO='644892530'",
                String.class))
            .as("Juror's pool entry should not have a USER_EDTQ set")
            .isEqualTo(null);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM JUROR.POOL WHERE PART_NO='644892530'",
                Long.class))
            .as("Juror's pool entry should have STATUS set to 1, meaning Summoned")
            .isEqualTo(IPoolStatus.SUMMONED);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM JUROR.POOL WHERE PART_NO='644892530'",
                Timestamp.class))
            .as("Juror's pool entry should still have a hearing date set")
            .isNotNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL_USER.PART_HIST WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a PART_HIST entry")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR.DISQ_LETT WHERE " +
                "PART_NO='644892530'", Integer.class))
            .as("Juror should not have a DISQ_LETT entry")
            .isEqualTo(0);
        softly.assertAll();
    }
}
