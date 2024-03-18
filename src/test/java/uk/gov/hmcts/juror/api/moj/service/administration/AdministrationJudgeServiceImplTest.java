package uk.gov.hmcts.juror.api.moj.service.administration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeCreateDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeUpdateDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.trial.JudgeRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("AdministrationJudgeServiceImpl")
class AdministrationJudgeServiceImplTest {

    private AdministrationJudgeServiceImpl administrationJudgeService;
    private JudgeRepository judgeRepository;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        this.judgeRepository = mock(JudgeRepository.class);
        this.administrationJudgeService = spy(new AdministrationJudgeServiceImpl(judgeRepository));
    }

    @AfterEach
    void afterEach() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
    }

    @Nested
    @DisplayName("Judge getJudge(long judgeId)")
    class GetJudge {
        @Test
        void positiveJudgeFound() {
            Judge judge = Judge.builder().id(321L).owner(TestConstants.VALID_COURT_LOCATION).build();
            doReturn(Optional.of(judge)).when(judgeRepository).findById(321L);

            assertThat(administrationJudgeService.getJudge(321L))
                .isEqualTo(judge);
            verify(judgeRepository, times(1)).findById(321L);
            securityUtilMockedStatic.verify(
                () -> SecurityUtil.validateCanAccessOwner(TestConstants.VALID_COURT_LOCATION), times(1));
        }

        @Test
        void negativeJudgeNotFound() {
            doReturn(Optional.empty()).when(judgeRepository).findById(321L);
            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> administrationJudgeService.getJudge(321L),
                "Exception should be thrown when judge not found");
            Assertions.assertThat(exception).isNotNull();
            Assertions.assertThat(exception.getCause()).isNull();
            Assertions.assertThat(exception.getMessage()).isEqualTo("Judge not found");

            verify(judgeRepository, times(1)).findById(321L);
            securityUtilMockedStatic.verify(
                () -> SecurityUtil.validateCanAccessOwner(any()), never());
        }
    }

    @Nested
    @DisplayName("public JudgeDetailsDto viewJudge(long judgeId)")
    class ViewJudge {

        @Test
        void positiveTypical() {
            Judge judge = Judge.builder().id(321L).owner(TestConstants.VALID_COURT_LOCATION).build();

            doReturn(judge).when(administrationJudgeService).getJudge(321L);

            JudgeDetailsDto judgeDetailsDto = administrationJudgeService.viewJudge(321L);
            assertThat(judgeDetailsDto).isNotNull();
            assertThat(judgeDetailsDto.getJudgeId()).isEqualTo(321L);

            verify(administrationJudgeService, times(1)).getJudge(321L);
        }
    }

    @Nested
    @DisplayName("public void deleteJudge(Long judgeId)")
    class DeleteJudge {

        @Test
        void positiveTypical() {
            Judge judge = Judge.builder().id(321L).owner(TestConstants.VALID_COURT_LOCATION).build();

            doReturn(judge).when(administrationJudgeService).getJudge(321L);

            administrationJudgeService.deleteJudge(321L);

            verify(administrationJudgeService, times(1)).getJudge(321L);
            verify(judgeRepository, times(1)).delete(judge);
        }

        @Test
        void negativeCanNotDeleteUsedJudge() {
            Judge judge =
                Judge.builder().id(321L).lastUsed(LocalDateTime.now()).owner(TestConstants.VALID_COURT_LOCATION)
                    .build();

            doReturn(judge).when(administrationJudgeService).getJudge(321L);

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> administrationJudgeService.deleteJudge(321L),
                "Exception should be thrown when judge is used");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("Judge has been used and cannot be deleted");
            assertThat(exception.getErrorCode()).isEqualTo(
                MojException.BusinessRuleViolation.ErrorCode.CANNOT_DELETE_USED_JUDGE);

            verify(administrationJudgeService, times(1)).getJudge(321L);
            verifyNoInteractions(judgeRepository);
        }
    }

    @Nested
    @DisplayName("public void updateJudge(Long judgeId, JudgeUpdateDto judgeUpdateDto)")
    class UpdateJudge {

        @Test
        void positiveTypical() {
            Judge judge = mock(Judge.class);
            when(judge.getCode()).thenReturn("code");
            doReturn(judge).when(administrationJudgeService).getJudge(321L);
            JudgeUpdateDto judgeUpdateDto = JudgeUpdateDto.builder()
                .judgeCode("code")
                .judgeName("name")
                .isActive(true)
                .build();
            administrationJudgeService.updateJudge(321L, judgeUpdateDto);

            verify(administrationJudgeService, times(1)).getJudge(321L);
            verify(judgeRepository, times(1)).save(judge);
            verify(administrationJudgeService, never()).verifyCodeDoesNotExist(any(),any());
            verify(judge, times(1)).setCode("code");
            verify(judge, times(1)).setName("name");
            verify(judge, times(1)).setActive(true);
            verify(judge, times(1)).getCode();
            verifyNoMoreInteractions(judge);
        }

        @Test
        void positiveCodeChanged() {
            Judge judge = mock(Judge.class);
            when(judge.getCode()).thenReturn("newCode");
            doNothing().when(administrationJudgeService).verifyCodeDoesNotExist(any(), any());
            doReturn(judge).when(administrationJudgeService).getJudge(321L);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(judge).getOwner();
            JudgeUpdateDto judgeUpdateDto = JudgeUpdateDto.builder()
                .judgeCode("code")
                .judgeName("name")
                .isActive(true)
                .build();
            administrationJudgeService.updateJudge(321L, judgeUpdateDto);

            verify(administrationJudgeService, times(1)).getJudge(321L);
            verify(judgeRepository, times(1)).save(judge);
            verify(administrationJudgeService, times(1)).verifyCodeDoesNotExist(TestConstants.VALID_COURT_LOCATION,
                "code");
            verify(judge, times(1)).setCode("code");
            verify(judge, times(1)).setName("name");
            verify(judge, times(1)).setActive(true);
            verify(judge, times(1)).getOwner();
            verify(judge, times(1)).getCode();
            verifyNoMoreInteractions(judge);
        }
    }

    @Nested
    @DisplayName("void verifyCodeDoesNotExist(String owner, String judgeCode)")
    class VerifyCodeDoesNotExist {
        @Test
        void positiveTypical() {
            doReturn(Optional.empty()).when(judgeRepository).findByOwnerAndCode(any(), any());
            administrationJudgeService.verifyCodeDoesNotExist("415", "CD1");
            verify(judgeRepository, times(1))
                .findByOwnerAndCode(TestConstants.VALID_COURT_LOCATION, "CD1");
        }

        @Test
        void negativeJudgeCodeAlreadyInUse() {
            Judge judge = mock(Judge.class);

            doReturn(Optional.of(judge)).when(judgeRepository)
                .findByOwnerAndCode(any(), any());
            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> administrationJudgeService.verifyCodeDoesNotExist("415", "CD1"),
                "Exception should be thrown when judge code already in use");
            Assertions.assertThat(exception).isNotNull();
            Assertions.assertThat(exception.getCause()).isNull();
            Assertions.assertThat(exception.getMessage()).isEqualTo("Judge with this code already exists");
            Assertions.assertThat(exception.getErrorCode())
                .isEqualTo(MojException.BusinessRuleViolation.ErrorCode.CODE_ALREADY_IN_USE);

            verify(judgeRepository, times(1))
                .findByOwnerAndCode(TestConstants.VALID_COURT_LOCATION, "CD1");
            verifyNoMoreInteractions(judgeRepository);
        }
    }

    @Nested
    @DisplayName("public List<JudgeDetailsDto> viewAllJudges(Boolean isActive)")
    class ViewAllJudges {
        @Test
        void positiveJudgesFound() {
            Judge judge1 = Judge.builder().id(1L).build();
            Judge judge2 = Judge.builder().id(2L).build();
            Judge judge3 = Judge.builder().id(3L).build();
            doReturn(List.of(judge1, judge2, judge3)).when(administrationJudgeService).getJudges(true);
            assertThat(administrationJudgeService.viewAllJudges(true))
                .containsExactlyInAnyOrder(new JudgeDetailsDto(judge1), new JudgeDetailsDto(judge2),
                    new JudgeDetailsDto(judge3));
            verify(administrationJudgeService, times(1)).getJudges(true);
        }

        @Test
        void positiveJudgesNotFound() {
            doReturn(List.of()).when(administrationJudgeService).getJudges(false);
            assertThat(administrationJudgeService.viewAllJudges(false)).isEmpty();
            verify(administrationJudgeService, times(1)).getJudges(false);
        }
    }

    @Nested
    @DisplayName("List<Judge> getJudges(Boolean isActive)")
    class GetJudges {

        @BeforeEach
        void beforeEach() {
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        }

        @Test
        void positiveNullIsActive() {
            List<Judge> response = List.of(mock(Judge.class), mock(Judge.class), mock(Judge.class));
            doReturn(response).when(judgeRepository).findByOwner(any());
            assertThat(administrationJudgeService.getJudges(null)).isEqualTo(response);
            verify(judgeRepository, times(1)).findByOwner(TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(judgeRepository);
        }

        @Test
        void positiveTrueIsActive() {
            List<Judge> response = List.of(mock(Judge.class), mock(Judge.class), mock(Judge.class));
            doReturn(response).when(judgeRepository).findByOwnerAndIsActive(any(), anyBoolean());
            assertThat(administrationJudgeService.getJudges(true)).isEqualTo(response);
            verify(judgeRepository, times(1)).findByOwnerAndIsActive(TestConstants.VALID_COURT_LOCATION, true);
            verifyNoMoreInteractions(judgeRepository);
        }

        @Test
        void positiveFalseIsActive() {
            List<Judge> response = List.of(mock(Judge.class), mock(Judge.class), mock(Judge.class));
            doReturn(response).when(judgeRepository).findByOwnerAndIsActive(any(), anyBoolean());
            assertThat(administrationJudgeService.getJudges(false)).isEqualTo(response);
            verify(judgeRepository, times(1)).findByOwnerAndIsActive(TestConstants.VALID_COURT_LOCATION, false);
            verifyNoMoreInteractions(judgeRepository);
        }
    }

    @Nested
    @DisplayName("public void createJudge(JudgeCreateDto judgeCreateDto)")
    class CreateJudge {
        @Test
        void positiveTypical() {
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

            doNothing().when(administrationJudgeService).verifyCodeDoesNotExist(any(), any());
            JudgeCreateDto judgeCreateDto = JudgeCreateDto.builder()
                .judgeCode("code")
                .judgeName("name")
                .build();
            administrationJudgeService.createJudge(judgeCreateDto);

            ArgumentCaptor<Judge> judgeArgumentCaptor = ArgumentCaptor.forClass(Judge.class);
            verify(judgeRepository, times(1)).save(judgeArgumentCaptor.capture());
            verify(administrationJudgeService, times(1))
                .verifyCodeDoesNotExist(TestConstants.VALID_COURT_LOCATION, "code");

            Judge judge = judgeArgumentCaptor.getValue();
            assertThat(judge).isNotNull();
            assertThat(judge.getOwner()).isEqualTo(TestConstants.VALID_COURT_LOCATION);
            assertThat(judge.getCode()).isEqualTo("code");
            assertThat(judge.getName()).isEqualTo("name");
            assertThat(judge.isActive()).isTrue();
            securityUtilMockedStatic.verify(SecurityUtil::getActiveOwner, times(1));
        }
    }

}
