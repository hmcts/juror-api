package uk.gov.hmcts.juror.api.moj.controller.response.administration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayDateTest {

    @Test
    void positiveConstructorTest() {
        Holidays holidays = Holidays.builder()
            .holiday(LocalDate.now())
            .description("description321")
            .build();
        HolidayDate holidayDate = new HolidayDate(holidays);
        assertThat(holidayDate.getDate()).isEqualTo(holidays.getHoliday());
        assertThat(holidayDate.getDescription()).isEqualTo(holidays.getDescription());
    }
}
