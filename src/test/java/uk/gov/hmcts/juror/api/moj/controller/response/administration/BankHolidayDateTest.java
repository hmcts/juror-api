package uk.gov.hmcts.juror.api.moj.controller.response.administration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class BankHolidayDateTest {

    @Test
    void positiveConstructorTest() {
        Holidays holidays = Holidays.builder()
            .holiday(LocalDate.now())
            .description("description321")
            .build();
        BankHolidayDate bankHolidayDate = new BankHolidayDate(holidays);
        assertThat(bankHolidayDate.getDate()).isEqualTo(holidays.getHoliday());
        assertThat(bankHolidayDate.getDescription()).isEqualTo(holidays.getDescription());
    }
}
