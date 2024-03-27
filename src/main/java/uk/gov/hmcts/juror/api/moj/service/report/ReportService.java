package uk.gov.hmcts.juror.api.moj.service.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;

public interface ReportService {
    AbstractReportResponse<?> viewStandardReport(StandardReportRequest standardReportRequest);
}
