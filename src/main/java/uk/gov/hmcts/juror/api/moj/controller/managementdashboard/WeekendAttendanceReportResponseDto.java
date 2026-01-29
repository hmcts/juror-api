package uk.gov.hmcts.juror.api.moj.controller.managementdashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
        private String courtLocationNameAndCode;
        private Integer saturdayTotal;
        private Integer sundayTotal;
        private Integer holidayTotal;
        private BigDecimal totalPaid;
    }

}
