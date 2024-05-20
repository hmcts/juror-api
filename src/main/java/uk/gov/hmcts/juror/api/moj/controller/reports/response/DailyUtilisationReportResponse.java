package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyUtilisationReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;
    private TableData tableData;

    public DailyUtilisationReportResponse(Map<String, AbstractReportResponse.DataTypeValue> reportHeadings) {
        this.headings = reportHeadings;
        this.tableData = new TableData(List.of(
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.DATE)
                .name(UtilisationReportService.TableHeading.DATE.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.DATE.getDataType()).build(),
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
                .dataType(UtilisationReportService.TableHeading.NON_ATTENDANCE_DAYS.getDataType()).build(),
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.UTILISATION)
                .name(UtilisationReportService.TableHeading.UTILISATION.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.UTILISATION.getDataType()).build()
        ));
        this.tableData.setWeeks(new ArrayList<>());
    }

    @Data
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    public static class TableData {
        private List<Heading> headings;
        private List<Week> weeks;
        private int overallTotalJurorWorkingDays;
        private int overallTotalSittingDays;
        private int overallTotalAttendanceDays;
        private int overallTotalNonAttendanceDays;
        private double overallTotalUtilisation;

        public TableData(List<Heading> headings) {
            this.headings = headings;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @ToString
        @AllArgsConstructor
        public static class Heading {
            private UtilisationReportService.TableHeading id;
            private String name;
            private String dataType;
        }

        @Data
        @NoArgsConstructor
        @ToString
        @AllArgsConstructor
        public static class Week {
            /**
             * A week is usually from Monday to Friday but could include weekends as well.
             * */
            private List<Day> days;
            private int weeklyTotalJurorWorkingDays;
            private int weeklyTotalSittingDays;
            private int weeklyTotalAttendanceDays;
            private int weeklyTotalNonAttendanceDays;
            private double weeklyTotalUtilisation;

            @Data
            @Builder
            @NoArgsConstructor
            @ToString
            @AllArgsConstructor
            public static class Day {
                private LocalDate date;
                private int jurorWorkingDays;
                private int sittingDays;
                private int attendanceDays;
                private int nonAttendanceDays;
                private double utilisation;
            }
        }
    }
}
