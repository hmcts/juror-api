package uk.gov.hmcts.juror.api.moj.service.audit;

import uk.gov.hmcts.juror.api.moj.audit.dto.TransportLimitAuditRecord;

import java.util.List;

public interface CourtLocationAuditService {

    /**
     * Get audit history showing only changes to transport limits.
     *
     * @param locCode The court location code
     * @return List of audit records where transport limits changed, ordered chronologically
     */

    List<TransportLimitAuditRecord> getTransportLimitAuditHistory(String locCode);

    /**
     * Get transport limit audit history for all court locations.
     * users to see all court limit changes.
     *
     * @return List of audit records showing transport limit changes across all courts
     */
    List<TransportLimitAuditRecord> getAllTransportLimitAuditHistory();

    /**
     * Get the most recent audit record for transport limits.
     *
     * @param locCode The court location code
     * @return The most recent audit record, or null if none exists
     */
    TransportLimitAuditRecord getLatestTransportLimitChange(String locCode);


}
