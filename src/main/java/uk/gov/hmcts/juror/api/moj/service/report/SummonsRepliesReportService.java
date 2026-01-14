package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.ResponsesCompletedReportResponse;

import java.time.LocalDate;

public interface SummonsRepliesReportService {

    DigitalSummonsRepliesReportResponse getDigitalSummonsRepliesReport(LocalDate month);

    ResponsesCompletedReportResponse getResponsesCompletedReport(LocalDate monthStartDate);

    String getResponsesCompletedReportCsv(LocalDate monthStartDate);

    enum TableHeading {
        DATE("Date", LocalDate.class.getSimpleName()),
        NO_OF_REPLIES("No of replies received",  Integer.class.getSimpleName());

        private final String displayName;
        private final String dataType;

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

    @Builder
    @Getter
    public static class CompletedResponseRecord {
        private String staffName;
        private LocalDate date;
        private Integer completedResponses;

        public CompletedResponseRecord(String staffName, LocalDate dayOfMonth, Integer completedResponses) {
            this.staffName = staffName;
            this.date = dayOfMonth;
            this.completedResponses = completedResponses;
        }
    }
}
