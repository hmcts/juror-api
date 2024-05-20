package uk.gov.hmcts.juror.api.moj.service.audit;

import uk.gov.hmcts.juror.api.moj.audit.dto.JurorAudit;

import java.time.LocalDate;
import java.util.List;

public interface JurorAuditService {
    List<JurorAudit> getAllAuditsFor(List<String> jurorNumbers);

    List<JurorAudit> getAllAuditsChangedBetweenAndHasCourt(LocalDate fromDate, LocalDate toDate,
                                                           List<String> locCodes);

    JurorAudit getPreviousJurorAudit(JurorAudit juror);
}
