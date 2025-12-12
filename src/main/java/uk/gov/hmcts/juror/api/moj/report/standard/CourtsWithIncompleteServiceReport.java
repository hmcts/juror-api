package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.JoinType;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standard report showing all courts with incomplete service jurors.
 * Displays court name with location code and count of incomplete jurors per court.
 * Access control is handled by the frontend application.
 *
 * Uses the same logic as IncompleteServiceReport - counts jurors based on
 * juror_pool status (RESPONDED, PANEL, JUROR) who are active and have
 * return_date <= cutoff date. Does NOT check appearance records.
 *
 * Columns:
 * - Court: Court name and location code (e.g., "CHESTER (415)")
 * - Incomplete Jurors: Count of jurors with incomplete service at that court
 */
@Slf4j
@Component
public class CourtsWithIncompleteServiceReport extends AbstractStandardReport {

    @Autowired
    public CourtsWithIncompleteServiceReport(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
              QJurorPool.jurorPool,
              DataType.COURT_LOCATION_NAME_AND_CODE_JP,
              DataType.INCOMPLETE_JURORS_COUNT);

        // Only join to Juror table - no need for Appearance table
        addJoinOverride(JoinOverrideDetails.builder()
                            .from(QJurorPool.jurorPool)
                            .to(QJuror.juror)
                            .joinType(JoinType.INNERJOIN)
                            .build());
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        log.info("Building query for CourtsWithIncompleteServiceReport with date: {}", request.getDate());

        // Filter by return date - jurors who should have returned by the cutoff date
        query.where(QJurorPool.jurorPool.pool.returnDate.loe(request.getDate()));

        // Filter by active status - only active jurors
        query.where(QJurorPool.jurorPool.isActive.eq(true));

        // Filter by status - RESPONDED, PANEL, JUROR (same as IncompleteServiceReport)
        query.where(QJurorPool.jurorPool.status.status.in(java.util.List.of(
            IJurorStatus.RESPONDED,
            IJurorStatus.PANEL,
            IJurorStatus.JUROR)));

        // Order by court name alphabetically
        query.orderBy(QJurorPool.jurorPool.pool.courtLocation.name.asc());

        // Group by court location (will group by loc_code and loc_name)
      //  addGroupBy(query, DataType.COURT_LOCATION_NAME_AND_CODE_JP);
        query.groupBy(
            QJurorPool.jurorPool.pool.courtLocation.locCode,
            QJurorPool.jurorPool.pool.courtLocation.name
        );

        log.debug("Query built successfully with cutoff date: {}", request.getDate());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<StandardTableData> tableData) {

        log.info("Generating headings for CourtsWithIncompleteServiceReport");

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        // Add cut-off date heading
        map.put("cut_off_date", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Cut-off Date")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getDate()))
            .build());

        // Calculate total incomplete jurors across all courts
        int totalIncompleteJurors = tableData.getData().stream()
            .mapToInt(row -> {
                Object countObj = row.get("incomplete_jurors_count");
                if (countObj instanceof Number) {
                    return ((Number) countObj).intValue();
                }
                return 0;
            })
            .sum();

        map.put("total_incomplete_jurors", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total Incomplete Jurors")
            .dataType(Integer.class.getSimpleName())
            .value(totalIncompleteJurors)
            .build());

        // Add total courts count
        map.put("total_courts", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total Courts")
            .dataType(Integer.class.getSimpleName())
            .value(tableData.getData().size())
            .build());

        log.debug("Headings generated - Total courts: {}, Total incomplete jurors: {}",
                  tableData.getData().size(), totalIncompleteJurors);

        return map;
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    /**
     * Validator for CourtsWithIncompleteServiceReport requests.
     */
    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireDate {
    }
}
