package uk.gov.hmcts.juror.api.bureau.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.UserJurorResponseAudit;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.staff.UserJurorResponseAuditRepository;

import java.time.LocalDateTime;


/**
 * Implementation of service to assign backlog responses when an officer makes changes.
 * {@link AssignOnUpdateService}
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AssignOnUpdateServiceImpl implements AssignOnUpdateService {

    private final UserRepository userRepository;
    private final UserJurorResponseAuditRepository userJurorResponseAuditRepository;


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void assignToCurrentLogin(DigitalResponse jurorResponse, String auditorUsername) {
        final User staffToAssign = userRepository.findByUsername(auditorUsername);
        if (ObjectUtils.isEmpty(staffToAssign)) {
            log.warn("Assigning user '{}' Staff record does not exist!", auditorUsername);
            throw new StaffAssignmentException("Assigning staff record does not exist!");
        }

        final LocalDateTime assignmentDate = LocalDateTime.now();
        log.trace("Assignment date: {}", assignmentDate);

        // 1. perform assignment update
        jurorResponse.setStaff(staffToAssign);
        jurorResponse.setStaffAssignmentDate(assignmentDate.toLocalDate());

        // 2. create audit entity for assignment update
        final UserJurorResponseAudit userJurorResponseAudit = UserJurorResponseAudit.builder()
            .jurorNumber(jurorResponse.getJurorNumber())
            .assignedBy(staffToAssign)
            .assignedTo(staffToAssign)
            .assignedOn(assignmentDate)
            .build();

        // 3. persist audit entity
        log.trace("Auditing assignment {}", userJurorResponseAudit);
        userJurorResponseAuditRepository.save(userJurorResponseAudit);
    }

}
