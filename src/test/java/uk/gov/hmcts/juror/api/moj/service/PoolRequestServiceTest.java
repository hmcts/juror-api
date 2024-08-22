package uk.gov.hmcts.juror.api.moj.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.DayType;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.PoolRequestException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.IActivePoolsRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolTypeRepository;
import uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
public class PoolRequestServiceTest {

    @Mock
    private PoolRequestRepository poolRequestRepository;
    @Mock
    private CourtLocationRepository courtLocationRepository;
    @Mock
    private PoolTypeRepository poolTypeRepository;
    @Mock
    private HolidaysRepository holidaysRepository;
    @Mock
    private ManageDeferralsService manageDeferralsService;
    @Mock
    private PoolHistoryRepository poolHistoryRepository;
    @Mock
    private PoolStatisticsService poolStatisticsService;
    @Mock
    private IActivePoolsRepository activePoolsCourtRepository;

    @InjectMocks
    PoolRequestServiceImpl poolRequestService;

    @Test
    public void test_checkAttendanceDate_monday_noHoliday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 3);
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("No Holiday record exists for a weekday - expect business day")
            .isEqualTo(DayType.BUSINESS_DAY);
    }

    @Test
    public void test_checkAttendanceDate_tuesday_noHoliday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 4);
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("No Holiday record exists for a weekday - expect business day")
            .isEqualTo(DayType.BUSINESS_DAY);
    }

    @Test
    public void test_checkAttendanceDate_wednesday_noHoliday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 5);
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("No Holiday record exists for a weekday - expect business day")
            .isEqualTo(DayType.BUSINESS_DAY);
    }

    @Test
    public void test_checkAttendanceDate_thursday_noHoliday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 6);
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("No Holiday record exists for a weekday - expect business day")
            .isEqualTo(DayType.BUSINESS_DAY);
    }

    @Test
    public void test_checkAttendanceDate_friday_noHoliday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 7);
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("No Holiday record exists for a weekday - expect business day")
            .isEqualTo(DayType.BUSINESS_DAY);
    }

    @Test
    public void test_checkAttendanceDate_saturday_noHoliday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 8);
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("No Holiday record exists for a weekend - expect weekend")
            .isEqualTo(DayType.WEEKEND);
    }

    @Test
    public void test_checkAttendanceDate_sunday_noHoliday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 9);
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("No Holiday record exists for a weekend - expect weekend")
            .isEqualTo(DayType.WEEKEND);
    }

    @Test
    public void test_checkAttendanceDate_monday_holiday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 3);
        Holidays holidays = new Holidays();
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.of(holidays));
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("Holiday record exists for a weekday - expect holiday")
            .isEqualTo(DayType.HOLIDAY);
    }

    @Test
    public void test_checkAttendanceDate_tuesday_holiday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 4);
        Holidays holidays = new Holidays();
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.of(holidays));
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("Holiday record exists for a weekday - expect holiday")
            .isEqualTo(DayType.HOLIDAY);
    }

    @Test
    public void test_checkAttendanceDate_wednesday_holiday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 5);
        Holidays holidays = new Holidays();
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.of(holidays));
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("Holiday record exists for a weekday - expect holiday")
            .isEqualTo(DayType.HOLIDAY);
    }

    @Test
    public void test_checkAttendanceDate_thursday_holiday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 6);
        Holidays holidays = new Holidays();
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.of(holidays));
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("Holiday record exists for a weekday - expect holiday")
            .isEqualTo(DayType.HOLIDAY);
    }

    @Test
    public void test_checkAttendanceDate_friday_holiday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 7);
        Holidays holidays = new Holidays();
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.of(holidays));
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("Holiday record exists for a weekday - expect holiday")
            .isEqualTo(DayType.HOLIDAY);
    }

    @Test
    public void test_checkAttendanceDate_saturday_holiday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 8);
        Holidays holidays = new Holidays();
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.of(holidays));
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("Holiday record exists for a weekend - expect weekend")
            .isEqualTo(DayType.WEEKEND);
    }

    @Test
    public void test_checkAttendanceDate_sunday_holiday() {
        String locationCode = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 9);
        Holidays holidays = new Holidays();
        Mockito.when(holidaysRepository.findOne(Mockito.any())).thenReturn(Optional.of(holidays));
        Mockito.when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(new CourtLocation()));

        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);

        assertThat(dayType).as("Holiday record exists for a weekend - expect weekend")
            .isEqualTo(DayType.WEEKEND);
    }

    @Test
    public void test_savePoolRequest_newPoolFromBureau_validCourt() {
        final ArgumentCaptor<PoolRequest> poolRequestArgumentCaptor = ArgumentCaptor.forClass(PoolRequest.class);
        final PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        final CourtLocation courtLocation = new CourtLocation();
        final PoolType poolType = new PoolType("CRO", "CROWN COURT");
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setOwner("415");

        String poolId = "123456789";
        Mockito.when(poolRequestRepository.saveAndFlush(Mockito.any())).thenReturn(poolRequest);
        Mockito.when(poolRequestRepository.findById(poolId)).thenReturn(Optional.empty());
        Mockito.when(courtLocationRepository.findById("415")).thenReturn(Optional.of(courtLocation));
        Mockito.when(poolTypeRepository.findById("CRO")).thenReturn(Optional.of(poolType));

        poolRequestService.savePoolRequest(poolRequestDto, buildPayload("415"));

        Mockito.verify(poolRequestRepository, Mockito.times(1)).findById(poolId);
        Mockito.verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(poolRequestArgumentCaptor.capture());
        PoolRequest pool = poolRequestArgumentCaptor.getValue();

        assertThat(pool).isNotNull();

        assertThat(pool.getPoolNumber())
            .as("Expect pool number to be mapped from request dto")
            .isEqualTo(poolRequestDto.getPoolNumber());
        assertThat(pool.getPoolType().getPoolType())
            .as("Expect pool type to be mapped from request dto")
            .isEqualTo(poolRequestDto.getPoolType());
        assertThat(pool.getNumberRequested())
            .as("Expect number requested to be mapped from the request dto")
            .isEqualTo(poolRequestDto.getNumberRequested());
        assertThat(pool.getTotalNoRequired())
            .as("Expect total number requested to be mapped from the request dto")
            .isEqualTo(poolRequestDto.getNumberRequested());
        assertThat(pool.getReturnDate())
            .as("Expect service start date to be mapped from request dto")
            .isEqualTo(poolRequestDto.getAttendanceDate());
        assertThat(pool.getAttendTime())
            .as("Expect attendance time to be mapped from request dto")
            .isEqualToIgnoringSeconds(LocalDateTime.of(poolRequestDto.getAttendanceDate(),
                poolRequestDto.getAttendanceTime()));
        assertThat(pool.getNewRequest())
            .as("Expect new request flag to be set to 'Y' - jurors have not yet been summoned")
            .isEqualTo('Y');
        assertThat(pool.getOwner())
            .as("Expect owner to be set to bureau's location code")
            .isEqualTo("400");
        assertThat(pool.isNilPool())
            .as("Expect nil pool flag to be set to false")
            .isFalse();
    }

    @Test
    public void test_savePoolRequest_newPoolFromBureau_validBureau() {

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setOwner("415");

        String poolId = "123456789";
        Mockito.when(poolRequestRepository.saveAndFlush(Mockito.any())).thenReturn(poolRequest);
        Mockito.when(poolRequestRepository.findById(poolId)).thenReturn(Optional.empty());

        CourtLocation courtLocation = new CourtLocation();
        Mockito.when(courtLocationRepository.findById("415")).thenReturn(Optional.of(courtLocation));
        PoolType poolType = new PoolType("CRO", "CROWN COURT");
        Mockito.when(poolTypeRepository.findById("CRO")).thenReturn(Optional.of(poolType));
        Mockito.when(poolRequestRepository.saveAndFlush(Mockito.any())).thenReturn(new PoolRequest());

        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestService.savePoolRequest(poolRequestDto, buildPayload("400"));

        ArgumentCaptor<PoolRequest> poolRequestArgumentCaptor = ArgumentCaptor.forClass(PoolRequest.class);
        Mockito.verify(poolRequestRepository, Mockito.times(1)).findById(poolId);
        Mockito.verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(poolRequestArgumentCaptor.capture());
        PoolRequest pool = poolRequestArgumentCaptor.getValue();

        assertThat(pool).isNotNull();

        assertThat(pool.getPoolNumber())
            .as("Expect pool number to be mapped from request dto")
            .isEqualTo(poolRequestDto.getPoolNumber());
        assertThat(pool.getPoolType().getPoolType())
            .as("Expect pool type to be mapped from request dto")
            .isEqualTo(poolRequestDto.getPoolType());
        assertThat(pool.getNumberRequested())
            .as("Expect number requested to be mapped from the request dto")
            .isEqualTo(poolRequestDto.getNumberRequested());
        assertThat(pool.getTotalNoRequired())
            .as("Expect total number requested to be mapped from the request dto")
            .isEqualTo(poolRequestDto.getNumberRequested());
        assertThat(pool.getReturnDate())
            .as("Expect service start date to be mapped from request dto")
            .isEqualTo(poolRequestDto.getAttendanceDate());
        assertThat(pool.getAttendTime())
            .as("Expect attendance time to be mapped from request dto")
            .isEqualToIgnoringSeconds(LocalDateTime.of(poolRequestDto.getAttendanceDate(),
                poolRequestDto.getAttendanceTime()));
        assertThat(pool.getNewRequest())
            .as("Expect new request flag to be set to 'Y' - jurors have not yet been summoned")
            .isEqualTo('Y');
        assertThat(pool.getOwner())
            .as("Expect owner to be set to bureau's location code")
            .isEqualTo("400");
        assertThat(pool.isNilPool())
            .as("Expect nil pool flag to be set to false")
            .isFalse();
    }

    @Test
    public void test_savePoolRequest_newPoolFromBureau_duplicatePoolRequest() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();

        String poolId = "123456789";
        Mockito.when(poolRequestRepository.findById(poolId)).thenReturn(Optional.of(new PoolRequest()));

        assertThatExceptionOfType(PoolRequestException.DuplicatePoolRequest.class)
            .isThrownBy(() -> poolRequestService.savePoolRequest(poolRequestDto, buildPayload("415")));

        Mockito.verify(poolRequestRepository, Mockito.times(1)).findById(poolId);
        Mockito.verify(poolRequestRepository, Mockito.never()).saveAndFlush(Mockito.any());
    }

    @Test
    public void test_savePoolRequest_newCourtOnlyPool_validCourt() {



        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setCourtOnly(true);
        poolRequestDto.setAttendanceTime(LocalTime.of(10,11));
        poolRequestDto.setNumberRequested(0);

        CourtLocation courtLocation = new CourtLocation();
        PoolType poolType = new PoolType("CRO", "CROWN COURT");

        String poolNumber = "123456789";

        Mockito.doReturn(null).when(poolRequestRepository).saveAndFlush(Mockito.any());
        Mockito.when(poolRequestRepository.findById(poolNumber)).thenReturn(Optional.empty());
        String courtOwner = "415";
        Mockito.when(courtLocationRepository.findById(courtOwner)).thenReturn(Optional.of(courtLocation));
        Mockito.when(poolTypeRepository.findById("CRO")).thenReturn(Optional.of(poolType));

        poolRequestService.savePoolRequest(poolRequestDto, buildPayload(courtOwner));

        Mockito.verify(poolRequestRepository, Mockito.times(1)).findById(poolNumber);
        ArgumentCaptor<PoolRequest> poolRequestArgumentCaptor = ArgumentCaptor.forClass(PoolRequest.class);
        Mockito.verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(poolRequestArgumentCaptor.capture());
        PoolRequest poolRequest = poolRequestArgumentCaptor.getValue();
        assertThat(poolRequest).isNotNull();

        assertThat(poolRequest.getPoolNumber())
            .as("Expect pool number to be mapped from request dto")
            .isEqualTo(poolRequestDto.getPoolNumber());
        assertThat(poolRequest.getPoolType().getPoolType())
            .as("Expect pool type to be mapped from request dto")
            .isEqualTo(poolRequestDto.getPoolType());
        assertThat(poolRequest.getNumberRequested())
            .as("Expect number requested to be set to null")
            .isNull();
        assertThat(poolRequest.getTotalNoRequired())
            .as("Expect total number requested to be set to 0")
            .isEqualTo(0);
        assertThat(poolRequest.getReturnDate())
            .as("Expect service start date to be mapped from request dto")
            .isEqualTo(poolRequestDto.getAttendanceDate());
        assertThat(poolRequest.getAttendTime())
            .as("Expect attendance time to be set")
            .isEqualTo(LocalDateTime.of(poolRequestDto.getAttendanceDate(), poolRequestDto.getAttendanceTime()));
        assertThat(poolRequest.getNewRequest())
            .as("Expect new request flag to be set to 'N' - pool request goes straight to created")
            .isEqualTo('N');
        assertThat(poolRequest.getOwner())
            .as("Expect owner to be set to primary court location code")
            .isEqualTo(courtOwner);
        assertThat(poolRequest.isNilPool())
            .as("Expect nil pool flag to be set to false")
            .isFalse();
    }

    @Test
    public void test_savePoolRequest_newCourtOnlyPool_invalidBureauUser() {


        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setCourtOnly(true);
        poolRequestDto.setAttendanceTime(null);
        poolRequestDto.setNumberRequested(0);

        String poolNumber = "123456789";

        Mockito.when(poolRequestRepository.findById(poolNumber)).thenReturn(Optional.empty());

        String bureauOwner = "400";
        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            poolRequestService.savePoolRequest(poolRequestDto, buildPayload(bureauOwner)));

        Mockito.verify(poolRequestRepository, Mockito.times(1)).findById(poolNumber);
        Mockito.verify(poolRequestRepository, Mockito.never()).saveAndFlush(Mockito.any());
    }

    private PoolRequestDto createValidPoolRequestDto() {
        return new PoolRequestDto("123456789", "415",
            LocalDate.of(2022, 10, 3), 150, "CRO",
            LocalTime.of(11, 12), 0, false);
    }

    private BureauJwtPayload buildPayload(String owner) {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login("SOME_USER")
            .owner(owner)
            .build();
    }

}
