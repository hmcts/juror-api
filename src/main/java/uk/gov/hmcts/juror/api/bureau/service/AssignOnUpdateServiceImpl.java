package uk.gov.hmcts.juror.api.bureau.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.bureau.domain.StaffJurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.StaffJurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Implementation of service to assign backlog responses when an officer makes changes.
 * {@link AssignOnUpdateService}
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AssignOnUpdateServiceImpl implements AssignOnUpdateService {

    private final UserRepository userRepository;
    private final StaffJurorResponseAuditRepository auditRepository;


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void assignToCurrentLogin(JurorResponse jurorResponse, String auditorUsername) {
        final User staffToAssign = userRepository.findByUsername(auditorUsername);
        if (ObjectUtils.isEmpty(staffToAssign)) {
            log.warn("Assigning user '{}' Staff record does not exist!", auditorUsername);
            throw new StaffAssignmentException("Assigning staff record does not exist!");
        }

        final Date assignmentDate = Date.from(Instant.now().truncatedTo(ChronoUnit.DAYS));
        if (log.isTraceEnabled()) {
            log.trace("Assignment date: {}", assignmentDate);
        }

        // 1. perform assignment update
        jurorResponse.setStaff(staffToAssign);
        jurorResponse.setStaffAssignmentDate(assignmentDate);

        // 2. create audit entity for assignment update
        final StaffJurorResponseAudit staffJurorResponseAudit = StaffJurorResponseAudit.realBuilder()
            .teamLeaderLogin(auditorUsername)
            .staffLogin(auditorUsername)
            .jurorNumber(jurorResponse.getJurorNumber())
            .dateReceived(jurorResponse.getDateReceived())
            .staffAssignmentDate(assignmentDate)
            .build();

        // 3. persist audit entity
        if (log.isTraceEnabled()) {
            log.trace("Auditing assignment {}", staffJurorResponseAudit);
        }
        auditRepository.save(staffJurorResponseAudit);
    }

}
