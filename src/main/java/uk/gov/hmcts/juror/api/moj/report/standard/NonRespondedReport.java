package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Map;




@Component
public class NonRespondedReport extends AbstractReport {
    @Autowired
    public NonRespondedReport(PoolRequestRepository poolRequestRepository) {
        super(
            poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.MOBILE_PHONE,
            DataType.HOME_PHONE);
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJuror.juror.responded.isNull()
            .or(QJuror.juror.responded.eq(false)));
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                                                         StandardReportResponse.TableData tableData) {
        Map<String, StandardReportResponse.DataTypeValue> map = loadStandardPoolHeaders(request,true,true);
        map.put("total_non_responded", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total non-responded")
            .dataType(Integer.class.getSimpleName())
            .value(tableData.getData().size())
            .build());
        return map;
    }
}
