package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.service.report.SummonsRepliesReportService;
import uk.gov.hmcts.juror.api.moj.service.report.SummonsRepliesReportServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DigitalSummonsRepliesReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;
    private TableData tableData;

    public DigitalSummonsRepliesReportResponse(Map<String, AbstractReportResponse.DataTypeValue> reportHeadings) {
        this.headings = reportHeadings;
        this.tableData = new TableData(List.of(
            TableData.Heading.builder().id(SummonsRepliesReportService.TableHeading.DATE)
                .name(SummonsRepliesReportService.TableHeading.DATE.getDisplayName())
                .dataType(SummonsRepliesReportService.TableHeading.DATE.getDataType()).build(),
            TableData.Heading.builder().id(SummonsRepliesReportService.TableHeading.NO_OF_REPLIES)
                .name(SummonsRepliesReportService.TableHeading.NO_OF_REPLIES.getDisplayName())
                .dataType(SummonsRepliesReportService.TableHeading.NO_OF_REPLIES.getDataType()).build()
        ));
        this.tableData.setData(new ArrayList<>());
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
            private SummonsRepliesReportServiceImpl.TableHeading id;
            private String name;
            private String dataType;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DataRow {
            private LocalDate date;
            private Integer noOfReplies;
        }
    }

}
