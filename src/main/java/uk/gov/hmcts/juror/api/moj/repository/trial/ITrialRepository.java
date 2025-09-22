package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.core.Tuple;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public interface ITrialRepository {

    <T> PaginatedList<T> getListOfTrials(TrialSearch trialSearch,
                                         Function<Trial, T> dataMapper);

    List<Trial> getListOfActiveTrials(String locCode);

    List<Tuple> getActiveTrialsWithJurorCount(String locationCode, LocalDate attendanceDate);

    List<PanelListDto> getReturnedJurors(String trialNo, String locCode);
}
