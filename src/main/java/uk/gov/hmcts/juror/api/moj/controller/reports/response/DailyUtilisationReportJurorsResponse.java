package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyUtilisationReportJurorsResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;
    private TableData tableData;

    public DailyUtilisationReportJurorsResponse(Map<String, AbstractReportResponse.DataTypeValue> reportHeadings) {
        this.headings = reportHeadings;
        this.tableData = new TableData(List.of(
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.JUROR)
                .name(UtilisationReportService.TableHeading.JUROR.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.JUROR.getDataType()).build(),
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.JUROR_WORKING_DAYS)
                .name(UtilisationReportService.TableHeading.JUROR_WORKING_DAYS.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.JUROR_WORKING_DAYS.getDataType()).build(),
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.SITTING_DAYS)
                .name(UtilisationReportService.TableHeading.SITTING_DAYS.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.SITTING_DAYS.getDataType()).build(),
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.ATTENDANCE_DAYS)
                .name(UtilisationReportService.TableHeading.ATTENDANCE_DAYS.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.ATTENDANCE_DAYS.getDataType()).build(),
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.NON_ATTENDANCE_DAYS)
                .name(UtilisationReportService.TableHeading.NON_ATTENDANCE_DAYS.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.NON_ATTENDANCE_DAYS.getDataType()).build()
        ));
        this.tableData.setJurors(new ArrayList<>());
    }

    @Data
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    public static class TableData {
        private List<Heading> headings;
        private int totalJurorWorkingDays;
        private int totalSittingDays;
        private int totalAttendanceDays;
        private int totalNonAttendanceDays;

        private List<Juror> jurors;

        public TableData(List<Heading> headings) {
            this.headings = headings;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Heading {
            private UtilisationReportService.TableHeading id;
            private String name;
            private String dataType;
        }


        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Juror {
            private String juror;
            private int jurorWorkingDay;
            private int sittingDay;
            private int attendanceDay;
            private int nonAttendanceDay;

        }
    }

}
