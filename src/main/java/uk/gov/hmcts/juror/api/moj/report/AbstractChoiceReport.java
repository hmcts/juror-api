package uk.gov.hmcts.juror.api.moj.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.service.report.IReport;

import java.util.function.Function;

public class AbstractChoiceReport implements IReport {


    private final Function<StandardReportRequest, IReport> reportFunction;

    public AbstractChoiceReport(Function<StandardReportRequest, IReport> reportFunction) {
        this.reportFunction = reportFunction;
    }


    @Override
    public AbstractReportResponse<?> getStandardReportResponse(StandardReportRequest standardReportRequest) {
        return reportFunction.apply(standardReportRequest).getStandardReportResponse(standardReportRequest);
    }

    @Override
    public Class<?> getRequestValidatorClass(StandardReportRequest standardReportRequest) {
        return reportFunction.apply(standardReportRequest).getRequestValidatorClass(standardReportRequest);
    }
}
