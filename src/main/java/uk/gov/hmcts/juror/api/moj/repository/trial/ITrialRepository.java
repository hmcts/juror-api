package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.core.Tuple;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;

import java.time.LocalDate;
import java.util.List;

public interface ITrialRepository {
    List<Trial> getListOfTrialsForCourtLocations(List<String> locCode, boolean isActiveFilter,
                                                 String trialNumber, Pageable pageable);

    List<Trial> getListOfActiveTrials(String locCode);

    Long getTotalTrialsForCourtLocations(List<String> locCode, boolean isActiveFilter);

    List<Tuple> getActiveTrialsWithJurorCount(String locationCode, LocalDate attendanceDate);
}
