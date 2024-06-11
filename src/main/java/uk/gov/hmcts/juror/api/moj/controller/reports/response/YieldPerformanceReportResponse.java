package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class YieldPerformanceReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;

    private YieldPerformanceReportResponse.TableData tableData;

    public YieldPerformanceReportResponse() {

        this.tableData = new YieldPerformanceReportResponse.TableData(List.of(
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.COURT.getId())
                .name(TableHeading.COURT.getDisplayName())
                .dataType(TableHeading.COURT.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.REQUESTED.getId())
                .name(TableHeading.REQUESTED.getDisplayName())
                .dataType(TableHeading.REQUESTED.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.CONFIRMED.getId())
                .name(TableHeading.CONFIRMED.getDisplayName())
                .dataType(TableHeading.CONFIRMED.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.BALANCE.getId())
                .name(TableHeading.BALANCE.getDisplayName())
                .dataType(TableHeading.BALANCE.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.DIFFERENCE.getId())
                .name(TableHeading.DIFFERENCE.getDisplayName())
                .dataType(TableHeading.DIFFERENCE.getDataType()).build(),
            YieldPerformanceReportResponse.TableData.Heading.builder().id(TableHeading.COMMENTS.getId())
                .name(TableHeading.COMMENTS.getDisplayName())
                .dataType(TableHeading.COMMENTS.getDataType()).build()
        ));
    }


    @Data
    @NoArgsConstructor
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
        @AllArgsConstructor
        public static class Heading {
            private String id;
            private String name;
            private String dataType;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class YieldData {
            private String court;
            private int requested;
            private int confirmed;
            private int balance;
            private double difference;
            private String comments;
        }
    }

    public enum TableHeading {
        COURT("court", "Court", String.class.getSimpleName()),
        REQUESTED("requested", "Requested", Integer.class.getSimpleName()),
        CONFIRMED("confirmed", "Confirmed", Integer.class.getSimpleName()),
        BALANCE("balance", "Balance",Integer.class.getSimpleName()),
        DIFFERENCE("difference", "Difference", Double.class.getSimpleName()),
        COMMENTS("comments", "Comments", String.class.getSimpleName());

        private String id;
        private String displayName;
        private String dataType;

        TableHeading(String id, String displayName, String dataType) {
            this.id = id;
            this.displayName = displayName;
            this.dataType = dataType;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDataType() {
            return dataType;
        }
    }

}