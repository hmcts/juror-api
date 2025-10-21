package uk.gov.hmcts.juror.api.moj.service.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;

import java.time.LocalDate;

public interface SummonsRepliesReportService {

    DigitalSummonsRepliesReportResponse getDigitalSummonsRepliesReport(LocalDate month);

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
}
