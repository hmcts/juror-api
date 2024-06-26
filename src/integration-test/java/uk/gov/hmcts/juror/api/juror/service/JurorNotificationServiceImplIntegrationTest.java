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
import uk.gov.hmcts.juror.api.juror.notify.EmailNotification;
import uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings({
    "PMD.ExcessivePublicCount",
    "PMD.TooManyMethods",
    "PMD.GodClass"
})
public class JurorNotificationServiceImplIntegrationTest extends AbstractIntegrationTest {
    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String TEMPLATE_SQL = "SELECT VALUE FROM juror_mod.APP_SETTING WHERE SETTING = ?";

    @Autowired
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;

    @Autowired
    private ResponseInspector responseInspector;

    @Autowired
    private JurorNotificationService notifyService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ### straight through

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StStraightThrough() {
        assertNotification("641500496", "NOTIFY_1ST_STRAIGHT_THROUGH",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StStraightThroughInel() {
        assertNotification("641500537", "NOTIFY_1ST_STRAIGHT_THROUGH_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StStraightThroughAdj() {
        assertNotification("641500540", "NOTIFY_1ST_STRAIGHT_THROUGH_ADJ",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StStraightThroughAdjInel() {
        assertNotification("641500540", "NOTIFY_1ST_STRAIGHT_THROUGH_ADJ_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdStraightThrough() {
        assertNotification("641500127", "NOTIFY_3RD_STRAIGHT_THROUGH",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdStraightThrough() {
        assertNotification("641500128", "NOTIFY_CY_3RD_STRAIGHT_THROUGH",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdStraightThroughInel() {
        assertNotification("641500119", "NOTIFY_3RD_STRAIGHT_THROUGH_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdStraightThroughInel() {
        assertNotification("641500120", "NOTIFY_CY_3RD_STRAIGHT_THROUGH_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdStraightThroughAdj() {
        assertNotification("641500130", "NOTIFY_3RD_STRAIGHT_THROUGH_ADJ",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdStraightThroughAdj() {
        assertNotification("641500131", "NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdStraightThroughAdjInel() {
        assertNotification("641500130", "NOTIFY_3RD_STRAIGHT_THROUGH_ADJ_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdStraightThroughAdjInel() {
        assertNotification("641500131", "NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StStraightThrough() {
        assertNotification("641500497", "NOTIFY_CY_1ST_STRAIGHT_THROUGH",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StStraightThroughInel() {
        assertNotification("641500538", "NOTIFY_CY_1ST_STRAIGHT_THROUGH_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StStraightThroughAdj() {
        assertNotification("641500541", "NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StStraightThroughAdjInel() {
        assertNotification("641500541", "NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ_INEL",
            NotifyTemplateType.STRAIGHT_THROUGH);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StAgeDisqualifiedAge() {
        assertNotification("641500518", "NOTIFY_1ST_DISQUALIFICATION_AGE",
            NotifyTemplateType.DISQUALIFICATION_AGE);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StAgeDisqualifiedAge() {
        assertNotification("641500519", "NOTIFY_CY_1ST_DISQUALIFICATION_AGE",
            NotifyTemplateType.DISQUALIFICATION_AGE);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdAgeDisqualifiedAge() {
        assertNotification("641500542", "NOTIFY_3RD_DISQUALIFICATION_AGE",
            NotifyTemplateType.DISQUALIFICATION_AGE);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdAgeDisqualifiedAge() {
        assertNotification("641500543", "NOTIFY_CY_3RD_DISQUALIFICATION_AGE",
            NotifyTemplateType.DISQUALIFICATION_AGE);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StDeferral() {
        assertNotification("641500521", "NOTIFY_1ST_DEFERRAL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StDeferralInel() {
        assertNotification("641500521", "NOTIFY_1ST_DEFERRAL_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StDeferralAdj() {
        assertNotification("641500529", "NOTIFY_1ST_DEFERRAL_ADJ",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StDeferralAdjInel() {
        assertNotification("641500529", "NOTIFY_1ST_DEFERRAL_ADJ_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdDeferral() {
        assertNotification("641500093", "NOTIFY_3RD_DEFERRAL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdDeferralInel() {
        assertNotification("641500093", "NOTIFY_3RD_DEFERRAL_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdDeferralAdj() {
        assertNotification("641500095", "NOTIFY_3RD_DEFERRAL_ADJ",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdDeferralAdjInel() {
        assertNotification("641500095", "NOTIFY_3RD_DEFERRAL_ADJ_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StDeferral() {
        assertNotification("641500522", "NOTIFY_CY_1ST_DEFERRAL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StDeferralInel() {
        assertNotification("641500522", "NOTIFY_CY_1ST_DEFERRAL_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StDeferralAdj() {
        assertNotification("641500530", "NOTIFY_CY_1ST_DEFERRAL_ADJ",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StDeferralAdjInel() {
        assertNotification("641500530", "NOTIFY_CY_1ST_DEFERRAL_ADJ_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdDeferral() {
        assertNotification("641500094", "NOTIFY_CY_3RD_DEFERRAL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdDeferralInel() {
        assertNotification("641500094", "NOTIFY_CY_3RD_DEFERRAL_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdDeferralAdj() {
        assertNotification("641500096", "NOTIFY_CY_3RD_DEFERRAL_ADJ",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdDeferralAdjInel() {
        assertNotification("641500096", "NOTIFY_CY_3RD_DEFERRAL_ADJ_INEL",
            NotifyTemplateType.DEFERRAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StExcusal() {
        assertNotification("641500531", "NOTIFY_1ST_EXCUSAL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StExcusalInel() {
        assertNotification("641500531", "NOTIFY_1ST_EXCUSAL_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StExcusalAdj() {
        assertNotification("641500534", "NOTIFY_1ST_EXCUSAL_ADJ",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify1StExcusalAdjInel() {
        assertNotification("641500534", "NOTIFY_1ST_EXCUSAL_ADJ_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdExcusal() {
        assertNotification("641500101", "NOTIFY_3RD_EXCUSAL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdExcusalInel() {
        assertNotification("641500101", "NOTIFY_3RD_EXCUSAL_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdExcusalAdj() {
        assertNotification("641500108", "NOTIFY_3RD_EXCUSAL_ADJ",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdExcusalAdjInel() {
        assertNotification("641500108", "NOTIFY_3RD_EXCUSAL_ADJ_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StExcusal() {
        assertNotification("641500532", "NOTIFY_CY_1ST_EXCUSAL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StExcusalInel() {
        assertNotification("641500532", "NOTIFY_CY_1ST_EXCUSAL_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StExcusalAdj() {
        assertNotification("641500535", "NOTIFY_CY_1ST_EXCUSAL_ADJ",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy1StExcusalAdjInel() {
        assertNotification("641500535", "NOTIFY_CY_1ST_EXCUSAL_ADJ_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdExcusal() {
        assertNotification("641500102", "NOTIFY_CY_3RD_EXCUSAL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdExcusalInel() {
        assertNotification("641500102", "NOTIFY_CY_3RD_EXCUSAL_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdExcusalAdj() {
        assertNotification("641500109", "NOTIFY_CY_3RD_EXCUSAL_ADJ",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql("/db/notify-all-jurors-ineligible.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdExcusalAdjInel() {
        assertNotification("641500109", "NOTIFY_CY_3RD_EXCUSAL_ADJ_INEL",
            NotifyTemplateType.EXCUSAL);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Transactional
    @Test
    public void sendNotificationResponseNotify3RdDeceased() {
        assertNotification("641500092", "NOTIFY_3RD_EXCUSAL_DECEASED",
            NotifyTemplateType.EXCUSAL_DECEASED);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/notify-scenarios.sql")
    @Sql({"/db/notify-all-jurors-welsh.sql", "/db/welsh_enabled.sql"})
    @Transactional
    @Test
    public void sendNotificationResponseNotifyCy3RdDeceased() {
        assertNotification("641500192", "NOTIFY_CY_3RD_EXCUSAL_DECEASED",
            NotifyTemplateType.EXCUSAL_DECEASED);
    }

    // *** custom assert ***

    private void assertNotification(final String jurorNumber, final String expectedTemplateKey,
                                    final NotifyTemplateType expectedType) {

        final String templateUuid = jdbcTemplate.queryForObject(TEMPLATE_SQL, String.class, expectedTemplateKey);
        assertThat(templateUuid).as("UUID value present")
            .isNotBlank().containsPattern(UUID_REGEX);

        final DigitalResponse savedResponse = loadDigitalResponse(jurorNumber);
        final NotifyTemplateType type = responseInspector.responseType(savedResponse);

        assertThat(type)
            .as("Correct template type")
            .isEqualTo(expectedType);

        final EmailNotification emailNotification = notifyService.createEmailNotification(savedResponse, type);

        assertThat(emailNotification.getTemplateId())
            .as("Correct Notify template selected")
            .isEqualTo(templateUuid);
    }

    private DigitalResponse loadDigitalResponse(final String jurorNumber) {
        final DigitalResponse response = jurorDigitalResponseRepository.findByJurorNumber(jurorNumber);
        if (response.getCjsEmployments() != null) {
            response.getCjsEmployments().size();
        }
        if (response.getReasonableAdjustments() != null) {
            response.getReasonableAdjustments().size();
        }
        return response;
    }
}