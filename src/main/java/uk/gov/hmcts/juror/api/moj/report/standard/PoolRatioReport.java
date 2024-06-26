package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PoolRatioReport extends AbstractStandardReport {

    private static final String RATIO_1_ID = "ratio_1";
    private static final String RATIO_2_ID = "ratio_2";

    public PoolRatioReport() {
        super(QJurorPool.jurorPool,
            DataType.COURT_LOCATION_NAME_AND_CODE_JP,
            DataType.POOL_NUMBER_BY_JP,
            DataType.TOTAL_REQUESTED,
            DataType.TOTAL_DEFERRED,
            DataType.TOTAL_SUMMONED,
            DataType.TOTAL_SUPPLIED);
        isBureauUserOnly();
    }

    @Override
    protected void postProcessTableData(StandardReportRequest request,
                                        AbstractReportResponse.TableData<StandardTableData> tableData) {
        tableData.setData(getCombinedTableData(tableData));
        tableData.setHeadings(getTableDataHeadings());
    }

    private StandardTableData getCombinedTableData(AbstractReportResponse.TableData<StandardTableData> tableData) {
        @Setter
        @Getter
        class TableData {
            private long requested;
            private long deferred;
            private long summoned;
            private long supplied;

            public void addRequested(long amount) {
                this.requested += amount;
            }

            public void addDeferred(long amount) {
                this.deferred += amount;
            }

            public void addSummoned(long amount) {
                this.summoned += amount;
            }

            public void addSupplied(long amount) {
                this.supplied += amount;
            }

            public double getRatio1() {
                double right = requested - deferred;
                if (right == 0.0) {
                    return 0.0;
                }
                double left = summoned - deferred;
                return left / right;
            }

            public double getRatio2() {
                double right = supplied - deferred;
                if (right == 0.0) {
                    return 0.0;
                }
                double left = summoned - deferred;
                return left / right;
            }
        }

        Map<String, TableData> combinedData = new ConcurrentHashMap<>();

        tableData.getData().forEach(stringObjectLinkedHashMap -> {
            String key = (String) stringObjectLinkedHashMap.get(DataType.COURT_LOCATION_NAME_AND_CODE_JP.getId());
            TableData data = combinedData.get(key);
            if (data == null) {
                data = new TableData();
                combinedData.put(key, data);
            }
            data.addRequested(
                ((Integer) stringObjectLinkedHashMap.getOrDefault(DataType.TOTAL_REQUESTED.getId(), 0)).longValue());
            data.addDeferred((Long) stringObjectLinkedHashMap.getOrDefault(DataType.TOTAL_DEFERRED.getId(), 0L));
            data.addSummoned((Long) stringObjectLinkedHashMap.getOrDefault(DataType.TOTAL_SUMMONED.getId(), 0L));
            data.addSupplied((Long) stringObjectLinkedHashMap.getOrDefault(DataType.TOTAL_SUPPLIED.getId(), 0L));
        });

        return new StandardTableData(
            combinedData.entrySet().stream()
                .map((entry) -> {
                    String key = entry.getKey();
                    TableData value = entry.getValue();
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    map.put(DataType.COURT_LOCATION_NAME_AND_CODE_JP.getId(), key);
                    map.put(DataType.TOTAL_REQUESTED.getId(), value.getRequested());
                    map.put(DataType.TOTAL_DEFERRED.getId(), value.getDeferred());
                    map.put(DataType.TOTAL_SUMMONED.getId(), value.getSummoned());
                    map.put(DataType.TOTAL_SUPPLIED.getId(), value.getSupplied());

                    map.put(RATIO_1_ID, value.getRatio1());
                    map.put(RATIO_2_ID, value.getRatio2());
                    return map;
                })
                .toList());
    }

    private List<AbstractReportResponse.TableData.Heading> getTableDataHeadings() {
        return List.of(
            getHeading(DataType.COURT_LOCATION_NAME_AND_CODE_JP),
            getHeading(DataType.TOTAL_REQUESTED),
            getHeading(DataType.TOTAL_DEFERRED),
            getHeading(DataType.TOTAL_SUMMONED),
            getHeading(DataType.TOTAL_SUPPLIED),
            AbstractReportResponse.TableData.Heading.builder()
                .id(RATIO_1_ID)
                .name("Ratio 1")
                .dataType(Double.class.getSimpleName())
                .build(),
            AbstractReportResponse.TableData.Heading.builder()
                .id(RATIO_2_ID)
                .name("Ratio 2")
                .dataType(Double.class.getSimpleName())
                .build()
        );
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.returnDate.between(request.getFromDate(), request.getToDate()));
        query.where(QJurorPool.jurorPool.pool.courtLocation.locCode.in(request.getCourts()));

        query.orderBy(QJurorPool.jurorPool.pool.courtLocation.name.asc(),
            QJurorPool.jurorPool.pool.courtLocation.locCode.asc());

        query.groupBy(
            QJurorPool.jurorPool.pool.poolNumber,
            QJurorPool.jurorPool.pool.numberRequested,
            QJurorPool.jurorPool.pool.courtLocation.locCode,
            QJurorPool.jurorPool.pool.courtLocation.name
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());
        map.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());
        return map;
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate,
        AbstractReport.Validators.RequireCourts {

    }
}
