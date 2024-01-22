package uk.gov.hmcts.juror.api.bureau.Notify;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType.COMMS;
import static uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType.EMAIL;
import static uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType.ENGLISH_TOKEN;
import static uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType.LETTER_COMMS;
import static uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType.SENT_TO_COURT;
import static uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType.SMS;
import static uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType.SU_SENT_TO_COURT;
import static uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType.WELSH_TOKEN;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JurorCommsNotifyTemplateTypeIntegrationTest {

    private static final boolean CY_LANG_TRUE = true;
    private static final boolean CY_LANG_FALSE = false;
    private static final int FIRST_WEEKLY_COMM = 1;
    private static final int SECOND_WEEKLY_COMM = 2;
    private static final int THIRD_WEEKLY_COMM = 3;

    private static final boolean SMS_COMMS_TRUE = true;
    private static final boolean SMS_COMMS_FALSE = false;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void enumValues() {
        assertThat(JurorCommsNotifyTemplateType.values())
            .hasSize(4)
            .containsOnly(LETTER_COMMS, SENT_TO_COURT, SU_SENT_TO_COURT, COMMS)
        ;
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getNotifyTemplateKey_COMMS() {
        final JurorCommsNotifyTemplateType type = COMMS;

        final Set<Object> notifyTemplateSettings = jdbcTemplate.queryForList("SELECT TEMPLATE_NAME FROM JUROR_DIGITAL" +
            ".NOTIFY_TEMPLATE_MAPPING").stream().map(s -> s.get("TEMPLATE_NAME")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_FALSE, FIRST_WEEKLY_COMM))
            .startsWith("1ST")
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(ENGLISH_TOKEN)
            .doesNotContain(EMAIL)
            .doesNotContain(SMS).isEqualTo("1ST_COMMS_ENG")
            .isIn(notifyTemplateSettings);
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_FALSE, SECOND_WEEKLY_COMM))
            .startsWith("2ND")
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(ENGLISH_TOKEN)
            .doesNotContain(EMAIL)
            .doesNotContain(SMS).isEqualTo("2ND_COMMS_ENG")
            .isIn(notifyTemplateSettings);
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_FALSE, THIRD_WEEKLY_COMM))
            .startsWith("3RD")
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(ENGLISH_TOKEN)
            .doesNotContain(EMAIL)
            .doesNotContain(SMS).isEqualTo("3RD_COMMS_ENG")
            .isIn(notifyTemplateSettings);
        softly.assertAll();
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getNotifyTemplateKey_SENT_TO_COURT() {
        final JurorCommsNotifyTemplateType type = SENT_TO_COURT;

        final Set<Object> notifyTemplateSettings = jdbcTemplate.queryForList("SELECT TEMPLATE_NAME FROM JUROR_DIGITAL"
            + ".NOTIFY_TEMPLATE_MAPPING").stream().map(s -> s.get("TEMPLATE_NAME")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_FALSE, SMS_COMMS_FALSE))
            .startsWith(type.toString())
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(ENGLISH_TOKEN)
            .containsOnlyOnce(EMAIL)
            .doesNotContain(SMS).isEqualTo("SENT_TO_COURT_EMAIL_ENG")
            .isIn(notifyTemplateSettings);
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_FALSE, SMS_COMMS_TRUE))
            .startsWith(type.toString())
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(ENGLISH_TOKEN)
            .containsOnlyOnce(SMS)
            .doesNotContain(EMAIL).isEqualTo("SENT_TO_COURT_SMS_ENG")
            .isIn(notifyTemplateSettings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getNotifyTemplateKey_SU_SENT_TO_COURT() {
        final JurorCommsNotifyTemplateType type = SU_SENT_TO_COURT;

        final Set<Object> notifyTemplateSettings = jdbcTemplate.queryForList("SELECT TEMPLATE_NAME FROM JUROR_DIGITAL"
            + ".NOTIFY_TEMPLATE_MAPPING").stream().map(s -> s.get("TEMPLATE_NAME")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_FALSE, SMS_COMMS_FALSE))
            .startsWith(type.toString())
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(ENGLISH_TOKEN)
            .containsOnlyOnce(EMAIL)
            .doesNotContain(SMS).isEqualTo("SU_SENT_TO_COURT_EMAIL_ENG")
            .isIn(notifyTemplateSettings);
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_FALSE, SMS_COMMS_TRUE))
            .startsWith(type.toString())
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(ENGLISH_TOKEN)
            .containsOnlyOnce(SMS)
            .doesNotContain(EMAIL).isEqualTo("SU_SENT_TO_COURT_SMS_ENG")
            .isIn(notifyTemplateSettings);
        softly.assertAll();
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getNotifyTemplateKey_COMMS_welshLanguage() {
        final JurorCommsNotifyTemplateType type = COMMS;

        final Set<Object> notifyTemplateSettings = jdbcTemplate.queryForList("SELECT TEMPLATE_NAME FROM JUROR_DIGITAL" +
            ".NOTIFY_TEMPLATE_MAPPING").stream().map(s -> s.get("TEMPLATE_NAME")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_TRUE, FIRST_WEEKLY_COMM))
            .startsWith("1ST")
            .doesNotContain(ENGLISH_TOKEN)
            .containsOnlyOnce(WELSH_TOKEN)
            .doesNotContain(EMAIL)
            .doesNotContain(SMS).isEqualTo("1ST_COMMS_CY")
            .isIn(notifyTemplateSettings);
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_TRUE, SECOND_WEEKLY_COMM))
            .startsWith("2ND")
            .doesNotContain(ENGLISH_TOKEN)
            .containsOnlyOnce(WELSH_TOKEN)
            .doesNotContain(EMAIL)
            .doesNotContain(SMS).isEqualTo("2ND_COMMS_CY")
            .isIn(notifyTemplateSettings);
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_TRUE, THIRD_WEEKLY_COMM))
            .startsWith("3RD")
            .doesNotContain(ENGLISH_TOKEN)
            .containsOnlyOnce(WELSH_TOKEN)
            .doesNotContain(EMAIL)
            .doesNotContain(SMS).isEqualTo("3RD_COMMS_CY")
            .isIn(notifyTemplateSettings);
        softly.assertAll();
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getNotifyTemplateKey_SENT_TO_COURT_welshLanguage() {
        final JurorCommsNotifyTemplateType type = SENT_TO_COURT;

        final Set<Object> notifyTemplateSettings = jdbcTemplate.queryForList("SELECT TEMPLATE_NAME FROM JUROR_DIGITAL" +
            ".NOTIFY_TEMPLATE_MAPPING").stream().map(s -> s.get("TEMPLATE_NAME")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_TRUE, SMS_COMMS_FALSE))
            .startsWith(type.toString())
            .doesNotContain(ENGLISH_TOKEN)
            .containsOnlyOnce(WELSH_TOKEN)
            .containsOnlyOnce(EMAIL)
            .doesNotContain(SMS).isEqualTo("SENT_TO_COURT_EMAIL_CY")
            .isIn(notifyTemplateSettings);
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_TRUE, SMS_COMMS_TRUE))
            .startsWith(type.toString())
            .doesNotContain(ENGLISH_TOKEN)
            .containsOnlyOnce(WELSH_TOKEN)
            .containsOnlyOnce(SMS)
            .doesNotContain(EMAIL).isEqualTo("SENT_TO_COURT_SMS_CY")
            .isIn(notifyTemplateSettings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/notify_template_mapping.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getNotifyTemplateKey_SU_SENT_TO_COURT_welshLanguage() {
        final JurorCommsNotifyTemplateType type = SU_SENT_TO_COURT;

        final Set<Object> notifyTemplateSettings = jdbcTemplate.queryForList("SELECT TEMPLATE_NAME FROM JUROR_DIGITAL" +
            ".NOTIFY_TEMPLATE_MAPPING").stream().map(s -> s.get("TEMPLATE_NAME")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_TRUE, SMS_COMMS_FALSE))
            .startsWith(type.toString())
            .doesNotContain(ENGLISH_TOKEN)
            .containsOnlyOnce(WELSH_TOKEN)
            .containsOnlyOnce(EMAIL)
            .doesNotContain(SMS).isEqualTo("SU_SENT_TO_COURT_EMAIL_CY")
            .isIn(notifyTemplateSettings);
        softly.assertThat(type.getNotifyTemplateKey(CY_LANG_TRUE, SMS_COMMS_TRUE))
            .startsWith(type.toString())
            .doesNotContain(ENGLISH_TOKEN)
            .containsOnlyOnce(WELSH_TOKEN)
            .containsOnlyOnce(SMS)
            .doesNotContain(EMAIL).isEqualTo("SU_SENT_TO_COURT_SMS_CY")
            .isIn(notifyTemplateSettings);
        softly.assertAll();
    }

}