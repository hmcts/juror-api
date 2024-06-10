package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class YieldPerformanceReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;

    private YieldPerformanceReportResponse.TableData tableData;

    public YieldPerformanceReportResponse() {

        this.tableData = new YieldPerformanceReportResponse.TableData(List.of(
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.COURT)
                .name(TableHeading.COURT.getDisplayName())
                .dataType(TableHeading.COURT.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.REQUESTED)
                .name(TableHeading.REQUESTED.getDisplayName())
                .dataType(TableHeading.REQUESTED.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.CONFIRMED)
                .name(TableHeading.CONFIRMED.getDisplayName())
                .dataType(TableHeading.CONFIRMED.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.BALANCE)
                .name(TableHeading.BALANCE.getDisplayName())
                .dataType(TableHeading.BALANCE.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.DIFFERENCE)
                .name(TableHeading.DIFFERENCE.getDisplayName())
                .dataType(TableHeading.DIFFERENCE.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.COMMENTS)
                .name(TableHeading.COMMENTS.getDisplayName())
                .dataType(TableHeading.COMMENTS.getDataType()).build()
        ));
    }


    @Data
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    public static class TableData {
        private List<YieldPerformanceReportResponse.TableData.Heading> headings;
        private List<YieldData> data;

        public TableData(List<YieldPerformanceReportResponse.TableData.Heading> headings) {
            this.headings = headings;
            data = new ArrayList<>();
        }

        @Data
        @Builder
        @NoArgsConstructor
        @ToString
        @AllArgsConstructor
        public static class Heading {
            private YieldPerformanceReportResponse.TableHeading id;
            private String name;
            private String dataType;
        }

        @Data
        @NoArgsConstructor
        @ToString
        @AllArgsConstructor
        @Builder
        public static class YieldData {
            private String courtLocation;
            private int requested;
            private int confirmed;
            private int balance;
            private double difference;
            private String comments;
        }
    }

    public enum TableHeading {
        COURT("Court", String.class.getSimpleName()),
        REQUESTED("Requested", Integer.class.getSimpleName()),
        CONFIRMED("Confirmed", Integer.class.getSimpleName()),
        BALANCE("Balance", Integer.class.getSimpleName()),
        DIFFERENCE("Difference", Double.class.getSimpleName()),
        COMMENTS("Comments", String.class.getSimpleName());

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