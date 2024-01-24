package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;

import java.util.List;

/**
 * Service for retrieving application settings.
 */
public interface AppSettingService {
    List<AppSetting> findAllSettings();

    /**
     * Gets the search result limit for bureau officers.
     *
     * @return the limit setting, nullable
     */
    Integer getBureauOfficerSearchResultLimit();

    /**
     * Gets the search result limit for team leaders.
     *
     * @return the limit setting, nullable
     */
    Integer getTeamLeaderSearchResultLimit();

    /**
     * Gets the default capacity for bureau officers.
     *
     * @return the capacity setting, nullable
     */
    Integer getDefaultCapacity();

    /**
     * Is welsh language support enabled?.
     *
     * @return Welsh support enabled
     */
    boolean isWelshEnabled();

    /**
     * Gets the total number of summonses sent value.
     *
     * @return the total summonses sent number, nullable
     */
    Integer getTotalNumberSummonsesSent();

    /**
     * Gets the total number of online replies  value.
     *
     * @return the total online replies number, nullable
     */
    Integer getTotalNumberOnlineReplies();

    /**
     * Gets the configuration number value set for sending emails or texts.
     *
     * @return the configuration number for sending emails or sms messages, nullable
     */
    Integer getSendEmailOrSms();

    /**
     * Gets the configuration number value set for TRIGGERED_COMMS_EXCUSAL_DAYS.
     *
     * @return the configuration number for TRIGGERED_COMMS_EXCUSAL_DAYS
     */
    Integer getTriggeredCommsExcusalDays();

    /**
     * Gets the configuration number value set for TRIGGERED_COMMS_SERVICE_COMPLETED_DAYS.
     *
     * @return the configuration number for TRIGGERED_COMMS_SERVICE_COMPLETED_DAYS
     */
    Integer getTriggeredCommsServiceCompletedDays();

    /**
     * Gets the configuration number value set for SMART_SURVEY_SUMMONS_RESPONSE_DAYS.
     *
     * @return the configuration number for SMART_SURVEY_SUMMONS_RESPONSE_DAYS
     */
    Integer getSmartSurveySummonsResponseDays();

    /**
     * Gets the Welsh Subject text needed to identify if a Welsh template has been selected in the Legacy application.
     *
     * @return the Welsh Subject Text
     */
    String getWelshTranslation();

    /**
     * Gets the configuration  value set for SMART_SURVEY_SUMMONS_RESPONSE_ID.
     *
     * @return the configuration number for SMART_SURVEY_SUMMONS_RESPONSE_ID
     */
    String getSmartSurveySummonsResponseSurveyId();

    /**
     * Gets the configuration  value set for SMART_SURVEY_SUMMONS_RESPONSE_ID.
     *
     * @return the configuration number for SMART_SURVEY_SUMMONS_RESPONSE_ID
     */
    String getSmartSurveySummonsResponseExportName();


}
