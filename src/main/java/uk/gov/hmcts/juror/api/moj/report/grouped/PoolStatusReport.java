package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class PoolStatusReport extends AbstractGroupedReport {
    private final JurorPoolRepository jurorPoolRepository;

    @Autowired
    public PoolStatusReport(PoolRequestRepository poolRequestRepository,
                            JurorPoolRepository jurorPoolRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            DataType.IS_ACTIVE,
            true,
            DataType.STATUS,
            DataType.POOL_MEMBER_COUNT);

        this.jurorPoolRepository = jurorPoolRepository;
    }

    @Override
    public Class<?> getRequestValidatorClass() {
        return AbstractReport.Validators.AbstractRequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber())
            .or(QJurorPool.jurorPool.pool.poolNumber.isNull()));
        query.where(QJurorStatus.jurorStatus.status.notIn(0,11,12,13));
        addGroupBy(query, DataType.STATUS, DataType.IS_ACTIVE, DataType.JUROR_STATUS);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                                                         AbstractReportResponse.TableData<Map<String,
                                                                             List<LinkedHashMap<String, Object>>>> tableData) {

        Optional<PoolRequest> optionalPoolRequest =
            getPoolRequestRepository().findByPoolNumber(request.getPoolNumber());
        if (optionalPoolRequest.isEmpty()) {
            throw new MojException.NotFound("Cannot find pool number " + request.getPoolNumber(), null);
        }

        PoolRequest poolRequest = optionalPoolRequest.get();

        Map<String, AbstractReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("pool_number", AbstractReportResponse.DataTypeValue.builder()
            .value(request.getPoolNumber())
            .dataType("String")
            .displayName("Pool number")
            .build());
        map.put("total_pool_members", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total pool members")
            .dataType("Long")
            .value(jurorPoolRepository.count(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber())))
            .build());
        map.put("total_requested_by_court", AbstractReportResponse.DataTypeValue.builder()
            .dataType("Long")
            .value(poolRequest.getTotalNoRequired())
            .displayName("Originally requested by court")
            .build());

        return map;
    }
}
