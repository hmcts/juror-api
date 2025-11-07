package uk.gov.hmcts.juror.api.moj.service.audit;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.audit.dto.TransportLimitAuditRecord;
import uk.gov.hmcts.juror.api.moj.domain.RevisionInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CourtLocationAuditServiceImpl implements CourtLocationAuditService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<TransportLimitAuditRecord> getTransportLimitAuditHistory(String locCode) {
        log.debug("Retrieving transport limit audit history for court location: {}", locCode);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Get all revision numbers for this court location
        List<Number> revisions = auditReader.getRevisions(CourtLocation.class, locCode);

        if (revisions.isEmpty()) {
            log.info("No audit history found for court location: {}", locCode);
            return new ArrayList<>();
        }

        List<TransportLimitAuditRecord> auditRecords = new ArrayList<>();

        for (int i = 0; i < revisions.size(); i++) {
            Number currentRevision = revisions.get(i);

            // Get current state at this revision
            CourtLocation currentEntity = auditReader.find(
                CourtLocation.class, locCode, currentRevision);

            if (currentEntity == null) {
                log.warn("Could not find court location {} at revision {}", locCode, currentRevision);
                continue;
            }

            // Get revision metadata (timestamp and changed_by)
            RevisionInfo revisionInfo = auditReader.findRevision(
                RevisionInfo.class, currentRevision);

            // Get previous state (if exists)
            CourtLocation previousEntity = null;
            if (i > 0) {
                Number previousRevision = revisions.get(i - 1);
                previousEntity = auditReader.find(
                    CourtLocation.class, locCode, previousRevision);
            }

            // Check if transport limits changed in this revision
            boolean publicTransportChanged = hasChanged(
                previousEntity != null ? previousEntity.getPublicTransportSoftLimit() : null,
                currentEntity.getPublicTransportSoftLimit()
            );

            boolean taxiChanged = hasChanged(
                previousEntity != null ? previousEntity.getTaxiSoftLimit() : null,
                currentEntity.getTaxiSoftLimit()
            );

            // Only add record if one of the transport limits changed
            if (publicTransportChanged || taxiChanged) {
                TransportLimitAuditRecord record = buildAuditRecord(
                    locCode,
                    currentEntity,
                    previousEntity,
                    revisionInfo,
                    currentRevision
                );
                auditRecords.add(record);
            }
        }

        log.info("Found {} audit records with transport limit changes for court location: {}",
                 auditRecords.size(), locCode);

        return auditRecords;
    }

    @Override
    @Transactional(readOnly = true)
    public TransportLimitAuditRecord getLatestTransportLimitChange(String locCode) {
        log.debug("Retrieving latest transport limit change for court location: {}", locCode);

        List<TransportLimitAuditRecord> history = getTransportLimitAuditHistory(locCode);

        if (history.isEmpty()) {
            log.info("No transport limit changes found for court location: {}", locCode);
            return null;
        }

        // Return the last record (most recent)
        return history.get(history.size() - 1);
    }

    /**
     * Check if a BigDecimal value has changed between revisions.
     *
     * @param previous Previous value (can be null)
     * @param current Current value (can be null)
     * @return true if values are different, false otherwise
     */
    private boolean hasChanged(BigDecimal previous, BigDecimal current) {
        if (previous == null && current == null) {
            return false;
        }
        if (previous == null || current == null) {
            return true;
        }
        return previous.compareTo(current) != 0;
    }

    /**
     * Build an audit record from entity states and revision info.
     *
     * @param locCode Court location code
     * @param currentEntity Current entity state
     * @param previousEntity Previous entity state (can be null)
     * @param revisionInfo Revision metadata
     * @param currentRevision Current revision number
     * @return Populated TransportLimitAuditRecord
     */
    private TransportLimitAuditRecord buildAuditRecord(
        String locCode,
        CourtLocation currentEntity,
        CourtLocation previousEntity,
        RevisionInfo revisionInfo,
        Number currentRevision) {

        return TransportLimitAuditRecord.builder()
            .locCode(locCode)
            .courtName(currentEntity.getLocCourtName())
            .revisionNumber(currentRevision.longValue())
            .changedBy(revisionInfo.getChangedBy())
            .changeDateTime(revisionInfo.getRevisionDate())
            .publicTransportPreviousValue(
                previousEntity != null ? previousEntity.getPublicTransportSoftLimit() : null)
            .publicTransportCurrentValue(currentEntity.getPublicTransportSoftLimit())
            .taxiPreviousValue(
                previousEntity != null ? previousEntity.getTaxiSoftLimit() : null)
            .taxiCurrentValue(currentEntity.getTaxiSoftLimit())
            .build();
    }
}
