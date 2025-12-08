package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService;
import uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WeekendAttendanceReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;
    private TableData tableData;

    public WeekendAttendanceReportResponse(Map<String, AbstractReportResponse.DataTypeValue> reportHeadings) {
        this.headings = reportHeadings;
        this.tableData = new TableData(List.of(
            TableData.Heading.builder().id(AttendanceReportService.TableHeading.COURT_LOCATION_NAME_AND_CODE)
                .name(AttendanceReportService.TableHeading.COURT_LOCATION_NAME_AND_CODE.getDisplayName())
                .dataType(AttendanceReportService.TableHeading.COURT_LOCATION_NAME_AND_CODE.getDataType()).build(),
            TableData.Heading.builder().id(AttendanceReportService.TableHeading.SATURDAY_TOTAL)
                .name(AttendanceReportService.TableHeading.SATURDAY_TOTAL.getDisplayName())
                .dataType(AttendanceReportService.TableHeading.SATURDAY_TOTAL.getDataType()).build(),
            TableData.Heading.builder().id(AttendanceReportService.TableHeading.SUNDAY_TOTAL)
                .name(AttendanceReportService.TableHeading.SUNDAY_TOTAL.getDisplayName())
                .dataType(AttendanceReportService.TableHeading.SUNDAY_TOTAL.getDataType()).build(),
            TableData.Heading.builder().id(AttendanceReportService.TableHeading.HOLIDAY_TOTAL)
                .name(AttendanceReportService.TableHeading.HOLIDAY_TOTAL.getDisplayName())
                .dataType(AttendanceReportService.TableHeading.HOLIDAY_TOTAL.getDataType()).build(),
            TableData.Heading.builder().id(AttendanceReportService.TableHeading.TOTAL_PAID)
                .name(AttendanceReportService.TableHeading.TOTAL_PAID.getDisplayName())
                .dataType(AttendanceReportService.TableHeading.TOTAL_PAID.getDataType()).build()
        ));
        this.tableData.setData(new ArrayList<>());
    }

    @Data
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    public static class TableData {
        private List<Heading> headings;
        private List<DataRow> data;

        public TableData(List<Heading> headings) {
            this.headings = headings;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Heading {
            private AttendanceReportServiceImpl.TableHeading id;
            private String name;
            private String dataType;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DataRow {
            private String courtLocationNameAndCode;
            private Integer saturdayTotal;
            private Integer sundayTotal;
            private Integer holidayTotal;
            private BigDecimal totalPaid;
        }
    }

}
