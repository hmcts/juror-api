package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class PersonAttendingSummaryReport extends AbstractGroupedReport {
    private final CourtLocationRepository courtLocationRepository;

    @Autowired
    public PersonAttendingSummaryReport(PoolRequestRepository poolRequestRepository,
                                        CourtLocationRepository courtLocationRepository) {
        super(poolRequestRepository,
              QJurorPool.jurorPool,
              ReportGroupBy.builder()
                  .dataType(DataType.POOL_NUMBER)
                  .removeGroupByFromResponse(true)
                  .build(),
              DataType.JUROR_NUMBER,
              DataType.FIRST_NAME,
              DataType.LAST_NAME);

        this.courtLocationRepository = courtLocationRepository;
        isCourtUserOnly();
    }

    public static List<Integer> getSupportedStatus(StandardReportRequest request) {
        List<Integer> allowedStatus = new ArrayList<>();
        allowedStatus.add(IJurorStatus.RESPONDED);
        if (Boolean.TRUE.equals(request.getIncludeSummoned())) {
            allowedStatus.add(IJurorStatus.SUMMONED);
        }
        if (Boolean.TRUE.equals(request.getIncludePanelMembers())) {
            allowedStatus.add(IJurorStatus.PANEL);
            allowedStatus.add(IJurorStatus.JUROR);
        }
        return allowedStatus;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.nextDate.eq(request.getDate()));
        query.where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(SecurityUtil.getLocCode()));
        query.where(QJurorPool.jurorPool.status.status.in(getSupportedStatus(request)));
        query.orderBy(QJurorPool.jurorPool.juror.lastName.asc());
    }


    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        GroupedReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("total_due", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total due to attend")
            .dataType(Integer.class.getSimpleName())
            .value(tableData.getData().getSize())
            .build());

        map.put("attendance_date", AbstractReportResponse.DataTypeValue.builder().displayName("Attendance date")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getDate())).build());

        map.put("court_name", AbstractReportResponse.DataTypeValue.builder().displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value(getCourtNameString(courtLocationRepository, SecurityUtil.getLocCode()))
            .build());

        return map;
    }

    @Override
    public Class<PersonAttendingSummaryReport.RequestValidator> getRequestValidatorClass() {
        return PersonAttendingSummaryReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireDate,
        Validators.RequireIncludeSummoned,
        Validators.RequireIncludePanelMembers {

    }
}
