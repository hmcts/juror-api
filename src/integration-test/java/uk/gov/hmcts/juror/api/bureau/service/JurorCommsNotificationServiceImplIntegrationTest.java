package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JurorCommsNotificationServiceImplIntegrationTest {
    private static final String UUID_REGEX = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f" +
        "]{12}$";
    private static final String NOTIFY_TEMPLATE_SQL = "SELECT TEMPLATE_ID FROM JUROR_DIGITAL.NOTIFY_TEMPLATE_MAPPING" +
        " WHERE TEMPLATE_NAME = ?";

    @Autowired
    private JurorResponseRepository jurorResponseRepository;

    @Autowired
    private PoolRepository poolRepository;

    @Autowired
    private JurorCommsNotificationService JurorCommsNotifiyService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/juror-comms-notify.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_4WKS_COMMS() {
        assertJurorCommsNotification("641500496", "1ST_COMMS_ENG",
            JurorCommsNotifyTemplateType.COMMS);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/juror-comms-notify.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_3WKS_COMMS() {
        assertJurorCommsNotification("641500540", "2ND_COMMS_ENG",
            JurorCommsNotifyTemplateType.COMMS);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/juror-comms-notify.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_2WKS_COMMS() {
        assertJurorCommsNotification("641500127", "3RD_COMMS_ENG",
            JurorCommsNotifyTemplateType.COMMS);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/juror-comms-notify.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_SEND_TO_COURT_SMS_CY_comms() {
        assertJurorCommsNotification("641500127", "SENT_TO_COURT_SMS_CY",
            JurorCommsNotifyTemplateType.SENT_TO_COURT);
    }


    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/juror-comms-notify.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_SU_SEND_TO_COURT_EMAIL_CY_comms() {
        assertJurorCommsNotification("641500127", "SU_SENT_TO_COURT_EMAIL_CY",
            JurorCommsNotifyTemplateType.SU_SENT_TO_COURT);
    }

    private void assertJurorCommsNotification(final String jurorNumber, final String expectedTemplateName,
                                              final JurorCommsNotifyTemplateType expectedType) {

        final String templateUUID = jdbcTemplate.queryForObject(NOTIFY_TEMPLATE_SQL, String.class,
            expectedTemplateName);
        assertThat(templateUUID).as("UUID value present")
            .isNotBlank().containsPattern(UUID_REGEX);

        Map<String, String> payLoad = new HashMap<>();
        payLoad.put("jurror number", "value1");
        payLoad.put("court", "value2");
        payLoad.put("SERVICESTARTDATE", "value2");
        payLoad.put("FIRSTNAME", "value2");
        payLoad.put("LASTNAME", "value2");

        final Pool pool = poolRepository.findByJurorNumber(jurorNumber);

        final EmailNotification emailNotification = JurorCommsNotifiyService.createEmailNotification(
            pool, expectedType, templateUUID, payLoad);

        assertThat(emailNotification.getTemplateId())
            .as("Correct Notify template selected")
            .isEqualTo(templateUUID);
    }

    private JurorResponse loadJurorResponse(final String jurorNumber) {
        final JurorResponse response = jurorResponseRepository.findByJurorNumber(jurorNumber);
        return response;
    }

}