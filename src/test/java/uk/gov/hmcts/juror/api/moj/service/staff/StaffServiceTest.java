package uk.gov.hmcts.juror.api.moj.service.staff;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.StaffJurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.staff.StaffJurorResponseAuditRepositoryMod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class StaffServiceTest {
    @Mock
    private UserRepository mockuserRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod mockJurorResponseRepository;

    @Mock
    private JurorPaperResponseRepositoryMod paperResponseRepositoryMod;

    @Mock
    private StaffJurorResponseAuditRepositoryMod mockStaffJurorResponseAuditRepository;

    @Mock
    private JurorResponseCommonRepositoryMod jurorResponseCommonRepositoryMod;

    @Mock
    private EntityManager mockEntityManager;

    @InjectMocks
    private StaffServiceImpl staffService;

    private static final String JUROR_NUMBER = "123456789";

    private static final DigitalResponse JUROR_RESPONSE_INVALID_CLOSED = DigitalResponse.builder()
        .jurorNumber(JUROR_NUMBER)
        .replyType(ReplyType.builder().type("Digital").build())
        .processingComplete(true)
        .urgent(false)
        .superUrgent(false)
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

    private static final String ASSIGNING_LOGIN = "assigner";

    private static final User ASSIGNER_STAFF_ENTITY = User.builder()
        .username(ASSIGNING_LOGIN)
        .name("Bob")
        .roles(Set.of(Role.TEAM_LEADER))
        .active(true)
        .build();

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_happy(String replyType) {
        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }


        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        doReturn(TARGET_LOGIN_ENTITY).when(mockuserRepository).findByUsername(TARGET_LOGIN);

        final StaffAssignmentResponseDto responseDto = staffService.changeAssignment(DTO, ASSIGNING_LOGIN);
        assertThat(responseDto)
            .isNotNull()
            .extracting(StaffAssignmentResponseDto::getAssignedBy, StaffAssignmentResponseDto::getAssignedTo,
                StaffAssignmentResponseDto::getJurorResponse)
            .containsExactly(ASSIGNING_LOGIN, TARGET_LOGIN, JUROR_NUMBER);
        assertThat(responseDto.getAssignmentDate()).isToday();

        verify(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        verify(mockuserRepository).findByUsername(TARGET_LOGIN);
        verify(mockStaffJurorResponseAuditRepository).save(any(StaffJurorResponseAuditMod.class));
        if(replyType.equals("Digital")) {
            verify(mockEntityManager).detach(any());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_happy_nullStaffAssignment(String replyType) {
        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }

        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        doReturn(TARGET_LOGIN_ENTITY).when(mockuserRepository).findByUsername(TARGET_LOGIN);
        StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .assignTo(null)
            .responseJurorNumber(JUROR_NUMBER)
            .build();

        final StaffAssignmentResponseDto responseDto = staffService.changeAssignment(dto, ASSIGNING_LOGIN);
        assertThat(responseDto)
            .isNotNull()
            .extracting(StaffAssignmentResponseDto::getAssignedBy, StaffAssignmentResponseDto::getAssignedTo,
                StaffAssignmentResponseDto::getJurorResponse)
            .containsExactly(ASSIGNING_LOGIN, null, JUROR_NUMBER);
        assertThat(responseDto.getAssignmentDate()).isToday();

        verify(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        verify(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        verify(mockStaffJurorResponseAuditRepository).save(any(StaffJurorResponseAuditMod.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_nullStaffAssignmentNotTeamLeader(String replyType) {
        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }

        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        doReturn(TARGET_LOGIN_ENTITY).when(mockuserRepository).findByUsername(TARGET_LOGIN);
        StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .assignTo(null)
            .responseJurorNumber(JUROR_NUMBER)
            .build();

        User normalUser = User.builder()
            .username(ASSIGNING_LOGIN)
            .name("Bob")
            .active(true)
            .build();

        doReturn(normalUser).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        MojException.Forbidden exception = Assertions.assertThrows(MojException.Forbidden.class,
            () -> staffService.changeAssignment(dto,
            ASSIGNING_LOGIN));
        assertThat(exception.getMessage())
            .as("Exception message")
            .isEqualTo("Unable to assign response for Juror 123456789 to backlog as user assigner does not have "
                + "rights");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_throwsExceptionOnNoStaffRecord() {
        doReturn(null).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        Assertions.assertThrows(MojException.NotFound.class, () -> staffService.changeAssignment(DTO, ASSIGNING_LOGIN));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_throwsExceptionOnNoJurorResponse(String replyType) {
        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }

        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        Assertions.assertThrows(MojException.NotFound.class, () -> staffService.changeAssignment(DTO, ASSIGNING_LOGIN));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_throwsExceptionOnNoAssignmentTargetStaffRecord(String replyType) {
        given(mockuserRepository.findByUsername(anyString()))
            .willReturn(ASSIGNER_STAFF_ENTITY)  // 1st call
            .willReturn(null);

        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }

        given(jurorResponseCommonRepositoryMod.findByJurorNumber(JUROR_NUMBER)).willReturn(jurorResponse);
        Assertions.assertThrows(MojException.NotFound.class, () -> staffService.changeAssignment(DTO, ASSIGNING_LOGIN));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_throwsExceptionOnAutoUser(String replyType) {
        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }

        User auto = User.builder().username("AUTO").active(true).build();
        StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .assignTo("AUTO")
            .responseJurorNumber(JUROR_NUMBER)
            .build();

        doReturn(auto).when(mockuserRepository).findByUsername("AUTO");
        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        MojException.BusinessRuleViolation exception = Assertions.assertThrows(MojException.BusinessRuleViolation.class,
            () -> staffService.changeAssignment(dto,
            ASSIGNING_LOGIN));

        assertThat(exception.getMessage())
            .as("Exception message")
            .isEqualTo("Cannot change assignment to user AUTO");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_urgentJurorResponse(String replyType) {
        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(TARGET_LOGIN_ENTITY).when(mockuserRepository).findByUsername(TARGET_LOGIN);

        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(true)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(true)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }

        StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .assignTo(null)
            .responseJurorNumber(JUROR_NUMBER)
            .build();

        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        MojException.BusinessRuleViolation exception = Assertions.assertThrows(MojException.BusinessRuleViolation.class,
            () -> staffService.changeAssignment(dto,
            ASSIGNING_LOGIN));
        assertThat(exception.getMessage())
            .as("Exception Message")
            .isEqualTo("Unable to assign response for Juror 123456789 to backlog as it is urgent");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_superUrgentJurorResponse(String replyType) {
        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(TARGET_LOGIN_ENTITY).when(mockuserRepository).findByUsername(TARGET_LOGIN);

        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(true)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(true)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }

        StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .assignTo(null)
            .responseJurorNumber(JUROR_NUMBER)
            .build();

        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        MojException.BusinessRuleViolation exception = Assertions.assertThrows(MojException.BusinessRuleViolation.class,
            () -> staffService.changeAssignment(dto,
                ASSIGNING_LOGIN));
        assertThat(exception.getMessage())
            .as("Exception Message")
            .isEqualTo("Unable to assign response for Juror 123456789 to backlog as it is super-urgent");
    }

    @ParameterizedTest
    @MethodSource("generator")
    public void changeAssignment_unhappy_incorrectProcessingStatus(Map<String, ProcessingStatus> statusMap) {
        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(TARGET_LOGIN_ENTITY).when(mockuserRepository).findByUsername(TARGET_LOGIN);
        String replyType = statusMap.keySet().toArray(new String[1])[0];

        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(statusMap.get(replyType))
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(statusMap.get(replyType))
                .build();
        }

        StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .assignTo(null)
            .responseJurorNumber(JUROR_NUMBER)
            .build();

        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        MojException.BusinessRuleViolation exception = Assertions.assertThrows(MojException.BusinessRuleViolation.class,
            () -> staffService.changeAssignment(dto,
                ASSIGNING_LOGIN));
        assertThat(exception.getMessage())
            .as("Exception Message")
            .isEqualTo("Unable to assign response for Juror 123456789 to backlog as the processing status is " + statusMap.get(replyType));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_processingStatusComplete(String replyType) {
        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(TARGET_LOGIN_ENTITY).when(mockuserRepository).findByUsername(TARGET_LOGIN);

        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(true)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(true)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.TODO)
                .build();
        }


        StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .assignTo(null)
            .responseJurorNumber(JUROR_NUMBER)
            .build();

        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        MojException.BusinessRuleViolation exception = Assertions.assertThrows(MojException.BusinessRuleViolation.class,
            () -> staffService.changeAssignment(dto,
                ASSIGNING_LOGIN));
        assertThat(exception.getMessage())
            .as("Exception Message")
            .isEqualTo("Rejected assignment as the response is already closed: 123456789");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paper", "Digital"})
    public void changeAssignment_unhappy_processingStatusClosed(String replyType) {
        doReturn(ASSIGNER_STAFF_ENTITY).when(mockuserRepository).findByUsername(ASSIGNING_LOGIN);
        doReturn(TARGET_LOGIN_ENTITY).when(mockuserRepository).findByUsername(TARGET_LOGIN);

        AbstractJurorResponse jurorResponse;
        if (replyType.equals("Digital")) {
            jurorResponse = DigitalResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.CLOSED)
                .build();
        } else {
            jurorResponse = PaperResponse.builder()
                .jurorNumber(JUROR_NUMBER)
                .replyType(ReplyType.builder().type(replyType).build())
                .processingComplete(false)
                .urgent(false)
                .superUrgent(false)
                .processingStatus(ProcessingStatus.CLOSED)
                .build();
        }

        StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .assignTo(null)
            .responseJurorNumber(JUROR_NUMBER)
            .build();

        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(JUROR_NUMBER);
        MojException.BusinessRuleViolation exception = Assertions.assertThrows(MojException.BusinessRuleViolation.class,
            () -> staffService.changeAssignment(dto,
                ASSIGNING_LOGIN));
        assertThat(exception.getMessage())
            .as("Exception Message")
            .isEqualTo("Rejected assignment as the response is already closed: 123456789");
    }

    private static Stream<Map<String, ProcessingStatus>> generator() {
        return Stream.of(
            Map.of("Paper", ProcessingStatus.AWAITING_CONTACT),
            Map.of("Paper", ProcessingStatus.AWAITING_TRANSLATION),
            Map.of("Paper", ProcessingStatus.AWAITING_COURT_REPLY),
            Map.of("Digital", ProcessingStatus.AWAITING_CONTACT),
            Map.of("Digital", ProcessingStatus.AWAITING_TRANSLATION),
            Map.of("Digital", ProcessingStatus.AWAITING_COURT_REPLY)
        );
    }
}

