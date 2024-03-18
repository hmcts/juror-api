package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Map;

@Component
public class CurrentPoolStatusReport extends AbstractReport {

    @Autowired
    public CurrentPoolStatusReport(PoolRequestRepository poolRequestRepository) {
        super(
            poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.STATUS,
            DataType.DEFERRALS,
            DataType.ABSENCES,
            DataType.CONTACT_DETAILS,
            DataType.WARNING);
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()))
            .leftJoin(QAppearance.appearance)
            .on(QAppearance.appearance.jurorNumber.eq(QJuror.juror.jurorNumber),
                QAppearance.appearance.attendanceType.eq(AttendanceType.ABSENT));
        addGroupBy(query,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.STATUS,
            DataType.DEFERRALS,
            DataType.CONTACT_DETAILS,
            DataType.WARNING
        );
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                                                         StandardReportResponse.TableData tableData) {
        Map<String, StandardReportResponse.DataTypeValue> map = loadStandardPoolHeaders(request, true, true);
        map.put("total_pool_members", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total Pool Members")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().size())
            .build());
        return map;
    }

    @Override
    public Class<?> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends AbstractRequestValidator {

    }
}
