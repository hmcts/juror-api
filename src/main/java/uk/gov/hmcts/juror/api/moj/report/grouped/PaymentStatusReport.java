package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QPaymentData;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.datatypes.PaymentStatusDataTypes;
import uk.gov.hmcts.juror.api.moj.report.grouped.groupby.GroupByPaymentStatus;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentStatusReport extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;

    public PaymentStatusReport(CourtLocationService courtLocationService) {
        super(QPaymentData.paymentData,
            new GroupByPaymentStatus(),
            PaymentStatusDataTypes.CREATION_DATE,
            PaymentStatusDataTypes.TOTAL_AMOUNT,
            PaymentStatusDataTypes.PAYMENTS,
            PaymentStatusDataTypes.CONSOLIDATED_FILE_REFERENCE);
        this.courtLocationService = courtLocationService;
        isCourtUserOnly();
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QPaymentData.paymentData.courtLocation.locCode.eq(SecurityUtil.getLocCode()));
        addGroupBy(
            query,
            PaymentStatusDataTypes.CREATION_DATE,
            PaymentStatusDataTypes.CONSOLIDATED_FILE_REFERENCE,
            PaymentStatusDataTypes.EXTRACTED
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                                                         AbstractReportResponse.TableData<GroupedTableData> tableData) {
        Map<String, AbstractReportResponse.DataTypeValue> headers = new HashMap<>();
        addCourtNameHeader(headers, courtLocationService.getCourtLocation(SecurityUtil.getLocCode()));
        return headers;
    }


    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator {
    }
}
