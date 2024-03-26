package uk.gov.hmcts.juror.api.moj.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.moj.domain.AppSetting;
import uk.gov.hmcts.juror.api.moj.repository.AppSettingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.math.NumberUtils.isDigits;

@Service
@Slf4j
public class AppSettingServiceImpl implements AppSettingService {
    private static final String SEARCH_RESULT_LIMIT_BUREAU_OFFICER = "SEARCH_RESULT_LIMIT_BUREAU_OFFICER";
    private static final String SEARCH_RESULT_LIMIT_TEAM_LEADER = "SEARCH_RESULT_LIMIT_TEAM_LEADER";
    private static final String AUTO_ASSIGNMENT_DEFAULT_CAPACITY = "AUTO_ASSIGNMENT_DEFAULT_CAPACITY";
    private static final String WELSH_LANGUAGE_ENABLED = "WELSH_LANGUAGE_ENABLED";
    private static final String TOTAL_NUMBER_SUMMONSES_SENT = "TOTAL_NUMBER_SUMMONSES_SENT";
    private static final String TOTAL_NUMBER_ONLINE_REPLIES = "TOTAL_NUMBER_ONLINE_REPLIES";
    private static final String SEND_EMAIL_OR_SMS = "SEND_EMAIL_OR_SMS";
    private static final String TRIGGERED_COMMS_EXCUSAL_DAYS = "TRIGGERED_COMMS_EXCUSAL_DAYS";
    private static final String TRIGGERED_COMMS_SERVICE_COMPLETED_DAYS = "TRIGGERED_COMMS_SERVICE_COMPLETED_DAYS";
    private static final String WELSH_TRANSLATION = "WELSH_TRANSLATION";
    private static final String SMART_SURVEY_SUMMONS_RESPONSE_SURVEY_ID = "SMART_SURVEY_SUMMONS_RESPONSE_SURVEY_ID";
    private static final String SMART_SURVEY_SUMMONS_RESPONSE_DAYS = "SMART_SURVEY_SUMMONS_RESPONSE_DAYS";
    private static final String SMART_SURVEY_SUMMONS_RESPONSE_EXPORT_NAME = "SMART_SURVEY_SUMMONS_RESPONSE_EXPORT_NAME";

    private final AppSettingRepository appSettingRepository;
    private final SystemParameterRepository systemParameterRepository;

    @Autowired
    public AppSettingServiceImpl(final AppSettingRepository appSettingRepository,
                                 final SystemParameterRepository systemParameterRepository) {
        Assert.notNull(appSettingRepository, "AppSettingRepository cannot be null!");
        Assert.notNull(systemParameterRepository, "SystemParameterRepository cannot be null!");
        this.appSettingRepository = appSettingRepository;
        this.systemParameterRepository = systemParameterRepository;
    }

    /**
     * Combine all application settings into a single list.
     *
     * @return All app settings and legacy Juror parameters
     */
    @Override
    public List<AppSetting> findAllSettings() {
        final ArrayList<AppSetting> appSettings = Lists.newArrayList(appSettingRepository.findAll());
        final ArrayList<SystemParameter> systemParameters = Lists.newArrayList(systemParameterRepository.findAll());

        final Stream<AppSetting> systemParamStream = systemParameters.stream().map(sp -> AppSetting.builder()
            .setting(String.valueOf(sp.getSpId()))
            .value(sp.getSpValue())
            .build());

        final List<AppSetting> combinedSettings = Stream.concat(appSettings.stream(), systemParamStream)
            .distinct()
            .collect(Collectors.toList());

        log.debug("Found {} application settings.", appSettings.size());
        log.debug("Found {} system parameters.", systemParameters.size());
        return combinedSettings;
    }

    @Override
    public Integer getBureauOfficerSearchResultLimit() {

        return convertToIntegerOrNull(SEARCH_RESULT_LIMIT_BUREAU_OFFICER);
    }

    @Override
    public Integer getTeamLeaderSearchResultLimit() {
        return convertToIntegerOrNull(SEARCH_RESULT_LIMIT_TEAM_LEADER);
    }

    @Override
    public Integer getDefaultCapacity() {
        return convertToIntegerOrNull(AUTO_ASSIGNMENT_DEFAULT_CAPACITY);
    }

    @Override
    public Integer getTotalNumberSummonsesSent() {
        return convertToIntegerOrNull(TOTAL_NUMBER_SUMMONSES_SENT);
    }

    @Override
    public Integer getTotalNumberOnlineReplies() {
        return convertToIntegerOrNull(TOTAL_NUMBER_ONLINE_REPLIES);
    }

    @Override
    public Integer getSendEmailOrSms() {
        return convertToIntegerOrNull(SEND_EMAIL_OR_SMS);
    }

    @Override
    public Integer getTriggeredCommsExcusalDays() {
        return convertToIntegerOrNull(TRIGGERED_COMMS_EXCUSAL_DAYS);
    }

    @Override
    public Integer getTriggeredCommsServiceCompletedDays() {
        return convertToIntegerOrNull(TRIGGERED_COMMS_SERVICE_COMPLETED_DAYS);
    }

    @Override
    public Integer getSmartSurveySummonsResponseDays() {
        return convertToIntegerOrNull(SMART_SURVEY_SUMMONS_RESPONSE_DAYS);
    }


    /**
     * Gets the APP_SETTINGS value WELSH_TRANSLATION.
     *
     * @return Welsh Translation Text
     */
    @Override
    public String getWelshTranslation() {
        Optional<AppSetting> welshTranslation = appSettingRepository.findById(WELSH_TRANSLATION);
        final AppSetting setting = welshTranslation.isPresent()
            ?
            welshTranslation.get()
            :
                null;
        if (setting != null) {
            final String welshTranslationText = setting.getValue();
            return
                (welshTranslationText);
        }

        return null;
    }


    @Override
    public boolean isWelshEnabled() {
        Optional<AppSetting> optWelshSetting = appSettingRepository.findById(WELSH_LANGUAGE_ENABLED);
        final AppSetting welshSetting = optWelshSetting.orElse(null);
        if (welshSetting != null) {
            boolean welshEnabled = BooleanUtils.toBoolean(welshSetting.getValue());
            if (log.isDebugEnabled()) {
                log.debug("Welsh language support enabled: {}", welshEnabled);
            }
            return welshEnabled;
        }
        log.warn("Welsh language support disabled. APP_SETTING {} not found!", WELSH_LANGUAGE_ENABLED);
        return false;
    }

    @Override
    public String getSmartSurveySummonsResponseSurveyId() {
        Optional<AppSetting> surveyId = appSettingRepository.findById(SMART_SURVEY_SUMMONS_RESPONSE_SURVEY_ID);
        final AppSetting setting = surveyId.isPresent()
            ?
            surveyId.get()
            :
                null;
        if (setting != null) {
            final String surveyIdText = setting.getValue();
            return
                (surveyIdText);
        }

        return null;
    }

    @Override
    public String getSmartSurveySummonsResponseExportName() {
        Optional<AppSetting> surveyId = appSettingRepository.findById(SMART_SURVEY_SUMMONS_RESPONSE_EXPORT_NAME);
        final AppSetting setting = surveyId.isPresent()
            ?
            surveyId.get()
            :
                null;
        if (setting != null) {
            final String surveyIdText = setting.getValue();
            return
                (surveyIdText);
        }

        return null;
    }

    /**
     * Attempts to convert setting value to integer, returns null if unable.
     *
     * @param settingName the name of the setting to retrieve
     * @return setting's value as an Integer, nullable
     */
    private Integer convertToIntegerOrNull(String settingName) {
        Optional<AppSetting> optSettingName = appSettingRepository.findById(settingName);
        final AppSetting setting = optSettingName.isPresent()
            ?
            optSettingName.get()
            :
                null;
        if (setting != null) {
            final String value = setting.getValue();
            if (isDigits(value) && NumberUtils.isCreatable(value)) {
                return Integer.valueOf(value);
            } else {
                log.warn("Configuration setting {} should have an integer value, but was {}", settingName, value);
                return null;
            }
        } else {
            log.warn("Configuration setting {} not set in database", settingName);
            return null;
        }
    }
}
