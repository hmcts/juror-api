package uk.gov.hmcts.juror.api.moj.service.report;


import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;

public interface JurySummoningMonitorReportService {

    JurySummoningMonitorReportResponse viewJurySummoningMonitorReport(
        JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest);

}
