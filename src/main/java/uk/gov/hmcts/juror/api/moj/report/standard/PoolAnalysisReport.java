package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PoolAnalysisReport extends AbstractStandardReport {

    private final CourtLocationRepository courtLocationRepository;

    @Autowired
    public PoolAnalysisReport(PoolRequestRepository poolRequestRepository,
        CourtLocationRepository courtLocationRepository) {

        super(
            poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.POOL_NUMBER_BY_JP,
            DataType.SERVICE_START_DATE,
            DataType.SUMMONS_TOTAL,
            DataType.RESPONDED_TOTAL,
            DataType.ATTENDED_TOTAL,
            DataType.PANEL_TOTAL,
            DataType.JUROR_TOTAL,
            DataType.EXCUSED_TOTAL,
            DataType.DISQUALIFIED_TOTAL,
            DataType.DEFERRED_TOTAL,
            DataType.REASSIGNED_TOTAL,
            DataType.UNDELIVERABLE_TOTAL,
            DataType.TRANSFERRED_TOTAL,
            DataType.FAILED_TO_ATTEND_TOTAL
        );

        this.courtLocationRepository = courtLocationRepository;
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QPoolRequest.poolRequest.returnDate.between(request.getFromDate(), request.getToDate()));
        query.where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.getActiveOwner()));
        query.groupBy(QJurorPool.jurorPool.pool.poolNumber, QPoolRequest.poolRequest.returnDate);
        query.orderBy(QJurorPool.jurorPool.pool.poolNumber.asc());
    }

    @Override
    protected void postProcessTableData(StandardReportRequest request,
                                        AbstractReportResponse.TableData<StandardTableData> tableData) {

        tableData.getData().forEach(pool -> {
            Integer summonsTotal = (Integer) pool.getOrDefault(DataType.SUMMONS_TOTAL.getId(), 0);
            Integer respondedTotal = (Integer) pool.getOrDefault(DataType.RESPONDED_TOTAL.getId(), 0);
            // Integer attendedTotal = (Integer) pool.getOrDefault(DataType.ATTENDED_TOTAL, 0);
            Integer attendedTotal = (Integer) pool.getOrDefault("Attended", 0);
            Integer panelTotal = (Integer) pool.getOrDefault(DataType.PANEL_TOTAL.getId(), 0);
            Integer jurorTotal = (Integer) pool.getOrDefault(DataType.JUROR_TOTAL.getId(), 0);
            Integer excusedTotal = (Integer) pool.getOrDefault(DataType.EXCUSED_TOTAL.getId(), 0);
            Integer disqualifiedTotal = (Integer) pool.getOrDefault(DataType.DISQUALIFIED_TOTAL.getId(), 0);
            Integer deferredTotal = (Integer) pool.getOrDefault(DataType.DEFERRED_TOTAL.getId(), 0);
            Integer reassignedTotal = (Integer) pool.getOrDefault(DataType.REASSIGNED_TOTAL.getId(), 0);
            Integer undeliverableTotal = (Integer) pool.getOrDefault(DataType.UNDELIVERABLE_TOTAL.getId(), 0);
            Integer transferredTotal = (Integer) pool.getOrDefault(DataType.TRANSFERRED_TOTAL.getId(), 0);
            Integer failedToAttendTotal = (Integer) pool.getOrDefault(DataType.FAILED_TO_ATTEND_TOTAL.getId(), 0);

            pool.put(DataType.SUMMONS_TOTAL_PERCENTAGE.getId(), 100);
            pool.put(DataType.RESPONDED_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, respondedTotal));
            pool.put(DataType.ATTENDED_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, attendedTotal));
            pool.put(DataType.PANEL_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, panelTotal));
            pool.put(DataType.JUROR_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, jurorTotal));
            pool.put(DataType.EXCUSED_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, excusedTotal));
            pool.put(DataType.DISQUALIFIED_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, disqualifiedTotal));
            pool.put(DataType.DEFERRED_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, deferredTotal));
            pool.put(DataType.REASSIGNED_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, reassignedTotal));
            pool.put(DataType.UNDELIVERABLE_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, undeliverableTotal));
            pool.put(DataType.TRANSFERRED_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, transferredTotal));
            pool.put(DataType.FAILED_TO_ATTEND_TOTAL_PERCENTAGE.getId(), calculatePercentage(summonsTotal, failedToAttendTotal));
        });
    }

    private Integer calculatePercentage(Integer total, Integer value) {
        return (int) Math.round((value * 100.0) / total);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        map.put("date_from", AbstractReportResponse.DataTypeValue.builder().displayName("Date From")
            .dataType(LocalDate.class.getSimpleName())
            .value(request.getFromDate())
            .build());
        map.put("date_to", AbstractReportResponse.DataTypeValue.builder().displayName("Date To")
            .dataType(LocalDate.class.getSimpleName())
            .value(request.getToDate())
            .build());

        if (SecurityUtil.isCourt()) {
            map.put("court_name", AbstractReportResponse.DataTypeValue.builder().displayName("Court Name")
                .dataType(String.class.getSimpleName())
                .value(getCourtNameString(this.courtLocationRepository, SecurityUtil.getLocCode()))
                .build());
        }

        return map;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
            Validators.RequireFromDate,
            Validators.RequireToDate {
    }
}
