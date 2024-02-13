package uk.gov.hmcts.juror.api.moj.repository.trial;

import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;

import java.util.List;

public interface ITrialRepository {
    List<Trial> getListOfTrialsForCourtLocations(List<String> locCode, boolean isActiveFilter,
                                                 String trialNumber, Pageable pageable);

    Long getTotalTrialsForCourtLocations(List<String> locCode, boolean isActiveFilter);
}
