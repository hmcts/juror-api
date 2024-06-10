package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import org.apache.logging.log4j.util.TriConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.BeanUtils;
import org.springframework.data.history.Revision;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.SerializationUtils;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.service.BureauService;
import uk.gov.hmcts.juror.api.bureau.service.ResponseExcusalService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.moj.controller.request.ConfirmIdentityDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ContactLogRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EditJurorRecordRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAddressDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDtoTest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessNameChangeRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessPendingJurorRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.UpdateAttendanceRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactEnquiryTypeListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactLogListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAttendanceDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorOverviewResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorRecordSearchDto;
import uk.gov.hmcts.juror.api.moj.controller.response.NameDetails;
import uk.gov.hmcts.juror.api.moj.controller.response.PaymentDetails;
import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorHistoryResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorPaymentsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.ContactCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryType;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.PendingJuror;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.domain.QReportsJurorPayments;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.IdCheckCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.PendingJurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactEnquiryTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorDetailRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.juror.JurorPaymentsSummaryRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.ReasonableAdjustmentsRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAuditChangeService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects",
    "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.NcssCount"})
class JurorRecordServiceTest {

    private static final String BUREAU_OWNER = "400";
    private static final String COURT_OWNER = "415";
    private static final String LOC_CODE = "415";
    private static final String VALID_JUROR_NUMBER = "123456789";

    @Mock
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    @Mock
    private PoolHistoryRepository poolHistoryRepository;
    @Mock
    private JurorAppearanceService jurorAppearanceService;
    @Mock
    private PanelRepository panelRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private ContactCodeRepository contactCodeRepository;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private CourtLocationService courtLocationService;
    @Mock
    private ContactLogRepository contactLogRepository;
    @Mock
    private ContactEnquiryTypeRepository contactEnquiryTypeRepository;
    @Mock
    private JurorDetailRepositoryMod jurorDetailRepositoryMod;
    @Mock
    private BureauService bureauService;
    @Mock
    private ResponseExcusalService responseExcusalService;
    @Mock
    private JurorAuditChangeService jurorAuditChangeService;
    @Mock
    private PrintDataService printDataService;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private PoolTypeRepository poolTypeRepository;
    @Mock
    private CourtLocationRepository courtLocationRepository;
    @Mock
    private GeneratePoolNumberService generatePoolNumberService;
    @Mock
    private PendingJurorRepository pendingJurorRepository;
    @Mock
    private PoolRequestRepository poolRequestRepository;
    @Mock
    private PendingJurorStatusRepository pendingJurorStatusRepository;
    @Mock
    private AppearanceRepository appearanceRepository;
    @Mock
    private ReasonableAdjustmentsRepository reasonableAdjustmentsRepository;
    @Mock
    private UserServiceModImpl userServiceMod;
    @Mock
    private JurorPaymentsSummaryRepository jurorPaymentsSummaryRepository;

    @Mock
    private Clock clock;
    @InjectMocks
    JurorRecordServiceImpl jurorRecordService;

    @Nested
    @DisplayName("void editJurorsBankDetails(RequestBankDetailsDto)")
    class EditJurorsBankDetails {

        @Test
        void happyPath() {
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSortCode("115578");
            dto.setAccountNumber("87654321");
            dto.setAccountHolderName("Mr Fname Lname");

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            when(jurorRepository.findById(jurorNumber)).thenReturn(Optional.of(juror));
            jurorRecordService.editJurorsBankDetails(dto);

            verify(jurorRepository, times(1)).findById(jurorNumber);
            verify(jurorRepository, times(1)).save(juror);
            verify(jurorHistoryService, times(1)).createEditBankSortCodeHistory(jurorNumber);
            verify(jurorHistoryService, times(1)).createEditBankAccountNameHistory(jurorNumber);
            verify(jurorHistoryService, times(1))
                .createEditBankAccountNumberHistory(jurorNumber);

            assertThat(juror.getBankAccountName()).isEqualTo(dto.getAccountHolderName());
            assertThat(juror.getBankAccountNumber()).isEqualTo(dto.getAccountNumber());
            assertThat(juror.getSortCode()).isEqualTo(dto.getSortCode());
        }

        @Test
        void jurorNumberNotFound() {
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSortCode("115578");
            dto.setAccountNumber("987654321");
            dto.setAccountHolderName("Mr Fname Lname");

            Juror juror = new Juror();

            assertThatExceptionOfType(MojException.NotFound.class)
                .isThrownBy(() -> jurorRecordService.editJurorsBankDetails(dto));

            verify(jurorRepository, times(1)).findById(jurorNumber);
            verify(jurorRepository, never()).save(juror);
            verify(jurorHistoryService, never()).createEditBankSortCodeHistory(jurorNumber);
            verify(jurorHistoryService, never()).createEditBankAccountNameHistory(jurorNumber);
            verify(jurorHistoryService, never()).createEditBankAccountNumberHistory(jurorNumber);
        }
    }

    @Test
    void testEditJurorRecord() {
        JurorPool jurorPool = createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER);

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(VALID_JUROR_NUMBER);
        final ReasonableAdjustments reasonableAdjustments = new ReasonableAdjustments();
        reasonableAdjustments.setDescription("Vision impairment");
        reasonableAdjustments.setCode("V");
        doReturn(Optional.of(reasonableAdjustments)).when(reasonableAdjustmentsRepository).findById(any());

        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto();
        jurorRecordService.editJurorDetails(buildPayload(BUREAU_OWNER), requestDto, VALID_JUROR_NUMBER);

        ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);
        verify(jurorRepository, times(1)).save(jurorArgumentCaptor.capture());
        Juror juror = jurorArgumentCaptor.getValue();
        assertThat(juror.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(juror.getFirstName()).isEqualTo(requestDto.getFirstName());
        assertThat(juror.getLastName()).isEqualTo(requestDto.getLastName());
        assertThat(juror.getDateOfBirth()).isEqualTo(requestDto.getDateOfBirth());

        assertThat(juror.getAddressLine1()).isEqualTo(requestDto.getAddressLineOne());
        assertThat(juror.getAddressLine2()).isEqualTo(requestDto.getAddressLineTwo());
        assertThat(juror.getAddressLine3()).isEqualTo(requestDto.getAddressLineThree());
        assertThat(juror.getAddressLine4()).isEqualTo(requestDto.getAddressTown());
        assertThat(juror.getAddressLine5()).isEqualTo(requestDto.getAddressCounty());
        assertThat(juror.getPostcode()).isEqualTo(requestDto.getAddressPostcode());

        assertThat(juror.getPhoneNumber()).isEqualTo(requestDto.getPrimaryPhone());
        assertThat(juror.getAltPhoneNumber()).isEqualTo(requestDto.getSecondaryPhone());
        assertThat(juror.getEmail()).isEqualTo(requestDto.getEmailAddress());

        assertThat(juror.getReasonableAdjustmentCode()).isEqualTo(requestDto.getSpecialNeed());
        assertThat(juror.getReasonableAdjustmentMessage()).isEqualTo(requestDto.getSpecialNeedMessage());

        assertThat(juror.getOpticRef()).isEqualTo(requestDto.getOpticReference());
        assertThat(juror.getPendingTitle()).isEqualTo(requestDto.getPendingTitle());
        assertThat(juror.getPendingFirstName()).isEqualTo(requestDto.getPendingFirstName());
        assertThat(juror.getPendingLastName()).isEqualTo(requestDto.getPendingLastName());
        assertThat(juror.getWelsh()).isNull();
    }

    @Test
    void testEditJurorRecordUpdateWelshFlagFromFalseToTrue() {
        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto();
        requestDto.setWelshLanguageRequired(true);

        JurorPool jurorPool = createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setWelsh(false);

        ReasonableAdjustments reasonableAdjustments = new ReasonableAdjustments();
        reasonableAdjustments.setDescription("Vision impairment");
        reasonableAdjustments.setCode("V");
        doReturn(Optional.of(reasonableAdjustments)).when(reasonableAdjustmentsRepository).findById(any());

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(VALID_JUROR_NUMBER);

        jurorRecordService.editJurorDetails(buildPayload(BUREAU_OWNER), requestDto, VALID_JUROR_NUMBER);

        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);
        verify(jurorRepository, times(1)).save(jurorArgumentCaptor.capture());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(VALID_JUROR_NUMBER);
        verify(jurorReasonableAdjustmentRepository, times(1)).save(any());

        Juror capturedJuror = jurorArgumentCaptor.getValue();
        assertThat(capturedJuror.getOpticRef()).isEqualTo(requestDto.getOpticReference());
        assertThat(capturedJuror.getWelsh()).isTrue();
    }

    @Test
    void testEditJurorRecordUpdateWelshFlagFromTrueToFalse() {
        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto();
        requestDto.setWelshLanguageRequired(false);

        JurorPool jurorPool = createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setWelsh(true);

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(VALID_JUROR_NUMBER);
        ReasonableAdjustments reasonableAdjustments = new ReasonableAdjustments();
        reasonableAdjustments.setDescription("Vision impairment");
        reasonableAdjustments.setCode("V");
        doReturn(Optional.of(reasonableAdjustments)).when(reasonableAdjustmentsRepository).findById(any());

        jurorRecordService.editJurorDetails(buildPayload(BUREAU_OWNER), requestDto, VALID_JUROR_NUMBER);

        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);
        verify(jurorRepository, times(1)).save(jurorArgumentCaptor.capture());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(VALID_JUROR_NUMBER);
        verify(jurorReasonableAdjustmentRepository, times(1)).save(any());

        Juror capturedJuror = jurorArgumentCaptor.getValue();
        assertThat(capturedJuror.getOpticRef()).isEqualTo(requestDto.getOpticReference());
        assertThat(capturedJuror.getWelsh()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEditJurorRecordUpdateNullWelshFlag(Boolean welshLanguageRequired) {
        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto();
        requestDto.setWelshLanguageRequired(welshLanguageRequired);

        JurorPool jurorPool = createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setWelsh(null);

        ReasonableAdjustments reasonableAdjustments = new ReasonableAdjustments();
        reasonableAdjustments.setDescription("Vision impairment");
        reasonableAdjustments.setCode("V");
        doReturn(Optional.of(reasonableAdjustments)).when(reasonableAdjustmentsRepository).findById(any());
        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(VALID_JUROR_NUMBER);

        jurorRecordService.editJurorDetails(buildPayload(BUREAU_OWNER), requestDto, VALID_JUROR_NUMBER);

        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);
        verify(jurorRepository, times(1)).save(jurorArgumentCaptor.capture());
        verify(jurorReasonableAdjustmentRepository, times(1)).findByJurorNumber(VALID_JUROR_NUMBER);
        verify(jurorReasonableAdjustmentRepository, times(1)).save(any());

        Juror capturedJuror = jurorArgumentCaptor.getValue();
        assertThat(capturedJuror.getOpticRef()).isEqualTo(requestDto.getOpticReference());
        assertThat(capturedJuror.getWelsh()).isEqualTo(welshLanguageRequired);
    }


    @Test
    void testEditJurorRecordRemovePendingNameChange() {
        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto();
        requestDto.setPendingTitle(null);
        requestDto.setPendingFirstName(null);
        requestDto.setPendingLastName(null);

        JurorPool jurorPool = createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setPendingTitle("Mx");
        juror.setPendingFirstName("Pending First Name");
        juror.setPendingLastName("Pending Last Name");

        ReasonableAdjustments reasonableAdjustments = new ReasonableAdjustments();
        reasonableAdjustments.setDescription("Vision impairment");
        reasonableAdjustments.setCode("V");
        doReturn(Optional.of(reasonableAdjustments)).when(reasonableAdjustmentsRepository).findById(any());

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(VALID_JUROR_NUMBER);

        jurorRecordService.editJurorDetails(buildPayload(BUREAU_OWNER), requestDto, VALID_JUROR_NUMBER);

        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);
        verify(jurorRepository, times(1)).save(jurorArgumentCaptor.capture());
        Juror capturedJuror = jurorArgumentCaptor.getValue();

        assertThat(capturedJuror.getOpticRef()).isEqualTo(requestDto.getOpticReference());
        assertThat(capturedJuror.getPendingTitle()).isEqualTo(requestDto.getPendingTitle());
        assertThat(capturedJuror.getPendingFirstName()).isEqualTo(requestDto.getPendingFirstName());
        assertThat(capturedJuror.getPendingLastName()).isEqualTo(requestDto.getPendingLastName());
    }

    @Test
    void testCheckJurorRecordDetailPhoneMobileAndHome() {
        String jurorNumber = "641500094";

        JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setPhoneNumber("0123456789");
        juror.setAltPhoneNumber("0987654321");
        juror.setWorkPhone("0543219876");

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());

        JurorDetailsResponseDto jurorDetailsResponseDto = jurorRecordService
            .getJurorDetails(buildPayload(COURT_OWNER), jurorNumber, LOC_CODE);

        assertThat(jurorDetailsResponseDto.getPrimaryPhone())
            .as("Expect the primary phone number to be the mobile number")
            .isEqualTo("0987654321");
        assertThat(jurorDetailsResponseDto.getSecondaryPhone())
            .as("Expect the secondary phone number to be the home number")
            .isEqualTo("0123456789");
    }

    @Test
    void testCheckJurorRecordDetailPhoneMobileAndWork() {
        String jurorNumber = "641500094";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setAltPhoneNumber("0987654321");
        juror.setWorkPhone("0543219876");

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());

        JurorDetailsResponseDto jurorDetailsResponseDto = jurorRecordService.getJurorDetails(buildPayload(COURT_OWNER),
            jurorNumber, LOC_CODE);

        assertThat(jurorDetailsResponseDto.getPrimaryPhone())
            .as("Expect the primary phone number to be the mobile number")
            .isEqualTo("0987654321");
        assertThat(jurorDetailsResponseDto.getSecondaryPhone())
            .as("Expect the secondary phone number to be the work number")
            .isEqualTo("0543219876");
    }

    @Test
    void testCheckJurorRecordDetailPhoneHomeAndWork() {
        String jurorNumber = "641500094";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setPhoneNumber("0123456789");
        juror.setWorkPhone("0543219876");

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());

        JurorDetailsResponseDto jurorDetailsResponseDto = jurorRecordService.getJurorDetails(buildPayload(COURT_OWNER),
            jurorNumber, LOC_CODE);

        assertThat(jurorDetailsResponseDto.getPrimaryPhone())
            .as("Expect the primary phone number to be the home number")
            .isEqualTo("0123456789");
        assertThat(jurorDetailsResponseDto.getSecondaryPhone())
            .as("Expect the secondary phone number to be the work number")
            .isEqualTo("0543219876");
    }

    @Test
    void testCheckJurorRecordDetailPhoneMobile() {
        String jurorNumber = "641500094";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setAltPhoneNumber("0987654321");

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());

        JurorDetailsResponseDto jurorDetailsResponseDto = jurorRecordService.getJurorDetails(buildPayload(COURT_OWNER),
            jurorNumber, LOC_CODE);

        assertThat(jurorDetailsResponseDto.getPrimaryPhone())
            .as("Expect the primary phone number to be the mobile number")
            .isEqualTo("0987654321");
        assertThat(jurorDetailsResponseDto.getSecondaryPhone())
            .as("Expect the secondary phone number to be null")
            .isNull();
    }

    @Test
    void testCheckJurorRecordDetailPhoneHome() {
        String jurorNumber = "641500094";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setPhoneNumber("0123456789");

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());

        JurorDetailsResponseDto jurorDetailsResponseDto = jurorRecordService.getJurorDetails(buildPayload(COURT_OWNER),
            jurorNumber, LOC_CODE);

        assertThat(jurorDetailsResponseDto.getPrimaryPhone())
            .as("Expect the primary phone number to be the home number")
            .isEqualTo("0123456789");
        assertThat(jurorDetailsResponseDto.getSecondaryPhone())
            .as("Expect the secondary phone number to be null")
            .isNull();
    }

    @Test
    void testCheckJurorRecordDetailPhoneWork() {
        String jurorNumber = "641500094";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setWorkPhone("0543219876");

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());

        JurorDetailsResponseDto jurorDetailsResponseDto = jurorRecordService.getJurorDetails(buildPayload(COURT_OWNER),
            jurorNumber, LOC_CODE);

        assertThat(jurorDetailsResponseDto.getPrimaryPhone())
            .as("Expect the primary phone number to be the work number")
            .isEqualTo("0543219876");
        assertThat(jurorDetailsResponseDto.getSecondaryPhone())
            .as("Expect the secondary phone number to be null")
            .isNull();
    }

    @Test
    void testCheckJurorRecordDetailManuallyCreatedJuror() {
        String jurorNumber = "641500094";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
        Juror juror = jurorPool.getJuror();
        juror.setWorkPhone("0543219876");

        PendingJuror pendingJuror = new PendingJuror();
        pendingJuror.setJurorNumber(jurorPool.getJurorNumber());
        pendingJuror.setPoolNumber(jurorPool.getPoolNumber());

        doReturn(jurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());
        doReturn(Optional.of(pendingJuror)).when(pendingJurorRepository).findById(jurorPool.getJurorNumber());

        JurorDetailsResponseDto jurorDetailsResponseDto = jurorRecordService.getJurorDetails(buildPayload(COURT_OWNER),
            jurorNumber, LOC_CODE);

        assertThat(jurorDetailsResponseDto.getCommonDetails().isManuallyCreated())
            .as("Expect juror to be manually created")
            .isEqualTo(true);
    }

    @Test
    void testCheckJurorRecordNotFound() {

        String jurorNumber = "641500094";
        doReturn(new ArrayList<>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(any(), anyBoolean());

        JurorOverviewResponseDto jurorOverviewResponseDto = jurorRecordService.getJurorOverview(buildPayload("415"),
            jurorNumber, LOC_CODE);

        assertThat(jurorOverviewResponseDto)
            .as("No Juror record matched the juror number")
            .isNull();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void testJurorOverviewResponseDtoContainsWelshFlag(Boolean welshFlag) {
        final String jurorNumber = "111111111";
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(LOC_CODE);
        final List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "400");

        List<JurorHistory> jurorHistoryList = new ArrayList<>();
        JurorHistory hist = new JurorHistory();
        hist.setJurorNumber(jurorNumber);
        hist.setHistoryCode(HistoryCodeMod.RESPONDED_POSITIVELY);
        jurorHistoryList.add(hist);

        jurorPools.get(0).getJuror().setWelsh(welshFlag);
        doReturn(jurorHistoryList).when(jurorHistoryRepository)
            .findByJurorNumberAndDateCreatedGreaterThanEqual(anyString(), any(LocalDate.class));

        doReturn(courtLocation).when(courtLocationService).getCourtLocation(LOC_CODE);
        doReturn(jurorPools.get(0)).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        JurorOverviewResponseDto expectedResponse = new JurorOverviewResponseDto();
        expectedResponse.setWelshLanguageRequired(welshFlag);

        JurorOverviewResponseDto actualResponse =
            jurorRecordService.getJurorOverview(buildPayload("400"), jurorNumber, LOC_CODE);

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);
        verify(jurorHistoryRepository, times(1))
            .findByJurorNumberAndDateCreatedGreaterThanEqual(anyString(), any(LocalDate.class));
        assertEquals(expectedResponse.getWelshLanguageRequired(), actualResponse.getWelshLanguageRequired(),
            "Welsh flag should be set to " + expectedResponse.getWelshLanguageRequired());
    }

    @Test
    void testCheckJurorRecordOverviewCourtUserDifferentCourt() {
        String jurorNumber = "416111111";
        String locCode = "416";
        when(jurorPoolRepository.findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(),
            any())).thenReturn(createValidJurorPool(jurorNumber, "416"));

        when(courtLocationService.getCourtLocation(any())).thenReturn(getCourtLocation());

        assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorRecordService.getJurorOverview(buildPayload("415"), jurorNumber, locCode));

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, getCourtLocation());
    }

    @Test
    void testSearchJurorRecordCourtUser() {

        String jurorNumber = "416010101";
        String owner = "416";
        when(jurorPoolRepository.findByJurorJurorNumberAndIsActive(any(), anyBoolean()))
            .thenReturn(createJurorPoolList(jurorNumber, owner));

        JurorRecordSearchDto jurorRecordSearchDto = jurorRecordService.searchJurorRecord(
            buildPayload("416"), jurorNumber);

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorRecordSearchDto.getData();

        assertThat(dataDto)
            .as("Expect there to be one juror record in the result list")
            .singleElement();

        JurorRecordSearchDto.JurorRecordSearchDataDto jurorSearchRecord = dataDto.get(0);

        assertThat(jurorSearchRecord.getJurorNumber())
            .as("Expect the juror number to be 416010101")
            .isEqualTo("416010101");
        assertThat(jurorSearchRecord.getPoolNumber())
            .as("Expect the pool number to be 641600090")
            .isEqualTo("641600090");
        assertThat(jurorSearchRecord.getCourtName())
            .as("Expect the court name to be TEST COURT")
            .isEqualTo("TEST COURT");
        assertThat(jurorSearchRecord.getCourtLocationCode())
            .as("Expect the court location to be 416")
            .isEqualTo("416");
        assertThat(jurorSearchRecord.getFirstName())
            .as("Expect the first name to be FIRSTNAME")
            .isEqualTo("FIRSTNAME");
        assertThat(jurorSearchRecord.getLastName())
            .as("Expect the first name to be LASTNAME")
            .isEqualTo("LASTNAME");
        assertThat(jurorSearchRecord.getAddressPostcode())
            .as("Expect the postcode to be M24 4GT")
            .isEqualTo("M24 4GT");
    }

    @Test
    void testSearchJurorRecordBureauUser() {
        String jurorNumber = "416010101";
        String owner = "416";
        when(jurorPoolRepository.findByJurorJurorNumberAndIsActive(any(), anyBoolean()))
            .thenReturn(createJurorPoolList(jurorNumber, owner));

        JurorRecordSearchDto jurorRecordSearchDto = jurorRecordService.searchJurorRecord(
            buildPayload("400"), jurorNumber);

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorRecordSearchDto.getData();

        assertThat(dataDto)
            .as("Expect there to be one juror record in the result list")
            .singleElement();

        JurorRecordSearchDto.JurorRecordSearchDataDto jurorSearchRecord = dataDto.get(0);

        assertThat(jurorSearchRecord.getJurorNumber())
            .as("Expect the juror number to be 416010101")
            .isEqualTo("416010101");
        assertThat(jurorSearchRecord.getPoolNumber())
            .as("Expect the pool number to be 641600090")
            .isEqualTo("641600090");
        assertThat(jurorSearchRecord.getCourtName())
            .as("Expect the court name to be TEST COURT")
            .isEqualTo("TEST COURT");
        assertThat(jurorSearchRecord.getCourtLocationCode())
            .as("Expect the court location to be 416")
            .isEqualTo("416");
        assertThat(jurorSearchRecord.getFirstName())
            .as("Expect the first name to be FIRSTNAME")
            .isEqualTo("FIRSTNAME");
        assertThat(jurorSearchRecord.getLastName())
            .as("Expect the first name to be LASTNAME")
            .isEqualTo("LASTNAME");
        assertThat(jurorSearchRecord.getAddressPostcode())
            .as("Expect the postcode to be M24 4GT")
            .isEqualTo("M24 4GT");
    }

    @Test
    void testSearchJurorRecordCourtUserBureauRecord() {
        String jurorNumber = "416010101";
        when(jurorPoolRepository.findByJurorJurorNumberAndIsActive(any(), anyBoolean()))
            .thenReturn(createJurorPoolList(jurorNumber, BUREAU_OWNER));

        JurorRecordSearchDto jurorRecordSearchDto = jurorRecordService.searchJurorRecord(
            buildPayload("416"), jurorNumber);

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorRecordSearchDto.getData();

        assertThat(dataDto)
            .as("Expect the result list to be empty")
            .isEmpty();
    }

    @Test
    void testSearchJurorRecordCourtUserNotOwnedRecord() {
        String jurorNumber = "416010101";
        when(jurorPoolRepository.findByJurorJurorNumberAndIsActive(any(), anyBoolean()))
            .thenReturn(createJurorPoolList(jurorNumber, COURT_OWNER));

        JurorRecordSearchDto jurorRecordSearchDto = jurorRecordService.searchJurorRecord(
            buildPayload("416"), jurorNumber);

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorRecordSearchDto.getData();

        assertThat(dataDto)
            .as("Expect the result list to be empty")
            .isEmpty();
    }

    @Test
    void testGetJurorContactLogsBureauUserBureauLogs() {
        final BureauJwtPayload payload = buildPayload(BUREAU_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER));
        ContactLog contactLog = createContactLog(VALID_JUROR_NUMBER,
            IContactCode.GENERAL, "Some test notes");

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(VALID_JUROR_NUMBER, true);
        doReturn(Collections.singletonList(contactLog)).when(contactLogRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER);
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER, true);


        ContactLogListDto contactLogListDto = jurorRecordService.getJurorContactLogs(payload, VALID_JUROR_NUMBER);

        assertThat(contactLogListDto.getData())
            .as("Expect one contact log data item to be mapped in to the DTO").hasSize(1);

        ContactLogListDto.ContactLogDataDto contactLogDataDto = contactLogListDto.getData().get(0);
        verifyContactLogData(contactLogDataDto, contactLog);
    }

    @Test
    void testGetJurorContactLogsBureauUserCourtLogs() {
        final BureauJwtPayload payload = buildPayload(BUREAU_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, COURT_OWNER));

        ContactLog contactLog = createContactLog(VALID_JUROR_NUMBER, IContactCode.GENERAL, "Some test notes");

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(VALID_JUROR_NUMBER, true);
        doReturn(Collections.singletonList(contactLog)).when(contactLogRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER);
        doReturn(jurorPools).when(jurorPoolRepository).findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER, true);


        ContactLogListDto contactLogListDto = jurorRecordService.getJurorContactLogs(payload, VALID_JUROR_NUMBER);

        assertThat(contactLogListDto.getData())
            .as("Expect one contact log data item to be mapped in to the DTO").hasSize(1);

        ContactLogListDto.ContactLogDataDto contactLogDataDto = contactLogListDto.getData().get(0);
        verifyContactLogData(contactLogDataDto, contactLog);
    }

    @Test
    void testGetJurorContactLogsBureauUserNoLogs() {
        final BureauJwtPayload payload = buildPayload(BUREAU_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER));

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(VALID_JUROR_NUMBER, true);
        doReturn(Collections.emptyList()).when(contactLogRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER);
        doReturn(jurorPools).when(jurorPoolRepository).findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER, true);


        ContactLogListDto contactLogListDto = jurorRecordService.getJurorContactLogs(payload, VALID_JUROR_NUMBER);

        assertThat(contactLogListDto.getData())
            .as("Expect no contact log data items to be mapped in to the DTO").isEmpty();
    }

    @Test
    void testGetJurorContactLogsBureauUserInvalidJuror() {
        BureauJwtPayload payload = buildPayload(BUREAU_OWNER);

        List<JurorPool> jurorPools = new ArrayList<>();

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(VALID_JUROR_NUMBER, true);

        assertThat(jurorPools).isEmpty();
        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorRecordService.getJurorContactLogs(payload, VALID_JUROR_NUMBER));
    }

    @Test
    void testGetJurorContactLogsCourtUserBureauLogs() {
        BureauJwtPayload payload = buildPayload(COURT_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER));

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER, true);

        assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorRecordService.getJurorContactLogs(payload, VALID_JUROR_NUMBER));
    }

    @Test
    void testGetJurorContactLogsCourtUserValidCourtLogs() {
        final BureauJwtPayload payload = buildPayload(COURT_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, COURT_OWNER));
        ContactLog contactLog1 = createContactLog(VALID_JUROR_NUMBER,
            IContactCode.GENERAL,
            "Some general test notes");
        ContactLog contactLog2 = createContactLog(VALID_JUROR_NUMBER,
            IContactCode.DISCUSS_DEFERRAL,
            "Some deferral test notes");

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER, true);
        doReturn(Arrays.asList(contactLog1, contactLog2)).when(contactLogRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER);

        ContactLogListDto contactLogListDto = jurorRecordService.getJurorContactLogs(payload, VALID_JUROR_NUMBER);

        assertThat(contactLogListDto.getData())
            .as("Expect two contact log data items to be mapped in to the DTO").hasSize(2);

        ContactLogListDto.ContactLogDataDto contactLogDataDto1 = contactLogListDto.getData().get(0);
        ContactLogListDto.ContactLogDataDto contactLogDataDto2 = contactLogListDto.getData().get(1);
        verifyContactLogData(contactLogDataDto1, contactLog1);
        verifyContactLogData(contactLogDataDto2, contactLog2);
    }

    @Test
    void testGetJurorContactLogsCourtUserDifferentCourt() {
        BureauJwtPayload payload = buildPayload(COURT_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, "799"));

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER, true);

        assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorRecordService.getJurorContactLogs(payload, VALID_JUROR_NUMBER));
    }

    private void verifyContactLogData(ContactLogListDto.ContactLogDataDto contactLogDataDto, ContactLog contactLog) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy KK:mm a", Locale.ENGLISH);
        String logDate = contactLog.getStartCall().format(formatter);

        assertThat(contactLogDataDto.getLogDate())
            .as("Expect the log date to be mapped from the original contact log object and formatted")
            .isEqualTo(logDate);
        assertThat(contactLogDataDto.getUsername())
            .as("Expect the username to be mapped from the original contact log object")
            .isEqualTo(contactLog.getUsername());
        assertThat(contactLogDataDto.getEnquiryType())
            .as("Expect the enquiry type to be mapped from the original contact log object")
            .isEqualTo(contactLog.getEnquiryType().getDescription());
        assertThat(contactLogDataDto.getNotes())
            .as("Expect the Notes to be mapped from the original contact log object")
            .isEqualTo(contactLog.getNotes());
    }

    @Test
    void testCreateJurorContactLogBureauUserHappyPath() {
        final ArgumentCaptor<ContactLog> contactArgumentCaptor = ArgumentCaptor.forClass(ContactLog.class);
        final BureauJwtPayload payload = buildPayload(BUREAU_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER));
        ContactCode contactEnquiryType = new ContactCode(IContactCode.GENERAL.getCode(),
            IContactCode.GENERAL.getDescription());
        ContactLogRequestDto requestDto = createContactLogRequestDto(VALID_JUROR_NUMBER, IContactCode.GENERAL);
        LocalDateTime startCall = LocalDateTime.now();
        requestDto.setStartCall(startCall);


        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(VALID_JUROR_NUMBER, true);
        doReturn(Optional.of(contactEnquiryType)).when(contactCodeRepository)
            .findById(IContactCode.GENERAL.getCode());
        doReturn(null).when(contactLogRepository)
            .saveAndFlush(any());

        assertThatNoException().isThrownBy(() -> jurorRecordService.createJurorContactLog(payload, requestDto));

        verify(contactLogRepository, times(1))
            .saveAndFlush(contactArgumentCaptor.capture());
        ContactLog contactLog = contactArgumentCaptor.getValue();

        assertThat(contactLog.getEnquiryType()).isEqualTo(contactEnquiryType);
        assertThat(contactLog.getJurorNumber()).isEqualTo(VALID_JUROR_NUMBER);
        assertThat(contactLog.getStartCall()).isEqualTo(startCall);
    }

    @Test
    void testCreateJurorContactLogBureauUserJurorNotFound() {
        BureauJwtPayload payload = buildPayload(BUREAU_OWNER);
        ContactLogRequestDto requestDto = createContactLogRequestDto(VALID_JUROR_NUMBER, IContactCode.GENERAL);

        List<JurorPool> jurorPools = new ArrayList<>();

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(VALID_JUROR_NUMBER, true);

        assertThat(jurorPools).isEmpty();
        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorRecordService.createJurorContactLog(payload, requestDto));

        verify(contactLogRepository, never()).saveAndFlush(any());
    }

    @Test
    void testCreateJurorContactLogBureauUserCourtOwnedRecord() {
        final ArgumentCaptor<ContactLog> contactArgumentCaptor = ArgumentCaptor.forClass(ContactLog.class);
        final BureauJwtPayload payload = buildPayload(BUREAU_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, COURT_OWNER));

        ContactCode contactEnquiryType = new ContactCode(IContactCode.GENERAL.getCode(),
            IContactCode.GENERAL.getDescription());
        ContactLogRequestDto requestDto = createContactLogRequestDto(VALID_JUROR_NUMBER, IContactCode.GENERAL);
        LocalDateTime startCall = LocalDateTime.now();
        requestDto.setStartCall(startCall);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(VALID_JUROR_NUMBER, true);
        doReturn(Optional.of(contactEnquiryType)).when(contactCodeRepository)
            .findById(IContactCode.GENERAL.getCode());

        ContactLog contactLog = new ContactLog();
        doReturn(contactLog).when(contactLogRepository)
            .saveAndFlush(any());

        assertThatNoException().isThrownBy(() -> jurorRecordService.createJurorContactLog(payload, requestDto));

        verify(contactLogRepository, times(1))
            .saveAndFlush(contactArgumentCaptor.capture());
        ContactLog contactCaptor = contactArgumentCaptor.getValue();

        assertThat(contactCaptor.getEnquiryType()).isEqualTo(contactEnquiryType);
        assertThat(contactCaptor.getJurorNumber()).isEqualTo(VALID_JUROR_NUMBER);
        assertThat(contactCaptor.getStartCall()).isEqualTo(startCall);
    }

    @Test
    void testCreateJurorContactLogCourtUserHappyPath() {
        final ArgumentCaptor<ContactLog> contactArgumentCaptor = ArgumentCaptor.forClass(ContactLog.class);
        final BureauJwtPayload payload = buildPayload(COURT_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, "415"));
        ContactCode contactEnquiryType =
            new ContactCode(IContactCode.GENERAL.getCode(), IContactCode.GENERAL.getDescription());
        ContactLogRequestDto requestDto = createContactLogRequestDto(VALID_JUROR_NUMBER, IContactCode.GENERAL);
        LocalDateTime startCall = LocalDateTime.now();
        requestDto.setStartCall(startCall);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER, true);
        doReturn(Optional.of(contactEnquiryType)).when(contactCodeRepository)
            .findById(IContactCode.GENERAL.getCode());
        doReturn(null).when(contactLogRepository)
            .saveAndFlush(any());

        assertThatNoException().isThrownBy(() -> jurorRecordService.createJurorContactLog(payload, requestDto));

        verify(contactLogRepository, times(1))
            .saveAndFlush(contactArgumentCaptor.capture());
        ContactLog contactLog = contactArgumentCaptor.getValue();
        assertThat(contactLog.getEnquiryType()).isEqualTo(contactEnquiryType);

        assertThat(contactLog.getJurorNumber()).isEqualTo(VALID_JUROR_NUMBER);
        assertThat(contactLog.getStartCall()).isEqualTo(startCall);
    }

    @Test
    void testCreateJurorContactLogCourtUserBureauOwnedRecord() {
        final BureauJwtPayload payload = buildPayload(COURT_OWNER);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(VALID_JUROR_NUMBER, BUREAU_OWNER));

        ContactEnquiryType contactEnquiryType = new ContactEnquiryType(ContactEnquiryCode.GE, "General Enquiry");
        ContactLog contactLog = new ContactLog();
        ContactLogRequestDto requestDto = createContactLogRequestDto(VALID_JUROR_NUMBER, IContactCode.GENERAL);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER, true);
        doReturn(Optional.of(contactEnquiryType)).when(contactEnquiryTypeRepository)
            .findById(ContactEnquiryCode.GE);
        doReturn(contactLog).when(contactLogRepository)
            .saveAndFlush(any());

        assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorRecordService.createJurorContactLog(payload, requestDto));

        verify(contactLogRepository, never()).saveAndFlush(any());
    }

    @Test
    void testCreateJurorContactLogCourtUserDifferentCourt() {
        final BureauJwtPayload payload = buildPayload("415");
        String jurorNumber = "123456789";
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, "416"));
        ContactEnquiryType contactEnquiryType = new ContactEnquiryType(ContactEnquiryCode.GE, "General Enquiry");
        ContactLog contactLog = new ContactLog();
        ContactLogRequestDto requestDto = createContactLogRequestDto(jurorNumber, IContactCode.GENERAL);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(Optional.of(contactEnquiryType)).when(contactEnquiryTypeRepository)
            .findById(ContactEnquiryCode.GE);
        doReturn(contactLog).when(contactLogRepository)
            .saveAndFlush(any());

        assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorRecordService.createJurorContactLog(payload, requestDto));

        verify(contactLogRepository, never()).saveAndFlush(any());
    }

    @Test
    void testCreateJurorContactLogCourtUserInvalidEnquiryType() {
        final BureauJwtPayload payload = buildPayload("415");
        String jurorNumber = "123456789";
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, "415"));
        ContactLog contactLog = new ContactLog();
        ContactLogRequestDto requestDto = createContactLogRequestDto(jurorNumber, IContactCode.GENERAL);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(contactLog).when(contactLogRepository).saveAndFlush(any());

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> jurorRecordService.createJurorContactLog(payload, requestDto));

        verify(contactLogRepository, never()).saveAndFlush(any());
    }

    private JurorPool createValidJurorPool(String jurorNumber, String owner) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setLocCourtName("CHESTER");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("jurorPool1");
        juror.setLastName("jurorPool1L");
        juror.setPostcode("M24 4GT");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setPool(poolRequest);
        jurorPool.setStatus(createJurorStatus(IJurorStatus.RESPONDED));
        jurorPool.setDateCreated(LocalDateTime.now().minusDays(3));

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    private List<JurorPool> createJurorPoolList(String jurorNumber, String owner) {

        final List<JurorPool> jurorPoolList = new ArrayList<>();

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("416");
        courtLocation.setName("TEST COURT");
        courtLocation.setLocCourtName("TEST COURT LONG NAME");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("641600090");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("FIRSTNAME");
        juror.setLastName("LASTNAME");
        juror.setPostcode("M24 4GT");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setStatus(createJurorStatus(IJurorStatus.RESPONDED));
        jurorPool.setPool(poolRequest);
        jurorPool.setDateCreated(LocalDateTime.now().minusDays(5));

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        jurorPoolList.add(jurorPool);

        return jurorPoolList;
    }

    private CourtLocation getCourtLocation() {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("TEST COURT");
        courtLocation.setName("TEST NAME");
        courtLocation.setLocCode("416");
        return courtLocation;
    }

    private BureauJwtPayload buildPayload(String owner) {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login("SOME_USER")
            .owner(owner)
            .build();
    }

    private ContactLog createContactLog(String jurorNumber,
                                        IContactCode contactCode, String notes) {
        return ContactLog.builder()
            .username("Test User")
            .jurorNumber(jurorNumber)
            .startCall(LocalDateTime.now())
            .enquiryType(new ContactCode(contactCode.getCode(), contactCode.getDescription()))
            .notes(notes)
            .lastUpdate(LocalDateTime.now())
            .repeatEnquiry(false)
            .build();
    }

    private ContactLogRequestDto createContactLogRequestDto(String jurorNumber, IContactCode enquiryCode) {
        ContactLogRequestDto contactLogRequestDto = new ContactLogRequestDto();
        contactLogRequestDto.setJurorNumber(jurorNumber);
        contactLogRequestDto.setEnquiryType(enquiryCode.getCode());
        return contactLogRequestDto;
    }

    @Test
    void testGetContactEnquiryTypes() {
        ContactEnquiryCode[] enquiryCodes = ContactEnquiryCode.values();
        List<ContactEnquiryType> enquiryTypes = new ArrayList<>();
        for (ContactEnquiryCode enquiryCode : enquiryCodes) {
            enquiryTypes.add(new ContactEnquiryType(enquiryCode, enquiryCode.toString()));
        }
        doReturn(enquiryTypes).when(contactEnquiryTypeRepository).findAll();

        ContactEnquiryTypeListDto dto = jurorRecordService.getContactEnquiryTypes();

        assertThat(dto).as("Expect a valid dto to be instantiated").isNotNull();
        assertThat(dto.getData())
            .as("Expect all enquiry codes to be mapped in to the dto").hasSize(enquiryCodes.length);
    }

    @Test
    void testGetJurorNotesBureauUserBureauOwnedRecord() {
        String owner = "415";
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, owner));
        JurorPool jurorPool = jurorPools.get(0);
        Juror juror = jurorPool.getJuror();
        juror.setNotes(notes);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThat(jurorRecordService.getJurorNotes(jurorNumber, owner).getNotes())
            .as("Expect the Pool Member to be found and the notes property to be returned")
            .isEqualTo(notes);
    }

    @Test
    void testGetJurorNotesBureauUserCourtOwnedRecord() {
        final String owner = "415";
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, "415"));
        JurorPool jurorPool = jurorPools.get(0);
        Juror juror = jurorPool.getJuror();
        juror.setNotes(notes);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThat(jurorRecordService.getJurorNotes(jurorNumber, owner).getNotes())
            .as("Expect the Pool Member to be found and the notes property to be returned")
            .isEqualTo(notes);
    }

    @Test
    void testGetJurorNotesBureauUserJurorPoolNotFound() {
        String owner = "415";
        String jurorNumber = "123456789";

        List<JurorPool> jurorPools = new ArrayList<>();

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThat(jurorPools).isEmpty();
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorRecordService.getJurorNotes(jurorNumber, owner));
    }

    @Test
    void testGetJurorNotesBureauUserNoNotes() {
        String owner = "415";
        String jurorNumber = "123456789";
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, owner));

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThat(jurorRecordService.getJurorNotes(jurorNumber, owner).getNotes())
            .as("Expect the Pool Member to be found but no notes data to be present")
            .isNull();
    }

    @Test
    void testGetJurorNotesCourtUserCourtOwnedRecord() {
        String owner = "415";
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, owner));
        JurorPool jurorPool = jurorPools.get(0);
        Juror juror = jurorPool.getJuror();
        juror.setNotes(notes);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThat(jurorRecordService.getJurorNotes(jurorNumber, owner).getNotes())
            .as("Expect the Pool Member to be found and the notes property to be returned")
            .isEqualTo(notes);
    }

    @Test
    void testGetJurorNotesCourtUserBureauOwnedRecord() {
        final String owner = "415";
        String jurorNumber = "123456789";
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, "400"));

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorRecordService.getJurorNotes(jurorNumber, owner));
    }

    @Test
    void testGetJurorNotesCourtUserDifferentCourt() {
        final String owner = "415";
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, "416"));
        JurorPool jurorPool = jurorPools.get(0);
        Juror juror = jurorPool.getJuror();
        juror.setNotes(notes);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorRecordService.getJurorNotes(jurorNumber, owner));
    }

    @Test
    void testSetJurorNotesBureauUserBureauOwnedRecord() {
        String owner = "400";
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, owner);

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(jurorNumber);

        assertThatNoException().isThrownBy(() -> jurorRecordService.setJurorNotes(jurorNumber, notes, owner));
    }

    @Test
    void testSetJurorNotesBureauUserCourtOwnedRecord() {
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(jurorNumber);
        assertThatNoException().isThrownBy(() -> jurorRecordService.setJurorNotes(jurorNumber, notes, BUREAU_OWNER));
    }

    @Test
    void testSetJurorNotesCourtUserCourtOwnedRecord() {
        String owner = "415";
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, owner);

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(jurorNumber);

        assertThatNoException().isThrownBy(() -> jurorRecordService.setJurorNotes(jurorNumber, notes, owner));
    }

    @Test
    void testSetJurorNotesCourtUserBureauOwnedRecord() {
        String owner = "415";
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, "400");

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(jurorNumber);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorRecordService.setJurorNotes(jurorNumber, notes, owner));
    }

    @Test
    void testSetJurorNotesCourtUserDifferentCourt() {
        String owner = "415";
        String jurorNumber = "123456789";
        String notes = "Some example notes";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, "416");

        doReturn(Optional.of(jurorPool.getJuror())).when(jurorRepository).findById(jurorNumber);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorRecordService.setJurorNotes(jurorNumber, notes, owner));
    }

    @Test
    void testGetBureauDetailsByJurorNumberBureauUserHappyPath() {
        String bureauOwnerCode = "400";
        String jurorNumber = "123456789";

        doReturn(createJurorPoolList(jurorNumber, bureauOwnerCode)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(Optional.of(new ModJurorDetail())).when(jurorDetailRepositoryMod).findById(jurorNumber);
        doReturn(createBureauJurorDetailDto(jurorNumber)).when(bureauService).mapJurorDetailsToDto(any());

        jurorRecordService.getBureauDetailsByJurorNumber(jurorNumber, bureauOwnerCode);

        // current user is bureau so no need to query records and check ownership for read only permission
        verify(jurorPoolRepository, times(2))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        verify(jurorDetailRepositoryMod, times(1)).findById(jurorNumber);
        verify(bureauService, times(1)).mapJurorDetailsToDto(any());
    }

    @Test
    void testGetBureauDetailsByJurorNumberCourtUserHappyPath() {
        String courtOwnerCode = "415";
        String jurorNumber = "123456789";
        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, courtOwnerCode);
        jurorPools.add(createValidJurorPool(jurorNumber, "435"));

        doReturn(jurorPools).when(jurorPoolRepository).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(Optional.of(new ModJurorDetail())).when(jurorDetailRepositoryMod).findById(jurorNumber);

        doReturn(createBureauJurorDetailDto(jurorNumber)).when(bureauService).mapJurorDetailsToDto(any());

        jurorRecordService.getBureauDetailsByJurorNumber(jurorNumber, courtOwnerCode);

        verify(jurorPoolRepository, times(2)).findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorDetailRepositoryMod, times(1)).findById(jurorNumber);
        verify(bureauService, times(1)).mapJurorDetailsToDto(any());
    }

    @Test
    void testGetBureauDetailsByJurorNumberNoMojDetails() {
        String bureauOwnerCode = "400";
        String jurorNumber = "123456789";
        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, bureauOwnerCode);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(Optional.empty()).when(jurorDetailRepositoryMod).findById(jurorNumber);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorRecordService.getBureauDetailsByJurorNumber(jurorNumber, bureauOwnerCode));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorDetailRepositoryMod, times(1)).findById(jurorNumber);
        verify(bureauService, never()).mapJurorDetailsToDto(any());
    }

    @Test
    void testGetBureauDetailsByJurorNumberInvalidPermission() {
        String jurorNumber = "123456789";
        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "415");
        jurorPools.add(createValidJurorPool(jurorNumber, "435"));

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorRecordService.getBureauDetailsByJurorNumber(jurorNumber, "411"));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorDetailRepositoryMod, never()).findById(jurorNumber);
        verify(bureauService, never()).mapJurorDetailsToDto(any());
    }

    @ParameterizedTest
    @EnumSource(value = PoliceCheck.class, mode = EnumSource.Mode.INCLUDE,
        names = {"NOT_CHECKED"})
    @NullSource
    void testBureauGetJurorOverviewPoliceCheckStatusNotChecked(PoliceCheck policeCheck) {
        final String jurorNumber = "111111111";
        final String locCode = "415";
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "400");
        doReturn(courtLocation).when(courtLocationService).getCourtLocation(locCode);

        jurorPools.get(0).setStatus(createJurorStatus(IJurorStatus.RESPONDED));
        Juror juror = jurorPools.get(0).getJuror();
        juror.setPoliceCheck(policeCheck);
        doReturn(jurorPools.get(0)).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        JurorOverviewResponseDto jurorOverviewResponseDto = jurorRecordService.getJurorOverview(buildPayload("400"),
            jurorNumber, locCode);

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);
        assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck()).as("Excepted status to be 'Not "
            + "Checked'").isEqualTo(policeCheck);
    }

    @Test
    void testBureauGetJurorOverviewPoliceCheckStatusNotCheckedThereWasAProblem() {
        final String jurorNumber = "111111111";
        final String locCode = "415";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);

        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "400");
        JurorPool jurorPool = jurorPools.get(0);
        jurorPool.setStatus(createJurorStatus(IJurorStatus.RESPONDED));

        Juror juror = jurorPool.getJuror();
        juror.setPoliceCheck(PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED);

        doReturn(courtLocation).when(courtLocationService).getCourtLocation(locCode);
        doReturn(jurorPools.get(0)).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        JurorOverviewResponseDto jurorOverviewResponseDto = jurorRecordService.getJurorOverview(buildPayload("400"),
            jurorNumber, locCode);

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);
        assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck()).as("Police check status")
            .isEqualTo(PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED);
    }

    @ParameterizedTest
    @EnumSource(value = PoliceCheck.class, mode = EnumSource.Mode.INCLUDE,
        names = {"IN_PROGRESS"})
    void testBureauGetJurorOverviewPoliceCheckStatusInProgress(PoliceCheck policeCheck) {
        final String jurorNumber = "111111111";
        final String locCode = "415";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);

        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "400");
        JurorPool jurorPool = jurorPools.get(0);
        jurorPool.setStatus(createJurorStatus(IJurorStatus.RESPONDED));

        Juror juror = jurorPool.getJuror();
        juror.setPoliceCheck(policeCheck);

        doReturn(courtLocation).when(courtLocationService).getCourtLocation(locCode);
        doReturn(jurorPools.get(0)).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        JurorOverviewResponseDto jurorOverviewResponseDto = jurorRecordService.getJurorOverview(buildPayload("400"),
            jurorNumber, locCode);

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);
        assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck()).as("Excepted status to be 'In "
            + "Progress'").isEqualTo(PoliceCheck.IN_PROGRESS);
    }

    @Test
    void testBureauGetJurorOverviewPoliceCheckStatusPassed() {
        final String jurorNumber = "111111111";
        final String locCode = "415";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);

        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "400");

        JurorPool jurorPool = jurorPools.get(0);
        jurorPool.setStatus(createJurorStatus(IJurorStatus.RESPONDED));

        Juror juror = jurorPool.getJuror();
        juror.setPoliceCheck(PoliceCheck.ELIGIBLE);

        doReturn(courtLocation).when(courtLocationService).getCourtLocation(locCode);
        doReturn(jurorPools.get(0)).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        JurorOverviewResponseDto jurorOverviewResponseDto = jurorRecordService.getJurorOverview(buildPayload("400"),
            jurorNumber, locCode);

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);
        assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck()).as("Police Check status")
            .isEqualTo(PoliceCheck.ELIGIBLE);
    }

    @Test
    void testBureauGetJurorOverviewJurorResponded() {
        final String jurorNumber = "111111111";
        final String locCode = "415";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);

        JurorStatus status = new JurorStatus();
        status.setStatus(IJurorStatus.RESPONDED);

        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "400");

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        jurorPools.get(0).setStatus(jurorStatus);

        JurorResponse response = new JurorResponse();
        response.setJurorNumber(jurorNumber);

        doReturn(courtLocation).when(courtLocationService).getCourtLocation(locCode);
        doReturn(jurorPools.get(0)).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        jurorRecordService.getJurorOverview(buildPayload("400"),
            jurorNumber, locCode);

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);
        verify(jurorResponseRepository, times(1)).findByJurorNumber(any(String.class));
    }

    @Test
    void testBureauGetJurorOverviewPoliceCheckStatusFailed() {
        final String jurorNumber = "111111111";
        final String locCode = "415";
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "400");

        Juror juror = jurorPools.get(0).getJuror();
        juror.setPoliceCheck(PoliceCheck.INELIGIBLE);

        doReturn(courtLocation).when(courtLocationService).getCourtLocation(locCode);
        doReturn(jurorPools.get(0)).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        JurorOverviewResponseDto jurorOverviewResponseDto = jurorRecordService.getJurorOverview(buildPayload("400"),
            jurorNumber, locCode);

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);
        assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck()).as("Police check status")
            .isEqualTo(PoliceCheck.INELIGIBLE);
    }

    @Test
    void testBureauGetJurorSummonsReplyPaperResponse() {
        final String jurorNumber = "111111111";
        final String locCode = "415";
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        final List<JurorPool> jurorPools = createJurorPoolList(jurorNumber, "400");

        List<JurorHistory> jurorHistoryList = new ArrayList<>();
        JurorHistory hist = new JurorHistory();
        hist.setJurorNumber(jurorNumber);
        hist.setHistoryCode(HistoryCodeMod.RESPONDED_POSITIVELY);
        jurorHistoryList.add(hist);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0); // same for minutes and seconds

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        jurorPools.get(0).setStatus(jurorStatus);
        doReturn(jurorHistoryList).when(jurorHistoryRepository)
            .findByJurorNumberAndDateCreatedGreaterThanEqual(anyString(), any(LocalDate.class));

        doReturn(courtLocation).when(courtLocationService).getCourtLocation(locCode);
        doReturn(jurorPools.get(0)).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);

        jurorRecordService.getJurorOverview(buildPayload("400"), jurorNumber, locCode);

        verify(jurorPoolRepository, times(1))
            .findByJurorNumberAndIsActiveAndCourt(jurorNumber, true, courtLocation);
        verify(jurorHistoryRepository, times(1))
            .findByJurorNumberAndDateCreatedGreaterThanEqual(anyString(), any(LocalDate.class));
    }

    @Test
    void testSetPendingNameChangeExistingJurorNoNameValues() {
        final String bureauOwner = "400";
        final String jurorNumber = "111111111";
        String poolNumber = "415230101";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setLocCourtName("CHESTER");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);

        Juror originalJuror = new Juror();
        originalJuror.setJurorNumber(jurorNumber);
        originalJuror.setOpticRef("12345678");
        originalJuror.setTitle(null);
        originalJuror.setFirstName("First");
        originalJuror.setLastName("Last");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setPool(poolRequest);
        jurorPool.setOwner(bureauOwner);

        originalJuror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(originalJuror);

        doReturn(null).when(jurorRepository).save(any(Juror.class));

        String pendingTitle = "Mx";
        String pendingFirstName = "Test";
        String pendingLastName = "Person";

        jurorRecordService.setPendingNameChange(originalJuror, pendingTitle, pendingFirstName, pendingLastName);

        verify(jurorRepository, times(1)).save(any(Juror.class));

        Juror updatedJuror = jurorPool.getJuror();
        assertThat(updatedJuror)
            .as("Expect a Pool Member Ext record to exist")
            .isNotNull();
        assertThat(updatedJuror.getPendingTitle())
            .as("Expect the Pool Member Ext record to be updated with the pending values")
            .isEqualTo(pendingTitle);
        assertThat(updatedJuror.getPendingFirstName())
            .as("Expect the Pool Member Ext record to be updated with the pending values")
            .isEqualTo(pendingFirstName);
        assertThat(updatedJuror.getPendingLastName())
            .as("Expect the Pool Member Ext record to be updated with the pending values")
            .isEqualTo(pendingLastName);

        assertThat(originalJuror.getTitle())
            .as("Expect Pool Member record not to be updated")
            .isNull();
        assertThat(originalJuror.getFirstName())
            .as("Expect Pool Member record not to be updated")
            .isEqualToIgnoringCase("First");
        assertThat(originalJuror.getLastName())
            .as("Expect Pool Member record not to be updated")
            .isEqualToIgnoringCase("Last");
    }

    @Test
    void testSetPendingNameChangeExistingJurorExistingNameValues() {
        final String bureauOwner = "400";
        final String jurorNumber = "111111111";
        String poolNumber = "415230101";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setLocCourtName("CHESTER");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);

        Juror originalJuror = new Juror();
        originalJuror.setJurorNumber(jurorNumber);
        originalJuror.setOpticRef("12345678");
        originalJuror.setPendingTitle("Dr");
        originalJuror.setPendingFirstName("Some");
        originalJuror.setPendingLastName("Name");
        originalJuror.setTitle(null);
        originalJuror.setFirstName("First");
        originalJuror.setLastName("Last");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setPool(poolRequest);
        jurorPool.setOwner(bureauOwner);
        jurorPool.setJuror(originalJuror);

        doReturn(null).when(jurorRepository).save(any(Juror.class));

        String pendingTitle = "Mx";
        String pendingFirstName = "Test";
        String pendingLastName = "Person";

        jurorRecordService.setPendingNameChange(originalJuror, pendingTitle, pendingFirstName, pendingLastName);

        verify(jurorRepository, times(1)).save(any(Juror.class));

        Juror updatedJuror = jurorPool.getJuror();
        assertThat(updatedJuror)
            .as("Expect a Pool Member Ext record to exist")
            .isNotNull();
        assertThat(updatedJuror.getPendingTitle())
            .as("Expect the Pool Member Ext record to be updated with the pending values")
            .isEqualTo(pendingTitle);
        assertThat(updatedJuror.getPendingFirstName())
            .as("Expect the Pool Member Ext record to be updated with the pending values")
            .isEqualTo(pendingFirstName);
        assertThat(updatedJuror.getPendingLastName())
            .as("Expect the Pool Member Ext record to be updated with the pending values")
            .isEqualTo(pendingLastName);

        assertThat(originalJuror.getTitle())
            .as("Expect Pool Member record not to be updated")
            .isNull();
        assertThat(originalJuror.getFirstName())
            .as("Expect Pool Member record not to be updated")
            .isEqualToIgnoringCase("First");
        assertThat(originalJuror.getLastName())
            .as("Expect Pool Member record not to be updated")
            .isEqualToIgnoringCase("Last");
    }

    @Test
    void testSetPendingNameChangeNoExistingJuror() {
        final String bureauOwner = "400";
        final String jurorNumber = "111111111";
        String poolNumber = "415230101";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setLocCourtName("CHESTER");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setTitle(null);
        juror.setFirstName("First");
        juror.setLastName("Last");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setPool(poolRequest);
        jurorPool.setOwner(bureauOwner);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        doReturn(null).when(jurorRepository).save(any(Juror.class));

        String pendingTitle = "Mx";
        String pendingFirstName = "Test";
        String pendingLastName = "Person";

        jurorRecordService.setPendingNameChange(juror, pendingTitle, pendingFirstName, pendingLastName);

        verify(jurorRepository, times(1)).save(any(Juror.class));

        assertThat(juror)
            .as("Expect a Pool Member Ext record to be created")
            .isNotNull();
        assertThat(juror.getPendingTitle())
            .as("Expect the Pool Member Ext record to be initialised with the pending values")
            .isEqualTo(pendingTitle);
        assertThat(juror.getPendingFirstName())
            .as("Expect the Pool Member Ext record to be initialised with the pending values")
            .isEqualTo(pendingFirstName);
        assertThat(juror.getPendingLastName())
            .as("Expect the Pool Member Ext record to be initialised with the pending values")
            .isEqualTo(pendingLastName);

        assertThat(juror.getTitle())
            .as("Expect Pool Member record not to be updated")
            .isNull();
        assertThat(juror.getFirstName())
            .as("Expect Pool Member record not to be updated")
            .isEqualToIgnoringCase("First");
        assertThat(juror.getLastName())
            .as("Expect Pool Member record not to be updated")
            .isEqualToIgnoringCase("Last");
    }

    @Test
    void testFixErrorInJurorNameAllChanged() {
        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);
        String bureauOwner = "400";
        String username = "BUREAU_USER";
        String jurorNumber = "111111111";

        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, username);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, bureauOwner);
        jurorPool.getPool().setPoolNumber("123456789");
        final JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "First", "Last");

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        doReturn(initChangedPropertyMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        doNothing().when(jurorAuditChangeService).recordPersonalDetailsHistory(anyString(),
            any(Juror.class), anyString(), anyString());

        jurorRecordService.fixErrorInJurorName(payload, jurorNumber, dto);

        verify(jurorRepository, times(1)).save(jurorArgumentCaptor.capture());
        Juror capturedJuror = jurorArgumentCaptor.getValue();
        assertThat(capturedJuror.getTitle()).isEqualTo(dto.getTitle());
        assertThat(capturedJuror.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(capturedJuror.getLastName()).isEqualTo(dto.getLastName());

        verify(jurorAuditChangeService, times(1))
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, times(3))
            .recordPersonalDetailsHistory(anyString(), any(Juror.class), anyString(),
                anyString());
    }

    @Test
    void testFixErrorInJurorNameNoChanges() {
        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);
        String bureauOwner = "400";
        String username = "BUREAU_USER";
        String jurorNumber = "111111111";

        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, username);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, bureauOwner);
        jurorPool.getPool().setPoolNumber("123456789");

        final Juror juror = jurorPool.getJuror();

        JurorNameDetailsDto dto = new JurorNameDetailsDto();
        BeanUtils.copyProperties(jurorPool, dto);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        doReturn(initChangedPropertyMap(Boolean.FALSE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        doNothing().when(jurorAuditChangeService).recordPersonalDetailsHistory(anyString(),
            any(Juror.class), anyString(), anyString());

        jurorRecordService.fixErrorInJurorName(payload, jurorNumber, dto);

        verify(jurorRepository, times(1)).save(jurorArgumentCaptor.capture());
        Juror capturedJuror = jurorArgumentCaptor.getValue();
        assertThat(capturedJuror.getTitle()).isEqualTo(juror.getTitle());
        assertThat(capturedJuror.getFirstName()).isEqualTo(juror.getFirstName());
        assertThat(capturedJuror.getLastName()).isEqualTo(juror.getLastName());

        verify(jurorAuditChangeService, times(1))
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, never()).recordPersonalDetailsHistory(anyString(),
            any(Juror.class), anyString(), anyString());
    }

    @Test
    void testFixErrorInJurorNameJurorPoolNotFound() {
        String bureauOwner = "400";
        String username = "BUREAU_USER";
        String jurorNumber = "111111111";

        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, username);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, bureauOwner);
        jurorPool.getPool().setPoolNumber("123456789");
        JurorNameDetailsDto dto = new JurorNameDetailsDto();
        BeanUtils.copyProperties(jurorPool, dto);

        doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorRecordService.fixErrorInJurorName(payload, jurorNumber, dto));

        verify(jurorPoolRepository, never()).save(any());

        verify(jurorAuditChangeService, never())
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, never())
            .recordPersonalDetailsHistory(anyString(), any(Juror.class), anyString(),
                anyString());
    }

    @Test
    void testFixErrorInJurorNameCourtUserBureauOwned() {
        String bureauOwner = "400";
        String courtOwner = "415";
        String username = "BUREAU_USER";
        String jurorNumber = "111111111";

        final BureauJwtPayload payload = TestUtils.createJwt(courtOwner, username);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, bureauOwner);
        jurorPool.getPool().setPoolNumber("123456789");
        JurorNameDetailsDto dto = new JurorNameDetailsDto();
        BeanUtils.copyProperties(jurorPool, dto);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorRecordService.fixErrorInJurorName(payload, jurorNumber, dto));

        verify(jurorPoolRepository, never()).save(any());

        verify(jurorAuditChangeService, never())
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, never())
            .recordPersonalDetailsHistory(anyString(), any(Juror.class), anyString(),
                anyString());
    }

    @Test
    void testFixErrorInJurorNameBureauUserCourtOwned() {
        String bureauOwner = "400";
        String courtOwner = "415";
        String username = "BUREAU_USER";
        String jurorNumber = "111111111";

        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, username);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        jurorPool.getPool().setPoolNumber("123456789");
        JurorNameDetailsDto dto = new JurorNameDetailsDto();
        BeanUtils.copyProperties(jurorPool, dto);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorRecordService.fixErrorInJurorName(payload, jurorNumber, dto));

        verify(jurorPoolRepository, never()).save(any());

        verify(jurorAuditChangeService, never())
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, never())
            .recordPersonalDetailsHistory(anyString(), any(Juror.class), anyString(),
                anyString());
    }

    @Test
    void testProcessPendingNameChangeApproved() {
        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);

        String courtOwner = "415";
        String username = "COURT_USER";
        String jurorNumber = "111111111";

        String pendingTitle = "Mr";
        String pendingFirstName = "First";
        final String pendingLastName = "Last";

        final BureauJwtPayload payload = TestUtils.createJwt(courtOwner, username);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        jurorPool.getPool().setPoolNumber("123456789");

        Juror juror = jurorPool.getJuror();
        juror.setPendingTitle(pendingTitle);
        juror.setPendingFirstName(pendingFirstName);
        juror.setPendingLastName(pendingLastName);

        String changeOfNameCode = "CN";
        String notes = "Marriage certificate and passport";

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.APPROVE, notes);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        doReturn(null).when(jurorPoolRepository)
            .save(any());
        doReturn(null).when(jurorPoolRepository)
            .saveAndFlush(any());
        doNothing().when(jurorAuditChangeService).recordContactLog(jurorPool.getJuror(), username,
            changeOfNameCode, notes);
        doNothing().when(jurorAuditChangeService).recordApprovalHistoryEvent(jurorNumber,
            dto.getDecision(), username, jurorPool.getPoolNumber());
        doReturn(initChangedPropertyMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        doNothing().when(jurorAuditChangeService).recordPersonalDetailsHistory(anyString(),
            any(Juror.class), anyString(), anyString());

        jurorRecordService.processPendingNameChange(payload, jurorNumber, dto);

        verify(jurorRepository, times(1))
            .saveAndFlush(jurorArgumentCaptor.capture());
        Juror capturedJuror = jurorArgumentCaptor.getValue();
        assertThat(capturedJuror.getTitle()).isEqualTo(pendingTitle);
        assertThat(capturedJuror.getFirstName()).isEqualTo(pendingFirstName);
        assertThat(capturedJuror.getLastName()).isEqualTo(pendingLastName);

        assertThat(capturedJuror.getPendingTitle()).isNull();
        assertThat(capturedJuror.getPendingFirstName()).isNull();
        assertThat(capturedJuror.getPendingLastName()).isNull();

        verify(jurorAuditChangeService, times(1))
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, times(3))
            .recordPersonalDetailsHistory(anyString(), any(Juror.class), anyString(),
                anyString());
        verify(jurorAuditChangeService, times(1))
            .recordContactLog(jurorPool.getJuror(), username, changeOfNameCode,
                "Approved the juror's name change. " + notes);
        verify(jurorAuditChangeService, times(1))
            .recordApprovalHistoryEvent(jurorNumber, dto.getDecision(), username,
                jurorPool.getPoolNumber());
    }

    @Test
    void testProcessPendingNameChangeRejected() {
        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);

        String courtOwner = "415";
        String username = "COURT_USER";
        String jurorNumber = "111111111";

        String pendingTitle = "Mr";
        String pendingFirstName = "First";
        final String pendingLastName = "Last";

        final BureauJwtPayload payload = TestUtils.createJwt(courtOwner, username);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        jurorPool.getPool().setPoolNumber("123456789");

        Juror juror = jurorPool.getJuror();
        juror.setPendingTitle(pendingTitle);
        juror.setPendingFirstName(pendingFirstName);
        juror.setPendingLastName(pendingLastName);

        String changeOfNameCode = "CN";
        String notes = "Their name has not been legally changed";

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, notes);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        doReturn(null).when(jurorPoolRepository)
            .save(any());
        doReturn(null).when(jurorPoolRepository)
            .saveAndFlush(any());
        doNothing().when(jurorAuditChangeService).recordContactLog(jurorPool.getJuror(), username,
            changeOfNameCode, notes);
        doNothing().when(jurorAuditChangeService).recordApprovalHistoryEvent(jurorPool.getJurorNumber(),
            dto.getDecision(), username, jurorPool.getPoolNumber());
        doReturn(initChangedPropertyMap(Boolean.TRUE)).when(jurorAuditChangeService)
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        doNothing().when(jurorAuditChangeService).recordPersonalDetailsHistory(anyString(),
            any(Juror.class), anyString(), anyString());

        jurorRecordService.processPendingNameChange(payload, jurorNumber, dto);

        verify(jurorPoolRepository, never()).save(any());

        verify(jurorRepository, times(1))
            .saveAndFlush(jurorArgumentCaptor.capture());
        Juror capturedJuror = jurorArgumentCaptor.getValue();
        assertThat(capturedJuror.getPendingTitle()).isNull();
        assertThat(capturedJuror.getPendingFirstName()).isNull();
        assertThat(capturedJuror.getPendingLastName()).isNull();

        verify(jurorAuditChangeService, never())
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, never())
            .recordPersonalDetailsHistory(anyString(), any(Juror.class),
                anyString(), anyString());
        verify(jurorAuditChangeService, times(1))
            .recordContactLog(jurorPool.getJuror(), username, changeOfNameCode,
                "Rejected the juror's name change. " + notes);
        verify(jurorAuditChangeService, times(1))
            .recordApprovalHistoryEvent(jurorNumber, dto.getDecision(), username,
                jurorPool.getPoolNumber());
    }

    @Test
    void testProcessPendingNameChangeInvalidPermission() {
        String courtOwner = "415";
        String username = "COURT_USER";
        String jurorNumber = "111111111";

        BureauJwtPayload payload = TestUtils.createJwt("416", username);


        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        String notes = "Their name has not been legally changed";

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, notes);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorRecordService.processPendingNameChange(payload, jurorNumber, dto));

        verify(jurorPoolRepository, never()).save(any());
        verify(jurorPoolRepository, never()).saveAndFlush(any());

        verify(jurorAuditChangeService, never())
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, never())
            .recordPersonalDetailsHistory(anyString(), any(Juror.class),
                anyString(), anyString());
        verify(jurorAuditChangeService, never())
            .recordContactLog(any(Juror.class), anyString(),
                anyString(), anyString());
        verify(jurorAuditChangeService, never())
            .recordApprovalHistoryEvent(anyString(), any(ApprovalDecision.class),
                anyString(), anyString());
    }

    @Test
    void testProcessPendingNameChangeNoJurorRecord() {
        String courtOwner = "415";
        String username = "COURT_USER";
        String jurorNumber = "111111111";

        BureauJwtPayload payload = TestUtils.createJwt(courtOwner, username);

        String notes = "Their name has not been legally changed";

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, notes);

        doReturn(new ArrayList<>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorRecordService.processPendingNameChange(payload, jurorNumber, dto));

        verify(jurorPoolRepository, never()).save(any());
        verify(jurorPoolRepository, never()).saveAndFlush(any());

        verify(jurorAuditChangeService, never())
            .initChangedPropertyMap(any(Juror.class), any(JurorNameDetailsDto.class));
        verify(jurorAuditChangeService, never())
            .recordPersonalDetailsHistory(anyString(), any(Juror.class),
                anyString(), anyString());
        verify(jurorAuditChangeService, never())
            .recordContactLog(any(Juror.class), anyString(),
                anyString(), anyString());
        verify(jurorAuditChangeService, never())
            .recordApprovalHistoryEvent(anyString(), any(ApprovalDecision.class),
                anyString(), anyString());
    }

    @Test
    void updateAttendanceHappyPathOnCall() {
        String courtOwner = "415";
        String jurorNumber = "111111111";

        UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
        dto.setOnCall(true);
        dto.setJurorNumber(jurorNumber);
        dto.setNextDate(null);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        jurorPool.setOnCall(false);
        jurorPool.setNextDate(LocalDate.now().plusWeeks(1));

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        jurorRecordService.updateAttendance(dto);

        verify(jurorPoolRepository, times(1)).save(jurorPool);

        assertThat(jurorPool.getOnCall()).isTrue();
        assertThat(jurorPool.getNextDate()).isNull();

    }

    @Test
    void updateAttendanceHappyPathChangeNextDate() {
        String courtOwner = "415";
        String jurorNumber = "111111111";

        UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
        dto.setOnCall(false);
        dto.setJurorNumber(jurorNumber);
        dto.setNextDate(LocalDate.now().plusWeeks(3));

        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        jurorPool.setOnCall(false);
        jurorPool.setNextDate(LocalDate.now().plusWeeks(1));

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        jurorRecordService.updateAttendance(dto);

        verify(jurorPoolRepository, times(1)).save(jurorPool);

        assertThat(jurorPool.getNextDate()).isEqualTo(dto.getNextDate());
        assertThat(jurorPool.getOnCall()).isFalse();

    }

    @Test
    void updateAttendanceOnCallAndNextDateBadRequest() {
        String courtOwner = "415";
        String jurorNumber = "111111111";

        UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
        dto.setJurorNumber(jurorNumber);
        dto.setOnCall(true);
        dto.setNextDate(LocalDate.now().plusWeeks(2));

        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        jurorPool.setOnCall(false);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            jurorRecordService.updateAttendance(dto));

        verify(jurorPoolRepository, never()).save(any());
    }

    @Test
    void updateAttendanceNoJurorPoolFound() {
        String courtOwner = "415";
        String jurorNumber = "111111111";
        String jurorNumberNotExist = "000111222";

        UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
        dto.setJurorNumber(jurorNumberNotExist);
        dto.setOnCall(true);
        dto.setNextDate(null);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        jurorPool.setOnCall(false);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorRecordService.updateAttendance(dto));

        verify(jurorPoolRepository, never()).save(any());
    }

    @Test
    void updateAttendanceJurorAlreadyOnCall() {
        String courtOwner = "415";
        String jurorNumber = "111111111";

        UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
        dto.setJurorNumber(jurorNumber);
        dto.setOnCall(true);
        dto.setNextDate(null);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, courtOwner);
        jurorPool.setOnCall(true);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            jurorRecordService.updateAttendance(dto));

        verify(jurorPoolRepository, never()).save(any());

    }

    private Map<String, Boolean> initChangedPropertyMap(Boolean defaultValue) {
        Map<String, Boolean> changedProperties = new ConcurrentHashMap<>();
        changedProperties.put("title", defaultValue);
        changedProperties.put("first Name", defaultValue);
        changedProperties.put("last Name", defaultValue);
        return changedProperties;
    }

    private EditJurorRecordRequestDto createEditJurorRecordRequestDto() {
        EditJurorRecordRequestDto editJurorRecordRequestDto = new EditJurorRecordRequestDto();

        editJurorRecordRequestDto.setTitle("Mr");
        editJurorRecordRequestDto.setFirstName("NewFirstName");
        editJurorRecordRequestDto.setLastName("NewLastName");
        editJurorRecordRequestDto.setAddressLineOne("addressLineOne");
        editJurorRecordRequestDto.setAddressLineTwo("addressLineTwo");
        editJurorRecordRequestDto.setAddressLineThree("addressLineThree");
        editJurorRecordRequestDto.setAddressTown("addressTown");
        editJurorRecordRequestDto.setAddressCounty("addressCounty");
        editJurorRecordRequestDto.setAddressPostcode("M24 4BP");
        editJurorRecordRequestDto.setDateOfBirth(LocalDate.parse("2022-02-01"));
        editJurorRecordRequestDto.setPrimaryPhone("071234566790");
        editJurorRecordRequestDto.setSecondaryPhone(null);
        editJurorRecordRequestDto.setEmailAddress("someEmail@exampleEmail.co.uk");
        editJurorRecordRequestDto.setSpecialNeed("V");
        editJurorRecordRequestDto.setSpecialNeedMessage("Vision impairment");
        editJurorRecordRequestDto.setOpticReference("22222222");
        editJurorRecordRequestDto.setPendingTitle("Mx");
        editJurorRecordRequestDto.setPendingFirstName("Pending First Name");
        editJurorRecordRequestDto.setPendingLastName("Pending Last Name");

        return editJurorRecordRequestDto;
    }

    private JurorStatus createJurorStatus(int statusCode) {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(statusCode);
        return jurorStatus;
    }

    @Nested
    @DisplayName("public void updatePncStatus(final String jurorNumber, final PoliceCheck policeCheck)")
    class UpdatePncCheckStatus {
        @BeforeEach
        void beforeEach() {
            when(clock.instant())
                .thenReturn(Instant.now());

            when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());
        }

        private JurorPool setupJurorPool(PoliceCheck policeCheck) {
            JurorPool jurorPool = createValidJurorPool(TestConstants.VALID_JUROR_NUMBER, "400");
            jurorPool.getJuror().setPoliceCheck(policeCheck);
            doReturn(List.of(jurorPool)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            return jurorPool;
        }

        @ParameterizedTest(name = "Straight forward only update police check field")
        @EnumSource(value = PoliceCheck.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"ELIGIBLE", "INELIGIBLE", "UNCHECKED_MAX_RETRIES_EXCEEDED",
                "IN_PROGRESS", "INSUFFICIENT_INFORMATION"})
        void positiveStraightForwardUpdateOnly(PoliceCheck policeCheck) {
            JurorPool jurorPool = setupJurorPool(null);
            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, policeCheck);

            assertEquals(policeCheck, jurorPool.getJuror().getPoliceCheck(),
                "Police status must be " + policeCheck);
            verifyNoInteractions(
                jurorHistoryService,
                printDataService
            );
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorRepository, times(1)).save(jurorPool.getJuror());
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
        }

        @ParameterizedTest(name = "None-Straight forward max retries exceeded")
        @EnumSource(value = PoliceCheck.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"NOT_CHECKED", "IN_PROGRESS", "ELIGIBLE", "INELIGIBLE", "INSUFFICIENT_INFORMATION"})
        void positiveNonMaxRetiesExceeded(PoliceCheck policeCheck) {
            JurorPool jurorPool = setupJurorPool(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR);
            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, policeCheck);
            assertEquals(PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED, jurorPool.getJuror().getPoliceCheck(),
                "Police status be UNCHECKED_MAX_RETRIES_EXCEEDED. If old status was error and new status is : "
                    + policeCheck);

            verify(jurorHistoryService, times(1))
                .createPoliceCheckQualifyHistory(jurorPool, false);

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorRepository, times(1)).save(jurorPool.getJuror());
            verify(printDataService, times(1)).printConfirmationLetter(jurorPool);
            verify(jurorHistoryService, times(1)).createConfirmationLetterHistory(jurorPool,
                "Confirmation Letter Auto");
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
        }

        @Test
        @DisplayName("ELIGIBLE")
        void positiveEligible() {
            JurorPool jurorPool = setupJurorPool(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR);
            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, PoliceCheck.ELIGIBLE);
            assertEquals(PoliceCheck.ELIGIBLE, jurorPool.getJuror().getPoliceCheck(),
                "Police status be ELIGIBLE.");

            verify(jurorHistoryService, times(1))
                .createPoliceCheckQualifyHistory(jurorPool, true);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorRepository, times(1)).save(jurorPool.getJuror());
            verify(printDataService, times(1)).printConfirmationLetter(jurorPool);
            verify(jurorHistoryService, times(1)).createConfirmationLetterHistory(jurorPool,
                "Confirmation Letter Auto");
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
        }

        @Test
        @DisplayName("ELIGIBLE - Court")
        void positiveEligibleCourt() {
            JurorPool jurorPool = setupJurorPool(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR);
            jurorPool.setOwner(TestConstants.VALID_COURT_LOCATION);
            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, PoliceCheck.ELIGIBLE);
            assertEquals(PoliceCheck.ELIGIBLE, jurorPool.getJuror().getPoliceCheck(),
                "Police status be ELIGIBLE.");

            verify(jurorHistoryService, times(1))
                .createPoliceCheckQualifyHistory(jurorPool, true);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorRepository, times(1)).save(jurorPool.getJuror());
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
        }

        @Test
        @DisplayName("INELIGIBLE")
        void positiveInEligible() {
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(6);
            when(jurorStatusRepository.findById(6)).thenReturn(Optional.of(jurorStatus));
            JurorPool jurorPool = setupJurorPool(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR);
            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, PoliceCheck.INELIGIBLE);
            assertEquals(PoliceCheck.INELIGIBLE, jurorPool.getJuror().getPoliceCheck(),
                "Police status be INELIGIBLE.");

            assertEquals(6, jurorPool.getStatus().getStatus(),
                "Juror pool status must be '6'");
            assertEquals("E", jurorPool.getJuror().getDisqualifyCode(),
                "Juror disqualify code must be 'E'");
            assertEquals(LocalDate.now(clock), jurorPool.getJuror().getDisqualifyDate(),
                "Just disqualify date must be today");

            verify(jurorHistoryService, times(1))
                .createPoliceCheckDisqualifyHistory(jurorPool);
            verify(printDataService, times(1)).printWithdrawalLetter(jurorPool);
            verify(jurorHistoryService, times(1)).createWithdrawHistory(jurorPool, "Withdrawal Letter Auto");

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorRepository, times(1)).save(jurorPool.getJuror());
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
        }

        @Test
        @DisplayName("INELIGIBLE - Court")
        void positiveInEligibleCourt() {
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(6);
            when(jurorStatusRepository.findById(6)).thenReturn(Optional.of(jurorStatus));
            JurorPool jurorPool = setupJurorPool(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR);
            jurorPool.setOwner(TestConstants.VALID_COURT_LOCATION);

            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, PoliceCheck.INELIGIBLE);
            assertEquals(PoliceCheck.INELIGIBLE, jurorPool.getJuror().getPoliceCheck(),
                "Police status be INELIGIBLE.");

            assertEquals(6, jurorPool.getStatus().getStatus(),
                "Juror pool status must be '6'");
            assertEquals("E", jurorPool.getJuror().getDisqualifyCode(),
                "Juror disqualify code must be 'E'");
            assertEquals(LocalDate.now(clock), jurorPool.getJuror().getDisqualifyDate(),
                "Just disqualify date must be today");

            verify(jurorHistoryService, times(1))
                .createPoliceCheckDisqualifyHistory(jurorPool);
            verifyNoMoreInteractions(printDataService);

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorRepository, times(1)).save(jurorPool.getJuror());
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
        }

        @Test
        @DisplayName("Old value and new value are same no update")
        void negativeNoUpdateOldAndSameMatch() {
            JurorPool jurorPool = setupJurorPool(PoliceCheck.ELIGIBLE);
            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, PoliceCheck.ELIGIBLE);
            assertEquals(PoliceCheck.ELIGIBLE, jurorPool.getJuror().getPoliceCheck(),
                "Police status be ELIGIBLE.");
            verifyNoInteractions(jurorHistoryService);
            verifyNoInteractions(printDataService);

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
            verifyNoInteractions(jurorRepository);
        }

        @Test
        @DisplayName("Police Check IN_PROGRESS: update police check field and trigger part history")
        void positivePoliceCheckUpdateInProgress() {
            JurorPool jurorPool = setupJurorPool(null);
            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, PoliceCheck.IN_PROGRESS);

            assertEquals(PoliceCheck.IN_PROGRESS, jurorPool.getJuror().getPoliceCheck(),
                "Police status must be IN_PROGRESS");
            verifyNoInteractions(
                printDataService
            );
            verify(jurorHistoryService, times(1))
                .createPoliceCheckInProgressHistory(jurorPool);
            verifyNoMoreInteractions(jurorHistoryService);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorRepository, times(1)).save(jurorPool.getJuror());
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
        }

        @Test
        @DisplayName("Police Check INSUFFICIENT_INFORMATION : update police check field and trigger part history")
        void positivePoliceCheckUpdateInsufficientInformation() {
            JurorPool jurorPool = setupJurorPool(null);
            jurorRecordService.updatePncStatus(TestConstants.VALID_JUROR_NUMBER, PoliceCheck.INSUFFICIENT_INFORMATION);

            assertEquals(PoliceCheck.INSUFFICIENT_INFORMATION, jurorPool.getJuror().getPoliceCheck(),
                "Police status must be INSUFFICIENT_INFORMATION");
            verifyNoInteractions(
                printDataService
            );
            verify(jurorHistoryService, times(1))
                .createPoliceCheckInsufficientInformationHistory(jurorPool);
            verifyNoMoreInteractions(jurorHistoryService);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(TestConstants.VALID_JUROR_NUMBER, true);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorRepository, times(1)).save(jurorPool.getJuror());
            verifyNoMoreInteractions(jurorPoolRepository, jurorRepository, jurorHistoryService, printDataService);
        }
    }

    @Nested
    @DisplayName("public void updateJurorToFailedToAttend(final String jurorNumber, final String poolNumber)")
    class UpdateJurorToFailedToAttend {

        @Test
        void typical() {
            JurorStatus failedToAttendStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.FAILED_TO_ATTEND))
                .thenReturn(Optional.of(failedToAttendStatus));


            JurorStatus status = mock(JurorStatus.class);
            when(status.getStatus()).thenReturn(IJurorStatus.RESPONDED);
            JurorPool jurorPool = mock(JurorPool.class);
            when(jurorPool.getStatus()).thenReturn(status);

            Juror juror = mock(Juror.class);
            when(jurorPool.getJuror()).thenReturn(juror);
            when(juror.getCompletionDate()).thenReturn(null);

            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(jurorPool);


            jurorRecordService.updateJurorToFailedToAttend(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verify(jurorPool, times(1)).setStatus(failedToAttendStatus);
            verify(jurorPool, times(1)).getStatus();
            verify(jurorPool, times(1)).getJuror();
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumber(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);
            verify(jurorHistoryService, times(1)).createFailedToAttendHistory(jurorPool);
            verifyNoMoreInteractions(jurorPoolRepository, jurorHistoryService, jurorPool);
        }

        @Test
        void notFound() {
            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(null);

            MojException.NotFound exception
                = assertThrows(MojException.NotFound.class, () -> jurorRecordService.updateJurorToFailedToAttend(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER), "Not found");
            assertEquals("Juror number " + TestConstants.VALID_JUROR_NUMBER
                    + " not found in pool " + TestConstants.VALID_POOL_NUMBER,
                exception.getMessage(), "Exception message should match");
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumber(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
        }

        @Test
        void wrongStatus() {
            JurorStatus failedToAttendStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.FAILED_TO_ATTEND))
                .thenReturn(Optional.of(failedToAttendStatus));

            JurorStatus status = mock(JurorStatus.class);
            when(status.getStatus()).thenReturn(IJurorStatus.JUROR);
            JurorPool jurorPool = mock(JurorPool.class);
            when(jurorPool.getStatus()).thenReturn(status);

            Juror juror = mock(Juror.class);
            when(jurorPool.getJuror()).thenReturn(juror);
            when(juror.getCompletionDate()).thenReturn(null);

            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(jurorPool);

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> jurorRecordService.updateJurorToFailedToAttend(
                    TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER),
                "Juror status must be responded in order to undo the failed to attend status.");

            assertEquals("Juror status must be responded in order to undo the failed to attend status.",
                exception.getMessage(), "Exception message should match");
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumber(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
        }

        @Test
        void hasCompletionDate() {
            JurorStatus failedToAttendStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.FAILED_TO_ATTEND))
                .thenReturn(Optional.of(failedToAttendStatus));

            JurorStatus status = mock(JurorStatus.class);
            when(status.getStatus()).thenReturn(IJurorStatus.RESPONDED);
            JurorPool jurorPool = mock(JurorPool.class);
            when(jurorPool.getStatus()).thenReturn(status);

            Juror juror = mock(Juror.class);
            when(jurorPool.getJuror()).thenReturn(juror);
            when(juror.getCompletionDate()).thenReturn(LocalDate.now());

            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(jurorPool);

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> jurorRecordService.updateJurorToFailedToAttend(
                    TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER),
                "Juror must not have a completion_date in order to undo the failed to attend status.");

            assertEquals(
                "This juror cannot be given a Failed To Attend status because they have been given a completion date. "
                    + "Only a Senior Jury Officer can be remove the completion date",

                exception.getMessage(), "Exception message should match");
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumber(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
        }

        @Test
        void hasApperances() {
            JurorStatus failedToAttendStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.FAILED_TO_ATTEND))
                .thenReturn(Optional.of(failedToAttendStatus));

            when(jurorAppearanceService.hasAttendances(TestConstants.VALID_JUROR_NUMBER))
                .thenReturn(true);

            JurorStatus status = mock(JurorStatus.class);
            when(status.getStatus()).thenReturn(IJurorStatus.RESPONDED);
            JurorPool jurorPool = mock(JurorPool.class);
            when(jurorPool.getStatus()).thenReturn(status);

            Juror juror = mock(Juror.class);
            when(jurorPool.getJuror()).thenReturn(juror);
            when(juror.getCompletionDate()).thenReturn(null);

            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(jurorPool);

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> jurorRecordService.updateJurorToFailedToAttend(
                    TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER),
                "Juror must not have any appearances in order to undo the failed to attend status.");

            assertEquals(
                "This juror cannot be given a Failed To Attend status because they have had attendances recorded."
                    + " The Failed To Attend status is only for jurors who have not attended at all",
                exception.getMessage(), "Exception message should match");
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumber(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
        }
    }

    @Nested
    @DisplayName("public void undoUpdateJurorToFailedToAttend(String jurorNumber, String poolNumber)")
    class UndoUpdateJurorToFailedToAttend {

        @Test
        void typical() {
            JurorStatus status = mock(JurorStatus.class);
            when(status.getStatus()).thenReturn(IJurorStatus.FAILED_TO_ATTEND);
            JurorPool jurorPool = mock(JurorPool.class);
            when(jurorPool.getStatus()).thenReturn(status);

            Juror juror = mock(Juror.class);
            when(jurorPool.getJuror()).thenReturn(juror);
            when(juror.getCompletionDate()).thenReturn(null);

            JurorStatus respondedStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED))
                .thenReturn(Optional.of(respondedStatus));

            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(jurorPool);


            jurorRecordService.undoUpdateJurorToFailedToAttend(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verify(jurorPool, times(1)).setStatus(respondedStatus);
            verify(jurorPool, times(1)).getStatus();
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumber(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);


            verify(jurorHistoryService, times(1)).createUndoFailedToAttendHistory(jurorPool);
            verifyNoMoreInteractions(jurorPoolRepository, jurorHistoryService, jurorPool);
        }

        @Test
        void notFound() {
            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(null);

            MojException.NotFound exception
                = assertThrows(MojException.NotFound.class, () -> jurorRecordService.undoUpdateJurorToFailedToAttend(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER), "Not found");

            assertEquals("Juror number " + TestConstants.VALID_JUROR_NUMBER
                    + " not found in pool " + TestConstants.VALID_POOL_NUMBER,
                exception.getMessage(), "Exception message should match");
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumber(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
        }

        @Test
        void wrongStatus() {
            JurorStatus status = mock(JurorStatus.class);
            when(status.getStatus()).thenReturn(IJurorStatus.RESPONDED);
            JurorPool jurorPool = mock(JurorPool.class);
            when(jurorPool.getStatus()).thenReturn(status);


            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(jurorPool);

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> jurorRecordService.undoUpdateJurorToFailedToAttend(
                    TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER),
                "Expect an exception when the pool status is not failed to respond");

            assertEquals("Juror status must be failed to attend in order to undo the failed to attend status.",
                exception.getMessage(), "Exception message should match");
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumber(
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository);
            verifyNoInteractions(jurorHistoryService);
        }
    }

    @Nested
    @DisplayName("public void createJurorRecord(BureauJWTPayload payload, JurorCreateRequestDto jurorCreateRequestDto)")
    class CreateJurorRecord {
        private PendingJurorStatus pendingJurorStatus;

        @BeforeEach
        void beforeEach() {
            pendingJurorStatus = mock(PendingJurorStatus.class);
            when(pendingJurorStatusRepository.findById(PendingJurorStatusEnum.QUEUED.getCode()))
                .thenReturn(Optional.ofNullable(pendingJurorStatus));
        }

        private BureauJwtPayload createBureauJwtPayload(String owner, String login) {
            BureauJwtPayload payload = new BureauJwtPayload();
            payload.setOwner(owner);
            payload.setLogin(login);
            return payload;
        }

        @Test
        void positiveTypicalExistingPool() {
            final BureauJwtPayload payload = createBureauJwtPayload("415", "COURT_USER");
            JurorCreateRequestDto dto = JurorCreateRequestDtoTest.createValidJurorCreateRequestExistingPoolDto();
            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequest.getOwner()).thenReturn("415");
            when(poolRequest.getPoolNumber()).thenReturn(dto.getPoolNumber());

            when(pendingJurorRepository.generatePendingJurorNumber(dto.getLocationCode()))
                .thenReturn("012345678");

            when(poolRequestRepository.findById(dto.getPoolNumber()))
                .thenReturn(Optional.ofNullable(poolRequest));

            jurorRecordService.createJurorRecord(payload, dto);

            ArgumentCaptor<PendingJuror> pendingJurorArgumentCaptor = ArgumentCaptor.forClass(PendingJuror.class);

            verify(pendingJurorRepository, times(1))
                .save(pendingJurorArgumentCaptor.capture());

            validateMatch(dto, pendingJurorArgumentCaptor.getValue(), "012345678", dto.getPoolNumber());

            verify(poolRequestRepository, times(1)).findById(dto.getPoolNumber());
            verify(pendingJurorRepository, times(1)).generatePendingJurorNumber(dto.getLocationCode());
            verify(pendingJurorStatusRepository, times(1)).findById(PendingJurorStatusEnum.QUEUED.getCode());

            verifyNoMoreInteractions(
                poolRequestRepository,
                pendingJurorStatusRepository,
                pendingJurorRepository);
            verifyNoInteractions(poolHistoryRepository);
        }

        @Test
        void positiveTypicalNewPool() {
            final BureauJwtPayload payload = createBureauJwtPayload("415", "COURT_USER");

            JurorCreateRequestDto dto = JurorCreateRequestDtoTest.createValidJurorCreateRequestNewPoolDto();

            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequest.getOwner()).thenReturn("415");
            when(poolRequest.getPoolNumber()).thenReturn(dto.getPoolNumber());

            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocationRepository.findById(dto.getLocationCode()))
                .thenReturn(Optional.ofNullable(courtLocation));

            when(courtLocation.getCourtAttendTime()).thenReturn(LocalTime.of(9, 15));

            PoolType poolType = mock(PoolType.class);
            when(poolTypeRepository.findById(dto.getPoolType()))
                .thenReturn(Optional.ofNullable(poolType));

            when(pendingJurorRepository.generatePendingJurorNumber(dto.getLocationCode()))
                .thenReturn("012345678");

            when(generatePoolNumberService.generatePoolNumber(dto.getLocationCode(), dto.getStartDate()))
                .thenReturn("87654321");

            when(poolRequestRepository.findById(dto.getPoolNumber()))
                .thenReturn(Optional.ofNullable(poolRequest));

            jurorRecordService.createJurorRecord(payload, dto);

            ArgumentCaptor<PoolRequest> poolRequestArgumentCaptor = ArgumentCaptor.forClass(PoolRequest.class);
            verify(poolRequestRepository, times(1))
                .saveAndFlush(poolRequestArgumentCaptor.capture());

            validateMatch(dto, poolRequestArgumentCaptor.getValue(), "87654321",
                "415", courtLocation, poolType);

            ArgumentCaptor<PendingJuror> pendingJurorArgumentCaptor = ArgumentCaptor.forClass(PendingJuror.class);
            verify(pendingJurorRepository, times(1))
                .save(pendingJurorArgumentCaptor.capture());

            validateMatch(dto, pendingJurorArgumentCaptor.getValue(), "012345678", "87654321");

            verify(pendingJurorRepository, times(1))
                .generatePendingJurorNumber(dto.getLocationCode());
            verify(pendingJurorStatusRepository, times(1))
                .findById(PendingJurorStatusEnum.QUEUED.getCode());

            ArgumentCaptor<PoolHistory> poolHistoryArgumentCaptor = ArgumentCaptor.forClass(PoolHistory.class);
            verify(poolHistoryRepository, times(1))
                .save(poolHistoryArgumentCaptor.capture());

            PoolHistory poolHistory = poolHistoryArgumentCaptor.getValue();
            assertEquals("87654321", poolHistory.getPoolNumber(), "Pool number must match");
            assertNotNull(poolHistory.getHistoryDate(), "History date must not be null");
            assertEquals(HistoryCode.PREQ, poolHistory.getHistoryCode(), "History code must match");
            assertEquals("COURT_USER", poolHistory.getUserId(), "UserId must match");
            assertEquals("Pool Request 87654321 created for pending Juror", poolHistory.getOtherInformation(),
                "Other info must match");


            verifyNoMoreInteractions(
                poolHistoryRepository,
                poolRequestRepository,
                pendingJurorStatusRepository,
                pendingJurorRepository);
        }

        @Test
        void negativeOwnersDoNotMatch() {
            JurorCreateRequestDto dto = JurorCreateRequestDtoTest.createValidJurorCreateRequestExistingPoolDto();
            BureauJwtPayload payload = createBureauJwtPayload("415", "COURT_USER");
            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequest.getOwner()).thenReturn("406");
            when(poolRequest.getPoolNumber()).thenReturn(dto.getPoolNumber());

            when(poolRequestRepository.findById(dto.getPoolNumber()))
                .thenReturn(Optional.ofNullable(poolRequest));

            MojException.Forbidden exception =
                assertThrows(MojException.Forbidden.class, () -> jurorRecordService.createJurorRecord(payload, dto),
                    "Pool not owned by same owner as pool");
            assertEquals("Court user cannot create a juror record in pool " + dto.getPoolNumber(),
                exception.getMessage(),
                "Exception message must match");

            verify(poolRequestRepository, times(1)).findById(dto.getPoolNumber());

            verifyNoMoreInteractions(poolRequestRepository);
            verifyNoInteractions(poolHistoryRepository,
                pendingJurorStatusRepository,
                pendingJurorRepository);
        }

        @Test
        void negativePendingJurorStatusNotFound() {
            final BureauJwtPayload payload = createBureauJwtPayload("415", "COURT_USER");
            JurorCreateRequestDto dto = JurorCreateRequestDtoTest.createValidJurorCreateRequestExistingPoolDto();
            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequest.getOwner()).thenReturn("415");
            when(poolRequest.getPoolNumber()).thenReturn(dto.getPoolNumber());

            reset(pendingJurorStatusRepository);
            when(pendingJurorStatusRepository.findById(PendingJurorStatusEnum.QUEUED.getCode()))
                .thenReturn(Optional.empty());

            when(poolRequestRepository.findById(dto.getPoolNumber()))
                .thenReturn(Optional.ofNullable(poolRequest));

            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class, () -> jurorRecordService.createJurorRecord(payload, dto),
                    "Pending Juror Status not found");
            assertEquals("Pending Juror Status not found",
                exception.getMessage(),
                "Exception message must match");

            verify(poolRequestRepository, times(1)).findById(dto.getPoolNumber());
            verify(pendingJurorStatusRepository, times(1)).findById(PendingJurorStatusEnum.QUEUED.getCode());

            verifyNoMoreInteractions(poolRequestRepository,
                pendingJurorStatusRepository);
            verifyNoInteractions(poolHistoryRepository,
                pendingJurorRepository);
        }

        private void validateMatch(JurorCreateRequestDto dto, PendingJuror pendingJuror, String jurorNumber,
                                   String poolNumber) {

            assertEquals(jurorNumber, pendingJuror.getJurorNumber(), "Juror number must match");
            assertEquals(poolNumber, pendingJuror.getPoolNumber(), "Pool number must match");
            assertEquals(dto.getTitle(), pendingJuror.getTitle(), "Title must match");
            assertEquals(dto.getFirstName(), pendingJuror.getFirstName(), "First name must match");
            assertEquals(dto.getLastName(), pendingJuror.getLastName(), "Last name must match");
            assertEquals(dto.getDateOfBirth(), pendingJuror.getDateOfBirth(), "Date of birth must match");

            //Validate address
            JurorAddressDto expected = dto.getAddress();
            assertEquals(expected.getLineOne(), pendingJuror.getAddressLine1(), "Address line one must match");
            assertEquals(expected.getLineTwo(), pendingJuror.getAddressLine2(), "Address line two must match");
            assertEquals(expected.getLineThree(), pendingJuror.getAddressLine3(), "Address line three must match");
            assertEquals(expected.getTown(), pendingJuror.getAddressLine4(), "Address town must match");
            assertEquals(expected.getCounty(), pendingJuror.getAddressLine5(), "Address county must match");
            assertEquals(expected.getPostcode(), pendingJuror.getPostcode(), "Address postcode must match");
            assertEquals(dto.getEmailAddress(), pendingJuror.getEmail(), "Email address must match");
            assertEquals(dto.getNotes(), pendingJuror.getNotes(), "Notes must match");
            assertEquals(pendingJurorStatus, pendingJuror.getStatus(), "Status must match");
            assertEquals(dto.getStartDate(), pendingJuror.getNextDate(), "Next day must match");
            assertTrue(pendingJuror.isResponded(), "Responded must be true");
        }

        private void validateMatch(JurorCreateRequestDto dto, PoolRequest poolRequest, String poolNumber,
                                   String owner, CourtLocation courtLocation, PoolType poolType) {

            assertEquals(poolNumber, poolRequest.getPoolNumber(), "Pool number must match");
            assertEquals(owner, poolRequest.getOwner(), "Owner must match");
            assertEquals(courtLocation, poolRequest.getCourtLocation(), "CourtLocation must match");
            assertEquals('N', poolRequest.getNewRequest(), "new Request must match");
            assertEquals(dto.getStartDate(), poolRequest.getReturnDate(), "Return date must match");
            assertNull(poolRequest.getNumberRequested(), "Number requested must be null");

            assertEquals(LocalDateTime.of(dto.getStartDate(),
                    courtLocation.getCourtAttendTime()),
                poolRequest.getAttendTime(), "Attend Time must match");

            assertEquals(poolType, poolRequest.getPoolType(), "Pool type must match");

        }

    }

    @Nested
    @DisplayName("public JurorAttendanceDetailsResponseDto getJurorAttendanceDetails(String jurorNumber,"
        + " String poolNumber, BureauJWTPayload payload) ")
    class JurorRecordAttendanceTab {

        @ParameterizedTest
        @ValueSource(strings = {"416", "400"})
        void positiveJurorAttendanceTab(String owner) {

            //test scenario where juror has attended
            final JurorPool jurorPool = createValidJurorPool(TestConstants.VALID_POOL_NUMBER, owner);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(
                    TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            List<Appearance> appearances = new ArrayList<>();
            Appearance appearance =
                Appearance.builder()
                    .attendanceDate(LocalDate.now())
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .timeIn(LocalTime.of(9, 0))
                    .timeOut(LocalTime.of(17, 0))
                    .attendanceType(AttendanceType.FULL_DAY)
                    .travelTime(LocalTime.of(1, 30))
                    .appearanceStage(AppearanceStage.EXPENSE_ENTERED)
                    .build();
            appearances.add(appearance);

            doReturn(appearances).when(appearanceRepository).findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            JurorAttendanceDetailsResponseDto jurorAttendanceDetailsResponseDto =
                jurorRecordService.getJurorAttendanceDetails(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, buildPayload(owner));

            assertEquals(1, jurorAttendanceDetailsResponseDto.getData().size(),
                "One attendance record should be returned");

            verify(jurorPoolRepository, times(1)).findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

        }

        @ParameterizedTest
        @ValueSource(strings = {"416", "100"})
        void negativeWrongOwner(String userOwner) {

            final String owner = "415";
            final JurorPool jurorPool = createValidJurorPool(TestConstants.VALID_POOL_NUMBER, owner);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            MojException.Forbidden exception =
                assertThrows(MojException.Forbidden.class,
                    () -> jurorRecordService.getJurorAttendanceDetails(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER, buildPayload(userOwner)), // a different owner to expected
                    "Current user does not have sufficient permission to view the juror pool record(s)");
            assertEquals("Current user does not have sufficient permission to view the juror pool record(s)",
                exception.getMessage(),
                "Exception message must match");

            verify(jurorPoolRepository, times(1))
                .findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(anyString(), anyString());
            verifyNoInteractions(appearanceRepository);

        }

        @Test
        void negativeJurorAttendanceTabNoRecords() {

            String owner = "415";
            final JurorPool jurorPool = createValidJurorPool(TestConstants.VALID_POOL_NUMBER, owner);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(
                    anyString(), anyString());

            List<Appearance> appearances = new ArrayList<>();

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            JurorAttendanceDetailsResponseDto jurorAttendanceDetailsResponseDto =
                jurorRecordService.getJurorAttendanceDetails(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, buildPayload(owner));

            assertEquals(0, jurorAttendanceDetailsResponseDto.getData().size(),
                "No attendance records should be returned");

            verify(jurorPoolRepository, times(1)).findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(
                anyString(),
                anyString());
            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);
        }

        @Test
        void jurorAttendanceDetailsExcludeCheckInAndCheckOutFromFilter() {
            String owner = "415";

            final JurorPool jurorPool = createValidJurorPool(TestConstants.VALID_POOL_NUMBER, owner);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            List<Appearance> appearances = new ArrayList<>();
            Appearance appearance1 = buildAppearance();
            appearances.add(appearance1);

            Appearance appearance2 = SerializationUtils.clone(appearance1);
            appearance2.setAppearanceStage(AppearanceStage.CHECKED_IN);
            appearances.add(appearance2);

            Appearance appearance3 = SerializationUtils.clone(appearance1);
            appearance3.setAppearanceStage(AppearanceStage.CHECKED_OUT);
            appearances.add(appearance3);

            doReturn(appearances).when(appearanceRepository).findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            JurorAttendanceDetailsResponseDto jurorAttendanceDetailsResponseDto =
                jurorRecordService.getJurorAttendanceDetails(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, buildPayload(owner));

            assertEquals(1, jurorAttendanceDetailsResponseDto.getData().size(),
                "One attendance record should be returned");

            verify(jurorPoolRepository, times(1)).findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);
        }

        @Test
        void jurorAttendanceDetailsAppearanceStageIsNull() {
            String owner = "415";

            final JurorPool jurorPool = createValidJurorPool(TestConstants.VALID_POOL_NUMBER, owner);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            List<Appearance> appearances = new ArrayList<>();
            Appearance appearance1 = buildAppearance();
            appearances.add(appearance1);

            Appearance appearance2 = SerializationUtils.clone(appearance1);
            appearance2.setAppearanceStage(null);
            appearance2.setAttendanceType(AttendanceType.ABSENT);
            appearances.add(appearance2);

            doReturn(appearances).when(appearanceRepository).findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            JurorAttendanceDetailsResponseDto jurorAttendanceDetailsResponseDto =
                jurorRecordService.getJurorAttendanceDetails(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, buildPayload(owner));

            assertEquals(2, jurorAttendanceDetailsResponseDto.getData().size(),
                "Two attendance record should be returned");

            verify(jurorPoolRepository, times(1))
                .findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumber(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER);
        }

        private Appearance buildAppearance() {
            return Appearance.builder()
                .attendanceDate(LocalDate.now())
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .timeIn(LocalTime.of(9, 0))
                .timeOut(LocalTime.of(17, 0))
                .attendanceType(AttendanceType.FULL_DAY)
                .travelTime(LocalTime.of(1, 30))
                .appearanceStage(AppearanceStage.EXPENSE_ENTERED)
                .build();
        }
    }

    @Nested
    @DisplayName("public JurorPaymentsResponseDto getJurorPayments(String jurorNumber)")
    class GetJurorPayments {
        Tuple generateData(Integer value) {
            Tuple tuple = mock(Tuple.class);

            doReturn(LocalDate.now().minusDays(value + 3)).when(tuple)
                .get(QReportsJurorPayments.reportsJurorPayments.attendanceDate);

            if (value == 0) {
                doReturn(true).when(tuple).get(QReportsJurorPayments.reportsJurorPayments.nonAttendance);
            } else {
                doReturn(false).when(tuple).get(QReportsJurorPayments.reportsJurorPayments.nonAttendance);
                doReturn(String.valueOf(value)).when(tuple)
                    .get(QReportsJurorPayments.reportsJurorPayments.attendanceAudit);

                doReturn(BigDecimal.valueOf(value)).when(tuple)
                    .get(QReportsJurorPayments.reportsJurorPayments.totalTravelDue);
                doReturn(BigDecimal.valueOf(value)).when(tuple)
                    .get(QReportsJurorPayments.reportsJurorPayments.totalFinancialLossDue);
                doReturn(BigDecimal.valueOf(value)).when(tuple)
                    .get(QReportsJurorPayments.reportsJurorPayments.subsistenceDue);
                doReturn(BigDecimal.valueOf(value)).when(tuple)
                    .get(QReportsJurorPayments.reportsJurorPayments.smartCardDue);
                doReturn(BigDecimal.valueOf(2 * value)).when(tuple)
                    .get(QReportsJurorPayments.reportsJurorPayments.totalDue);

                if (value < 3) {
                    doReturn(String.valueOf(value)).when(tuple)
                        .get(QReportsJurorPayments.reportsJurorPayments.latestPaymentFAuditId);
                    doReturn(LocalDateTime.now().minusDays(value)).when(tuple)
                        .get(QReportsJurorPayments.reportsJurorPayments.paymentDate);
                    doReturn(BigDecimal.valueOf(2 * value)).when(tuple)
                        .get(QReportsJurorPayments.reportsJurorPayments.totalPaid);
                }
            }

            return tuple;
        }

        @AfterEach
        void afterEach() {
            TestUtils.afterAll();
        }

        @Test
        void positiveGetPayments() {
            String jurorNumber = "641500094";
            JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
            TestUtils.mockSecurityUtil(
                BureauJwtPayload.builder()
                    .owner("415")
                    .build()
            );

            doReturn(List.of(jurorPool)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActive(any(), anyBoolean());

            doReturn(List.of(generateData(0), generateData(1), generateData(2), generateData(3)))
                .when(jurorPaymentsSummaryRepository).fetchPaymentLogByJuror(jurorNumber);

            JurorPaymentsResponseDto payments = jurorRecordService.getJurorPayments(jurorNumber);

            assertEquals(3, payments.getAttendances(), "Incorrect number of attendances");
            assertEquals(1, payments.getNonAttendances(), "Incorrect number of non-attendances");
            assertEquals(BigDecimal.valueOf(6), payments.getFinancialLoss(), "Incorrect financial loss total");
            assertEquals(BigDecimal.valueOf(6), payments.getTravel(), "Incorrect travel total");
            assertEquals(BigDecimal.valueOf(6), payments.getSubsistence(), "Incorrect subsistence total");
            assertEquals(BigDecimal.valueOf(6), payments.getTotalPaid(), "Incorrect total paid");
            assertEquals(4, payments.getData().size(), "Incorrect number of attendance entries");
        }

        @Test
        void negativeNoPermission() {
            String jurorNumber = "641500094";
            JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
            TestUtils.mockSecurityUtil(
                BureauJwtPayload.builder()
                    .owner("416")
                    .build()
            );

            doReturn(List.of(jurorPool)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActive(any(), anyBoolean());

            assertThrows(MojException.Forbidden.class, () -> jurorRecordService.getJurorPayments(jurorNumber));
        }
    }

    @Nested
    @DisplayName("public JurorHistoryResponseDto getJurorHistory(String jurorNumber)")
    class GetJurorHistory {
        String poolNumber = "1234567";

        JurorHistory generateData(String item, Integer dateOffset) {
            JurorHistory historyItem = mock(JurorHistory.class);
            HistoryCodeMod code = mock(HistoryCodeMod.class);

            doReturn(item).when(code).getDescription();

            doReturn(code).when(historyItem).getHistoryCode();
            doReturn(LocalDateTime.now().minusDays(dateOffset)).when(historyItem).getDateCreated();
            doReturn(LocalDate.now().minusDays(dateOffset)).when(historyItem).getOtherInformationDate();
            doReturn(poolNumber).when(historyItem).getPoolNumber();
            doReturn(item).when(historyItem).getOtherInformation();
            doReturn(String.valueOf(item.charAt(0))).when(historyItem).getOtherInformationRef();
            doReturn("USER").when(historyItem).getCreatedBy();

            return historyItem;
        }

        @AfterEach
        void afterEach() {
            TestUtils.afterAll();
        }

        @Test
        void positiveGetHistory() {
            String jurorNumber = "641500094";
            JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
            TestUtils.mockSecurityUtil(
                BureauJwtPayload.builder()
                    .owner("415")
                    .build()
            );

            doReturn(List.of(jurorPool)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActive(any(), anyBoolean());

            doReturn(List.of(generateData("First", 1), generateData("Second", 2)))
                .when(jurorHistoryRepository).findByJurorNumber(jurorNumber);

            JurorHistoryResponseDto history = jurorRecordService.getJurorHistory(jurorNumber);

            assertEquals(2, history.data.size(), "Incorrect history length");
        }

        @Test
        void negativeNoPermission() {
            String jurorNumber = "641500094";
            JurorPool jurorPool = createValidJurorPool(jurorNumber, COURT_OWNER);
            TestUtils.mockSecurityUtil(
                BureauJwtPayload.builder()
                    .owner("416")
                    .build()
            );

            doReturn(List.of(jurorPool)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActive(any(), anyBoolean());

            assertThrows(MojException.Forbidden.class, () -> jurorRecordService.getJurorPayments(jurorNumber));
        }
    }

    @Nested
    @DisplayName("public void processPendingJuror(ProcessPendingJurorRequestDto processPendingJurorRequestDto)")
    class ProcessJurorRecord {

        @Test
        void positiveApprovePendingJuror() {

            TestUtils.setupAuthentication("415", "SENIORJURORUSER", "9");

            ProcessPendingJurorRequestDto processPendingJurorRequestDto =
                getProcessPendingJurorRequestDto(ApprovalDecision.APPROVE);

            PendingJurorStatus pendingJurorStatus =
                PendingJurorStatus.builder().code(PendingJurorStatusEnum.AUTHORISED.getCode()).build();

            when(pendingJurorStatusRepository.findById(PendingJurorStatusEnum.AUTHORISED.getCode()))
                .thenReturn(Optional.of(pendingJurorStatus));

            PendingJuror pendingJuror = getPendingJuror();
            when(pendingJurorRepository.findById(processPendingJurorRequestDto.getJurorNumber()))
                .thenReturn(Optional.of(pendingJuror));

            PoolRequest poolRequest = getPoolRequest();
            when(poolRequestRepository.findById("123456789"))
                .thenReturn(Optional.of(poolRequest));

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED))
                .thenReturn(Optional.of(jurorStatus));

            jurorRecordService.processPendingJuror(processPendingJurorRequestDto);

            verify(pendingJurorRepository, times(1)).findById(any(String.class));
            verify(pendingJurorStatusRepository, times(1)).findById(any(Character.class));
            verify(poolRequestRepository, times(1)).findById(any(String.class));
            verify(jurorRepository, times(1)).save(any(Juror.class));
            verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
            verify(pendingJurorRepository, times(1)).save(any(PendingJuror.class));
        }

        @Test
        void positiveRejectPendingJuror() {

            TestUtils.setupAuthentication("415", "SENIORJURORUSER", "9");

            ProcessPendingJurorRequestDto processPendingJurorRequestDto =
                getProcessPendingJurorRequestDto(ApprovalDecision.REJECT);
            PendingJuror pendingJuror = getPendingJuror();
            PendingJurorStatus pendingJurorStatus =
                PendingJurorStatus.builder().code(PendingJurorStatusEnum.QUEUED.getCode()).build();
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            when(pendingJurorStatusRepository.findById(PendingJurorStatusEnum.REJECTED.getCode()))
                .thenReturn(Optional.of(pendingJurorStatus));

            when(pendingJurorRepository.findById(processPendingJurorRequestDto.getJurorNumber()))
                .thenReturn(Optional.of(pendingJuror));

            jurorRecordService.processPendingJuror(processPendingJurorRequestDto);

            verify(pendingJurorRepository, times(1)).findById(any(String.class));
            verify(pendingJurorStatusRepository, times(1)).findById(any(Character.class));
            verify(pendingJurorRepository, times(1)).save(any(PendingJuror.class));

            verifyNoInteractions(poolRequestRepository);
            verifyNoInteractions(jurorRepository);
            verifyNoInteractions(jurorPoolRepository);

        }

        @ParameterizedTest
        @ValueSource(chars = {'R', 'A'})
        void negativePendingJurorWrongStatus(char pendingJurorStatusCode) {

            TestUtils.setupAuthentication("415", "SENIORJURORUSER", "9");

            ProcessPendingJurorRequestDto processPendingJurorRequestDto =
                getProcessPendingJurorRequestDto(ApprovalDecision.APPROVE);
            PendingJuror pendingJuror = getPendingJuror();
            pendingJuror.setStatus(
                PendingJurorStatus.builder().code(pendingJurorStatusCode).build());

            when(pendingJurorRepository.findById(processPendingJurorRequestDto.getJurorNumber()))
                .thenReturn(Optional.of(pendingJuror));

            MojException.BadRequest exception =
                assertThrows(MojException.BadRequest.class,
                    () -> jurorRecordService.processPendingJuror(processPendingJurorRequestDto),
                    "Pending Juror has already been processed");
            assertEquals("Pending Juror has already been processed",
                exception.getMessage(),
                "Exception message must match");

            verify(pendingJurorRepository, times(1)).findById(any(String.class));
            verifyNoInteractions(pendingJurorStatusRepository);
            verifyNoInteractions(poolRequestRepository);
            verifyNoInteractions(jurorRepository);
            verifyNoInteractions(jurorPoolRepository);

        }

        @Nested
        @DisplayName("public FilterableJurorDetailsResponseDto getJurorDetails(FilterableJurorDetailsRequestDto "
            + "request)")
        class GetJurorDetailsFilterable {
            private MockedStatic<PaymentDetails> paymentDetailsMockedStatic;
            private MockedStatic<NameDetails> nameDetailsMockedStatic;
            private MockedStatic<JurorAddressDto> jurorAddressDtoMockedStatic;

            @AfterEach
            void mockCurrentUser() {
                if (paymentDetailsMockedStatic != null) {
                    paymentDetailsMockedStatic.close();
                }
                if (nameDetailsMockedStatic != null) {
                    nameDetailsMockedStatic.close();
                }
                if (jurorAddressDtoMockedStatic != null) {
                    jurorAddressDtoMockedStatic.close();
                }
            }

            @Test
            void typicalWithJurorVersion() {
                assertAndTrigger(FilterableJurorDetailsRequestDto.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .jurorVersion(1L)
                    .include(List.of(
                        FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS,
                        NAME_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.MILEAGE))
                    .build());
            }

            @Test
            void typicalWithOutJurorVersion() {
                assertAndTrigger(FilterableJurorDetailsRequestDto.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .jurorVersion(null)
                    .include(List.of(
                        FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.MILEAGE))
                    .build());
            }

            private void assertAndTrigger(FilterableJurorDetailsRequestDto request) {
                Juror juror = mock(Juror.class);
                JurorPool jurorPool = mock(JurorPool.class);

                PaymentDetails paymentDetails = mock(PaymentDetails.class);
                paymentDetailsMockedStatic = mockStatic(PaymentDetails.class);
                paymentDetailsMockedStatic.when(() -> PaymentDetails.from(juror)).thenReturn(paymentDetails);

                NameDetails nameDetails = mock(NameDetails.class);
                nameDetailsMockedStatic = mockStatic(NameDetails.class);
                nameDetailsMockedStatic.when(() -> NameDetails.from(juror)).thenReturn(nameDetails);

                JurorAddressDto jurorAddressDto = mock(JurorAddressDto.class);
                jurorAddressDtoMockedStatic = mockStatic(JurorAddressDto.class);
                jurorAddressDtoMockedStatic.when(() -> JurorAddressDto.from(juror)).thenReturn(jurorAddressDto);

                jurorRecordService = spy(jurorRecordService);

                doReturn(juror).when(jurorRecordService)
                    .getJuror(request.getJurorNumber(), request.getJurorVersion());

                FilterableJurorDetailsResponseDto response = jurorRecordService.getJurorDetails(request);

                assertThat(response).isNotNull();
                assertThat(response.getJurorNumber()).isNotNull().isEqualTo(request.getJurorNumber());
                assertThat(response.getJurorVersion()).isEqualTo(request.getJurorVersion());

                TriConsumer<Object, Object, FilterableJurorDetailsRequestDto.IncludeType> includeTypeValidator =
                    (mock, actual, includeType) -> {
                        if (request.getInclude().contains(includeType)) {
                            assertThat(actual).isNotNull().isEqualTo(mock);
                        } else {
                            assertThat(actual).isNull();
                        }
                    };
                includeTypeValidator.accept(paymentDetails, response.getPaymentDetails(),
                    FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS);
                includeTypeValidator.accept(nameDetails, response.getNameDetails(),
                    NAME_DETAILS);
                includeTypeValidator.accept(jurorAddressDto, response.getAddress(),
                    FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS);


                verify(jurorRecordService, times(1))
                    .getJuror(request.getJurorNumber(), request.getJurorVersion());
            }
        }

        @Nested
        @DisplayName("Juror getJuror(String jurorNumber, Long jurorVersion)")
        class GetJuror {
            @Test
            void positiveJurorNumberWithNoVersion() {
                Juror juror = mock(Juror.class);
                when(jurorRepository.findByJurorNumber(TestConstants.VALID_JUROR_NUMBER))
                    .thenReturn(juror);

                assertThat(
                    jurorRecordService.getJuror(TestConstants.VALID_JUROR_NUMBER, null)
                ).isEqualTo(juror);

                verify(jurorRepository, times(1))
                    .findByJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                verifyNoMoreInteractions(jurorRepository);
            }

            @Test
            @SuppressWarnings("unchecked")
            void positiveJurorNumberWithVersion() {
                Juror juror = mock(Juror.class);
                Revision<Long, Juror> revision = mock(Revision.class);
                when(revision.getEntity()).thenReturn(juror);

                when(jurorRepository.findRevision(TestConstants.VALID_JUROR_NUMBER, 1L))
                    .thenReturn(Optional.of(revision));

                assertThat(
                    jurorRecordService.getJuror(TestConstants.VALID_JUROR_NUMBER, 1L)
                ).isEqualTo(juror);

                verify(jurorRepository, times(1))
                    .findRevision(TestConstants.VALID_JUROR_NUMBER, 1L);
                verifyNoMoreInteractions(jurorRepository);
            }

            @Test
            void negativeJurorNumberWithNoVersionNotFound() {
                when(jurorRepository.findByJurorNumber(TestConstants.VALID_JUROR_NUMBER))
                    .thenReturn(null);

                MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                    () -> jurorRecordService.getJuror(TestConstants.VALID_JUROR_NUMBER, null),
                    "When juror cannot be found an exception should be thrown");

                assertThat(exception.getMessage()).isNotNull().isEqualTo(
                    "Juror not found: JurorNumber: " + TestConstants.VALID_JUROR_NUMBER + " Revision: null"
                );
                assertThat(exception.getCause()).isNull();
            }

            @Test
            void negativeJurorNumberWithVersionNotFound() {
                when(jurorRepository.findRevision(TestConstants.VALID_JUROR_NUMBER, 1L))
                    .thenReturn(Optional.empty());

                MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                    () -> jurorRecordService.getJuror(TestConstants.VALID_JUROR_NUMBER, 1L),
                    "When juror cannot be found an exception should be thrown");

                assertThat(exception.getMessage()).isNotNull().isEqualTo(
                    "Juror not found: JurorNumber: " + TestConstants.VALID_JUROR_NUMBER + " Revision: 1"
                );
                assertThat(exception.getCause()).isNull();
            }
        }

        private PoolRequest getPoolRequest() {
            return PoolRequest.builder()
                .poolNumber("123456789")
                .owner("123456789")
                .courtLocation(new CourtLocation())
                .newRequest('N')
                .returnDate(LocalDate.of(2021, 1, 1))
                .attendTime(LocalDateTime.of(LocalDate.of(2021, 1, 1), LocalTime.of(9, 15)))
                .poolType(new PoolType())
                .build();
        }

        private PendingJuror getPendingJuror() {
            return PendingJuror.builder()
                .jurorNumber("123456789")
                .poolNumber("123456789")
                .title("Mr")
                .firstName("John")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .addressLine1("Address Line 1")
                .addressLine2("Address Line 2")
                .addressLine3("Address Line 3")
                .addressLine4("Address Line 4")
                .addressLine5("Address Line 5")
                .postcode("M24 4BP")
                .email("email@com")
                .notes("Notes")
                .status(PendingJurorStatus.builder().code(PendingJurorStatusEnum.QUEUED.getCode()).build())
                .nextDate(LocalDate.now().plusDays(10))
                .responded(true)
                .build();
        }

        private ProcessPendingJurorRequestDto getProcessPendingJurorRequestDto(ApprovalDecision approvalDecision) {
            return ProcessPendingJurorRequestDto.builder()
                .jurorNumber("123456789")
                .decision(approvalDecision)
                .comments("Some Comments")
                .build();
        }
    }

    @Nested
    @DisplayName("Juror getJurorBankDetails(String jurorNumber)")
    class GetJurorBankDetails {

        public static final String JUROR_NUMBER = "123456789";

        @Test
        void positiveTypical() {
            String courtOwner = "415";
            String username = "JURY_USER";

            TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

            Juror juror = new Juror();
            juror.setJurorNumber(JUROR_NUMBER);
            juror.setBankAccountNumber("12345678");
            juror.setSortCode("115578");
            juror.setAddressLine1("Address Line 1");
            juror.setAddressLine2("Address Line 1");
            juror.setAddressLine3("Address Line 1");
            juror.setAddressLine4("Address Line 1");
            juror.setAddressLine5("Address Line 1");
            juror.setPostcode("M24 4BP");
            juror.setNotes("Some notes");

            JurorPool jurorPool = new JurorPool();
            jurorPool.setJuror(juror);
            jurorPool.setOwner("415");

            juror.setAssociatedPools(Set.of(jurorPool));

            when(jurorRepository.findById(JUROR_NUMBER))
                .thenReturn(Optional.of(juror));

            JurorBankDetailsDto jurorBankDetailsDto = jurorRecordService.getJurorBankDetails(JUROR_NUMBER);

            verifyJurorBankDetails(jurorBankDetailsDto, juror);
            verify(jurorRepository, times(1)).findById(JUROR_NUMBER);

        }

        void verifyJurorBankDetails(JurorBankDetailsDto jurorBankDetailsDto, Juror juror) {
            assertThat(jurorBankDetailsDto).isNotNull();
            assertThat(jurorBankDetailsDto.getBankAccountNumber()).isEqualTo(juror.getBankAccountNumber());
            assertThat(jurorBankDetailsDto.getSortCode()).isEqualTo(juror.getSortCode());
            assertThat(jurorBankDetailsDto.getAddressLineOne()).isEqualTo(juror.getAddressLine1());
            assertThat(jurorBankDetailsDto.getAddressLineTwo()).isEqualTo(juror.getAddressLine2());
            assertThat(jurorBankDetailsDto.getAddressLineThree()).isEqualTo(juror.getAddressLine3());
            assertThat(jurorBankDetailsDto.getAddressLineFour()).isEqualTo(juror.getAddressLine4());
            assertThat(jurorBankDetailsDto.getAddressLineFive()).isEqualTo(juror.getAddressLine5());
            assertThat(jurorBankDetailsDto.getPostCode()).isEqualTo(juror.getPostcode());
            assertThat(jurorBankDetailsDto.getNotes()).isEqualTo(juror.getNotes());
        }

        @Test
        void negativeUserForbidden() {
            String courtOwner = "415";
            String username = "JURY_USER";

            TestUtils.setUpMockAuthentication(courtOwner, username, "1", List.of(courtOwner));

            Juror juror = new Juror();
            juror.setJurorNumber(JUROR_NUMBER);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setJuror(juror);
            jurorPool.setOwner("416"); // different owner to the current user

            juror.setAssociatedPools(Set.of(jurorPool));

            when(jurorRepository.findById(JUROR_NUMBER))
                .thenReturn(Optional.of(juror));

            MojException.Forbidden exception = assertThrows(MojException.Forbidden.class,
                () -> jurorRecordService.getJurorBankDetails(JUROR_NUMBER),
                "When user does not have access to juror record an exception should be thrown");

            assertThat(exception.getMessage()).isNotNull().isEqualTo(
                "User does not have ownership of the supplied juror record");
            assertThat(exception.getCause()).isNull();
        }


        @Test
        void negativeJurorRecordNotFound() {
            when(jurorRepository.findById(JUROR_NUMBER))
                .thenReturn(Optional.empty());

            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> jurorRecordService.getJurorBankDetails(JUROR_NUMBER),
                "When juror cannot be found an exception should be thrown");

            assertThat(exception.getMessage()).isNotNull().isEqualTo(
                "Unable to find valid juror record for Juror Number: " + JUROR_NUMBER);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Confirm juror identity")
    class ConfirmJurorIdentity {

        @Test
        void happyPath() {
            TestUtils.setUpMockAuthentication("415", "CourtUser", "1", List.of("415"));
            String jurorNumber = "111111111";

            ConfirmIdentityDto dto = ConfirmIdentityDto.builder()
                .jurorNumber(jurorNumber)
                .idCheckCode(IdCheckCodeEnum.L)
                .build();

            JurorPool jurorPool = createValidJurorPool(VALID_JUROR_NUMBER, "415");

            when(jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true))
                .thenReturn(Collections.singletonList(jurorPool));
            jurorRecordService.confirmIdentity(dto);

            ArgumentCaptor<JurorPool> jurorPoolArgumentCaptor = ArgumentCaptor.forClass(JurorPool.class);

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActive(jurorNumber, true);
            verify(jurorPoolRepository, times(1))
                .save(jurorPoolArgumentCaptor.capture());

            JurorPool updatedJurorPool = jurorPoolArgumentCaptor.getValue();
            assertEquals(IdCheckCodeEnum.L.getCode(), updatedJurorPool.getIdChecked(), "Id check code must match");

            verify(jurorHistoryService, times(1)).createIdentityConfirmedHistory(jurorPool);

        }

        @Test
        void wrongCourtUser() {
            TestUtils.setUpMockAuthentication("416", "CourtUser", "1", List.of("416"));
            String jurorNumber = "111111111";

            ConfirmIdentityDto dto = ConfirmIdentityDto.builder()
                .jurorNumber(jurorNumber)
                .idCheckCode(IdCheckCodeEnum.L)
                .build();

            JurorPool jurorPool = createValidJurorPool(jurorNumber, "415");
            when(jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true))
                .thenReturn(Collections.singletonList(jurorPool));

            MojException.Forbidden exception
                = assertThrows(MojException.Forbidden.class, () -> jurorRecordService.confirmIdentity(dto),
                "Forbidden exception");

            assertEquals("Current user (416) does not own any Juror "
                    + "Pool associations for Juror Number: " + jurorNumber,
                exception.getMessage(), "Exception message should match");

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActive(jurorNumber, true);
            verify(jurorPoolRepository, times(0)).save(any(JurorPool.class));
            verify(jurorHistoryService, times(0)).createIdentityConfirmedHistory(any(JurorPool.class));

        }


        @Test
        void jurorNotFound() {
            TestUtils.setUpMockAuthentication("416", "CourtUser", "1", List.of("416"));
            String jurorNumber = "111111111";

            ConfirmIdentityDto dto = ConfirmIdentityDto.builder()
                .jurorNumber(jurorNumber)
                .idCheckCode(IdCheckCodeEnum.L)
                .build();

            when(jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true))
                .thenReturn(Collections.emptyList());

            MojException.NotFound exception
                = assertThrows(MojException.NotFound.class, () -> jurorRecordService.confirmIdentity(dto),
                "Not found");

            assertEquals("Unable to find any Juror Pool associations for juror number " + jurorNumber,
                exception.getMessage(), "Exception message should match");

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActive(jurorNumber, true);
            verify(jurorPoolRepository, times(0)).save(any(JurorPool.class));
            verify(jurorHistoryService, times(0)).createIdentityConfirmedHistory(any(JurorPool.class));

        }
    }

    @Nested
    @DisplayName("Mark juror as responded")
    class MarkAsRespondedTest {

        @Test
        void shouldMarkJurorAsRespondedWhenJurorExistsAndDateOfBirthIsNotNull() {
            TestUtils.setUpMockAuthentication("400", "Bureau", "1", List.of("400"));

            final String jurorNumber = "123456789";
            final Juror juror = new Juror();
            juror.setDateOfBirth(LocalDate.now().minusYears(20));

            final PoolRequest poolRequest = new PoolRequest();
            poolRequest.setPoolNumber("123456789");

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner("400");
            jurorPool.setPool(poolRequest);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setLocCode("415");
            jurorPool.setLocation(courtLocation.getLocCode());
            jurorPool.setJuror(juror);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true))
                    .thenReturn(List.of(jurorPool));
            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED)).thenReturn(Optional.of(jurorStatus));

            jurorRecordService.markResponded(jurorNumber);

            verify(jurorRepository, times(1)).save(juror);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verify(jurorHistoryRepository, times(1)).save(any(JurorHistory.class));
        }

        @Test
        void shouldThrowExceptionWhenJurorDoesNotExist() {

            TestUtils.setUpMockAuthentication("415", "CourtUser", "1", List.of("415"));

            String jurorNumber = "123456789";
            when(jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true)).thenReturn(List.of());

            assertThrows(MojException.NotFound.class, () -> jurorRecordService.markResponded(jurorNumber));
        }

        @Test
        void shouldThrowExceptionWhenJurorDateOfBirthIsNull() {
            TestUtils.setUpMockAuthentication("400", "Bureau", "1", List.of("400"));

            final String jurorNumber = "123456789";
            final Juror juror = new Juror();

            PoolRequest poolRequest = new PoolRequest();
            poolRequest.setPoolNumber("123456789");

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner("400");
            jurorPool.setPool(poolRequest);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setLocCode("415");
            jurorPool.setLocation(courtLocation.getLocCode());
            jurorPool.setJuror(juror);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true))
                .thenReturn(List.of(jurorPool));

            Exception exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> jurorRecordService.markResponded(jurorNumber));

            assertEquals("Juror date of birth is required to mark as responded", exception.getMessage(),
                "Exception message should match");

        }
    }


    private BureauJurorDetailDto createBureauJurorDetailDto(String jurorNumber) {

        BureauJurorDetailDto bureauJurorDetailDto = new BureauJurorDetailDto();
        bureauJurorDetailDto.setJurorNumber(jurorNumber);

        return bureauJurorDetailDto;
    }
}
