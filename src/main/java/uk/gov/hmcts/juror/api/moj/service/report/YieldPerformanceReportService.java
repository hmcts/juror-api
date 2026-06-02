package uk.gov.hmcts.juror.api.moj.service.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.request.YieldPerformanceReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse;

@FunctionalInterface
public interface YieldPerformanceReportService {

    YieldPerformanceReportResponse viewYieldPerformanceReport(
        YieldPerformanceReportRequest yieldPerformanceReportRequest);

}
