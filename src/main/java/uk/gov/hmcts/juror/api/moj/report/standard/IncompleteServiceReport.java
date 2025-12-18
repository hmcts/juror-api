package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.JoinType;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IncompleteServiceReport extends AbstractStandardReport {

    private final CourtLocationRepository courtLocationRepository;

    @Autowired
    public IncompleteServiceReport(PoolRequestRepository poolRequestRepository,
                                   CourtLocationRepository courtLocationRepository) {
        super(poolRequestRepository,
            QJuror.juror,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POOL_NUMBER_BY_JP,
            DataType.LAST_ATTENDANCE_DATE,
            DataType.NEXT_ATTENDANCE_DATE);
        this.courtLocationRepository = courtLocationRepository;

        addAuthenticationConsumer(request -> {
            boolean isCourt = SecurityUtil.isCourt();
            boolean isSuperUser = SecurityUtil.hasPermission(Permission.SUPER_USER);

            // Allow access if user is a court user OR a super user
            if (!isCourt && !isSuperUser) {
                throw new MojException.Forbidden("User not allowed to access this report", null);
            }

            // If court user (and not super user), check ownership
            if (isCourt && !isSuperUser) {
                checkOwnership(request.getLocCode(), false);
            }

        });

        addJoinOverride(JoinOverrideDetails.builder()
            .from(QJuror.juror)
            .joinType(JoinType.LEFTJOIN)
            .to(QAppearance.appearance)
            .predicatesToAdd(
                List.of(QAppearance.appearance.attendanceType.isNull()
                    .or(QAppearance.appearance.attendanceType.notIn(AttendanceType.ABSENT,
                        AttendanceType.NON_ATTENDANCE,
                        AttendanceType.NON_ATTENDANCE_LONG_TRIAL)))
            )
            .build());
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query
            .where(QJurorPool.jurorPool.pool.returnDate.loe(request.getDate()))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(request.getLocCode()));
        if (!SecurityUtil.hasPermission(Permission.SUPER_USER)) {
            query.where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.getActiveOwner()));
        }
        query
            .where(QJurorPool.jurorPool.isActive.eq(true))
            .where(QJurorPool.jurorPool.status.status.in(List.of(IJurorStatus.RESPONDED, IJurorStatus.PANEL,
                IJurorStatus.JUROR)))
            .orderBy(QJuror.juror.jurorNumber.asc());

        addGroupBy(query, DataType.JUROR_NUMBER, DataType.FIRST_NAME, DataType.LAST_NAME, DataType.POOL_NUMBER_BY_JP,
            DataType.NEXT_ATTENDANCE_DATE);
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("total_incomplete_service", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total incomplete service")
            .dataType(Integer.class.getSimpleName())
            .value(tableData.getData().size())
            .build());

        map.put("cut_off_date", AbstractReportResponse.DataTypeValue.builder().displayName("Cut-off Date")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getDate())).build());

        map.put("court_name", AbstractReportResponse.DataTypeValue.builder().displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value(getCourtNameString(courtLocationRepository, request.getLocCode()))
            .build());

        return map;
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireDate,
        Validators.RequireLocCode {

    }
}
