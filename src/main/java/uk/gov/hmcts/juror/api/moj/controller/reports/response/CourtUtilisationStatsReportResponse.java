package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtUtilisationStatsReportResponse {

    private Map<String, AbstractReportResponse.DataTypeValue> headings;
    private TableData tableData;

    public CourtUtilisationStatsReportResponse(Map<String, AbstractReportResponse.DataTypeValue> reportHeadings) {
        this.headings = reportHeadings;
        this.tableData = new TableData(List.of(
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.COURT_NAME)
                .name(UtilisationReportService.TableHeading.COURT_NAME.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.COURT_NAME.getDataType()).build(),
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.UTILISATION)
                .name(UtilisationReportService.TableHeading.UTILISATION.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.UTILISATION.getDataType()).build(),
            TableData.Heading.builder().id(UtilisationReportService.TableHeading.DATE_LAST_RUN)
                .name(UtilisationReportService.TableHeading.DATE_LAST_RUN.getDisplayName())
                .dataType(UtilisationReportService.TableHeading.DATE_LAST_RUN.getDataType()).build()
        ));
    }

    @Data
    @NoArgsConstructor
    public static class TableData {
        private List<Heading> headings;

        public TableData(List<Heading> headings) {
            this.headings = headings;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Heading {
            private UtilisationReportService.TableHeading id;
            private String name;
            private String dataType;
        }
    }

}
