package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.controller.request.AutoAssignRequest;
import uk.gov.hmcts.juror.api.bureau.controller.response.AutoAssignResponse;
import uk.gov.hmcts.juror.api.bureau.domain.UserQueries;
import uk.gov.hmcts.juror.api.bureau.exception.AutoAssignException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.StaffJurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.StaffJurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * Implementation of the auto-assignment service using a round-robin strategy.
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AutoAssignmentServiceImpl implements AutoAssignmentService {

    static final int DEFAULT_CAPACITY_FALLBACK = 60;

    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    private final UserRepository userRepository;
    private final AppSettingService appSettingService;
    private final StaffJurorResponseAuditRepositoryMod auditRepository;


    @Override
    @Transactional
    public void autoAssign(AutoAssignRequest request, String requestingUser) throws AutoAssignException {

        if (requestingUser == null || isBlank(requestingUser)) {
            log.error("Requesting user value was invalid ({}), and is required for audit", requestingUser);
            throw new AutoAssignException.RequestingUserIsRequired();
        }

        List<String> logins = Lists.transform(request.getData(), AutoAssignRequest.StaffCapacity::getLogin);
        List<String> duplicates = logins.stream().filter(login -> Collections.frequency(logins, login) > 1).toList();

        if (!duplicates.isEmpty()) {
            log.error("Input contained duplicate capacity values for logins: {}", duplicates);
            throw new AutoAssignException.DuplicateCapacityValues(duplicates);
        }

        final int totalCapacity = request.getData().stream().map(AutoAssignRequest.StaffCapacity::getCapacity).mapToInt(
            Integer::intValue).sum();

        log.trace("Total assignment capacity for auto-assignment is {}", totalCapacity);

        final List<DigitalResponse> backlog = Lists.newLinkedList(jurorResponseRepository.findAll(
            JurorResponseQueries.byUnassignedTodoNonUrgent(),
            JurorResponseQueries.oldestFirst()
        ));
        final int backlogSize = backlog.size();

        log.trace("Backlog size for auto-assignment is {}", backlogSize);

        if (totalCapacity > backlogSize) {
            log.error("Total capacity of {} exceeds backlog size of {}", totalCapacity, backlogSize);
            throw new AutoAssignException.CapacityBiggerThanBacklog(totalCapacity, backlogSize);
        }

        final List<StaffJurorResponseAuditMod> auditEntries =
            assignAndAudit(backlog, request.getData(), requestingUser);

        jurorResponseRepository.saveAll(backlog);
        auditRepository.saveAll(auditEntries);

    }

    @Override
    public AutoAssignResponse getAutoAssignmentData() {

        final List<User> bureauOfficers = Lists.newLinkedList(userRepository
            .findAll(UserQueries.activeBureauOfficers()));

        List<AutoAssignResponse.StaffCapacityResponse> staffCapacityList = new ArrayList<>(bureauOfficers.size());

        final int defaultCapacity = getDefaultCapacity();

        for (User bureauOfficer : bureauOfficers) {

            final long incompletes = jurorResponseRepository.count(JurorResponseQueries.assignedIncompletes(
                bureauOfficer));
            final long urgents = jurorResponseRepository.count(JurorResponseQueries.assignedUrgents(bureauOfficer));

            final AutoAssignResponse.StaffCapacityResponse capacity = AutoAssignResponse.StaffCapacityResponse
                .responseBuilder()
                .capacity(defaultCapacity)
                .urgents(urgents)
                .allocation(defaultCapacity + urgents)
                .incompletes(incompletes)
                .login(bureauOfficer.getUsername())
                .name(bureauOfficer.getName())
                .build();

            staffCapacityList.add(capacity);
        }
        return AutoAssignResponse.builder().data(staffCapacityList)
            .meta(AutoAssignResponse.AutoAssignmentMetadata.builder()
                .backlogSize(jurorResponseRepository.count(JurorResponseQueries.byUnassignedTodoNonUrgent()))
                .build())
            .build();
    }

    /**
     * Gets the default capacity for a bureau officer.
     * The default value is 60, but this can be overridden with a database setting.
     *
     * @return default capacity
     * @see AppSettingService#getDefaultCapacity()
     */
    private Integer getDefaultCapacity() {
        Integer configuredValue = appSettingService.getDefaultCapacity();
        if (configuredValue == null) {
            log.debug("No default capacity value configured, using fallback value of {}", DEFAULT_CAPACITY_FALLBACK);
            configuredValue = DEFAULT_CAPACITY_FALLBACK;
        } else if (configuredValue <= 0) {
            log.error(
                "Invalid default capacity value of {} configured (must be greater than 0), using fallback value of {}",
                configuredValue,
                DEFAULT_CAPACITY_FALLBACK
            );
            configuredValue = DEFAULT_CAPACITY_FALLBACK;
        }
        log.trace("Returning capacity value of {}", configuredValue);
        return configuredValue;
    }

    /**
     * Assigns backlog items to staff members.
     *
     * @param backlog        backlog to assign
     * @param staffCapacity  capacity details of staff members
     * @param requestingUser the logged-in user for audit purposes
     * @return audit entries for the assignments
     * @throws AutoAssignException if input is invalid
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<StaffJurorResponseAuditMod> assignAndAudit(List<DigitalResponse> backlog,
                                                           List<AutoAssignRequest.StaffCapacity> staffCapacity,
                                                           String requestingUser) throws AutoAssignException {

        final List<String> staffLogins = staffCapacity.stream().map(AutoAssignRequest.StaffCapacity::getLogin).toList();
        LinkedList<User> staff = new LinkedList<>(userRepository.findAllByUsernameIn(staffLogins));
        checkForMissingStaff(staffLogins, staff);
        checkForInvalidStaff(staff);

        log.debug("Assigning backlog items to bureau officers");

        final Map<String, Integer> capacityMap = staffCapacity.stream().collect(Collectors.toMap(
            AutoAssignRequest.StaffCapacity::getLogin,
            AutoAssignRequest.StaffCapacity::getCapacity
        ));

        final Map<User, Set<DigitalResponse>> allocation = distributeWorkload(backlog, staff, capacityMap);

        final List<StaffJurorResponseAuditMod> auditEntries = new LinkedList<>();
        final LocalDate now = LocalDateTime.now().toLocalDate();

        allocation.forEach((key, value) -> value.forEach(r -> {
            r.setStaff(key);
            r.setStaffAssignmentDate(now);
            r.setVersion(r.getVersion() != null ? r.getVersion() + 1 : 1);
            auditEntries.add(StaffJurorResponseAuditMod.realBuilder()
                .teamLeaderLogin(requestingUser)
                .staffLogin(key.getUsername())
                .jurorNumber(r.getJurorNumber())
                .dateReceived(r.getDateReceived())
                .staffAssignmentDate(now)
                .build());
        }));
        return auditEntries;
    }

    /**
     * Distributes workload among staff members
     *
     * <p>Externalising this reduces the amount of work required if the assignment changes in future - e.g. from
     * sequential to round-robin
     *
     * @param backlog               backlog items to distribute
     * @param staff                 staff members to distribute to
     * @param staffMemberCapacities capacity limits for each staff member
     * @return distributed workload
     */
    private Map<User, Set<DigitalResponse>> distributeWorkload(Collection<DigitalResponse> backlog, List<User> staff,
                                                               Map<String, Integer> staffMemberCapacities) {
        final Iterator<DigitalResponse> backlogItems = backlog.iterator();

        Map<User, Set<DigitalResponse>> allocation = new HashMap<>();

        for (final User staffMember : staff) {
            for (int j = 0; j < staffMemberCapacities.get(staffMember.getUsername()) && backlogItems.hasNext(); j++) {
                allocation.computeIfAbsent(staffMember, s -> new HashSet<>()).add(backlogItems.next());
            }
        }

        return allocation;
    }

    /**
     * Checks to ensure that we retrieved all the staff members the front-end expects us to use.
     *
     * @param staffLogins staff logins specified by front-end
     * @param staff       staff entities retrieved from database
     * @throws AutoAssignException if there are fewer entities than logins
     */
    private void checkForMissingStaff(List<String> staffLogins, List<User> staff) throws AutoAssignException {
        final Set<String> retrievedLogins = staff.stream().map(User::getUsername).collect(Collectors.toSet());
        final List<String> missing =
            staffLogins.stream().filter(s -> !retrievedLogins.contains(s)).toList();

        if (!missing.isEmpty()) {
            log.error("Illegal state: staff logins supplied by frontend not present in database: {}", missing);
            throw new AutoAssignException.MissingLogins(missing);
        }
    }

    /**
     * Checks that all staff members are valid candidates for auto-assignment (i.e. not inactive and not a team leader).
     */
    private void checkForInvalidStaff(Collection<User> staff) throws AutoAssignException {
        List<User> ineligible =
            staff.stream().filter(s -> !s.isActive() || !nullSafeEquals(s.getLevel(), SecurityUtil.STANDARD_USER_LEVEL))
                .toList();
        if (!ineligible.isEmpty()) {
            final List<String> ineligibleLogins = Lists.transform(ineligible, User::getUsername);
            log.error("Input included staff members not eligible for auto-allocation: {}", ineligibleLogins);
            throw new AutoAssignException.IneligibleStaff(ineligibleLogins);
        }
    }
}
