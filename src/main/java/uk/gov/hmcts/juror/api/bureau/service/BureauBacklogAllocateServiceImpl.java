package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauBacklogAllocateRequestDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauBacklogAllocateException;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.UserJurorResponseAudit;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.UserJurorResponseAuditRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Implementation of the Backlog Allocate Responses service. Allocation performed on specified Bureau Officers.
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BureauBacklogAllocateServiceImpl implements BureauBacklogAllocateService {

    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    private final UserRepository userRepository;
    private final UserJurorResponseAuditRepository userJurorResponseAuditRepository;

    @Override
    @Transactional
    public void allocateBacklogReplies(BureauBacklogAllocateRequestDto request, String requestingUser) {
        log.trace("Inside BureauBacklogAllocateServiceImpl.allocateReplies()...user : {} ", requestingUser);

        if (requestingUser == null || isBlank(requestingUser)) {
            log.error("Requesting user value was invalid ({}), and is required for audit", requestingUser);
            throw new BureauBacklogAllocateException.RequestingUserIsRequired();
        }
        performAllocations(request.getOfficerAllocations(), requestingUser);

    }

    /**
     * Drives the Allocations, performs various data setups and retrieves the reponses backlog data.
     *
     * @param staffAllocation List of Requested StaffAllocations.
     * @param requestingUser  Logged in user.
     */
    private void performAllocations(List<BureauBacklogAllocateRequestDto.StaffAllocation> staffAllocation,
                                    String requestingUser) {

        // Get the Staff records from the db for all the officers in the request body
        final List<String> staffLogins = Lists.transform(
            staffAllocation,
            BureauBacklogAllocateRequestDto.StaffAllocation::getUserId
        );
        LinkedList<User> staff = new LinkedList<>(userRepository.findAllByUsernameIn(staffLogins));

        //transfom the request body in to a map<Officer, StaffAllocationObject).
        final Map<String, BureauBacklogAllocateRequestDto.StaffAllocation> urgencyMap = staffAllocation.stream()
            .collect(Collectors.toMap(
                BureauBacklogAllocateRequestDto.StaffAllocation::getUserId,
                officerAllocation -> officerAllocation
            ));

        for (User staffMember : staff) {

            final List<DigitalResponse> toBeAllocated = new LinkedList<>();
            final List<UserJurorResponseAudit> auditEntries = new LinkedList<>();

            log.trace("Allocating backlog responses to bureau officer : {} ", staffMember.getUsername());

            //Process NON URGENT responses for this staffMember
            Integer staffMemberNonUrgentCount = MoreObjects.firstNonNull(
                urgencyMap.get(staffMember.getUsername()).getNonUrgentCount(),
                0
            );
            if (staffMemberNonUrgentCount > 0) {
                toBeAllocated.addAll(allocateResponses(
                    JurorResponseQueries.byUnassignedTodoNonUrgent(),
                    staffMember,
                    staffMemberNonUrgentCount
                ));
            }

            //Process URGENT responses for this staffMember
            Integer staffMemberUrgentCount = MoreObjects.firstNonNull(
                urgencyMap.get(staffMember.getUsername()).getUrgentCount(),
                0
            );
            if (staffMemberUrgentCount > 0) {
                toBeAllocated.addAll(allocateResponses(
                    JurorResponseQueries.byUnassignedTodoUrgent(),
                    staffMember,
                    staffMemberUrgentCount
                ));
            }

            // Generate audit records for allocations.
            if (!toBeAllocated.isEmpty()) {
                auditEntries.addAll(auditAllocatedEntries(toBeAllocated, staffMember, requestingUser));
            }

            //persist Allocated response data and audit data.
            log.trace(
                "toBeAllocated count : {} (prior to save) and hashcode : {} ",
                toBeAllocated.size(),
                toBeAllocated.hashCode()
            );
            log.trace(
                "audit count : {} (prior to save) and hashcode : {} ",
                auditEntries.size(),
                auditEntries.hashCode()
            );

            try {
                jurorResponseRepository.saveAll(toBeAllocated);
                userJurorResponseAuditRepository.saveAll(auditEntries);
            } catch (OptimisticLockingFailureException olfe) {
                log.warn("One or more Juror responses was updated by another user!. Try Allocation again.");
                throw new BureauOptimisticLockingException(olfe);
            } catch (RuntimeException e) {
                throw new BureauBacklogAllocateException.FailedToSaveAllocations(staffMember.getUsername());
            }
            log.debug(
                "{} backlog responses allocated to bureau officer : {} ",
                toBeAllocated.size(),
                staffMember.getUsername()
            );
        }

    }

    /**
     * Called for each Urgency (Non Urgent, Urgent, Super Urgent to retrieve the relevant responses and allocate them
     * to the given staffMember.
     *
     * @param condition            Creiteria for the query to return the responses based on urgency.
     * @param staffMember          details of selected staff to allocate responses to.
     * @param urgencyAllocateCount Requested Allocate count for Staff (ie nonUrgentCount)
     *
     * @return List Allocated Responses for staff member.
     */
    private List<DigitalResponse> allocateResponses(BooleanExpression condition, User staffMember,
                                                    Integer urgencyAllocateCount) {

        //Fetch the responses to allocate to the staffMember. Only fetches requested amount (urgencyAllocateCount)
        final List<DigitalResponse> urgencybacklog = getBacklogData(condition, urgencyAllocateCount);

        //Perform Allocations to the staffMember
        return allocate(urgencybacklog, staffMember, urgencyAllocateCount);
    }

    /**
     * Allocates workload among staff members.
     *
     * @param backlogData          repsonses to allocate.
     * @param staff                staffMember to allocate responses to.
     * @param urgencyAllocateCount Requested Allocate count for Staff (ie nonUrgentCount)
     *
     * @return List Allocated Responses for saff member.
     */
    private List<DigitalResponse> allocate(List<DigitalResponse> backlogData, User staff,
                                           Integer urgencyAllocateCount) {

        final List<DigitalResponse> allocation = new LinkedList<>();

        final Iterator<DigitalResponse> backlogItems = backlogData.iterator();
        final LocalDate now = LocalDateTime.now().toLocalDate();

        for (int j = 0;
             j < urgencyAllocateCount && backlogItems.hasNext();
             j++) {
            allocation.add(backlogItems.next());
        }

        allocation.forEach((r) -> {
            r.setStaff(staff);
            r.setStaffAssignmentDate(now);
            r.setVersion(r.getVersion() != null
                ?
                r.getVersion() + 1
                :
                    1);

        });
        return allocation;
    }

    /**
     * Audit Records for the allocation for a particular staff member.
     *
     * @param allocation     allocated responses.
     * @param staff          details of selected staff to allocate responses to.
     * @param requestingUser logged in user
     *
     * @return List StaffJurorResponseAudit
     */
    private List<UserJurorResponseAudit> auditAllocatedEntries(List<DigitalResponse> allocation, User staff,
                                                               String requestingUser) {

        final List<UserJurorResponseAudit> auditEntries = new LinkedList<>();

        final User assignedBy = userRepository.findByUsername(requestingUser);
        if (ObjectUtils.isEmpty(userRepository)) {
            throw new StaffAssignmentException("Assigning staff record does not exist!");
        }

        allocation.forEach(r -> {
            auditEntries.add(UserJurorResponseAudit.builder()
                .jurorNumber(r.getJurorNumber())
                .assignedBy(assignedBy)
                .assignedTo(staff)
                .assignedOn(LocalDateTime.now())
                .build());
        });

        return auditEntries;
    }

    /**
     * Query the Juror backlog responses based on given conditions and order.
     *
     * @param condition    Criteria for response query.
     * @param resultsLimit Requested no of responses to retrieve.
     *
     * @return List JurorResponse
     */
    private List<DigitalResponse> getBacklogData(BooleanExpression condition, int resultsLimit) {
        Page<DigitalResponse> responses = jurorResponseRepository.findAll(condition, PageRequest.of(
            0, resultsLimit, Sort.Direction.ASC, "dateReceived"));
        return responses.getContent();
    }

}
