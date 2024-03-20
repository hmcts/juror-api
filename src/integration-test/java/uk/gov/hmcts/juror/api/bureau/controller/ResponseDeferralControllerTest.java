package uk.gov.hmcts.juror.api.bureau.controller;

import org.assertj.core.api.SoftAssertions;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.THistoryCode;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.bureau.service.ResponseDeferralServiceImpl;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ResponseDeferralController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.LawOfDemeter")
public class ResponseDeferralControllerTest extends AbstractIntegrationTest {
    private static final String WORK_RELATED_EXCUSAL_CODE = "W";

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDeferralControllerTest_processJurorDeferral.sql"
    })
    public void processJurorDeferralAccept_happy() throws Exception {
        final String jurorNumber = "644892530";
        final String staffLogin = "STAFF1";
        final Integer validVersion = 2;
        final LocalDate fourWeeksFromNextMonday =
            LocalDate.now().atStartOfDay().plusHours(12).with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .plusWeeks(4).toLocalDate();
        final LocalDate twelveMonthsFromNow = LocalDate.now().plusYears(1);
        final String description = "Manual deferral of juror response happy path.";

        final URI uri = URI.create("/api/v1/bureau/juror/defer/" + jurorNumber);

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(staffLogin)
            .daysToExpire(89)
            .owner(staffLogin)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE",
            Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isNotEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod"
            + ".JUROR_RESPONSE WHERE JUROR_NUMBER = '644892530'", String.class));
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.JUROR_RESPONSE WHERE "
            + "JUROR_NUMBER = '644892530'", String.class)).isNull();

        final ResponseDeferralController.DeferralDto dto = ResponseDeferralController.DeferralDto.builder()
            .version(validVersion)
            .acceptDeferral(true)
            .deferralReason(WORK_RELATED_EXCUSAL_CODE)
            .deferralDate(fourWeeksFromNextMonday)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ResponseDeferralController.DeferralDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(exchange.getBody()).isNull();

        // assert db state after merge.
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod"
            + ".juror_reasonable_adjustment", Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod"
            + ".juror_response_cjs_employment", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class))
            .isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class))
            .isEqualTo(0);

        //assert the merge has taken place
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER = '644892530'", Boolean.class)).isEqualTo(true);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
                String.class)).isEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER = '644892530'", String.class));

        // assert the last name change was applied and audited
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
                String.class)).isEqualTo("DOE");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod"
            + ".JUROR_RESPONSE_AUD WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("CLOSED");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod"
            + ".JUROR_RESPONSE_AUD WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");

        // assert the deferral was correctly set and audited
        softly.assertThat(jdbcTemplate.queryForObject("SELECT status FROM juror_mod.juror_pool WHERE juror_number = "
                + "'644892530'",
            Long.class)).isEqualTo(IPoolStatus.DEFERRED);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number = '644892530'",
                Boolean.class)).isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT def_date FROM juror_mod.juror_pool WHERE juror_number = "
                + "'644892530'",
            LocalDate.class)).isInTheFuture().isEqualTo(fourWeeksFromNextMonday).isBefore(twelveMonthsFromNow);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT date_excused FROM juror_mod.juror WHERE juror_number = '644892530'"
                , Date.class)).isInSameDayAs(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number = '644892530'",
                String.class)).isEqualTo(WORK_RELATED_EXCUSAL_CODE);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM juror_mod.juror_pool WHERE juror_number = '644892530'",
                String.class)).isEqualTo(staffLogin);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT NO_DEF_POS FROM juror_mod.juror WHERE juror_number = '644892530'"
                , Long.class)).isEqualTo(1L);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEXT_DATE FROM juror_mod.juror_pool WHERE juror_number "
                + "= '644892530'",
            String.class)).isNullOrEmpty();

        // assert defer_dbf was set correctly
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OWNER FROM JUROR.DEFER_DBF WHERE part_no = '644892530'"
            , String.class)).isEqualTo(JurorDigitalApplication.JUROR_OWNER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DEFER_TO FROM JUROR.DEFER_DBF WHERE part_no = "
                + "'644892530'", LocalDate.class)).isInTheFuture().isEqualTo(fourWeeksFromNextMonday)
            .isBefore(twelveMonthsFromNow);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT CHECKED FROM JUROR.DEFER_DBF WHERE part_no = "
            + "'644892530'", String.class)).as("CHECKED should be null").isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT LOC_CODE FROM JUROR.DEFER_DBF WHERE part_no = "
            + "'644892530'", String.class)).isEqualTo("448");

        // assert staff assignment was updated from backlog to logged in user
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.JUROR_RESPONSE WHERE "
            + "JUROR_NUMBER = '644892530'", String.class)).isEqualTo(staffLogin);

        //assert PART_HIST entries set correctly
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history WHERE juror_number = "
                + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", Integer.class)).isEqualTo(1);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT USER_ID FROM juror_mod.juror_history WHERE juror_number = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", String.class)).asString()
            .isEqualTo(staffLogin);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM juror_mod.juror_history WHERE juror_number"
                    + " = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", String.class)).asString()
            .isEqualTo(PartHist.RESPONDED);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_history WHERE juror_number = "
                + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", String.class)).isEqualTo(
            jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_pool WHERE juror_number = '644892530'",
                String.class));
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT date_created FROM juror_mod.juror_history WHERE juror_number = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", Date.class))
            .isCloseTo(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()), 5000L);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history WHERE juror_number = "
                + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", Integer.class)).isEqualTo(1);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT USER_ID FROM juror_mod.juror_history WHERE juror_number = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", String.class)).asString()
            .isEqualTo(staffLogin);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM juror_mod.juror_history WHERE juror_number"
                    + " = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", String.class)).asString()
            .startsWith(
                "Add defer - ").endsWith(WORK_RELATED_EXCUSAL_CODE);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_history WHERE juror_number = "
                + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", String.class)).isEqualTo(
            jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_pool WHERE juror_number = '644892530'",
                String.class));
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT date_created FROM juror_mod.juror_history WHERE juror_number = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", Date.class))
            .isCloseTo(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()), 5000L);

        //assert DEF_LETT entry is set correctly
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class))
            .isEqualTo(1);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT OWNER FROM JUROR.DEF_LETT WHERE part_no = '644892530'",
                String.class)).isEqualTo(JurorDigitalApplication.JUROR_OWNER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EXC_CODE FROM JUROR.DEF_LETT WHERE part_no = "
            + "'644892530'", String.class)).isEqualTo(WORK_RELATED_EXCUSAL_CODE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DATE_DEF FROM JUROR.DEF_LETT WHERE part_no = "
                + "'644892530'", LocalDate.class)).isInTheFuture().isEqualTo(fourWeeksFromNextMonday)
            .isBefore(twelveMonthsFromNow);
        softly.assertAll();
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDeferralControllerTest_processJurorDeferral.sql"
    })
    public void processJurorDeferralAccept_unhappy_optimisticLock() throws Exception {
        final String JUROR_NUMBER = "644892530";
        final String STAFF_LOGIN = "STAFF1";
        final Integer INVALID_VERSION = 1;// invalid version, 1 behind DB
        final LocalDate FOUR_WEEKS_FROM_NEXT_MONDAY =
            LocalDate.now().atStartOfDay().plusHours(12).with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .plus(4, ChronoUnit.WEEKS).toLocalDate();
        final String description = "Manual deferral of juror response unhappy path.";

        final URI uri = URI.create("/api/v1/bureau/juror/defer/" + JUROR_NUMBER);

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(STAFF_LOGIN)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE",
            Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isNotEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod"
            + ".JUROR_RESPONSE WHERE JUROR_NUMBER = '644892530'", String.class));
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();

        final ResponseDeferralController.DeferralDto dto = ResponseDeferralController.DeferralDto.builder()
            .version(INVALID_VERSION)
            .acceptDeferral(true)
            .deferralReason(WORK_RELATED_EXCUSAL_CODE)
            .deferralDate(FOUR_WEEKS_FROM_NEXT_MONDAY)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ResponseDeferralController.DeferralDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert db state after merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE",
            Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isNotEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod"
            + ".JUROR_RESPONSE WHERE JUROR_NUMBER = '644892530'", String.class));
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();
    }

    //@Test(expected = ResponseDeferralServiceImpl.DeferralDateInvalidException.class)
    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDeferralControllerTest_processJurorDeferral.sql"
    })
    public void processJurorDeferralAccept_unhappy_outOfRangeDeferralDate() throws Exception {
        final String jurorNumber = "644892530";
        final String staffLogin = "STAFF1";
        final Integer version = 2;
        final LocalDate outOfRangeDeferralDate =
            LocalDate.now().atStartOfDay().plusHours(12).with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .plus(14, ChronoUnit.MONTHS).toLocalDate();
        final String description = "Manual deferral of juror response unhappy path.";

        final URI uri = URI.create("/api/v1/bureau/juror/defer/" + jurorNumber);

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(staffLogin)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE",
            Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isNotEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod"
            + ".JUROR_RESPONSE WHERE JUROR_NUMBER = '644892530'", String.class));

        final ResponseDeferralController.DeferralDto dto = ResponseDeferralController.DeferralDto.builder()
            .version(version)
            .acceptDeferral(true)
            .deferralReason(WORK_RELATED_EXCUSAL_CODE)
            .deferralDate(outOfRangeDeferralDate)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ResponseDeferralController.DeferralDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(
            ResponseDeferralServiceImpl.DeferralDateInvalidException.class.getTypeName());

        // assert db state after merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE",
            Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isNotEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod"
            + ".JUROR_RESPONSE WHERE JUROR_NUMBER = '644892530'", String.class));
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/ResponseDeferralControllerTest_processJurorDeferral.sql"
    })
    public void processJurorDeferralDenial_happy() throws Exception {
        final String jurorNumber = "644892530";
        final String staffLogin = "STAFF1";
        final Integer validVersion = 2;
        final String description = "Manual deferral of juror response happy path.";

        final URI uri = URI.create("/api/v1/bureau/juror/defer/" + jurorNumber);

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(staffLogin)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE",
            Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isNotEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod"
            + ".juror_response WHERE JUROR_NUMBER = '644892530'", String.class));

        final ResponseDeferralController.DeferralDto dto = ResponseDeferralController.DeferralDto.builder()
            .version(validVersion)
            .acceptDeferral(false)
            .deferralReason(WORK_RELATED_EXCUSAL_CODE)
//                .deferralDate(null)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ResponseDeferralController.DeferralDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(exchange.getBody()).isNull();

        // assert db state after merge.
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod"
            + ".juror_reasonable_adjustment", Integer.class)).isEqualTo(1);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod"
            + ".juror_response_cjs_employment", Integer.class)).isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class))
            .isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class))
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class))
            .isEqualTo(1);

        //assert the merge has taken place
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER = '644892530'", Boolean.class)).isEqualTo(true);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
                String.class)).isEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER = '644892530'", String.class));

        // assert the last name change was applied and audited
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
                String.class)).isEqualTo("DOE");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod"
            + ".JUROR_RESPONSE_AUD WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("CLOSED");
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod"
            + ".JUROR_RESPONSE_AUD WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");

        // assert the deferral was correctly set and audited
        softly.assertThat(jdbcTemplate.queryForObject("SELECT STATUS FROM juror_mod.juror_pool WHERE juror_number = "
                + "'644892530'",
            Long.class)).isEqualTo(IPoolStatus.RESPONDED);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT RESPONDED FROM juror_mod.juror WHERE juror_number = '644892530'",
                Boolean.class)).isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT date_disq FROM juror_mod.juror WHERE juror_number = "
                + "'644892530'",
            Date.class)).isNull();
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT date_excused FROM juror_mod.juror WHERE juror_number = '644892530'"
                , Date.class)).isNull();
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT excusal_code FROM juror_mod.juror WHERE juror_number = '644892530'",
                String.class)).isEqualTo(WORK_RELATED_EXCUSAL_CODE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT USER_EDTQ FROM juror_mod.juror_pool WHERE juror_number "
                + "= '644892530'",
            String.class)).isEqualTo(staffLogin);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT NO_DEF_POS FROM juror_mod.juror WHERE juror_number = '644892530'"
                , Long.class)).isNull();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT next_date FROM juror_mod.juror_pool WHERE juror_number "
                + "= '644892530'",
            Date.class)).isInTheFuture();

        //assert PART_HIST entries set correctly
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history WHERE juror_number = "
                + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", Integer.class)).isEqualTo(1);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT USER_ID FROM juror_mod.juror_history WHERE juror_number = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", String.class)).asString()
            .isEqualTo(staffLogin);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM juror_mod.juror_history WHERE juror_number"
                    + " = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", String.class)).asString()
            .isEqualTo(PartHist.RESPONDED);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_history WHERE juror_number = "
                + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", String.class)).isEqualTo(
            jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_pool WHERE juror_number = '644892530'",
                String.class));
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT date_created FROM juror_mod.juror_history WHERE juror_number = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.RESPONDED + "'", Date.class))
            .isCloseTo(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()), 5000L);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history WHERE juror_number = "
                + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", Integer.class)).isEqualTo(1);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT USER_ID FROM juror_mod.juror_history WHERE juror_number = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", String.class)).asString()
            .isEqualTo(staffLogin);
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT OTHER_INFORMATION FROM juror_mod.juror_history WHERE juror_number"
                    + " = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", String.class)).asString()
            .startsWith(
                "Deferral Denied - ").endsWith(WORK_RELATED_EXCUSAL_CODE);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_history WHERE juror_number = "
                + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", String.class)).isEqualTo(
            jdbcTemplate.queryForObject("SELECT pool_number FROM juror_mod.juror_pool WHERE juror_number = '644892530'",
                String.class));
        softly.assertThat(
                jdbcTemplate.queryForObject("SELECT date_created FROM juror_mod.juror_history WHERE juror_number = "
                    + "'644892530' AND HISTORY_CODE = '" + THistoryCode.DEFERRED + "'", Date.class))
            .isCloseTo(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()), 5000L);

        //assert DEF_LETT entry is NOT set
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class))
            .isEqualTo(0);

        //assert DEF_DENIED entry is set correctly
        softly.assertThat(jdbcTemplate.queryForObject("SELECT OWNER FROM JUROR.DEF_DENIED WHERE part_no = "
            + "'644892530'", String.class)).asString().isEqualTo(JurorDigitalApplication.JUROR_OWNER);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT EXC_CODE FROM JUROR.DEF_DENIED WHERE part_no = "
            + "'644892530'", String.class)).asString().isEqualTo(WORK_RELATED_EXCUSAL_CODE);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT DATE_DEF FROM JUROR.DEF_DENIED WHERE part_no = "
            + "'644892530'", Date.class)).isCloseTo(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant())
            , 5000L);
        softly.assertAll();
    }

    @Test
    public void processJurorDeferralDenial_unhappy_optimisticLock() throws Exception {
        final String JUROR_NUMBER = "644892530";
        final String STAFF_LOGIN = "STAFF1";
        final Integer INVALID_VERSION = 1;// invalid version, 1 behind DB
        final String description = "Manual deferral of juror response unhappy path.";

        final URI uri = URI.create("/api/v1/bureau/juror/defer/" + JUROR_NUMBER);

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login(STAFF_LOGIN)
            .daysToExpire(89)
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE",
            Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isNotEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod"
            + ".JUROR_RESPONSE WHERE JUROR_NUMBER = '644892530'", String.class));
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();

        final ResponseDeferralController.DeferralDto dto = ResponseDeferralController.DeferralDto.builder()
            .version(INVALID_VERSION)
            .acceptDeferral(false)
            .deferralReason(WORK_RELATED_EXCUSAL_CODE)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ResponseDeferralController.DeferralDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody()).isNotNull();
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getTypeName());

        // assert db state after merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_AUD",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEFER_DBF", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_LETT", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.DEF_DENIED", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE",
            Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isNotEqualTo(jdbcTemplate.queryForObject("SELECT LAST_NAME FROM juror_mod"
            + ".JUROR_RESPONSE WHERE JUROR_NUMBER = '644892530'", String.class));
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();
    }
}