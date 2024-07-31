package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.juror.domain.ApplicationSettings;

import java.util.Optional;

public interface ApplicationSettingService {

    Integer toInteger(ApplicationSettings.Setting setting, Integer defaultValue);

    Optional<ApplicationSettings> getAppSetting(ApplicationSettings.Setting setting);
}
