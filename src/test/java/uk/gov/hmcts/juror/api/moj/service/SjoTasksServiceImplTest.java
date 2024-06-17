package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.MockedStatic;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("SjoTasksServiceImpl")
class SjoTasksServiceImplTest {

    private JurorPoolRepository jurorPoolRepository;
    private JurorStatusRepository jurorStatusRepository;
    private JurorHistoryService jurorHistoryService;
    private JurorPoolService jurorPoolService;
    private SjoTasksServiceImpl sjoTasksService;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        this.jurorPoolRepository = mock(JurorPoolRepository.class);
        this.jurorStatusRepository = mock(JurorStatusRepository.class);
        this.jurorHistoryService = mock(JurorHistoryService.class);
        this.jurorPoolService = mock(JurorPoolService.class);
        this.sjoTasksService = new SjoTasksServiceImpl(jurorPoolRepository, jurorStatusRepository, jurorHistoryService);

        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Nested
    @DisplayName("GetFailedToAttendJurors")
    class GetFailedToAttendJurors {

        @Test
        void positiveTypicalPoolSearch() {
            SecurityContextHolder.getContext().setAuthentication(
                new BureauJwtAuthentication(List.of(),
                    TestUtils.createJwt("415", "COURT_USER", "0", List.of("MANAGER", "SENIOR_JUROR_OFFICER"))
                ));

            JurorDetailsDto response1 = mock(JurorDetailsDto.class);
            when(response1.getJurorNumber()).thenReturn("111111111");
            when(response1.getPoolNumber()).thenReturn("111111111");
            when(response1.getFirstName()).thenReturn("FNAME1");
            when(response1.getLastName()).thenReturn("LNAME1");
            when(response1.getPostCode()).thenReturn("POSTCODE1");

            JurorDetailsDto response2 = mock(JurorDetailsDto.class);
            when(response2.getJurorNumber()).thenReturn("111111112");
            when(response2.getPoolNumber()).thenReturn("111111112");
            when(response2.getFirstName()).thenReturn("FNAME2");
            when(response2.getLastName()).thenReturn("LNAME2");
            when(response2.getPostCode()).thenReturn("POSTCODE2");

            JurorDetailsDto response3 = mock(JurorDetailsDto.class);
            when(response3.getJurorNumber()).thenReturn("111111113");
            when(response3.getPoolNumber()).thenReturn("111111113");
            when(response3.getFirstName()).thenReturn("FNAME3");
            when(response3.getLastName()).thenReturn("LNAME3");
            when(response3.getPostCode()).thenReturn("POSTCODE3");

            JurorPoolSearch jurorPoolSearch = JurorPoolSearch.builder()
                .poolNumber("415")
                .jurorStatus(IJurorStatus.FAILED_TO_ATTEND)
                .pageLimit(5)
                .pageNumber(1)
                .build();

            // mock the response
            PaginatedList<JurorDetailsDto> result = new PaginatedList<>();
            result.setData(List.of(response1, response2, response3));

            doReturn(result).when(jurorPoolService).search(jurorPoolSearch);

            // get the mocked response and verify
            PaginatedList<JurorDetailsDto> jurors = jurorPoolService.search(jurorPoolSearch);

            assertThat(jurors).isNotNull();
            assertThat(jurors.getData()).hasSize(3);
            List<JurorDetailsDto> data = jurors.getData();

            JurorDetailsDto juror1 = data.get(0);
            assertThat(juror1.getJurorNumber()).isEqualTo("111111111");
            assertThat(juror1.getPoolNumber()).isEqualTo("111111111");
            assertThat(juror1.getFirstName()).isEqualTo("FNAME1");
            assertThat(juror1.getLastName()).isEqualTo("LNAME1");
            assertThat(juror1.getPostCode()).isEqualTo("POSTCODE1");

            JurorDetailsDto juror2 = data.get(1);
            assertThat(juror2.getJurorNumber()).isEqualTo("111111112");
            assertThat(juror2.getPoolNumber()).isEqualTo("111111112");
            assertThat(juror2.getFirstName()).isEqualTo("FNAME2");
            assertThat(juror2.getLastName()).isEqualTo("LNAME2");
            assertThat(juror2.getPostCode()).isEqualTo("POSTCODE2");

            JurorDetailsDto juror3 = data.get(2);
            assertThat(juror3.getJurorNumber()).isEqualTo("111111113");
            assertThat(juror3.getPoolNumber()).isEqualTo("111111113");
            assertThat(juror3.getFirstName()).isEqualTo("FNAME3");
            assertThat(juror3.getLastName()).isEqualTo("LNAME3");
            assertThat(juror3.getPostCode()).isEqualTo("POSTCODE3");

            verify(jurorPoolService, times(1)).search(jurorPoolSearch);

            verifyNoMoreInteractions(jurorStatusRepository, jurorHistoryService);
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void negativeNoFailedToAttendJurors(List<JurorDetailsDto> data) {
            SecurityContextHolder.getContext().setAuthentication(
                new BureauJwtAuthentication(List.of(),
                    TestUtils.createJwt("415", "COURT_USER", "0", List.of("MANAGER", "SENIOR_JUROR_OFFICER"))
                ));

            JurorPoolSearch jurorPoolSearch = JurorPoolSearch.builder()
                .poolNumber("415")
                .jurorStatus(IJurorStatus.FAILED_TO_ATTEND)
                .pageLimit(5)
                .pageNumber(1)
                .build();

            // mock the response
            PaginatedList<JurorDetailsDto> result = new PaginatedList<>();

            result.setData(data);

            doReturn(result).when(jurorPoolService).search(jurorPoolSearch);

            // get the mocked response and verify
            MojException.NotFound exception = assertThrows(
                MojException.NotFound.class,
                () -> jurorPoolService.search(jurorPoolSearch)
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("No juror pools found that meet your search criteria.");

            verify(jurorPoolService, times(1)).search(jurorPoolSearch);
            verifyNoMoreInteractions(jurorStatusRepository, jurorHistoryService);
        }
    }

    @Nested
    @DisplayName("UndoFailedToAttendJurors")
    class UndoFailedToAttendJurors {

        @Test
        void positiveTypical() {
            JurorPool jurorPool = mock(JurorPool.class);
            when(jurorPool.getJurorNumber()).thenReturn("111111111");
            when(jurorPool.getPoolNumber()).thenReturn("111111111");

            JurorStatus jurorStatus = mock(JurorStatus.class);

            when(jurorPool.getStatus()).thenReturn(jurorStatus);
            when(jurorStatus.getStatus()).thenReturn(IJurorStatus.FAILED_TO_ATTEND);

            JurorStatus respondedStatus = mock(JurorStatus.class);

            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED)).thenReturn(Optional.of(respondedStatus));

            SecurityContextHolder.getContext().setAuthentication(
                new BureauJwtAuthentication(List.of(),
                    TestUtils.createJwt("415", "COURT_USER", "0", List.of("MANAGER", "SENIOR_JUROR_OFFICER"))
                ));

            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn("415");

            when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                "111111111", true, SecurityUtil.getActiveOwner()
            )).thenReturn(jurorPool);

            sjoTasksService.undoFailedToAttendStatus("111111111", "111111111");

            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.RESPONDED);

            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActiveAndOwner(
                "111111111", true, SecurityUtil.getActiveOwner()
            );

            verify(jurorPool, times(1)).setStatus(respondedStatus);
            verify(jurorHistoryService, times(1)).createUndoFailedToAttendHistory(jurorPool);
            verify(jurorPoolRepository, times(1)).save(jurorPool);

            verifyNoMoreInteractions(jurorPoolRepository, jurorStatusRepository, jurorHistoryService);
        }

        @Test
        void positiveMultiple() {
            JurorPool jurorPool1 = mock(JurorPool.class);
            when(jurorPool1.getJurorNumber()).thenReturn("111111111");
            when(jurorPool1.getPoolNumber()).thenReturn("111111111");

            JurorPool jurorPool2 = mock(JurorPool.class);
            when(jurorPool2.getJurorNumber()).thenReturn("111111112");
            when(jurorPool2.getPoolNumber()).thenReturn("111111112");

            JurorPool jurorPool3 = mock(JurorPool.class);
            when(jurorPool3.getJurorNumber()).thenReturn("111111113");
            when(jurorPool3.getPoolNumber()).thenReturn("111111113");

            JurorStatus jurorStatus = mock(JurorStatus.class);

            when(jurorPool1.getStatus()).thenReturn(jurorStatus);
            when(jurorPool2.getStatus()).thenReturn(jurorStatus);
            when(jurorPool3.getStatus()).thenReturn(jurorStatus);
            when(jurorStatus.getStatus()).thenReturn(IJurorStatus.FAILED_TO_ATTEND);

            JurorStatus respondedStatus = mock(JurorStatus.class);

            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED)).thenReturn(Optional.of(respondedStatus));

            SecurityContextHolder.getContext().setAuthentication(
                new BureauJwtAuthentication(List.of(),
                    TestUtils.createJwt("415", "COURT_USER", "0", List.of("MANAGER", "SENIOR_JUROR_OFFICER"))
                ));

            when(SecurityUtil.getActiveOwner()).thenReturn("415");

            when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                "111111111", true, SecurityUtil.getActiveOwner()
            )).thenReturn(jurorPool1);

            when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                "111111112", true, SecurityUtil.getActiveOwner()
            )).thenReturn(jurorPool2);

            when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                "111111113", true, SecurityUtil.getActiveOwner()
            )).thenReturn(jurorPool3);

            sjoTasksService.undoFailedToAttendStatus("111111111", "111111111");
            sjoTasksService.undoFailedToAttendStatus("111111112", "111111112");
            sjoTasksService.undoFailedToAttendStatus("111111113", "111111113");

            verify(jurorStatusRepository, times(3)).findById(IJurorStatus.RESPONDED);

            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActiveAndOwner(
                "111111111", true, SecurityUtil.getActiveOwner()
            );
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActiveAndOwner(
                "111111112", true, SecurityUtil.getActiveOwner()
            );
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActiveAndOwner(
                "111111113", true, SecurityUtil.getActiveOwner()
            );

            verify(jurorPool1, times(1)).setStatus(respondedStatus);
            verify(jurorPool2, times(1)).setStatus(respondedStatus);
            verify(jurorPool3, times(1)).setStatus(respondedStatus);

            verify(jurorHistoryService, times(1)).createUndoFailedToAttendHistory(jurorPool1);
            verify(jurorHistoryService, times(1)).createUndoFailedToAttendHistory(jurorPool2);
            verify(jurorHistoryService, times(1)).createUndoFailedToAttendHistory(jurorPool3);

            verify(jurorPoolRepository, times(1)).save(jurorPool1);
            verify(jurorPoolRepository, times(1)).save(jurorPool2);
            verify(jurorPoolRepository, times(1)).save(jurorPool3);

            verifyNoMoreInteractions(jurorPoolRepository, jurorStatusRepository, jurorHistoryService);
        }

        @Test
        void negativeNoFailedToAttendJurors() {
            JurorStatus respondedStatus = mock(JurorStatus.class);
            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED)).thenReturn(Optional.of(respondedStatus));

            when(SecurityUtil.getActiveOwner()).thenReturn("415");

            when(jurorPoolRepository
                .findByJurorJurorNumberAndIsActiveAndOwner("111111111", true, SecurityUtil.getActiveOwner())
            ).thenReturn(null);

            MojException.NotFound exception = assertThrows(
                MojException.NotFound.class,
                () -> sjoTasksService.undoFailedToAttendStatus("111111111", "111111111")
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo("No Failed To Attend juror pool found for Juror number 111111111");

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveAndOwner("111111111", true, SecurityUtil.getActiveOwner());

            verifyNoMoreInteractions(jurorStatusRepository, jurorPoolRepository, jurorHistoryService);
        }

    }

}
