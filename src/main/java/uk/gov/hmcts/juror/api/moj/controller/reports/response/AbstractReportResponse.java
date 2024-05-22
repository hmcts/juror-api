package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.report.IDataType;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = GroupedReportResponse.class, name = "Standard Response"),
    @JsonSubTypes.Type(value = StandardReportResponse.class, name = "Grouped Response")
})
public class AbstractReportResponse<T> {
    private Map<String, DataTypeValue> headings;
    private TableData<T> tableData;

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
    @ToString
    @AllArgsConstructor
    public static class TableData<T> {
        private List<Heading> headings;
        private T data;

        public void removeData(IDataType... dataTypes) {
            if (data instanceof StandardTableData standardTableData) {
                for (IDataType dataType : dataTypes) {
                    headings.removeIf(heading -> dataType.getId().equals(heading.getId()));
                }
                standardTableData.removeDataTypes(dataTypes);
            }
            throw new UnsupportedOperationException("This operation curretnly only supports StandardTableData");

        }

        @Data
        @Builder
        @NoArgsConstructor
        @ToString
        @AllArgsConstructor
        public static class Heading {
            private String id;
            private String name;
            private String dataType;
            private List<Heading> headings;
        }
    }
}
