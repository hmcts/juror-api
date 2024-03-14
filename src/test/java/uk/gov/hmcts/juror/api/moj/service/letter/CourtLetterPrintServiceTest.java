package uk.gov.hmcts.juror.api.moj.service.letter;

import com.querydsl.core.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.QWelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CertificateOfExemptionRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CertificateOfExemptionRequestDto.CertificateOfExemptionRequestDtoBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.system.SystemParameterMod;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.SystemParameterRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.letter.CourtPrintLetterRepository;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtLetterPrintServiceImpl;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CyclomaticComplexity", "PMD.TooManyMethods"})
public class CourtLetterPrintServiceTest {

    private JurorRepository jurorRepository;
    private CourtLetterPrintServiceImpl courtLetterPrintServiceImpl;
    private JurorHistoryRepository jurorHistoryRepository;
    private WelshCourtLocationRepository welshCourtLocationRepository;
    private SystemParameterRepositoryMod systemParameterRepositoryMod;
    private CourtPrintLetterRepository courtPrintLetterRepository;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    private static final QWelshCourtLocation WELSH_COURT_LOCATION = QWelshCourtLocation.welshCourtLocation;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QAppearance APPEARANCE = QAppearance.appearance;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QTrial TRIAL = QTrial.trial;

    private static final int ENGLISH_URL_PARAM = 102;
    private static final int WELSH_URL_PARAM = 103;

    @BeforeEach
    void beforeEach() {
        this.jurorRepository = mock(JurorRepository.class);
        this.welshCourtLocationRepository = mock(WelshCourtLocationRepository.class);
        this.systemParameterRepositoryMod = mock(SystemParameterRepositoryMod.class);
        this.jurorHistoryRepository = mock(JurorHistoryRepository.class);
        this.courtPrintLetterRepository = mock(CourtPrintLetterRepository.class);
        this.courtLetterPrintServiceImpl = new CourtLetterPrintServiceImpl(
            systemParameterRepositoryMod, jurorRepository, welshCourtLocationRepository, jurorHistoryRepository,
            courtPrintLetterRepository);

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
    @DisplayName("reissue letter - Deferral Granted")
    class DeferralGranted {
        @Test
        @DisplayName("Reissue deferral granted letter happy - English")
        void reissueDeferralGrantedLetterEnglishHappy() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createDeferralDataEnglish(LocalDate.of(2024, 1, 1), "435")).when(
                    courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_GRANTED, false, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.DEFERRAL_GRANTED), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "The Crown Court\n"
                        + "at COURT NAME")
                    .isEqualTo("The Crown Court\n"
                        + "at COURT NAME");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'ADDRESS 1'")
                    .isEqualTo("ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'ADDRESS 2'")
                    .isEqualTo("ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'ADDRESS 3'")
                    .isEqualTo("ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'ADDRESS 4'")
                    .isEqualTo("ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'ADDRESS 5'")
                    .isEqualTo("ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'ADDRESS 6'")
                    .isEqualTo("ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");

                assertThat(dto.getUrl())
                    .as("Expect URL to be ENGLISH_URL")
                    .isEqualTo("ENGLISH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nAn Officer of the Crown Court");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getDeferredToDate())
                    .as("Expect Date to be 01 January 2024")
                    .isEqualTo("01 January 2024");
                assertThat(dto.getAttendTime())
                    .as("Expect attend time to be 09:00")
                    .isEqualTo(LocalTime.of(9, 0));
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be false")
                    .isFalse();
            }

            verify(systemParameterRepositoryMod, times(1)).findById(anyInt());
            verify(jurorRepository, times(1)).findByJurorNumber(anyString());
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(anyString(), any(), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("Reissue deferral granted letter happy - Welsh")
        void reissueDeferralGrantedLetterWelshHappy() {
            final String jurorNumber = "111111112";
            final String courtOwner = "457";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber, true, courtOwner));
            WelshCourtLocation welshCourtLocation = WelshCourtLocation.builder().locCode(courtOwner).build();
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.of(welshCourtLocation));
            doReturn(createSystemParam(WELSH_URL_PARAM, "WELSH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());

            doReturn(createDeferralDataWelsh(LocalDate.of(2024, 1, 1), jurorNumber, "ABERTAWE"))
                .when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_GRANTED, true, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.DEFERRAL_GRANTED), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName()).as("Expect court name to be Llys y Goron\nynAbertawe")
                    .isEqualTo("Llys y Goron\nynAbertawe");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'WELSH ADDRESS 1'")
                    .isEqualTo("WELSH ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'WELSH ADDRESS 2'")
                    .isEqualTo("WELSH ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'WELSH ADDRESS 3'")
                    .isEqualTo("WELSH ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'WELSH ADDRESS 4'")
                    .isEqualTo("WELSH ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'WELSH ADDRESS 5'")
                    .isEqualTo("WELSH ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'WELSH ADDRESS 6'")
                    .isEqualTo("WELSH ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nSwyddog Llys");

                assertThat(dto.getUrl())
                    .as("Expect URL to be WELSH_URL")
                    .isEqualTo("WELSH_URL");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getDeferredToDate())
                    .as("Expect date to be 01 Ionawr 2024")
                    .isEqualTo("01 Ionawr 2024");
                assertThat(dto.getAttendTime())
                    .as("Expect attend time to be 09:30")
                    .isEqualTo(LocalTime.of(9, 30));
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be true")
                    .isTrue();
            }

            verify(systemParameterRepositoryMod, times(1)).findById(anyInt());
            verify(jurorRepository, times(1)).findByJurorNumber(anyString());
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(anyString(), any(), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("Reissue deferral granted letter - Duplicate request English")
        void reissueDeferralGrantedLetterDuplicateRequest() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createDeferralDataEnglish(LocalDate.of(2024, 1, 1), "435"))
                .when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_GRANTED, false, courtOwner);

            //requested three letters for juror number - 111111111
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(jurorNumber);
            jurorNumbers.add(jurorNumber);
            jurorNumbers.add(jurorNumber);
            PrintLettersRequestDto requestDto = createPrintLetterRequest(jurorNumbers,
                CourtLetterType.DEFERRAL_GRANTED);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(requestDto, "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "The Crown Court\n"
                        + "at COURT NAME")
                    .isEqualTo("The Crown Court\n"
                        + "at COURT NAME");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'ADDRESS 1'")
                    .isEqualTo("ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'ADDRESS 2'")
                    .isEqualTo("ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'ADDRESS 3'")
                    .isEqualTo("ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'ADDRESS 4'")
                    .isEqualTo("ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'ADDRESS 5'")
                    .isEqualTo("ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'ADDRESS 6'")
                    .isEqualTo("ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");

                assertThat(dto.getUrl())
                    .as("Expect URL to be ENGLISH_URL")
                    .isEqualTo("ENGLISH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nAn Officer of the Crown Court");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getDeferredToDate())
                    .as("Expect Date to be 01 January 2024")
                    .isEqualTo("01 January 2024");
                assertThat(dto.getAttendTime())
                    .as("Expect attend time to be 09:00")
                    .isEqualTo(LocalTime.of(9, 0));
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be false")
                    .isFalse();
            }

            verify(systemParameterRepositoryMod, times(1)).findById(anyInt());
            verify(jurorRepository, times(1)).findByJurorNumber(anyString());
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(anyString(), any(), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("Reissue deferral granted letter - Date formatting Welsh")
        @SuppressWarnings("checkstyle:MissingSwitchDefaultCheck")
        void reissueDeferralGrantedLetterFormatDateWelsh() {
            final String courtOwner = "457";
            mockCurrentUser(courtOwner);

            List<String> jurorNumbers = new ArrayList<>();
            final int totalMonths = 12;
            for (int i = 0;
                 i < totalMonths;
                 i++) {
                String jurorNumber = String.format("1111111%02d", i + 1);
                when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                    true, courtOwner));
                doReturn(createDeferralDataWelsh(LocalDate.of(2024, i + 1, 1), jurorNumber, "ABERTAWE")).when(
                        courtPrintLetterRepository)
                    .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_GRANTED, true, courtOwner);
                jurorNumbers.add(jurorNumber);
            }

            WelshCourtLocation welshCourtLocation = WelshCourtLocation.builder().locCode(courtOwner).build();
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.of(welshCourtLocation));
            doReturn(createSystemParam(WELSH_URL_PARAM, "WELSH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());


            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(jurorNumbers, CourtLetterType.DEFERRAL_GRANTED),
                    "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 12").isEqualTo(12);

            for (int i = 0;
                 i < 12;
                 i++) {
                PrintLetterDataResponseDto dto = response.get(i);
                switch (LocalDate.of(2024, i + 1, 1).getMonth()) {
                    case JANUARY -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Ionawr")
                        .contains("Ionawr");
                    case FEBRUARY -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Chwefror")
                        .contains("Chwefror");
                    case MARCH -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Mawrth")
                        .contains("Mawrth");
                    case APRIL -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Ebrill")
                        .contains("Ebrill");
                    case MAY -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Mai")
                        .contains("Mai");
                    case JUNE -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Mehefin")
                        .contains("Mehefin");
                    case JULY -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Gorffenaf")
                        .contains("Gorffenaf");
                    case AUGUST -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Awst")
                        .contains("Awst");
                    case SEPTEMBER -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Medi")
                        .contains("Medi");
                    case OCTOBER -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Hydref")
                        .contains("Hydref");
                    case NOVEMBER -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Tachwedd")
                        .contains("Tachwedd");
                    case DECEMBER -> assertThat(dto.getDeferredToDate())
                        .as("Expect date to contain Rhagfyr")
                        .contains("Rhagfyr");
                }
            }
        }

        @Test
        @DisplayName("Reissue deferral granted letter - Court name formatting - English - Crown Court")
        void reissueDeferralGrantedLetterFormatCourtNameEnglishCrownCourt() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createDeferralDataEnglish(LocalDate.of(2024, 1, 1), "435")).when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_GRANTED, false, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.DEFERRAL_GRANTED), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            assertThat(response.get(0).getCourtName())
                .as("Expect court name to be The Royal Court\n of Justice")
                .isEqualTo("The Crown Court\nat " + "COURT NAME");
        }

        @Test
        @DisplayName("Reissue deferral granted letter - Court name formatting - English - Royal court")
        void reissueDeferralGrantedLetterFormatCourtNameEnglishRoyalCourt() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createDeferralDataEnglish(LocalDate.of(2024, 1, 1), "626")).when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_GRANTED, false, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.DEFERRAL_GRANTED), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            assertThat(response.get(0).getCourtName())
                .as("Expect court name to be The Royal Court\nof Justice")
                .isEqualTo("The Royal Court\nof Justice");
        }

        @Test
        @DisplayName("Reissue deferral granted letter - Court name formatting - Welsh")
        void reissueDeferralGrantedLetterFormatCourtNameWelsh() {
            final String courtOwner = "457";
            mockCurrentUser(courtOwner);

            List<String> jurorNumbers = new ArrayList<>();
            List<String> courtNames = new ArrayList<>();

            courtNames.add("BOURT NAME");
            courtNames.add("MOURT NAME");
            courtNames.add("COURT NAME");
            courtNames.add("DOURT NAME");
            courtNames.add("GOURT NAME");
            courtNames.add("POURT NAME");
            courtNames.add("TOURT NAME");
            courtNames.add("OOURT NAME");

            for (int i = 0;
                 i < courtNames.size();
                 i++) {
                String jurorNumber = String.format("1111111%02d", i + 1);

                when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                    true, courtOwner));
                doReturn(createDeferralDataWelsh(
                    LocalDate.of(2024, 1, 1), jurorNumber, courtNames.get(i)))
                    .when(courtPrintLetterRepository)
                    .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_GRANTED, true, courtOwner);

                jurorNumbers.add(jurorNumber);
            }

            WelshCourtLocation welshCourtLocation = WelshCourtLocation.builder().locCode(courtOwner).build();
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.of(welshCourtLocation));
            doReturn(createSystemParam(WELSH_URL_PARAM, "WELSH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(jurorNumbers, CourtLetterType.DEFERRAL_GRANTED),
                    "TEST_COURTUSER");

            assertThat(response.size()).as(
                "Expect size to be " + courtNames.size()).isEqualTo(courtNames.size());

            for (int i = 0;
                 i < courtNames.size();
                 i++) {
                final String formattedCourtName = response.get(i).getCourtName();
                switch (courtNames.get(i).toLowerCase().charAt(0)) {
                    case 'b', 'm' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nym Mourt name")
                        .isEqualTo("Llys y Goron\nym Mourt name");
                    case 'c' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nyng Nghourt name")
                        .isEqualTo("Llys y Goron\nyng Nghourt name");
                    case 'd' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nyn Nourt name")
                        .isEqualTo("Llys y Goron\nyn Nourt name");
                    case 'g' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nyng Ngourt name")
                        .isEqualTo("Llys y Goron\nyng Ngourt name");
                    case 'p' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nym Mhourt name")
                        .isEqualTo("Llys y Goron\nym Mhourt name");
                    case 't' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nyn Nhourt name")
                        .isEqualTo("Llys y Goron\nyn Nhourt name");
                    default -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nynOourt name")
                        .isEqualTo("Llys y Goron\nynOourt name");
                }
            }
        }
    }

    @Nested
    @DisplayName("(Re)-Issue Letter - Deferral Refused")
    class DeferralRefused {
        @Test
        @DisplayName("Reissue deferral refused letter happy - English")
        void deferralRefusedLetterEnglishHappy() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createPrintLetterTupleEnglish(courtOwner))
                .when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_REFUSED, false, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.DEFERRAL_REFUSED), "TEST_USER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "The Crown Court\n"
                        + "at COURT NAME")
                    .isEqualTo("The Crown Court\n"
                        + "at COURT NAME");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'ADDRESS 1'")
                    .isEqualTo("ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'ADDRESS 2'")
                    .isEqualTo("ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'ADDRESS 3'")
                    .isEqualTo("ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'ADDRESS 4'")
                    .isEqualTo("ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'ADDRESS 5'")
                    .isEqualTo("ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'ADDRESS 6'")
                    .isEqualTo("ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");

                assertThat(dto.getUrl())
                    .as("Expect URL to be ENGLISH_URL")
                    .isEqualTo("ENGLISH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nAn Officer of the Crown Court");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getAttendTime())
                    .as("Expect attendance time to be 09:30")
                    .isEqualTo(LocalTime.of(9, 0));
            }

            verify(systemParameterRepositoryMod, times(1)).findById(ENGLISH_URL_PARAM);
            verify(jurorRepository, times(1)).findByJurorNumber(jurorNumber);
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(anyString(), any(), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("Reissue deferral refused letter happy - Welsh")
        void reissueDeferralRefusedLetterWelshHappy() {
            final String jurorNumber = "111111112";
            final String courtOwner = "457";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber, true, courtOwner));
            WelshCourtLocation welshCourtLocation = WelshCourtLocation.builder().locCode(courtOwner).build();
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.of(welshCourtLocation));
            doReturn(createSystemParam(WELSH_URL_PARAM, "WELSH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());

            doReturn(createPrintLetterTupleWelsh(jurorNumber, "ABERTAWE"))
                .when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_REFUSED, true, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.DEFERRAL_REFUSED), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName()).as("Expect court name to be Llys y Goron\nynAbertawe")
                    .isEqualTo("Llys y Goron\nynAbertawe");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'WELSH ADDRESS 1'")
                    .isEqualTo("WELSH ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'WELSH ADDRESS 2'")
                    .isEqualTo("WELSH ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'WELSH ADDRESS 3'")
                    .isEqualTo("WELSH ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'WELSH ADDRESS 4'")
                    .isEqualTo("WELSH ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'WELSH ADDRESS 5'")
                    .isEqualTo("WELSH ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'WELSH ADDRESS 6'")
                    .isEqualTo("WELSH ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nSwyddog Llys");

                assertThat(dto.getUrl())
                    .as("Expect URL to be WELSH_URL")
                    .isEqualTo("WELSH_URL");


                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getAttendTime())
                    .as("Expect attendance time to be 09:30")
                    .isEqualTo(LocalTime.of(9, 30));
            }

            verify(systemParameterRepositoryMod, times(1)).findById(anyInt());
            verify(jurorRepository, times(1)).findByJurorNumber(anyString());
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(anyString(), any(), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("Reissue deferral refused letter - Duplicate request English")
        void reissueDeferralRefusedLetterDuplicateRequest() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createPrintLetterTupleEnglish(courtOwner))
                .when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.DEFERRAL_REFUSED, false, courtOwner);

            //requested three letters for juror number - 111111111
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(jurorNumber);
            jurorNumbers.add(jurorNumber);
            jurorNumbers.add(jurorNumber);
            PrintLettersRequestDto requestDto = createPrintLetterRequest(jurorNumbers,
                CourtLetterType.DEFERRAL_REFUSED);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(requestDto, "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "The Crown Court\n"
                        + "at COURT NAME")
                    .isEqualTo("The Crown Court\n"
                        + "at COURT NAME");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'ADDRESS 1'")
                    .isEqualTo("ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'ADDRESS 2'")
                    .isEqualTo("ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'ADDRESS 3'")
                    .isEqualTo("ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'ADDRESS 4'")
                    .isEqualTo("ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'ADDRESS 5'")
                    .isEqualTo("ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'ADDRESS 6'")
                    .isEqualTo("ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");

                assertThat(dto.getUrl())
                    .as("Expect URL to be ENGLISH_URL")
                    .isEqualTo("ENGLISH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nAn Officer of the Crown Court");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getAttendTime())
                    .as("Expect attendance time to be 09:30")
                    .isEqualTo(LocalTime.of(9, 0));
            }

            verify(systemParameterRepositoryMod, times(1)).findById(anyInt());
            verify(jurorRepository, times(1)).findByJurorNumber(anyString());
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(anyString(), any(), anyBoolean(), anyString());
        }
    }

    @Nested
    @DisplayName("(Re)-Issue Letter - Excusal Refused")
    class ExcusalRefused {
        @Test
        @DisplayName("Reissue excused refused letter happy - English")
        void reissueExcusedRefusedLetterHappyEnglish() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createExcusalRefusedDataEnglish(LocalDate.of(2024, 1, 1), "415")).when(
                    courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.EXCUSAL_REFUSED, false, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.EXCUSAL_REFUSED), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "The Crown Court\n"
                        + "at COURT NAME")
                    .isEqualTo("The Crown Court\n"
                        + "at COURT NAME");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'ADDRESS 1'")
                    .isEqualTo("ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'ADDRESS 2'")
                    .isEqualTo("ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'ADDRESS 3'")
                    .isEqualTo("ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'ADDRESS 4'")
                    .isEqualTo("ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'ADDRESS 5'")
                    .isEqualTo("ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'ADDRESS 6'")
                    .isEqualTo("ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");

                assertThat(dto.getUrl())
                    .as("Expect URL to be ENGLISH_URL")
                    .isEqualTo("ENGLISH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nAn Officer of the Crown Court");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be false")
                    .isFalse();
                assertThat(dto.getCourtManager()).as("Expect court manager to be: The Court Manager").isEqualTo("The "
                    + "Court Manager");
            }

            verify(systemParameterRepositoryMod, times(1)).findById(anyInt());
            verify(jurorRepository, times(1)).findByJurorNumber(anyString());
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(anyString(), any(), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("Reissue excused refused letter happy - Welsh")
        void reissueExcusedRefusedLetterHappyWelsh() {
            final String jurorNumber = "999999999";
            final String courtOwner = "457";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber, true, courtOwner));
            WelshCourtLocation welshCourtLocation = WelshCourtLocation.builder().locCode(courtOwner).build();
            when(welshCourtLocationRepository.findById(anyString()))
                .thenReturn(Optional.of(welshCourtLocation));
            doReturn(createSystemParam(WELSH_URL_PARAM, "WELSH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createExcusalRefusedDataWelsh(LocalDate.of(2024, 1, 1))).when(
                    courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.EXCUSAL_REFUSED, true, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.EXCUSAL_REFUSED), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName()).as("Expect court name to be Llys y Goron\nynAbertawe")
                    .isEqualTo("Llys y Goron\nynAbertawe");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'WELSH ADDRESS 1'")
                    .isEqualTo("WELSH ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'WELSH ADDRESS 2'")
                    .isEqualTo("WELSH ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'WELSH ADDRESS 3'")
                    .isEqualTo("WELSH ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'WELSH ADDRESS 4'")
                    .isEqualTo("WELSH ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'WELSH ADDRESS 5'")
                    .isEqualTo("WELSH ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'WELSH ADDRESS 6'")
                    .isEqualTo("WELSH ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nSwyddog Llys");

                assertThat(dto.getUrl())
                    .as("Expect URL to be WELSH_URL")
                    .isEqualTo("WELSH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nSwyddog Llys");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be true")
                    .isTrue();
                assertThat(dto.getCourtManager())
                    .as("Expect court manager to be Y Rheolwr Llys")
                    .isEqualTo("Y Rheolwr Llys");
            }

            verify(systemParameterRepositoryMod, times(1)).findById(anyInt());
            verify(jurorRepository, times(1)).findByJurorNumber(anyString());
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(anyString(), any(), anyBoolean(), anyString());
        }
    }

    @Nested
    @DisplayName("Certificate of exemption")
    class CertificateOfExemption {
        @Test
        @DisplayName("Issue certificate of exemption - English - happy")
        void issueCertificateOfExemptionEnglish() {
            final String jurorNumber = "111111111";
            final String courtOwner = "435";
            final ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);


            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(courtOwner)).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(ENGLISH_URL_PARAM);
            doReturn(createCertificateOfExemptionData(false)).when(
                    courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_EXEMPTION, false, courtOwner,
                    "TEST TRIAL");

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createCertificateOfExemptionRequest(Collections.singletonList(jurorNumber), null),
                    "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "The Crown Court\n"
                        + "at COURT NAME")
                    .isEqualTo("The Crown Court\n"
                        + "at COURT NAME");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'ADDRESS 1'")
                    .isEqualTo("ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'ADDRESS 2'")
                    .isEqualTo("ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'ADDRESS 3'")
                    .isEqualTo("ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'ADDRESS 4'")
                    .isEqualTo("ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'ADDRESS 5'")
                    .isEqualTo("ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'ADDRESS 6'")
                    .isEqualTo("ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");

                assertThat(dto.getUrl())
                    .as("Expect URL to be ENGLISH_URL")
                    .isEqualTo("ENGLISH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nAn Officer of the Crown Court");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be false")
                    .isFalse();
                assertThat(dto.getJudgeName())
                    .as("Expect judge name to be SIR DREDD")
                    .isEqualTo("SIR DREDD");
                assertThat(dto.getPeriodOfExemption())
                    .as("Expect the period of exemption to be indefinite")
                    .isEqualTo("indefinite");
                assertThat(dto.getDefendant())
                    .as("Expect the defendant to be TEST DEFENDANT")
                    .isEqualTo("TEST DEFENDANT");
            }

            verify(systemParameterRepositoryMod, times(1)).findById(ENGLISH_URL_PARAM);
            verify(jurorRepository, times(1)).findByJurorNumber(jurorNumber);
            verify(welshCourtLocationRepository, times(1)).findById(courtOwner);
            verify(jurorHistoryRepository, times(1)).save(jurorHistoryArgumentCaptor.capture());

            JurorHistory history = jurorHistoryArgumentCaptor.getValue();

            assertThat(history.getHistoryCode())
                .as("History Code")
                .isEqualTo(HistoryCodeMod.CERTIFICATE_OF_EXEMPTION);
            assertThat(history.getJurorNumber())
                .as("Juror number")
                .isEqualTo(jurorNumber);
            assertThat(history.getCreatedBy())
                .as("Current logged in user")
                .isEqualTo("TEST_COURTUSER");
            assertThat(
                history.getDateCreated())//NOPMD - suppressed JUnitAssertionsShouldIncludeMessage - false positive see https://github.com/pmd/pmd/issues/1565
                .as("Date created")
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(history.getOtherInformation())
                .as("Other information")
                .isEqualTo("Print Certificate of Exemption");
            assertThat(history.getOtherInformationDate())
                .as("Other information date")
                .isEqualTo(LocalDate.now());
            assertThat(history.getOtherInformationRef())
                .as("Other information Ref")
                .isEqualTo("indefinite");
            assertThat(history.getPoolNumber())
                .as("Pool number")
                .isEqualTo("222222222");

            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_EXEMPTION, false, courtOwner,
                    "TEST TRIAL");
        }

        @Test
        @DisplayName("Issue certificate of exemption- override judge's name - English")
        void issueCertificateOfExemptionEnglishOverrideJudgeName() {
            final String jurorNumber = "111111111";
            final String courtOwner = "435";
            final ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber,
                false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createCertificateOfExemptionData(false)).when(
                    courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_EXEMPTION, false, courtOwner,
                    "TEST TRIAL");

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createCertificateOfExemptionRequest(Collections.singletonList(jurorNumber),
                        "MR DREDD"),
                    "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName())
                    .as("Expect court name to be " + "The Crown Court\n"
                        + "at COURT NAME")
                    .isEqualTo("The Crown Court\n"
                        + "at COURT NAME");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'ADDRESS 1'")
                    .isEqualTo("ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'ADDRESS 2'")
                    .isEqualTo("ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'ADDRESS 3'")
                    .isEqualTo("ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'ADDRESS 4'")
                    .isEqualTo("ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'ADDRESS 5'")
                    .isEqualTo("ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'ADDRESS 6'")
                    .isEqualTo("ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");

                assertThat(dto.getUrl())
                    .as("Expect URL to be ENGLISH_URL")
                    .isEqualTo("ENGLISH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nAn Officer of the Crown Court");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be false")
                    .isFalse();
                assertThat(dto.getJudgeName())
                    .as("Expect judge name to be SIR DREDD")
                    .isEqualTo("MR DREDD");
                assertThat(dto.getPeriodOfExemption())
                    .as("Expect the period of exemption to be indefinite")
                    .isEqualTo("indefinite");
                assertThat(dto.getDefendant())
                    .as("Expect the defendant to be TEST DEFENDANT")
                    .isEqualTo("TEST DEFENDANT");
            }

            verify(systemParameterRepositoryMod, times(1)).findById(ENGLISH_URL_PARAM);
            verify(jurorRepository, times(1)).findByJurorNumber(jurorNumber);
            verify(welshCourtLocationRepository, times(1)).findById(courtOwner);
            verify(jurorHistoryRepository, times(1)).save(jurorHistoryArgumentCaptor.capture());
            JurorHistory history = jurorHistoryArgumentCaptor.getValue();

            assertThat(history.getHistoryCode())
                .as("History Code")
                .isEqualTo(HistoryCodeMod.CERTIFICATE_OF_EXEMPTION);
            assertThat(history.getJurorNumber())
                .as("Juror number")
                .isEqualTo(jurorNumber);
            assertThat(history.getCreatedBy())
                .as("Username")
                .isEqualTo("TEST_COURTUSER");
            assertThat(
                history.getDateCreated())//NOPMD - suppressed JUnitAssertionsShouldIncludeMessage - false positive see https://github.com/pmd/pmd/issues/1565
                .as("Created date")
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(history.getOtherInformation())
                .as("Other information")
                .isEqualTo("Print Certificate of Exemption");
            assertThat(history.getOtherInformationDate())
                .as("Other information date")
                .isEqualTo(LocalDate.now());
            assertThat(history.getOtherInformationRef())
                .as("Other information Ref")
                .isEqualTo("indefinite");
            assertThat(history.getPoolNumber())
                .as("Pool number")
                .isEqualTo("222222222");

            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_EXEMPTION, false, courtOwner,
                    "TEST TRIAL");
        }

        @Test
        @DisplayName("Issue certificate of exemption - welsh - happy")
        void issueCertificateOfExemptionWelsh() {
            final String jurorNumber = "999999999";
            final String courtOwner = "457";
            final ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(createJuror(jurorNumber, true, courtOwner));
            WelshCourtLocation welshCourtLocation = WelshCourtLocation.builder().locCode(courtOwner).build();
            when(welshCourtLocationRepository.findById(courtOwner)).thenReturn(Optional.of(welshCourtLocation));
            doReturn(createSystemParam(WELSH_URL_PARAM, "WELSH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());
            doReturn(createCertificateOfExemptionData(true)).when(
                    courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_EXEMPTION, true, courtOwner,
                    "TEST TRIAL");

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createCertificateOfExemptionRequest(Collections.singletonList(jurorNumber), null),
                    "TEST_COURT");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName()).as("Expect court name to be Llys y Goron\nynAbertawe")
                    .isEqualTo("Llys y Goron\nynAbertawe");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'WELSH ADDRESS 1'")
                    .isEqualTo("WELSH ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'WELSH ADDRESS 2'")
                    .isEqualTo("WELSH ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'WELSH ADDRESS 3'")
                    .isEqualTo("WELSH ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'WELSH ADDRESS 4'")
                    .isEqualTo("WELSH ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'WELSH ADDRESS 5'")
                    .isEqualTo("WELSH ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'WELSH ADDRESS 6'")
                    .isEqualTo("WELSH ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nSwyddog Llys");

                assertThat(dto.getUrl())
                    .as("Expect URL to be WELSH_URL")
                    .isEqualTo("WELSH_URL");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nSwyddog Llys");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be true")
                    .isTrue();
                assertThat(dto.getJudgeName())
                    .as("Expect judge name to be SIR DREDD")
                    .isEqualTo("SIR DREDD");
                assertThat(dto.getPeriodOfExemption())
                    .as("Expect the period of exemption to be indefinite")
                    .isEqualTo("indefinite");
                assertThat(dto.getDefendant())
                    .as("Expect the defendant to be TEST DEFENDANT")
                    .isEqualTo("TEST DEFENDANT");
            }

            verify(systemParameterRepositoryMod, times(1)).findById(WELSH_URL_PARAM);
            verify(jurorRepository, times(1)).findByJurorNumber(jurorNumber);
            verify(welshCourtLocationRepository, times(1)).findById(courtOwner);
            verify(jurorHistoryRepository, times(1)).save(jurorHistoryArgumentCaptor.capture());
            JurorHistory history = jurorHistoryArgumentCaptor.getValue();

            assertThat(history.getHistoryCode())
                .as("History Code")
                .isEqualTo(HistoryCodeMod.CERTIFICATE_OF_EXEMPTION);
            assertThat(history.getJurorNumber())
                .as("Juror number")
                .isEqualTo(jurorNumber);
            assertThat(history.getCreatedBy())
                .as("Username")
                .isEqualTo("TEST_COURT");
            assertThat(
                history.getDateCreated())//NOPMD - suppressed JUnitAssertionsShouldIncludeMessage - false positive see https://github.com/pmd/pmd/issues/1565
                .as("Created date")
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(history.getOtherInformation())
                .as("Other information")
                .isEqualTo("Print Certificate of Exemption");
            assertThat(history.getOtherInformationDate())
                .as("Other information date")
                .isEqualTo(LocalDate.now());
            assertThat(history.getOtherInformationRef())
                .as("Other information Ref")
                .isEqualTo("indefinite");
            assertThat(history.getPoolNumber())
                .as("Pool number")
                .isEqualTo("222222223");

            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_EXEMPTION, true, courtOwner,
                    "TEST TRIAL");

        }

    }

    PrintLettersRequestDto createPrintLetterRequest(List<String> jurorNumbers, CourtLetterType courtLetterType) {
        return PrintLettersRequestDto.builder()
            .letterType(courtLetterType)
            .jurorNumbers(jurorNumbers)
            .build();
    }

    CertificateOfExemptionRequestDto createCertificateOfExemptionRequest(List<String> jurorNumbers, String judge) {
        CertificateOfExemptionRequestDtoBuilder builder =
            CertificateOfExemptionRequestDto.builder()
                .exemptionPeriod("indefinite")
                .jurorNumbers(jurorNumbers)
                .trialNumber("TEST TRIAL")
                .letterType(CourtLetterType.CERTIFICATE_OF_EXEMPTION);

        if (judge != null) {
            builder.judge(judge);
        }
        return builder.build();
    }

    Juror createJuror(String jurorNumber, boolean welsh, String owner) {
        Juror juror = new Juror();

        juror.setFirstName("FNAME");
        juror.setLastName("LNAME");

        juror.setAddressLine1("ADDRESS LINE 1");
        juror.setAddressLine2("ADDRESS LINE 2");
        juror.setAddressLine3("ADDRESS LINE 3");
        juror.setAddressLine4("ADDRESS LINE 4");
        juror.setAddressLine5("ADDRESS LINE 5");
        juror.setPostcode("POST CDE");

        juror.setNoDefPos(1);

        juror.setJurorNumber(jurorNumber);
        juror.setWelsh(welsh);

        JurorPool jurorPool = JurorPool.builder().juror(juror).owner(owner).build();
        juror.setAssociatedPools(Set.of(jurorPool));
        return juror;
    }

    Optional<SystemParameterMod> createSystemParam(int id, String value) {
        SystemParameterMod sysParam = mock(SystemParameterMod.class);
        doReturn("UNIT TEST").when(sysParam).getCreatedBy();
        doReturn(id).when(sysParam).getId();
        doReturn(value).when(sysParam).getValue();
        return Optional.of(sysParam);
    }

    Tuple createPrintLetterTupleEnglish(String courtLocation) {
        Tuple data = mock(Tuple.class);

        when(data.get(COURT_LOCATION.name)).thenReturn("COURT NAME");
        when(data.get(COURT_LOCATION.address1)).thenReturn("ADDRESS 1");
        when(data.get(COURT_LOCATION.address2)).thenReturn("ADDRESS 2");
        when(data.get(COURT_LOCATION.address3)).thenReturn("ADDRESS 3");
        when(data.get(COURT_LOCATION.address4)).thenReturn("ADDRESS 4");
        when(data.get(COURT_LOCATION.address5)).thenReturn("ADDRESS 5");
        when(data.get(COURT_LOCATION.address6)).thenReturn("ADDRESS 6");

        when(data.get(COURT_LOCATION.locPhone)).thenReturn("0123456789");
        when(data.get(COURT_LOCATION.locCode)).thenReturn(courtLocation);
        when(data.get(COURT_LOCATION.signatory)).thenReturn("TEST SIGNATURE");
        when(data.get(COURT_LOCATION.postcode)).thenReturn("PST CODE");

        when(data.get(JUROR_POOL.juror.firstName)).thenReturn("FNAME");
        when(data.get(JUROR_POOL.juror.lastName)).thenReturn("LNAME");
        when(data.get(JUROR_POOL.juror.addressLine1)).thenReturn("ADDRESS LINE 1");
        when(data.get(JUROR_POOL.juror.addressLine2)).thenReturn("ADDRESS LINE 2");
        when(data.get(JUROR_POOL.juror.addressLine3)).thenReturn("ADDRESS LINE 3");
        when(data.get(JUROR_POOL.juror.addressLine4)).thenReturn("ADDRESS LINE 4");
        when(data.get(JUROR_POOL.juror.addressLine5)).thenReturn("ADDRESS LINE 5");
        when(data.get(JUROR_POOL.juror.postcode)).thenReturn("JUROR POST CODE");
        when(data.get(JUROR_POOL.juror.jurorNumber)).thenReturn("111111111");

        when(data.get(POOL_REQUEST.courtLocation.locCode)).thenReturn(courtLocation);
        when(data.get(POOL_REQUEST.poolNumber)).thenReturn("222222222");
        when(data.get(POOL_REQUEST.attendTime)).thenReturn(LocalDateTime.of(2024, 2, 9, 9, 0));

        return data;
    }

    Tuple createDeferralDataEnglish(LocalDate deferralDate, String courtLocation) {
        Tuple data = createPrintLetterTupleEnglish(courtLocation);
        when(data.get(JUROR_POOL.deferralDate)).thenReturn(deferralDate);
        when(data.get(JUROR_POOL.deferralCode)).thenReturn("A");
        return data;
    }

    Tuple createPrintLetterTupleWelsh(String jurorNumber, String courtName) {
        Tuple data = mock(Tuple.class);
        when(data.get(WELSH_COURT_LOCATION.locCourtName)).thenReturn(courtName);
        when(data.get(WELSH_COURT_LOCATION.address1)).thenReturn("WELSH ADDRESS 1");
        when(data.get(WELSH_COURT_LOCATION.address2)).thenReturn("WELSH ADDRESS 2");
        when(data.get(WELSH_COURT_LOCATION.address3)).thenReturn("WELSH ADDRESS 3");
        when(data.get(WELSH_COURT_LOCATION.address4)).thenReturn("WELSH ADDRESS 4");
        when(data.get(WELSH_COURT_LOCATION.address5)).thenReturn("WELSH ADDRESS 5");
        when(data.get(WELSH_COURT_LOCATION.address6)).thenReturn("WELSH ADDRESS 6");

        when(data.get(COURT_LOCATION.locCode)).thenReturn("457");
        when(data.get(COURT_LOCATION.locPhone)).thenReturn("0123456789");
        when(data.get(COURT_LOCATION.signatory)).thenReturn("TEST SIGNATURE");
        when(data.get(COURT_LOCATION.postcode)).thenReturn("PST CODE");

        when(data.get(JUROR_POOL.juror.firstName)).thenReturn("FNAME");
        when(data.get(JUROR_POOL.juror.lastName)).thenReturn("LNAME");
        when(data.get(JUROR_POOL.juror.addressLine1)).thenReturn("ADDRESS LINE 1");
        when(data.get(JUROR_POOL.juror.addressLine2)).thenReturn("ADDRESS LINE 2");
        when(data.get(JUROR_POOL.juror.addressLine3)).thenReturn("ADDRESS LINE 3");
        when(data.get(JUROR_POOL.juror.addressLine4)).thenReturn("ADDRESS LINE 4");
        when(data.get(JUROR_POOL.juror.addressLine5)).thenReturn("ADDRESS LINE 5");
        when(data.get(JUROR_POOL.juror.postcode)).thenReturn("JUROR POST CODE");
        when(data.get(JUROR_POOL.juror.jurorNumber)).thenReturn(jurorNumber);

        when(data.get(POOL_REQUEST.poolNumber)).thenReturn("222222223");
        when(data.get(POOL_REQUEST.courtLocation.locCode)).thenReturn("457");
        when(data.get(POOL_REQUEST.attendTime)).thenReturn(LocalDateTime.of(2024, 2, 9, 9, 30));

        return data;
    }

    Tuple createDeferralDataWelsh(LocalDate deferralDate, String jurorNumber, String courtName) {
        Tuple data = createPrintLetterTupleWelsh(jurorNumber, courtName);
        when(data.get(JUROR_POOL.deferralDate)).thenReturn(deferralDate);
        when(data.get(JUROR_POOL.deferralCode)).thenReturn("A");
        return data;
    }

    Tuple createExcusalRefusedDataEnglish(LocalDate excusalDate, String courtLocation) {
        Tuple data = createPrintLetterTupleEnglish(courtLocation);
        when(data.get(JUROR_POOL.juror.excusalDate)).thenReturn(excusalDate);
        when(data.get(JUROR_POOL.juror.excusalCode)).thenReturn("C");
        return data;
    }

    Tuple createExcusalRefusedDataWelsh(LocalDate excusalDate) {
        Tuple data = createPrintLetterTupleWelsh("999999999", "Abertawe");
        when(data.get(JUROR_POOL.juror.excusalDate)).thenReturn(excusalDate);
        when(data.get(JUROR_POOL.juror.excusalCode)).thenReturn("C");
        return data;
    }

    Tuple createCertificateOfExemptionData(boolean welsh) {
        Tuple data;
        if (welsh) {
            data = createPrintLetterTupleWelsh("999999999", "ABERTAWE");
        } else {
            data = createPrintLetterTupleEnglish("435");
        }
        when(data.get(TRIAL.judge.description)).thenReturn("SIR DREDD");
        when(data.get(TRIAL.description)).thenReturn("TEST DEFENDANT");
        return data;
    }

    @Nested
    @DisplayName("letter - Certificate of Attendance")
    class CertificateOfAttendance {
        @Test
        @DisplayName("CertificateOfAttendance - English")
        void certificateOfAttendanceLetterEnglishHappy() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(anyString())).thenReturn(
                createJuror(jurorNumber, false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(102);
            doReturn(createCertificateOfAttendanceDataEnglish(BigDecimal.valueOf(40), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), false, "435")).when(
                    courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, false, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.CERTIFICATE_OF_ATTENDANCE), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);

            assertThat(response.get(0).getCourtName())
                .as("Expect court name to be " + "The Crown Court\n"
                    + "at COURT NAME")
                .isEqualTo("The Crown Court\n"
                    + "at COURT NAME");
            assertThat(response.get(0).getCourtAddressLine1())
                .as("Expect address line 1 to be 'ADDRESS 1'")
                .isEqualTo("ADDRESS 1");
            assertThat(response.get(0).getCourtAddressLine2())
                .as("Expect address line 2 to be 'ADDRESS 2'")
                .isEqualTo("ADDRESS 2");
            assertThat(response.get(0).getCourtAddressLine3())
                .as("Expect address line 3 to be 'ADDRESS 3'")
                .isEqualTo("ADDRESS 3");
            assertThat(response.get(0).getCourtAddressLine4())
                .as("Expect address line 4 to be 'ADDRESS 4'")
                .isEqualTo("ADDRESS 4");
            assertThat(response.get(0).getCourtAddressLine5())
                .as("Expect address line 5 to be 'ADDRESS 5'")
                .isEqualTo("ADDRESS 5");
            assertThat(response.get(0).getCourtAddressLine6())
                .as("Expect address line 6 to be 'ADDRESS 6'")
                .isEqualTo("ADDRESS 6");
            assertThat(response.get(0).getCourtPostCode())
                .as("Expect post code to be 'PST CODE'")
                .isEqualTo("PST CODE");
            assertThat(response.get(0).getCourtPhoneNumber())
                .as("Expect court number to be 0123456789")
                .isEqualTo("0123456789");

            assertThat(response.get(0).getUrl())
                .as("Expect URL to be ENGLISH_URL")
                .isEqualTo("ENGLISH_URL");
            assertThat(response.get(0).getSignature())
                .as("Expect signatory to be ")
                .isEqualTo("TEST SIGNATURE\n\nAn Officer of the Crown Court");

            assertThat(response.get(0).getJurorFirstName())
                .as("Expect first name to be FNAME")
                .isEqualTo("FNAME");
            assertThat(response.get(0).getJurorLastName())
                .as("Expect last name to be LNAME")
                .isEqualTo("LNAME");
            assertThat(response.get(0).getJurorAddressLine1())
                .as("Expect address line 1 to be ADDRESS LINE 1")
                .isEqualTo("ADDRESS LINE 1");
            assertThat(response.get(0).getJurorAddressLine2())
                .as("Expect address line 2 to be ADDRESS LINE 2")
                .isEqualTo("ADDRESS LINE 2");
            assertThat(response.get(0).getJurorAddressLine3())
                .as("Expect address line 3 to be ADDRESS LINE 3")
                .isEqualTo("ADDRESS LINE 3");
            assertThat(response.get(0).getJurorAddressLine4())
                .as("Expect address line 4 to be ADDRESS LINE 4")
                .isEqualTo("ADDRESS LINE 4");
            assertThat(response.get(0).getJurorAddressLine5())
                .as("Expect address line 5 to be ADDRESS LINE 5")
                .isEqualTo("ADDRESS LINE 5");
            assertThat(response.get(0).getJurorPostcode())
                .as("Expect post code to be JUROR POST CODE")
                .isEqualTo("JUROR POST CODE");
            assertThat(response.get(0).getJurorNumber())
                .as("Expect juror number to be " + jurorNumber)
                .isEqualTo(jurorNumber);
            assertThat(response.get(0).getAttendTime())
                .as("Expect attend time to be 09:00")
                .isEqualTo(LocalTime.of(9, 0));
            assertThat(response.get(0).getWelsh())
                .as("Expect welsh to be false")
                .isFalse();

            assertThat(response.get(0).getNonAttendance())
                .as("Expect non attendance to be Non Attendance")
                .isEqualTo("Non Attendance");
            assertThat(response.get(0).getLossOfEarnings())
                .as("Expect loss of earnings to be 40")
                .isEqualTo(new BigDecimal(40));
            assertThat(response.get(0).getChildCare())
                .as("Expect child care to be 10")
                .isEqualTo(new BigDecimal(10));
            assertThat(response.get(0).getMisc())
                .as("Expect misc to be 10")
                .isEqualTo(new BigDecimal(10));

            ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

            verify(systemParameterRepositoryMod, times(1)).findById(102);
            verify(jurorRepository, times(1)).findByJurorNumber(jurorNumber);
            verify(welshCourtLocationRepository, times(1)).findById("435");
            verify(jurorHistoryRepository, times(1)).save(jurorHistoryArgumentCaptor.capture());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, false,
                    courtOwner);

            JurorHistory jurorHistory = jurorHistoryArgumentCaptor.getValue();
            assertThat(jurorHistory.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(jurorHistory.getDateCreated()).isNotNull();
            assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CERTIFICATE_OF_RECOGNITION);
            assertThat(jurorHistory.getOtherInformation()).isEqualTo("Certificate of Attendance");
            assertThat(jurorHistory.getCreatedBy()).isEqualTo("TEST_COURTUSER");
        }

        @Test
        @DisplayName("CertificateOfAttendance happy - Welsh")
        void certificateOfAttendanceLetterWelshHappy() {
            final String jurorNumber = "111111112";
            final String courtOwner = "457";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(jurorNumber)).thenReturn(createJuror(jurorNumber, true, courtOwner));
            WelshCourtLocation welshCourtLocation = WelshCourtLocation.builder().locCode(courtOwner).build();
            when(welshCourtLocationRepository.findById(courtOwner)).thenReturn(Optional.of(welshCourtLocation));
            doReturn(createSystemParam(WELSH_URL_PARAM, "WELSH_URL"))
                .when(systemParameterRepositoryMod).findById(103);

            doReturn(createCertificateOfAttendanceDataWelsh(jurorNumber, BigDecimal.valueOf(40), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), false, "ABERTAWE"))
                .when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, true, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.CERTIFICATE_OF_ATTENDANCE), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            for (PrintLetterDataResponseDto dto : response) {
                assertThat(dto.getCourtName()).as("Expect court name to be Llys y Goron\nynAbertawe")
                    .isEqualTo("Llys y Goron\nynAbertawe");
                assertThat(dto.getCourtAddressLine1())
                    .as("Expect address line 1 to be 'WELSH ADDRESS 1'")
                    .isEqualTo("WELSH ADDRESS 1");
                assertThat(dto.getCourtAddressLine2())
                    .as("Expect address line 2 to be 'WELSH ADDRESS 2'")
                    .isEqualTo("WELSH ADDRESS 2");
                assertThat(dto.getCourtAddressLine3())
                    .as("Expect address line 3 to be 'WELSH ADDRESS 3'")
                    .isEqualTo("WELSH ADDRESS 3");
                assertThat(dto.getCourtAddressLine4())
                    .as("Expect address line 4 to be 'WELSH ADDRESS 4'")
                    .isEqualTo("WELSH ADDRESS 4");
                assertThat(dto.getCourtAddressLine5())
                    .as("Expect address line 5 to be 'WELSH ADDRESS 5'")
                    .isEqualTo("WELSH ADDRESS 5");
                assertThat(dto.getCourtAddressLine6())
                    .as("Expect address line 6 to be 'WELSH ADDRESS 6'")
                    .isEqualTo("WELSH ADDRESS 6");
                assertThat(dto.getCourtPostCode())
                    .as("Expect post code to be 'PST CODE'")
                    .isEqualTo("PST CODE");
                assertThat(dto.getCourtPhoneNumber())
                    .as("Expect court number to be 0123456789")
                    .isEqualTo("0123456789");
                assertThat(dto.getSignature())
                    .as("Expect signatory to be ")
                    .isEqualTo("TEST SIGNATURE\n\nSwyddog Llys");

                assertThat(dto.getUrl())
                    .as("Expect URL to be WELSH_URL")
                    .isEqualTo("WELSH_URL");

                assertThat(dto.getJurorFirstName())
                    .as("Expect first name to be FNAME")
                    .isEqualTo("FNAME");
                assertThat(dto.getJurorLastName())
                    .as("Expect last name to be LNAME")
                    .isEqualTo("LNAME");
                assertThat(dto.getJurorAddressLine1())
                    .as("Expect address line 1 to be ADDRESS LINE 1")
                    .isEqualTo("ADDRESS LINE 1");
                assertThat(dto.getJurorAddressLine2())
                    .as("Expect address line 2 to be ADDRESS LINE 2")
                    .isEqualTo("ADDRESS LINE 2");
                assertThat(dto.getJurorAddressLine3())
                    .as("Expect address line 3 to be ADDRESS LINE 3")
                    .isEqualTo("ADDRESS LINE 3");
                assertThat(dto.getJurorAddressLine4())
                    .as("Expect address line 4 to be ADDRESS LINE 4")
                    .isEqualTo("ADDRESS LINE 4");
                assertThat(dto.getJurorAddressLine5())
                    .as("Expect address line 5 to be ADDRESS LINE 5")
                    .isEqualTo("ADDRESS LINE 5");
                assertThat(dto.getJurorPostcode())
                    .as("Expect post code to be JUROR POST CODE")
                    .isEqualTo("JUROR POST CODE");
                assertThat(dto.getJurorNumber())
                    .as("Expect juror number to be " + jurorNumber)
                    .isEqualTo(jurorNumber);
                assertThat(dto.getAttendTime())
                    .as("Expect attend time to be 09:30")
                    .isEqualTo(LocalTime.of(9, 30));
                assertThat(dto.getWelsh())
                    .as("Expect welsh to be true")
                    .isTrue();

                assertThat(response.get(0).getNonAttendance())
                    .as("Expect non attendance to be Non Attendance")
                    .isEqualTo("Non Attendance");
                assertThat(response.get(0).getLossOfEarnings())
                    .as("Expect loss of earnings to be 40")
                    .isEqualTo(new BigDecimal(40));
                assertThat(response.get(0).getChildCare())
                    .as("Expect child care to be 10")
                    .isEqualTo(new BigDecimal(10));
                assertThat(response.get(0).getMisc())
                    .as("Expect misc to be 10")
                    .isEqualTo(new BigDecimal(10));
            }

            ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

            verify(systemParameterRepositoryMod, times(1)).findById(103);
            verify(jurorRepository, times(1)).findByJurorNumber(jurorNumber);
            verify(welshCourtLocationRepository, times(1)).findById(courtOwner);
            verify(jurorHistoryRepository, times(1)).save(jurorHistoryArgumentCaptor.capture());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, true,
                    courtOwner);

            JurorHistory jurorHistory = jurorHistoryArgumentCaptor.getValue();
            assertThat(jurorHistory.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(jurorHistory.getDateCreated()).isNotNull();
            assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CERTIFICATE_OF_RECOGNITION);
            assertThat(jurorHistory.getOtherInformation()).isEqualTo("Certificate of Attendance");
            assertThat(jurorHistory.getCreatedBy()).isEqualTo("TEST_COURTUSER");
        }

        @Test
        @DisplayName("CertificateOfAttendance letter - Court name formatting - English - Crown Court")
        void certificateOfAttendanceLetterFormatCourtNameEnglishCrownCourt() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(jurorNumber)).thenReturn(
                createJuror(jurorNumber, false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(102);
            doReturn(createCertificateOfAttendanceDataEnglish(BigDecimal.valueOf(40), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), false, "435")).when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, false, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.CERTIFICATE_OF_ATTENDANCE), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            assertThat(response.get(0).getCourtName())
                .as("Expect court name to be The Royal Court\n of Justice")
                .isEqualTo("The Crown Court\nat " + "COURT NAME");

            ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

            verify(systemParameterRepositoryMod, times(1)).findById(102);
            verify(jurorRepository, times(1)).findByJurorNumber(jurorNumber);
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(jurorHistoryArgumentCaptor.capture());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, false,
                    courtOwner);

            JurorHistory jurorHistory = jurorHistoryArgumentCaptor.getValue();
            assertThat(jurorHistory.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(jurorHistory.getDateCreated()).isNotNull();
            assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CERTIFICATE_OF_RECOGNITION);
            assertThat(jurorHistory.getOtherInformation()).isEqualTo("Certificate of Attendance");
            assertThat(jurorHistory.getCreatedBy()).isEqualTo("TEST_COURTUSER");
        }

        @Test
        @DisplayName("CertificateOfAttendance letter - Court name formatting - English - Royal court")
        void certificateOfAttendanceLetterFormatCourtNameEnglishRoyalCourt() {
            final String jurorNumber = "111111111";
            final String courtOwner = "415";
            mockCurrentUser(courtOwner);

            when(jurorRepository.findByJurorNumber(jurorNumber)).thenReturn(
                createJuror(jurorNumber, false, courtOwner));
            when(welshCourtLocationRepository.findById(anyString())).thenReturn(Optional.empty());
            doReturn(createSystemParam(ENGLISH_URL_PARAM, "ENGLISH_URL"))
                .when(systemParameterRepositoryMod).findById(102);
            doReturn(createCertificateOfAttendanceDataEnglish(BigDecimal.valueOf(40), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), false, "626")).when(courtPrintLetterRepository)
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, false, courtOwner);

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(Collections.singletonList(jurorNumber),
                    CourtLetterType.CERTIFICATE_OF_ATTENDANCE), "TEST_COURTUSER");

            assertThat(response.size()).as("Expect size to be 1").isEqualTo(1);
            assertThat(response.get(0).getCourtName())
                .as("Expect court name to be The Royal Court\nof Justice")
                .isEqualTo("The Royal Court\nof Justice");

            ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

            verify(systemParameterRepositoryMod, times(1)).findById(102);
            verify(jurorRepository, times(1)).findByJurorNumber(jurorNumber);
            verify(welshCourtLocationRepository, times(1)).findById(anyString());
            verify(jurorHistoryRepository, times(1)).save(jurorHistoryArgumentCaptor.capture());
            verify(courtPrintLetterRepository, times(1))
                .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, false,
                    courtOwner);

            JurorHistory jurorHistory = jurorHistoryArgumentCaptor.getValue();
            assertThat(jurorHistory.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(jurorHistory.getDateCreated()).isNotNull();
            assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CERTIFICATE_OF_RECOGNITION);
            assertThat(jurorHistory.getOtherInformation()).isEqualTo("Certificate of Attendance");
            assertThat(jurorHistory.getCreatedBy()).isEqualTo("TEST_COURTUSER");
        }


        @Test
        @DisplayName("Certificate of Attendance letter - Court name formatting - Welsh")
        void certificateOfAttendanceLetterFormatCourtNameWelsh() {
            final String courtOwner = "457";
            mockCurrentUser(courtOwner);

            List<String> jurorNumbers = new ArrayList<>();
            List<String> courtNames = new ArrayList<>();

            courtNames.add("BOURT NAME");
            courtNames.add("MOURT NAME");
            courtNames.add("COURT NAME");
            courtNames.add("DOURT NAME");
            courtNames.add("GOURT NAME");
            courtNames.add("POURT NAME");
            courtNames.add("TOURT NAME");
            courtNames.add("OOURT NAME");

            for (int i = 0;
                 i < courtNames.size();
                 i++) {
                String jurorNumber = String.format("1111111%02d", i + 1);

                when(jurorRepository.findByJurorNumber(jurorNumber)).thenReturn(
                    createJuror(jurorNumber, true, courtOwner));
                doReturn(createCertificateOfAttendanceDataWelsh(jurorNumber, BigDecimal.valueOf(40),
                    BigDecimal.valueOf(10),
                    BigDecimal.valueOf(10), false, courtNames.get(i)))
                    .when(courtPrintLetterRepository)
                    .retrievePrintInformation(jurorNumber, CourtLetterType.CERTIFICATE_OF_ATTENDANCE, true, courtOwner);

                jurorNumbers.add(jurorNumber);
            }

            WelshCourtLocation welshCourtLocation = WelshCourtLocation.builder().locCode(courtOwner).build();
            when(welshCourtLocationRepository.findById(courtOwner)).thenReturn(Optional.of(welshCourtLocation));
            doReturn(createSystemParam(WELSH_URL_PARAM, "WELSH_URL"))
                .when(systemParameterRepositoryMod).findById(anyInt());

            List<PrintLetterDataResponseDto> response = courtLetterPrintServiceImpl
                .getPrintLettersData(createPrintLetterRequest(jurorNumbers, CourtLetterType.CERTIFICATE_OF_ATTENDANCE),
                    "TEST_COURTUSER");

            assertThat(response.size()).as(
                "Expect size to be " + courtNames.size()).isEqualTo(courtNames.size());

            for (int i = 0;
                 i < courtNames.size();
                 i++) {
                final String formattedCourtName = response.get(i).getCourtName();
                switch (courtNames.get(i).toLowerCase().charAt(0)) {
                    case 'b', 'm' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nym Mourt name")
                        .isEqualTo("Llys y Goron\nym Mourt name");
                    case 'c' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nyng Nghourt name")
                        .isEqualTo("Llys y Goron\nyng Nghourt name");
                    case 'd' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nyn Nourt name")
                        .isEqualTo("Llys y Goron\nyn Nourt name");
                    case 'g' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nyng Ngourt name")
                        .isEqualTo("Llys y Goron\nyng Ngourt name");
                    case 'p' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nym Mhourt name")
                        .isEqualTo("Llys y Goron\nym Mhourt name");
                    case 't' -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nyn Nhourt name")
                        .isEqualTo("Llys y Goron\nyn Nhourt name");
                    default -> assertThat(formattedCourtName).as("Expect name to be Llys y Goron\nynOourt name")
                        .isEqualTo("Llys y Goron\nynOourt name");
                }
            }
        }

        Tuple createCertificateOfAttendanceDataEnglish(BigDecimal lossOfEarnings, BigDecimal childCare,
                                                       BigDecimal miscAmount, Boolean nonAttendance,
                                                       String courtLocation) {
            Tuple data = createPrintLetterTupleEnglish(courtLocation);
            when(data.get(APPEARANCE.lossOfEarningsDue)).thenReturn(lossOfEarnings);
            when(data.get(APPEARANCE.childcareDue)).thenReturn(childCare);
            when(data.get(APPEARANCE.miscAmountDue)).thenReturn(miscAmount);
            when(data.get(APPEARANCE.nonAttendanceDay)).thenReturn(nonAttendance);
            return data;
        }

        Tuple createCertificateOfAttendanceDataWelsh(String jurorNumber, BigDecimal lossOfEarnings,
                                                     BigDecimal childCare, BigDecimal miscAmount,
                                                     Boolean nonAttendance, String courtName) {
            Tuple data = createPrintLetterTupleWelsh(jurorNumber, courtName);
            when(data.get(APPEARANCE.lossOfEarningsDue)).thenReturn(lossOfEarnings);
            when(data.get(APPEARANCE.childcareDue)).thenReturn(childCare);
            when(data.get(APPEARANCE.miscAmountDue)).thenReturn(miscAmount);
            when(data.get(APPEARANCE.nonAttendanceDay)).thenReturn(nonAttendance);
            return data;
        }
    }
}

