package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.time.LocalTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DailyExpenseTime {
    @NotNull(groups = {DailyExpense.AttendanceDay.class, DailyExpense.NonAttendanceDay.class})
    @JsonProperty("pay_attendance")
    private PayAttendanceType payAttendance;

    @Null(groups = {DailyExpense.NonAttendanceDay.class})
    @JsonProperty("travel_time")
    private LocalTime travelTime;
}
