package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.service.JurorServiceMod;
import uk.gov.hmcts.juror.api.moj.service.UserService;
import uk.gov.hmcts.juror.api.moj.service.audit.JurorAuditService;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JurorAmendmentByJurorReport extends AbstractJurorAmendmentReport {


    @Autowired
    public JurorAmendmentByJurorReport(JurorAuditService jurorAuditService,
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
        Juror juror = jurorService.getJurorFromJurorNumber(request.getJurorNumber());
        JurorUtils.checkOwnershipForCurrentUser(juror, SecurityUtil.getActiveOwner());
        AbstractReportResponse.TableData<List<JurorAmendmentReportRow>> tableData =
            getTableData(List.of(request.getJurorNumber()));

        tableData.getData().forEach(jurorAmendmentReportRow -> {
            jurorAmendmentReportRow.setFrom(null);
            jurorAmendmentReportRow.setJurorNumber(null);
        });
        tableData.getHeadings().removeIf(heading ->
            heading.getId().equalsIgnoreCase("juror_number")
                || heading.getId().equalsIgnoreCase("from"));
        response.setTableData(tableData);
        response.setHeadings(getHeadings(request, juror.getName()));
        return response;
    }

    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request, String jurorName) {
        Map<String, AbstractReportResponse.DataTypeValue> headings =
            new ConcurrentHashMap<>();
        headings.put("report_created", AbstractReportResponse.DataTypeValue.builder()
            .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
            .dataType(LocalDateTime.class.getSimpleName())
            .build());

        headings.put("juror_number", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Juror Number")
            .value(request.getJurorNumber())
            .dataType(String.class.getSimpleName())
            .build());

        headings.put("juror_name", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Juror Name")
            .value(jurorName)
            .dataType(String.class.getSimpleName())
            .build());

        return headings;

    }


    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequiredJurorNumber {

    }
}
