package uk.gov.hmcts.juror.api.bureau.controller;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsNotificationService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.SmsNotification;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ResponseSendToCourtController}.
 *
 * <p>Updated By Baharak Askarikeya - 05/07/19 - Send email & sms to super urgent send to court - JDB-3996
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResponseSendToCourtControllerTest extends AbstractIntegrationTest {

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private PoolRepository poolRepository;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @Autowired
    private JurorCommsNotificationService jurorCommsNotifiyService;

    private HttpHeaders httpHeaders;

    private static final String NOTIFY_TEMPLATE_SQL =
        "SELECT TEMPLATE_ID FROM juror_mod.NOTIFY_TEMPLATE_MAPPING WHERE TEMPLATE_NAME = ?";

    private static final String UUID_REGEX =
        "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$";
    private static final String JUROR_NUMBER_1 = "209092530";
    private static final String JUROR_NUMBER_2 = "641500130";
    private static final String JUROR_NUMBER_3 = "641500542";
    private static final String JUROR_NUMBER_4 = "641500127";
    private static final String TEMPLATE_NAME_1 = "SU_SENT_TO_COURT_EMAIL_ENG";
    private static final String TEMPLATE_NAME_2 = "SU_SENT_TO_COURT_SMS_ENG";
    private static final String EMAIL = "test@email.dev";
    private static final String VALUE2 = "value2";

    private ResponseSendToCourtController.SendToCourtDto dto;
    private Map<String, String> payLoad;
    private String templateUuid;
    private JurorPool pool;


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
        "/db/BureauResponseSendToCourtController.sql",
        "/db/notify_template_mapping.sql",
        "/db/juror-comms-notify.sql"
    })
    public void processJurorToCourt_Email_Null() throws Exception {
        assertGetDto();

        // assert db state before.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(34);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE WHERE "
            + "JUROR_NUMBER = '209092530'", Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE WHERE "
            + "JUROR_NUMBER = '209092530'", String.class)).isEqualTo("TODO");
        assertThat(jdbcTemplate.queryForObject("SELECT Email FROM juror_mod.JUROR_RESPONSE WHERE JUROR_NUMBER = "
            + "'209092530'", String.class)).isEqualTo(null);
        assertThat(jdbcTemplate.queryForObject("SELECT ALT_PHONE_NUMBER FROM juror_mod.JUROR_RESPONSE WHERE "
            + "JUROR_NUMBER = '209092530'", String.class)).isEqualTo("07554498123");

        URI uri = URI.create("/api/v1/bureau/juror/tocourt/209092530");

        RequestEntity<ResponseSendToCourtController.SendToCourtDto> requestEntity = new RequestEntity<>(dto,
            httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<SpringBootErrorResponse> responseEntity = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        assertJurorCommsEmailNotification(JUROR_NUMBER_1, TEMPLATE_NAME_1,
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);

        assertJurorCommsSmsNotification(JUROR_NUMBER_1, TEMPLATE_NAME_2,
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);

        // response assertions
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responseEntity).isNotNull();
        softly.assertThat(responseEntity.getBody()).isNotNull();


        // assert db state after.
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER = '209092530'", Boolean.class)).isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE "
            + "WHERE JUROR_NUMBER = '209092530'", String.class)).isEqualTo("CLOSED");
        softly.assertAll();
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/juror-comms-notify.sql")
    @Test
    public void processJurorToCourtSmsNull() throws Exception {
        assertGetDto();
        assertJurorCommsEmailNotification(JUROR_NUMBER_2, TEMPLATE_NAME_1,
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);

        assertJurorCommsSmsNotification(JUROR_NUMBER_2, TEMPLATE_NAME_2,
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);

        assertThat(jdbcTemplate.queryForObject("SELECT ALT_PHONE_NUMBER FROM juror_mod.JUROR_RESPONSE WHERE "
            + "JUROR_NUMBER = '641500130'", String.class)).isEqualTo(null);
        assertThat(jdbcTemplate.queryForObject("SELECT Email FROM juror_mod.JUROR_RESPONSE WHERE JUROR_NUMBER = "
            + "'641500130'", String.class)).isEqualTo(EMAIL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/juror-comms-notify.sql")
    @Test
    public void processJurorToCourtSmsAndEmailNull() throws Exception {
        assertGetDto();

        assertJurorCommsEmailNotification(JUROR_NUMBER_3, TEMPLATE_NAME_1,
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);
        assertJurorCommsSmsNotification(JUROR_NUMBER_3, TEMPLATE_NAME_2,
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);

        assertThat(jdbcTemplate.queryForObject("SELECT Email FROM juror_mod.JUROR_RESPONSE WHERE JUROR_NUMBER = "
            + "'641500542'", String.class)).isEqualTo(null);
        assertThat(jdbcTemplate.queryForObject("SELECT ALT_PHONE_NUMBER FROM juror_mod.JUROR_RESPONSE WHERE "
            + "JUROR_NUMBER = '641500542'", String.class)).isEqualTo(null);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/juror-comms-notify.sql")
    @Test
    public void sendSuSendToCourtSmsAndEmailEngComms() throws Exception {
        assertGetDto();

        assertJurorCommsEmailNotification(JUROR_NUMBER_4, TEMPLATE_NAME_1,
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);
        assertJurorCommsSmsNotification(JUROR_NUMBER_4, TEMPLATE_NAME_2,
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);

        assertThat(jdbcTemplate.queryForObject("SELECT Email FROM juror_mod.JUROR_RESPONSE WHERE JUROR_NUMBER = "
            + "'641500130'", String.class)).isEqualTo(EMAIL);
        assertThat(jdbcTemplate.queryForObject("SELECT ALT_PHONE_NUMBER FROM juror_mod.JUROR_RESPONSE WHERE "
            + "JUROR_NUMBER = '641500127'", String.class)).isEqualTo("07332297123");
    }


    private void assertJurorCommsEmailNotification(final String jurorNumber, final String expectedTemplateName,
                                                   final JurorCommsNotifyTemplateType expectedType) {

        configureNotification(jurorNumber, expectedTemplateName);

        final EmailNotification emailNotification = jurorCommsNotifiyService.createEmailNotification(
            pool, expectedType, templateUuid, payLoad);

        assertThat(emailNotification.getTemplateId())
            .as("Correct Email Notify template selected")
            .isEqualTo(templateUuid);
    }

    private void assertJurorCommsSmsNotification(final String jurorNumber, final String expectedTemplateName,
                                                 final JurorCommsNotifyTemplateType expectedType) {

        configureNotification(jurorNumber, expectedTemplateName);

        final SmsNotification smsNotification = jurorCommsNotifiyService.createSmsNotification(
            pool, expectedType, templateUuid, payLoad);

        assertThat(smsNotification.getTemplateId())
            .as("Correct Sms Notify template selected")
            .isEqualTo(templateUuid);
    }

    private void configureNotification(final String jurorNumber, final String templateName) {

        templateUuid = jdbcTemplate.queryForObject(NOTIFY_TEMPLATE_SQL, String.class, templateName);
        assertThat(templateUuid).as("UUID value present")
            .isNotBlank().containsPattern(UUID_REGEX);

        payLoad = new HashMap<>();
        payLoad.put("jurror number", "value1");
        payLoad.put("court", VALUE2);
        payLoad.put("SERVICESTARTDATE", VALUE2);
        payLoad.put("FIRSTNAME", VALUE2);
        payLoad.put("LASTNAME", VALUE2);

        pool = jurorPoolRepository.findByJurorJurorNumber(jurorNumber);

    }

    private void assertGetDto() throws Exception {
        final Integer validVersion = 555;

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("ncrawford")
            .owner("400")
            .build());


        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        dto = ResponseSendToCourtController.SendToCourtDto.builder()
            .version(validVersion)
            .build();
    }
}
