package uk.gov.hmcts.juror.api.moj.service.audit;

import uk.gov.hmcts.juror.api.moj.domain.Juror;

import java.time.LocalDate;
import java.util.List;

public interface JurorAuditService {
    List<Juror> getAllAuditsFor(List<String> jurorNumbers);

    List<Juror> getAllAuditsChangedBetweenAndHasCourt(LocalDate fromDate, LocalDate toDate,
                                                     List<String> locCodes);

    Juror getNextJurorAudit(Juror juror);

    Juror getPreviousJurorAudit(Juror juror);
}
