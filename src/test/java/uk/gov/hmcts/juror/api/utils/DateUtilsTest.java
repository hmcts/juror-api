package uk.gov.hmcts.juror.api.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import static uk.gov.hmcts.juror.api.TestUtils.compareLocalDates;

@RunWith(SpringRunner.class)
public class DateUtilsTest {

    @Test
    public void test_getStartOfWeekFromDate_mondayProvided() {
        // instantiate a date for a Monday
        LocalDate providedDate = LocalDate.of(2023, 5, 22);

        Assertions.assertThat(providedDate.getDayOfWeek())
            .as("Provided date should already be a Monday")
            .isEqualTo(DayOfWeek.MONDAY);

        LocalDate startOfWeek = DateUtils.getStartOfWeekFromDate(providedDate);

        Assertions.assertThat(startOfWeek.getDayOfWeek())
            .as("Start of week date should remain as a Monday")
            .isEqualTo(DayOfWeek.MONDAY);

        compareLocalDates(providedDate, startOfWeek);
    }

    @Test
    public void test_getStartOfWeekFromDate_tuesdayProvided() {
        // instantiate a date for a Tuesday
        LocalDate providedDate = LocalDate.of(2023, 5, 23);

        Assertions.assertThat(providedDate.getDayOfWeek())
            .as("Provided date should be a Tuesday")
            .isEqualTo(DayOfWeek.TUESDAY);

        LocalDate startOfWeek = DateUtils.getStartOfWeekFromDate(providedDate);

        Assertions.assertThat(startOfWeek.getDayOfWeek())
            .as("Start of week date should now as a Monday")
            .isEqualTo(DayOfWeek.MONDAY);

        compareLocalDates(startOfWeek, providedDate.minusDays(1));
    }

    @Test
    public void test_getStartOfWeekFromDate_wednesdayProvided() {
        // instantiate a date for a Wednesday
        LocalDate providedDate = LocalDate.of(2023, 5, 24);

        Assertions.assertThat(providedDate.getDayOfWeek())
            .as("Provided date should be a Wednesday")
            .isEqualTo(DayOfWeek.WEDNESDAY);

        LocalDate startOfWeek = DateUtils.getStartOfWeekFromDate(providedDate);

        Assertions.assertThat(startOfWeek.getDayOfWeek())
            .as("Start of week date should now as a Monday")
            .isEqualTo(DayOfWeek.MONDAY);

        compareLocalDates(startOfWeek, providedDate.minusDays(2));
    }

    @Test
    public void test_getStartOfWeekFromDate_thursdayProvided() {
        // instantiate a date for a Thursday
        LocalDate providedDate = LocalDate.of(2023, 5, 25);

        Assertions.assertThat(providedDate.getDayOfWeek())
            .as("Provided date should be a Thursday")
            .isEqualTo(DayOfWeek.THURSDAY);

        LocalDate startOfWeek = DateUtils.getStartOfWeekFromDate(providedDate);

        Assertions.assertThat(startOfWeek.getDayOfWeek())
            .as("Start of week date should now as a Monday")
            .isEqualTo(DayOfWeek.MONDAY);

        compareLocalDates(startOfWeek, providedDate.minusDays(3));
    }

    @Test
    public void test_getStartOfWeekFromDate_fridayProvided() {
        // instantiate a date for a Friday
        LocalDate providedDate = LocalDate.of(2023, 5, 26);

        Assertions.assertThat(providedDate.getDayOfWeek())
            .as("Provided date should be a Friday")
            .isEqualTo(DayOfWeek.FRIDAY);

        LocalDate startOfWeek = DateUtils.getStartOfWeekFromDate(providedDate);

        Assertions.assertThat(startOfWeek.getDayOfWeek())
            .as("Start of week date should now as a Monday")
            .isEqualTo(DayOfWeek.MONDAY);

        compareLocalDates(startOfWeek, providedDate.minusDays(4));
    }

    @Test
    public void test_getStartOfWeekFromDate_saturdayProvided() {
        // instantiate a date for a Saturday
        LocalDate providedDate = LocalDate.of(2023, 5, 27);

        Assertions.assertThat(providedDate.getDayOfWeek())
            .as("Provided date should be a Saturday")
            .isEqualTo(DayOfWeek.SATURDAY);

        LocalDate startOfWeek = DateUtils.getStartOfWeekFromDate(providedDate);

        Assertions.assertThat(startOfWeek.getDayOfWeek())
            .as("Start of week date should now as a Monday")
            .isEqualTo(DayOfWeek.MONDAY);

        compareLocalDates(startOfWeek, providedDate.minusDays(5));
    }

    @Test
    public void test_getStartOfWeekFromDate_sundayProvided() {
        // instantiate a date for a Sunday
        LocalDate providedDate = LocalDate.of(2023, 5, 28);

        Assertions.assertThat(providedDate.getDayOfWeek())
            .as("Provided date should be a Sunday")
            .isEqualTo(DayOfWeek.SUNDAY);

        LocalDate startOfWeek = DateUtils.getStartOfWeekFromDate(providedDate);

        Assertions.assertThat(startOfWeek.getDayOfWeek())
            .as("Start of week date should now as a Monday")
            .isEqualTo(DayOfWeek.MONDAY);

        compareLocalDates(startOfWeek, providedDate.minusDays(6));
    }

    @Test
    public void testConvertLocalisedDateToIsoNoZeroPadding() {
        String localisedDate = "1/2/2023";
        String isoDate = DateUtils.convertLocalisedDateToISO(localisedDate);
        Assertions.assertThat(isoDate).isEqualTo("2023-02-01");
    }

    @Test
    public void testConvertLocalisedDateToIsoZeroPaddedDay() {
        String localisedDate = "01/2/2023";
        String isoDate = DateUtils.convertLocalisedDateToISO(localisedDate);
        Assertions.assertThat(isoDate).isEqualTo("2023-02-01");
    }

    @Test
    public void testConvertLocalisedDateToIsoZeroPaddedMonth() {
        String localisedDate = "1/02/2023";
        String isoDate = DateUtils.convertLocalisedDateToISO(localisedDate);
        Assertions.assertThat(isoDate).isEqualTo("2023-02-01");
    }

    @Test
    public void testConvertLocalisedDateToIsoZeroPaddedDayAndMonth() {
        String localisedDate = "01/02/2023";
        String isoDate = DateUtils.convertLocalisedDateToISO(localisedDate);
        Assertions.assertThat(isoDate).isEqualTo("2023-02-01");
    }

    @Test
    public void testConvertLocalisedDateToIsoEmptyString() {
        String empty = "";
        Assertions.assertThatExceptionOfType(DateTimeParseException.class).isThrownBy(() ->
            DateUtils.convertLocalisedDateToISO(empty));
    }

    @Test
    public void testConvertLocalisedDateToIsoInvalidDateFormat() {
        String invalidDate = "2023-02-01";
        Assertions.assertThatExceptionOfType(DateTimeParseException.class).isThrownBy(() ->
            DateUtils.convertLocalisedDateToISO(invalidDate));
    }

    @Test
    public void testConvertLocalisedDateToIsoNullString() {
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
            DateUtils.convertLocalisedDateToISO(null));
    }

    @Test
    public void test_convertToDateViaInstant_validArgument() {
        LocalDate dateToConvert = LocalDate.of(2023, 6, 14);
        Date convertedDate = DateUtils.convertToDateViaInstant(dateToConvert);

        Assertions.assertThat(convertedDate).isInstanceOf(Date.class);

        Assertions.assertThat(convertedDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()).isEqualTo(dateToConvert);
    }

    @Test
    public void test_convertToDateViaInstant_nullArgument() {
        Date convertedDate = DateUtils.convertToDateViaInstant(null);
        Assertions.assertThat(convertedDate)
            .as("Expected result is null")
            .isNull();
    }

    @Test
    public void test_getNumberOfStartingWeeks_fourWeeks() {
        List<LocalDate> startingWeekDates = DateUtils.getNumberOfStartingWeeks(4, LocalDate.now());
        Assertions.assertThat(startingWeekDates.size())
            .as("Expected list size to be four")
            .isEqualTo(4);
        Assertions.assertThat(startingWeekDates.get(0))
            .as("Expected first item to be %s", DateUtils.getStartOfWeekFromDate(LocalDate.now()))
            .isEqualTo(DateUtils.getStartOfWeekFromDate(LocalDate.now()));
        Assertions.assertThat(startingWeekDates.get(1))
            .as("Expected second item to be %s", DateUtils.getStartOfWeekFromDate(LocalDate.now()).plusWeeks(1))
            .isEqualTo(DateUtils.getStartOfWeekFromDate(LocalDate.now()).plusWeeks(1));
        Assertions.assertThat(startingWeekDates.get(2))
            .as("Expected third item to be %s", DateUtils.getStartOfWeekFromDate(LocalDate.now()).plusWeeks(2))
            .isEqualTo(DateUtils.getStartOfWeekFromDate(LocalDate.now()).plusWeeks(2));
        Assertions.assertThat(startingWeekDates.get(3))
            .as("Expected fourth item to be %s", DateUtils.getStartOfWeekFromDate(LocalDate.now()).plusWeeks(3))
            .isEqualTo(DateUtils.getStartOfWeekFromDate(LocalDate.now()).plusWeeks(3));
    }

    @Test
    public void test_getNumberOfStartingWeeks_zeroWeeks() {
        List<LocalDate> startingWeekDates = DateUtils.getNumberOfStartingWeeks(0, LocalDate.now());
        Assertions.assertThat(startingWeekDates.size())
            .as("Expected list size to be zero")
            .isEqualTo(0);
    }

    @Test
    public void test_getNumberOfStartingWeeks_negativeWeeks() {
        List<LocalDate> startingWeekDates = DateUtils.getNumberOfStartingWeeks(-1, LocalDate.now());
        Assertions.assertThat(startingWeekDates.size())
            .as("Expected list size to be zero")
            .isEqualTo(0);
    }
}
