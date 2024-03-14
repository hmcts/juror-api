package uk.gov.hmcts.juror.api.moj.xerox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("PMD.LawOfDemeter")
class LetterBaseTest {
    private MockedStatic<Calendar> mockStaticCalendar;
    @Mock
    private Calendar mockCalendar;

    @BeforeEach
    void mockCalendar() {
        mockStaticCalendar = mockStatic(Calendar.class);
        mockStaticCalendar.when(() -> Calendar.getInstance((Locale)any())).thenReturn(mockCalendar);
        mockStaticCalendar.when(Calendar::getInstance).thenReturn(mockCalendar);
        // Mock for business logic
        doReturn(Calendar.MONDAY).when(mockCalendar).get(Calendar.DAY_OF_WEEK);
        // Mocks for date formatter
        doReturn(17).when(mockCalendar).get(Calendar.DAY_OF_MONTH);
        doReturn("Monday").when(mockCalendar).getDisplayName(eq(Calendar.DAY_OF_WEEK), eq(Calendar.LONG), any());
        doReturn("January").when(mockCalendar).getDisplayName(eq(Calendar.MONTH), eq(Calendar.LONG), any());
        doReturn(2024).when(mockCalendar).get(Calendar.YEAR);
    }

    @AfterEach
    void afterEach() {
        if (mockStaticCalendar != null) {
            mockStaticCalendar.close();
        }
    }

    private LetterBase.LetterContext.LetterContextBuilder testContextBuilder() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);

        return LetterBase.LetterContext.builder()
            .jurorPool(LetterTestUtils.testJurorPool(date));
    }

    @Test
    void dateOfLetterIsCorrectEarlyInWeek() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());
        testLetter.addData(LetterBase.LetterDataType.DATE_OF_LETTER, 18);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "17 JANUARY 2024", 18
        ));
    }

    @Test
    void dateOfLetterIsCorrectLateInWeek() {
        // TODO: Fails to test properly due to mocks
        doReturn(Calendar.FRIDAY).when(mockCalendar).get(Calendar.DAY_OF_WEEK);

        LetterBase testLetter = new LetterBase(testContextBuilder().build());
        testLetter.addData(LetterBase.LetterDataType.DATE_OF_LETTER, 18);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "17 JANUARY 2024", 18
        ));
    }

    @Test
    void dateOfLetterThrowsAtWeekend() {
        doReturn(Calendar.SUNDAY).when(mockCalendar).get(Calendar.DAY_OF_WEEK);

        LetterBase testLetter = new LetterBase(testContextBuilder().build());
        testLetter.addData(LetterBase.LetterDataType.DATE_OF_LETTER, 18);
        assertThatExceptionOfType(MojException.BusinessRuleViolation.class)
            .isThrownBy(testLetter::getLetterString);
    }

    @Test
    void courtLocationCodeIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_LOCATION_CODE, 3);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "457", 3
        ));
    }

    @Test
    void courtNameIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_NAME, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "SWANSEA CROWN COURT", 15
        ));
    }

    @Test
    void courtAddress1IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_ADDRESS1, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "THE LAW COURTS", 15
        ));
    }

    @Test
    void courAddress2IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_ADDRESS2, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "ST HELENS ROAD", 15
        ));
    }

    @Test
    void courtAddress3IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_ADDRESS3, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "SWANSEA", 15
        ));
    }

    @Test
    void courtAddress4IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_ADDRESS4, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "SHREWSBURY", 15
        ));
    }

    @Test
    void courtAddress5IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_ADDRESS5, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "COURT_ADDRESS_5", 15
        ));
    }

    @Test
    void courtAddress6IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_ADDRESS6, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "COURT_ADDRESS_6", 15
        ));
    }

    @Test
    void courtPostcodeIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_POSTCODE, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "SY2 6LU", 15
        ));
    }

    @Test
    void courtPhoneIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_PHONE, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "01792637000", 15
        ));
    }

    @Test
    void courtFaxIsEmpty() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_FAX, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "", 15
        ));
    }

    @Test
    void courtInsertIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.INSERT_INDICATORS, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "TWO WEEKS", 15
        ));
    }

    @Test
    void courtSignatoryIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());
        testLetter.addData(LetterBase.LetterDataType.COURT_SIGNATORY, 15);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad(
            "JURY MANAGER", 15
        ));
    }

    @Test
    void bureauNameIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_NAME, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("JURY CENTRAL SUMMONING BUREAU", 40));
    }

    @Test
    void bureauAddress1IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_ADDRESS1, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("THE COURT SERVICE", 40));
    }

    @Test
    void bureauAddress2IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_ADDRESS2, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("FREEPOST LON 19669", 40));
    }

    @Test
    void bureauAddress3IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_ADDRESS3, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("POCOCK STREET", 40));
    }

    @Test
    void bureauAddress4IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_ADDRESS4, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("LONDON", 40));
    }

    @Test
    void bureauAddress5IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_ADDRESS5, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("BUREAU_ADDRESS_5", 40));
    }

    @Test
    void bureauAddress6IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_ADDRESS6, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("BUREAU_ADDRESS_6", 40));
    }

    @Test
    void bureauPostcodeIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_POSTCODE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("SE1 0YG", 40));
    }

    @Test
    void bureauPhoneIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_PHONE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("0845 3555567", 40));
    }

    @Test
    void bureauFaxIsBlank() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_FAX, 40);
        // Fax is deprecated and always empty
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("", 40));
    }

    @Test
    void bureauSignatoryIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .bureauLocation(LetterTestUtils.testBureauLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.BUREAU_SIGNATORY, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("JURY MANAGER", 40));
    }

    @Test
    void dateOfAttendanceIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.DATE_OF_ATTENDANCE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("MONDAY 17 JANUARY, 2024", 40));
    }

    @Test
    void deferralDateIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.DEFERRAL_DATE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("MONDAY 17 JANUARY, 2024", 40));
    }

    @Test
    void welshDateOfAttendanceIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_DATE_OF_ATTENDANCE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("DYDD LLUN 17 IONAWR, 2024", 40));
    }

    @Test
    void welshDeferralDateIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_DEFERRAL_DATE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("DYDD LLUN 17 IONAWR, 2024", 40));
    }

    @Test
    void timeOfAttendanceIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.TIME_OF_ATTENDANCE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("10:00", 40));
    }

    @Test
    void deferralTimeIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .courtLocation(LetterTestUtils.testCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.DEFERRAL_TIME, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("10:00", 40));
    }

    @Test
    void jurorTitleIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_TITLE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("MR", 40));
    }

    @Test
    void jurorFirstNameIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_FIRST_NAME, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("FNAMEEIGHTTHREEONE", 40));
    }

    @Test
    void jurorLastNameIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_LAST_NAME, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("LNAMEEIGHTTHREEONE", 40));
    }

    @Test
    void jurorAddress1IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_ADDRESS1, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("831 STREET NAME", 40));
    }

    @Test
    void jurorAddress2IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_ADDRESS2, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("ANYTOWN", 40));
    }

    @Test
    void jurorAddress3IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_ADDRESS3, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("JUROR_ADDRESS_3", 40));
    }

    @Test
    void jurorAddress4IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_ADDRESS4, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("JUROR_ADDRESS_4", 40));
    }

    @Test
    void jurorAddress5IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_ADDRESS5, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("JUROR_ADDRESS_5", 40));
    }

    @Test
    void jurorAddress6IsBlank() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_ADDRESS6, 40);
        // Always empty
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("", 40));
    }

    @Test
    void jurorPostcodeIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_POSTCODE, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("SY2 6LU", 40));
    }

    @Test
    void jurorNumberIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.JUROR_NUMBER, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("641500541", 40));
    }

    @Test
    void poolNumberIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder().build());

        testLetter.addData(LetterBase.LetterDataType.POOL_NUMBER, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("415221201", 40));
    }

    @Test
    void additionalInformationIsCorrect() {
        final String additionalInformation =
            "This is the additional information for the request additional information letter";
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .additionalInformation(additionalInformation).build());

        testLetter.addData(LetterBase.LetterDataType.ADDITIONAL_INFORMATION, 210);
        assertThat(testLetter.getLetterString())
            .isEqualTo(LetterTestUtils.pad(additionalInformation.toUpperCase(), 210));
    }

    @Test
    void welshCourtNameIsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .welshCourtLocation(LetterTestUtils.testWelshCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_COURT_NAME, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("ABERTAWE", 40));
    }

    @Test
    void welshCourtAddress1IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .welshCourtLocation(LetterTestUtils.testWelshCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_COURT_ADDRESS1, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("Y LLYSOEDD BARN", 40));
    }

    @Test
    void welshCourtAddress2IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .welshCourtLocation(LetterTestUtils.testWelshCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_COURT_ADDRESS2, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("LON SAN HELEN", 40));
    }

    @Test
    void welshCourtAddress3IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .welshCourtLocation(LetterTestUtils.testWelshCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_COURT_ADDRESS3, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("ABERTAWE", 40));
    }

    @Test
    void welshCourtAddress4IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .welshCourtLocation(LetterTestUtils.testWelshCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_COURT_ADDRESS4, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("WELSH_COURT_ADDRESS_4", 40));
    }

    @Test
    void welshCourtAddress5IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .welshCourtLocation(LetterTestUtils.testWelshCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_COURT_ADDRESS5, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("WELSH_COURT_ADDRESS_5", 40));
    }

    @Test
    void welshCourtAddress6IsCorrect() {
        LetterBase testLetter = new LetterBase(testContextBuilder()
                                                   .welshCourtLocation(LetterTestUtils.testWelshCourtLocation())
                                                   .build());

        testLetter.addData(LetterBase.LetterDataType.WELSH_COURT_ADDRESS6, 40);
        assertThat(testLetter.getLetterString()).isEqualTo(LetterTestUtils.pad("WELSH_COURT_ADDRESS_6", 40));
    }

}
