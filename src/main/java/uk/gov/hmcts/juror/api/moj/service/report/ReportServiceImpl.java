package uk.gov.hmcts.juror.api.moj.service.report;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private final Map<String, AbstractReport> reports;

    @Autowired
    public ReportServiceImpl(List<AbstractReport> reports) {
        this.reports = reports.stream()
            .collect(Collectors.toMap(AbstractReport::getName, report -> report));
        if (this.reports.size() != reports.size()) {
            throw new MojException.InternalServerError("Duplicate report names found", null);
        }
    }

    @Override
    public StandardReportResponse viewStandardReport(StandardReportRequest standardReportRequest) {
        AbstractReport abstractReport = reports.get(standardReportRequest.getReportType());
        if (abstractReport == null) {
            throw new MojException.NotFound("Report not found", null);
        }
        return abstractReport.getStandardReportResponse(standardReportRequest);
    }
}
