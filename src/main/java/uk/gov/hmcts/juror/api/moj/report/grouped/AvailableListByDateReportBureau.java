package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.JoinType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.support.AvailableListReport;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AvailableListByDateReportBureau extends AbstractGroupedReport implements AvailableListReport {

    public AvailableListByDateReportBureau(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            ReportGroupBy.builder()
                .dataType(DataType.COURT_LOCATION_NAME_AND_CODE)
                .removeGroupByFromResponse(true)
                .nested(ReportGroupBy.builder()
                    .dataType(DataType.POOL_NUMBER_AND_COURT_TYPE)
                    .removeGroupByFromResponse(true)
                    .build())
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.STATUS,
            DataType.JUROR_REASONABLE_ADJUSTMENT_WITH_MESSAGE,
            DataType.ON_CALL);

        addJoinOverride(
            JoinOverrideDetails.builder()
                .from(QJurorPool.jurorPool)
                .joinType(JoinType.LEFTJOIN)
                .to(QReasonableAdjustments.reasonableAdjustments)
                .build()
        );
        isBureauUserOnly();
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        addStandardFilters(query, request);
        query.where(QJurorPool.jurorPool.nextDate.eq(request.getDate()));
        query.orderBy(
            ((ComparableExpressionBase<?>) DataType.COURT_LOCATION_NAME_AND_CODE.getExpression()).asc(),
            QJurorPool.jurorPool.pool.poolNumber.asc(),
            QJurorPool.jurorPool.juror.jurorNumber.asc()
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData) {
        Map<String, AbstractReportResponse.DataTypeValue> headings = new LinkedHashMap<>(
            getHeadingsInternal(request, tableData));
        headings.put("attendance_date",
            AbstractReportResponse.DataTypeValue.builder()
                .displayName("Attendance date")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(request.getDate()))
                .build());
        return headings;
    }

    @Override
    public Class<AvailableListByDateReportBureau.RequestValidator> getRequestValidatorClass() {
        return AvailableListByDateReportBureau.RequestValidator.class;
    }

    public interface RequestValidator extends
        AvailableListReport.RequestValidator,
        Validators.RequireDate {
    }
}
