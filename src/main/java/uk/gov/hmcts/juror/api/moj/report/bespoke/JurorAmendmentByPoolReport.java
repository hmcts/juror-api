package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;
import uk.gov.hmcts.juror.api.moj.service.JurorServiceMod;
import uk.gov.hmcts.juror.api.moj.service.UserService;
import uk.gov.hmcts.juror.api.moj.service.audit.JurorAuditService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JurorAmendmentByPoolReport extends AbstractJurorAmendmentReport {

    private final JurorPoolService jurorPoolService;

    @Autowired
    public JurorAmendmentByPoolReport(
        JurorPoolService jurorPoolService,
        JurorAuditService jurorAuditService,
        JurorServiceMod jurorService,
        UserService userService) {
        super(jurorAuditService, jurorService, userService);
        this.jurorPoolService = jurorPoolService;
    }

    @Override
    @Transactional(readOnly = true)
    public JurorAmendmentReportResponse getStandardReportResponse(
        StandardReportRequest request) {
        JurorAmendmentReportResponse response = new JurorAmendmentReportResponse();

        SecurityUtil.validateCanAccessRole(Role.SENIOR_JUROR_OFFICER);
        PoolRequest poolRequest = jurorPoolService.getPoolRequest(request.getPoolNumber());
        SecurityUtil.validateCanAccessOwner(poolRequest.getOwner());

        response.setTableData(getTableData(
            poolRequest.getJurorPools()
                .stream()
                .map(JurorPool::getJurorNumber)
                .toList()
        ));
        response.setHeadings(getHeadings(request, poolRequest));
        return response;
    }

    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request, PoolRequest poolRequest) {
        Map<String, AbstractReportResponse.DataTypeValue> headings =
            new ConcurrentHashMap<>();
        headings.put("report_created", AbstractReportResponse.DataTypeValue.builder()
            .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
            .dataType(LocalDateTime.class.getSimpleName())
            .build());

        headings.put("pool_number", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Pool Number")
            .value(request.getPoolNumber())
            .dataType(String.class.getSimpleName())
            .build());

        headings.put("pool_type", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Pool type")
            .value(poolRequest.getPoolType().getDescription())
            .dataType(String.class.getSimpleName())
            .build());

        headings.put("service_start_date", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Service Start Date")
            .value(DateTimeFormatter.ISO_DATE_TIME.format(poolRequest.getReturnDate()))
            .dataType(LocalDate.class.getSimpleName())
            .build());

        headings.put("court_name", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .value(poolRequest.getCourtLocation().getNameWithLocCode())
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
        AbstractReport.Validators.RequirePoolNumber {

    }
}
