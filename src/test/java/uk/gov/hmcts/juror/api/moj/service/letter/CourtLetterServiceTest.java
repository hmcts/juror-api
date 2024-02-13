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
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.DeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterResponseData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.NonDeferralLetterData;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralDeniedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.DeferralDeniedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.DeferralGrantedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CourtLetterServiceTest {

    private DeferralGrantedLetterListRepository deferralGrantedLetterListRepository;
    private DeferralDeniedLetterListRepository deferralDeniedLetterListRepository;
    private ExcusalCodeRepository excusalCodeRepository;
    private CourtLetterServiceImpl courtLetterService;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        this.deferralGrantedLetterListRepository = mock(DeferralGrantedLetterListRepository.class);
        this.deferralDeniedLetterListRepository = mock(DeferralDeniedLetterListRepository.class);
        this.excusalCodeRepository = mock(ExcusalCodeRepository.class);

        this.courtLetterService = new CourtLetterServiceImpl(deferralGrantedLetterListRepository,
            deferralDeniedLetterListRepository, excusalCodeRepository);

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

            when(deferralGrantedLetterListRepository.findJurorsEligibleForDeferralGrantedLetter(Mockito.any(),
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

            when(deferralGrantedLetterListRepository.findJurorsEligibleForDeferralGrantedLetter(Mockito.any(),
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

            when(deferralGrantedLetterListRepository.findJurorsEligibleForDeferralGrantedLetter(Mockito.any(),
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
            ExcusalCodeEntity excusalCode = new ExcusalCodeEntity();
            excusalCode.setDescription(reasonDesc);
            excusalCode.setExcusalCode(reasonCode);

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
            ExcusalCodeEntity excusalCode = new ExcusalCodeEntity();
            excusalCode.setDescription(reasonDesc);
            excusalCode.setExcusalCode(reasonCode);

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
            return createDeferralDeniedLetterList(jurorNumber, status, dateRefused, reason, poolNumber, null);
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


}
