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
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyUtilisationReportResponse {

    private List<Heading> headings;
    private TableData tableData;

    public DailyUtilisationReportResponse(List<Heading> reportHeadings) {
        this.headings = reportHeadings;
        this.tableData = new TableData(List.of(
            TableData.Heading.builder().name(TableData.TableHeading.DATE)
                .displayName(TableData.TableHeading.DATE.getDisplayName())
                .dataType(TableData.TableHeading.DATE.getDataType()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.JUROR_WORKING_DAYS)
                .displayName(TableData.TableHeading.JUROR_WORKING_DAYS.getDisplayName())
                .dataType(TableData.TableHeading.JUROR_WORKING_DAYS.getDataType()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.SITTING_DAYS)
                .displayName(TableData.TableHeading.SITTING_DAYS.getDisplayName())
                .dataType(TableData.TableHeading.SITTING_DAYS.getDataType()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.ATTENDANCE_DAYS)
                .displayName(TableData.TableHeading.ATTENDANCE_DAYS.getDisplayName())
                .dataType(TableData.TableHeading.ATTENDANCE_DAYS.getDataType()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.NON_ATTENDANCE_DAYS)
                .displayName(TableData.TableHeading.NON_ATTENDANCE_DAYS.getDisplayName())
                .dataType(TableData.TableHeading.NON_ATTENDANCE_DAYS.getDataType()).build(),
            TableData.Heading.builder().name(TableData.TableHeading.UTILISATION)
                .displayName(TableData.TableHeading.UTILISATION.getDisplayName())
                .dataType(TableData.TableHeading.UTILISATION.getDataType()).build()
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
        private String dataType;
        private Object value;
    }

    @Data
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    public static class TableData {
        private List<Heading> headings;

        private List<Week> weeks;

        public TableData(List<Heading> headings) {
            this.headings = headings;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @ToString
        @AllArgsConstructor
        public static class Heading {
            private TableHeading name;
            private String displayName;
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

        private int overallTotalJurorWorkingDays;
        private int overallTotalSittingDays;
        private int overallTotalAttendanceDays;
        private int overallTotalNonAttendanceDays;
        private double overallTotalUtilisation;

        public enum TableHeading {
            DATE("Date", LocalDate.class.getSimpleName()),
            JUROR_WORKING_DAYS("Juror Working Days", Integer.class.getSimpleName()),
            SITTING_DAYS("Sitting Days", Integer.class.getSimpleName()),
            ATTENDANCE_DAYS("Attendance Days", Integer.class.getSimpleName()),
            NON_ATTENDANCE_DAYS("Non-Attendance Days",  Integer.class.getSimpleName()),
            UTILISATION("Utilisation", Double.class.getSimpleName());

            private String displayName;
            private String dataType;

            TableHeading(String displayName, String dataType) {
                this.displayName = displayName;
                this.dataType = dataType;
            }

            public String getDisplayName() {
                return displayName;
            }

            public String getDataType() {
                return dataType;
            }

        }
    }

    public enum ReportHeading {
        DATE_FROM("Date from", LocalDate.class.getSimpleName()),
        DATE_TO("Date to", LocalDate.class.getSimpleName()),
        REPORT_CREATED("Report created", LocalDate.class.getSimpleName()),
        TIME_CREATED("Time created", LocalDate.class.getSimpleName()),
        COURT_NAME("Court name", String.class.getSimpleName());

        private String displayName;

        private String dataType;

        ReportHeading(String displayName, String dataType) {
            this.displayName = displayName;
            this.dataType = dataType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDataType() {
            return dataType;
        }
    }

}
