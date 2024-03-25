package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.QRegionNotifyTemplateMod;

/**
 * QueryDSL queries for {@Link messages}.
 */


public class RegionNotifyTemplateQueriesMod {

    private static final String MESSAGE_FORMAT_SMS = "SMS";
    private static final String MESSAGE_FORMAT_EMAIL = "EMAIL";
    private static final String WELSH_LANGUAGE = "Y";
    private static final String TRIGGERED_TEMPLATE_EXCUSAL_ID = "EXCUSAL";
    private static final String TRIGGERED_TEMPLATE_COMPLETE_ID = "SVC_COMPLETE";

    private RegionNotifyTemplateQueriesMod() {

    }

    private static final QRegionNotifyTemplateMod regionNotifyTemplateDetail =
        QRegionNotifyTemplateMod.regionNotifyTemplateMod;

    /**
     * Matches RegionalNotifyTemplates by REGION_ID AND LEGACY_TEMPLATE_ID where MESSAGE_FORMAT is equal to 'SMS' .
     *
     
     
     */

    public static BooleanExpression regionNotifyTemplateByIdAndSms(final String regionIdSms,
                                                                   Integer legacyTemplateIdSms) {
        return regionNotifyTemplateDetail.regionId.eq(regionIdSms)
            .and(regionNotifyTemplateDetail.legacyTemplateId.eq(legacyTemplateIdSms))
            .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_SMS));
    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID AND LEGACY_TEMPLATE_ID where MESSAGE_FORMAT is equal to 'EMAIL' .
     *
     
     
     */

    public static BooleanExpression regionNotifyTemplateByIdAndEmail(final String regionIdEmail,
                                                                     Integer legacyTemplateIdEmail) {
        return regionNotifyTemplateDetail.regionId.eq(regionIdEmail)
            .and(regionNotifyTemplateDetail.legacyTemplateId.eq(legacyTemplateIdEmail))
            .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_EMAIL));
    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID AND LEGACY_TEMPLATE_ID where MESSAGE_FORMAT is equal to 'SMS' and
     * WELSH_LANGUAGE is equal to 'Y' .
     *
     
     
     */
    public static BooleanExpression regionNotifyTemplateByIdAndSmsWelsh(final String regionIdSmsWelsh,
                                                                        Integer legacyTemplateIdSmsWelsh) {
        return regionNotifyTemplateDetail.regionId.eq(regionIdSmsWelsh)
            .and(regionNotifyTemplateDetail.legacyTemplateId.eq(legacyTemplateIdSmsWelsh)).and(
                regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_SMS)
                    .and(regionNotifyTemplateDetail.welsh.eq(WELSH_LANGUAGE)));
    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID AND LEGACY_TEMPLATE_ID where MESSAGE_FORMAT is equal to 'EMAIL'
     * and WELSH_LANGUAGE is equal to 'Y' .
     *
     
     
     */
    public static BooleanExpression regionNotifyTemplateByIdAndEmailWelsh(final String regionIdEmailWelsh,
                                                                          Integer legacyTemplateIdEmailWelsh) {
        return regionNotifyTemplateDetail.regionId.eq(regionIdEmailWelsh)
            .and(regionNotifyTemplateDetail.legacyTemplateId.eq(legacyTemplateIdEmailWelsh)).and(
                regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_EMAIL)
                    .and(regionNotifyTemplateDetail.welsh.eq(WELSH_LANGUAGE)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'SMS' .
     *
     */
    public static BooleanExpression regionNotifyTriggeredExcusalTemplateSmsId(final String regionIdExcusalSms) {
        return regionNotifyTemplateDetail.regionId.eq(regionIdExcusalSms).and(
            regionNotifyTemplateDetail.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_EXCUSAL_ID)
                .and(regionNotifyTemplateDetail.welsh.isNull())
                .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_SMS)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'EMAIL' .
     *
     */
    public static BooleanExpression regionNotifyTriggeredExcusalTemplateEmailId(final String regionIdExcusalEmail) {
        return regionNotifyTemplateDetail.regionId.eq(regionIdExcusalEmail).and(
            regionNotifyTemplateDetail.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_EXCUSAL_ID)
                .and(regionNotifyTemplateDetail.welsh.isNull())
                .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_EMAIL)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = SVC_COMPLETE and MESSAGE_FORMAT =
     * 'SMS' .
     *
     */
    public static BooleanExpression regionNotifyTriggeredCompletedTemplateSmsId(final String regionIdCompletedSms) {
        return regionNotifyTemplateDetail.regionId.eq(regionIdCompletedSms).and(
            regionNotifyTemplateDetail.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_COMPLETE_ID)
                .and(regionNotifyTemplateDetail.welsh.isNull())
                .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_SMS)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'EMAIL' .
     *
     */
    public static BooleanExpression regionNotifyTriggeredCompletedTemplateEmailId(final String regionIdCompletedEmail) {
        return regionNotifyTemplateDetail.regionId.eq(regionIdCompletedEmail).and(
            regionNotifyTemplateDetail.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_COMPLETE_ID)
                .and(regionNotifyTemplateDetail.welsh.isNull())
                .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_EMAIL)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = SVC_COMPLETE and MESSAGE_FORMAT =
     * 'EMAIL' .
     * and Welsh = Y.
     *
     */
    public static BooleanExpression welshRegionNotifyTriggeredCompletedTemplateEmailId(
        final String welshRegionIdCompletedEmail, String isCompletedEmailWelsh) {
        return regionNotifyTemplateDetail.regionId.eq(welshRegionIdCompletedEmail)
            .and(regionNotifyTemplateDetail.welsh.eq(isCompletedEmailWelsh)).and(
                regionNotifyTemplateDetail.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_COMPLETE_ID)
                    .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_EMAIL)));


    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = SVC_COMPLETE and MESSAGE_FORMAT =
     * 'SMS' .
     * and welsh = Y.
     *
     */
    public static BooleanExpression welshRegionNotifyTriggeredCompletedTemplateSmsId(
        final String welshRegionIdCompletedSms, String isCompletedSmsWelsh) {
        return regionNotifyTemplateDetail.regionId.eq(welshRegionIdCompletedSms)
            .and(regionNotifyTemplateDetail.welsh.eq(isCompletedSmsWelsh)).and(
                regionNotifyTemplateDetail.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_COMPLETE_ID)
                    .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_SMS)));

    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'EMAIL'
     * and welsh = Y .
     *
     */
    public static BooleanExpression welshRegionNotifyTriggeredExcusalTemplateEmailId(
        final String welshRegionIdExcusalEmail, String isExcusalEmailWelsh) {
        return regionNotifyTemplateDetail.regionId.eq(welshRegionIdExcusalEmail)
            .and(regionNotifyTemplateDetail.welsh.eq(isExcusalEmailWelsh)).and(
                regionNotifyTemplateDetail.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_EXCUSAL_ID)
                    .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_EMAIL)));
    }

    /**
     * Matches RegionalNotifyTemplates by REGION_ID where TRIGGERED_TEMPLATE_ID = EXCUSAL and MESSAGE_FORMAT = 'SMS'
     * and welsh = Y .
     *
     */
    public static BooleanExpression welshRegionNotifyTriggeredExcusalTemplateSmsId(final String welshRegionIdExcusalSms,
                                                                                   String isExcusalSmsWelsh) {
        return regionNotifyTemplateDetail.regionId.eq(welshRegionIdExcusalSms)
            .and(regionNotifyTemplateDetail.welsh.eq(isExcusalSmsWelsh)).and(
                regionNotifyTemplateDetail.triggeredTemplateId.eq(TRIGGERED_TEMPLATE_EXCUSAL_ID)
                    .and(regionNotifyTemplateDetail.messageFormat.eq(MESSAGE_FORMAT_SMS)));


    }
}
