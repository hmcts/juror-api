package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class AbstractStandardReportTestSupport
    <R extends AbstractStandardReport>
    extends AbstractReportTestSupport<List<LinkedHashMap<String, Object>>, R> {


    public AbstractStandardReportTestSupport(EntityPath<?> from,
                                             Class<?> validatorClass, DataType... dataTypes) {
        super(from, validatorClass, dataTypes);
    }


    @Override
    protected List<LinkedHashMap<String, Object>> createData() {
        return new ArrayList<>();
    }
}
