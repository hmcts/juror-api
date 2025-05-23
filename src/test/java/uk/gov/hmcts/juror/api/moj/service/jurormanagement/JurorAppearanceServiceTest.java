package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import com.querydsl.core.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.AddAttendanceDayDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.ConfirmAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDateDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsOnTrialResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.UnconfirmedJurorDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.UnconfirmedJurorResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QCourtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusGroup;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.UpdateAttendanceStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.service.AppearanceCreationServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;
import uk.gov.hmcts.juror.api.moj.service.trial.PanelService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.TestConstants.VALID_COURT_LOCATION;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_IN;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.CHECKED_OUT;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.EXPENSE_ENTERED;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.ATTENDANCE_RECORD_ALREADY_EXISTS;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods",
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
    @Mock
    private TrialRepository trialRepository;
    @Mock
    private PanelRepository panelRepository;
    @Mock
    private JurorHistoryServiceImpl jurorHistoryService;
    @Mock
    private JurorPoolService jurorPoolService;
    @Mock
    private PanelService panelService;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AppearanceCreationServiceImpl appearanceCreationService;

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

    private static final String JUROR_POOL_1 = "123456789";

    @BeforeEach
    public void setUp() {
        TestUtils.setUpMockAuthentication("415", "COURT_USER", "1", List.of("415"));
        doReturn(0L).when(appearanceCreationService).getLastVersionNumber(any(), any(), any());
    }


    @Nested
    @DisplayName("boolean isLongTrialDay(List<LocalDate> appearanceDates, LocalDate dateToCheck)")
    class IsLongTrialDay {
        @ParameterizedTest
        @ValueSource(ints = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        })
        void positiveFalse(int offset) {
            assertTest(offset, false);
        }

        @ParameterizedTest
        @ValueSource(ints = {
            10, 11, 12, 13, 14, 15
        })
        void positiveTrue(int offset) {
            assertTest(offset, true);
        }

        private void assertTest(int offset, boolean expectedValue) {
            final LocalDate baseDay = LocalDate.of(2023, 1, 1);
            LocalDate searchDate = baseDay.plusDays(offset);

            List<LocalDate> appearanceDates = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                Appearance appearance = mock(Appearance.class);
                LocalDate localDate = baseDay.plusDays(i);
                doReturn(localDate).when(appearance).getAttendanceDate();
                appearanceDates.add(localDate);
            }

            assertThat(jurorAppearanceService.isLongTrialDay(
                appearanceDates, searchDate))
                .isEqualTo(expectedValue);
        }
    }

    @Test
    void markJurorAsAbsentCourtLocationNotFound() {
        List<String> jurors = new ArrayList<>();
        jurors.add(JUROR1);

        when(courtLocationRepository.findByLocCode(anyString())).thenReturn(Optional.empty());

        UpdateAttendanceDto request = buildUpdateAttendanceDto(jurors);
        request.getCommonData().setStatus(UpdateAttendanceStatus.CONFIRM_ATTENDANCE);
        request.getCommonData().setCheckOutTime(null);
        request.getCommonData().setSingleJuror(Boolean.TRUE);

        Tuple t1 = mock(Tuple.class);
        mockQueryResultAbsent(t1, JUROR8, "TEST", "EIGHT", 2);

        Tuple t2 = mock(Tuple.class);
        mockQueryResultAbsent(t2, JUROR9, "TEST", "NINE", 2);

        List<Tuple> absentTuples = new ArrayList<>();
        absentTuples.add(t1);
        absentTuples.add(t2);

        RetrieveAttendanceDetailsDto dto = buildRetrieveAttendanceDetailsDto(jurors);

        doReturn(absentTuples).when(appearanceRepository).retrieveNonAttendanceDetails(dto.getCommonData());        //
        // invoke actual service method under test
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                jurorAppearanceService.markJurorAsAbsent(buildPayload("415", List.of("415")),
                    request.getCommonData())).as("Court location not found")
            .withMessageContaining("Court location not found");
    }

    @Test
    void addAttendanceDayHappyPath() {
        jurorAppearanceService = spy(jurorAppearanceService);

        doReturn(null).when(jurorAppearanceService).processAppearance(any(), any(), anyBoolean(), anyBoolean());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(JUROR_POOL_1);

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorJurorNumberAndPoolPoolNumber(
                JUROR_123456789, JUROR_POOL_1);

        CourtLocation courtLocation = getCourtLocation();
        poolRequest.setCourtLocation(courtLocation);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(LOC_415);
        AddAttendanceDayDto dto = buildAddAttendanceDayDto();

        final AppearanceId appearanceId = new AppearanceId(JUROR_123456789, dto.getAttendanceDate(),
            courtLocation);
        final Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAttendanceDate(dto.getAttendanceDate());
        appearance.setCourtLocation(courtLocation);
        appearance.setAppearanceStage(CHECKED_OUT);

        doReturn(Optional.of(appearance)).when(appearanceRepository).findById(appearanceId);
        when(jurorRepository.findById(JUROR_123456789)).thenReturn(
            Optional.of(createJuror(JUROR_123456789, IJurorStatus.JUROR)));

        jurorAppearanceService.addAttendanceDay(buildPayload(OWNER_415, Arrays.asList("415", "462", "767")),
            dto);

        ArgumentCaptor<JurorAppearanceDto> appearanceDtoCaptor = ArgumentCaptor.forClass(JurorAppearanceDto.class);
        ArgumentCaptor<BureauJwtPayload> payloadArgumentCaptor = ArgumentCaptor.forClass(BureauJwtPayload.class);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndPoolPoolNumber(JUROR_123456789, "123456789");
        verify(jurorAppearanceService, times(1)).processAppearance(payloadArgumentCaptor.capture(),
            appearanceDtoCaptor.capture(), eq(true), eq(false));
        verify(courtLocationRepository, times(1)).findByLocCode(LOC_415);
        verify(appearanceRepository, times(2)).findById(appearanceId);
        verify(jurorHistoryService, times(1)).createPoolAttendanceHistory(any(), any());
        verify(jurorExpenseService, times(1)).applyDefaultExpenses(anyList());
        verify(appearanceRepository, times(1)).saveAndFlush(any(Appearance.class));

        BureauJwtPayload payload = payloadArgumentCaptor.getValue();
        assertThat(payload.getOwner()).isEqualTo(OWNER_415);

        JurorAppearanceDto appearanceDto = appearanceDtoCaptor.getValue();

        assertThat(appearanceDto).isNotNull();
        assertThat(appearanceDto.getAttendanceDate()).isEqualTo(dto.getAttendanceDate());
        assertThat(appearanceDto.getJurorNumber()).isEqualTo(JUROR_123456789);
        assertThat(appearanceDto.getCheckInTime()).isEqualTo(dto.getCheckInTime());
        assertThat(appearanceDto.getCheckOutTime()).isEqualTo(dto.getCheckOutTime());
        assertThat(appearanceDto.getLocationCode()).isEqualTo(dto.getLocationCode());

    }

    @Test
    void addAttendanceDayBadPayloadDayInFuture() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(JUROR_POOL_1);

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorJurorNumberAndPoolPoolNumber(
                JUROR_123456789, JUROR_POOL_1);

        AddAttendanceDayDto dto = buildAddAttendanceDayDto();
        dto.setAttendanceDate(now().plusDays(1));

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            jurorAppearanceService.addAttendanceDay(buildPayload(OWNER_415, List.of("415", "462", "767")),
                dto)).as("Requested attendance date is in the future.");

        verify(appearanceRepository, never())
            .findByLocCodeAndJurorNumberAndAttendanceDate(any(), any(), any());
        verify(jurorRepository, never()).findByJurorNumber(any());
        verify(jurorPoolRepository, never()).save(any());
    }


    @Test
    void addAttendanceDayWrongAccess() {
        jurorAppearanceService = spy(jurorAppearanceService);

        doReturn(null).when(jurorAppearanceService).processAppearance(any(), any(), anyBoolean(), anyBoolean());
        doReturn(null).when(jurorAppearanceService).updateConfirmAttendance(any(), anyList());

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(JUROR_POOL_1);

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorJurorNumberAndPoolPoolNumber(
                JUROR_123456789, JUROR_POOL_1);

        AddAttendanceDayDto dto = buildAddAttendanceDayDto();

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
                jurorAppearanceService.addAttendanceDay(buildPayload("400", List.of("400")),
                    dto)).as("Invalid access to juror pool")
            .withMessageContaining("Invalid access to juror pool");

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndPoolPoolNumber(JUROR_123456789, "123456789");
        verify(jurorAppearanceService, never()).processAppearance(any(), any(), anyBoolean(), anyBoolean());
        verify(jurorAppearanceService, never()).updateConfirmAttendance(any(), anyList());

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
        poolRequest.setPoolNumber(JUROR_POOL_1);
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        doAnswer(invocation -> invocation.<Appearance>getArgument(0))
                .when(appearanceRepository).save(any());

        JurorAppearanceResponseDto.JurorAppearanceResponseData appearanceData = JurorAppearanceResponseDto
            .JurorAppearanceResponseData.builder().jurorNumber(JUROR_123456789)
            .lastName("LASTNAME")
            .firstName("FIRSTNAME")
            .checkInTime(LocalTime.of(9, 30))
            .jurorStatus(IJurorStatus.RESPONDED)
            .build();

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList = new ArrayList<>();
        appearanceDataList.add(appearanceData);

        when(appearanceRepository.getAppearanceRecords(anyString(), any(), anyString(), any()))
            .thenReturn(appearanceDataList);

        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        jurorAppearanceDto.setCheckOutTime(LocalTime.of(17, 30));

        jurorAppearanceService.processAppearance(buildPayload(OWNER_415, Arrays.asList("415", "462", "767")),
            jurorAppearanceDto, true, false);

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(jurorRepository, times(2))
            .findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
        verify(courtLocationRepository, times(1))
            .findById(LOC_415);
        verify(appearanceRepository, times(1)).save(appearanceArgumentCaptor.capture());

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
        poolRequest.setPoolNumber(JUROR_POOL_1);
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        doAnswer(invocation -> invocation.<Appearance>getArgument(0))
                .when(appearanceRepository).save(any());

        JurorAppearanceResponseDto.JurorAppearanceResponseData appearanceData = JurorAppearanceResponseDto
            .JurorAppearanceResponseData.builder().jurorNumber(JUROR_123456789)
            .lastName("LASTNAME")
            .firstName("FIRSTNAME")
            .checkInTime(LocalTime.of(9, 30))
            .jurorStatus(IJurorStatus.RESPONDED)
            .build();

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList = new ArrayList<>();
        appearanceDataList.add(appearanceData);

        when(appearanceRepository.getAppearanceRecords(anyString(), any(), anyString(), any()))
            .thenReturn(appearanceDataList);
        when(jurorRepository.findByJurorNumber(JUROR_123456789)).thenReturn(createJuror(JUROR_123456789,
                                                                                        IJurorStatus.JUROR));

        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();
        jurorAppearanceService.processAppearance(buildPayload(OWNER_415, Arrays.asList("415", "462", "767")),
            jurorAppearanceDto);

        verify(jurorRepository, times(2))
            .findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
        verify(courtLocationRepository, times(1))
            .findById(LOC_415);
    }

    @Test
    void testCheckInJurorUnhappyDatabaseSaveIssue() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(JUROR_POOL_1);
        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
        jurorPool.setPool(poolRequest);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);
        doAnswer(invocation -> invocation.<Appearance>getArgument(0))
                .when(appearanceRepository).save(any());

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        when(appearanceRepository.getAppearanceRecords(anyString(), any(), anyString(), any()))
            .thenReturn(new ArrayList<>());

        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();

        assertThatExceptionOfType(MojException.InternalServerError.class).isThrownBy(() ->
            jurorAppearanceService.processAppearance(buildPayload(OWNER_415, Arrays.asList("415", "462", "767")),
                jurorAppearanceDto));

        verify(jurorRepository, times(2))
            .findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
        verify(courtLocationRepository, times(1))
            .findById(LOC_415);
    }

    @Test
    void testCheckInJurorInvalidJurorStatus() {
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_123456789);

        JurorPool jurorPool = getJurorPool(juror, IJurorStatus.DISQUALIFIED);
        juror.setAssociatedPools(Collections.singleton(jurorPool));
        CourtLocation courtLocation = getCourtLocation();

        doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        JurorAppearanceDto jurorAppearanceDto = buildJurorAppearanceDto();

        assertThatExceptionOfType(MojException.BusinessRuleViolation.class).isThrownBy(() ->
            jurorAppearanceService.processAppearance(buildPayload("415", Arrays.asList("415", "462", "767")),
                jurorAppearanceDto));

        verify(jurorRepository, times(1))
            .findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
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
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(CHECKED_IN);
        doReturn(Optional.of(appearance)).when(appearanceRepository)
            .findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check in a juror who is already checked in")
            .withMessageContaining("Juror 123456789 has already checked in");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
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
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(CHECKED_OUT);
        doReturn(Optional.of(appearance)).when(appearanceRepository)
            .findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check in a juror who is already checked out")
            .withMessageContaining("Juror 123456789 has already checked out");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
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
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(EXPENSE_ENTERED);
        doReturn(Optional.of(appearance)).when(appearanceRepository)
            .findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check in a juror who is already confirmed attendance")
            .withMessageContaining("Juror 123456789 has already confirmed their attendance");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
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
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(CHECKED_OUT);
        doReturn(Optional.of(appearance)).when(appearanceRepository)
            .findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check out a juror who is already checked out")
            .withMessageContaining("Juror 123456789 has already checked out");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
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
        doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR_123456789);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());
        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_123456789);
        appearance.setAppearanceStage(EXPENSE_ENTERED);
        doReturn(Optional.of(appearance)).when(appearanceRepository)
            .findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR_123456789, now());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                jurorAppearanceService.processAppearance(buildPayload(OWNER_415, List.of(LOC_415)),
                    jurorAppearanceDto)).as("Cannot check out a juror who has already confirmed attendance")
            .withMessageContaining("Juror 123456789 has already confirmed their attendance");

        verify(jurorRepository, times(1)).findById(JUROR_123456789);
        verify(jurorPoolService, timeout(1)).getJurorPoolFromUser(JUROR_123456789);
        verify(courtLocationRepository, times(1)).findById(LOC_415);
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testGetAppearancesNoRecordsFound() {
        CourtLocation courtLocation = getCourtLocation();
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(anyString());

        doReturn(new ArrayList<Tuple>()).when(appearanceRepository).getAppearanceRecords(anyString(),
            any(), anyString(), any());

        JurorAppearanceResponseDto jurorAppearanceResponseDto =
            jurorAppearanceService.getAppearanceRecords(LOC_415, now(),
                buildPayload(OWNER_415, Collections.singletonList(LOC_415)), JurorStatusGroup.AT_COURT);

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

        Juror juror = new Juror();
        juror.setJurorNumber(JUROR7);
        when(jurorRepository.findByJurorNumber(JUROR7))
            .thenReturn(juror);

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

        UpdateAttendanceDto request = buildUpdateAttendanceDto(new ArrayList<>());
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
        verify(appearanceRepository, times(2)).findAllById(any());
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


            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool(POOL_NUMBER_415230101));

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool(POOL_NUMBER_415230101));

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


            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool(POOL_NUMBER_415230101));

            JurorPool jurorPool = buildJurorPool(POOL_NUMBER_415230101);
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.PANEL);
            jurorPool.setStatus(jurorStatus);
            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
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


            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool(POOL_NUMBER_415230101));

            JurorPool jurorPool = buildJurorPool(POOL_NUMBER_415230101);
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.JUROR);
            jurorPool.setStatus(jurorStatus);
            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
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


            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(buildJurorPool(POOL_NUMBER_415230101));

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
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


            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
                .thenReturn(null);

            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR2, POOL_NUMBER_415230101, Boolean.TRUE))
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


            JurorPool jurorPool = buildJurorPool(POOL_NUMBER_415230101);
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.EXCUSED);
            jurorPool.setStatus(jurorStatus);
            when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(
                VALID_COURT_LOCATION, JUROR1, POOL_NUMBER_415230101, Boolean.TRUE))
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

        private JurorPool buildJurorPool(String poolNumber) {

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(VALID_COURT_LOCATION);
            courtLocation.setLocCode(VALID_COURT_LOCATION);
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            PoolRequest poolRequest = new PoolRequest();
            poolRequest.setPoolNumber(poolNumber);
            poolRequest.setCourtLocation(courtLocation);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setPool(poolRequest);
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
                .locationCode(VALID_COURT_LOCATION)
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
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPools);

            when(jurorPoolRepository.findJurorsOnCallAtCourtLocation(VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPoolsOnCall);

            when(jurorPoolRepository.findJurorsNotInAttendanceAtCourtLocation(
                VALID_COURT_LOCATION, pools))
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

            when(jurorPoolRepository.getJurorsToDismiss(
                anyList(), anyList(), anyString()))
                .thenReturn(jurorsToDismissTuples);


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
                .locationCode(VALID_COURT_LOCATION)
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
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPools);

            when(jurorPoolRepository.findJurorsOnCallAtCourtLocation(VALID_COURT_LOCATION,
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

            when(jurorPoolRepository.getJurorsToDismiss(
                anyList(), anyList(), anyString()))
                .thenReturn(jurorsToDismissTuples);


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
                .locationCode(VALID_COURT_LOCATION)
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
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(
                VALID_COURT_LOCATION, pools))
                .thenReturn(jurorPools);

            when(jurorPoolRepository.findJurorsNotInAttendanceAtCourtLocation(
                VALID_COURT_LOCATION, pools))
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

            when(jurorPoolRepository.getJurorsToDismiss(
                anyList(), anyList(), anyString()))
                .thenReturn(jurorsToDismissTuples);


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
                .locationCode(VALID_COURT_LOCATION)
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
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(VALID_COURT_LOCATION,
                pools)).thenReturn(jurorPools);

            // in attendance
            Tuple t1 = mock(Tuple.class);
            mockQueryResultDismissal(t1, "FIRSTNAME1", "LASTNAME1", "In attendance",
                LocalTime.of(9, 30), now().toString(), now().minusDays(7));

            List<Tuple> jurorsToDismissTuples = new ArrayList<>();
            jurorsToDismissTuples.add(t1);

            when(jurorPoolRepository.getJurorsToDismiss(
                anyList(), anyList(), anyString()))
                .thenReturn(jurorsToDismissTuples);


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
                .locationCode(VALID_COURT_LOCATION)
                .includeNotInAttendance(true)
                .includeOnCall(true)
                .numberOfJurorsToDismiss(3)
                .build();

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            when(jurorPoolRepository.findJurorsInAttendanceAtCourtLocation(
                VALID_COURT_LOCATION, pools))
                .thenReturn(new ArrayList<>());

            when(jurorPoolRepository.findJurorsOnCallAtCourtLocation(VALID_COURT_LOCATION,
                pools)).thenReturn(new ArrayList<>());

            when(jurorPoolRepository.findJurorsNotInAttendanceAtCourtLocation(
                VALID_COURT_LOCATION, pools))
                .thenReturn(new ArrayList<>());


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
            .login("COURT_USER")
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

        doReturn(jurorPool1).when(jurorPoolService).getJurorPoolFromUser(JUROR1);
        doReturn(jurorPool2).when(jurorPoolService).getJurorPoolFromUser(JUROR2);
        doReturn(jurorPool3).when(jurorPoolService).getJurorPoolFromUser(JUROR3);
        doReturn(jurorPool5).when(jurorPoolService).getJurorPoolFromUser(JUROR5);
        doReturn(jurorPool6).when(jurorPoolService).getJurorPoolFromUser(JUROR6);
        doReturn(jurorPool7).when(jurorPoolService).getJurorPoolFromUser(JUROR7);


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

        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR1, JurorStatusGroup.AT_COURT))
            .thenReturn(jurorAppearance1);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR2, JurorStatusGroup.AT_COURT))
            .thenReturn(jurorAppearance2);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR3, JurorStatusGroup.AT_COURT))
            .thenReturn(jurorAppearance3);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR5, JurorStatusGroup.AT_COURT))
            .thenReturn(jurorAppearance5);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR6, JurorStatusGroup.AT_COURT))
            .thenReturn(jurorAppearance6);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR7, JurorStatusGroup.AT_COURT))
            .thenReturn(jurorAppearance7);

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
        doReturn(jurorPool7).when(jurorPoolService).getJurorPoolFromUser(JUROR7);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance7 = buildAttendanceRecords(JUROR7,
            "SEVEN", null, null, IJurorStatus.RESPONDED);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR7, JurorStatusGroup.AT_COURT))
            .thenReturn(jurorAppearance7);

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
        doReturn(jurorPool2).when(jurorPoolService).getJurorPoolFromUser(JUROR2);

        doReturn(jurorPool3).when(jurorPoolService).getJurorPoolFromUser(JUROR3);
        doReturn(jurorPool6).when(jurorPoolService).getJurorPoolFromUser(JUROR6);
        doReturn(jurorPool7).when(jurorPoolService).getJurorPoolFromUser(JUROR7);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance2 = buildAttendanceRecords(JUROR2,
            "TWO", LocalTime.of(9, 30), null, IJurorStatus.JUROR);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance3 = buildAttendanceRecords(JUROR3,
            "THREE", LocalTime.of(9, 30), null, IJurorStatus.PANEL);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance6 = buildAttendanceRecords(JUROR6,
            "SIX", LocalTime.of(9, 30), null, IJurorStatus.RESPONDED);

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> jurorAppearance7 = buildAttendanceRecords(JUROR7,
            "SEVEN", LocalTime.of(9, 30), null, IJurorStatus.RESPONDED);

        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR2, JurorStatusGroup.ALL))
            .thenReturn(jurorAppearance2);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR3, JurorStatusGroup.ALL))
            .thenReturn(jurorAppearance3);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR6, JurorStatusGroup.ALL))
            .thenReturn(jurorAppearance6);
        when(appearanceRepository.getAppearanceRecords("415", now(), JUROR7, JurorStatusGroup.ALL))
            .thenReturn(jurorAppearance7);

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
        courtLocation.setOwner("415");
        courtLocation.setLocCode("415");
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

        Juror juror1 = Juror.builder().jurorNumber(JUROR1).firstName("TEST").lastName("ONE").build();
        Juror juror2 = Juror.builder().jurorNumber(JUROR2).firstName("TEST").lastName("TWO").build();
        Juror juror3 = Juror.builder().jurorNumber(JUROR3).firstName("TEST").lastName("THREE").build();
        Juror juror6 = Juror.builder().jurorNumber(JUROR6).firstName("TEST").lastName("SIX").build();
        Juror juror7 = Juror.builder().jurorNumber(JUROR7).firstName("TEST").lastName("SEVEN").build();

        PoolRequest poolRequest =
            PoolRequest.builder().owner("415").poolNumber("415000001").courtLocation(courtLocation).build();

        JurorPool jurorPool1 = JurorPool.builder().juror(juror1).pool(poolRequest).build();
        JurorPool jurorPool2 = JurorPool.builder().juror(juror2).pool(poolRequest).build();
        JurorPool jurorPool3 = JurorPool.builder().juror(juror3).pool(poolRequest).build();
        JurorPool jurorPool6 = JurorPool.builder().juror(juror6).pool(poolRequest).build();
        JurorPool jurorPool7 = JurorPool.builder().juror(juror7).pool(poolRequest).build();

        doReturn(jurorPool1).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(eq(JUROR1), eq(true),
            any());
        doReturn(jurorPool2).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(eq(JUROR2), eq(true),
            any());
        doReturn(jurorPool3).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(eq(JUROR3), eq(true),
            any());
        doReturn(jurorPool6).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(eq(JUROR6), eq(true),
            any());
        doReturn(jurorPool7).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(eq(JUROR7), eq(true),
            any());

        when(jurorRepository.findById(JUROR1)).thenReturn(Optional.of(createJuror(JUROR1, IJurorStatus.JUROR)));
        when(jurorRepository.findById(JUROR2)).thenReturn(Optional.of(createJuror(JUROR2, IJurorStatus.JUROR)));
        when(jurorRepository.findById(JUROR3)).thenReturn(Optional.of(createJuror(JUROR3, IJurorStatus.JUROR)));
        when(jurorRepository.findById(JUROR6)).thenReturn(Optional.of(createJuror(JUROR6, IJurorStatus.JUROR)));
        when(jurorRepository.findById(JUROR7)).thenReturn(Optional.of(createJuror(JUROR7, IJurorStatus.JUROR)));

    }


    @DisplayName("public void addNonAttendance(JurorNonAttendanceDto request)")
    @Nested
    class NonAttendance {

        private static final String COURT_OWNER = "415";
        private static final String COURT_LOCATION_CODE = "415";
        private static final String JUROR_NUMBER = "111111111";
        private static final String POOL_NUMBER = "415230101";
        private static final String USERNAME = "COURT_USER";

        @Test
        void positiveNonAttendanceAdded() {

            TestUtils.setUpMockAuthentication(COURT_OWNER, USERNAME, "1", List.of(COURT_OWNER));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(COURT_OWNER);
            courtLocation.setLocCode(COURT_LOCATION_CODE);
            when(courtLocationRepository.findByLocCode(COURT_LOCATION_CODE)).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(POOL_NUMBER)
                .returnDate(now().minusDays(20))
                .courtLocation(courtLocation)
                .build();

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(COURT_OWNER);
            jurorPool.setPool(poolRequest);

            Juror juror = new Juror();
            juror.setJurorNumber(JUROR_NUMBER);
            juror.setFinancialLoss(BigDecimal.valueOf(63.90));
            jurorPool.setJuror(juror);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);

            when(jurorRepository.findById(JUROR_NUMBER)).thenReturn(
                Optional.of(createJuror(JUROR_NUMBER, IJurorStatus.JUROR)));

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(JUROR_NUMBER)
                .nonAttendanceDate(now())
                .poolNumber(POOL_NUMBER)
                .locationCode(COURT_LOCATION_CODE)
                .build();

            LocalDate nonAttendanceDate = now();
            doReturn(Optional.empty()).when(appearanceRepository)
                .findByLocCodeAndJurorNumberAndAttendanceDate(
                    COURT_LOCATION_CODE, JUROR_NUMBER, nonAttendanceDate);

            Appearance appearance = Appearance.builder()
                .jurorNumber(JUROR_NUMBER)
                .attendanceDate(now())
                .courtLocation(courtLocation)
                .appearanceStage(CHECKED_IN)
                .build();

            doReturn(Optional.of(appearance)).when(appearanceRepository)
                .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                    COURT_LOCATION_CODE, JUROR_NUMBER, nonAttendanceDate);

            jurorAppearanceService.addNonAttendance(request);

            verify(courtLocationRepository, times(1)).findByLocCode(COURT_LOCATION_CODE);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            verify(appearanceRepository, times(1))
                .findByLocCodeAndJurorNumberAndAttendanceDate(
                    COURT_LOCATION_CODE, JUROR_NUMBER, nonAttendanceDate);
            verify(appearanceRepository, times(1)).saveAndFlush(any());
        }

        @Test
        void positiveNonAttendanceInTheFutureAdded() {

            TestUtils.setUpMockAuthentication(COURT_OWNER, USERNAME, "1", List.of(COURT_OWNER));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(COURT_OWNER);
            courtLocation.setLocCode(COURT_LOCATION_CODE);
            when(courtLocationRepository.findByLocCode(COURT_LOCATION_CODE)).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(POOL_NUMBER)
                .returnDate(now().minusDays(5))
                .courtLocation(courtLocation)
                .build();

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(COURT_OWNER);
            jurorPool.setPool(poolRequest);

            Juror juror = new Juror();
            juror.setJurorNumber(JUROR_NUMBER);
            juror.setFinancialLoss(BigDecimal.valueOf(63.90));
            jurorPool.setJuror(juror);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);

            when(jurorRepository.findById(JUROR_NUMBER)).thenReturn(
                Optional.of(createJuror(JUROR_NUMBER, IJurorStatus.JUROR)));

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(JUROR_NUMBER)
                .nonAttendanceDate(now().plusDays(5))
                .poolNumber(POOL_NUMBER)
                .locationCode(COURT_LOCATION_CODE)
                .build();

            LocalDate nonAttendanceDate = now().plusDays(5);
            doReturn(Optional.empty()).when(appearanceRepository)
                .findByLocCodeAndJurorNumberAndAttendanceDate(
                    COURT_LOCATION_CODE, JUROR_NUMBER, nonAttendanceDate);

            Appearance appearance = Appearance.builder()
                .jurorNumber(JUROR_NUMBER)
                .attendanceDate(now())
                .courtLocation(courtLocation)
                .appearanceStage(CHECKED_IN)
                .build();

            doReturn(Optional.of(appearance)).when(appearanceRepository)
                .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                    COURT_LOCATION_CODE, JUROR_NUMBER, nonAttendanceDate);

            jurorAppearanceService.addNonAttendance(request);

            verify(courtLocationRepository, times(1)).findByLocCode(COURT_LOCATION_CODE);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            verify(appearanceRepository, times(1))
                .findByLocCodeAndJurorNumberAndAttendanceDate(
                    COURT_LOCATION_CODE, JUROR_NUMBER, nonAttendanceDate);
            verify(appearanceRepository, times(1)).saveAndFlush(any());
        }


        @Test
        void negativeNonAttendanceCourtNotFound() {

            TestUtils.setUpMockAuthentication(COURT_OWNER, USERNAME, "1", List.of(COURT_OWNER));

            when(courtLocationRepository.findByLocCode(COURT_LOCATION_CODE)).thenReturn(Optional.empty());

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(JUROR_NUMBER)
                .nonAttendanceDate(now())
                .poolNumber(POOL_NUMBER)
                .locationCode(COURT_LOCATION_CODE)
                .build();

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                    jurorAppearanceService.addNonAttendance(request))
                .withMessage("Court location " + COURT_LOCATION_CODE + " not found");

            verify(courtLocationRepository, times(1)).findByLocCode(COURT_LOCATION_CODE);
            verify(courtLocationRepository, times(0)).findById(anyString());
            verify(jurorPoolRepository, times(0))
                .findByJurorJurorNumberAndPoolPoolNumber(anyString(), anyString());
            verify(appearanceRepository, times(0))
                .findByLocCodeAndJurorNumberAndAttendanceDate(
                    anyString(), anyString(), any());
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }

        @Test
        void negativeNonAttendanceForbiddenCourtUser() {

            TestUtils.setUpMockAuthentication(COURT_OWNER, USERNAME, "1", List.of(COURT_OWNER));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("416"); // different to user owner
            when(courtLocationRepository.findByLocCode("416")).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById("416")).thenReturn(Optional.of(courtLocation));

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(JUROR_NUMBER)
                .nonAttendanceDate(now())
                .poolNumber(POOL_NUMBER)
                .locationCode("416")
                .build();

            assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
                    jurorAppearanceService.addNonAttendance(request))
                .withMessage("User does not have access");

            verify(courtLocationRepository, times(0)).findByLocCode("416");
            verify(courtLocationRepository, times(0)).findById("416");
            verify(jurorPoolRepository, times(0))
                .findByJurorJurorNumberAndPoolPoolNumber(anyString(), anyString());
            verify(appearanceRepository, times(0))
                .findByLocCodeAndJurorNumberAndAttendanceDate(anyString(), anyString(), any());
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }

        @Test
        void negativeNonAttendancePoolNotFound() {

            TestUtils.setUpMockAuthentication(COURT_OWNER, USERNAME, "1", List.of(COURT_OWNER));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(COURT_OWNER);
            when(courtLocationRepository.findByLocCode(COURT_LOCATION_CODE)).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            doReturn(null).when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(JUROR_NUMBER)
                .nonAttendanceDate(now())
                .poolNumber(POOL_NUMBER)
                .locationCode(COURT_LOCATION_CODE)
                .build();

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                    jurorAppearanceService.addNonAttendance(request))
                .withMessage("Juror not found in Pool " + request.getPoolNumber());

            verify(courtLocationRepository, times(1)).findByLocCode(COURT_LOCATION_CODE);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            verify(appearanceRepository, times(0))
                .findByLocCodeAndJurorNumberAndAttendanceDate(anyString(), anyString(), any());
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }

        @Test
        void negativeNonAttendanceJurorInAttendance() {

            TestUtils.setUpMockAuthentication(COURT_OWNER, USERNAME, "1", List.of(COURT_OWNER));

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner(COURT_OWNER);
            courtLocation.setLocCode(COURT_LOCATION_CODE);
            when(courtLocationRepository.findByLocCode(COURT_LOCATION_CODE)).thenReturn(Optional.of(courtLocation));
            when(courtLocationRepository.findById(any())).thenReturn(Optional.of(courtLocation));

            PoolRequest poolRequest = PoolRequest.builder()
                .poolNumber(POOL_NUMBER)
                .returnDate(now().minusDays(20))
                .courtLocation(courtLocation)
                .build();

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(COURT_OWNER);
            jurorPool.setPool(poolRequest);

            Juror juror = new Juror();
            juror.setJurorNumber(JUROR_NUMBER);
            juror.setFinancialLoss(BigDecimal.valueOf(63.90));
            jurorPool.setJuror(juror);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);

            Appearance appearance = new Appearance();
            appearance.setAppearanceStage(CHECKED_IN);

            LocalDate nonAttendanceDate = now();
            doReturn(Optional.of(appearance)).when(appearanceRepository)
                .findByLocCodeAndJurorNumberAndAttendanceDate(
                    COURT_LOCATION_CODE, JUROR_NUMBER, nonAttendanceDate);

            final JurorNonAttendanceDto request = JurorNonAttendanceDto.builder()
                .jurorNumber(JUROR_NUMBER)
                .nonAttendanceDate(now())
                .poolNumber(POOL_NUMBER)
                .locationCode(COURT_LOCATION_CODE)
                .build();

            MojException.BusinessRuleViolation exception =
                assertThrows(MojException.BusinessRuleViolation.class,
                    () -> jurorAppearanceService.addNonAttendance(request), "verify exception is thrown");

            assertThat(exception.getMessage()).isEqualTo("Juror " + JUROR_NUMBER + " already has an "
                + "attendance record for the date " + nonAttendanceDate);

            assertThat(exception.getErrorCode()).isEqualTo(ATTENDANCE_RECORD_ALREADY_EXISTS);

            verify(courtLocationRepository, times(1)).findByLocCode(COURT_LOCATION_CODE);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            verify(appearanceRepository, times(1))
                .findByLocCodeAndJurorNumberAndAttendanceDate(
                    COURT_LOCATION_CODE, JUROR_NUMBER, nonAttendanceDate);
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("void realignAttendanceType(Appearance appearance)")
    class RealignAttendanceType {
        private List<Appearance> appearanceList;

        @BeforeEach
        void before() {
            this.appearanceList = new ArrayList<>();
            jurorAppearanceService = spy(jurorAppearanceService);

        }

        private Appearance mockAppearance(LocalTime timeIn, LocalTime timeOut,
                                          String locCode,
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

            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getLocCode()).thenReturn(locCode);
            when(appearance.getCourtLocation()).thenReturn(courtLocation);

            when(jurorAppearanceService.getAllAppearances(TestConstants.VALID_JUROR_NUMBER))
                .thenReturn(appearanceList);

            appearanceList.add(appearance);

            when(jurorRepository.findById(TestConstants.VALID_JUROR_NUMBER)).thenReturn(
                Optional.of(createJuror(TestConstants.VALID_JUROR_NUMBER, IJurorStatus.JUROR)));

            when(jurorAppearanceService.isLongTrialDay(
                anyList(),
                eq(localDate)
            )).thenReturn(isLongTrialDay);
            return appearance;
        }

        @Test
        void positiveFulLDay() {
            Appearance appearance = mockAppearance(
                LocalTime.of(9, 30),
                LocalTime.of(17, 30),
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
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
                VALID_COURT_LOCATION,
                false,
                Boolean.FALSE,
                AttendanceType.ABSENT,
                true);
            jurorAppearanceService.realignAttendanceType(appearance);
            verify(appearance, never()).setAttendanceType(any());
        }
    }

    @Nested
    @DisplayName("retrieveJurorsOnTrials")
    class JurorsOnTrialsTest {

        @Test
        @DisplayName("Get Jurors On Trials happy path")
        void jurorsOnTrialHappy() {


            final String locationCode = "415";
            final LocalDate attendanceDate = now();

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");

            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(locationCode);

            Tuple t1 = mock(Tuple.class);
            doReturn("T10000000").when(t1).get(QTrial.trial.trialNumber);
            doReturn("test defendants").when(t1).get(QTrial.trial.description);
            doReturn(TrialType.CIV).when(t1).get(QTrial.trial.trialType);
            doReturn("Big Court Room").when(t1).get(QCourtroom.courtroom.description);
            doReturn("Big Judge").when(t1).get(QTrial.trial.judge.name);
            doReturn(8L).when(t1).get(QPanel.panel.count());

            List<Tuple> jurorsOnTrialTuples = new ArrayList<>();
            jurorsOnTrialTuples.add(t1);

            when(trialRepository.getActiveTrialsWithJurorCount(locationCode, attendanceDate))
                .thenReturn(jurorsOnTrialTuples);

            Tuple t2 = mock(Tuple.class);
            doReturn("T10000000").when(t2).get(QTrial.trial.trialNumber);
            doReturn("Trial Desc").when(t2).get(QTrial.trial.description);

            List<Tuple> jurorsOnTrialsAttended = new ArrayList<>();
            jurorsOnTrialsAttended.add(t2);

            when(appearanceRepository.getTrialsWithAttendanceCount(locationCode,
                attendanceDate)).thenReturn(jurorsOnTrialsAttended);

            JurorsOnTrialResponseDto
                response = jurorAppearanceService.retrieveJurorsOnTrials(locationCode, attendanceDate);

            assertThat(response.getTrialsList().size()).isEqualTo(1);

            verify(courtLocationRepository, times(1)).findById(locationCode);
            verify(trialRepository, times(1)).getActiveTrialsWithJurorCount(locationCode, attendanceDate);
            verify(appearanceRepository, times(1)).getTrialsWithAttendanceCount(locationCode, attendanceDate);

        }

        @Test
        @DisplayName("Get Jurors On Trials - wrong court")
        void jurorsOnTrialWrongCourt() {


            final String locationCode = "416";
            final LocalDate attendanceDate = now();

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("416");

            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(locationCode);

            assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
                    jurorAppearanceService.retrieveJurorsOnTrials(locationCode, attendanceDate))
                .as("User does not have access to court location")
                .withMessageContaining("Cannot access court details for this location "
                    + locationCode);

            verify(courtLocationRepository, times(1)).findById(locationCode);
            verify(trialRepository, times(0)).getActiveTrialsWithJurorCount(locationCode, attendanceDate);
            verify(appearanceRepository, times(0)).getTrialsWithAttendanceCount(locationCode, attendanceDate);

        }

        @Test
        @DisplayName("Get Jurors On Trials No Records found")
        void jurorsOnTrialNoRecordsFound() {


            final String locationCode = "415";
            final LocalDate attendanceDate = now();

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");

            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findById(locationCode);

            List<Tuple> jurorsOnTrialTuples = new ArrayList<>();

            when(trialRepository.getActiveTrialsWithJurorCount(locationCode, attendanceDate))
                .thenReturn(jurorsOnTrialTuples);

            List<Tuple> jurorsOnTrialsAttended = new ArrayList<>();

            when(appearanceRepository.getTrialsWithAttendanceCount(locationCode,
                attendanceDate)).thenReturn(jurorsOnTrialsAttended);

            JurorsOnTrialResponseDto
                response = jurorAppearanceService.retrieveJurorsOnTrials(locationCode, attendanceDate);

            assertThat(response.getTrialsList().size()).isEqualTo(0);

            verify(courtLocationRepository, times(1)).findById(locationCode);
            verify(trialRepository, times(1)).getActiveTrialsWithJurorCount(locationCode, attendanceDate);
            verify(appearanceRepository, times(1)).getTrialsWithAttendanceCount(locationCode, attendanceDate);

        }

        @Test
        @DisplayName("Confirm Juror attendance happy path")
        void confirmAttendanceHappy() {
            final String locationCode = "415";

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            courtLocation.setLocCode(locationCode);

            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(locationCode);

            final Juror juror1 = new Juror();
            juror1.setJurorNumber(JUROR1);

            final Juror juror2 = new Juror();
            juror2.setJurorNumber(JUROR2);

            final Trial trial = mock(Trial.class);
            when(trial.getTrialNumber()).thenReturn(TestConstants.VALID_TRIAL_NUMBER);
            final Panel panel1 = mock(Panel.class);
            final Panel panel2 = mock(Panel.class);
            when(panel1.getTrial()).thenReturn(trial);
            when(panel2.getTrial()).thenReturn(trial);
            doReturn(panel1).when(panelRepository).findActivePanel(locationCode, JUROR1);
            doReturn(panel2).when(panelRepository).findActivePanel(locationCode, JUROR2);

            final PoolRequest poolRequest = new PoolRequest();
            poolRequest.setPoolNumber("987654321");
            poolRequest.setOwner("415");

            final JurorPool jurorPool1 = getJurorPool(juror1, IJurorStatus.JUROR);
            jurorPool1.setPool(poolRequest);
            jurorPool1.setOwner("415");

            final JurorPool jurorPool2 = getJurorPool(juror2, IJurorStatus.JUROR);
            jurorPool2.setPool(poolRequest);
            jurorPool2.setOwner("415");

            doReturn(jurorPool1).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(
                JUROR1, true, courtLocation);
            doReturn(jurorPool2).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(
                JUROR2, true, courtLocation);

            juror1.setAssociatedPools(Set.of(jurorPool1));
            juror2.setAssociatedPools(Set.of(jurorPool2));
            doReturn(Optional.of(juror1)).when(jurorRepository).findById(JUROR1);
            doReturn(Optional.of(juror2)).when(jurorRepository).findById(JUROR2);

            doReturn(jurorPool1).when(jurorPoolService).getJurorPoolFromUser(JUROR1);
            doReturn(jurorPool2).when(jurorPoolService).getJurorPoolFromUser(JUROR2);

            final UpdateAttendanceDto request = buildUpdateAttendanceDto(locationCode);
            request.setJuror(Arrays.asList(JUROR1, JUROR2));


            Appearance appearance1 = Appearance.builder()
                .jurorNumber(JUROR1)
                .attendanceDate(request.getCommonData().getAttendanceDate())
                .courtLocation(courtLocation)
                .poolNumber(jurorPool1.getPool().getPoolNumber())
                .build();

            Appearance appearance2 = Appearance.builder()
                .jurorNumber(JUROR2)
                .attendanceDate(request.getCommonData().getAttendanceDate())
                .courtLocation(courtLocation)
                .poolNumber(jurorPool2.getPool().getPoolNumber())
                .build();

            when(appearanceRepository.findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR1,
                now().minusDays(1))).thenReturn(Optional.of(appearance1));
            when(appearanceRepository.findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR2,
                now().minusDays(1))).thenReturn(Optional.of(appearance2));
            when(appearanceRepository.getNextAttendanceAuditNumber()).thenReturn(10_123_456L);

            doReturn(List.of(appearance1)).when(appearanceRepository)
                .findAllByJurorNumber(JUROR1);
            doReturn(List.of(appearance2)).when(appearanceRepository)
                .findAllByJurorNumber(JUROR2);

            jurorAppearanceService.confirmJuryAttendance(request);

            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verify(jurorRepository, times(2)).findById(JUROR1);
            verify(jurorRepository, times(2)).findById(JUROR2);
            verify(jurorPoolRepository, times(1))
                .findByJurorNumberAndIsActiveAndCourt(JUROR1, true, courtLocation);
            verify(jurorPoolRepository, times(1))
                .findByJurorNumberAndIsActiveAndCourt(JUROR2, true, courtLocation);

            verify(appearanceRepository, times(1)).getNextAttendanceAuditNumber();
            verify(appearanceRepository, times(2)).findByLocCodeAndJurorNumberAndAttendanceDate(
                LOC_415, JUROR1, now().minusDays(1));
            verify(appearanceRepository, times(2)).findByLocCodeAndJurorNumberAndAttendanceDate(
                LOC_415, JUROR2, now().minusDays(1));

            ArgumentCaptor<Appearance> appearanceCaptor = ArgumentCaptor.forClass(Appearance.class);

            verify(appearanceRepository, times(2)).saveAndFlush(appearanceCaptor.capture());
            verify(jurorExpenseService, times(2)).applyDefaultExpenses(
                appearanceCaptor.capture(), any());

            Appearance capturedAppearance1 =
                appearanceCaptor.getAllValues().stream()
                    .filter(app -> JUROR1.equalsIgnoreCase(app.getJurorNumber()))
                    .findFirst().get();
            assertThat(JUROR1).isEqualTo(capturedAppearance1.getJurorNumber());
            assertThat(capturedAppearance1.getAttendanceDate()).isEqualTo(request.getCommonData().getAttendanceDate());
            assertThat(capturedAppearance1.getCourtLocation()).isEqualTo(courtLocation);
            assertThat(capturedAppearance1.getPoolNumber()).isEqualTo(jurorPool1.getPool().getPoolNumber());
            assertThat(capturedAppearance1.getAttendanceType()).isEqualTo(AttendanceType.FULL_DAY);
            assertThat(capturedAppearance1.getAppearanceStage()).isEqualTo(EXPENSE_ENTERED);
            assertThat(capturedAppearance1.getAttendanceAuditNumber()).isEqualTo("J10123456");
            assertThat(capturedAppearance1.getSatOnJury()).isTrue();


            Appearance capturedAppearance2 =
                appearanceCaptor.getAllValues().stream()
                    .filter(app -> JUROR2.equalsIgnoreCase(app.getJurorNumber()))
                    .findFirst().get();
            assertThat(JUROR2).isEqualTo(capturedAppearance2.getJurorNumber());
            assertThat(capturedAppearance2.getAttendanceDate()).isEqualTo(request.getCommonData().getAttendanceDate());
            assertThat(capturedAppearance2.getCourtLocation()).isEqualTo(courtLocation);
            assertThat(capturedAppearance2.getPoolNumber()).isEqualTo(jurorPool1.getPool().getPoolNumber());
            assertThat(capturedAppearance2.getAttendanceType()).isEqualTo(AttendanceType.FULL_DAY);
            assertThat(capturedAppearance2.getAppearanceStage()).isEqualTo(EXPENSE_ENTERED);
            assertThat(capturedAppearance2.getAttendanceAuditNumber()).isEqualTo("J10123456");
            assertThat(capturedAppearance2.getSatOnJury()).isTrue();

            verify(jurorPoolRepository, times(2)).saveAndFlush(any());

            verify(jurorHistoryService, times(1)).createJuryAttendanceHistory(jurorPool1,
                capturedAppearance1, panel1);
            verify(jurorHistoryService, times(1))
                .createJuryAttendanceHistory(jurorPool2, capturedAppearance2, panel2);
        }

        @Test
        @DisplayName("Confirm Juror attendance - Juror owner invalid")
        void confirmAttendanceInvalidJurorOwner() {


            final String locationCode = "416";
            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("416");
            courtLocation.setLocCode(locationCode);

            final Juror juror = new Juror();
            juror.setJurorNumber(JUROR_123456789);

            final PoolRequest poolRequest = new PoolRequest();
            poolRequest.setPoolNumber("987654321");

            final JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
            jurorPool.setPool(poolRequest);
            jurorPool.setOwner("416");

            Set<JurorPool> jurorPools = new HashSet<>();
            jurorPools.add(jurorPool);
            juror.setAssociatedPools(jurorPools);
            doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR_123456789);

            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(locationCode);

            final UpdateAttendanceDto request = buildUpdateAttendanceDto(locationCode);
            assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
                    jurorAppearanceService.confirmJuryAttendance(request))
                .as("User does not own juror record")
                .withMessageContaining("User does not have ownership of the supplied "
                    + "juror record");

            verify(appearanceRepository, times(1)).getNextAttendanceAuditNumber();
            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verify(jurorRepository, times(1)).findById(JUROR_123456789);
            verifyNoInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
            verify(appearanceRepository, never()).findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR_123456789,
                request.getCommonData().getAttendanceDate());
        }

        @Test
        @DisplayName("Confirm Juror attendance - juror record not found")
        void confirmAttendanceNoJurorsFound() {

            TestUtils.setUpMockAuthentication("416", "COURT_USER", "1", List.of("416"));
            final String locationCode = "416";

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("416");
            courtLocation.setLocCode(locationCode);

            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(locationCode);
            doReturn(Optional.empty()).when(jurorRepository).findById(JUROR_123456789);

            final UpdateAttendanceDto request = buildUpdateAttendanceDto(locationCode);
            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                    jurorAppearanceService.confirmJuryAttendance(request))
                .as("Juror record not found")
                .withMessageContaining("Unable to find valid juror record for Juror Number: "
                    + JUROR_123456789);

            verify(appearanceRepository, times(1)).getNextAttendanceAuditNumber();
            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verify(jurorRepository, times(1)).findById(JUROR_123456789);
            verifyNoInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
            verify(appearanceRepository, never()).findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR_123456789,
                request.getCommonData().getAttendanceDate());
        }

        @Test
        @DisplayName("Confirm Juror attendance - Invalid court location")
        void confirmAttendanceInvalidCourtLocation() {

            TestUtils.setUpMockAuthentication("999", "COURT_USER", "1", List.of("999"));
            final String locationCode = "999";

            doReturn(Optional.empty()).when(courtLocationRepository).findByLocCode(locationCode);

            final UpdateAttendanceDto request = buildUpdateAttendanceDto(locationCode);
            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                    jurorAppearanceService.confirmJuryAttendance(request))
                .as("Court location not found").withMessageContaining("Court location not found");

            verify(appearanceRepository, times(1)).getNextAttendanceAuditNumber();
            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verifyNoInteractions(jurorRepository);
            verifyNoInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
            verify(appearanceRepository, never()).findByLocCodeAndJurorNumberAndAttendanceDate(LOC_415, JUROR_123456789,
                request.getCommonData().getAttendanceDate());
        }

        private UpdateAttendanceDto buildUpdateAttendanceDto(String locationCode) {
            UpdateAttendanceDto.CommonData commonData = new UpdateAttendanceDto.CommonData();
            commonData.setStatus(UpdateAttendanceStatus.CONFIRM_ATTENDANCE);
            commonData.setAttendanceDate(now().minusDays(1));
            commonData.setLocationCode(locationCode);
            commonData.setCheckInTime(LocalTime.of(9, 00));
            commonData.setCheckOutTime(LocalTime.of(16, 00));
            commonData.setSingleJuror(Boolean.FALSE);

            UpdateAttendanceDto request = new UpdateAttendanceDto();
            request.setCommonData(commonData);
            request.setJuror(Arrays.asList(JUROR_123456789, JUROR_123456789));

            return request;
        }

    }


    @Nested
    @DisplayName("UnconfirmedJurors")
    class UnconfirmedJurors {

        @Test
        @DisplayName("Get unconfirmed jurors happy path")
        void unconfirmedJurorsHappy() {
            final String locationCode = "415";
            final LocalDate attendanceDate = now().minusDays(7);

            LocalTime checkInTime = LocalTime.of(9, 00);
            LocalTime checkOutTime = LocalTime.of(16, 00);

            Tuple t1 = mock(Tuple.class);
            mockUnconfirmedJurorTuple(t1,  "123456789",  "Joe",  "Lastname",
                IJurorStatus.RESPONDED,  checkInTime,  checkOutTime);

            Tuple t2 = mock(Tuple.class);
            mockUnconfirmedJurorTuple(t2, "987654321",  "Bob", "Last-Name",
                IJurorStatus.JUROR, checkInTime, checkOutTime);

            List<Tuple> unconfirmedJurorTuples = new ArrayList<>();
            unconfirmedJurorTuples.add(t1);
            unconfirmedJurorTuples.add(t2);
            when(appearanceRepository.getUnconfirmedJurors(locationCode, attendanceDate))
                .thenReturn(unconfirmedJurorTuples);

            UnconfirmedJurorResponseDto response = jurorAppearanceService.retrieveUnconfirmedJurors(
                locationCode, attendanceDate);

            assertThat(response.getJurors().size()).isEqualTo(2);

            List<UnconfirmedJurorDataDto> jurors = response.getJurors();
            assertThat(jurors.get(0).getJurorNumber()).isEqualTo("123456789");
            assertThat(jurors.get(0).getFirstName()).isEqualTo("Joe");
            assertThat(jurors.get(0).getLastName()).isEqualTo("Lastname");
            assertThat(jurors.get(0).getStatus()).isEqualTo(JurorStatusEnum.RESPONDED);
            assertThat(jurors.get(0).getCheckInTime()).isEqualTo(checkInTime);
            assertThat(jurors.get(0).getCheckOutTime()).isEqualTo(checkOutTime);

            assertThat(jurors.get(1).getJurorNumber()).isEqualTo("987654321");
            assertThat(jurors.get(1).getFirstName()).isEqualTo("Bob");
            assertThat(jurors.get(1).getLastName()).isEqualTo("Last-Name");
            assertThat(jurors.get(1).getStatus()).isEqualTo(JurorStatusEnum.JUROR);
            assertThat(jurors.get(1).getCheckInTime()).isEqualTo(checkInTime);
            assertThat(jurors.get(1).getCheckOutTime()).isEqualTo(checkOutTime);

            verify(appearanceRepository, times(1)).getUnconfirmedJurors(locationCode, attendanceDate);

        }

        @Test
        @DisplayName("Get unconfirmed jurors day not confirmed")
        void dayNotConfirmed() {
            String locationCode = "415";
            LocalDate attendanceDate = now().minusDays(1);

            assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                    jurorAppearanceService.retrieveUnconfirmedJurors(
                        locationCode, attendanceDate)).as("Day not confirmed")
                .withMessageContaining("Attendance for location " + locationCode + " on date "
                    + attendanceDate + " is not confirmed");

            verify(appearanceRepository, never()).getUnconfirmedJurors(locationCode, attendanceDate);
        }

        private void mockUnconfirmedJurorTuple(Tuple t1, String jurorNumber, String firstName, String lastName,
                                               int status, LocalTime checkInTime,
                                               LocalTime checkOutTime) {
            doReturn(jurorNumber).when(t1).get(0, String.class);
            doReturn(firstName).when(t1).get(1, String.class);
            doReturn(lastName).when(t1).get(2, String.class);
            doReturn(status).when(t1).get(3, Integer.class);
            doReturn(checkInTime).when(t1).get(4, LocalTime.class);
            doReturn(checkOutTime).when(t1).get(5, LocalTime.class);
        }

    }

    @Nested
    @DisplayName("ConfirmJuror")
    class ConfirmJuror {

        @Test
        @DisplayName("confirm juror happy path")
        void confirmJurorHappy() {

            TestUtils.setUpMockAuthentication("415", "COURT_USER", "1", List.of("415"));
            final String locationCode = "415";

            final ConfirmAttendanceDto request = ConfirmAttendanceDto.builder()
                .jurorNumber(JUROR1)
                .attendanceDate(now().minusDays(1))
                .locationCode(locationCode)
                .checkInTime(LocalTime.of(9, 00))
                .checkOutTime(LocalTime.of(16, 00))
                .build();

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            courtLocation.setLocCode(locationCode);
            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(locationCode);

            final Juror juror = new Juror();
            juror.setJurorNumber(JUROR1);

            final PoolRequest poolRequest = new PoolRequest();
            poolRequest.setPoolNumber("987654321");
            poolRequest.setOwner("415");

            final JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
            jurorPool.setPool(poolRequest);
            jurorPool.setOwner("415");

            juror.setAssociatedPools(Set.of(jurorPool));

            doReturn(jurorPool).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(JUROR1, true,
                courtLocation);
            doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR1);

            doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR1);

            Appearance appearance = Appearance.builder()
                .jurorNumber(JUROR1)
                .attendanceDate(request.getAttendanceDate())
                .courtLocation(courtLocation)
                .poolNumber(jurorPool.getPool().getPoolNumber())
                .build();

            when(appearanceRepository.findByLocCodeAndJurorNumberAndAttendanceDate(locationCode, JUROR1,
                now().minusDays(1))).thenReturn(Optional.of(appearance));
            when(appearanceRepository.getNextAttendanceAuditNumber()).thenReturn(10_123_456L);

            jurorAppearanceService.confirmAttendance(request);

            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verify(jurorRepository, times(2)).findById(JUROR1);
            verify(jurorPoolRepository, times(1))
                .findByJurorNumberAndIsActiveAndCourt(JUROR1, true, courtLocation);
            verify(appearanceRepository, times(1)).getNextAttendanceAuditNumber();
            verify(appearanceRepository, times(2))
                .findByLocCodeAndJurorNumberAndAttendanceDate(locationCode, JUROR1, now().minusDays(1));

            verify(jurorPoolRepository, times(1)).saveAndFlush(jurorPool);
            verify(appearanceRepository, times(1)).saveAndFlush(appearance);

        }


        @Test
        @DisplayName("confirm juror - no appearance record")
        void confirmJurorNoAppearanceRecord() {

            TestUtils.setUpMockAuthentication("415", "COURT_USER", "1", List.of("415"));
            final String locationCode = "415";

            final ConfirmAttendanceDto request = ConfirmAttendanceDto.builder()
                .jurorNumber(JUROR1)
                .attendanceDate(now().minusDays(1))
                .locationCode(locationCode)
                .checkInTime(LocalTime.of(9, 00))
                .checkOutTime(LocalTime.of(16, 00))
                .build();

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            courtLocation.setLocCode(locationCode);
            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(locationCode);

            final Juror juror = new Juror();
            juror.setJurorNumber(JUROR1);

            final PoolRequest poolRequest = new PoolRequest();
            poolRequest.setPoolNumber("987654321");
            poolRequest.setOwner("415");

            final JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
            jurorPool.setPool(poolRequest);
            jurorPool.setOwner("415");

            juror.setAssociatedPools(Set.of(jurorPool));

            doReturn(jurorPool).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(JUROR1, true,
                courtLocation);
            doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR1);

            doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(JUROR1);

            when(appearanceRepository.findByLocCodeAndJurorNumberAndAttendanceDate(locationCode, JUROR1,
                now().minusDays(1)))
                .thenReturn(Optional.of(
                    Appearance.builder()
                        .jurorNumber(JUROR1)
                        .attendanceDate(now().minusDays(1))
                        .courtLocation(courtLocation)
                        .poolNumber("987654321")
                        .build()));
            when(appearanceRepository.getNextAttendanceAuditNumber()).thenReturn(10_123_456L);

            jurorAppearanceService.confirmAttendance(request);

            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verify(jurorRepository, times(2)).findById(JUROR1);
            verify(jurorPoolRepository, times(1)).findByJurorNumberAndIsActiveAndCourt(JUROR1, true, courtLocation);
            verify(appearanceRepository, times(1)).getNextAttendanceAuditNumber();
            verify(appearanceRepository, times(2)).findByLocCodeAndJurorNumberAndAttendanceDate(locationCode, JUROR1,
                now().minusDays(1));

            verify(jurorPoolRepository, times(1)).saveAndFlush(jurorPool);
            verify(appearanceRepository, times(1)).saveAndFlush(any(Appearance.class));

        }

        @Test
        @DisplayName("confirm juror - invalid court location")
        void confirmJurorInvalidCourtLocation() {

            TestUtils.setUpMockAuthentication("415", "COURT_USER", "1", List.of("415"));
            final String locationCode = "999";

            ConfirmAttendanceDto request = ConfirmAttendanceDto.builder()
                .jurorNumber(JUROR1)
                .attendanceDate(now().minusDays(1))
                .locationCode(locationCode)
                .checkInTime(LocalTime.of(9, 00))
                .checkOutTime(LocalTime.of(16, 00))
                .build();

            doReturn(Optional.empty()).when(courtLocationRepository).findByLocCode(locationCode);

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                    jurorAppearanceService.confirmAttendance(request))
                .as("Court location not found").withMessageContaining("Court location not found");

            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verifyNoInteractions(jurorRepository);
            verifyNoInteractions(jurorPoolRepository);
            verifyNoInteractions(appearanceRepository);

        }

        @Test
        @DisplayName("confirm juror - invalid juror number")
        void confirmJurorInvalidJurorNumber() {

            TestUtils.setUpMockAuthentication("415", "COURT_USER", "1", List.of("415"));
            final String locationCode = "415";

            final ConfirmAttendanceDto request = ConfirmAttendanceDto.builder()
                .jurorNumber("987654321")
                .attendanceDate(now().minusDays(1))
                .locationCode(locationCode)
                .checkInTime(LocalTime.of(9, 00))
                .checkOutTime(LocalTime.of(16, 00))
                .build();

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            courtLocation.setLocCode(locationCode);
            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(locationCode);

            doReturn(Optional.empty()).when(jurorRepository).findById(JUROR1);

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                    jurorAppearanceService.confirmAttendance(request))
                .as("Juror record not found").withMessageContaining(
                    "Unable to find valid juror record for Juror Number: " + "987654321");

            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verify(jurorRepository, times(1)).findById("987654321");
            verifyNoInteractions(jurorPoolRepository);
            verifyNoInteractions(appearanceRepository);

        }

        @Test
        @DisplayName("confirm juror - no owned active juror pool")
        void confirmJurorNoOwnedActiveJurorPool() {

            TestUtils.setUpMockAuthentication("415", "COURT_USER", "1", List.of("415"));
            final String locationCode = "415";

            final ConfirmAttendanceDto request = ConfirmAttendanceDto.builder()
                .jurorNumber(JUROR1)
                .attendanceDate(now().minusDays(1))
                .locationCode(locationCode)
                .checkInTime(LocalTime.of(9, 00))
                .checkOutTime(LocalTime.of(16, 00))
                .build();

            final CourtLocation courtLocation = new CourtLocation();
            courtLocation.setOwner("415");
            courtLocation.setLocCode(locationCode);
            doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(locationCode);

            final Juror juror = new Juror();
            juror.setJurorNumber(JUROR1);

            doReturn(Optional.of(juror)).when(jurorRepository).findById(JUROR1);

            final PoolRequest poolRequest = new PoolRequest();
            poolRequest.setPoolNumber("987654321");
            poolRequest.setOwner("416");

            final JurorPool jurorPool = getJurorPool(juror, IJurorStatus.RESPONDED);
            jurorPool.setPool(poolRequest);
            jurorPool.setOwner("416");

            juror.setAssociatedPools(Set.of(jurorPool));

            doReturn(jurorPool).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(JUROR1, true,
                courtLocation);

            assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
                    jurorAppearanceService.confirmAttendance(request))
                .as("User does not own juror record").withMessageContaining(
                    "User does not have ownership of the supplied " + "juror record");

            verify(courtLocationRepository, times(1)).findByLocCode(locationCode);
            verify(jurorRepository, times(1)).findById(JUROR1);
            verifyNoInteractions(appearanceRepository);

        }

    }

}
