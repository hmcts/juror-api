package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.JoinType;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QPendingJuror;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.datatypes.PendingJurorTypes;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ManuallyCreatedJurorsReport extends AbstractStandardReport {

    public ManuallyCreatedJurorsReport() {
        super(QPendingJuror.pendingJuror,
            PendingJurorTypes.JUROR_NUMBER,
            PendingJurorTypes.CREATED_ON,
            PendingJurorTypes.CREATED_BY,
            PendingJurorTypes.FIRST_NAME,
            PendingJurorTypes.LAST_NAME,
            PendingJurorTypes.ADDRESS_COMBINED,
            PendingJurorTypes.STATUS,
            PendingJurorTypes.NOTES,
            PendingJurorTypes.POOL_NUMBER,
            PendingJurorTypes.SERVICE_COMPLETED);
        isCourtUserOnly();
        addJoinOverride(JoinOverrideDetails.builder()
            .from(QPendingJuror.pendingJuror)
            .joinType(JoinType.LEFTJOIN)
            .to(QJuror.juror)
            .predicatesToAdd(List.of(QJuror.juror.jurorNumber.eq(QPendingJuror.pendingJuror.jurorNumber)))
            .build());
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QPendingJuror.pendingJuror.dateAdded.between(request.getFromDate(), request.getToDate()));
        query.where(QPendingJuror.pendingJuror.pool.courtLocation.locCode.in(SecurityUtil.getCourts()));
        query.orderBy(QPendingJuror.pendingJuror.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {
        Map<String, AbstractReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("total_manually_created_jurors", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total manually-created jurors")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().size())
            .build());
        map.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());
        map.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());
        return map;
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {
    }
}
