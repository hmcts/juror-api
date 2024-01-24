package uk.gov.hmcts.juror.api.juror.service;

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
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JurorNotificationServiceImplIntegrationTest extends AbstractIntegrationTest {
    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String TEMPLATE_SQL = "SELECT VALUE FROM JUROR_DIGITAL.APP_SETTINGS WHERE SETTING = ?";

    @Autowired
    private JurorResponseRepository jurorResponseRepository;

    @Autowired
    private ResponseInspector responseInspector;

    @Autowired
    private JurorNotificationService notifyService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ### straight through

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_STRAIGHT_THROUGH() {
        assertNotification("641500496", "NOTIFY_1ST_STRAIGHT_THROUGH",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_STRAIGHT_THROUGH_INEL() {
        assertNotification("641500537", "NOTIFY_1ST_STRAIGHT_THROUGH_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_STRAIGHT_THROUGH_ADJ() {
        assertNotification("641500540", "NOTIFY_1ST_STRAIGHT_THROUGH_ADJ",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_STRAIGHT_THROUGH_ADJ_INEL() {
        assertNotification("641500540", "NOTIFY_1ST_STRAIGHT_THROUGH_ADJ_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_STRAIGHT_THROUGH() {
        assertNotification("641500127", "NOTIFY_3RD_STRAIGHT_THROUGH",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_STRAIGHT_THROUGH() {
        assertNotification("641500128", "NOTIFY_CY_3RD_STRAIGHT_THROUGH",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_STRAIGHT_THROUGH_INEL() {
        assertNotification("641500119", "NOTIFY_3RD_STRAIGHT_THROUGH_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_STRAIGHT_THROUGH_INEL() {
        assertNotification("641500120", "NOTIFY_CY_3RD_STRAIGHT_THROUGH_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_STRAIGHT_THROUGH_ADJ() {
        assertNotification("641500130", "NOTIFY_3RD_STRAIGHT_THROUGH_ADJ",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ() {
        assertNotification("641500131", "NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_STRAIGHT_THROUGH_ADJ_INEL() {
        assertNotification("641500130", "NOTIFY_3RD_STRAIGHT_THROUGH_ADJ_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ_INEL() {
        assertNotification("641500131", "NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_STRAIGHT_THROUGH() {
        assertNotification("641500497", "NOTIFY_CY_1ST_STRAIGHT_THROUGH",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_STRAIGHT_THROUGH_INEL() {
        assertNotification("641500538", "NOTIFY_CY_1ST_STRAIGHT_THROUGH_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ() {
        assertNotification("641500541", "NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ_INEL() {
        assertNotification("641500541", "NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_AGE_DISQUALIFIED_AGE() {
        assertNotification("641500518", "NOTIFY_1ST_DISQUALIFICATION_AGE",
            NotifyTemplateType.DISQUALIFICATION_AGE);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_AGE_DISQUALIFIED_AGE() {
        assertNotification("641500519", "NOTIFY_CY_1ST_DISQUALIFICATION_AGE",
            NotifyTemplateType.DISQUALIFICATION_AGE);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_AGE_DISQUALIFIED_AGE() {
        assertNotification("641500542", "NOTIFY_3RD_DISQUALIFICATION_AGE",
            NotifyTemplateType.DISQUALIFICATION_AGE);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_AGE_DISQUALIFIED_AGE() {
        assertNotification("641500543", "NOTIFY_CY_3RD_DISQUALIFICATION_AGE",
            NotifyTemplateType.DISQUALIFICATION_AGE);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_DEFERRAL() {
        assertNotification("641500521", "NOTIFY_1ST_DEFERRAL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_DEFERRAL_INEL() {
        assertNotification("641500521", "NOTIFY_1ST_DEFERRAL_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_DEFERRAL_ADJ() {
        assertNotification("641500529", "NOTIFY_1ST_DEFERRAL_ADJ",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_DEFERRAL_ADJ_INEL() {
        assertNotification("641500529", "NOTIFY_1ST_DEFERRAL_ADJ_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_DEFERRAL() {
        assertNotification("641500093", "NOTIFY_3RD_DEFERRAL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_DEFERRAL_INEL() {
        assertNotification("641500093", "NOTIFY_3RD_DEFERRAL_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_DEFERRAL_ADJ() {
        assertNotification("641500095", "NOTIFY_3RD_DEFERRAL_ADJ",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_DEFERRAL_ADJ_INEL() {
        assertNotification("641500095", "NOTIFY_3RD_DEFERRAL_ADJ_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_DEFERRAL() {
        assertNotification("641500522", "NOTIFY_CY_1ST_DEFERRAL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_DEFERRAL_INEL() {
        assertNotification("641500522", "NOTIFY_CY_1ST_DEFERRAL_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_DEFERRAL_ADJ() {
        assertNotification("641500530", "NOTIFY_CY_1ST_DEFERRAL_ADJ",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_DEFERRAL_ADJ_INEL() {
        assertNotification("641500530", "NOTIFY_CY_1ST_DEFERRAL_ADJ_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_DEFERRAL() {
        assertNotification("641500094", "NOTIFY_CY_3RD_DEFERRAL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_DEFERRAL_INEL() {
        assertNotification("641500094", "NOTIFY_CY_3RD_DEFERRAL_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_DEFERRAL_ADJ() {
        assertNotification("641500096", "NOTIFY_CY_3RD_DEFERRAL_ADJ",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_DEFERRAL_ADJ_INEL() {
        assertNotification("641500096", "NOTIFY_CY_3RD_DEFERRAL_ADJ_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_EXCUSAL() {
        assertNotification("641500531", "NOTIFY_1ST_EXCUSAL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_EXCUSAL_INEL() {
        assertNotification("641500531", "NOTIFY_1ST_EXCUSAL_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_EXCUSAL_ADJ() {
        assertNotification("641500534", "NOTIFY_1ST_EXCUSAL_ADJ",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_1ST_EXCUSAL_ADJ_INEL() {
        assertNotification("641500534", "NOTIFY_1ST_EXCUSAL_ADJ_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_EXCUSAL() {
        assertNotification("641500101", "NOTIFY_3RD_EXCUSAL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_EXCUSAL_INEL() {
        assertNotification("641500101", "NOTIFY_3RD_EXCUSAL_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_EXCUSAL_ADJ() {
        assertNotification("641500108", "NOTIFY_3RD_EXCUSAL_ADJ",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_EXCUSAL_ADJ_INEL() {
        assertNotification("641500108", "NOTIFY_3RD_EXCUSAL_ADJ_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_EXCUSAL() {
        assertNotification("641500532", "NOTIFY_CY_1ST_EXCUSAL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_EXCUSAL_INEL() {
        assertNotification("641500532", "NOTIFY_CY_1ST_EXCUSAL_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_EXCUSAL_ADJ() {
        assertNotification("641500535", "NOTIFY_CY_1ST_EXCUSAL_ADJ",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_1ST_EXCUSAL_ADJ_INEL() {
        assertNotification("641500535", "NOTIFY_CY_1ST_EXCUSAL_ADJ_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_EXCUSAL() {
        assertNotification("641500102", "NOTIFY_CY_3RD_EXCUSAL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_EXCUSAL_INEL() {
        assertNotification("641500102", "NOTIFY_CY_3RD_EXCUSAL_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_EXCUSAL_ADJ() {
        assertNotification("641500109", "NOTIFY_CY_3RD_EXCUSAL_ADJ",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_EXCUSAL_ADJ_INEL() {
        assertNotification("641500109", "NOTIFY_CY_3RD_EXCUSAL_ADJ_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_3RD_DECEASED() {
        assertNotification("641500092", "NOTIFY_3RD_EXCUSAL_DECEASED",
            NotifyTemplateType.EXCUSAL_DECEASED);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponse_NOTIFY_CY_3RD_DECEASED() {
        assertNotification("641500192", "NOTIFY_CY_3RD_EXCUSAL_DECEASED",
            NotifyTemplateType.EXCUSAL_DECEASED);
    }

    // *** custom assert ***

    private void assertNotification(final String jurorNumber, final String expectedTemplateKey,
                                    final NotifyTemplateType expectedType) {

        final String templateUUID = jdbcTemplate.queryForObject(TEMPLATE_SQL, String.class, expectedTemplateKey);
        assertThat(templateUUID).as("UUID value present")
            .isNotBlank().containsPattern(UUID_REGEX);

        final JurorResponse savedResponse = loadJurorResponse(jurorNumber);
        final NotifyTemplateType type = responseInspector.responseType(savedResponse);

        assertThat(type)
            .as("Correct template type")
            .isEqualTo(expectedType);

        final EmailNotification emailNotification = notifyService.createEmailNotification(savedResponse, type);

        assertThat(emailNotification.getTemplateId())
            .as("Correct Notify template selected")
            .isEqualTo(templateUUID);
    }

    private JurorResponse loadJurorResponse(final String jurorNumber) {
        final JurorResponse response = jurorResponseRepository.findByJurorNumber(jurorNumber);
        if (response.getCjsEmployments() != null) {
            response.getCjsEmployments().size();
        }
        if (response.getSpecialNeeds() != null) {
            response.getSpecialNeeds().size();
        }
        return response;
    }
}