package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class DeferredListByDateReport extends AbstractStandardReport {

    @Autowired
    public DeferredListByDateReport(PoolRequestRepository poolRequestRepository) {
        super(
            poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.DEFERRED_TO,
            DataType.NUMBER_DEFERRED);
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.deferralDate.isNotNull());
        if (SecurityUtil.isCourt()) {
            query.where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));
        }
        query.orderBy(QJurorPool.jurorPool.deferralDate.asc());
        addGroupBy(query, DataType.DEFERRED_TO);
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("total_deferred", StandardReportResponse.DataTypeValue.builder()
            .displayName("Total deferred")
            .dataType(Long.class.getSimpleName())
            .value(
                tableData.getData()
                    .stream()
                    .map(row -> (Long) row.get(DataType.NUMBER_DEFERRED.getId()))
                    .reduce(0L, Long::sum))
            .build());
        return map;
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return DeferredListByDateReport.RequestValidator.class;
    }

    public interface RequestValidator extends AbstractReport.Validators.AbstractRequestValidator {

    }
}