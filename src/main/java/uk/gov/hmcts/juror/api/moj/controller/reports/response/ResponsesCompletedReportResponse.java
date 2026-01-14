package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResponsesCompletedReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;
    private TableData tableData;

    public ResponsesCompletedReportResponse(Map<String, AbstractReportResponse.DataTypeValue> reportHeadings) {
        this.headings = reportHeadings;
        // will need to dynamically build table headings based on the month selected
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
            private int id;
            private String name;
            private String dataType;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DataRow {
            private String staffName;
            private List<Integer> dailyTotals; // list of totals for each day in the month
            private Integer staffTotal;
        }
    }

}
