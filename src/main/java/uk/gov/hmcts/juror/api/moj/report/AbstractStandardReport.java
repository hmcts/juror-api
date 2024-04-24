package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class AbstractStandardReport extends AbstractReport<List<LinkedHashMap<String, Object>>> {

    public AbstractStandardReport(EntityPath<?> from,
                                  IDataType... dataType) {
        this(null, from, dataType);
    }

    public AbstractStandardReport(PoolRequestRepository poolRequestRepository, EntityPath<?> from,
                                  IDataType... dataType) {
        super(poolRequestRepository, from, dataType);
    }

    @Override
    protected List<LinkedHashMap<String, Object>> getTableData(List<Tuple> data) {
        return getTableDataAsList(data);
    }


    @Override
    protected StandardReportResponse createBlankResponse() {
        return new StandardReportResponse();
    }
}
