package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.grouped.groupby.GroupByAppearanceTrialNumberOrPoolNumber;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UnpaidAttendanceReportDetailedReport extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;

    @Autowired
    public UnpaidAttendanceReportDetailedReport(CourtLocationService courtLocationService) {
        super(QAppearance.appearance,
            new GroupByAppearanceTrialNumberOrPoolNumber(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.AUDIT_NUMBER,
            DataType.ATTENDANCE_TYPE,
            DataType.EXPENSE_STATUS);
        this.courtLocationService = courtLocationService;
        isCourtUserOnly();
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QAppearance.appearance.appearanceStage.in(
            AppearanceStage.EXPENSE_ENTERED,
            AppearanceStage.EXPENSE_EDITED
        ));
        query.where(QAppearance.appearance.locCode.eq(SecurityUtil.getLocCode()));
        query.where(QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate()));
        query.where(QAppearance.appearance.hideOnUnpaidExpenseAndReports.isFalse());

        query.orderBy(
            QAppearance.appearance.poolNumber.asc(),
            QAppearance.appearance.attendanceDate.asc(),
            QAppearance.appearance.jurorNumber.asc(),
            QAppearance.appearance.locCode.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> headings = new ConcurrentHashMap<>();

        headings.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date From")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());
        headings.put("date_to", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Date To")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());
        headings.put("total_unpaid_attendances", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Total unpaid attendances")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().getSize())
            .build());

        Map.Entry<String, GroupedReportResponse.DataTypeValue> entry =
            getCourtNameHeader(courtLocationService.getCourtLocation(SecurityUtil.getActiveOwner()));
        headings.put(entry.getKey(), entry.getValue());

        return headings;
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
