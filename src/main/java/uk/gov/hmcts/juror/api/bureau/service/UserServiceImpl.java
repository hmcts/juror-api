package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.request.AssignmentsMultiRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.MultipleStaffAssignmentDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.ReassignResponsesDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AssignmentsListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AssignmentsListDto.AssignmentListDataDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.OperationFailureDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.OperationFailureListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffRosterResponseDto;
import uk.gov.hmcts.juror.api.bureau.exception.ReassignException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCommon;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.AUTO_USER;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.active;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.inactive;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.owner;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.sortNameAsc;

/**
 * Implementation of {@link UserService}.
 */
@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserJurorResponseAuditRepository userJurorResponseAuditRepository;
    private final JurorPoolRepository poolRepository;
    private final EntityManager entityManager;
    private final BureauTransformsService bureauTransformsService;
    private final JurorResponseCommonRepositoryMod jurorResponseCommonRepositoryMod;
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @Transactional
    @Override
    public StaffAssignmentResponseDto changeAssignment(final StaffAssignmentRequestDto staffAssignmentRequestDto,
                                                       final String currentUser)
        throws StaffAssignmentException {
        // 1. validate input
        log.debug("Update assignment");
        final User assigningUser = userRepository.findByUsername(currentUser);
        if (ObjectUtils.isEmpty(assigningUser)) {
            log.warn("Assigning user '{}' Staff record does not exist!", currentUser);
            throw new StaffAssignmentException("Assigning staff record does not exist!");
        }
        AbstractJurorResponse jurorResponse = jurorResponseCommonRepositoryMod.findByJurorNumber(
            staffAssignmentRequestDto.getResponseJurorNumber());

        if (ObjectUtils.isEmpty(jurorResponse)) {
            log.warn("Response '{}' record does not exist!", staffAssignmentRequestDto.getResponseJurorNumber());
            throw new StaffAssignmentException("Juror response record does not exist!");
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
                throw new StaffAssignmentException.StatusClosed(
                    jurorResponse.getJurorNumber(),
                    staffAssignmentRequestDto.getAssignTo()
                );
            }
        }

        User assignToUser = null;
        if (staffAssignmentRequestDto.getAssignTo() != null) {
            if (0 == JurorDigitalApplication.AUTO_USER.compareToIgnoreCase(staffAssignmentRequestDto.getAssignTo())) {
                log.error("Cannot assign the {} user to responses manually", JurorDigitalApplication.AUTO_USER);
                throw new StaffAssignmentException(
                    "Cannot change assignment to user " + JurorDigitalApplication.AUTO_USER);
            }
            assignToUser = userRepository.findByUsername(staffAssignmentRequestDto.getAssignTo());
            if (ObjectUtils.isEmpty(assignToUser)) {
                log.warn(
                    "Assigned to user '{}' Staff record does not exist!",
                    staffAssignmentRequestDto.getAssignTo()
                );
                throw new StaffAssignmentException("Assigned to staff record does not exist!");
            }
        } else if (jurorResponse.getProcessingStatus() == ProcessingStatus.TODO) {
            // user not supplied, so move the response to the backlog.
            log.debug("No user assigned to the response - return to backlog");

            // JDB-2641 Urgent summons cannot be assigned to backlog
            if (jurorResponse.isUrgent()) {
                log.debug(
                    "Unable to assign response for Juror {} to backlog as it is urgent",
                    jurorResponse.getJurorNumber()
                );
                throw new StaffAssignmentException.StatusUrgent(
                    jurorResponse.getJurorNumber(),
                    staffAssignmentRequestDto.getAssignTo()
                );
            }

            // JDB-2488 AC18 - Only team leads can send to backlog
            if (!assigningUser.isTeamLeader()) {
                log.debug("Unable to assign response {} to backlog as user {} does not have rights",
                    jurorResponse.getJurorNumber(), assigningUser.getUsername()
                );
                throw new StaffAssignmentException(String.format(
                    "Unable to assign response for Juror %s to backlog"
                        + " as user %s does not have rights",
                    jurorResponse.getJurorNumber(),
                    assigningUser.getUsername()
                ));
            }

        } else {
            log.debug("Unable to assign response for Juror {} to backlog as the processing status is {}",
                jurorResponse.getJurorNumber(), jurorResponse.getProcessingStatus()
            );
            throw new StaffAssignmentException(String.format(
                "Unable to assign response for Juror %s to backlog"
                    + " as the processing status is %s",
                jurorResponse.getJurorNumber(),
                jurorResponse.getProcessingStatus()
            ));
        }

        final LocalDateTime assignmentDate = LocalDateTime.now();
        log.trace("Assignment date: {}", assignmentDate);

        // 2. audit entity
        final UserJurorResponseAudit userJurorResponseAudit = UserJurorResponseAudit.builder()
            .jurorNumber(jurorResponse.getJurorNumber())
            .assignedBy(assigningUser)
            .assignedTo(assignToUser)
            .assignedOn(assignmentDate)
            .build();

        // 3. perform update
        //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
        entityManager.detach(jurorResponse);
        jurorResponse.setStaff(assignToUser);// may be null!
        jurorResponse.setStaffAssignmentDate(assignmentDate.toLocalDate());

        // set optimistic lock version from UI
        log.debug("Version: DB={}, UI={}", jurorResponse.getVersion(), staffAssignmentRequestDto.getVersion());
        jurorResponse.setVersion(staffAssignmentRequestDto.getVersion());

        // 4. persist
        log.trace("Updating assignment on {}", jurorResponse);
        saveResponse(jurorResponse);

        log.trace("Auditing assignment {}", userJurorResponseAudit);
        userJurorResponseAuditRepository.save(userJurorResponseAudit);

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

    private void saveResponse(AbstractJurorResponse jurorResponse) {
        if (jurorResponse.getReplyType().getType().equals(ReplyMethod.DIGITAL.getDescription())) {
            jurorResponseRepository.save((DigitalResponse) jurorResponse);
        } else {
            jurorPaperResponseRepository.save((PaperResponse) jurorResponse);
        }
    }

    @Override
    public StaffListDto getAll() {
        return StaffListDto.builder()
            .data(StaffListDto.StaffListDataDto.builder()
                .activeStaff(StreamSupport.stream(userRepository.findAll(active(), sortNameAsc())
                        .spliterator(), false)
                    .map(bureauTransformsService::toStaffDto)
                    .collect(Collectors.toList())
                )
                .inactiveStaff(StreamSupport.stream(userRepository.findAll(inactive(), sortNameAsc())
                        .spliterator(), false)
                    .map(bureauTransformsService::toStaffDto)
                    .collect(Collectors.toList()))
                .build()
            )
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffRosterResponseDto activeStaffRoster() {
        log.trace("Getting active staff roster");
        final List<User> activeStaffList =
            Lists.newArrayList(
                userRepository.findAll(active().and(owner(SecurityUtil.getActiveOwner())),
                    sortNameAsc()));
        log.debug("Found {} active staff members", activeStaffList.size());
        return StaffRosterResponseDto.builder()
            .data(activeStaffList.stream()
                .map(s -> StaffRosterResponseDto.StaffDto.builder()
                    .login(s.getUsername())
                    .name(s.getName())
                    .build())
                .sorted(Comparator.comparing(StaffRosterResponseDto.StaffDto::getName))
                .toList())
            .build();
    }

    @Override
    public StaffDetailDto getOne(final String login) throws NoMatchForLoginException {
        final User match = userRepository.findByUsername(login);
        if (match == null) {
            throw new NoMatchForLoginException(login);
        }
        return StaffDetailDto.builder().data(bureauTransformsService.toStaffDto(match)).build();
    }

    /**
     * Assign urgent response.
     *
     * @param urgentJurorResponse Previously persisted juror response entity
     *
     * @throws StaffAssignmentException Failed to assign the response to a staff member
     */
    @Override
    @Transactional
    public void assignUrgentResponse(final DigitalResponse urgentJurorResponse) throws StaffAssignmentException {
        if (!urgentJurorResponse.isUrgent()) {
            // this state should be invalid
            log.warn("Not urgent: {}", urgentJurorResponse);
            throw new StaffAssignmentException("Response not urgent: " + urgentJurorResponse);
        }

        // get an attached version of the response from the DB
        AbstractJurorResponse updateResponse = jurorResponseCommonRepositoryMod.findByJurorNumber(
            urgentJurorResponse.getJurorNumber());

        //for want of a unified DTO for JUROR.POOL

        final JurorPool pool =
            poolRepository.findOne(QJuror.juror.jurorNumber.eq(urgentJurorResponse.getJurorNumber())).get();
        final String courtId = pool.getCourt().getLocCode();

        final List<User> availableStaff = new ArrayList<>();
        Iterables.addAll(availableStaff, userRepository.findUsersByCourt(entityManager, courtId));
        if (!availableStaff.isEmpty()) {
            // assign a random staff member to the juror response
            final User staffToAssign = availableStaff.get(RandomUtils.nextInt(0, availableStaff.size()));
            updateResponse.setStaff(staffToAssign);

            final LocalDateTime now = LocalDateTime.now();
            log.debug("Setting now as {}", now);
            updateResponse.setStaffAssignmentDate(now.toLocalDate());

            log.trace("Assigning juror response {}", urgentJurorResponse);
            saveResponse(updateResponse);

            final User assignedBy = userRepository.findByUsername(AUTO_USER);
            if (ObjectUtils.isEmpty(userRepository)) {
                throw new StaffAssignmentException("Assigning staff record does not exist!");
            }

            final UserJurorResponseAudit userJurorResponseAudit = UserJurorResponseAudit.builder()
                .jurorNumber(urgentJurorResponse.getJurorNumber())
                .assignedBy(assignedBy)
                .assignedTo(staffToAssign)
                .assignedOn(now)
                .build();

            log.trace("Auditing urgent assignment {}", userJurorResponseAudit);
            userJurorResponseAuditRepository.save(userJurorResponseAudit);
        } else {
            // business logic dictates that if no staff exist for the court leave the response unassigned.
            log.warn("No staff matched court {}. Leaving juror {} response unassigned!", courtId,
                urgentJurorResponse.getJurorNumber()
            );
        }

        if (log.isDebugEnabled()) {
            log.debug("Assigned {} response to {} on {}", urgentJurorResponse.getJurorNumber(),
                updateResponse.getStaff(), updateResponse.getStaffAssignmentDate()
            );
        }
    }

    @Override
    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new MojException.NotFound("User not found", null);
        }
        return user;
    }

    /**
     * Get multiple staff assignments.
     *
     * @param responseListDto List of response numbers to retrieve information on
     * @param currentUser     The user carrying out this operation.
     */
    @Transactional(readOnly = true)
    @Override
    public AssignmentsListDto getStaffAssignments(AssignmentsMultiRequestDto responseListDto, String currentUser) {
        List<AssignmentListDataDto> assignmentListDataDtos = new ArrayList<>();

        List<JurorResponseCommon> jurorResponses =
            jurorResponseCommonRepositoryMod.findByJurorNumberIn(responseListDto.getJurorNumbers());

        Set<String> jurorResponseSet = new HashSet<>();
        jurorResponses.forEach(jurorResponse -> jurorResponseSet.add(jurorResponse.getJurorNumber()));

        // create list of missing juror responses
        List<String> missingResponses = responseListDto.getJurorNumbers().stream()
            .filter(jr -> !jurorResponseSet.contains(jr))
            .collect(Collectors.toList());

        if (missingResponses.isEmpty()) {
            log.warn(String.format(
                "%s juror responses could not be found when trying to "
                    + "retrieve staff assignments: %s", 0,
                String.join(", ", missingResponses)
            ));
        }

        // process list of responses
        for (JurorResponseCommon jurorResponse : jurorResponses) {

            String assignedTo = null;
            if (null != jurorResponse.getStaff()) {
                assignedTo = jurorResponse.getStaff().getUsername();
            }

            StringBuilder jurorDisplayName = new StringBuilder();
            String jurorTitle = jurorResponse.getTitle();
            String jurorFirstName = jurorResponse.getFirstName();
            String jurorLastName = jurorResponse.getLastName();

            jurorDisplayName.append(jurorTitle != null ? jurorTitle : "");
            jurorDisplayName.append(jurorFirstName != null ? " " + jurorFirstName : "");
            jurorDisplayName.append(jurorLastName != null ? " " + jurorLastName : "");

            AssignmentListDataDto assignmentListDataDto = new AssignmentListDataDto(
                jurorResponse.getJurorNumber(),
                jurorResponse.getVersion(),
                assignedTo,
                jurorResponse.getProcessingStatus(),
                jurorResponse.isUrgent(),
                jurorDisplayName.toString().trim()
            );
            assignmentListDataDtos.add(assignmentListDataDto);
        }

        return new AssignmentsListDto(assignmentListDataDtos);
    }

    /**
     * Change multiple staff assignments. Internally calls {@link #changeAssignment(StaffAssignmentRequestDto, String)}.
     *
     * @param multipleStaffAssignmentDto Multiple staff assignment request payload.
     * @param currentUser                The user carrying out this operation.
     *
     * @throws StaffAssignmentException Failed to assign the staff member to the response.
     * @see #changeAssignment(StaffAssignmentRequestDto, String)
     */
    @Transactional
    @Override
    public OperationFailureListDto multipleChangeAssignment(
        final MultipleStaffAssignmentDto multipleStaffAssignmentDto,
        final String currentUser) throws StaffAssignmentException {

        List<OperationFailureDto> failuresList = new ArrayList<>();

        multipleStaffAssignmentDto.getResponses().forEach(responseMetadata -> {
            // convert the multi update data for the staff service and update
            try {
                StaffAssignmentResponseDto responseDto = this.changeAssignment(StaffAssignmentRequestDto.builder()
                    .assignTo(multipleStaffAssignmentDto.getAssignTo())
                    .responseJurorNumber(responseMetadata.getResponseJurorNumber())
                    .version(responseMetadata.getVersion())
                    .build(), currentUser);
                if (log.isTraceEnabled()) {
                    log.trace("Assignment updated: {}", responseDto);
                }
            } catch (StaffAssignmentException.StatusUrgent | StaffAssignmentException.StatusClosed e) {
                log.debug("StaffAssignment Status-related exception caught during multiple assignment", e);
                OperationFailureDto failureReason = new OperationFailureDto(
                    responseMetadata.getResponseJurorNumber(),
                    e.getReasonCode()
                );
                failuresList.add(failureReason);
            } catch (StaffAssignmentException e) {
                log.debug("StaffAssignment exception caught during multiple assignment", e);
                OperationFailureDto failureReason = new OperationFailureDto(
                    responseMetadata.getResponseJurorNumber(),
                    e.getReasonCode()
                );
                failuresList.add(failureReason);
            }
        });
        return new OperationFailureListDto(failuresList);
    }

    @Override
    @Transactional
    public void reassignResponses(String auditorUsername, ReassignResponsesDto reassignResponsesDto) {
        final String staffToDeactivateLogin = reassignResponsesDto.getStaffToDeactivate();
        log.debug("Received reassignment/deactivate orders for staff member {} from team lead {}",
            staffToDeactivateLogin, auditorUsername
        );

        final User staffToDeactivate = userRepository.findByUsername(staffToDeactivateLogin);
        if (staffToDeactivate == null) {
            log.error("Unable to deactivate staff member {} as they could not be found", staffToDeactivateLogin);
            throw new ReassignException.StaffMemberNotFound(staffToDeactivateLogin);
        }

        final List<ProcessingStatus> urgentStatuses = Arrays.asList(
            ProcessingStatus.AWAITING_CONTACT,
            ProcessingStatus.AWAITING_COURT_REPLY,
            ProcessingStatus.AWAITING_TRANSLATION,
            ProcessingStatus.TODO
        );

        final List<ProcessingStatus> pendingStatuses = Arrays.asList(
            ProcessingStatus.AWAITING_CONTACT,
            ProcessingStatus.AWAITING_COURT_REPLY,
            ProcessingStatus.AWAITING_TRANSLATION
        );

        final List<ProcessingStatus> todoStatus = Collections.singletonList(ProcessingStatus.TODO);

        BooleanExpression urgentsFilter = JurorResponseQueries.byAssignmentAndProcessingStatusAndUrgency(
            staffToDeactivateLogin, urgentStatuses, true);
        long count = jurorResponseRepository.count(urgentsFilter);
        if (count > 0) {
            // there are urgent/super-urgent responses to be reassigned
            if (reassignResponsesDto.getUrgentsLogin() == null) {
                log.error(
                    "Cannot assign {}'s Urgent/Super-Urgent responses to backlog as this is forbidden",
                    staffToDeactivateLogin
                );
                throw new ReassignException.UnableToAssignToBacklog(staffToDeactivateLogin, "Urgent/Super-Urgent");
            }
            log.debug("Attempting to reassign {} Urgent responses from {} to {}",
                count, staffToDeactivateLogin, reassignResponsesDto.getUrgentsLogin()
            );
            reassignResponseGroup(urgentsFilter, auditorUsername, reassignResponsesDto.getUrgentsLogin());
        }

        BooleanExpression pendingFilter = JurorResponseQueries.byAssignmentAndProcessingStatusAndUrgency(
            staffToDeactivateLogin, pendingStatuses, false);
        count = jurorResponseRepository.count(pendingFilter);
        if (count > 0) {
            // there are 'pending' responses to be reassigned
            if (reassignResponsesDto.getPendingLogin() == null) {
                log.error(
                    "Cannot assign {}'s Pending responses to backlog as this is forbidden",
                    staffToDeactivateLogin
                );
                throw new ReassignException.UnableToAssignToBacklog(staffToDeactivateLogin, "Pending");
            }
            log.debug("Attempting to reassign {} Pending responses from {} to {}", count, staffToDeactivateLogin,
                reassignResponsesDto.getPendingLogin()
            );
            reassignResponseGroup(pendingFilter, auditorUsername, reassignResponsesDto.getPendingLogin());
        }

        BooleanExpression todoFilter = JurorResponseQueries.byAssignmentAndProcessingStatusAndUrgency(
            staffToDeactivateLogin, todoStatus, false);
        count = jurorResponseRepository.count(todoFilter);
        if (count > 0) {
            // there are to-do responses to be reassigned
            log.debug("Attempting to reassign {} To-do responses from {} to {}",
                count, staffToDeactivateLogin, reassignResponsesDto.getTodoLogin()
            );
            reassignResponseGroup(todoFilter, auditorUsername, reassignResponsesDto.getTodoLogin());
        }

        log.info("Reassigned responses from {} pending deactivation of staff account.", staffToDeactivateLogin);
    }

    private void reassignResponseGroup(BooleanExpression responseGroupFilter, String assigningUser,
                                       String assignToUser) {
        User newStaffToAssign = null;
        if (!Strings.isNullOrEmpty(assignToUser)) {
            newStaffToAssign = userRepository.findByUsername(assignToUser);
            if (newStaffToAssign == null) {
                log.error("Unable to find staff member {}", assignToUser);
                throw new ReassignException.StaffMemberNotFound(assignToUser);
            }
        }

        Iterable<DigitalResponse> jurorResponses = jurorResponseRepository.findAll(responseGroupFilter);

        User assignedBy = userRepository.findByUsername(assigningUser);
        if (assignedBy == null) {
            throw new ReassignException.StaffMemberNotFound(assigningUser);
        }

        for (DigitalResponse jurorResponse : jurorResponses) {
            final LocalDateTime assignmentDate = LocalDateTime.now();
            log.trace("Assignment date: {}", assignmentDate);

            // audit entity
            final UserJurorResponseAudit userJurorResponseAudit = UserJurorResponseAudit.builder()
                .jurorNumber(jurorResponse.getJurorNumber())
                .assignedBy(assignedBy)
                .assignedTo(newStaffToAssign)
                .assignedOn(assignmentDate)
                .build();

            // perform update
            jurorResponse.setStaff(newStaffToAssign);// may be null!
            jurorResponse.setStaffAssignmentDate(assignmentDate.toLocalDate());

            // persist
            log.trace("Updating assignment on {}", jurorResponse);
            jurorResponseRepository.save(jurorResponse);
            log.trace("Auditing reassignment {}", userJurorResponseAudit);

            userJurorResponseAuditRepository.save(userJurorResponseAudit);
        }
    }
}
