package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
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
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceServiceImpl;

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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.APPEARANCE_CONFIRMED;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_IN;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_OUT;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.LawOfDemeter", "PMD.NcssCount"})
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
    void testCheckInJurorHappy() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
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

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
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
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
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
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
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
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
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
        appearance.setAppearanceStage(APPEARANCE_CONFIRMED);
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
        appearance.setAppearanceStage(APPEARANCE_CONFIRMED);
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
        MojException.NotImplemented exception = assertThrows(MojException.NotImplemented.class,
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
        MojException.NotImplemented exception = assertThrows(MojException.NotImplemented.class,
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

    private BureauJWTPayload buildPayload(String owner, List<String> courts) {
        return BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("COURT_USER")
            .daysToExpire(89)
            .owner(owner)
            .staff(BureauJWTPayload.Staff.builder().courts(courts).build())
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

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
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

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
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

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
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

        List<Appearance> checkedInAttendances  = new ArrayList<>();
        Appearance appearance1 = buildAppearance(JUROR1, checkInTime, null, APPEARANCE_CONFIRMED);
        Appearance appearance6 = buildAppearance(JUROR6, checkInTime, null, APPEARANCE_CONFIRMED);
        Appearance appearance7 = buildAppearance(JUROR7, checkInTime, null, APPEARANCE_CONFIRMED);
        checkedInAttendances.add(appearance1);
        checkedInAttendances.add(appearance6);
        checkedInAttendances.add(appearance7);

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");

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
            final List<JurorPool> jurorPools  = new ArrayList<>();
            JurorPool jurorPool1 = new JurorPool();
            jurorPool1.setJuror(new Juror());
            jurorPool1.setStatus(jurorStatus);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setIsActive(true);
            jurorPools.add(jurorPool1);

            // create juror pools on call
            final List<JurorPool> jurorPoolsOnCall  = new ArrayList<>();
            JurorPool jurorPool2 = new JurorPool();
            jurorPool2.setJuror(new Juror());
            jurorPool2.setStatus(jurorStatus);
            jurorPool2.setPool(poolRequest);
            jurorPool2.setIsActive(true);
            jurorPools.add(jurorPool2);

            // create juror pools not in attendance
            final List<JurorPool> jurorPoolsNotInAttendance  = new ArrayList<>();
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

            assertEquals(3, jurorsToDismissResponseDto.getData().size());
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
            final List<JurorPool> jurorPools  = new ArrayList<>();
            JurorPool jurorPool1 = new JurorPool();
            jurorPool1.setJuror(new Juror());
            jurorPool1.setStatus(jurorStatus);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setIsActive(true);
            jurorPools.add(jurorPool1);

            // create juror pools on call
            final List<JurorPool> jurorPoolsOnCall  = new ArrayList<>();
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

            assertEquals(2, jurorsToDismissResponseDto.getData().size());
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
            final List<JurorPool> jurorPools  = new ArrayList<>();
            JurorPool jurorPool1 = new JurorPool();
            jurorPool1.setJuror(new Juror());
            jurorPool1.setStatus(jurorStatus);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setIsActive(true);
            jurorPools.add(jurorPool1);

            // create juror pools not in attendance
            final List<JurorPool> jurorPoolsNotInAttendance  = new ArrayList<>();
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

            assertEquals(2, jurorsToDismissResponseDto.getData().size());
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
            List<JurorPool> jurorPools  = new ArrayList<>();
            JurorPool jurorPool1 = new JurorPool();
            jurorPool1.setJuror(new Juror());
            jurorPool1.setStatus(jurorStatus);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setIsActive(true);
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

            assertEquals(1, jurorsToDismissResponseDto.getData().size());
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

            assertEquals(0, jurorsToDismissResponseDto.getData().size());
        }
    }
}
