package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("PMD.UselessParentheses") // false positive
public class DeferredListByCourtReport extends AbstractGroupedReport {

    @Autowired
    public DeferredListByCourtReport(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            ReportGroupBy.builder()
                .dataType(DataType.COURT_LOCATION_NAME_AND_CODE)
                .removeGroupByFromResponse(true)
                .sortDataFunction(data -> {
                    List<String> keys = new ArrayList<>(data.keySet());
                    keys.sort(String::compareTo);
                    LinkedHashMap<String, List<GroupedTableData>> sortedData = new LinkedHashMap<>();
                    keys.forEach(key -> sortedData.put(key, data.get(key)));
                    return sortedData;
                })
                .build(),
            DataType.DEFERRED_TO,
            DataType.NUMBER_DEFERRED);
    }


    @Override
    public void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.deferralDate.isNotNull());
        query.where(QJurorPool.jurorPool.deferralDate.goe(LocalDate.now()));
        if (SecurityUtil.isCourt() || (SecurityUtil.isBureau() && request.getFilterOwnedDeferrals())) {
            query.where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));
        }
        query.orderBy(QJurorPool.jurorPool.deferralDate.asc());
        query.groupBy(
            QCourtLocation.courtLocation.name,
            QCourtLocation.courtLocation.locCode,
            QJurorPool.jurorPool.deferralDate
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> headings = new ConcurrentHashMap<>();

        headings.put("total_deferred", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Total deferred")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().getAllDataItems()
                .stream()
                .map(row -> (Long) row.get(DataType.NUMBER_DEFERRED.getId()))
                .reduce(0L, Long::sum))
            .build());

        return headings;
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return DeferredListByCourtReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator {

    }
}
