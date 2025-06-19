package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IJurorCommonResponseRepositoryMod {


    List<Tuple> getJurorResponseDetailsByUsernameAndStatus(String staffLogin,
                                                           Collection<ProcessingStatus> processingStatus,
                                                           Predicate... predicates);

    List<Tuple> getJurorResponseDetailsByCourtAndStatus(String locCode,
                                                           Collection<ProcessingStatus> processingStatus,
                                                           Predicate... predicates);

    Map<ProcessingStatus, Long> getJurorResponseCounts(Predicate... predicates);
}
