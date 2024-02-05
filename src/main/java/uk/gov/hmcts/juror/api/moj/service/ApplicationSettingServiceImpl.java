package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.ApplicationSettings;
import uk.gov.hmcts.juror.api.moj.repository.ApplicationSettingRepository;

import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationSettingServiceImpl implements ApplicationSettingService {

    private final ApplicationSettingRepository applicationSettingRepository;

    @Override
    public LocalTime toLocalTime(ApplicationSettings.Setting setting) {
        return getAppSetting(setting)
            .map(settings -> LocalTime.parse(settings.getValue()))
            .orElse(null);
    }

    @Override
    public Optional<ApplicationSettings> getAppSetting(ApplicationSettings.Setting setting) {
        return applicationSettingRepository.findById(setting);
    }
}
