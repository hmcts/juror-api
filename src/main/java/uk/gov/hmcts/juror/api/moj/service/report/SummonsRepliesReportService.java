package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.ResponsesCompletedReportResponse;

import java.time.LocalDate;

public interface SummonsRepliesReportService {

    DigitalSummonsRepliesReportResponse getDigitalSummonsRepliesReport(LocalDate month);

    ResponsesCompletedReportResponse getResponsesCompletedReport(LocalDate monthStartDate);

    @Getter
    enum TableHeading {
        DATE("Date", LocalDate.class.getSimpleName()),
        NO_OF_REPLIES("No of replies received",  Integer.class.getSimpleName());

        private final String displayName;
        private final String dataType;

        TableHeading(String displayName, String dataType) {
            this.displayName = displayName;
            this.dataType = dataType;
        }

    }

    record CompletedResponseRecord(String staffName, LocalDate date, Integer completedResponses) {}
}
