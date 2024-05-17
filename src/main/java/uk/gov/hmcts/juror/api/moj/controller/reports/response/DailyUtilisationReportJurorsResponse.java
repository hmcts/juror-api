package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
            TableData.Heading.builder().id(TableData.TableHeading.JUROR)
                .name(TableData.TableHeading.JUROR.getDisplayName())
                .dataType(TableData.TableHeading.JUROR.getDataType()).build(),
            TableData.Heading.builder().id(TableData.TableHeading.JUROR_WORKING_DAYS)
                .name(TableData.TableHeading.JUROR_WORKING_DAYS.getDisplayName())
                .dataType(TableData.TableHeading.JUROR_WORKING_DAYS.getDataType()).build(),
            TableData.Heading.builder().id(TableData.TableHeading.SITTING_DAYS)
                .name(TableData.TableHeading.SITTING_DAYS.getDisplayName())
                .dataType(TableData.TableHeading.SITTING_DAYS.getDataType()).build(),
            TableData.Heading.builder().id(TableData.TableHeading.ATTENDANCE_DAYS)
                .name(TableData.TableHeading.ATTENDANCE_DAYS.getDisplayName())
                .dataType(TableData.TableHeading.ATTENDANCE_DAYS.getDataType()).build(),
            TableData.Heading.builder().id(TableData.TableHeading.NON_ATTENDANCE_DAYS)
                .name(TableData.TableHeading.NON_ATTENDANCE_DAYS.getDisplayName())
                .dataType(TableData.TableHeading.NON_ATTENDANCE_DAYS.getDataType()).build()
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
        @ToString
        @AllArgsConstructor
        public static class Heading {
            private TableHeading id;
            private String name;
            private String dataType;
        }


        @Data
        @Builder
        @NoArgsConstructor
        @ToString
        @AllArgsConstructor
        public static class Juror {
            private String juror;
            private int jurorWorkingDay;
            private int sittingDay;
            private int attendanceDay;
            private int nonAttendanceDay;

        }

        public enum TableHeading {
            JUROR("Juror", String.class.getSimpleName()),
            JUROR_WORKING_DAYS("Juror working day", Integer.class.getSimpleName()),
            SITTING_DAYS("Sitting day", Integer.class.getSimpleName()),
            ATTENDANCE_DAYS("Attendance day", Integer.class.getSimpleName()),
            NON_ATTENDANCE_DAYS("Non-attendance day",  Integer.class.getSimpleName());

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

}
