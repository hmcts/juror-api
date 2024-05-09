package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.juror.domain.QPool;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class UnpaidAttendanceSummaryReport extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;

    @Autowired
    public UnpaidAttendanceSummaryReport(PoolRequestRepository poolRequestRepository,
                                         CourtLocationService courtLocationService) {
        super(poolRequestRepository,
            QAppearance.appearance,
            DataType.POOL_NUMBER,
            true,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME);
        this.courtLocationService = courtLocationService;

        isCourtUserOnly();
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(
            QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate())
                .and(QAppearance.appearance.appearanceStage.eq(AppearanceStage.EXPENSE_ENTERED))
                    .or(QAppearance.appearance.appearanceStage.eq(AppearanceStage.EXPENSE_EDITED))
                    .and(QAppearance.appearance.isDraftExpense.isFalse()));
        query.orderBy(QPool.pool.poolNumber.asc(), QJuror.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<Map<String, List<LinkedHashMap<String, Object>>>> tableData) {

        Map<String, GroupedReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date From")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());
        map.put("date_to", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Date To")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());
        map.put("total_unpaid_attendances", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Total Unpaid Attendances")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().values().stream()
                .map(List::size)
                .reduce(0, Integer::sum))
            .build());
        map.put("court_name", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value(getCourtNameHeader(courtLocationService.getCourtLocation(SecurityUtil.getActiveOwner())))
            .build());

        return map;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {

    }
}
