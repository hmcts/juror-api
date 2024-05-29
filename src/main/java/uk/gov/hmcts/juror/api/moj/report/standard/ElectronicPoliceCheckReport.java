package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class ElectronicPoliceCheckReport extends AbstractStandardReport {

    public ElectronicPoliceCheckReport() {
        super(QJurorPool.jurorPool,
            DataType.POOL_NUMBER_JP,
            DataType.POLICE_CHECK_RESPONDED,
            DataType.POLICE_CHECK_SUBMITTED,
            DataType.POLICE_CHECK_COMPLETE,
            DataType.POLICE_CHECK_TIMED_OUT,
            DataType.POLICE_CHECK_DISQUALIFIED);
        isBureauUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.BUREAU_OWNER));
        query.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED));

        query.where(QJuror.juror.policeCheckLastUpdate.between(
            request.getFromDate().atTime(LocalTime.MIN),
            request.getToDate().atTime(LocalTime.MAX))
        );
        addGroupBy(query, DataType.POOL_NUMBER_JP);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        return Map.of("date_from", AbstractReportResponse.DataTypeValue.builder().displayName("Date from")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate())).build(),
            "date_to", AbstractReportResponse.DataTypeValue.builder().displayName("Date to")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(request.getToDate())).build());
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireFromDate,
        Validators.RequireToDate {
    }
}
