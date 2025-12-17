package uk.gov.hmcts.juror.api.moj.controller.managementdashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class WeekendAttendanceReportResponseDto {

    private List<WeekendAttendanceRecord> records;

    public WeekendAttendanceReportResponseDto() {
        records = new ArrayList<>();
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class WeekendAttendanceRecord {
        private String court;
        private Integer daysElapsed;
        private LocalDate reportLastRun;
        private Double utilisation;
    }

}
