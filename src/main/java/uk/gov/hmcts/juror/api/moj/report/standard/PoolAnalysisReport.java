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
import uk.gov.hmcts.juror.api.moj.utils.NumberUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            DataType.JURORS_SUMMONED_TOTAL,
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

        if (SecurityUtil.isSatellite()) {
            query.where(QPoolRequest.poolRequest.courtLocation.locCode.eq(SecurityUtil.getLocCode()));
        } else {
            query.where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.getActiveOwner()));
        }

        query.groupBy(QJurorPool.jurorPool.pool.poolNumber, QPoolRequest.poolRequest.returnDate);
        query.orderBy(QJurorPool.jurorPool.pool.poolNumber.asc());
    }

    @Override
    protected void postProcessTableData(StandardReportRequest request,
                                        AbstractReportResponse.TableData<StandardTableData> tableData) {

        tableData.getData().forEach(pool -> {
            Long summonsTotal = (Long) pool.getOrDefault(DataType.JURORS_SUMMONED_TOTAL.getId(), 0L);
            int respondedTotal = (Integer) pool.getOrDefault(DataType.RESPONDED_TOTAL.getId(), 0);
            int attendedTotal = (Integer) pool.getOrDefault(DataType.ATTENDED_TOTAL.getId(), 0);
            int panelTotal = (Integer) pool.getOrDefault(DataType.PANEL_TOTAL.getId(), 0);
            int jurorTotal = (Integer) pool.getOrDefault(DataType.JUROR_TOTAL.getId(), 0);
            int excusedTotal = (Integer) pool.getOrDefault(DataType.EXCUSED_TOTAL.getId(), 0);
            int disqualifiedTotal = (Integer) pool.getOrDefault(DataType.DISQUALIFIED_TOTAL.getId(), 0);
            int deferredTotal = (Integer) pool.getOrDefault(DataType.DEFERRED_TOTAL.getId(), 0);
            int reassignedTotal = (Integer) pool.getOrDefault(DataType.REASSIGNED_TOTAL.getId(), 0);
            int undeliverableTotal = (Integer) pool.getOrDefault(DataType.UNDELIVERABLE_TOTAL.getId(), 0);
            int transferredTotal = (Integer) pool.getOrDefault(DataType.TRANSFERRED_TOTAL.getId(), 0);
            int failedToAttendTotal = (Integer) pool.getOrDefault(DataType.FAILED_TO_ATTEND_TOTAL.getId(), 0);

            pool.put(DataType.RESPONDED_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(respondedTotal, summonsTotal));
            pool.put(DataType.ATTENDED_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(attendedTotal, summonsTotal));
            pool.put(DataType.PANEL_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(panelTotal, summonsTotal));
            pool.put(DataType.JUROR_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(jurorTotal, summonsTotal));
            pool.put(DataType.EXCUSED_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(excusedTotal, summonsTotal));
            pool.put(DataType.DISQUALIFIED_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(disqualifiedTotal, summonsTotal));
            pool.put(DataType.DEFERRED_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(deferredTotal, summonsTotal));
            pool.put(DataType.REASSIGNED_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(reassignedTotal, summonsTotal));
            pool.put(DataType.UNDELIVERABLE_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(undeliverableTotal, summonsTotal));
            pool.put(DataType.TRANSFERRED_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(transferredTotal, summonsTotal));
            pool.put(DataType.FAILED_TO_ATTEND_TOTAL_PERCENTAGE.getId(),
                NumberUtils.calculatePercentage(failedToAttendTotal, summonsTotal));
        });
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        map.put("date_from", AbstractReportResponse.DataTypeValue.builder().displayName("Date From")
            .dataType(LocalDate.class.getSimpleName())
            .value(request.getFromDate().format(DateTimeFormatter.ISO_DATE))
            .build());
        map.put("date_to", AbstractReportResponse.DataTypeValue.builder().displayName("Date To")
            .dataType(LocalDate.class.getSimpleName())
            .value(request.getToDate().format(DateTimeFormatter.ISO_DATE))
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
