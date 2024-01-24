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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.request.AssignmentsMultiRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.MultipleStaffAssignmentDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.ReassignResponsesDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffMemberCrudRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffMemberCrudResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AssignmentsListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AssignmentsListDto.AssignmentListDataDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.OperationFailureDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.OperationFailureListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffRosterResponseDto;
import uk.gov.hmcts.juror.api.bureau.domain.QTeam;
import uk.gov.hmcts.juror.api.bureau.domain.StaffAmendmentAction;
import uk.gov.hmcts.juror.api.bureau.domain.StaffAudit;
import uk.gov.hmcts.juror.api.bureau.domain.StaffAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.StaffJurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.StaffJurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.Team;
import uk.gov.hmcts.juror.api.bureau.domain.TeamRepository;
import uk.gov.hmcts.juror.api.bureau.exception.ReassignException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.QJurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.QPool;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.AUTO_USER;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.active;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.inactive;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.loginAllowed;
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
    private final JurorResponseRepository jurorResponseRepository;
    private final StaffJurorResponseAuditRepository staffJurorResponseAuditRepository;
    private final PoolRepository poolRepository;
    private final EntityManager entityManager;
    private final TeamRepository teamRepository;
    private final StaffAuditRepository staffAuditRepository;
    private final BureauTransformsService bureauTransformsService;


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
        final JurorResponse jurorResponse = jurorResponseRepository.findByJurorNumber(
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
            if (jurorResponse.getUrgent()) {
                log.debug(
                    "Unable to assign response for Juror {} to backlog as it is urgent",
                    jurorResponse.getJurorNumber()
                );
                throw new StaffAssignmentException.StatusUrgent(
                    jurorResponse.getJurorNumber(),
                    staffAssignmentRequestDto.getAssignTo()
                );
            }

            // JDB-2641 Super Urgent summons cannot be assigned to backlog
            if (jurorResponse.getSuperUrgent()) {
                log.debug(
                    "Unable to assign response for Juror {} to backlog as it is super-urgent",
                    jurorResponse.getJurorNumber()
                );
                throw new StaffAssignmentException.StatusSuperUrgent(
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

        final Date assignmentDate = Date.from(Instant.now().truncatedTo(ChronoUnit.DAYS));
        if (log.isTraceEnabled()) {
            log.trace("Assignment date: {}", assignmentDate);
        }

        // 2. audit entity
        final StaffJurorResponseAudit staffJurorResponseAudit = StaffJurorResponseAudit.realBuilder()
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
        //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
        entityManager.detach(jurorResponse);
        jurorResponse.setStaff(assignToUser);// may be null!
        jurorResponse.setStaffAssignmentDate(assignmentDate);
        // set optimistic lock version from UI
        log.debug("Version: DB={}, UI={}", jurorResponse.getVersion(), staffAssignmentRequestDto.getVersion());
        jurorResponse.setVersion(staffAssignmentRequestDto.getVersion());

        // 4. persist
        if (log.isTraceEnabled()) {
            log.trace("Updating assignment on {}", jurorResponse);
        }
        jurorResponseRepository.save(jurorResponse);
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
            Lists.newArrayList(userRepository.findAll(active().and(owner(SecurityUtil.getActiveUsersBureauPayload().getOwner())),
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
     * @param urgentJurorResponse Previously persisted juror response entity
     * @throws StaffAssignmentException Failed to assign the response to a staff member
     */
    @Override
    @Transactional
    public void assignUrgentResponse(final JurorResponse urgentJurorResponse) throws StaffAssignmentException {
        if (!urgentJurorResponse.getSuperUrgent() && !urgentJurorResponse.getUrgent()) {
            // this state should be invalid
            log.warn("Not urgent or super urgent: {}", urgentJurorResponse);
            throw new StaffAssignmentException("Response not urgent: " + urgentJurorResponse);
        }

        // get an attached version of the response from the DB
        final JurorResponse updateResponse =
            jurorResponseRepository.findByJurorNumber(urgentJurorResponse.getJurorNumber());

        //for want of a unified DTO for JUROR.POOL

        final Pool pool = poolRepository.findOne(QPool.pool.jurorNumber.eq(urgentJurorResponse.getJurorNumber())).get();
        final String courtId = pool.getCourt().getLocCode();

        final List<User> availableStaff = new ArrayList<>();
        Iterables.addAll(availableStaff, userRepository.findUsersByCourt(entityManager, courtId));
        if (!availableStaff.isEmpty()) {
            // assign a random staff member to the juror response
            final User staffToAssign = availableStaff.get(RandomUtils.nextInt(0, availableStaff.size()));
            updateResponse.setStaff(staffToAssign);
            final Date now =
                Date.from(Instant.now().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toInstant());
            if (log.isDebugEnabled()) {
                log.debug("Setting now as {}", now);
            }
            updateResponse.setStaffAssignmentDate(now);
            if (log.isTraceEnabled()) {
                log.trace("Assigning juror response {}", urgentJurorResponse);
            }
            jurorResponseRepository.save(updateResponse);

            final StaffJurorResponseAudit staffJurorResponseAudit = StaffJurorResponseAudit.realBuilder()
                .teamLeaderLogin(AUTO_USER)
                .staffLogin(staffToAssign.getUsername())
                .jurorNumber(urgentJurorResponse.getJurorNumber())
                .dateReceived(urgentJurorResponse.getDateReceived())
                .staffAssignmentDate(now)
                .build();
            if (log.isTraceEnabled()) {
                log.trace("Auditing urgent assignment {}", staffJurorResponseAudit);
            }
            staffJurorResponseAuditRepository.save(staffJurorResponseAudit);
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

    /**
     * Get multiple staff assignments.
     *
     * @param responseListDto List of response numbers to retrieve information on
     * @param currentUser     The user carrying out this operation.
     */
    public AssignmentsListDto getStaffAssignments(AssignmentsMultiRequestDto responseListDto, String currentUser) {
        List<AssignmentListDataDto> assignmentListDataDtos = new ArrayList<>();

        final QJurorResponse query = QJurorResponse.jurorResponse;
        Iterable<JurorResponse> jurorResponses =
            jurorResponseRepository.findAll(query.jurorNumber.in(responseListDto.getJurorNumbers()));

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
        for (JurorResponse jurorResponse : jurorResponses) {

            String assignedTo = null;
            if (null != jurorResponse.getStaff()) {
                assignedTo = jurorResponse.getStaff().getUsername();
            }

            StringBuilder jurorDisplayName = new StringBuilder();
            String jurorTitle = jurorResponse.getTitle();
            String jurorFirstName = jurorResponse.getFirstName();
            String jurorLastName = jurorResponse.getLastName();

            jurorDisplayName.append((jurorTitle != null)
                ?
                jurorTitle
                :
                "");
            jurorDisplayName.append((jurorFirstName != null)
                ?
                " " + jurorFirstName
                :
                "");
            jurorDisplayName.append((jurorLastName != null)
                ?
                " " + jurorLastName
                :
                "");

            AssignmentListDataDto assignmentListDataDto = new AssignmentListDataDto(
                jurorResponse.getJurorNumber(),
                jurorResponse.getVersion(),
                assignedTo,
                jurorResponse.getProcessingStatus(),
                jurorResponse.getUrgent(),
                jurorResponse.getSuperUrgent(),
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
            } catch (StaffAssignmentException.StatusUrgent | StaffAssignmentException.StatusSuperUrgent
                     | StaffAssignmentException.StatusClosed e) {
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

        Iterable<JurorResponse> jurorResponses = jurorResponseRepository.findAll(responseGroupFilter);

        for (JurorResponse jurorResponse : jurorResponses) {
            final Date assignmentDate = Date.from(Instant.now().truncatedTo(ChronoUnit.DAYS));
            if (log.isTraceEnabled()) {
                log.trace("Assignment date: {}", assignmentDate);
            }

            // audit entity
            final StaffJurorResponseAudit staffJurorResponseAudit = StaffJurorResponseAudit.realBuilder()
                .teamLeaderLogin(assigningUser)
                .staffLogin(assignToUser)
                .jurorNumber(jurorResponse.getJurorNumber())
                .dateReceived(jurorResponse.getDateReceived())
                .staffAssignmentDate(assignmentDate)
                .build();

            // perform update
            jurorResponse.setStaff(newStaffToAssign);// may be null!
            jurorResponse.setStaffAssignmentDate(assignmentDate);

            // persist
            if (log.isTraceEnabled()) {
                log.trace("Updating assignment on {}", jurorResponse);
            }
            jurorResponseRepository.save(jurorResponse);
            if (log.isTraceEnabled()) {
                log.trace("Auditing reassignment {}", staffJurorResponseAudit);
            }
            staffJurorResponseAuditRepository.save(staffJurorResponseAudit);
        }
    }

    @Override
    @Transactional
    public StaffMemberCrudResponseDto createNewStaffMember(
        final StaffMemberCrudRequestDto staffMember, final String currentUser)
        throws StaffMemberCrudException {
        log.trace("Creating new staff member");
        //1. check the login name is not in use by another staff member entry
        if (0 == JurorDigitalApplication.AUTO_USER.compareToIgnoreCase(staffMember.getLogin())) {
            log.error("Cannot create the {} user!", JurorDigitalApplication.AUTO_USER);
            throw new StaffMemberCrudException("Cannot create user " + JurorDigitalApplication.AUTO_USER);
        }

        final User existingStaff = userRepository.findByUsername(staffMember.getLogin());
        if (existingStaff != null) {
            // found an existing staff member assigned to the juror login name (bad)
            final String message =
                "Juror username " + staffMember.getLogin() + " has already been allocated to "
                    + existingStaff.getName();
            log.warn(message);
            throw new StaffMemberCrudException(message);
        }
        log.trace("Login name {} is not already assigned to a staff member", staffMember.getLogin());

        //2. get a attached reference to the team
        Optional<Team> optTeam = teamRepository.findOne(QTeam.team.id.eq(staffMember.getTeam()));
        final Team team = optTeam.orElse(null);
        if (null == team) {
            log.error("Team {} was not found!", staffMember.getTeam());
            throw new StaffMemberCrudException("Invalid team id: " + staffMember.getTeam());
        }

        //3. save the new staff member

        final User staffEntity = userRepository.save(staffMemberCrudDtoToStaffEntity(staffMember, team));
        //4. audit change
        final StaffAudit staffAudit = StaffAudit.builder(staffEntity, StaffAmendmentAction.CREATE, currentUser).build();
        log.trace("Auditing: {}", staffAudit);
        staffAuditRepository.save(staffAudit);

        //5. commit the new user to the database and refresh the entity (Ensures courts are loaded)
        entityManager.flush();
        entityManager.refresh(staffEntity);
        //6. return dto of the new team member
        return staffEntityToStaffMemberCrudDto(staffEntity);
    }

    @Override
    public StaffMemberCrudResponseDto updateStaffMember(final String login, final StaffMemberCrudRequestDto staffMember,
                                                        final String currentUser)
        throws StaffMemberCrudException, JurorAccountLockedException {
        log.trace("Updating new staff member");
        // check the staff member exists
        if (0 == JurorDigitalApplication.AUTO_USER.compareToIgnoreCase(login)) {
            log.error("Cannot update the {} user!", JurorDigitalApplication.AUTO_USER);
            throw new StaffMemberCrudException("Cannot update user " + JurorDigitalApplication.AUTO_USER);
        }
        Optional<User> optStaff = userRepository.findOne(QUser.user.username.equalsIgnoreCase(login));
        final User existingStaff = optStaff.orElse(null);
        if (null == existingStaff) {
            // didn't find the staff member (bad)
            final String message = "Juror username " + staffMember.getLogin() + "does not exist";
            log.warn(message);
            throw new StaffMemberCrudException(message);
        }
        if (log.isTraceEnabled()) {
            log.trace("Found staff member {}", existingStaff);
        }
        if (!existingStaff.isActive()
            && staffMember.isActive()
            && userRepository.count(loginAllowed(login)) == 0) {
            log.info("Cannot activate {} as they are not allowed to login to Juror", login);
            throw new JurorAccountLockedException("Cannot activate user that is not permitted to login to Juror");
        }

        //2. get a attached reference to the team if it has changed
        Optional<Team> optTeam = teamRepository.findOne(QTeam.team.id.eq(staffMember.getTeam()));
        final Team updateTeam = optTeam.orElse(null);
        if (null == updateTeam && log.isDebugEnabled()) {
            log.debug("Team to update {} was not found!", staffMember.getTeam());
        }

        //3. audit the existing user as a history event within the transaction
        final StaffAudit staffAudit = StaffAudit.builder(existingStaff, StaffAmendmentAction.EDIT, currentUser).build();
        log.trace("Auditing: {}", staffAudit);
        // NOTE: save the audit after the changes have been saved!

        //4. update the new staff member
        // note we do not allow updating the login field as it is the primary key
        existingStaff.setName(staffMember.getName());
        existingStaff.setLevel(staffMember.getLevel());
        existingStaff.setActive(staffMember.isActive());

        existingStaff.setTeam(updateTeam);
        existingStaff.setVersion(staffMember.getVersion());


        entityManager.detach(existingStaff);
        final User updatedStaff = userRepository.save(existingStaff);
        staffAuditRepository.save(staffAudit);

        //5. return dto of the new team member
        final StaffMemberCrudResponseDto updatedStaffDto = staffEntityToStaffMemberCrudDto(updatedStaff);
        if (log.isTraceEnabled()) {
            log.trace("Updated staff from {} to {}", staffMember, updatedStaff);
        }
        return updatedStaffDto;
    }

    @PreAuthorize(SecurityUtil.TEAM_LEADER_AUTH)
    private User staffMemberCrudDtoToStaffEntity(final StaffMemberCrudRequestDto dto, final Team teamEntity) {
        BureauJWTPayload authPrincipal = SecurityUtil.getActiveUsersBureauPayload();
        final User staff = new User(
            authPrincipal.getOwner(),
            dto.getLogin(),
            dto.getName(),
            dto.getLevel(),
            dto.isActive(),
            null,
            null,
            teamEntity,
            dto.getVersion(),
            null,
            null,
            null,
            null,
            0,
            true
        );
        if (log.isTraceEnabled()) {
            log.trace("Converted {} and {} into {}", dto, teamEntity, staff);
        }
        return staff;
    }

    /**
     * Convert a {@link User} entity to a {@link StaffMemberCrudResponseDto}.
     *
     * @param entity domain layer entity
     * @return controller layer dto
     */
    private StaffMemberCrudResponseDto staffEntityToStaffMemberCrudDto(final User entity) {
        final StaffMemberCrudResponseDto dto = new StaffMemberCrudResponseDto(
            entity.getUsername(),
            entity.getName(),
            entity.isTeamLeader(),
            entity.isActive(),
            entity.getTeam().getId(),
            entity.getCourtAtIndex(0, null),
            entity.getCourtAtIndex(1, null),
            entity.getCourtAtIndex(2, null),
            entity.getCourtAtIndex(3, null),
            entity.getCourtAtIndex(4, null),
            entity.getCourtAtIndex(5, null),
            entity.getCourtAtIndex(6, null),
            entity.getCourtAtIndex(7, null),
            entity.getCourtAtIndex(8, null),
            entity.getCourtAtIndex(9, null),
            entity.getVersion()
        );

        if (log.isTraceEnabled()) {
            log.trace("Converted {} into {}", entity, dto);
        }

        return dto;
    }
}
