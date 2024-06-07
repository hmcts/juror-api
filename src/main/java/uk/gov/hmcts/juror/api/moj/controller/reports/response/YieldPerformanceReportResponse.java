package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class YieldPerformanceReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;

    private List<YieldPerformanceData> yieldPerformanceData;

    @Builder
    public static class YieldPerformanceData {
        private String courtLocation;
        private int requested;
        private int confirmed;
        private int balance;
        private double difference;
        private String comments;
    }

}