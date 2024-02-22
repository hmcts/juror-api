package uk.gov.hmcts.juror.api.moj.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysRepository;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.juror.domain.HolidaysQueries.isCourtHoliday;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HolidayITest extends ContainerTest {
    @Autowired
    HolidaysRepository holidaysRepository;

    @BeforeClass
    public static void setUp() {
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/Holiday.sql"})
    public void testCourtHolidayNoPublicHoliday() {
        String locationCode = "415";
        Iterable<Holidays> holidayIterable = holidaysRepository.findAll(isCourtHoliday(locationCode,
            Date.valueOf(LocalDate.of(2023, 9,
                20))));
        ArrayList<Holidays> holidays = new ArrayList<>();
        holidayIterable.forEach(holidays::add);

        assertThat(holidays.size()).as("Expected one holiday date").isEqualTo(1);
        assertThat(holidays.get(0).getPublicHoliday()).as("Should not be a public holiday").isFalse();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/Holiday.sql"})
    public void testCourtHolidayWithPublicHoliday() {
        String locationCode = "415";
        Iterable<Holidays> holidayIterable = holidaysRepository.findAll(isCourtHoliday(locationCode,
            Date.valueOf(LocalDate.of(2023, 9,
                28))));
        ArrayList<Holidays> holidays = new ArrayList<>();
        holidayIterable.forEach(holidays::add);
        assertThat(holidays.size()).as("Expected two holiday dates").isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/Holiday.sql"})
    public void tesPublicHolidayNullOwner() {
        Iterable<Holidays> holidayIterable = holidaysRepository.findAll(isCourtHoliday(null,
            Date.valueOf(LocalDate.of(2023, 9,
                29))));
        ArrayList<Holidays> holidays = new ArrayList<>();
        holidayIterable.forEach(holidays::add);
        assertThat(holidays.size()).as("Expected one holiday dates").isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/Holiday.sql"})
    public void tesPublicHolidayBlankOwner() {
        Iterable<Holidays> holidayIterable = holidaysRepository.findAll(isCourtHoliday(" ",
            Date.valueOf(LocalDate.of(2023, 9,
                29))));
        ArrayList<Holidays> holidays = new ArrayList<>();
        holidayIterable.forEach(holidays::add);
        assertThat(holidays.size()).as("Expected one holiday dates").isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/Holiday.sql"})
    public void tesPublicHolidayEmptyOwner() {
        Iterable<Holidays> holidayIterable = holidaysRepository.findAll(isCourtHoliday("",
            Date.valueOf(LocalDate.of(2023, 9,
                29))));
        ArrayList<Holidays> holidays = new ArrayList<>();
        holidayIterable.forEach(holidays::add);
        assertThat(holidays.size()).as("Expected one holiday dates").isEqualTo(1);
    }
}
