package uk.gov.hmcts.juror.api.juror.notify;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.ADJUSTMENT_SUFFIX;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.DEFERRAL;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.DISQUALIFICATION_AGE;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.EXCUSAL;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.EXCUSAL_DECEASED;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.FIRST_PERSON_TOKEN;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.INELIGIBILITY_SUFFIX;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.KEY_PREFIX;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.STRAIGHT_THROUGH;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.THIRD_PARTY_TOKEN;
import static uk.gov.hmcts.juror.api.juror.notify.NotifyTemplateType.WELSH_TOKEN;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotifyTemplateTypeIntegrationTest extends ContainerTest {
    private static final boolean WITH_ADJUSTMENTS = true;
    private static final boolean NO_ADJUSTMENTS = false;

    private static final boolean THIRD_PARTY = true;
    private static final boolean FIRST_PERSON = false;
    private static final boolean EN_LANG = false;
    private static final boolean CY_LANG = true;
    public static final boolean ELLIGIBLE = false;//NOTE: the Ineligibility being a thing is the positive!
    public static final boolean INELLIGIBLE = true;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void enumValues() {
        assertThat(NotifyTemplateType.values())
            .hasSize(5)
            .containsOnly(DEFERRAL, DISQUALIFICATION_AGE, EXCUSAL, EXCUSAL_DECEASED, STRAIGHT_THROUGH)
        ;
    }

    //"DISQUALIFICATION" removed in JDB-2895 - replaced by the STRAIGHT_THROUGH_WITH_INELIGIBILTY

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyDisqualificationAge() {
        final NotifyTemplateType type = DISQUALIFICATION_AGE;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_DISQUALIFICATION_AGE")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_DISQUALIFICATION_AGE")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)//not adjustable type!
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_DISQUALIFICATION_AGE")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)//not adjustable type!
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_DISQUALIFICATION_AGE")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyExcusalDeceased() {
        final NotifyTemplateType type = EXCUSAL_DECEASED;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_EXCUSAL_DECEASED")
            .isNotIn(settings);//Not a supported business function
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_EXCUSAL_DECEASED")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)//not adjustable type!
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_EXCUSAL_DECEASED")
            .isNotIn(settings);//Not a supported business function
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)//not adjustable type!
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_EXCUSAL_DECEASED")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyExcusal() {
        final NotifyTemplateType type = EXCUSAL;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX)
            .isEqualTo("NOTIFY_1ST_EXCUSAL")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_EXCUSAL")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_EXCUSAL_ADJ")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_EXCUSAL_ADJ")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyStraightThrough() {
        final NotifyTemplateType type = STRAIGHT_THROUGH;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_STRAIGHT_THROUGH")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_STRAIGHT_THROUGH")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_STRAIGHT_THROUGH_ADJ")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_STRAIGHT_THROUGH_ADJ")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyDeferral() {
        final NotifyTemplateType type = DEFERRAL;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_DEFERRAL")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_DEFERRAL")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_1ST_DEFERRAL_ADJ")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, EN_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .doesNotContain(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_3RD_DEFERRAL_ADJ")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyDisqualificationAgeWelshLanguage() {
        final NotifyTemplateType type = DISQUALIFICATION_AGE;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_DISQUALIFICATION_AGE")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_DISQUALIFICATION_AGE")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)//not adjustable type!
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_DISQUALIFICATION_AGE")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)//not adjustable type!
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_DISQUALIFICATION_AGE")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyExcusalDeceasedWelshLanguage() {
        final NotifyTemplateType type = EXCUSAL_DECEASED;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_EXCUSAL_DECEASED")
            .isNotIn(settings);//Not a supported business function
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_EXCUSAL_DECEASED")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)//not adjustable type!
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_EXCUSAL_DECEASED")
            .isNotIn(settings);//Not a supported business function
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)//not adjustable type!
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_EXCUSAL_DECEASED")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyExcusalWelshLanguage() {
        final NotifyTemplateType type = EXCUSAL;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_EXCUSAL")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_EXCUSAL")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_EXCUSAL_ADJ")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_EXCUSAL_ADJ")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyStraightThroughWelshLanguage() {
        final NotifyTemplateType type = STRAIGHT_THROUGH;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_STRAIGHT_THROUGH")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_STRAIGHT_THROUGH")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ")
            .isIn(settings);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    @Sql("/db/welsh_enabled.sql")
    public void getAppSettingKeyDeferralWelshLanguage() {
        final NotifyTemplateType type = DEFERRAL;

        final Set<Object> settings =
            jdbcTemplate.queryForList("SELECT SETTING FROM juror_mod.app_setting").stream().map(s -> s.get(
                "SETTING")).collect(Collectors.toSet());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_DEFERRAL")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(NO_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .doesNotContain(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_DEFERRAL")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, FIRST_PERSON, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(FIRST_PERSON_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(THIRD_PARTY_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_1ST_DEFERRAL_ADJ")
            .isIn(settings);
        softly.assertThat(type.getAppSettingKey(WITH_ADJUSTMENTS, THIRD_PARTY, CY_LANG, ELLIGIBLE))
            .startsWith(KEY_PREFIX)
            .contains(WELSH_TOKEN)
            .containsOnlyOnce(THIRD_PARTY_TOKEN)
            .endsWith(ADJUSTMENT_SUFFIX)
            .doesNotContain(FIRST_PERSON_TOKEN)
            .doesNotContain(INELIGIBILITY_SUFFIX).isEqualTo("NOTIFY_CY_3RD_DEFERRAL_ADJ")
            .isIn(settings);
        softly.assertAll();
    }
}