package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import com.querydsl.core.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.AddAttendanceDayDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDateDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_IN;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_OUT;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.EXPENSE_ENTERED;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.ATTENDANCE_RECORD_ALREADY_EXISTS;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.LawOfDemeter",
    "PMD.NcssCount", "PMD.CouplingBetweenObjects"})
@ExtendWith(SpringExtension.class)
class JurorAppearanceServiceTest {
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private AppearanceRepository appearanceRepository;
    @Mock
    private CourtLocationRepository courtLocationRepository;
    @Mock
    private JurorExpenseService jurorExpenseService;

    @InjectMocks
    JurorAppearanceServiceImpl jurorAppearanceService;

    private static final String JUROR_123456789 = "123456789";
    private static final String JUROR1 = "111111111";
    private static final String JUROR2 = "222222222";
    private static final String JUROR3 = "333333333";
    private static final String JUROR5 = "555555555";
    private static final String JUROR6 = "666666666";
    private static final String JUROR7 = "777777777";
    private static final String JUROR8 = "888888888";
    private static final String JUROR9 = "999999999";

    private static final String OWNER_415 = "415";
    private static final String LOC_415 = "415";

    @Test
    void addAttendanceDayHappyPath() {
        jurorAppearanceService = spy(jurorAppearanceService);

        doReturn(null).when(jurorAppearanceService).processAppearance(any(), any(), anyBoolean());
        doReturn(null).when(jurorAppearanceService).updateConfirmAttendance(any());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorJurorNumberAndPoolPoolNumber(
                JUROR_123456789, "123456789");

        AddAttendanceDayDto dto = buildAddAttendanceDayDto();
        jurorAppearanceService.addAttendanceDay(buildPayload(OWNER_415, Arrays.asList("415", "462", "767")),
            dto);

        ArgumentCaptor<JurorAppearanceDto> appearanceDtoCaptor = ArgumentCaptor.forClass(JurorAppearanceDto.class);
        ArgumentCaptor<UpdateAttendanceDto.CommonData> attendanceDtoCaptor =
            ArgumentCaptor.forClass(UpdateAttendanceDto.CommonData.class);
        ArgumentCaptor<BureauJwtPayload> payloadArgumentCaptor = ArgumentCaptor.forClass(BureauJwtPayload.class);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndPoolPoolNumber(JUROR_123456789, "123456789");
        verify(jurorAppearanceService, times(1)).processAppearance(payloadArgumentCaptor.capture(),
            appearanceDtoCaptor.capture(), eq(true));
        verify(jurorAppearanceService, times(1)).updateConfirmAttendance(attendanceDtoCaptor.capture());

        JurorAppearanceDto appearanceDto = appearanceDtoCaptor.getValue();

        assertThat(appearanceDto).isNotNull();
        assertThat(appearanceDto.getAttendanceDate()).isEqualTo(dto.getAttendanceDate());
        assertThat(appearanceDto.getJurorNumber()).isEqualTo(dto.getJurorNumber());
        assertThat(appearanceDto.getCheckInTime()).isEqualTo(dto.getCheckInTime());
        assertThat(appearanceDto.getCheckOutTime()).isEqualTo(dto.getCheckOutTime());
        assertThat(appearanceDto.getLocationCode()).isEqualTo(dto.getLocationCode());

        UpdateAttendanceDto.CommonData commonData = attendanceDtoCaptor.getValue();

        assertThat(commonData).isNotNull();
        assertThat(commonData.getAttendanceDate()).isEqualTo(dto.getAttendanceDate());
        assertThat(commonData.getCheckInTime()).isEqualTo(dto.getCheckInTime());
        assertThat(commonData.getCheckOutTime()).isEqualTo(dto.getCheckOutTime());
        assertThat(commonData.getLocationCode()).isEqualTo(dto.getLocationCode());

    }

    @Test
    void addAttendanceDayWrongAccess() {
        jurorAppearanceService = spy(jurorAppearanceService);

        doReturn(null).when(jurorAppearanceService).processAppearance(any(), any(), anyBoolean());
        doReturn(null).when(jurorAppearanceService).updateConfirmAttendance(any());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorJurorNumberAndPoolPoolNumber(
                JUROR_123456789, "123456789");

        AddAttendanceDayDto dto = buildAddAttendanceDayDto();

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
                jurorAppearanceService.addAttendanceDay(buildPayload("400", List.of("400")),
                    dto)).as("Invalid access to juror pool")
            .withMessageContaining("Invalid access to juror pool");

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndPoolPoolNumber(JUROR_123456789, "123456789");
        verify(jurorAppearanceService, never()).processAppearance(any(), any(), anyBoolean());
        verify(jurorAppearanceService, never()).updateConfirmAttendance(any());

    }

    @Test
    void addAttendanceDayJurorPoolNotFound() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorJurorNumberAndPoolPoolNumber(
                JUROR_123456789, "111111111");

        AddAttendanceDayDto dto = buildAddAttendanceDayDto();

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                jurorAppearanceService.addAttendanceDay(buildPayload("417", List.of("417")),
                    dto)).as("No valid juror pool found")
            .withMessageContaining("No valid juror pool found");

    }

    @Test
    void processAppearanceBooleanTrue() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        JurorAppearanceResponseDto.JurorAppearanceResponseData appearanceData = JurorAppearanceResponseDto
            .JurorAppearanceResponseData.builder().jurorNumber(JUROR_123456789)
            .lastName("LASTNAME")
            .firstName("FIRSTNAME")
            .checkInTime(LocalTime.of(9, 30))
            .jurorStatus(IJurorStatus.RESPONDED)
            .build();

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList = new ArrayList<>();
        appearanceDataList.add(appearanceData);

        when(appearanceRepository.getAppearanceRecords(anyString(), any(), anyString()))
            .thenReturn(appearanceDataList);

        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        jurorAppearanceDto.setCheckOutTime(LocalTime.of(17, 30));

        jurorAppearanceService.processAppearance(buildPayload(OWNER_415, Arrays.asList("415", "462", "767")),
            jurorAppearanceDto, true);

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(jurorRepository, times(1))
            .findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
        verify(courtLocationRepository, times(1))
            .findById(LOC_415);
        verify(appearanceRepository, times(1)).saveAndFlush(appearanceArgumentCaptor.capture());

        Appearance appearance = appearanceArgumentCaptor.getValue();

        assertThat(appearance.getTimeIn()).isEqualTo(jurorAppearanceDto.getCheckInTime());
        assertThat(appearance.getTimeOut()).isEqualTo(jurorAppearanceDto.getCheckOutTime());
        assertThat(appearance.getAppearanceStage()).isEqualTo(CHECKED_IN);
    }

    @Test
    void validateTimeAndAppearanceStage() {
        LocalTime timeIn = LocalTime.of(9, 30);
        LocalTime timeOut = LocalTime.of(17, 30);

        jurorAppearanceService = spy(jurorAppearanceService);

        jurorAppearanceService.validateTimeAndAppearanceStage(timeIn, timeOut, CHECKED_IN, true);

        verify(jurorAppearanceService, times(1)).validateCheckInNotNull(timeIn);
        verify(jurorAppearanceService, times(1)).validateCheckOutNotNull(timeOut);
        verify(jurorAppearanceService, times(1)).validateBothCheckInAndOutTimeNotNull(timeIn, timeOut);
        verify(jurorAppearanceService, never()).validateBothCheckInAndOutTimeNotSet(any(), any());
    }

    @Test
    void testCheckInJurorHappy() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        JurorAppearanceResponseDto.JurorAppearanceResponseData appearanceData = JurorAppearanceResponseDto
            .JurorAppearanceResponseData.builder().jurorNumber(JUROR_123456789)
            .lastName("LASTNAME")
            .firstName("FIRSTNAME")
            .checkInTime(LocalTime.of(9, 30))
            .jurorStatus(IJurorStatus.RESPONDED)
            .build();

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList = new ArrayList<>();
        appearanceDataList.add(appearanceData);

        when(appearanceRepository.getAppearanceRecords(anyString(), any(), anyString()))
            .thenReturn(appearanceDataList);

        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        jurorAppearanceService.processAppearance(buildPayload(OWNER_415, Arrays.asList("415", "462", "767")),
            jurorAppearanceDto);

        verify(jurorRepository, times(1))
            .findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
        verify(courtLocationRepository, times(1))
            .findById(LOC_415);
        verify(appearanceRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorUnhappyDatabaseSaveIssue() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        when(appearanceRepository.getAppearanceRecords(anyString(), any(), anyString()))
            .thenReturn(new ArrayList<>());

        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();

        assertThatExceptionOfType(MojException.InternalServerError.class).isThrownBy(() ->
            jurorAppearanceService.processAppearance(buildPayload(OWNER_415, Arrays.asList("415", "462", "767")),
                jurorAppearanceDto));

        verify(jurorRepository, times(1))
            .findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
        verify(courtLocationRepository, times(1))
            .findById(LOC_415);
        verify(appearanceRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidJurorStatus() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.DISQUALIFIED);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            jurorAppearanceService.processAppearance(buildPayload("415", Arrays.asList("415", "462", "767")),
                jurorAppearanceDto));

        verify(jurorRepository, times(1))
            .findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
        verify(courtLocationRepository, times(1))
            .findById(LOC_415);
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidCourtOwner() {
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorAppearanceService.processAppearance(buildPayload("417", List.of("417")),
                jurorAppearanceDto));

        verify(courtLocationRepository, times(1))
            .findById(any());
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorBureauOfficer() {
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorAppearanceService.processAppearance(buildPayload("400", null),
                jurorAppearanceDto));

        verify(courtLocationRepository, times(1))
            .findById(any());
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidCheckinAndCheckoutDateSupplied() {
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        jurorAppearanceDto.setCheckOutTime(LocalTime.of(12, 30));

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                jurorAppearanceDto));

        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidCheckingAndCheckoutDateNotSupplied() {
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        jurorAppearanceDto.setCheckInTime(null);
        jurorAppearanceDto.setCheckOutTime(null);

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                jurorAppearanceDto));

        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidCheckInTimeNotSupplied() {
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        jurorAppearanceDto.setCheckInTime(null);
        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload("417", List.of("417")),
                    jurorAppearanceDto)).as("Must have a check in time if appearance stage is CHECKED_IN")
            .withMessageContaining("Check-in time cannot be null");

        verifyNoInteractions(courtLocationRepository);
        verifyNoInteractions(appearanceRepository);
    }

    @Test
    void testCheckInJurorInvalidCheckOutTimeNotSupplied() {
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        jurorAppearanceDto.setAppearanceStage(CHECKED_OUT);
        jurorAppearanceDto.setCheckOutTime(null);
        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload("417", List.of("417")),
                    jurorAppearanceDto)).as("Must have a check out time if appearance stage is CHECKED_OUT")
            .withMessageContaining("Check-out time cannot be null");

        verifyNoInteractions(courtLocationRepository);
        verifyNoInteractions(appearanceRepository);
    }

    @Test
    void testCheckInJurorInvalidCannotCheckInIfAlreadyCheckedIn() {
        final JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        CourtLocation courtLocation = getCourtLocation();
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(CHECKED_IN);
        doReturn(appearance).when(appearanceRepository).findByJurorNumberAndAttendanceDate(JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check in a juror who is already checked in")
            .withMessageContaining("Juror 123456789 has already checked in");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
        verify(courtLocationRepository, times(1)).findById(LOC_415);
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidCannotCheckInIfAlreadyCheckedOut() {
        final JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        CourtLocation courtLocation = getCourtLocation();
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(CHECKED_OUT);
        doReturn(appearance).when(appearanceRepository).findByJurorNumberAndAttendanceDate(JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check in a juror who is already checked out")
            .withMessageContaining("Juror 123456789 has already checked out");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
        verify(courtLocationRepository, times(1)).findById(LOC_415);
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidCannotCheckInIfAlreadyConfirmed() {
        final JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        CourtLocation courtLocation = getCourtLocation();
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(EXPENSE_ENTERED);
        doReturn(appearance).when(appearanceRepository).findByJurorNumberAndAttendanceDate(JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check in a juror who is already confirmed attendance")
            .withMessageContaining("Juror 123456789 has already confirmed their attendance");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
        verify(courtLocationRepository, times(1)).findById(LOC_415);
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidCannotCheckOutIfAlreadyCheckedOut() {
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        jurorAppearanceDto.setAppearanceStage(CHECKED_OUT);
        jurorAppearanceDto.setCheckOutTime(LocalTime.of(12, 30));
        jurorAppearanceDto.setCheckInTime(null);
        CourtLocation courtLocation = getCourtLocation();
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(CHECKED_OUT);
        doReturn(appearance).when(appearanceRepository).findByJurorNumberAndAttendanceDate(JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check out a juror who is already checked out")
            .withMessageContaining("Juror 123456789 has already checked out");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
        verify(courtLocationRepository, times(1)).findById(LOC_415);
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testCheckInJurorInvalidCannotCheckOutIfAlreadyConfirmed() {
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        jurorAppearanceDto.setAppearanceStage(CHECKED_OUT);
        jurorAppearanceDto.setCheckOutTime(LocalTime.of(12, 30));
        jurorAppearanceDto.setCheckInTime(null);
        CourtLocation courtLocation = getCourtLocation();
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
                JUROR_123456789, true);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(EXPENSE_ENTERED);
        doReturn(appearance).when(appearanceRepository).findByJurorNumberAndAttendanceDate(JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check out a juror who has already confirmed attendance")
            .withMessageContaining("Juror 123456789 has already confirmed their attendance");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
        verify(courtLocationRepository, times(1)).findById(LOC_415);
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testGetAppearancesNoRecordsFound() {
        CourtLocation courtLocation = getCourtLocation();
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        doReturn(new ArrayList<Tuple>()).when(appearanceRepository).getAppearanceRecords(anyString(),
            any(), anyString());

        JurorAppearanceResponseDto jurorAppearanceResponseDto =
            jurorAppearanceService.getAppearanceRecords(LOC_415, now(),
                buildPayload(OWNER_415, Collections.singletonList(LOC_415)));

        assertThat(jurorAppearanceResponseDto).isNotNull();
        assertThat(jurorAppearanceResponseDto.getData()).isEmpty();
    }

    @Test
    void hasAppearancesTrue() {
        when(appearanceRepository.countByJurorNumber(TestConstants.VALID_JUROR_NUMBER)).thenReturn(1L);
        assertTrue(jurorAppearanceService.hasAppearances(TestConstants.VALID_JUROR_NUMBER),
            "Should return true if juror has appearances");
    }

    @Test
    void hasAppearancesFalse() {
        when(appearanceRepository.countByJurorNumber(TestConstants.VALID_JUROR_NUMBER)).thenReturn(0L);
        assertFalse(jurorAppearanceService.hasAppearances(TestConstants.VALID_JUROR_NUMBER),
            "Should return false if juror does not have appearances");
    }

    @Test
    @DisplayName("retrieveAttendanceDetails() - tag JUROR_NUMBER")
    void retrieveAttendanceDetailsJurorNumberOkay() {
        // mock request and dependencies
        RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(null);
        request.getCommonData().setTag(RetrieveAttendanceDetailsTag.JUROR_NUMBER);

        retrieveAttendanceDetailsJurorNumberOkayMockSetup(request);

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.retrieveAttendanceDetails(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        assertThat(response.getDetails()).hasSize(4);

        verify(courtLocationRepository, times(1)).findById(anyString());
        verify(appearanceRepository, times(1))
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any(AppearanceId.class));
        verify(appearanceRepository, never()).deleteById(any(AppearanceId.class));
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(any());
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("retrieveAttendanceDetails() - tag CONFIRM_ATTENDANCE")
    void retrieveAttendanceDetailsConfirmAttendanceOkay() {
        // mock request and dependencies
        RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(null);
        request.getCommonData().setTag(RetrieveAttendanceDetailsTag.CONFIRM_ATTENDANCE);

        retrieveAttendanceDetailsConfirmAttendanceOkayMockSetup(request);

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.retrieveAttendanceDetails(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        assertThat(response.getDetails()).hasSize(2);
        List<AttendanceDetailsResponse.Details> details = response.getDetails();
        assertThat(details.get(0).getJurorNumber()).isEqualTo(JUROR8);
        assertThat(details.get(0).getFirstName()).isEqualTo("TEST");
        assertThat(details.get(0).getLastName()).isEqualTo("EIGHT");
        assertThat(details.get(0).getJurorStatus()).isEqualTo(2);
        assertThat(details.get(0).getCheckInTime()).isNull();
        assertThat(details.get(0).getCheckOutTime()).isNull();
        assertThat(details.get(0).getIsNoShow()).isNull();
        assertThat(details.get(0).getAppearanceStage()).isNull();

        assertThat(details.get(1).getJurorNumber()).isEqualTo(JUROR9);
        assertThat(details.get(1).getFirstName()).isEqualTo("TEST");
        assertThat(details.get(1).getLastName()).isEqualTo("NINE");
        assertThat(details.get(1).getJurorStatus()).isEqualTo(2);
        assertThat(details.get(1).getCheckInTime()).isNull();
        assertThat(details.get(1).getCheckOutTime()).isNull();
        assertThat(details.get(1).getIsNoShow()).isNull();
        assertThat(details.get(1).getAppearanceStage()).isNull();

        AttendanceDetailsResponse.Summary summary = response.getSummary();
        assertThat(summary.getCheckedIn()).isEqualTo(2L);
        assertThat(summary.getCheckedOut()).isZero();
        assertThat(summary.getCheckedInAndOut()).isZero();
        assertThat(summary.getPanelled()).isZero();
        assertThat(summary.getAbsent()).isEqualTo(2L);
        assertThat(summary.getDeleted()).isZero();
        assertThat(summary.getAdditionalInformation()).isNull();

        verify(courtLocationRepository, times(1)).findById(anyString());
        verify(appearanceRepository, times(1))
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any(AppearanceId.class));
        verify(appearanceRepository, never()).deleteById(any(AppearanceId.class));
        verify(appearanceRepository, times(1))
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("retrieveAttendanceDetails() - tag NOT_CHECKED_OUT")
    void retrieveAttendanceDetailsNotCheckedOutOkay() {
        // mock request and dependencies
        RetrieveAttendanceDetailsDto request = buildRetrieveAttendanceDetailsDto(null);

        retrieveAttendanceDetailsNotCheckedOutOkayMockSetup(request);

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.retrieveAttendanceDetails(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        assertThat(response.getDetails()).hasSize(2);

        List<AttendanceDetailsResponse.Details> details = response.getDetails();
        assertThat(details.get(0).getJurorNumber()).isEqualTo(JUROR3);
        assertThat(details.get(0).getFirstName()).isEqualTo("TEST");
        assertThat(details.get(0).getLastName()).isEqualTo("THREE");
        assertThat(details.get(0).getJurorStatus()).isEqualTo(3);
        assertThat(details.get(0).getCheckInTime()).isEqualTo("09:30");
        assertThat(details.get(0).getCheckOutTime()).isNull();
        assertThat(details.get(0).getIsNoShow()).isEqualTo(Boolean.FALSE);
        assertThat(details.get(0).getAppearanceStage()).isEqualTo(CHECKED_IN);

        assertThat(details.get(1).getJurorNumber()).isEqualTo(JUROR6);
        assertThat(details.get(1).getFirstName()).isEqualTo("TEST");
        assertThat(details.get(1).getLastName()).isEqualTo("SIX");
        assertThat(details.get(1).getJurorStatus()).isEqualTo(2);
        assertThat(details.get(1).getCheckInTime()).isEqualTo("09:30");
        assertThat(details.get(1).getCheckOutTime()).isNull();
        assertThat(details.get(1).getIsNoShow()).isEqualTo(Boolean.FALSE);
        assertThat(details.get(1).getAppearanceStage()).isEqualTo(CHECKED_IN);

        verify(courtLocationRepository, times(1)).findById(anyString());
        verify(appearanceRepository, times(1))
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any(AppearanceId.class));
        verify(appearanceRepository, never()).deleteById(any(AppearanceId.class));
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("updateAttendance() - status CHECK_OUT")
    void updateAttendanceCheckOut() {
        // mock request and dependencies
        final UpdateAttendanceDto request = buildUpdateAttendanceDto(null);

        updateAttendanceCheckOutMockSetup();

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.updateAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        assertThat(response.getDetails()).hasSize(1);

        List<AttendanceDetailsResponse.Details> details = response.getDetails();
        assertThat(details.get(0).getJurorNumber()).isEqualTo(JUROR3);
        assertThat(details.get(0).getFirstName()).isEqualTo("TEST");
        assertThat(details.get(0).getLastName()).isEqualTo("THREE");
        assertThat(details.get(0).getJurorStatus()).isEqualTo(3);

        AttendanceDetailsResponse.Summary summary = response.getSummary();
        assertThat(summary.getCheckedIn()).isZero();
        assertThat(summary.getCheckedOut()).isEqualTo(3L);
        assertThat(summary.getCheckedInAndOut()).isZero();
        assertThat(summary.getPanelled()).isEqualTo(1L);
        assertThat(summary.getAbsent()).isZero();
        assertThat(summary.getDeleted()).isZero();
        assertThat(summary.getAdditionalInformation()).isNull();

        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, times(1))
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, times(3)).findById(any(AppearanceId.class));
        verify(appearanceRepository, never()).deleteById(any(AppearanceId.class));
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, times(1)).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("updateAttendance() - status CHECK_OUT_PANELLED")
    void updateAttendanceCheckOutPanelled() {
        // mock request and dependencies
        final UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
        request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_OUT_PANELLED);

        updateAttendanceCheckOutMockSetup();

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.updateAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        assertThat(response.getDetails()).isNull();

        AttendanceDetailsResponse.Summary summary = response.getSummary();
        assertThat(summary.getCheckedIn()).isZero();
        assertThat(summary.getCheckedOut()).isEqualTo(4);
        assertThat(summary.getCheckedInAndOut()).isZero();
        assertThat(summary.getPanelled()).isZero();
        assertThat(summary.getAbsent()).isZero();
        assertThat(summary.getDeleted()).isZero();
        assertThat(summary.getAdditionalInformation()).isNull();

        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, times(1))
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, times(4)).findById(any(AppearanceId.class));
        verify(appearanceRepository, never()).deleteById(any(AppearanceId.class));
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, times(1)).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("updateAttendance() - status CHECK_IN")
    void updateAttendanceCheckIn() {
        // mock request and dependencies
        LocalTime checkInTime = LocalTime.of(9, 50);

        UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
        request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_IN);
        request.getCommonData().setCheckInTime(checkInTime);
        request.getCommonData().setCheckOutTime(null);

        updateAttendanceCheckInMockSetUp(checkInTime);

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.updateAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        List<AttendanceDetailsResponse.Details> details = response.getDetails();
        assertThat(details).isNull();

        AttendanceDetailsResponse.Summary summary = response.getSummary();
        assertThat(summary.getCheckedIn()).isEqualTo(6);
        assertThat(summary.getCheckedOut()).isZero();
        assertThat(summary.getCheckedInAndOut()).isZero();
        assertThat(summary.getPanelled()).isZero();
        assertThat(summary.getAbsent()).isZero();
        assertThat(summary.getDeleted()).isZero();
        assertThat(summary.getAdditionalInformation()).isNull();

        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, times(1))
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, times(6)).findById(any(AppearanceId.class));
        verify(appearanceRepository, never()).deleteById(any(AppearanceId.class));
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, times(1)).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("updateAttendance() - status CHECK_IN.  Exception thrown because check-in time is null")
    void updateAttendanceCheckInNull() {
        // mock request and dependencies
        UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
        request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_IN);
        request.getCommonData().setCheckInTime(null);

        updateAttendanceCheckInMockSetUp(LocalTime.of(9, 32));

        // invoke actual service method under test
        MojException.BadRequest exception = assertThrows(MojException.BadRequest.class,
            () -> jurorAppearanceService.updateAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request),
            "Expected exception to be thrown when check-in time is null");

        assertEquals("Check-in time cannot be null", exception.getMessage(),
            "Check-in time cannot be null");

        // verify response
        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, never()).retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any());
        verify(appearanceRepository, never()).deleteById(any());
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("updateAttendance() - status CHECK_IN_AND_OUT")
    void updateAttendanceCheckInAndOut() {
        // mock request and dependencies
        List<String> jurors = new ArrayList<>();
        jurors.add(JUROR7);
        UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
        request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_IN_AND_OUT);
        request.getCommonData().setCheckInTime(LocalTime.of(9, 50));
        request.getCommonData().setCheckOutTime(LocalTime.of(17, 53));
        request.getCommonData().setSingleJuror(Boolean.TRUE);

        updateAttendanceCheckInAndOutMockSetUp();

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.updateAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        List<AttendanceDetailsResponse.Details> details = response.getDetails();
        assertThat(details).isNull();

        AttendanceDetailsResponse.Summary summary = response.getSummary();
        assertThat(summary.getCheckedIn()).isZero();
        assertThat(summary.getCheckedOut()).isZero();
        assertThat(summary.getCheckedInAndOut()).isEqualTo(1);
        assertThat(summary.getPanelled()).isZero();
        assertThat(summary.getAbsent()).isZero();
        assertThat(summary.getDeleted()).isZero();
        assertThat(summary.getAdditionalInformation()).isNull();

        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, never())
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, times(1)).findById(any(AppearanceId.class));
        verify(appearanceRepository, never()).deleteById(any(AppearanceId.class));
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, times(1)).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("updateAttendance() - status CONFIRM_ATTENDANCE")
    void updateAttendanceConfirmAttendance() {
        // mock request and dependencies
        LocalTime checkInTime = LocalTime.of(9, 57);

        UpdateAttendanceDto request = buildUpdateAttendanceDto(null);
        request.getCommonData().setStatus(UpdateAttendanceStatus.CONFIRM_ATTENDANCE);
        request.getCommonData().setCheckOutTime(null);

        updateAttendanceConfirmAttendanceMockSetup(checkInTime);

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.updateAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        List<AttendanceDetailsResponse.Details> details = response.getDetails();
        assertThat(details).isNull();

        AttendanceDetailsResponse.Summary summary = response.getSummary();
        assertThat(summary.getCheckedIn()).isEqualTo(3L);
        assertThat(summary.getCheckedOut()).isZero();
        assertThat(summary.getCheckedInAndOut()).isZero();
        assertThat(summary.getPanelled()).isZero();
        assertThat(summary.getAbsent()).isEqualTo(2L);
        assertThat(summary.getDeleted()).isZero();
        assertThat(summary.getAdditionalInformation()).isNull();

        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, times(1))
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any());
        verify(appearanceRepository, never()).deleteById(any());
        verify(appearanceRepository, times(1)).retrieveNonAttendanceDetails(any());
        verify(appearanceRepository, times(1))
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, times(1)).findAllById(any());
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, times(2)).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("updateAttendance() - SingleJuror flag is true but multiple jurors in request ")
    void updateAttendanceSingleJurorFlagWithMultipleJurors() {
        // mock request and dependencies
        List<String> jurors = new ArrayList<>();
        jurors.add(JUROR1);
        jurors.add(JUROR2);
        UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
        request.getCommonData().setStatus(UpdateAttendanceStatus.CHECK_OUT);
        request.getCommonData().setCheckOutTime(null);
        request.getCommonData().setSingleJuror(Boolean.TRUE);

        // invoke actual service method under test
        MojException.BadRequest exception = assertThrows(MojException.BadRequest.class,
            () -> jurorAppearanceService.updateAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request),
            "Multiple jurors not allowed for single record update");

        assertEquals("Multiple jurors not allowed for single record update", exception.getMessage(),
            "Multiple jurors not allowed for single record update");

        // verify response
        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, never()).retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any());
        verify(appearanceRepository, never()).deleteById(any());
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("deleteAttendance() - status DELETE")
    void deleteAttendance() {
        // mock request and dependencies
        List<String> jurors = new ArrayList<>();
        jurors.add(JUROR1);
        UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
        request.getCommonData().setStatus(UpdateAttendanceStatus.DELETE);
        request.getCommonData().setCheckOutTime(null);
        request.getCommonData().setSingleJuror(Boolean.TRUE);

        deleteAttendanceMockSetup(false);

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.deleteAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        List<AttendanceDetailsResponse.Details> details = response.getDetails();
        assertThat(details).isNull();

        AttendanceDetailsResponse.Summary summary = response.getSummary();
        assertThat(summary.getCheckedIn()).isZero();
        assertThat(summary.getCheckedOut()).isZero();
        assertThat(summary.getCheckedInAndOut()).isZero();
        assertThat(summary.getPanelled()).isZero();
        assertThat(summary.getAbsent()).isZero();
        assertThat(summary.getDeleted()).isEqualTo(1L);
        assertThat(summary.getAdditionalInformation()).isNull();

        verify(courtLocationRepository, times(1)).findById(anyString());
        verify(appearanceRepository, never()).retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, times(1)).findById(any());
        verify(appearanceRepository, times(1)).deleteById(any());
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("deleteAttendance() - status DELETE.  SingleJuror flag is false - not implemented exception")
    void deleteAttendanceMultipleJurorsAndSingleJurorFlagIsFalse() {
        // mock request and dependencies
        List<String> jurors = new ArrayList<>();
        jurors.add(JUROR1);
        UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
        request.getCommonData().setStatus(UpdateAttendanceStatus.DELETE);
        request.getCommonData().setCheckOutTime(null);

        // invoke actual service method under test
        MojException.BadRequest exception = assertThrows(MojException.BadRequest.class,
            () -> jurorAppearanceService.deleteAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request),
            "Expected exception to be thrown when SingleJuror flag is false");

        assertEquals("Cannot delete multiple juror attendance records", exception.getMessage(),
            "Cannot delete multiple juror attendance records");

        // verify response
        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, never()).retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any());
        verify(appearanceRepository, never()).deleteById(any());
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("deleteAttendance() - status DELETE.  Cannot delete multiple jurors when SingleJuror flag is True - "
        + "not implemented exception")
    void deleteAttendanceMultipleJurorsButSingleJurorFlagIsTrue() {
        // mock request and dependencies
        List<String> jurors = new ArrayList<>();
        jurors.add(JUROR1);
        jurors.add(JUROR2);
        UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
        request.getCommonData().setStatus(UpdateAttendanceStatus.DELETE);
        request.getCommonData().setCheckOutTime(null);
        request.getCommonData().setSingleJuror(Boolean.TRUE);

        // invoke actual service method under test
        MojException.BadRequest exception = assertThrows(MojException.BadRequest.class,
            () -> jurorAppearanceService.deleteAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request),
            "Expected exception to be thrown when multiple juror attendance records are being deleted");

        assertEquals("Cannot delete multiple juror attendance records", exception.getMessage(),
            "Cannot delete multiple juror attendance records");

        // verify response
        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, never()).retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any());
        verify(appearanceRepository, never()).deleteById(any());
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("deleteAttendance() - status DELETE.  No attendance record")
    void deleteAttendanceNoAttendanceRecordExists() {
        // mock request and dependencies
        List<String> jurors = new ArrayList<>();
        jurors.add(JUROR1);
        UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
        request.getCommonData().setStatus(UpdateAttendanceStatus.DELETE);
        request.getCommonData().setCheckOutTime(null);
        request.getCommonData().setSingleJuror(Boolean.TRUE);

        deleteAttendanceMockSetup(true);

        // invoke actual service method under test
        AttendanceDetailsResponse response =
            jurorAppearanceService.deleteAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request);

        // assert and verify response
        List<AttendanceDetailsResponse.Details> details = response.getDetails();
        assertThat(details).isNull();

        AttendanceDetailsResponse.Summary summary = response.getSummary();
        assertThat(summary.getCheckedIn()).isZero();
        assertThat(summary.getCheckedOut()).isZero();
        assertThat(summary.getCheckedInAndOut()).isZero();
        assertThat(summary.getPanelled()).isZero();
        assertThat(summary.getAbsent()).isZero();
        assertThat(summary.getDeleted()).isZero();
        assertThat(summary.getAdditionalInformation()).isEqualTo(
            "No attendance record found for juror number 111111111");

        verify(courtLocationRepository, times(1)).findById(anyString());
        verify(appearanceRepository, never()).retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, times(1)).findById(any());
        verify(appearanceRepository, never()).deleteById(any());
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("deleteAttendance() - status CHECK-OUT.  Incorrect status set - bad request exception")
    void deleteAttendanceIncorrectStatus() {
        // mock request and dependencies
        List<String> jurors = new ArrayList<>();
        jurors.add(JUROR1);
        UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
        request.getCommonData().setCheckOutTime(null);
        request.getCommonData().setSingleJuror(Boolean.TRUE);

        // invoke actual service method under test
        MojException.BadRequest exception = assertThrows(MojException.BadRequest.class,
            () -> jurorAppearanceService.deleteAttendance(buildPayload(OWNER_415, List.of(LOC_415)), request),
            "Cannot delete attendance records for status CHECK_OUT");

        assertEquals("Cannot delete attendance records for status CHECK_OUT", exception.getMessage(),
            "Cannot delete attendance records for status CHECK_OUT");

        // verify response
        verify(courtLocationRepository, never()).findById(anyString());
        verify(appearanceRepository, never()).retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));
        verify(appearanceRepository, never()).findById(any());
        verify(appearanceRepository, never()).deleteById(any());
        verify(appearanceRepository, never())
            .retrieveNonAttendanceDetails(any(RetrieveAttendanceDetailsDto.CommonData.class));
        verify(appearanceRepository, never()).findAllById(Collections.singleton(any(AppearanceId.class)));
        verify(appearanceRepository, never()).saveAndFlush(any());
        verify(appearanceRepository, never()).saveAllAndFlush(any());
    }

    @Nested
    @DisplayName("Service method: updateAttendanceDate()")
    class UpdateAttendanceDate {
        static final String POOL_NUMBER_415230101 = "415230101";
        static final LocalDate ATTENDANCE_DATE = now().plusMonths(1);

        @Test
        @DisplayName("Update attendance date - all jurors updated - status is responded")
        void updateAttendanceDateAllJurorsUpdatedStatusIsResponded() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR1);
            jurorNumbers.add(JUROR2);
            final UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool());

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool());

            when(jurorPoolRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());

            String response = jurorAppearanceService.updateAttendanceDate(request);

            assertEquals("Attendance date updated for 2 juror(s)", response,
                "2 juror pool records should be updated with the attendance date");

            verify(jurorPoolRepository, times(2))
                .findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(anyString(),
                    anyString(), anyString(), anyBoolean());
            verify(jurorPoolRepository, times(1)).saveAllAndFlush(any());
        }

        @Test
        @DisplayName("Update attendance date - all jurors updated - status is panelled")
        void updateAttendanceDateAllJurorsUpdatedStatusIsPanelled() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR1);
            final UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool());

            JurorPool jurorPool = buildJurorPool();
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.PANEL);
            jurorPool.setStatus(jurorStatus);
            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(jurorPool);

            when(jurorPoolRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());

            String response = jurorAppearanceService.updateAttendanceDate(request);

            assertEquals("Attendance date updated for 1 juror(s)", response,
                "1 juror pool record should be updated with the attendance date");

            verify(jurorPoolRepository, times(1))
                .findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(anyString(),
                    anyString(), anyString(), anyBoolean());
            verify(jurorPoolRepository, times(1)).saveAllAndFlush(any());
        }

        @Test
        @DisplayName("Update attendance date - all jurors updated - status is juror")
        void updateAttendanceDateAllJurorsUpdatedStatusIsJuror() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR1);
            final UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool());

            JurorPool jurorPool = buildJurorPool();
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.JUROR);
            jurorPool.setStatus(jurorStatus);
            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(jurorPool);

            when(jurorPoolRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());

            String response = jurorAppearanceService.updateAttendanceDate(request);

            assertEquals("Attendance date updated for 1 juror(s)", response,
                "1 juror pool record should be updated with the attendance date");

            verify(jurorPoolRepository, times(1))
                .findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(anyString(),
                    anyString(), anyString(), anyBoolean());
            verify(jurorPoolRepository, times(1)).saveAllAndFlush(any());
        }

        @Test
        @DisplayName("Update attendance date - some jurors updated")
        void updateAttendanceDateSomeJurorsUpdated() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR1);
            jurorNumbers.add(JUROR3); // does not exist

            final UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool());

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(null);

            when(jurorPoolRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());

            String response = jurorAppearanceService.updateAttendanceDate(request);

            assertEquals("Attendance date updated for 1 juror(s)", response,
                "1 juror pool record should be updated with the attendance date");

            verify(jurorPoolRepository, times(2))
                .findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(anyString(),
                    anyString(), anyString(), anyBoolean());
            verify(jurorPoolRepository, times(1)).saveAllAndFlush(any());
        }

        @Test
        @DisplayName("Update attendance date - no jurors updated - no juror pool record")
        void updateAttendanceDateNoJurorsUpdatedJurorNoJurorPoolRecord() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR1); // juror pool record does not exist
            jurorNumbers.add(JUROR3); // juror pool record does not exist

            final UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(null);

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(null);

            when(jurorPoolRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());

            String response = jurorAppearanceService.updateAttendanceDate(request);

            assertEquals("Attendance date updated for 0 juror(s)", response,
                "0 juror pool records should be updated with the attendance date");
        }

        @Test
        @DisplayName("Update attendance date - no jurors updated - status invalid")
        void updateAttendanceDateNoJurorsUpdatedStatusInvalid() {
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR1); // juror pool record does not exist

            final UpdateAttendanceDateDto request = buildUpdateAttendanceDateDto(jurorNumbers);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            JurorPool jurorPool = buildJurorPool();
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.EXCUSED);
            jurorPool.setStatus(jurorStatus);
            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                TestConstants.VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(jurorPool);

            when(jurorPoolRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());

            String response = jurorAppearanceService.updateAttendanceDate(request);

            assertEquals("Attendance date updated for 0 juror(s)", response,
                "0 juror pool records should be updated with the attendance date");
        }

        private UpdateAttendanceDateDto buildUpdateAttendanceDateDto(List<String> jurorNumbers) {
            return UpdateAttendanceDateDto.builder()
                .jurorNumbers(jurorNumbers)
                .attendanceDate(ATTENDANCE_DATE)
                .poolNumber(POOL_NUMBER_415230101)
                .build();
        }

        private JurorPool buildJurorPool() {
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setJuror(new Juror());
            jurorPool.setIsActive(true);
            jurorPool.setStatus(jurorStatus);
            jurorPool.setNextDate(now().plusWeeks(1));

            return jurorPool;
        }
    }

    @Nested
    @DisplayName("jurorToDismissTest")
    class JurorToDismissTest {

        @Test
        void positiveRequestAllCategories() {

            List<String> pools = new ArrayList<>();
            pools.add(TestConstants.VALID_POOL_NUMBER);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .build();

            final JurorsToDismissRequestDto jurorsToDismissRequestDto = JurorsToDismissRequestDto.builder()
                .poolNumbers(pools)
                .locationCode(TestConstants.VALID_COURT_LOCATION)
                .includeNotInAttendance(true)
                .includeOnCall(true)
                .numberOfJurorsToDismiss(3)
                .build();

            // create juror pools in attendance
            final List<JurorPool> jurorPools = new ArrayList<>();
            JurorPool jurorPool1 = new JurorPool();
            jurorPool1.setJuror(new Juror());
            jurorPool1.setStatus(jurorStatus);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setIsActive(true);
            jurorPools.add(jurorPool1);

            // create juror pools on call
            final List<JurorPool> jurorPoolsOnCall = new ArrayList<>();
            JurorPool jurorPool2 = new JurorPool();
            jurorPool2.setJuror(new Juror());
            jurorPool2.setStatus(jurorStatus);
            jurorPool2.setPool(poolRequest);
            jurorPool2.setIsActive(true);
            jurorPools.add(jurorPool2);

            // create juror pools not in attendance
            final List<JurorPool> jurorPoolsNotInAttendance = new ArrayList<>();
            JurorPool jurorPool3 = new JurorPool();
            jurorPool3.setJuror(new Juror());
            jurorPool3.setStatus(jurorStatus);
            jurorPool3.setPool(poolRequest);
            jurorPool3.setIsActive(true);
            jurorPools.add(jurorPool3);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(TestConstants.VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPools);

            when(jurorPoolRepository.findJurorsOnCallAtCourtLocation(TestConstants.VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPoolsOnCall);

            when(jurorPoolRepository.findJurorsNotInAttendanceAtCourtLocation(
                TestConstants.VALID_COURT_LOCATION, pools))
                .thenReturn(jurorPoolsNotInAttendance);

            // in attendance
            Tuple t1 = mock(Tuple.class);
            mockQueryResultDismissal(t1, "FIRSTNAME1", "LASTNAME1", "In attendance",
                LocalTime.of(9, 30), now().toString(), now().minusDays(7));
            // on call
            Tuple t2 = mock(Tuple.class);
            mockQueryResultDismissal(t2, "FIRSTNAME2", "LASTNAME2", "On call",
                null, "On call", now().minusDays(7));
            // not in attendance
            Tuple t3 = mock(Tuple.class);
            mockQueryResultDismissal(t3, "FIRSTNAME3", "LASTNAME3", "Other",
                null, now().plusDays(2).toString(), now().minusDays(7));

            List<Tuple> jurorsToDismissTuples = new ArrayList<>();
            jurorsToDismissTuples.add(t1);
            jurorsToDismissTuples.add(t2);
            jurorsToDismissTuples.add(t3);

            when(jurorPoolRepository.getJurorsToDismiss(Mockito.anyList(), Mockito.anyList(),
                Mockito.anyString()))
                .thenReturn(jurorsToDismissTuples);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            JurorsToDismissResponseDto jurorsToDismissResponseDto =
                jurorAppearanceService.retrieveJurorsToDismiss(jurorsToDismissRequestDto);

            assertThat(jurorsToDismissResponseDto.getData()).hasSize(3);
        }

        @Test
        void positiveRequestInAttendanceAndOnCall() {

            List<String> pools = new ArrayList<>();
            pools.add(TestConstants.VALID_POOL_NUMBER);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .build();

            final JurorsToDismissRequestDto jurorsToDismissRequestDto = JurorsToDismissRequestDto.builder()
                .poolNumbers(pools)
                .locationCode(TestConstants.VALID_COURT_LOCATION)
                .includeNotInAttendance(false)
                .includeOnCall(true)
                .numberOfJurorsToDismiss(2)
                .build();

            // create juror pools in attendance
            final List<JurorPool> jurorPools = new ArrayList<>();
            JurorPool jurorPool1 = new JurorPool();
            jurorPool1.setJuror(new Juror());
            jurorPool1.setStatus(jurorStatus);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setIsActive(true);
            jurorPools.add(jurorPool1);

            // create juror pools on call
            final List<JurorPool> jurorPoolsOnCall = new ArrayList<>();
            JurorPool jurorPool2 = new JurorPool();
            jurorPool2.setJuror(new Juror());
            jurorPool2.setStatus(jurorStatus);
            jurorPool2.setPool(poolRequest);
            jurorPool2.setIsActive(true);
            jurorPools.add(jurorPool2);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(TestConstants.VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPools);

            when(jurorPoolRepository.findJurorsOnCallAtCourtLocation(TestConstants.VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPoolsOnCall);

            // in attendance
            Tuple t1 = mock(Tuple.class);
            mockQueryResultDismissal(t1, "FIRSTNAME1", "LASTNAME1", "In attendance",
                LocalTime.of(9, 30), now().toString(), now().minusDays(7));
            // on call
            Tuple t2 = mock(Tuple.class);
            mockQueryResultDismissal(t2, "FIRSTNAME2", "LASTNAME2", "On call",
                null, "On call", now().minusDays(7));

            List<Tuple> jurorsToDismissTuples = new ArrayList<>();
            jurorsToDismissTuples.add(t1);
            jurorsToDismissTuples.add(t2);

            when(jurorPoolRepository.getJurorsToDismiss(Mockito.anyList(), Mockito.anyList(),
                Mockito.anyString()))
                .thenReturn(jurorsToDismissTuples);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            JurorsToDismissResponseDto jurorsToDismissResponseDto =
                jurorAppearanceService.retrieveJurorsToDismiss(jurorsToDismissRequestDto);

            assertThat(jurorsToDismissResponseDto.getData()).hasSize(2);
        }

        @Test
        void positiveRequestInAttendanceAndNotInAttendance() {

            List<String> pools = new ArrayList<>();
            pools.add(TestConstants.VALID_POOL_NUMBER);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .build();

            final JurorsToDismissRequestDto jurorsToDismissRequestDto = JurorsToDismissRequestDto.builder()
                .poolNumbers(pools)
                .locationCode(TestConstants.VALID_COURT_LOCATION)
                .includeNotInAttendance(true)
                .includeOnCall(false)
                .numberOfJurorsToDismiss(2)
                .build();

            // create juror pools in attendance
            final List<JurorPool> jurorPools = new ArrayList<>();
            JurorPool jurorPool1 = new JurorPool();
            jurorPool1.setJuror(new Juror());
            jurorPool1.setStatus(jurorStatus);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setIsActive(true);
            jurorPools.add(jurorPool1);

            // create juror pools not in attendance
            final List<JurorPool> jurorPoolsNotInAttendance = new ArrayList<>();
            JurorPool jurorPool3 = new JurorPool();
            jurorPool3.setJuror(new Juror());
            jurorPool3.setStatus(jurorStatus);
            jurorPool3.setPool(poolRequest);
            jurorPool3.setIsActive(true);
            jurorPools.add(jurorPool3);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(
                TestConstants.VALID_COURT_LOCATION, pools))
                .thenReturn(jurorPools);

            when(jurorPoolRepository.findJurorsNotInAttendanceAtCourtLocation(
                TestConstants.VALID_COURT_LOCATION, pools))
                .thenReturn(jurorPoolsNotInAttendance);

            // in attendance
            Tuple t1 = mock(Tuple.class);
            mockQueryResultDismissal(t1, "FIRSTNAME1", "LASTNAME1", "In attendance",
                LocalTime.of(9, 30), now().toString(), now().minusDays(7));

            // not in attendance
            Tuple t3 = mock(Tuple.class);
            mockQueryResultDismissal(t3, "FIRSTNAME3", "LASTNAME3", "Other",
                null, now().plusDays(2).toString(), now().minusDays(7));

            List<Tuple> jurorsToDismissTuples = new ArrayList<>();
            jurorsToDismissTuples.add(t1);
            jurorsToDismissTuples.add(t3);

            when(jurorPoolRepository.getJurorsToDismiss(Mockito.anyList(), Mockito.anyList(),
                Mockito.anyString()))
                .thenReturn(jurorsToDismissTuples);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            JurorsToDismissResponseDto jurorsToDismissResponseDto =
                jurorAppearanceService.retrieveJurorsToDismiss(jurorsToDismissRequestDto);

            assertThat(jurorsToDismissResponseDto.getData()).hasSize(2);
        }

        @Test
        void positiveRequestInAttendanceOnly() {

            List<String> pools = new ArrayList<>();
            pools.add(TestConstants.VALID_POOL_NUMBER);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .build();

            final JurorsToDismissRequestDto jurorsToDismissRequestDto = JurorsToDismissRequestDto.builder()
                .poolNumbers(pools)
                .locationCode(TestConstants.VALID_COURT_LOCATION)
                .includeNotInAttendance(false)
                .includeOnCall(false)
                .numberOfJurorsToDismiss(1)
                .build();

            // create juror pools in attendance
            JurorPool jurorPool1 = new JurorPool();
            jurorPool1.setJuror(new Juror());
            jurorPool1.setStatus(jurorStatus);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setIsActive(true);

            List<JurorPool> jurorPools = new ArrayList<>();
            jurorPools.add(jurorPool1);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(TestConstants.VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPools);

            // in attendance
            Tuple t1 = mock(Tuple.class);
            mockQueryResultDismissal(t1, "FIRSTNAME1", "LASTNAME1", "In attendance",
                LocalTime.of(9, 30), now().toString(), now().minusDays(7));

            List<Tuple> jurorsToDismissTuples = new ArrayList<>();
            jurorsToDismissTuples.add(t1);

            when(jurorPoolRepository.getJurorsToDismiss(Mockito.anyList(), Mockito.anyList(),
                Mockito.anyString()))
                .thenReturn(jurorsToDismissTuples);

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            JurorsToDismissResponseDto jurorsToDismissResponseDto =
                jurorAppearanceService.retrieveJurorsToDismiss(jurorsToDismissRequestDto);

            assertThat(jurorsToDismissResponseDto.getData()).hasSize(1);
        }

        private void mockQueryResultDismissal(Tuple t1, String firstName, String lastName, String attending,
                                              LocalTime checkInTime, String nextDueAtCourt,
                                              LocalDate serviceStartDate) {
            doReturn(firstName).when(t1).get(0, String.class);
            doReturn(lastName).when(t1).get(1, String.class);
            doReturn(attending).when(t1).get(2, String.class);
            doReturn(checkInTime).when(t1).get(3, LocalTime.class);
            doReturn(nextDueAtCourt).when(t1).get(4, String.class);
            doReturn(serviceStartDate).when(t1).get(5, LocalDate.class);
        }

        @Test
        void negativeNoneFound() {

            List<String> pools = new ArrayList<>();
            pools.add(TestConstants.VALID_POOL_NUMBER);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            final JurorsToDismissRequestDto jurorsToDismissRequestDto = JurorsToDismissRequestDto.builder()
                .poolNumbers(pools)
                .locationCode(TestConstants.VALID_COURT_LOCATION)
                .includeNotInAttendance(true)
                .includeOnCall(true)
                .numberOfJurorsToDismiss(3)
                .build();

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(
                TestConstants.VALID_COURT_LOCATION, pools))
                .thenReturn(new ArrayList<>());

            when(jurorPoolRepository.findJurorsOnCallAtCourtLocation(TestConstants.VALID_COURT_LOCATION,
                pools)).thenReturn(new ArrayList<>());

            when(jurorPoolRepository.findJurorsNotInAttendanceAtCourtLocation(
                TestConstants.VALID_COURT_LOCATION, pools))
                .thenReturn(new ArrayList<>());

            TestUtils.setupAuthentication("415", "COURT_USER", "1");

            JurorsToDismissResponseDto jurorsToDismissResponseDto =
                jurorAppearanceService.retrieveJurorsToDismiss(jurorsToDismissRequestDto);

            assertThat(jurorsToDismissResponseDto.getData().size()).isZero();
        }
    }

    private Juror createJuror(String jurorNumber, int status) {
        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = getJurorPool(juror, status);
        juror.setAssociatedPools(Collections.singleton(jurorPool));

        return juror;
    }

    private UpdateAttendanceDto buildUpdateAttendanceDto(List<String> jurors) {
        UpdateAttendanceDto.CommonData commonData = new UpdateAttendanceDto.CommonData();
        commonData.setStatus(UpdateAttendanceStatus.CHECK_OUT);
        commonData.setAttendanceDate(now());
        commonData.setLocationCode("415");
        commonData.setCheckOutTime(LocalTime.of(17, 30));
        commonData.setSingleJuror(Boolean.FALSE);

        UpdateAttendanceDto request = new UpdateAttendanceDto();
        request.setCommonData(commonData);
        request.setJuror(jurors);

        return request;
    }

    private List<JurorAppearanceResponseDto.JurorAppearanceResponseData> buildAttendanceRecords(
        String jurorNumber, String lastName, LocalTime in, LocalTime out, int status) {

        JurorAppearanceResponseDto.JurorAppearanceResponseData appearanceData = JurorAppearanceResponseDto
            .JurorAppearanceResponseData.builder().jurorNumber(jurorNumber)
            .firstName("TEST")
            .lastName(lastName)
            .checkInTime(in)
            .checkOutTime(out)
            .jurorStatus(status)
            .appStage(CHECKED_IN)
            .build();

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList = new ArrayList<>();
        appearanceDataList.add(appearanceData);

        return appearanceDataList;
    }

    private RetrieveAttendanceDetailsDto buildRetrieveAttendanceDetailsDto(List<String> jurors) {
        RetrieveAttendanceDetailsDto.CommonData commonData = new RetrieveAttendanceDetailsDto.CommonData();
        commonData.setAttendanceDate(now());
        commonData.setLocationCode("415");
        commonData.setTag(RetrieveAttendanceDetailsTag.NOT_CHECKED_OUT);

        return RetrieveAttendanceDetailsDto.builder()
            .commonData(commonData)
            .juror(jurors)
            .build();
    }

    private Appearance buildAppearance(String jurorNumber, LocalTime in, LocalTime out, AppearanceStage appStage) {
        return Appearance.builder()
            .jurorNumber(jurorNumber)
            .attendanceDate(now())
            .courtLocation(getCourtLocation())
            .timeIn(in)
            .timeOut(out)
            .appearanceStage(appStage)
            .build();
    }

    private void mockQueryResultAttendance(Tuple attendanceTuple, String jurorNumber, String firstName, String lastName,
                                           int jurorStatus, LocalTime checkInTime, LocalTime checkOutTime,
                                           Boolean isNoShow, AppearanceStage appStage) {
        doReturn(jurorNumber).when(attendanceTuple).get(0, String.class);
        doReturn(firstName).when(attendanceTuple).get(1, String.class);
        doReturn(lastName).when(attendanceTuple).get(2, String.class);
        doReturn(jurorStatus).when(attendanceTuple).get(3, Integer.class);
        doReturn(checkInTime).when(attendanceTuple).get(4, LocalTime.class);
        doReturn(checkOutTime).when(attendanceTuple).get(5, LocalTime.class);
        doReturn(isNoShow).when(attendanceTuple).get(6, Boolean.class);
        doReturn(appStage).when(attendanceTuple).get(7, AppearanceStage.class);
    }

    private void mockQueryResultAbsent(Tuple absentTuple, String jurorNumber, String firstName, String lastName,
                                       int jurorStatus) {
        doReturn(jurorNumber).when(absentTuple).get(0, String.class);
        doReturn(firstName).when(absentTuple).get(1, String.class);
        doReturn(lastName).when(absentTuple).get(2, String.class);
        doReturn(jurorStatus).when(absentTuple).get(3, Integer.class);
    }

    private JurorPool getJurorPool(Juror juror, int status) {
        JurorPool jurorPool = new JurorPool();
        jurorPool.setJuror(juror);
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(status);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setIsActive(true);
        jurorPool.setOwner(OWNER_415);
        return jurorPool;
    }

    private static CourtLocation getCourtLocation() {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setOwner("415");
        return courtLocation;
    }

    private JurorAppearanceDto buildJurorAppearanceDto() {
        return JurorAppearanceDto.builder()
            .jurorNumber(JUROR_123456789)
            .locationCode(OWNER_415)
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .appearanceStage(CHECKED_IN)
            .build();
    }

    private AddAttendanceDayDto buildAddAttendanceDayDto() {
        return AddAttendanceDayDto.builder()
            .jurorNumber(JUROR_123456789)
            .poolNumber("123456789")
            .locationCode(OWNER_415)
            .attendanceDate(now())
            .checkInTime(LocalTime.of(9, 30))
            .checkOutTime(LocalTime.of(17, 0))
            .build();
    }

    private BureauJwtPayload buildPayload(String owner, List<String> courts) {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("COURT_USER")
            .daysToExpire(89)
            .owner(owner)
            .staff(BureauJwtPayload.Staff.builder().courts(courts).build())
            .build();
    }

    private void retrieveAttendanceDetailsConfirmAttendanceOkayMockSetup(RetrieveAttendanceDetailsDto request) {
        doReturn(Optional.of(getCourtLocation())).when(courtLocationRepository).findById(anyString());

        Tuple t1 = mock(Tuple.class);
        mockQueryResultAttendance(t1, JUROR1, "TEST", "ONE", 2,
            LocalTime.of(9, 30), LocalTime.of(17, 3), Boolean.FALSE, CHECKED_OUT);

        Tuple t2 = mock(Tuple.class);
        mockQueryResultAttendance(t2, JUROR6, "TEST", "SIX", 2,
            LocalTime.of(9, 30), null, Boolean.FALSE, CHECKED_OUT);

        List<Tuple> attendanceTuples = new ArrayList<>();
        attendanceTuples.add(t1);
        attendanceTuples.add(t2);
        doReturn(attendanceTuples).when(appearanceRepository).retrieveAttendanceDetails(request);

        Tuple t3 = mock(Tuple.class);
        mockQueryResultAbsent(t3, JUROR8, "TEST", "EIGHT", 2);

        Tuple t4 = mock(Tuple.class);
        mockQueryResultAbsent(t4, JUROR9, "TEST", "NINE", 2);

        List<Tuple> absentTuples = new ArrayList<>();
        absentTuples.add(t3);
        absentTuples.add(t4);
        doReturn(absentTuples).when(appearanceRepository).retrieveNonAttendanceDetails(request.getCommonData());
    }

    private void retrieveAttendanceDetailsNotCheckedOutOkayMockSetup(RetrieveAttendanceDetailsDto request) {
        doReturn(Optional.of(getCourtLocation())).when(courtLocationRepository).findById(anyString());

        Tuple t1 = mock(Tuple.class);
        mockQueryResultAttendance(t1, JUROR3, "TEST", "THREE",
            3, LocalTime.of(9, 30), null, Boolean.FALSE, CHECKED_IN);

        Tuple t2 = mock(Tuple.class);
        mockQueryResultAttendance(t2, JUROR6, "TEST", "SIX",
            2, LocalTime.of(9, 30), null, Boolean.FALSE, CHECKED_IN);

        List<Tuple> attendanceTuples = new ArrayList<>();
        attendanceTuples.add(t1);
        attendanceTuples.add(t2);
        doReturn(attendanceTuples).when(appearanceRepository).retrieveAttendanceDetails(request);
    }

    private void retrieveAttendanceDetailsJurorNumberOkayMockSetup(RetrieveAttendanceDetailsDto request) {
        doReturn(Optional.of(getCourtLocation())).when(courtLocationRepository).findById(anyString());

        Tuple t1 = mock(Tuple.class);
        mockQueryResultAttendance(t1, JUROR1, "TEST", "ONE", 2,
            LocalTime.of(9, 30), LocalTime.of(17, 3), Boolean.FALSE, CHECKED_OUT);

        Tuple t2 = mock(Tuple.class);
        mockQueryResultAttendance(t2, JUROR2, "TEST", "TWO", 4,
            LocalTime.of(9, 30), LocalTime.of(17, 30), Boolean.FALSE, CHECKED_OUT);

        Tuple t3 = mock(Tuple.class);
        mockQueryResultAttendance(t3, JUROR3, "TEST", "THREE",
            3, LocalTime.of(9, 30), null, Boolean.FALSE, CHECKED_IN);

        Tuple t4 = mock(Tuple.class);
        mockQueryResultAttendance(t4, JUROR6, "TEST", "SIX",
            2, LocalTime.of(9, 30), null, Boolean.FALSE, CHECKED_IN);

        List<Tuple> attendanceTuples = new ArrayList<>();
        attendanceTuples.add(t1);
        attendanceTuples.add(t2);
        attendanceTuples.add(t3);
        attendanceTuples.add(t4);
        doReturn(attendanceTuples).when(appearanceRepository).retrieveAttendanceDetails(request);
    }

    private void updateAttendanceCheckInMockSetUp(LocalTime checkInTime) {
        Tuple t1 = mock(Tuple.class);
        mockQueryResultAttendance(t1, JUROR1, "TEST", "ONE",
            2, checkInTime, null, Boolean.FALSE, null);

        Tuple t2 = mock(Tuple.class);
        mockQueryResultAttendance(t2, JUROR2, "TEST", "TWO",
            4, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        Tuple t3 = mock(Tuple.class);
        mockQueryResultAttendance(t3, JUROR3, "TEST", "THREE",
            3, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        Tuple t5 = mock(Tuple.class);
        mockQueryResultAttendance(t5, JUROR5, "TEST", "FIVE",
            2, checkInTime, null, Boolean.FALSE, null);

        Tuple t6 = mock(Tuple.class);
        mockQueryResultAttendance(t6, JUROR6, "TEST", "SIX",
            2, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        Tuple t7 = mock(Tuple.class);
        mockQueryResultAttendance(t7, JUROR7, "TEST", "SEVEN",
            2, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        List<Tuple> attendanceTuples = new ArrayList<>();
        attendanceTuples.add(t1);
        attendanceTuples.add(t2);
        attendanceTuples.add(t3);
        attendanceTuples.add(t5);
        attendanceTuples.add(t6);
        attendanceTuples.add(t7);
        doReturn(attendanceTuples).when(appearanceRepository)
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));

        doReturn(Optional.of(createJuror(JUROR1, IJurorStatus.RESPONDED))).when(jurorRepository).findById(JUROR1);
        doReturn(Optional.of(createJuror(JUROR2, IJurorStatus.JUROR))).when(jurorRepository).findById(JUROR2);
        doReturn(Optional.of(createJuror(JUROR3, IJurorStatus.PANEL))).when(jurorRepository).findById(JUROR3);
        doReturn(Optional.of(createJuror(JUROR5, IJurorStatus.RESPONDED))).when(jurorRepository).findById(JUROR5);
        doReturn(Optional.of(createJuror(JUROR6, IJurorStatus.RESPONDED))).when(jurorRepository).findById(JUROR6);
        doReturn(Optional.of(createJuror(JUROR7, IJurorStatus.RESPONDED))).when(jurorRepository).findById(JUROR7);

        JurorPool jurorPool1 = getJurorPool(createJuror(JUROR1, IJurorStatus.RESPONDED), IJurorStatus.RESPONDED);
        JurorPool jurorPool2 = getJurorPool(createJuror(JUROR2, IJurorStatus.JUROR), IJurorStatus.JUROR);
        JurorPool jurorPool3 = getJurorPool(createJuror(JUROR3, IJurorStatus.PANEL), IJurorStatus.PANEL);
        JurorPool jurorPool5 = getJurorPool(createJuror(JUROR5, IJurorStatus.RESPONDED), IJurorStatus.RESPONDED);
        JurorPool jurorPool6 = getJurorPool(createJuror(JUROR6, IJurorStatus.RESPONDED), IJurorStatus.RESPONDED);
        JurorPool jurorPool7 = getJurorPool(createJuror(JUROR7, IJurorStatus.RESPONDED), IJurorStatus.RESPONDED);
        doReturn(Collections.singletonList(jurorPool1)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR1, true);
        doReturn(Collections.singletonList(jurorPool2)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR2, true);
        doReturn(Collections.singletonList(jurorPool3)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR3, true);
        doReturn(Collections.singletonList(jurorPool5)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR5, true);
        doReturn(Collections.singletonList(jurorPool6)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR6, true);
        doReturn(Collections.singletonList(jurorPool7)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR7, true);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance1 = buildAttendanceRecords(JUROR1,
            "ONE", null, null, IJurorStatus.JUROR);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance2 = buildAttendanceRecords(JUROR2,
            "TWO", null, null, IJurorStatus.JUROR);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance3 = buildAttendanceRecords(JUROR3,
            "THREE", null, null, IJurorStatus.PANEL);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance5 = buildAttendanceRecords(JUROR5,
            "FIVE", null, null, IJurorStatus.RESPONDED);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance6 = buildAttendanceRecords(JUROR6,
            "SIX", null, null, IJurorStatus.RESPONDED);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance7 = buildAttendanceRecords(JUROR7,
            "SEVEN", null, null, IJurorStatus.RESPONDED);

        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR1)).thenReturn(jurorAppearance1);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR2)).thenReturn(jurorAppearance2);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR3)).thenReturn(jurorAppearance3);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR5)).thenReturn(jurorAppearance5);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR6)).thenReturn(jurorAppearance6);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR7)).thenReturn(jurorAppearance7);

        CourtLocation courtLocation = mock(CourtLocation.class);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(OWNER_415);

        when(appearanceRepository.findById(new AppearanceId(JUROR1, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR1, null, null, CHECKED_IN)));
        when(appearanceRepository.findById(new AppearanceId(JUROR2, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR2, null, null, CHECKED_IN)));
        when(appearanceRepository.findById(new AppearanceId(JUROR3, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR3, null, null, CHECKED_IN)));
        when(appearanceRepository.findById(new AppearanceId(JUROR5, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR5, null, null, CHECKED_IN)));
        when(appearanceRepository.findById(new AppearanceId(JUROR6, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR6, null, null, CHECKED_IN)));
        when(appearanceRepository.findById(new AppearanceId(JUROR7, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR7, null, null, CHECKED_IN)));

        when(appearanceRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());
    }

    private void updateAttendanceCheckInAndOutMockSetUp() {
        doReturn(Optional.of(createJuror(JUROR7, IJurorStatus.RESPONDED))).when(jurorRepository).findById(JUROR7);

        JurorPool jurorPool7 = getJurorPool(createJuror(JUROR7, IJurorStatus.RESPONDED), IJurorStatus.RESPONDED);
        doReturn(Collections.singletonList(jurorPool7)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR7, true);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance7 = buildAttendanceRecords(JUROR7,
            "SEVEN", null, null, IJurorStatus.RESPONDED);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR7)).thenReturn(jurorAppearance7);

        CourtLocation courtLocation = mock(CourtLocation.class);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(OWNER_415);

        when(appearanceRepository.findById(new AppearanceId(JUROR7, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR7, null, null, CHECKED_IN)));

        when(appearanceRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());
    }

    private void updateAttendanceCheckOutMockSetup() {
        Tuple t1 = mock(Tuple.class);
        mockQueryResultAttendance(t1, JUROR2, "TEST", "TWO",
            4, LocalTime.of(9, 30), null, Boolean.FALSE, CHECKED_IN);

        Tuple t2 = mock(Tuple.class);
        mockQueryResultAttendance(t2, JUROR3, "TEST", "THREE",
            3, LocalTime.of(9, 30), null, Boolean.FALSE, CHECKED_IN);

        Tuple t3 = mock(Tuple.class);
        mockQueryResultAttendance(t3, JUROR6, "TEST", "SIX",
            2, LocalTime.of(9, 30), null, Boolean.FALSE, CHECKED_IN);

        Tuple t4 = mock(Tuple.class);
        mockQueryResultAttendance(t4, JUROR7, "TEST", "SEVEN",
            2, LocalTime.of(9, 53), null, Boolean.FALSE, CHECKED_IN);

        List<Tuple> attendanceTuples = new ArrayList<>();
        attendanceTuples.add(t1);
        attendanceTuples.add(t2);
        attendanceTuples.add(t3);
        attendanceTuples.add(t4);
        doReturn(attendanceTuples).when(appearanceRepository)
            .retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class));

        doReturn(Optional.of(createJuror(JUROR2, IJurorStatus.JUROR))).when(jurorRepository).findById(JUROR2);
        doReturn(Optional.of(createJuror(JUROR3, IJurorStatus.PANEL))).when(jurorRepository).findById(JUROR3);
        doReturn(Optional.of(createJuror(JUROR6, IJurorStatus.RESPONDED))).when(jurorRepository).findById(JUROR6);
        doReturn(Optional.of(createJuror(JUROR7, IJurorStatus.RESPONDED))).when(jurorRepository).findById(JUROR7);

        JurorPool jurorPool2 = getJurorPool(createJuror(JUROR2, IJurorStatus.JUROR), IJurorStatus.JUROR);
        JurorPool jurorPool3 = getJurorPool(createJuror(JUROR3, IJurorStatus.PANEL), IJurorStatus.PANEL);
        JurorPool jurorPool6 = getJurorPool(createJuror(JUROR6, IJurorStatus.RESPONDED), IJurorStatus.RESPONDED);
        JurorPool jurorPool7 = getJurorPool(createJuror(JUROR7, IJurorStatus.RESPONDED), IJurorStatus.RESPONDED);
        doReturn(Collections.singletonList(jurorPool2)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR2, true);
        doReturn(Collections.singletonList(jurorPool3)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR3, true);
        doReturn(Collections.singletonList(jurorPool6)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR6, true);
        doReturn(Collections.singletonList(jurorPool7)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR7, true);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance2 = buildAttendanceRecords(JUROR2,
            "TWO", LocalTime.of(9, 30), null, IJurorStatus.JUROR);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance3 = buildAttendanceRecords(JUROR3,
            "THREE", LocalTime.of(9, 30), null, IJurorStatus.PANEL);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance6 = buildAttendanceRecords(JUROR6,
            "SIX", LocalTime.of(9, 30), null, IJurorStatus.RESPONDED);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance7 = buildAttendanceRecords(JUROR7,
            "SEVEN", LocalTime.of(9, 30), null, IJurorStatus.RESPONDED);

        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR2)).thenReturn(jurorAppearance2);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR3)).thenReturn(jurorAppearance3);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR6)).thenReturn(jurorAppearance6);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR7)).thenReturn(jurorAppearance7);

        CourtLocation courtLocation = mock(CourtLocation.class);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(OWNER_415);

        when(appearanceRepository.findById(new AppearanceId(JUROR2, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR2, LocalTime.of(9, 30), null, CHECKED_IN)));
        when(appearanceRepository.findById(new AppearanceId(JUROR3, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR3, LocalTime.of(9, 30), null, CHECKED_IN)));
        when(appearanceRepository.findById(new AppearanceId(JUROR6, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR6, LocalTime.of(9, 30), null, CHECKED_IN)));
        when(appearanceRepository.findById(new AppearanceId(JUROR7, now(), courtLocation)))
            .thenReturn(Optional.of(buildAppearance(JUROR7, LocalTime.of(9, 30), null, CHECKED_IN)));

        when(appearanceRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());
    }

    private void updateAttendanceConfirmAttendanceMockSetup(LocalTime checkInTime) {
        // checked in jurors
        Tuple t1 = mock(Tuple.class);
        mockQueryResultAttendance(t1, JUROR1, "TEST", "ONE",
            2, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        Tuple t6 = mock(Tuple.class);
        mockQueryResultAttendance(t6, JUROR6, "TEST", "SIX",
            2, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        Tuple t7 = mock(Tuple.class);
        mockQueryResultAttendance(t7, JUROR7, "TEST", "SEVEN",
            2, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        List<Tuple> checkedInJurorsTuples = new ArrayList<>();
        checkedInJurorsTuples.add(t1);
        checkedInJurorsTuples.add(t6);
        checkedInJurorsTuples.add(t7);
        when(appearanceRepository.retrieveAttendanceDetails(any(RetrieveAttendanceDetailsDto.class)))
            .thenReturn(checkedInJurorsTuples);

        List<Appearance> checkedInAttendances = new ArrayList<>();
        Appearance appearance1 = buildAppearance(JUROR1, checkInTime, null, EXPENSE_ENTERED);
        Appearance appearance6 = buildAppearance(JUROR6, checkInTime, null, EXPENSE_ENTERED);
        Appearance appearance7 = buildAppearance(JUROR7, checkInTime, null, EXPENSE_ENTERED);
        checkedInAttendances.add(appearance1);
        checkedInAttendances.add(appearance6);
        checkedInAttendances.add(appearance7);

        CourtLocation courtLocation = mock(CourtLocation.class);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(OWNER_415);

        List<AppearanceId> appearanceIds = new ArrayList<>();
        AppearanceId appearanceId1 = new AppearanceId(JUROR1, now(), courtLocation);
        AppearanceId appearanceId6 = new AppearanceId(JUROR6, now(), courtLocation);
        AppearanceId appearanceId7 = new AppearanceId(JUROR7, now(), courtLocation);
        appearanceIds.add(appearanceId1);
        appearanceIds.add(appearanceId6);
        appearanceIds.add(appearanceId7);

        when(appearanceRepository.findAllById(appearanceIds)).thenReturn(checkedInAttendances);

        // absent jurors
        Tuple t2 = mock(Tuple.class);
        mockQueryResultAttendance(t2, JUROR2, "TEST", "TWO",
            2, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        Tuple t3 = mock(Tuple.class);
        mockQueryResultAttendance(t3, JUROR3, "TEST", "THREE",
            2, checkInTime, null, Boolean.FALSE, CHECKED_IN);

        List<Tuple> absentJurorsTuples = new ArrayList<>();
        absentJurorsTuples.add(t2);
        absentJurorsTuples.add(t3);
        when(appearanceRepository.retrieveNonAttendanceDetails(any())).thenReturn(absentJurorsTuples);

        when(appearanceRepository.saveAllAndFlush(any())).thenReturn(new ArrayList<>());
    }

    private void deleteAttendanceMockSetup(Boolean noAttendanceRecord) {
        when(courtLocationRepository.findById(anyString())).thenReturn(Optional.of(getCourtLocation()));

        when(jurorRepository.findById(JUROR1)).thenReturn(Optional.of(createJuror(JUROR1, IJurorStatus.RESPONDED)));

        JurorPool jurorPool1 = getJurorPool(createJuror(JUROR1, IJurorStatus.RESPONDED), IJurorStatus.RESPONDED);
        when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR1, true))
            .thenReturn(Collections.singletonList(jurorPool1));

        if (noAttendanceRecord) {
            when(appearanceRepository.findById(any())).thenReturn(Optional.empty());
        } else {
            when(appearanceRepository.findById(any()))
                .thenReturn(Optional.of(buildAppearance(JUROR1, null, null, CHECKED_IN)));
        }
        doNothing().when(appearanceRepository).deleteById(any(AppearanceId.class));
    }


    @DisplayName("public void addNonAttendance(JurorNonAttendanceDto request)")
    @Nested
    class NonAttendance {

        private static String courtOwner = "415";
        private static String courtLocationCode = "415";
        private static String jurorNumber = "111111111";
        private static String poolNumber = "415230101";
        private static String username = "COURT_USER";

        @Test
        void positiveNonAttendanceAdded() {

            TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(courtOwner);
            when(courtLocationRepository.findByLocCode(courtLocationCode)).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(poolNumber)
                .returnDate(now().minusDays(20))
                .build();

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(courtOwner);
            jurorPool.setPool(poolRequest);

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);
            juror.setFinancialLoss(BigDecimal.valueOf(63.90));
            jurorPool.setJuror(juror);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(jurorNumber)
                .nonAttendanceDate(now())
                .poolNumber(poolNumber)
                .locationCode(courtLocationCode)
                .build();

            LocalDate nonAttendanceDate = now();
            doReturn(Optional.empty()).when(appearanceRepository)
                .findByJurorNumberAndPoolNumberAndAttendanceDate(jurorNumber,
                    poolNumber, nonAttendanceDate);

            jurorAppearanceService.addNonAttendance(request);

            verify(courtLocationRepository, times(1)).findByLocCode(courtLocationCode);
            verify(courtLocationRepository, times(1)).findById(courtLocationCode);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
            verify(appearanceRepository, times(1)).findByJurorNumberAndPoolNumberAndAttendanceDate(jurorNumber,
                poolNumber, nonAttendanceDate);
            verify(appearanceRepository, times(1)).saveAndFlush(any());
        }

        @Test
        void happyNonAttendanceJurorNoShow() {

            TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(courtOwner);
            when(courtLocationRepository.findByLocCode(courtLocationCode)).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(poolNumber)
                .returnDate(now().minusDays(20))
                .build();

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(courtOwner);
            jurorPool.setPool(poolRequest);

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);
            juror.setFinancialLoss(BigDecimal.valueOf(63.90));
            jurorPool.setJuror(juror);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);

            Appearance appearance = new Appearance();
            appearance.setNoShow(true);
            appearance.setAttendanceType(AttendanceType.ABSENT);

            LocalDate nonAttendanceDate = now();
            doReturn(Optional.of(appearance)).when(appearanceRepository)
                .findByJurorNumberAndPoolNumberAndAttendanceDate(jurorNumber,
                    poolNumber, nonAttendanceDate);

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(jurorNumber)
                .nonAttendanceDate(now())
                .poolNumber(poolNumber)
                .locationCode(courtLocationCode)
                .build();

            jurorAppearanceService.addNonAttendance(request);

            ArgumentCaptor<Appearance> savedAppearanceCaptor = ArgumentCaptor.forClass(Appearance.class);
            verify(courtLocationRepository, times(1)).findByLocCode(courtLocationCode);
            verify(courtLocationRepository, times(1)).findById(courtLocationCode);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
            verify(appearanceRepository, times(1))
                .findByJurorNumberAndPoolNumberAndAttendanceDate(jurorNumber,
                    poolNumber, nonAttendanceDate);
            verify(appearanceRepository, times(1)).delete(appearance);
            //verify(appearanceRepository, times(1)).saveAndFlush(appearanceSaved);

            verify(appearanceRepository, times(1)).saveAndFlush(
                savedAppearanceCaptor.capture());


            Appearance appearanceSaved = savedAppearanceCaptor.getValue();
            assertThat(appearanceSaved.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(appearanceSaved.getPoolNumber()).isEqualTo(poolNumber);
            assertThat(appearanceSaved.getAttendanceDate()).isEqualTo(nonAttendanceDate);
            assertThat(appearanceSaved.getCourtLocation()).isEqualTo(courtLocation);
            assertThat(appearanceSaved.getNonAttendanceDay()).isTrue();
            assertThat(appearanceSaved.getAttendanceType()).isEqualTo(AttendanceType.NON_ATTENDANCE);
            assertThat(appearanceSaved.getAppearanceStage()).isEqualTo(EXPENSE_ENTERED);
            assertThat(appearanceSaved.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
            assertThat(appearanceSaved.getCreatedBy()).isEqualTo(username);

            verify(jurorExpenseService, times(1))
                .applyDefaultExpenses(savedAppearanceCaptor.capture(), eq(juror));

            assertThat(savedAppearanceCaptor.getValue()).isEqualTo(appearanceSaved);
        }

        @Test
        void negativeNonAttendanceCourtNotFound() {

            TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

            when(courtLocationRepository.findByLocCode(courtLocationCode)).thenReturn(Optional.empty());

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(jurorNumber)
                .nonAttendanceDate(now())
                .poolNumber(poolNumber)
                .locationCode(courtLocationCode)
                .build();

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                    jurorAppearanceService.addNonAttendance(request))
                .withMessage("Court location " + courtLocationCode + " not found");

            verify(courtLocationRepository, times(1)).findByLocCode(courtLocationCode);
            verify(courtLocationRepository, times(0)).findById(Mockito.anyString());
            verify(jurorPoolRepository, times(0))
                .findByJurorJurorNumberAndPoolPoolNumber(Mockito.anyString(), Mockito.anyString());
            verify(appearanceRepository, times(0)).findByJurorNumberAndPoolNumberAndAttendanceDate(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }

        @Test
        void negativeNonAttendanceForbiddenCourtUser() {

            TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("416"); // different to user owner
            when(courtLocationRepository.findByLocCode("416")).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById("416")).thenReturn(Optional.of(courtLocation));

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(jurorNumber)
                .nonAttendanceDate(now())
                .poolNumber(poolNumber)
                .locationCode("416")
                .build();

            assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
                    jurorAppearanceService.addNonAttendance(request))
                .withMessage("Cannot access court details for this location "
                    + "416");

            verify(courtLocationRepository, times(1)).findByLocCode("416");
            verify(courtLocationRepository, times(1)).findById("416");
            verify(jurorPoolRepository, times(0))
                .findByJurorJurorNumberAndPoolPoolNumber(Mockito.anyString(), Mockito.anyString());
            verify(appearanceRepository, times(0)).findByJurorNumberAndPoolNumberAndAttendanceDate(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }

        @Test
        void negativeNonAttendancePoolNotFound() {

            TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(courtOwner);
            when(courtLocationRepository.findByLocCode(courtLocationCode)).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            doReturn(null).when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(jurorNumber)
                .nonAttendanceDate(now())
                .poolNumber(poolNumber)
                .locationCode(courtLocationCode)
                .build();

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                    jurorAppearanceService.addNonAttendance(request))
                .withMessage("Juror not found in Pool " + request.getPoolNumber());

            verify(courtLocationRepository, times(1)).findByLocCode(courtLocationCode);
            verify(courtLocationRepository, times(1)).findById(courtLocationCode);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
            verify(appearanceRepository, times(0)).findByJurorNumberAndPoolNumberAndAttendanceDate(Mockito.anyString(),
                Mockito.anyString(), Mockito.any());
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }

        @Test
        void negativeNonAttendanceJurorInAttendance() {

            TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(courtOwner);
            when(courtLocationRepository.findByLocCode(courtLocationCode)).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById(Mockito.any())).thenReturn(Optional.of(courtLocation));

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(poolNumber)
                .returnDate(now().minusDays(20))
                .build();

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(courtOwner);
            jurorPool.setPool(poolRequest);

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);
            juror.setFinancialLoss(BigDecimal.valueOf(63.90));
            jurorPool.setJuror(juror);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);

            Appearance appearance = new Appearance();
            appearance.setAppearanceStage(CHECKED_IN);

            LocalDate nonAttendanceDate = now();
            doReturn(Optional.of(appearance)).when(appearanceRepository)
                .findByJurorNumberAndPoolNumberAndAttendanceDate(jurorNumber,
                    poolNumber, nonAttendanceDate);

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(jurorNumber)
                .nonAttendanceDate(now())
                .poolNumber(poolNumber)
                .locationCode(courtLocationCode)
                .build();

            MojException.BusinessRuleViolation exception =
                assertThrows(MojException.BusinessRuleViolation.class,
                    () -> jurorAppearanceService.addNonAttendance(request), "verify exception is thrown");

            assertThat(exception.getMessage()).isEqualTo("Juror " + jurorNumber + " already has an "
                + "attendance record for the date " + nonAttendanceDate);

            assertThat(exception.getErrorCode()).isEqualTo(ATTENDANCE_RECORD_ALREADY_EXISTS);

            verify(courtLocationRepository, times(1)).findByLocCode(courtLocationCode);
            verify(courtLocationRepository, times(1)).findById(courtLocationCode);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
            verify(appearanceRepository, times(1)).findByJurorNumberAndPoolNumberAndAttendanceDate(jurorNumber,
                poolNumber, nonAttendanceDate);
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("void realignAttendanceType(Appearance appearance)")
    class RealignAttendanceType {

        private Appearance mockAppearance(LocalTime timeIn, LocalTime timeOut,
                                          boolean isFulLDay,
                                          Boolean noShow, AttendanceType attendanceType,
                                          boolean isLongTrialDay) {

            Appearance appearance = mock(Appearance.class);
            when(appearance.getTimeIn()).thenReturn(timeIn);
            when(appearance.getTimeOut()).thenReturn(timeOut);
            when(appearance.getNoShow()).thenReturn(noShow);
            when(appearance.getAttendanceType()).thenReturn(attendanceType);

            LocalDate localDate = now();
            when(appearance.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(appearance.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            when(appearance.getAttendanceDate()).thenReturn(localDate);
            when(appearance.isFullDay()).thenReturn(isFulLDay);


            when(jurorExpenseService.isLongTrialDay(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER,
                localDate
            )).thenReturn(isLongTrialDay);
            return appearance;
        }

        @Test
        void positiveFulLDay() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                true,
                Boolean.FALSE,
                null,
                false);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, times(1))
                .setAttendanceType(AttendanceType.FULL_DAY);
        }

        @Test
        void positiveFullDayLong() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                true,
                Boolean.FALSE,
                null,
                true);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, times(1))
                .setAttendanceType(AttendanceType.FULL_DAY_LONG_TRIAL);
        }

        @Test
        void positiveHalfDay() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                false,
                Boolean.FALSE,
                null,
                false);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, times(1))
                .setAttendanceType(AttendanceType.HALF_DAY);

        }

        @Test
        void positiveHalfDayLong() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                false,
                Boolean.FALSE,
                null,
                true);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, times(1))
                .setAttendanceType(AttendanceType.HALF_DAY_LONG_TRIAL);
        }

        @Test
        void positiveNonAttendanceDay() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                false,
                Boolean.FALSE,
                AttendanceType.NON_ATTENDANCE_LONG_TRIAL,
                false);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, times(1))
                .setAttendanceType(AttendanceType.NON_ATTENDANCE);
        }

        @Test
        void positiveAbsent() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                false,
                Boolean.FALSE,
                AttendanceType.NON_ATTENDANCE,
                true);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, times(1))
                .setAttendanceType(AttendanceType.NON_ATTENDANCE_LONG_TRIAL);
        }

        @Test
        void positiveNoTimeIn() {
            Appearance appearance = mockAppearance(
                null,
                LocalTime.of(17, 30),
                false,
                Boolean.FALSE,
                AttendanceType.NON_ATTENDANCE,
                true);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, never()).setAttendanceType(any());
        }

        @Test
        void positiveNoTimeOut() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                null,
                false,
                Boolean.FALSE,
                AttendanceType.NON_ATTENDANCE,
                true);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, never()).setAttendanceType(any());
        }

        @Test
        void positiveIsNoShow() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                false,
                Boolean.TRUE,
                AttendanceType.NON_ATTENDANCE,
                true);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, never()).setAttendanceType(any());
        }

        @Test
        void positiveIsAbsent() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                false,
                Boolean.FALSE,
                AttendanceType.ABSENT,
                true);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, never()).setAttendanceType(any());
        }
    }
}
