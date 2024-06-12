package uk.gov.hmcts.juror.api.moj.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.UserJurorResponseAudit;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.staff.UserJurorResponseAuditRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AssignOnUpdateServiceModImpl implements AssignOnUpdateServiceMod {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserJurorResponseAuditRepository userJurorResponseAuditRepository;


    @Override
    public void assignToCurrentLogin(DigitalResponse jurorResponse, String auditorUsername) {
        final User staffToAssign =
            userRepository.findOne(
                QUser.user.username.eq(auditorUsername)).orElseThrow(() -> {
                    log.warn("Assigning user '{}' Staff record does not exist!", auditorUsername);
                    return new MojException.NotFound("Assigning staff record does not exist!",
                        null);
                });

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
