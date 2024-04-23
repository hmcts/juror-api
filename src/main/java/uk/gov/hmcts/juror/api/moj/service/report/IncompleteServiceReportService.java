package uk.gov.hmcts.juror.api.moj.service.report;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;

import java.time.LocalDate;

@Component
public interface IncompleteServiceReportService {

    AbstractReportResponse<?> viewIncompleteServiceReport(String location, LocalDate cutOffDate);
}
