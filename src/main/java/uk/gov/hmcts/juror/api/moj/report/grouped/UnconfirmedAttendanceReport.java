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
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class UnconfirmedAttendanceReport extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;

    @Autowired
    public UnconfirmedAttendanceReport(CourtLocationService courtLocationService) {
        super(
            QAppearance.appearance,
            ReportGroupBy.builder()
                .dataType(DataType.APPEARANCE_DATE_AND_POOL_TYPE)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.APPEARANCE_POOL_NUMBER,
            DataType.APPEARANCE_TRIAL_NUMBER,
            DataType.APPEARANCE_CHECKED_IN,
            DataType.APPEARANCE_CHECKED_OUT);

        isCourtUserOnly();
        this.courtLocationService = courtLocationService;
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(
            QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate())
                .and(QAppearance.appearance.appearanceStage.in(
                    AppearanceStage.CHECKED_IN,
                    AppearanceStage.CHECKED_OUT
                )));
        query.where(QAppearance.appearance.locCode.eq(SecurityUtil.getLocCode()));
        query.orderBy(QAppearance.appearance.attendanceDate.desc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, GroupedReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("total_unconfirmed_attendances", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Total unconfirmed attendances")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().getSize())
            .build());

        Map.Entry<String, GroupedReportResponse.DataTypeValue> entry =
            getCourtNameHeader(courtLocationService.getCourtLocation(SecurityUtil.getLocCode()));
        map.put(entry.getKey(), entry.getValue());

        return map;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireFromDate,
        Validators.RequireToDate {
    }
}
