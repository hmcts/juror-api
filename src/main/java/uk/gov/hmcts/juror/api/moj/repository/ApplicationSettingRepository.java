package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.juror.domain.ApplicationSettings;

@Repository
public interface ApplicationSettingRepository extends ReadOnlyRepository<ApplicationSettings,
    ApplicationSettings.Setting> {
}
