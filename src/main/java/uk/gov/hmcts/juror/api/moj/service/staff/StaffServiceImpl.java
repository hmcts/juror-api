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
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.StaffJurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.StaffJurorResponseAuditRepositoryMod;

import java.time.LocalDate;

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
    private StaffJurorResponseAuditRepositoryMod staffJurorResponseAuditRepository;

    @Autowired
    private JurorPoolRepository poolRepository;

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    @SuppressWarnings({"PMD.NcssCount",
        "PMD.CognitiveComplexity",
        "PMD.NPathComplexity",
        "PMD.LawOfDemeter",
        "PMD.CyclomaticComplexity"})
    public StaffAssignmentResponseDto changeAssignment(final StaffAssignmentRequestDto staffAssignmentRequestDto,
                                                       final String currentUser) {
        // 1. validate input
        log.debug("Update assignment");
        final User assigningUser = userRepository.findByUsername(currentUser);
        if (ObjectUtils.isEmpty(assigningUser)) {
            log.warn("Assigning user '{}' Staff record does not exist!", currentUser);
            throw new MojException.NotFound("Assigning staff record does not exist!", null);
        }

        AbstractJurorResponse jurorResponse =
            jurorResponseCommonRepositoryMod.findByJurorNumber(staffAssignmentRequestDto.getResponseJurorNumber());

        if (ObjectUtils.isEmpty(jurorResponse)) {
            log.warn("Response '{}' record does not exist!", staffAssignmentRequestDto.getResponseJurorNumber());
            throw new MojException.NotFound("Juror response record does not exist!", null);
        } else {
            /*
             * Log conditions where we may wish to stop the assignment based on unconfirmed AC.
             */
            // validate states of response invalid for assignment
            if (jurorResponse.getProcessingComplete()
                || ProcessingStatus.CLOSED == jurorResponse.getProcessingStatus()) {
                // log status of record to trace for now (It's already closed and/or processed)
                if (log.isTraceEnabled()) {
                    log.trace(
                        "Juror Response {}: processingComplete={} processingStatus={}",
                        jurorResponse.getJurorNumber(),
                        jurorResponse.getProcessingComplete(),
                        jurorResponse.getProcessingStatus()
                    );
                }
                log.error("Rejected assignment as the response is already closed: {}", staffAssignmentRequestDto);
                throw new MojException.BusinessRuleViolation(
                    "Rejected assignment as the response is already closed: "
                        + staffAssignmentRequestDto.getResponseJurorNumber(),
                    null);
            }
        }

        User assignToUser = null;
        if (staffAssignmentRequestDto.getAssignTo() != null) {
            if (0 == JurorDigitalApplication.AUTO_USER.compareToIgnoreCase(staffAssignmentRequestDto.getAssignTo())) {
                log.error("Cannot assign the {} user to responses manually", JurorDigitalApplication.AUTO_USER);
                throw new MojException.BusinessRuleViolation(
                    "Cannot change assignment to user " + JurorDigitalApplication.AUTO_USER, null);
            }
            assignToUser = userRepository.findByUsername(staffAssignmentRequestDto.getAssignTo());
            if (ObjectUtils.isEmpty(assignToUser)) {
                log.warn(
                    "Assigned to user '{}' Staff record does not exist!",
                    staffAssignmentRequestDto.getAssignTo()
                );
                throw new MojException.NotFound("Assigned to staff record does not exist!", null);
            }
        } else if (jurorResponse.getProcessingStatus() == ProcessingStatus.TODO) {
            // user not supplied, so move the response to the backlog.
            log.debug("No user assigned to the response - return to backlog");

            // JDB-2641 Urgent summons cannot be assigned to backlog
            if (jurorResponse.getUrgent()) {
                log.debug(
                    "Unable to assign response for Juror {} to backlog as it is urgent",
                    jurorResponse.getJurorNumber()
                );
                throw new MojException.BusinessRuleViolation("Unable to assign response for Juror "
                    + jurorResponse.getJurorNumber() + " to backlog as it is urgent", null);
            }

            // JDB-2641 Super Urgent summons cannot be assigned to backlog
            if (jurorResponse.getSuperUrgent()) {
                log.debug(
                    "Unable to assign response for Juror {} to backlog as it is super-urgent",
                    jurorResponse.getJurorNumber()
                );
                throw new MojException.BusinessRuleViolation("Unable to assign response for Juror "
                    + jurorResponse.getJurorNumber() + " to backlog as it is super-urgent", null);
            }

            // JDB-2488 AC18 - Only team leads can send to backlog
            if (!assigningUser.isTeamLeader()) {
                log.debug("Unable to assign response {} to backlog as user {} does not have rights",
                    jurorResponse.getJurorNumber(), assigningUser.getUsername()
                );
                throw new MojException.Forbidden(String.format(
                    "Unable to assign response for Juror %s to backlog"
                        + " as user %s does not have rights",
                    jurorResponse.getJurorNumber(),
                    assigningUser.getUsername()), null
                );
            }

        } else {
            log.debug("Unable to assign response for Juror {} to backlog as the processing status is {}",
                jurorResponse.getJurorNumber(), jurorResponse.getProcessingStatus()
            );
            throw new MojException.BusinessRuleViolation(String.format(
                "Unable to assign response for Juror %s to backlog"
                    + " as the processing status is %s",
                jurorResponse.getJurorNumber(),
                jurorResponse.getProcessingStatus()
            ), null);
        }

        final LocalDate assignmentDate = LocalDate.now();
        if (log.isTraceEnabled()) {
            log.trace("Assignment date: {}", assignmentDate);
        }

        // 2. audit entity
        final StaffJurorResponseAuditMod staffJurorResponseAudit = StaffJurorResponseAuditMod.realBuilder()
            .teamLeaderLogin(assigningUser.getUsername())
            .staffLogin(assignToUser != null
                ?
                assignToUser.getUsername()
                :
                    null)
            .jurorNumber(jurorResponse.getJurorNumber())
            .dateReceived(jurorResponse.getDateReceived())
            .staffAssignmentDate(assignmentDate)
            .build();

        // 3. perform update

        jurorResponse.setStaff(assignToUser);// may be null!
        jurorResponse.setStaffAssignmentDate(assignmentDate);
        // set optimistic lock version from UI
        log.debug("Version: DB={}, UI={}", jurorResponse.getVersion(), staffAssignmentRequestDto.getVersion());

        // 4. persist
        if (log.isTraceEnabled()) {
            log.trace("Updating assignment on {}", jurorResponse);
        }

        if (jurorResponse.getReplyType().getType().equals(ReplyMethod.DIGITAL.getDescription())) {
            //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
            entityManager.detach(jurorResponse);
            jurorResponse.setVersion(staffAssignmentRequestDto.getVersion());
            jurorResponseRepository.save((DigitalResponse) jurorResponse);
        } else {
            jurorPaperResponseRepositoryMod.save((PaperResponse) jurorResponse);
        }

        if (log.isTraceEnabled()) {
            log.trace("Auditing assignment {}", staffJurorResponseAudit);
        }
        staffJurorResponseAuditRepository.save(staffJurorResponseAudit);

        // 5. response
        final String assignedTo = jurorResponse.getStaff() != null
            ?
            jurorResponse.getStaff().getUsername()
            :
                null;
        log.info(
            "Updated staff assignment: '{}' assigned '{}' to response '{}' on '{}'",
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
}
