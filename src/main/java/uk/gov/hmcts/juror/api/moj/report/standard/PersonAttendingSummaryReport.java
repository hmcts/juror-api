package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class PersonAttendingSummaryReport extends AbstractStandardReport {
    private final CourtLocationRepository courtLocationRepository;

    @Autowired
    public PersonAttendingSummaryReport(PoolRequestRepository poolRequestRepository,
                                        CourtLocationRepository courtLocationRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME);

        this.courtLocationRepository = courtLocationRepository;
        isCourtUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.nextDate.eq(request.getDate()));
        query.where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(SecurityUtil.getLocCode()));
        if (request.getIncludeSummoned()) {
            query.where(QJurorPool.jurorPool.status.status
                .in(IJurorStatus.SUMMONED, IJurorStatus.RESPONDED));
        } else {
            query.where(QJurorPool.jurorPool.status.status.in(IJurorStatus.RESPONDED));
        }
        query.orderBy(QJurorPool.jurorPool.juror.lastName.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("total_due", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total due to attend")
            .dataType(Integer.class.getSimpleName())
            .value(tableData.getData().size())
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
        Validators.RequireIncludeSummoned {

    }
}
