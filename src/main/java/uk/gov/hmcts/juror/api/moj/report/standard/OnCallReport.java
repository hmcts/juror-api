package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;

@Component
public class OnCallReport extends AbstractStandardReport {
    @Autowired
    public OnCallReport(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.MOBILE_PHONE,
            DataType.HOME_PHONE
        );

        isCourtUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()))
            .where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.getActiveOwner()))
            .where(QJurorPool.jurorPool.onCall.eq(true))
            .where(QJurorPool.jurorPool.isActive.eq(true))
            .orderBy(QJuror.juror.jurorNumber.asc());
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return OnCallReport.RequestValidator.class;
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> map = loadStandardPoolHeaders(request, true, false);
        map.put("total_on_call", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total on call")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().size())
            .build());
        return map;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequirePoolNumber {
    }
}