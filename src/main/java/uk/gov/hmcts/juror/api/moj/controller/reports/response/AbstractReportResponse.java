package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.DeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.ExcusalLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.FailedToAttendLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.NonDeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PostponeLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.WithdrawalLetterData;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
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
    @AllArgsConstructor
    public static class TableData<T> {
        private List<Heading> headings;

        private T data;


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
