package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CompletionOfServiceReport extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;

    public CompletionOfServiceReport(PoolRequestRepository poolRequestRepository,
                                     CourtLocationService courtLocationService) {
        super(
            poolRequestRepository,
            QJurorPool.jurorPool,
                ReportGroupBy.builder()
                    .dataType(DataType.POOL_NUMBER_AND_COURT_TYPE)
                    .removeGroupByFromResponse(true)
                    .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.COMPLETION_DATE
        );
        this.courtLocationService = courtLocationService;
        isCourtUserOnly();
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return CompletionOfServiceReport.RequestValidator.class;
    }

    @Override
    public void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.juror.completionDate.between(request.getFromDate(), request.getToDate()));
        query.where(QJurorPool.jurorPool.location.eq(SecurityUtil.getLocCode()));
        query.orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request, GroupedReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("date_from", StandardReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());
        map.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());
        map.put("total_pool_members_completed", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total pool members completed")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().getSize())
            .build());

        Map.Entry<String, GroupedReportResponse.DataTypeValue> entry =
            getCourtNameHeader(courtLocationService.getCourtLocation(SecurityUtil.getLocCode()));
        map.put(entry.getKey(), entry.getValue());
        return map;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {
    }
}
