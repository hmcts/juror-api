package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;

import java.time.LocalDate;
import java.util.List;

public interface IJurorDigitalResponseRepositoryMod {
    Tuple getAssignRepliesStatistics();

    List<Tuple> getAssignRepliesStatisticForUsers();

    List<Tuple> getDigitalSummonsRepliesForMonth(LocalDate startMonth);
}
