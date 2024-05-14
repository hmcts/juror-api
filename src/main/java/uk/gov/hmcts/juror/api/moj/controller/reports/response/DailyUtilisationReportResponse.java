package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyUtilisationReportResponse {

    private List<Heading> reportHeadings;
    private TableData tableData;

    public DailyUtilisationReportResponse(List<Heading> reportHeadings) {
        this.reportHeadings = reportHeadings;
        this.tableData = new TableData();
        this.tableData.setHeadings(List.of(
            TableData.Heading.builder().name(TableData.TableHeading.DATE).displayName(TableData.TableHeading.DATE.getDisplayName()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.JUROR_WORKING_DAYS).displayName(TableData.TableHeading.JUROR_WORKING_DAYS.getDisplayName()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.SITTING_DAYS).displayName(TableData.TableHeading.SITTING_DAYS.getDisplayName()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.ATTENDANCE_DAYS).displayName(TableData.TableHeading.ATTENDANCE_DAYS.getDisplayName()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.NON_ATTENDANCE_DAYS).displayName(TableData.TableHeading.NON_ATTENDANCE_DAYS.getDisplayName()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.UTILISATION).displayName(TableData.TableHeading.UTILISATION.getDisplayName()).build()
        ));
        this.tableData.setWeeks(new ArrayList<>());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Heading {
        private ReportHeading name;
        private String displayName;
        private Object value;
    }

    @Data
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    public static class TableData {
        private List<Heading> headings;

        private List<Week> weeks;

        @Data
        @Builder
        @NoArgsConstructor
        @ToString
        @AllArgsConstructor
        public static class Heading {
            private TableHeading name;
            private String displayName;
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

        private int overallTotalJurorWorkingDays;
        private int overallTotalSittingDays;
        private int overallTotalAttendanceDays;
        private int overallTotalNonAttendanceDays;
        private double overallTotalUtilisation;

        public enum TableHeading {
            DATE("Date"),
            JUROR_WORKING_DAYS("Juror Working Days"),
            SITTING_DAYS("Sitting Days"),
            ATTENDANCE_DAYS("Attendance Days"),
            NON_ATTENDANCE_DAYS("Non-Attendance Days"),
            UTILISATION("Utilisation");

            private String displayName;

            TableHeading(String displayName) {
                this.displayName = displayName;
            }

            public String getDisplayName() {
                return displayName;
            }

        }
    }

    public enum ReportHeading {
        DATE_FROM("Date from"),
        DATE_TO("Date to"),
        REPORT_CREATED("Report created"),
        TIME_CREATED("Time created"),
        COURT_NAME("Court name");

        private String displayName;

        ReportHeading(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

}
