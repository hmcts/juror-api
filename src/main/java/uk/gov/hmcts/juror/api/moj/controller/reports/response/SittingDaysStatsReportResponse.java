package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.service.report.SittingDaysReportService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SittingDaysStatsReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;
    private TableData tableData;

    public SittingDaysStatsReportResponse(Map<String, AbstractReportResponse.DataTypeValue> reportHeadings) {
        this.headings = reportHeadings;
        this.tableData = new TableData(Arrays.stream(SittingDaysReportService.TableHeading.values())
            .map(heading -> TableData.Heading.builder()
                .id(heading)
                .name(heading.getDisplayName())
                .dataType(heading.getDataType())
                .build())
            .toList());
        this.tableData.setData(new ArrayList<>());
    }

    @Data
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Heading {
            private SittingDaysReportService.TableHeading id;
            private String name;
            private String dataType;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class DataRow {
            private String courtLocationNameAndCode;
            private int zeroSittingDays;
            private int oneSittingDay;
            private int twoSittingDays;
            private int threeSittingDays;
            private int fourSittingDays;
            private int fiveSittingDays;
            private int sixSittingDays;
            private int sevenSittingDays;
            private int eightSittingDays;
            private int nineSittingDays;
            private int tenSittingDays;
            private int elevenOrMoreSittingDays;
            private int totalJurors;
            private int totalSittingDays;
        }
    }
}
