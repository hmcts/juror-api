package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.JoinType;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.support.AvailableListReport;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Map;

@Component
public class AvailableListByPoolReport extends AbstractStandardReport implements AvailableListReport {


    @Autowired
    public AvailableListByPoolReport(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.STATUS,
            DataType.JUROR_REASONABLE_ADJUSTMENT_WITH_MESSAGE);

        addJoinOverride(
            JoinOverrideDetails.builder()
                .from(QJurorPool.jurorPool)
                .joinType(JoinType.LEFTJOIN)
                .to(QReasonableAdjustments.reasonableAdjustments)
                .build()
        );
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        addStandardFilters(query, request);
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        query.orderBy(
            QJurorPool.jurorPool.pool.poolNumber.asc()
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> headings = loadStandardPoolHeaders(request, true, true);
        headings.putAll(getHeadingsInternal(request, tableData));
        return headings;
    }


    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AvailableListReport.RequestValidator,
        AbstractReport.Validators.RequirePoolNumber {
    }

}
