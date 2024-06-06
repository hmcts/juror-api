package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.bureau.controller.request.MultipleStaffAssignmentDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.OperationFailureListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffRosterResponseDto;
import uk.gov.hmcts.juror.api.bureau.domain.TeamRepository;
import uk.gov.hmcts.juror.api.bureau.domain.UserQueries;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.UserJurorResponseAudit;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.UserJurorResponseAuditRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.owner;

/**
 * Test for {@link UserServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository mockuserRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod mockJurorResponseRepository;

    @Mock
    private UserJurorResponseAuditRepository mockUserJurorResponseAuditRepository;

    @Mock
    private JurorResponseCommonRepositoryMod jurorResponseCommonRepositoryMod;

    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;


    @Mock
    private EntityManager mockEntityManager;

    @Mock
    private BureauTransformsService bureauTransformsService;

    @Mock
    private PoolRepository mockPoolDetailsRepository;

    @Mock
    private TeamRepository mockTeamRepository;


    @InjectMocks
    private UserServiceImpl userService;

    private static final String ASSIGNING_LOGIN = "assigner";
    private static final User ASSIGNER_STAFF_ENTITY = User.builder()
        .username(ASSIGNING_LOGIN)
        .name("Bob")
        .userType(UserType.BUREAU)
        .roles(Set.of(Role.MANAGER))
        .active(true)
        .build();


    private static final String JUROR_NUMBER = "123456789";
    private static final String JUROR_NUMBER_2 = "987654321";
    private static final String JUROR_NUMBER_3 = "123456788";
    private static final DigitalResponse JUROR_RESPONSE = DigitalResponse.builder()
        .jurorNumber(JUROR_NUMBER)
        .processingComplete(false)
        .urgent(false)
        .replyType(ReplyType.builder().type(ReplyMethod.DIGITAL.getDescription()).build())
        .processingStatus(ProcessingStatus.TODO)
        .build();
    private static final DigitalResponse JUROR_RESPONSE_2 = DigitalResponse.builder()
        .jurorNumber(JUROR_NUMBER_2)
        .processingComplete(false)
        .urgent(false)
        .replyType(ReplyType.builder().type(ReplyMethod.DIGITAL.getDescription()).build())
        .processingStatus(ProcessingStatus.TODO)
        .build();
    private static final DigitalResponse JUROR_RESPONSE_INVALID_AWAITING_CONTACT = DigitalResponse.builder()
        .jurorNumber(JUROR_NUMBER_3)
        .processingComplete(false)
        .urgent(false)
        .processingStatus(ProcessingStatus.AWAITING_CONTACT)
        .build();
    private static final DigitalResponse JUROR_RESPONSE_INVALID_CLOSED = DigitalResponse.builder()
        .jurorNumber(JUROR_NUMBER)
        .processingComplete(true)
        .urgent(false)
        .processingStatus(ProcessingStatus.CLOSED)
        .build();

    private static final String TARGET_LOGIN = "assignee";
    private static final User TARGET_LOGIN_ENTITY = User.builder()
        .username(TARGET_LOGIN)
        .name("Sally")
        .active(true)
        .build();
    private static final StaffAssignmentRequestDto DTO = StaffAssignmentRequestDto.builder()
        .assignTo(TARGET_LOGIN)
        .responseJurorNumber(JUROR_NUMBER)
        .build();

    @AfterEach
    public void afterEach() {
        TestUtils.afterAll();
    }

    @Test
    public void changeAssignment_happy() {
        given(mockuserRepository.findByUsername(ASSIGNING_LOGIN)).willReturn(ASSIGNER_STAFF_ENTITY);
        given(jurorResponseCommonRepositoryMod.findByJurorNumber(JUROR_NUMBER)).willReturn(JUROR_RESPONSE);
        given(mockuserRepository.findByUsername(TARGET_LOGIN)).willReturn(TARGET_LOGIN_ENTITY);

        final StaffAssignmentResponseDto responseDto = userService.changeAssignment(DTO, ASSIGNING_LOGIN);
        assertThat(responseDto)
            .isNotNull()
            .extracting(StaffAssignmentResponseDto::getAssignedBy, StaffAssignmentResponseDto::getAssignedTo,
                StaffAssignmentResponseDto::getJurorResponse)
            .containsExactly(ASSIGNING_LOGIN, TARGET_LOGIN, JUROR_NUMBER)
        ;
        assertThat(responseDto.getAssignmentDate()).isToday();

        verify(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        verify(mockJurorResponseRepository).save(any(DigitalResponse.class));
        verify(mockuserRepository).findByUsername(TARGET_LOGIN);
        verify(mockUserJurorResponseAuditRepository).save(any(UserJurorResponseAudit.class));
        verify(mockEntityManager).detach(any());
    }

    @Test
    public void changeAssignment_happy_nullStaffAssignment() {
        given(mockuserRepository.findByUsername(ASSIGNING_LOGIN)).willReturn(ASSIGNER_STAFF_ENTITY);
        given(jurorResponseCommonRepositoryMod.findByJurorNumber(JUROR_NUMBER)).willReturn(JUROR_RESPONSE);
        given(mockuserRepository.findByUsername(TARGET_LOGIN)).willReturn(TARGET_LOGIN_ENTITY);

        final StaffAssignmentResponseDto responseDto = userService.changeAssignment(DTO, ASSIGNING_LOGIN);
        assertThat(responseDto)
            .isNotNull()
            .extracting(StaffAssignmentResponseDto::getAssignedBy, StaffAssignmentResponseDto::getAssignedTo,
                StaffAssignmentResponseDto::getJurorResponse)
            .containsExactly(ASSIGNING_LOGIN, TARGET_LOGIN, JUROR_NUMBER)
        ;
        assertThat(responseDto.getAssignmentDate()).isToday();

        verify(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        verify(mockUserJurorResponseAuditRepository).save(any(UserJurorResponseAudit.class));
    }

    @Test(expected = StaffAssignmentException.class)
    public void changeAssignment_unhappy_throwsExceptionOnNoStaffRecord() {
        given(mockuserRepository.findByUsername(ASSIGNING_LOGIN)).willReturn(null);

        userService.changeAssignment(DTO, ASSIGNING_LOGIN);
    }

    @Test(expected = StaffAssignmentException.class)
    public void changeAssignment_unhappy_throwsExceptionOnNoJurorResponse() {
        given(mockuserRepository.findByUsername(ASSIGNING_LOGIN)).willReturn(ASSIGNER_STAFF_ENTITY);
        given(jurorResponseCommonRepositoryMod.findByJurorNumber(JUROR_NUMBER)).willReturn(null);

        userService.changeAssignment(DTO, ASSIGNING_LOGIN);
    }

    @Test(expected = StaffAssignmentException.class)
    public void changeAssignment_unhappy_throwsExceptionOnNoAssignmentTargetStaffRecord() {
        given(mockuserRepository.findByUsername(anyString()))
            .willReturn(ASSIGNER_STAFF_ENTITY)  // 1st call
            .willReturn(null)             // 2nd call
        ;
        given(jurorResponseCommonRepositoryMod.findByJurorNumber(JUROR_NUMBER))
            .willReturn(JUROR_RESPONSE_INVALID_CLOSED);

        userService.changeAssignment(DTO, ASSIGNING_LOGIN);
    }

    @Test
    public void activeStaffRoster_happy_convertsToDtos() {
        TestUtils.mockSecurityUtil(
            BureauJwtPayload.builder()
                .owner("400")
                .build()
        );

        final List<User> staffList = new ArrayList<>(3);
        staffList.add(User.builder()
            .username("aaa")
            .name("Bob")
            .active(true)
            .build());
        staffList.add(User.builder()
            .username("bbb")
            .name("Charlie")
            .active(true)
            .build());
        staffList.add(User.builder()
            .username("ccc")
            .name("Ali")
            .active(true)
            .build());
        assertThat(staffList)
            .describedAs("Mock db query return is not ordered")
            .extracting("name").containsExactly("Bob", "Charlie", "Ali");


        given(mockuserRepository.findAll(UserQueries.active().and(owner("400")), UserQueries.sortNameAsc()))
            .willReturn(staffList);

        final List<StaffRosterResponseDto.StaffDto> staffDtoList = userService.activeStaffRoster().getData();
        assertThat(staffDtoList)
            .describedAs("Dto contains the correct names sorted alphabetically")
            .hasSize(3)
            .extracting("name")
            .containsExactly("Ali", "Bob", "Charlie");

        verify(mockuserRepository, times(1)).findAll(UserQueries.active().and(owner("400")), UserQueries.sortNameAsc());
    }

    @Test
    public void multipleChangeAssignment_happy() {
        given(mockuserRepository.findByUsername(anyString()))
            .willReturn(ASSIGNER_STAFF_ENTITY)
            .willReturn(TARGET_LOGIN_ENTITY)
            .willReturn(ASSIGNER_STAFF_ENTITY)
            .willReturn(TARGET_LOGIN_ENTITY)
        ;
        given(jurorResponseCommonRepositoryMod.findByJurorNumber(anyString()))
            .willReturn(JUROR_RESPONSE)
            .willReturn(JUROR_RESPONSE_2)
        ;

        final MultipleStaffAssignmentDto.ResponseMetadata assignment1 =
            MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber(JUROR_NUMBER)
                .version(0)
                .build();
        final MultipleStaffAssignmentDto.ResponseMetadata assignment2 =
            MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber(JUROR_NUMBER_2)
                .version(0)
                .build();

        final MultipleStaffAssignmentDto multipleStaffAssignmentDto = MultipleStaffAssignmentDto.builder()
            .assignTo(ASSIGNING_LOGIN)
            .responses(Arrays.asList(assignment1, assignment2))
            .build();

        userService.multipleChangeAssignment(multipleStaffAssignmentDto, ASSIGNING_LOGIN);

        verify(mockuserRepository, times(4)).findByUsername(anyString());
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER_2);
        verify(mockUserJurorResponseAuditRepository, times(2)).save(any(UserJurorResponseAudit.class));
        verify(mockJurorResponseRepository, times(2)).save(any(DigitalResponse.class));
        verify(mockUserJurorResponseAuditRepository, times(2)).save(any(UserJurorResponseAudit.class));
    }

    @Test
    public void multipleChangeAssignment_happy_nullStaffAssignment() {
        given(mockuserRepository.findByUsername(anyString())).willReturn(ASSIGNER_STAFF_ENTITY);
        given(jurorResponseCommonRepositoryMod.findByJurorNumber(anyString()))
            .willReturn(JUROR_RESPONSE)
            .willReturn(JUROR_RESPONSE_2)
        ;

        final MultipleStaffAssignmentDto.ResponseMetadata assignment1 =
            MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber(JUROR_NUMBER)
                .version(0)
                .build();
        final MultipleStaffAssignmentDto.ResponseMetadata assignment2 =
            MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber(JUROR_NUMBER_2)
                .version(0)
                .build();

        final MultipleStaffAssignmentDto multipleStaffAssignmentDto = MultipleStaffAssignmentDto.builder()
            .assignTo(null)
            .responses(Arrays.asList(assignment1, assignment2))
            .build();

        userService.multipleChangeAssignment(multipleStaffAssignmentDto, ASSIGNING_LOGIN);

        verify(mockuserRepository, times(2)).findByUsername(anyString());//Only gets called for the assigning staff
        // members record!
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER_2);
        verify(mockUserJurorResponseAuditRepository, times(2)).save(any(UserJurorResponseAudit.class));
        verify(mockJurorResponseRepository, times(2)).save(any(DigitalResponse.class));
        verify(mockUserJurorResponseAuditRepository, times(2)).save(any(UserJurorResponseAudit.class));
    }

    @Test
    public void multipleChangeAssignment_unhappy_nullStaffAssignment_badStatus() {
        given(mockuserRepository.findByUsername(anyString())).willReturn(ASSIGNER_STAFF_ENTITY);
        given(jurorResponseCommonRepositoryMod.findByJurorNumber(anyString()))
            .willReturn(JUROR_RESPONSE)
            .willReturn(JUROR_RESPONSE_2)
            .willReturn(JUROR_RESPONSE_INVALID_AWAITING_CONTACT);

        final MultipleStaffAssignmentDto.ResponseMetadata assignment1 =
            MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber(JUROR_NUMBER)
                .version(0)
                .build();
        final MultipleStaffAssignmentDto.ResponseMetadata assignment2 =
            MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber(JUROR_NUMBER_2)
                .version(0)
                .build();
        final MultipleStaffAssignmentDto.ResponseMetadata assignment3 =
            MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber(JUROR_NUMBER_3) // this should cause the process to fail
                .version(0)
                .build();

        final MultipleStaffAssignmentDto multipleStaffAssignmentDto = MultipleStaffAssignmentDto.builder()
            .assignTo(null)
            .responses(Arrays.asList(assignment1, assignment2, assignment3))
            .build();


        OperationFailureListDto failureList = userService.multipleChangeAssignment(multipleStaffAssignmentDto,
            ASSIGNING_LOGIN);
        assertThat(failureList.getFailureDtos().size()).isEqualTo(1);
        assertThat(failureList.getFailureDtos().get(0).getJurorNumber()).isEqualTo(JUROR_NUMBER_3);

        verify(mockuserRepository, times(3)).findByUsername(anyString());//Only gets called for the assigning staff
        // members record!
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER_2);
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER_3);

        // only two attempts to update should have occurred
        verify(mockUserJurorResponseAuditRepository, times(2)).save(any(UserJurorResponseAudit.class));
        verify(mockJurorResponseRepository, times(2)).save(any(DigitalResponse.class));
        verify(mockUserJurorResponseAuditRepository, times(2)).save(any(UserJurorResponseAudit.class));


    }

    @Test
    public void positiveFindByUsernameFound() {
        User user = mock(User.class);
        doReturn(user).when(mockuserRepository).findByUsername("USER321");
        assertThat(userService.findByUsername("USER321")).isEqualTo(user);
    }

    @Test
    public void negativeFindByUsernameNotFound() {
        doReturn(null).when(mockuserRepository).findByUsername("USER321");
        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
            () -> userService.findByUsername("USER321"),
            "Expected findByUsername to throw not found exception hen user is not found");
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo("User not found");


    }
}
