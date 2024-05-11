package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.IReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class UnpaidAttendanceReportDetailed extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;


    @Autowired
    public UnpaidAttendanceReportDetailed(PoolRequestRepository poolRequestRepository,
                                          CourtLocationService courtLocationService) {
        super(poolRequestRepository,
            QAppearance.appearance,
            new IReportGroupBy() {

                @Override
                public String getGroupFunction(GroupedTableData groupedTableData) {
                    Object trialNumber = groupedTableData.get(DataType.APPEARANCE_TRIAL_NUMBER.getId());
                    if (trialNumber != null) {
                        return "Trial " + trialNumber;
                    } else {
                        Object poolNumber = groupedTableData.get(DataType.APPEARANCE_POOL_NUMBER.getId());
                        return "Pool " + poolNumber;
                    }
                }

                @Override
                public IReportGroupBy getNested() {
                    return ReportGroupBy.builder()
                        .dataType(DataType.ATTENDANCE_DATE)
                        .removeGroupByFromResponse(true)
                        .build();
                }

                @Override
                public GroupByResponse getGroupedByResponse() {
                    return GroupByResponse.builder()
                        .name("TRIAL_NUMBER_OR_POOL_NUMBER")
                        .build();
                }

                @Override
                public Collection<? extends IDataType> getRequiredDataTypes() {
                    return List.of(DataType.APPEARANCE_TRIAL_NUMBER, DataType.APPEARANCE_POOL_NUMBER,
                        DataType.ATTENDANCE_DATE);
                }

                @Override
                public Collection<String> getKeysToRemove() {
                    return List.of(DataType.APPEARANCE_TRIAL_NUMBER.getId(), DataType.APPEARANCE_POOL_NUMBER.getId(),
                        DataType.ATTENDANCE_DATE.getId());
                }
            },
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
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QAppearance.appearance.appearanceStage.in(
            AppearanceStage.EXPENSE_ENTERED,
            AppearanceStage.EXPENSE_EDITED)
        );
        query.where(QAppearance.appearance.locCode.in(SecurityUtil.getCourts()));
        query.where(QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate()));
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<GroupedTableData> tableData) {

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
            .displayName("Total unpaid attendances")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().getSize())
            .build());

        Map.Entry<String, GroupedReportResponse.DataTypeValue> entry =
            getCourtNameHeader(courtLocationService.getCourtLocation(SecurityUtil.getActiveOwner()));
        map.put(entry.getKey(), entry.getValue());


        return map;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireFromDate,
        Validators.RequireToDate {

    }
}
