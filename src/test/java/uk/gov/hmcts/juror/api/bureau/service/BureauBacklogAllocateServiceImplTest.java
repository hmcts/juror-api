package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauBacklogAllocateRequestDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauBacklogAllocateException;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.UserJurorResponseAuditRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link BureauBacklogAllocateServiceImpl}.
 */
class BureauBacklogAllocateServiceImplTest {

    private static final Comparator<DigitalResponse> ASCENDING_DATE_ORDER =
        Comparator.comparing(DigitalResponse::getDateReceived);
    private static final Integer NON_URGENT_TO_ALLOCATE_TO_STAFF = 1;
    private static final Integer URGENT_TO_ALLOCATE_TO_STAFF = 2;

    private JurorDigitalResponseRepositoryMod responseRepo;

    private BureauBacklogAllocateServiceImpl bureauBacklogAllocateService;

    @BeforeEach
    public void setUp() {

        responseRepo = Mockito.mock(JurorDigitalResponseRepositoryMod.class);
        UserRepository userRepo = Mockito.mock(UserRepository.class);
        UserJurorResponseAuditRepository auditRepo = Mockito.mock(UserJurorResponseAuditRepository.class);

        this.bureauBacklogAllocateService = spy(new BureauBacklogAllocateServiceImpl(
            responseRepo, userRepo, auditRepo));

        User user1 = User.builder().name("Post Staff 1").username("staff1").active(true).build();
        User user2 = User.builder().name("Post Staff 2").username("staff2").active(true).build();
        User user3 = User.builder().name("Post Staff 3").username("staff3").active(true).build();

        doReturn(Arrays.asList(user1, user2, user3)).when(userRepo).findAllByUsernameIn(anyList());

        final LocalDateTime now = LocalDateTime.now();
        List<DigitalResponse> toBeAllocated = Lists.newLinkedList();

        //nonurgent backlog
        List<DigitalResponse> backlog = generateResponses(NON_URGENT_TO_ALLOCATE_TO_STAFF, false, now);
        Page<DigitalResponse> nonUrgentResponses = new PageImpl<>(backlog);

        //urgent backlog
        List<DigitalResponse> urgentBacklog = generateResponses(URGENT_TO_ALLOCATE_TO_STAFF, true, now);
        Page<DigitalResponse> urgentResponses = new PageImpl<>(urgentBacklog);


        // If the service class doesn't use the correct parameters (for whatever reason) the tests will fall over
        // because the mock repo will return nothing


        doReturn(nonUrgentResponses).when(responseRepo).findAll(JurorResponseQueries.byUnassignedTodoNonUrgent()
                .and(JurorResponseQueries.jurorIsNotTransferred()),
            PageRequest.of(0, NON_URGENT_TO_ALLOCATE_TO_STAFF, Sort.Direction.ASC, "dateReceived"));

        doReturn(urgentResponses).when(responseRepo).findAll(JurorResponseQueries.byUnassignedTodoUrgent()
                .and(JurorResponseQueries.jurorIsNotTransferred()),
            PageRequest.of(0, URGENT_TO_ALLOCATE_TO_STAFF, Sort.Direction.ASC, "dateReceived"));


        toBeAllocated.addAll(backlog);
        toBeAllocated.addAll(urgentBacklog);
    }

    /**
     * Allocates responses to staff members - happy Path.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void allocateRepliesHappyPath(boolean urgentFlag) {
        List<DigitalResponse> toBeAllocated = generateResponses(1, urgentFlag, LocalDateTime.now());

        // convert the list to a page
        Page<DigitalResponse> page = new PageImpl<>(toBeAllocated);

        doReturn(page).when(responseRepo).findAll(any(Predicate.class), any(PageRequest.class));

        bureauBacklogAllocateService.allocateBacklogReplies(BureauBacklogAllocateRequestDto.builder()
            .officerAllocations(Arrays.asList(
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(1).urgentCount(1)
                    .userId("staff1").build(),
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(1).urgentCount(1)
                    .userId("staff2").build(),
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(1).urgentCount(1)
                    .userId("staff3").build()
            )).build(), "loggedInUser");
        final LocalDateTime currentTime = LocalDateTime.now();

        // Difference will likely be < 1s, but allowing a margin of error for running on a _really_ slow
        // machine/build slave
        final LocalDate minusFiveSeconds =
            LocalDate.from(currentTime.minusSeconds(5));
        final LocalDate plusFiveSeconds =
            LocalDate.from(currentTime.plusSeconds(5));

        final ArgumentCaptor<List<DigitalResponse>> listCaptor
            = ArgumentCaptor.forClass((Class) List.class);

        verify(responseRepo, times(3)).saveAll(listCaptor.capture());
        listCaptor.getValue().forEach(response -> {
            assertThat(response.getStaffAssignmentDate())
                .describedAs("Assignment date should be set to the time the auto-assign ran at")
                .isNotNull()
                .isBetween(minusFiveSeconds, plusFiveSeconds);
            assertThat(response.getStaff().getName())
                .describedAs("Backlog item should be assigned to a bureau officer")
                .containsAnyOf("Post Staff 1", "Post Staff 2", "Post Staff 3");
        });
    }


    /**
     * Tests that a BureauBacklogAllocateException is thrown if the requesting user is not provided.
     *
     * @throws Exception if the test falls over
     */
    @Test
    void allocateRepliesErrorPathRequestingUserIsRequired() throws Exception {

        Assertions.assertThatExceptionOfType(BureauBacklogAllocateException.RequestingUserIsRequired.class)
            .isThrownBy(() ->
            bureauBacklogAllocateService.allocateBacklogReplies(BureauBacklogAllocateRequestDto.builder()
                                                                    .officerAllocations(Arrays.asList(
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .userId("staff1").build(),
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .userId("staff2").build(),
                BureauBacklogAllocateRequestDto.StaffAllocation.builder()
                    .nonUrgentCount(NON_URGENT_TO_ALLOCATE_TO_STAFF).urgentCount(URGENT_TO_ALLOCATE_TO_STAFF)
                    .userId("staff3").build()
            )).build(), null));



    }

    /**
     * Called by setup to generate the Juror Response Test/Retrieved data for each urgency category.
     *
     * @param responseCount No of responses to be allocated - requested.
     * @param urgent        urgent response boolean.
     * @param now           datetime stamp.
     *
     * @return List JurorResponses
     */
    private List<DigitalResponse> generateResponses(int responseCount, Boolean urgent,
                                                    LocalDateTime now) {

        List<DigitalResponse> responses = Lists.newLinkedList();
        for (int i = 0;
             i < responseCount;
             i++) {
            DigitalResponse response = new DigitalResponse();
            response.setJurorNumber(String.valueOf(i));
            response.setUrgent(urgent);
            response.setDateReceived(now.minusHours(i));
            responses.add(response);
        }
        // This is the order the repository will return entries in
        responses.sort(ASCENDING_DATE_ORDER);
        return responses;
    }
}
