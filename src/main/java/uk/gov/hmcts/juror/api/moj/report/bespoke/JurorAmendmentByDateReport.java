package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.service.JurorServiceMod;
import uk.gov.hmcts.juror.api.moj.service.UserService;
import uk.gov.hmcts.juror.api.moj.service.audit.JurorAuditService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JurorAmendmentByDateReport extends AbstractJurorAmendmentReport {

    @Autowired
    public JurorAmendmentByDateReport(
        JurorAuditService jurorAuditService,
        JurorServiceMod jurorService,
        UserService userService) {
        super(jurorAuditService, jurorService, userService);
    }

    @Override
    @Transactional(readOnly = true)
    public JurorAmendmentReportResponse getStandardReportResponse(
        StandardReportRequest request) {
        JurorAmendmentReportResponse response = new JurorAmendmentReportResponse();

        SecurityUtil.validateCanAccessRole(Role.SENIOR_JUROR_OFFICER);

        response.setTableData(getTableDataAudits(
            jurorAuditService
                .getAllAuditsChangedBetweenAndHasCourt(request.getFromDate(), request.getToDate(),
                    SecurityUtil.getCourts())
        ));
        response.setHeadings(getHeadings(request));
        return response;
    }

    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request) {
        Map<String, AbstractReportResponse.DataTypeValue> headings =
            new ConcurrentHashMap<>();
        headings.put("report_created", AbstractReportResponse.DataTypeValue.builder()
            .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
            .dataType(LocalDateTime.class.getSimpleName())
            .build());

        headings.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .dataType(LocalDate.class.getSimpleName())
            .build());

        headings.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .dataType(LocalDate.class.getSimpleName())
            .build());

        return headings;

    }


    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {

    }
}
