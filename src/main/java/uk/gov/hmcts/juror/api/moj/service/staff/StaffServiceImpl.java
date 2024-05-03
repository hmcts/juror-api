package uk.gov.hmcts.juror.api.moj.service.staff;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.UserJurorResponseAudit;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.UserJurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;

@Slf4j
@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Autowired
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepositoryMod;
    @Autowired
    private JurorResponseCommonRepositoryMod jurorResponseCommonRepositoryMod;
    @Autowired
    private UserJurorResponseAuditRepository userJurorResponseAuditRepository;
    @Autowired
    private JurorPoolRepository poolRepository;
    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    @SuppressWarnings({"PMD.NcssCount",
        "PMD.CognitiveComplexity",
        "PMD.NPathComplexity",
        "PMD.LawOfDemeter"})
    public StaffAssignmentResponseDto changeAssignment(final StaffAssignmentRequestDto staffAssignmentRequestDto,
                                                       final String currentUser) {
        log.trace("enter changeAssignment");

        // 1. validate input
        final User assigningUser = userRepository.findByUsername(currentUser);
        if (ObjectUtils.isEmpty(assigningUser)) {
            log.warn("Assigning user '{}' Staff record does not exist!", currentUser);
            throw new MojException.NotFound("Assigning staff record does not exist!", null);
        }

        AbstractJurorResponse jurorResponse =
            getJurorResponseForAssignment(staffAssignmentRequestDto.getResponseJurorNumber());

        User assignToUser = null;
        if (staffAssignmentRequestDto.getAssignTo() != null) {

            assignToUser = getValidAssignToUser(staffAssignmentRequestDto.getAssignTo());

        } else if (jurorResponse.getProcessingStatus() == ProcessingStatus.TODO) {

            // user not supplied, so move the response to the backlog.
            log.debug("No user assigned to the response - return to backlog");
            validateJurorResponseForBacklogAssignment(jurorResponse, assigningUser.getUsername());

        } else {
            throw new MojException.BusinessRuleViolation(String.format("Unable to assign response for Juror %s to "
                    + "backlog as the processing status is %s", jurorResponse.getJurorNumber(),
                jurorResponse.getProcessingStatus()), null);
        }

        final LocalDateTime assignedOn = LocalDateTime.now();
        if (log.isTraceEnabled()) {
            log.trace("Assignment date: {}", assignedOn);
        }

        // 2. audit entity
        final UserJurorResponseAudit userJurorResponseAudit = UserJurorResponseAudit.builder()
            .assignedBy(assigningUser)
            .assignedTo(assignToUser)
            .jurorNumber(jurorResponse.getJurorNumber())
            .assignedOn(assignedOn)
            .build();

        // 3. perform update
        jurorResponse.setStaff(assignToUser);// may be null!
        jurorResponse.setStaffAssignmentDate(assignedOn.toLocalDate());

        log.trace("Updating assignment on {}", jurorResponse);

        if (jurorResponse.getReplyType().getType().equals(ReplyMethod.DIGITAL.getDescription())) {
            //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
            entityManager.detach(jurorResponse);
            jurorResponse.setVersion(staffAssignmentRequestDto.getVersion());
            jurorResponseRepository.save((DigitalResponse) jurorResponse);
        } else {
            jurorPaperResponseRepositoryMod.save((PaperResponse) jurorResponse);
        }

        log.trace("Auditing assignment {}", userJurorResponseAudit);
        userJurorResponseAuditRepository.save(userJurorResponseAudit);

        // 5. response
        final String assignedTo = jurorResponse.getStaff() != null
            ? jurorResponse.getStaff().getUsername()
            : null;

        log.info("Updated staff assignment: '{}' assigned '{}' to response '{}' on '{}'",
            assigningUser.getUsername(),
            assignedTo,
            jurorResponse.getJurorNumber(),
            jurorResponse.getStaffAssignmentDate()
        );

        return StaffAssignmentResponseDto.builder()
            .assignedBy(assigningUser.getUsername())
            .assignedTo(assignedTo)
            .jurorResponse(jurorResponse.getJurorNumber())
            .assignmentDate(jurorResponse.getStaffAssignmentDate())
            .build();
    }

    private AbstractJurorResponse getJurorResponseForAssignment(String jurorNumber) {
        log.debug("Retrieve juror response for juror: {}", jurorNumber);

        AbstractJurorResponse jurorResponse =
            jurorResponseCommonRepositoryMod.findByJurorNumber(jurorNumber);

        if (ObjectUtils.isEmpty(jurorResponse)) {

            log.warn("Response '{}' record does not exist!", jurorNumber);
            throw new MojException.NotFound("Juror response record does not exist!", null);

        } else if (Boolean.TRUE.equals(jurorResponse.getProcessingComplete())
            || ProcessingStatus.CLOSED == jurorResponse.getProcessingStatus()) {

            log.trace("Juror Response {}: processingComplete={} processingStatus={}",
                jurorResponse.getJurorNumber(),
                jurorResponse.getProcessingComplete(),
                jurorResponse.getProcessingStatus());

            throw new MojException.BusinessRuleViolation(
                "Rejected assignment as the response is already closed: "
                    + jurorNumber, null);
        }

        return jurorResponse;
    }

    private User getValidAssignToUser(String username) {
        log.trace("Enter getAssignToUser for username {}", username);

        if (JurorDigitalApplication.AUTO_USER.equalsIgnoreCase(username)) {

            log.error("Cannot assign the {} user to responses manually", JurorDigitalApplication.AUTO_USER);
            throw new MojException.BusinessRuleViolation(
                "Cannot change assignment to user " + JurorDigitalApplication.AUTO_USER, null);
        }

        User assignToUser = null;

        log.trace("Retrieve user record from the database for username {}", username);
        assignToUser = userRepository.findByUsername(username);

        if (ObjectUtils.isEmpty(assignToUser)) {
            throw new MojException.NotFound("Assigned to staff record does not exist!", null);
        }

        log.trace("Exit getAssignToUser for username {}", username);
        return assignToUser;
    }

    private void validateJurorResponseForBacklogAssignment(AbstractJurorResponse jurorResponse, String assignedBy) {
        log.trace("Enter validateJurorResponseForBacklogAssignment");

        // JDB-2641 Urgent summons cannot be assigned to backlog
        if (jurorResponse.isUrgent()) {
            throw new MojException.BusinessRuleViolation("Unable to assign response for Juror "
                + jurorResponse.getJurorNumber() + " to backlog as it is urgent", null);
        }

        // JDB-2641 Super Urgent summons cannot be assigned to backlog
        if (jurorResponse.isSuperUrgent()) {
            throw new MojException.BusinessRuleViolation("Unable to assign response for Juror "
                + jurorResponse.getJurorNumber() + " to backlog as it is super-urgent", null);
        }

        // JDB-2488 AC18 - Only team leads can send to backlog
        if (!SecurityUtil.isBureauManager()) {
            throw new MojException.Forbidden(String.format("Unable to assign response for Juror %s to backlog "
                + "as user %s does not have rights", jurorResponse.getJurorNumber(), assignedBy), null);
        }

        log.trace("Exit validateJurorResponseForBacklogAssignment");
    }
}
