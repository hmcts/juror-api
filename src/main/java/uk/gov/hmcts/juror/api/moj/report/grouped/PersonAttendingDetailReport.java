package uk.gov.hmcts.juror.api.moj.report.grouped;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class PersonAttendingDetailReport extends AbstractGroupedReport {
    private final CourtLocationRepository courtLocationRepository;

    @Autowired
    public PersonAttendingDetailReport(PoolRequestRepository poolRequestRepository,
                                       CourtLocationRepository courtLocationRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            ReportGroupBy.builder()
                .dataType(DataType.POOL_NUMBER)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTAL_ADDRESS,
            DataType.CONTACT_DETAILS);

        this.courtLocationRepository = courtLocationRepository;
        isCourtUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.nextDate.eq(request.getDate()));
        query.where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(SecurityUtil.getLocCode()));
        if (request.getIncludeSummoned()) {
            query.where(QJurorPool.jurorPool.status.status.in(IJurorStatus.SUMMONED,
                                                              IJurorStatus.RESPONDED,
                                                              IJurorStatus.PANEL,
                                                              IJurorStatus.JUROR));
        } else {
            query.where(QJurorPool.jurorPool.status.status.in(IJurorStatus.RESPONDED,
                                                              IJurorStatus.PANEL,
                                                              IJurorStatus.JUROR));
        }
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> getHeadings(
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
    public Class<PersonAttendingDetailReport.RequestValidator> getRequestValidatorClass() {
        return PersonAttendingDetailReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireDate,
        Validators.RequireIncludeSummoned {

    }
}
