package uk.gov.hmcts.juror.api.moj.service.report;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.ValidationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private final Map<String, IReport> reports;
    private final ValidationService validateService;

    @Autowired
    public ReportServiceImpl(List<IReport> reports, ValidationService validateService) {
        this.reports = reports.stream()
            .collect(Collectors.toMap(IReport::getName, report -> report));
        this.validateService = validateService;
        if (this.reports.size() != reports.size()) {
            throw new MojException.InternalServerError("Duplicate report names found", null);
        }
    }

    @Override
    public AbstractReportResponse<?> viewStandardReport(StandardReportRequest standardReportRequest) {
        IReport abstractReport = reports.get(standardReportRequest.getReportType());
        if (abstractReport == null) {
            throw new MojException.NotFound("Report not found", null);
        }
        validateService.validate(standardReportRequest, abstractReport.getRequestValidatorClass());
        return abstractReport.getStandardReportResponse(standardReportRequest);
    }
}
