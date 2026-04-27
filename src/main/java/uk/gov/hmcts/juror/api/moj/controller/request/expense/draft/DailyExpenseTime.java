package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyExpenseTime {
    @NotNull(groups = {
        DailyExpense.AttendanceDay.class,
        DailyExpense.NonAttendanceDay.class,
        DailyExpense.EditDay.class
    })
    private PayAttendanceType payAttendance;

    @Null(groups = {DailyExpense.NonAttendanceDay.class})
    @JsonFormat(pattern = "HH:mm")
    private LocalTime travelTime;
}
