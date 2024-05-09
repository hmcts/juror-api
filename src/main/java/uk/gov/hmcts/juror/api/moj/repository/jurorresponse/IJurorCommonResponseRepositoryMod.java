package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.util.Collection;
import java.util.List;

public interface IJurorCommonResponseRepositoryMod {


    List<Tuple> getJurorResponseDetailsByUsernameAndStatus(String staffLogin,
                                                           Collection<ProcessingStatus> processingStatus,
                                                           Predicate... predicates);
}
