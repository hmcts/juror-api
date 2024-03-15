package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StandardReportResponse {


    private Map<String, DataTypeValue> headings;


    private TableData tableData;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataTypeValue {
        private String displayName;
        private String dataType;
        private Object value;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableData {
        private List<Heading> headings;

        private List<LinkedHashMap<String, Object>> data;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Heading {
            private String id;
            private String name;
            private String dataType;
            private List<Heading> headings;
        }
    }

}
