package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.juror.domain.ApplicationSettings;

import java.time.LocalTime;
import java.util.Optional;

public interface ApplicationSettingService {
    LocalTime toLocalTime(ApplicationSettings.Setting setting);

    Optional<ApplicationSettings> getAppSetting(ApplicationSettings.Setting setting);
}
