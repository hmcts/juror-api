package uk.gov.hmcts.juror.api.moj.service.letter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.CertificateOfAttendanceLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.DeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.ExcusalLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterResponseData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.NonDeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.WithdrawalLetterData;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.CertificateOfAttendanceLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralDeniedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalRefusedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.WithdrawalLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.repository.MojExcusalCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.CertificateOfAttendanceListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.DeferralDeniedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.DeferralGrantedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.ExcusalGrantedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.ExcusalRefusalLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.FailedToAttendLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.ShowCauseLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.WithdrawalLetterListRepository;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtExcusalRefusedLetterServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtFailedToAttendLetterServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtLetterServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtPostponementLetterServiceImpl;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.ExcessiveImports")
public class CourtLetterServiceTest {

    private DeferralGrantedLetterListRepository deferralGrantedLetterListRepository;
    private ExcusalGrantedLetterListRepository excusalGrantedLetterListRepository;
    private ExcusalRefusalLetterListRepository excusalRefusalLetterListRepository;
    private DeferralDeniedLetterListRepository deferralDeniedLetterListRepository;
    private WithdrawalLetterListRepository withdrawalLetterListRepository;
    private CertificateOfAttendanceListRepository certificateOfAttendanceListRepository;
    private MojExcusalCodeRepository excusalCodeRepository;
    private ShowCauseLetterListRepository showCauseLetterListRepository;
    private FailedToAttendLetterListRepository failedToAttendLetterListRepository;

    private CourtLetterServiceImpl courtLetterService;
    private CourtExcusalRefusedLetterServiceImpl courtExcusalRefusedLetterService;
    private CourtFailedToAttendLetterServiceImpl courtFailedToAttendLetterService;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        this.deferralGrantedLetterListRepository = mock(DeferralGrantedLetterListRepository.class);
        this.withdrawalLetterListRepository = mock(WithdrawalLetterListRepository.class);
        this.excusalGrantedLetterListRepository = mock(ExcusalGrantedLetterListRepository.class);
        this.deferralDeniedLetterListRepository = mock(DeferralDeniedLetterListRepository.class);
        this.certificateOfAttendanceListRepository = mock(CertificateOfAttendanceListRepository.class);
        this.showCauseLetterListRepository = mock(ShowCauseLetterListRepository.class);
        this.excusalCodeRepository = mock(MojExcusalCodeRepository.class);
        this.courtExcusalRefusedLetterService = mock(CourtExcusalRefusedLetterServiceImpl.class);
        this.excusalRefusalLetterListRepository = mock(ExcusalRefusalLetterListRepository.class);
        this.failedToAttendLetterListRepository = mock(FailedToAttendLetterListRepository.class);
        this.courtExcusalRefusedLetterService =
            new CourtExcusalRefusedLetterServiceImpl(excusalRefusalLetterListRepository);

        CourtPostponementLetterServiceImpl courtPostponementLetterService =
            mock(CourtPostponementLetterServiceImpl.class);

        ShowCauseLetterListRepository showCauseLetterListRepository = mock(ShowCauseLetterListRepository.class);

        this.courtLetterService =
            new CourtLetterServiceImpl(certificateOfAttendanceListRepository, deferralGrantedLetterListRepository,
                excusalGrantedLetterListRepository,
                deferralDeniedLetterListRepository,
                withdrawalLetterListRepository,
                excusalCodeRepository,
                showCauseLetterListRepository,
                courtPostponementLetterService,
                courtExcusalRefusedLetterService,
                courtFailedToAttendLetterService);

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void afterEach() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
    }

    private void mockCurrentUser(String owner) {
        securityUtilMockedStatic = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
            .thenReturn(owner);
        securityUtilMockedStatic.when(SecurityUtil::getLocCode)
            .thenReturn(owner);
    }

    @Nested
    @DisplayName("getEligibleList - Deferral Granted")
    class DeferralGranted {

        @Test
        @DisplayName("getEligibleList - exclude printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListExcludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String reason = "Moved from area";
            LocalDate deferredTo = LocalDate.of(2024, 1, 29);
            boolean includePrinted = false;

            List<DeferralGrantedLetterList> results =
                List.of(createDeferralLetterList(jurorNumber, deferredTo, reason, poolNumber));

            when(deferralGrantedLetterListRepository.findJurorsEligibleForDeferralGrantedLetter(
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.DEFERRAL_GRANTED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((DeferralLetterData) data.get(0), jurorNumber, deferredTo, reason, poolNumber, null);
        }

        @Test
        @DisplayName("getEligibleList - include printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListIncludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String reason = "Moved from area";
            LocalDate deferredTo = LocalDate.of(2024, 1, 29);
            LocalDateTime datePrinted = LocalDateTime.of(2024, 1, 22, 12, 35);
            boolean includePrinted = true;

            List<DeferralGrantedLetterList> results =
                List.of(createDeferralLetterList(jurorNumber, deferredTo, reason, poolNumber,
                    datePrinted));

            when(deferralGrantedLetterListRepository.findJurorsEligibleForDeferralGrantedLetter(
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.DEFERRAL_GRANTED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((DeferralLetterData) data.get(0), jurorNumber, deferredTo, reason, poolNumber,
                datePrinted.toLocalDate());
        }

        @Test
        @DisplayName("getEligibleList - no data")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListNoData() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            boolean includePrinted = false;

            List<DeferralGrantedLetterList> results = new ArrayList<>();

            when(deferralGrantedLetterListRepository.findJurorsEligibleForDeferralGrantedLetter(
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.DEFERRAL_GRANTED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            Assertions.assertThat(response.getData()).isEmpty();
        }

        private DeferralGrantedLetterList createDeferralLetterList(String jurorNumber, LocalDate deferredTo,
                                                                   String reason,
                                                                   String poolNumber) {
            return createDeferralLetterList(jurorNumber, deferredTo, reason, poolNumber, null);
        }

        private DeferralGrantedLetterList createDeferralLetterList(String jurorNumber, LocalDate deferredTo,
                                                                   String reason, String poolNumber,
                                                                   LocalDateTime datePrinted) {
            return DeferralGrantedLetterList.builder()
                .jurorNumber(jurorNumber)
                .firstName("Test")
                .lastName("Person")
                .postcode("CH1 2AN")
                .status("deferred")
                .deferralDate(deferredTo)
                .deferralReason(reason)
                .poolNumber(poolNumber)
                .datePrinted(datePrinted)
                .build();
        }

        private void validateHeadings(List<String> headings) {
            Assertions.assertThat(headings.size()).isEqualTo(9);
            Assertions.assertThat(headings.get(0)).isEqualToIgnoringCase("Juror number");
            Assertions.assertThat(headings.get(1)).isEqualToIgnoringCase("First name");
            Assertions.assertThat(headings.get(2)).isEqualToIgnoringCase("Last name");
            Assertions.assertThat(headings.get(3)).isEqualToIgnoringCase("Postcode");
            Assertions.assertThat(headings.get(4)).isEqualToIgnoringCase("Status");
            Assertions.assertThat(headings.get(5)).isEqualToIgnoringCase("Deferred to");
            Assertions.assertThat(headings.get(6)).isEqualToIgnoringCase("Reason");
            Assertions.assertThat(headings.get(7)).isEqualToIgnoringCase("Date printed");
            Assertions.assertThat(headings.get(8)).isEqualToIgnoringCase("Pool Number");
        }

        private void validateDataTypes(List<String> dataTypes) {
            Assertions.assertThat(dataTypes.size()).isEqualTo(9);
            Assertions.assertThat(dataTypes.get(0)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(1)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(2)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(3)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(4)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(5)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(6)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(7)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(8)).isEqualToIgnoringCase("hidden");
        }

        private void validateData(DeferralLetterData data, String jurorNumber, LocalDate deferredTo, String reason,
                                  String poolNumber, LocalDate datePrinted) {
            Assertions.assertThat(data.getJurorNumber()).isEqualToIgnoringCase(jurorNumber);
            Assertions.assertThat(data.getFirstName()).isEqualToIgnoringCase("Test");
            Assertions.assertThat(data.getLastName()).isEqualToIgnoringCase("Person");
            Assertions.assertThat(data.getPostcode()).isEqualToIgnoringCase("CH1 2AN");
            Assertions.assertThat(data.getStatus()).isEqualToIgnoringCase("Deferred");
            Assertions.assertThat(data.getDeferredTo()).isEqualTo(deferredTo);
            Assertions.assertThat(data.getReason()).isEqualToIgnoringCase(reason);
            Assertions.assertThat(data.getDatePrinted()).isEqualTo(datePrinted);
            Assertions.assertThat(data.getPoolNumber()).isEqualToIgnoringCase(poolNumber);
        }
    }

    @Nested
    @DisplayName("getEligibleList - Deferral Refused")
    class DeferralRefused {

        @Test
        @DisplayName("getEligibleList - exclude printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListExcludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String otherInformation = "Deferral Denied - A";
            String statusDesc = "Responded";
            LocalDate dateRefused = LocalDate.of(2024, 1, 29);

            String reasonCode = "A";
            String reasonDesc = "Moved from area";
            ExcusalCode excusalCode = new ExcusalCode();
            excusalCode.setDescription(reasonDesc);
            excusalCode.setCode(reasonCode);

            List<DeferralDeniedLetterList> results =
                List.of(
                    createDeferralDeniedLetterList(jurorNumber, statusDesc, dateRefused, otherInformation, poolNumber));

            doReturn(results).when(deferralDeniedLetterListRepository)
                .findJurorsEligibleForDeferralDeniedLetter(Mockito.any(), Mockito.any());
            doReturn(Optional.of(excusalCode)).when(excusalCodeRepository).findById(reasonCode);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.DEFERRAL_REFUSED)
                .jurorNumber(jurorNumber)
                .includePrinted(false)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((NonDeferralLetterData) data.get(0), jurorNumber, statusDesc, dateRefused, reasonDesc,
                poolNumber, null);
        }

        @Test
        @DisplayName("getEligibleList - include printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListIncludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String otherInfo = "Deferral Denied - I";
            String statusDesc = "Deferred";
            LocalDate dateRefused = LocalDate.of(2024, 1, 29);
            LocalDateTime datePrinted = LocalDateTime.of(2024, 1, 22, 12, 35);

            String reasonCode = "I";
            String reasonDesc = "Ill";
            ExcusalCode excusalCode = new ExcusalCode();
            excusalCode.setDescription(reasonDesc);
            excusalCode.setCode(reasonCode);

            List<DeferralDeniedLetterList> results =
                List.of(createDeferralDeniedLetterList(jurorNumber, statusDesc, dateRefused, otherInfo, poolNumber,
                    datePrinted));

            doReturn(results).when(deferralDeniedLetterListRepository)
                .findJurorsEligibleForDeferralDeniedLetter(Mockito.any(), Mockito.any());
            doReturn(Optional.of(excusalCode)).when(excusalCodeRepository).findById(reasonCode);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.DEFERRAL_REFUSED)
                .jurorNumber(jurorNumber)
                .includePrinted(true)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((NonDeferralLetterData) data.get(0), jurorNumber, statusDesc, dateRefused, reasonDesc,
                poolNumber,
                datePrinted.toLocalDate());
        }

        @Test
        @DisplayName("getEligibleList - no data")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListNoData() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            boolean includePrinted = false;

            List<DeferralDeniedLetterList> results = new ArrayList<>();

            doReturn(results).when(deferralDeniedLetterListRepository)
                .findJurorsEligibleForDeferralDeniedLetter(Mockito.any(), Mockito.any());

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.DEFERRAL_REFUSED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            Assertions.assertThat(response.getData()).isEmpty();
        }

        private DeferralDeniedLetterList createDeferralDeniedLetterList(String jurorNumber, String status,
                                                                        LocalDate dateRefused, String reason,
                                                                        String poolNumber) {
            return createDeferralDeniedLetterList(jurorNumber, status, dateRefused, reason, poolNumber,
                null);
        }

        private DeferralDeniedLetterList createDeferralDeniedLetterList(String jurorNumber, String status,
                                                                        LocalDate dateRefused,
                                                                        String otherInformation, String poolNumber,
                                                                        LocalDateTime datePrinted) {
            return DeferralDeniedLetterList.builder()
                .jurorNumber(jurorNumber)
                .firstName("Test")
                .lastName("Person")
                .postcode("CH1 2AN")
                .status(status)
                .refusalDate(dateRefused)
                .otherInformation(otherInformation)
                .poolNumber(poolNumber)
                .datePrinted(datePrinted)
                .build();
        }

        private void validateHeadings(List<String> headings) {
            Assertions.assertThat(headings.size()).isEqualTo(9);
            Assertions.assertThat(headings.get(0)).isEqualToIgnoringCase("Juror number");
            Assertions.assertThat(headings.get(1)).isEqualToIgnoringCase("First name");
            Assertions.assertThat(headings.get(2)).isEqualToIgnoringCase("Last name");
            Assertions.assertThat(headings.get(3)).isEqualToIgnoringCase("Postcode");
            Assertions.assertThat(headings.get(4)).isEqualToIgnoringCase("Status");
            Assertions.assertThat(headings.get(5)).isEqualToIgnoringCase("Date refused");
            Assertions.assertThat(headings.get(6)).isEqualToIgnoringCase("Reason");
            Assertions.assertThat(headings.get(7)).isEqualToIgnoringCase("Date printed");
            Assertions.assertThat(headings.get(8)).isEqualToIgnoringCase("Pool Number");
        }

        private void validateDataTypes(List<String> dataTypes) {
            Assertions.assertThat(dataTypes.size()).isEqualTo(9);
            Assertions.assertThat(dataTypes.get(0)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(1)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(2)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(3)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(4)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(5)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(6)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(7)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(8)).isEqualToIgnoringCase("hidden");
        }

        private void validateData(NonDeferralLetterData data, String jurorNumber, String status, LocalDate dateRefused,
                                  String reason, String poolNumber, LocalDate datePrinted) {
            Assertions.assertThat(data.getJurorNumber()).isEqualToIgnoringCase(jurorNumber);
            Assertions.assertThat(data.getFirstName()).isEqualToIgnoringCase("Test");
            Assertions.assertThat(data.getLastName()).isEqualToIgnoringCase("Person");
            Assertions.assertThat(data.getPostcode()).isEqualToIgnoringCase("CH1 2AN");
            Assertions.assertThat(data.getStatus()).isEqualToIgnoringCase(status);
            Assertions.assertThat(data.getDateRefused()).isEqualTo(dateRefused);
            Assertions.assertThat(data.getReason()).isEqualToIgnoringCase(reason);
            Assertions.assertThat(data.getDatePrinted()).isEqualTo(datePrinted);
            Assertions.assertThat(data.getPoolNumber()).isEqualToIgnoringCase(poolNumber);
        }

    }

    @Nested
    @DisplayName("getEligibleList - Excusal Granted")
    public class ExcusalGranted {
        @Test
        @DisplayName("getEligibleList - exclude printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListExcludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String reason = "Moved from area";
            LocalDate deferredTo = LocalDate.of(2024, 1, 29);
            boolean includePrinted = false;

            List<ExcusalGrantedLetterList> results =
                List.of(createExcusalLetterList(jurorNumber, deferredTo, reason, poolNumber, null));

            when(excusalGrantedLetterListRepository.findJurorsEligibleForExcusalGrantedLetter(
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((ExcusalLetterData) data.get(0), jurorNumber, deferredTo, reason, poolNumber, null);
        }

        @Test
        @DisplayName("getEligibleList - include printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListIncludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String reason = "Moved from area";
            LocalDate dateExcused = LocalDate.of(2024, 1, 29);
            LocalDateTime datePrinted = LocalDateTime.of(2024, 1, 22, 12, 35);
            boolean includePrinted = true;

            List<ExcusalGrantedLetterList> results =
                List.of(createExcusalLetterList(jurorNumber, dateExcused, reason, poolNumber,
                    datePrinted));

            when(excusalGrantedLetterListRepository.findJurorsEligibleForExcusalGrantedLetter(
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((ExcusalLetterData) data.get(0), jurorNumber, dateExcused, reason, poolNumber,
                datePrinted.toLocalDate());
        }

        @Test
        @DisplayName("getEligibleList - no data")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListNoData() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            boolean includePrinted = false;

            List<ExcusalGrantedLetterList> results = new ArrayList<>();

            when(excusalGrantedLetterListRepository.findJurorsEligibleForExcusalGrantedLetter(
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.EXCUSAL_GRANTED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            Assertions.assertThat(response.getData()).isEmpty();
        }

        private ExcusalGrantedLetterList createExcusalLetterList(String jurorNumber, LocalDate dateExcused,
                                                                 String reason, String poolNumber,
                                                                 LocalDateTime datePrinted) {
            return ExcusalGrantedLetterList.builder()
                .jurorNumber(jurorNumber)
                .firstName("Test")
                .lastName("Person")
                .postcode("CH1 2AN")
                .status("Excused")
                .dateExcused(dateExcused)
                .reason(reason)
                .poolNumber(poolNumber)
                .datePrinted(datePrinted)
                .build();
        }

        private void validateHeadings(List<String> headings) {
            Assertions.assertThat(headings.size()).isEqualTo(9);
            Assertions.assertThat(headings.get(0)).isEqualToIgnoringCase("Juror number");
            Assertions.assertThat(headings.get(1)).isEqualToIgnoringCase("First name");
            Assertions.assertThat(headings.get(2)).isEqualToIgnoringCase("Last name");
            Assertions.assertThat(headings.get(3)).isEqualToIgnoringCase("Postcode");
            Assertions.assertThat(headings.get(4)).isEqualToIgnoringCase("Status");
            Assertions.assertThat(headings.get(5)).isEqualToIgnoringCase("Date excused");
            Assertions.assertThat(headings.get(6)).isEqualToIgnoringCase("Reason");
            Assertions.assertThat(headings.get(7)).isEqualToIgnoringCase("Date printed");
            Assertions.assertThat(headings.get(8)).isEqualToIgnoringCase("Pool Number");
        }

        private void validateDataTypes(List<String> dataTypes) {
            Assertions.assertThat(dataTypes.size()).isEqualTo(9);
            Assertions.assertThat(dataTypes.get(0)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(1)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(2)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(3)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(4)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(5)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(6)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(7)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(8)).isEqualToIgnoringCase("hidden");
        }

        private void validateData(ExcusalLetterData data, String jurorNumber, LocalDate dateExcused, String reason,
                                  String poolNumber, LocalDate datePrinted) {
            Assertions.assertThat(data.getJurorNumber()).isEqualToIgnoringCase(jurorNumber);
            Assertions.assertThat(data.getFirstName()).isEqualToIgnoringCase("Test");
            Assertions.assertThat(data.getLastName()).isEqualToIgnoringCase("Person");
            Assertions.assertThat(data.getPostcode()).isEqualToIgnoringCase("CH1 2AN");
            Assertions.assertThat(data.getStatus()).isEqualToIgnoringCase("Excused");
            Assertions.assertThat(data.getDateExcused()).isEqualTo(dateExcused);
            Assertions.assertThat(data.getReason()).isEqualToIgnoringCase(reason);
            Assertions.assertThat(data.getDatePrinted()).isEqualTo(datePrinted);
            Assertions.assertThat(data.getPoolNumber()).isEqualToIgnoringCase(poolNumber);
        }
    }

    @Nested
    @DisplayName("getEligibleList - Excusal Refused")
    public class ExcusalRefused {
        @Test
        @DisplayName("getEligibleList - exclude printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListExcludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String reason = "Moved from area";
            LocalDate deferredTo = LocalDate.of(2024, 1, 29);
            boolean includePrinted = false;

            List<ExcusalRefusedLetterList> results =
                List.of(createExcusalRefusedLetterList(jurorNumber, deferredTo, reason, poolNumber, null));

            when(excusalRefusalLetterListRepository.findJurorsEligibleForExcusalRefusalLetter(Mockito.any(),
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.EXCUSAL_REFUSED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((NonDeferralLetterData) data.get(0), jurorNumber, deferredTo, reason, poolNumber, null);
        }

        @Test
        @DisplayName("getEligibleList - include printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListIncludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String reason = "Moved from area";
            LocalDate dateExcused = LocalDate.of(2024, 1, 29);
            LocalDateTime datePrinted = LocalDateTime.of(2024, 1, 22, 12, 35);
            boolean includePrinted = true;

            List<ExcusalRefusedLetterList> results =
                List.of(createExcusalRefusedLetterList(jurorNumber, dateExcused, reason, poolNumber,
                    datePrinted));

            when(excusalRefusalLetterListRepository.findJurorsEligibleForExcusalRefusalLetter(Mockito.any(),
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.EXCUSAL_REFUSED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((NonDeferralLetterData) data.get(0), jurorNumber, dateExcused, reason, poolNumber,
                datePrinted.toLocalDate());
        }

        @Test
        @DisplayName("getEligibleList - no data")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListNoData() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            boolean includePrinted = false;

            List<ExcusalRefusedLetterList> results = new ArrayList<>();

            when(excusalRefusalLetterListRepository.findJurorsEligibleForExcusalRefusalLetter(Mockito.any(),
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.EXCUSAL_REFUSED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            Assertions.assertThat(response.getData()).isEmpty();
        }


        private ExcusalRefusedLetterList createExcusalRefusedLetterList(String jurorNumber, LocalDate dateExcused,
                                                                        String reason, String poolNumber,
                                                                        LocalDateTime datePrinted) {
            return ExcusalRefusedLetterList.builder()
                .jurorNumber(jurorNumber)
                .firstName("Test")
                .lastName("Person")
                .postcode("CH1 2AN")
                .status("Excused")
                .dateExcused(dateExcused)
                .reason(reason)
                .poolNumber(poolNumber)
                .datePrinted(datePrinted)
                .build();
        }

        private void validateHeadings(List<String> headings) {
            Assertions.assertThat(headings.size()).isEqualTo(9);
            Assertions.assertThat(headings.get(0)).isEqualToIgnoringCase("Juror number");
            Assertions.assertThat(headings.get(1)).isEqualToIgnoringCase("First name");
            Assertions.assertThat(headings.get(2)).isEqualToIgnoringCase("Last name");
            Assertions.assertThat(headings.get(3)).isEqualToIgnoringCase("Postcode");
            Assertions.assertThat(headings.get(4)).isEqualToIgnoringCase("Status");
            Assertions.assertThat(headings.get(5)).isEqualToIgnoringCase("Date refused");
            Assertions.assertThat(headings.get(6)).isEqualToIgnoringCase("Reason");
            Assertions.assertThat(headings.get(7)).isEqualToIgnoringCase("Date printed");
            Assertions.assertThat(headings.get(8)).isEqualToIgnoringCase("Pool Number");
        }

        private void validateDataTypes(List<String> dataTypes) {
            Assertions.assertThat(dataTypes.size()).isEqualTo(9);
            Assertions.assertThat(dataTypes.get(0)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(1)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(2)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(3)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(4)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(5)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(6)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(7)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(8)).isEqualToIgnoringCase("hidden");
        }

        private void validateData(NonDeferralLetterData data, String jurorNumber, LocalDate dateRefused, String reason,
                                  String poolNumber, LocalDate datePrinted) {
            Assertions.assertThat(data.getJurorNumber()).isEqualToIgnoringCase(jurorNumber);
            Assertions.assertThat(data.getFirstName()).isEqualToIgnoringCase("Test");
            Assertions.assertThat(data.getLastName()).isEqualToIgnoringCase("Person");
            Assertions.assertThat(data.getPostcode()).isEqualToIgnoringCase("CH1 2AN");
            Assertions.assertThat(data.getStatus()).isEqualToIgnoringCase("Excused");
            Assertions.assertThat(data.getDateRefused()).isEqualTo(dateRefused);
            Assertions.assertThat(data.getReason()).isEqualToIgnoringCase(reason);
            Assertions.assertThat(data.getDatePrinted()).isEqualTo(datePrinted);
            Assertions.assertThat(data.getPoolNumber()).isEqualToIgnoringCase(poolNumber);
        }
    }

    @Nested
    @DisplayName("getEligibleList - Withdrawal")
    public class Withdrawal {

        @Test
        @DisplayName("getEligibleList - exclude printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListExcludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String code = "A";
            LocalDate dateDisqualified = LocalDate.of(2024, 1, 29);
            boolean includePrinted = false;

            List<WithdrawalLetterList> results =
                List.of(createWithdrawalLetterList(jurorNumber, dateDisqualified, code, poolNumber, null));

            when(withdrawalLetterListRepository.findJurorsEligibleForWithdrawalLetter(Mockito.any(),
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.WITHDRAWAL)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((WithdrawalLetterData) data.get(0), jurorNumber, dateDisqualified, "Age", poolNumber, null);
        }

        @Test
        @DisplayName("getEligibleList - include printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListIncludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String code = "A";
            LocalDate dateDisqualified = LocalDate.of(2024, 1, 29);
            LocalDateTime datePrinted = LocalDateTime.of(2024, 1, 22, 12, 35);
            boolean includePrinted = true;

            List<WithdrawalLetterList> results =
                List.of(createWithdrawalLetterList(jurorNumber, dateDisqualified, code, poolNumber, datePrinted));

            when(withdrawalLetterListRepository.findJurorsEligibleForWithdrawalLetter(Mockito.any(),
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.WITHDRAWAL)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((WithdrawalLetterData) data.get(0), jurorNumber, dateDisqualified, "Age", poolNumber,
                datePrinted.toLocalDate());
        }

        @Test
        @DisplayName("getEligibleList - no data")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListNoData() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            boolean includePrinted = false;

            List<WithdrawalLetterList> results = new ArrayList<>();

            when(withdrawalLetterListRepository.findJurorsEligibleForWithdrawalLetter(Mockito.any(),
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.WITHDRAWAL)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            Assertions.assertThat(response.getData()).isEmpty();
        }

        private WithdrawalLetterList createWithdrawalLetterList(String jurorNumber, LocalDate dateDisqualified,
                                                                String code, String poolNumber,
                                                                LocalDateTime datePrinted) {
            return WithdrawalLetterList.builder()
                .jurorNumber(jurorNumber)
                .firstName("Test")
                .lastName("Person")
                .postcode("CH1 2AN")
                .status("Disqualified")
                .dateDisqualified(dateDisqualified)
                .disqualifiedCode(code)
                .poolNumber(poolNumber)
                .datePrinted(datePrinted)
                .build();
        }

        private void validateHeadings(List<String> headings) {
            Assertions.assertThat(headings.size()).isEqualTo(9);
            Assertions.assertThat(headings.get(0)).isEqualToIgnoringCase("Juror number");
            Assertions.assertThat(headings.get(1)).isEqualToIgnoringCase("First name");
            Assertions.assertThat(headings.get(2)).isEqualToIgnoringCase("Last name");
            Assertions.assertThat(headings.get(3)).isEqualToIgnoringCase("Postcode");
            Assertions.assertThat(headings.get(4)).isEqualToIgnoringCase("Status");
            Assertions.assertThat(headings.get(5)).isEqualToIgnoringCase("Date disqualified");
            Assertions.assertThat(headings.get(6)).isEqualToIgnoringCase("Reason");
            Assertions.assertThat(headings.get(7)).isEqualToIgnoringCase("Date printed");
            Assertions.assertThat(headings.get(8)).isEqualToIgnoringCase("Pool Number");
        }

        private void validateDataTypes(List<String> dataTypes) {
            Assertions.assertThat(dataTypes.size()).isEqualTo(9);
            Assertions.assertThat(dataTypes.get(0)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(1)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(2)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(3)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(4)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(5)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(6)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(7)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(8)).isEqualToIgnoringCase("hidden");
        }

        private void validateData(WithdrawalLetterData data, String jurorNumber, LocalDate dateDisqualified,
                                  String reason, String poolNumber, LocalDate datePrinted) {
            Assertions.assertThat(data.getJurorNumber()).isEqualToIgnoringCase(jurorNumber);
            Assertions.assertThat(data.getFirstName()).isEqualToIgnoringCase("Test");
            Assertions.assertThat(data.getLastName()).isEqualToIgnoringCase("Person");
            Assertions.assertThat(data.getPostcode()).isEqualToIgnoringCase("CH1 2AN");
            Assertions.assertThat(data.getStatus()).isEqualToIgnoringCase("Disqualified");
            Assertions.assertThat(data.getDateDisqualified()).isEqualTo(dateDisqualified);
            Assertions.assertThat(data.getReason()).isEqualToIgnoringCase(reason);
            Assertions.assertThat(data.getDatePrinted()).isEqualTo(datePrinted);
            Assertions.assertThat(data.getPoolNumber()).isEqualToIgnoringCase(poolNumber);
        }

    }

    @Nested
    @DisplayName("getEligibleList - CertificateOfAttendance")
    class CertificateOfAttendance {

        @Test
        @DisplayName("getEligibleList - CertificateOfAttendance")
        void eligibleListExcludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            LocalDate startDate = LocalDate.of(2024, 1, 29);
            LocalDate completionDate = LocalDate.of(2024, 2, 7);
            boolean includePrinted = false;

            List<CertificateOfAttendanceLetterList> results =
                List.of(createCertificateOfAttendanceLetterList(jurorNumber, startDate, completionDate, poolNumber));

            when(certificateOfAttendanceListRepository.findJurorsEligibleForCertificateOfAcceptanceLetter(any(),
                any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.CERTIFICATE_OF_ATTENDANCE)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            assertHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            assertDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            assertData((CertificateOfAttendanceLetterData) data.get(0), jurorNumber,
                startDate, completionDate, poolNumber, null);

            CourtLetterSearchCriteria searchCriteria = new CourtLetterSearchCriteria(jurorNumber, null,
                null, null, false);

            verify(certificateOfAttendanceListRepository, times(1)).findJurorsEligibleForCertificateOfAcceptanceLetter(
                searchCriteria, owner);
        }

        @Test
        @DisplayName("getEligibleList - include printed")
        void eligibleListIncludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            LocalDate startDate = LocalDate.of(2024, 1, 29);
            LocalDate completionDate = LocalDate.of(2024, 2, 7);
            LocalDateTime datePrinted = LocalDateTime.of(2024, 1, 22, 12, 35);
            boolean includePrinted = true;

            List<CertificateOfAttendanceLetterList> results =
                List.of(createCertificateOfAttendanceLetterList(jurorNumber, startDate, completionDate, poolNumber,
                    datePrinted));

            when(certificateOfAttendanceListRepository.findJurorsEligibleForCertificateOfAcceptanceLetter(Mockito.any(),
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.CERTIFICATE_OF_ATTENDANCE)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            assertHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            assertDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            assertData((CertificateOfAttendanceLetterData) data.get(0), jurorNumber,
                startDate, completionDate, poolNumber, datePrinted.toLocalDate());

            CourtLetterSearchCriteria searchCriteria = new CourtLetterSearchCriteria(jurorNumber, null,
                null, null, true);

            verify(certificateOfAttendanceListRepository, times(1)).findJurorsEligibleForCertificateOfAcceptanceLetter(
                searchCriteria, owner);
        }

        @Test
        @DisplayName("getEligibleList - no data")
        void eligibleListNoData() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            boolean includePrinted = false;

            List<CertificateOfAttendanceLetterList> results = new ArrayList<>();

            when(certificateOfAttendanceListRepository.findJurorsEligibleForCertificateOfAcceptanceLetter(Mockito.any(),
                Mockito.any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.CERTIFICATE_OF_ATTENDANCE)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            assertHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            assertDataTypes(dataTypes);

            Assertions.assertThat(response.getData()).isEmpty();

            CourtLetterSearchCriteria searchCriteria = new CourtLetterSearchCriteria(jurorNumber, null,
                null, null, false);

            verify(certificateOfAttendanceListRepository, times(1)).findJurorsEligibleForCertificateOfAcceptanceLetter(
                searchCriteria, owner);
        }

        private CertificateOfAttendanceLetterList createCertificateOfAttendanceLetterList(String jurorNumber,
                                                                                          LocalDate startDate,
                                                                                          LocalDate completionDate,
                                                                                          String poolNumber) {
            return createCertificateOfAttendanceLetterList(jurorNumber, startDate, completionDate, poolNumber, null);
        }

        private CertificateOfAttendanceLetterList createCertificateOfAttendanceLetterList(String jurorNumber,
                                                                                          LocalDate startDate,
                                                                                          LocalDate completionDate,
                                                                                          String poolNumber,
                                                                                          LocalDateTime datePrinted) {
            return CertificateOfAttendanceLetterList.builder()
                .jurorNumber(jurorNumber)
                .firstName("Test")
                .lastName("Person")
                .startDate(startDate)
                .completionDate(completionDate)
                .poolNumber(poolNumber)
                .datePrinted(datePrinted)
                .build();
        }

        private void assertHeadings(List<String> headings) {
            Assertions.assertThat(headings.size()).isEqualTo(7);
            Assertions.assertThat(headings.get(0)).isEqualToIgnoringCase("Juror number");
            Assertions.assertThat(headings.get(1)).isEqualToIgnoringCase("First name");
            Assertions.assertThat(headings.get(2)).isEqualToIgnoringCase("Last name");
            Assertions.assertThat(headings.get(3)).isEqualToIgnoringCase("Pool Number");
            Assertions.assertThat(headings.get(4)).isEqualToIgnoringCase("Start Date");
            Assertions.assertThat(headings.get(5)).isEqualToIgnoringCase("Completion Date");
            Assertions.assertThat(headings.get(6)).isEqualToIgnoringCase("Date printed");

        }

        private void assertDataTypes(List<String> dataTypes) {
            Assertions.assertThat(dataTypes.size()).isEqualTo(7);
            Assertions.assertThat(dataTypes.get(0)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(1)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(2)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(3)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(4)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(5)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(6)).isEqualToIgnoringCase("date");
        }

        private void assertData(CertificateOfAttendanceLetterData data, String jurorNumber, LocalDate startDate,
                                LocalDate completionDate,
                                String poolNumber, LocalDate datePrinted) {
            Assertions.assertThat(data.getJurorNumber()).isEqualToIgnoringCase(jurorNumber);
            Assertions.assertThat(data.getFirstName()).isEqualToIgnoringCase("Test");
            Assertions.assertThat(data.getLastName()).isEqualToIgnoringCase("Person");
            Assertions.assertThat(data.getStartDate()).isEqualTo(startDate);
            Assertions.assertThat(data.getCompletionDate()).isEqualTo(completionDate);
            Assertions.assertThat(data.getDatePrinted()).isEqualTo(datePrinted);
            Assertions.assertThat(data.getPoolNumber()).isEqualToIgnoringCase(poolNumber);
        }
    }


}
