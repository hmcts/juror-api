package uk.gov.hmcts.juror.api.moj.service.report;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private final Map<String, AbstractReport> reports;
    private final Validator validator;

    @Autowired
    public ReportServiceImpl(List<AbstractReport> reports, Validator validator) {
        this.validator = validator;
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
        validate(standardReportRequest, abstractReport);
        return abstractReport.getStandardReportResponse(standardReportRequest);
    }

    private void validate(StandardReportRequest request, AbstractReport abstractReport) {
        Set<ConstraintViolation<StandardReportRequest>> violations =
            validator.validate(request, abstractReport.getRequestValidatorClass());
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
