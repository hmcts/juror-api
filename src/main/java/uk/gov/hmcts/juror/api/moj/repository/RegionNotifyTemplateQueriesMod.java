package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.QRegionNotifyTemplateMod;

/**
 * QueryDSL queries for {@Link messages}.
 */

@SuppressWarnings({"PMD.TooManyMethods"})
public final class RegionNotifyTemplateQueriesMod {

    private static final String MESSAGE_FORMAT_SMS = "SMS";
    private static final String MESSAGE_FORMAT_EMAIL = "EMAIL";
    private static final String WELSH_LANGUAGE = "Y";
    private static final String TRIGGERED_TEMPLATE_EXCUSAL_ID = "EXCUSAL";
    private static final String TRIGGERED_TEMPLATE_COMPLETE_ID = "SVC_COMPLETE";
    private static final QRegionNotifyTemplateMod REGION_NOTIFY_TEMPLATE_DETAIL =
        QRegionNotifyTemplateMod.regionNotifyTemplateMod;

    private RegionNotifyTemplateQueriesMod() {

    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID AND LEGACY_TEMPLATE_ID where MESSAGE_FORMAT is equal to 'SMS' .
     *


     */

    public static BooleanExpression regionNotifyTemplateByIdAndSms(final String regionIdSms,
                                                                   Integer legacyTemplateIdSms) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(regionIdSms)
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.legacyTemplateId.eq(legacyTemplateIdSms))
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_SMS));
    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID AND LEGACY_TEMPLATE_ID where MESSAGE_FORMAT is equal to 'EMAIL' .
     *


     */

    public static BooleanExpression regionNotifyTemplateByIdAndEmail(final String regionIdEmail,
                                                                     Integer legacyTemplateIdEmail) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(regionIdEmail)
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.legacyTemplateId.eq(legacyTemplateIdEmail))
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_EMAIL));
    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID AND LEGACY_TEMPLATE_ID where MESSAGE_FORMAT is equal to 'SMS' and
     * WELSH_LANGUAGE is equal to 'Y' .
     *


     */
    public static BooleanExpression regionNotifyTemplateByIdAndSmsWelsh(final String regionIdSmsWelsh,
                                                                        Integer legacyTemplateIdSmsWelsh) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(regionIdSmsWelsh)
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.legacyTemplateId.eq(legacyTemplateIdSmsWelsh)).and(
                REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_SMS)
                    .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.eq(WELSH_LANGUAGE)));
    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID AND LEGACY_TEMPLATE_ID where MESSAGE_FORMAT is equal to 'EMAIL'
     * and WELSH_LANGUAGE is equal to 'Y' .
     *


     */
    public static BooleanExpression regionNotifyTemplateByIdAndEmailWelsh(final String regionIdEmailWelsh,
                                                                          Integer legacyTemplateIdEmailWelsh) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(regionIdEmailWelsh)
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.legacyTemplateId.eq(legacyTemplateIdEmailWelsh)).and(
                REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_EMAIL)
                    .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.eq(WELSH_LANGUAGE)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'SMS' .
     *
     */
    public static BooleanExpression regionNotifyTriggeredExcusalTemplateSmsId(final String regionIdExcusalSms) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(regionIdExcusalSms).and(
            REGION_NOTIFY_TEMPLATE_DETAIL.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_EXCUSAL_ID)
                .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.isNull())
                .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_SMS)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'EMAIL' .
     *
     */
    public static BooleanExpression regionNotifyTriggeredExcusalTemplateEmailId(final String regionIdExcusalEmail) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(regionIdExcusalEmail).and(
            REGION_NOTIFY_TEMPLATE_DETAIL.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_EXCUSAL_ID)
                .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.isNull())
                .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_EMAIL)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = SVC_COMPLETE and MESSAGE_FORMAT =
     * 'SMS' .
     *
     */
    public static BooleanExpression regionNotifyTriggeredCompletedTemplateSmsId(final String regionIdCompletedSms) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(regionIdCompletedSms).and(
            REGION_NOTIFY_TEMPLATE_DETAIL.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_COMPLETE_ID)
                .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.isNull())
                .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_SMS)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'EMAIL' .
     *
     */
    public static BooleanExpression regionNotifyTriggeredCompletedTemplateEmailId(final String regionIdCompletedEmail) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(regionIdCompletedEmail).and(
            REGION_NOTIFY_TEMPLATE_DETAIL.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_COMPLETE_ID)
                .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.isNull())
                .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_EMAIL)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = SVC_COMPLETE and MESSAGE_FORMAT =
     * 'EMAIL' .
     * and Welsh = Y.
     *
     */
    public static BooleanExpression welshRegionNotifyTriggeredCompletedTemplateEmailId(
        final String welshRegionIdCompletedEmail, String isCompletedEmailWelsh) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(welshRegionIdCompletedEmail)
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.eq(isCompletedEmailWelsh)).and(
                REGION_NOTIFY_TEMPLATE_DETAIL.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_COMPLETE_ID)
                    .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_EMAIL)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = SVC_COMPLETE and MESSAGE_FORMAT =
     * 'SMS' .
     * and welsh = Y.
     *
     */
    public static BooleanExpression welshRegionNotifyTriggeredCompletedTemplateSmsId(
        final String welshRegionIdCompletedSms, String isCompletedSmsWelsh) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(welshRegionIdCompletedSms)
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.eq(isCompletedSmsWelsh)).and(
                REGION_NOTIFY_TEMPLATE_DETAIL.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_COMPLETE_ID)
                    .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_SMS)));

    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'EMAIL'
     * and welsh = Y .
     *
     */
    public static BooleanExpression welshRegionNotifyTriggeredExcusalTemplateEmailId(
        final String welshRegionIdExcusalEmail, String isExcusalEmailWelsh) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(welshRegionIdExcusalEmail)
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.eq(isExcusalEmailWelsh)).and(
                REGION_NOTIFY_TEMPLATE_DETAIL.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_EXCUSAL_ID)
                    .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_EMAIL)));
    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'SMS'
     * and welsh = Y .
     *
     */
    public static BooleanExpression welshRegionNotifyTriggeredExcusalTemplateSmsId(final String welshRegionIdExcusalSms,
                                                                                   String isExcusalSmsWelsh) {
        return REGION_NOTIFY_TEMPLATE_DETAIL.regionId.eq(welshRegionIdExcusalSms)
            .and(REGION_NOTIFY_TEMPLATE_DETAIL.welsh.eq(isExcusalSmsWelsh)).and(
                REGION_NOTIFY_TEMPLATE_DETAIL.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_EXCUSAL_ID)
                    .and(REGION_NOTIFY_TEMPLATE_DETAIL.messageFormat.eq(MESSAGE_FORMAT_SMS)));


    }
}
