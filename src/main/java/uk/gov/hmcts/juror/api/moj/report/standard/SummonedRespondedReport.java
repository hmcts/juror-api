package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SummonedRespondedReport extends AbstractStandardReport {

    @Autowired
    public SummonedRespondedReport(PoolRequestRepository poolRequestRepository) {
        super(
            poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTAL_ADDRESS,
            DataType.JUROR_POSTCODE,
            DataType.SUMMONED_RESPONDED);

        isBureauUserOnly();
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        query.where(QJurorPool.jurorPool.status.status.in(List.of(IJurorStatus.RESPONDED, IJurorStatus.SUMMONED)));
        query.orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }


    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new HashMap<>();
        map.put("pool_number", StandardReportResponse.DataTypeValue.builder()
            .displayName("Pool number")
            .dataType("String")
            .value(request.getPoolNumber())
            .build());
        return map;
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequirePoolNumber {
    }
}
