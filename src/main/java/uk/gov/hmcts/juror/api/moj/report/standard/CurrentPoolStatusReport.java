package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Map;

@Component
public class CurrentPoolStatusReport extends AbstractStandardReport {

    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public CurrentPoolStatusReport(PoolRequestRepository poolRequestRepository, EntityManager entityManager) {
        super(
            poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.STATUS,
            DataType.DEFERRALS,
            DataType.ABSENCES,
            DataType.CONTACT_DETAILS,
            DataType.WARNING);
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()))
            .leftJoin(QAppearance.appearance)
            .on(QAppearance.appearance.jurorNumber.eq(QJuror.juror.jurorNumber),
                QAppearance.appearance.attendanceType.eq(AttendanceType.ABSENT));
        addGroupBy(query,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.STATUS,
            DataType.DEFERRALS,
            DataType.CONTACT_DETAILS,
            DataType.WARNING
        );
    }


    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = loadStandardPoolHeaders(request, true, true);
        map.put("number_of_jurors_summoned",StandardReportResponse.DataTypeValue.builder()
            .displayName("Number of Jurors Summoned")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().size())
            .build());

        long attendedCount = getJurorsAttendedCount(request.getPoolNumber());
        map.put("number_of_jurors_attended", StandardReportResponse.DataTypeValue.builder()
            .displayName("Number of Jurors Attended")
            .dataType(Long.class.getSimpleName())
            .value(attendedCount)
            .build());


        long poolMemberCount = getPoolMembersCount(request.getPoolNumber());
        map.put("total_pool_members", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total Pool Members ")
            .dataType(Long.class.getSimpleName())
            .value(poolMemberCount)
            .build());

        return map;
    }

    /**
     * Count the number of unique jurors who have attended (not absent) for this pool.
    */
    long getJurorsAttendedCount(String poolNumber) {
        Long result = jpaQueryFactory
            .select(QJurorPool.jurorPool.juror.jurorNumber.countDistinct())
            .from(QJurorPool.jurorPool)
            .join(QAppearance.appearance).on(
                QAppearance.appearance.jurorNumber.eq(QJurorPool.jurorPool.juror.jurorNumber),
                QAppearance.appearance.poolNumber.eq(QJurorPool.jurorPool.pool.poolNumber)
            )
            .where(
                QJurorPool.jurorPool.pool.poolNumber.eq(poolNumber),
                QAppearance.appearance.nonAttendanceDay.coalesce(false).eq(false),
                QAppearance.appearance.noShow.coalesce(false).eq(false)
            )
            .fetchOne();
        return result != null ? result : 0L;
    }

    long getPoolMembersCount(String poolNumber) {
        Long result = jpaQueryFactory
            .select(QJurorPool.jurorPool.juror.jurorNumber.count())
            .from(QJurorPool.jurorPool)
            .where(
                QJurorPool.jurorPool.pool.poolNumber.eq(poolNumber),
            QJurorPool.jurorPool.status.status.in(IJurorStatus.RESPONDED,IJurorStatus.PANEL,IJurorStatus.JUROR))
            .fetchOne();
        return result != null ? result : 0L;

    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequirePoolNumber {

    }
}
