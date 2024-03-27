package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class UndeliverableListReport extends AbstractStandardReport {

    @Autowired
    public UndeliverableListReport(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTAL_ADDRESS);

        isBureauUserOnly();
    }

    @Override
    public Class<?> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.UNDELIVERABLE))
            .orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = loadStandardPoolHeaders(request, true, true);
        map.put("total_undelivered", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total undelivered")
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