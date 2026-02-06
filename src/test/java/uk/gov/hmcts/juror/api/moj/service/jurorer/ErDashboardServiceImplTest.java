package uk.gov.hmcts.juror.api.moj.service.jurorer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.service.FileUploadsService;
import uk.gov.hmcts.juror.api.jurorer.service.LocalAuthorityService;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ErDashboardServiceImplTest {

    @Mock
    private LocalAuthorityService localAuthorityService;
    @Mock
    private DeadlineRepository deadlineRepository;
    @Mock
    private FileUploadsService fileUploadsService;

    @InjectMocks
    private ErDashboardServiceImpl erDashboardService;


    @BeforeEach
    void beforeEach() {
        this.localAuthorityService = mock(LocalAuthorityService.class);
        this.deadlineRepository = mock(DeadlineRepository.class);
        this.fileUploadsService = mock(FileUploadsService.class);

        this.erDashboardService = new ErDashboardServiceImpl(
            localAuthorityService, deadlineRepository, fileUploadsService);
    }

    @Test
    void erDashboardStatsNoDeadlineData() {

        when(deadlineRepository.getCurrentDeadline())
            .thenReturn(Optional.empty());

        MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
                                                 () -> erDashboardService.getErDashboardStats(),
               "Expected exception to be thrown when no deadline data is found");

        assertEquals("Upload deadline data not found - it should always exist", exception.getMessage(),
                     "Expected exception message to be Upload deadline data not found - it should always exist");

    }

    @Test
    void erDashboardStatsNoLaData() {

        Deadline deadline = Deadline.builder()
            .id(1)
            .deadlineDate(LocalDate.now().plusDays(30))
            .uploadStartDate(LocalDate.now().minusDays(30))
            .build();

        when(deadlineRepository.getCurrentDeadline())
            .thenReturn(Optional.ofNullable(deadline));

        when(localAuthorityService.getAllLocalAuthorities(true))
            .thenReturn(java.util.Collections.emptyList());

        ErDashboardStatsResponseDto responseDto = erDashboardService.getErDashboardStats();

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getDeadlineDate()).isNotNull();

        assertEquals(deadline.getDeadlineDate(), responseDto.getDeadlineDate(),
                     "Expected deadline date in response to match the deadline date from the repository");
        assertEquals(30, responseDto.getDaysRemaining(),
                     "Expected days remaining in response to be 30 when deadline is 30 days in the future");
        assertEquals(0, responseDto.getTotalNumberOfLocalAuthorities(),
                     "Expected total number of local authorities to be 0 when no local authority data is found");
        assertEquals(0, responseDto.getNotUploadedCount(),
                     "Expected not uploaded count to be 0 when no local authority data is found");
        assertEquals(0, responseDto.getUploadedCount(),
                     "Expected uploaded count to be 0 when no local authority data is found");

        verify(deadlineRepository).getCurrentDeadline();
        verify(localAuthorityService).getAllLocalAuthorities(true);

    }

    @Test
    void localAuthorityStatusNoData() {

        ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder()
            .build();

        when(fileUploadsService.getLatestUploadForEachLa())
            .thenReturn(List.of());

        when(localAuthorityService.getAllLocalAuthorities(true))
            .thenReturn(List.of());

        ErLocalAuthorityStatusResponseDto responseDto = erDashboardService.getLocalAuthorityStatus(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getLocalAuthorityStatuses()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseDto.getLocalAuthorityStatuses()).isEmpty();

        verify(fileUploadsService).getLatestUploadForEachLa();
        verify(localAuthorityService).getAllLocalAuthorities(true);
    }

    @Test
    void localAuthoritiesNoData() {

        when(localAuthorityService.getAllLocalAuthorities(false))
            .thenReturn(List.of());

        LocalAuthoritiesResponseDto responseDto = erDashboardService.getLocalAuthorities(false);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getLocalAuthorities()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseDto.getLocalAuthorities()).isEmpty();

        verify(localAuthorityService).getAllLocalAuthorities(false);
    }
}
