package uk.gov.hmcts.juror.api.moj.service.jurorer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.domain.UploadStatus;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.ReminderHistoryRepository;
import uk.gov.hmcts.juror.api.jurorer.service.FileUploadsService;
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;
import uk.gov.hmcts.juror.api.jurorer.service.LocalAuthorityService;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthorityInfoResponseDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ErDashboardServiceImplTest {

    @Mock
    private LocalAuthorityService localAuthorityService;
    @Mock
    private DeadlineRepository deadlineRepository;
    @Mock
    private FileUploadsService fileUploadsService;
    @Mock
    private LaUserService laUserService;
    @Mock
    private ReminderHistoryRepository reminderHistoryRepository;

    @InjectMocks
    private ErDashboardServiceImpl erDashboardService;


    @BeforeEach
    void beforeEach() {
        this.localAuthorityService = mock(LocalAuthorityService.class);
        this.deadlineRepository = mock(DeadlineRepository.class);
        this.fileUploadsService = mock(FileUploadsService.class);
        this.laUserService = mock(LaUserService.class);
        this.reminderHistoryRepository = mock(ReminderHistoryRepository.class);

        this.erDashboardService = new ErDashboardServiceImpl(
            localAuthorityService, deadlineRepository, fileUploadsService, laUserService,
            reminderHistoryRepository);
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
    void erDashboardStatsHappy() {

        Deadline deadline = Deadline.builder()
            .id(1)
            .deadlineDate(LocalDate.now().plusDays(20))
            .uploadStartDate(LocalDate.now().minusDays(10))
            .build();

        when(deadlineRepository.getCurrentDeadline())
            .thenReturn(Optional.ofNullable(deadline));

        List<LocalAuthority> localAuthorities = List.of(
            LocalAuthority.builder().laCode("LA1").laName("Local Authority 1").active(true)
                .uploadStatus(UploadStatus.UPLOADED).build(),
            LocalAuthority.builder().laCode("LA2").laName("Local Authority 2").active(true)
                .uploadStatus(UploadStatus.NOT_UPLOADED).build(),
            LocalAuthority.builder().laCode("LA3").laName("Local Authority 3").active(true)
                .uploadStatus(UploadStatus.UPLOADED).build()
        );

        when(localAuthorityService.getAllLocalAuthorities(true))
            .thenReturn(localAuthorities);

        ErDashboardStatsResponseDto responseDto = erDashboardService.getErDashboardStats();

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getDeadlineDate()).isNotNull();

        assertEquals(deadline.getDeadlineDate(), responseDto.getDeadlineDate(),
                     "Expected deadline date in response to match the deadline date from the repository");
        assertEquals(20, responseDto.getDaysRemaining(),
                     "Expected days remaining in response to be 20 when deadline is 20 days in the future");
        assertEquals(3, responseDto.getTotalNumberOfLocalAuthorities(),
                     "Expected total number of local authorities to be 3");
        assertEquals(1, responseDto.getNotUploadedCount(),
                     "Expected not uploaded count to be 1");
        assertEquals(2, responseDto.getUploadedCount(),
                     "Expected uploaded count to be 2");

        verify(deadlineRepository).getCurrentDeadline();
        verify(localAuthorityService).getAllLocalAuthorities(true);

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
    void localAuthorityStatusHappy() {

        ErLocalAuthorityStatusRequestDto requestDto = ErLocalAuthorityStatusRequestDto.builder()
            .build();

        List<FileUploadsService.FileUploadStatus> fileUploadStatuses = List.of(
            FileUploadsService.FileUploadStatus.builder()
                .localAuthorityCode("LA1")
                .laUsername("user1")
                .lastUploadDate(LocalDate.now().minusDays(5).atStartOfDay())
                .build(),
            FileUploadsService.FileUploadStatus.builder()
                .localAuthorityCode("LA3")
                .laUsername("user3")
                .lastUploadDate(LocalDate.now().minusDays(3).atStartOfDay())
                .build()
        );

        when(fileUploadsService.getLatestUploadForEachLa())
            .thenReturn(fileUploadStatuses);

        List<LocalAuthority> localAuthorities = List.of(
            LocalAuthority.builder().laCode("LA1").laName("Local Authority 1").active(true)
                .uploadStatus(UploadStatus.UPLOADED).build(),
            LocalAuthority.builder().laCode("LA2").laName("Local Authority 2").active(true)
                .uploadStatus(UploadStatus.NOT_UPLOADED).build(),
            LocalAuthority.builder().laCode("LA3").laName("Local Authority 3").active(true)
                .uploadStatus(UploadStatus.UPLOADED).build()
        );

        when(localAuthorityService.getAllLocalAuthorities(true))
            .thenReturn(localAuthorities);

        ErLocalAuthorityStatusResponseDto responseDto = erDashboardService.getLocalAuthorityStatus(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getLocalAuthorityStatuses()).isNotNull();

        assertEquals(3, responseDto.getLocalAuthorityStatuses().size(),
                     "Expected local authority statuses list size to be 3");

        ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus laStatus =
                                                                responseDto.getLocalAuthorityStatuses().get(0);
        assertEquals("LA1", laStatus.getLocalAuthorityCode(),
                        "Expected first local authority code in response to be LA1");
        assertEquals("Local Authority 1", laStatus.getLocalAuthorityName(),
                        "Expected first local authority name in response to be Local Authority 1");
        assertEquals(UploadStatus.UPLOADED, laStatus.getUploadStatus(),
                        "Expected first local authority upload status in response to be UPLOADED");
        assertEquals(fileUploadStatuses.get(0).getLastUploadDate(), laStatus.getLastUploadDate(),
                        "Expected first local authority last upload date in response to match the last upload"
                            + " date from the file upload status");

        laStatus = responseDto.getLocalAuthorityStatuses().get(1);
        assertEquals("LA2", laStatus.getLocalAuthorityCode(),
                     "Expected second local authority code in response to be LA2");
        assertEquals("Local Authority 2", laStatus.getLocalAuthorityName(),
                     "Expected second local authority name in response to be Local Authority 2");
        assertEquals(UploadStatus.NOT_UPLOADED, laStatus.getUploadStatus(),
                     "Expected second local authority upload status in response to be NOT_UPLOADED");
        assertThat(laStatus.getLastUploadDate()).isNull();

        laStatus = responseDto.getLocalAuthorityStatuses().get(2);
        assertEquals("LA3", laStatus.getLocalAuthorityCode(),
                     "Expected first local authority code in response to be LA3");
        assertEquals("Local Authority 3", laStatus.getLocalAuthorityName(),
                     "Expected first local authority name in response to be Local Authority 3");
        assertEquals(UploadStatus.UPLOADED, laStatus.getUploadStatus(),
                     "Expected first local authority upload status in response to be UPLOADED");
        assertEquals(fileUploadStatuses.get(1).getLastUploadDate(), laStatus.getLastUploadDate(),
                     "Expected first local authority last upload date in response to match the last upload date"
                         + " from the file upload status");

        verify(fileUploadsService).getLatestUploadForEachLa();
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
    void localAuthoritiesHappy() {

        List<LocalAuthority> localAuthorities = List.of(
            LocalAuthority.builder().laCode("LA1").laName("Local Authority 1").active(true)
                .uploadStatus(UploadStatus.UPLOADED).build(),
            LocalAuthority.builder().laCode("LA2").laName("Local Authority 2").active(true)
                .uploadStatus(UploadStatus.NOT_UPLOADED).build(),
            LocalAuthority.builder().laCode("LA3").laName("Local Authority 3").active(true)
                .uploadStatus(UploadStatus.UPLOADED).build()
        );

        when(localAuthorityService.getAllLocalAuthorities(false))
            .thenReturn(localAuthorities);

        LocalAuthoritiesResponseDto responseDto = erDashboardService.getLocalAuthorities(false);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getLocalAuthorities()).isNotNull();

        assertEquals(3, responseDto.getLocalAuthorities().size(),
                     "Expected local authorities list size to be 3");
        LocalAuthoritiesResponseDto.LocalAuthorityData la = responseDto.getLocalAuthorities().get(0);
        assertEquals("LA1", la.getLocalAuthorityCode(),
                     "Expected first local authority code in response to be LA1");
        assertEquals("Local Authority 1", la.getLocalAuthorityName(),
                     "Expected first local authority name in response to be Local Authority 1");

        la = responseDto.getLocalAuthorities().get(1);
        assertEquals("LA2", la.getLocalAuthorityCode(),
                     "Expected second local authority code in response to be LA2");
        assertEquals("Local Authority 2", la.getLocalAuthorityName(),
                     "Expected second local authority name in response to be Local Authority 2");

        la = responseDto.getLocalAuthorities().get(2);
        assertEquals("LA3", la.getLocalAuthorityCode(),
                     "Expected third local authority code in response to be LA3");
        assertEquals("Local Authority 3", la.getLocalAuthorityName(),
                     "Expected third local authority name in response to be Local Authority 3");

        verify(localAuthorityService).getAllLocalAuthorities(false);
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

    @Test
    void getLocalAuthorityInfoHappy() {

        String laCode = "123";

        LocalAuthority localAuthority = LocalAuthority.builder()
            .laCode(laCode)
            .laName("Local Authority 123")
            .active(true)
            .uploadStatus(UploadStatus.UPLOADED)
            .build();

        when(localAuthorityService.getLocalAuthorityByCode(laCode))
            .thenReturn(localAuthority);

        LocalAuthorityInfoResponseDto responseDto = erDashboardService.getLocalAuthorityInfo(laCode);

        assertThat(responseDto).isNotNull();
        assertEquals(laCode, responseDto.getLocalAuthorityCode(),
                     "Expected local authority code in response to match the LA code from the repository");

        verify(localAuthorityService).getLocalAuthorityByCode(laCode);

    }


    @Test
    void getLocalAuthorityInfoInvalidLaCode() {

        String laCode = "A23";

        MojException.BadRequest exception = assertThrows(MojException.BadRequest.class,
                          () -> erDashboardService.getLocalAuthorityInfo(laCode),
                          "Expected exception to be thrown when invalid LA code format is provided");

        assertEquals("Invalid LA code format: A23", exception.getMessage(),
                     "Expected exception message to be Invalid LA code format: A23");

        verifyNoInteractions(localAuthorityService);

    }

    @Test
    void getLocalAuthorityInfoNoLaData() {
        String laCode = "123";

        when(localAuthorityService.getLocalAuthorityByCode(laCode))
            .thenReturn(null);

        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                                                 () -> erDashboardService.getLocalAuthorityInfo(laCode),
                                                 "Expected exception to be thrown when LA not found");

        assertEquals("Local authority not found for code: 123", exception.getMessage(),
                     "Expected exception message to be Local authority not found for code: 123");

        verify(localAuthorityService).getLocalAuthorityByCode(laCode);

    }

}
