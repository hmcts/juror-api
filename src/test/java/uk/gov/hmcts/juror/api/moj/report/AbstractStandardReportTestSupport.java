package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;

public abstract class AbstractStandardReportTestSupport
    <R extends AbstractStandardReport>
    extends AbstractReportTestSupport<StandardTableData, R> {


    public AbstractStandardReportTestSupport(EntityPath<?> from,
                                             Class<?> validatorClass, DataType... dataTypes) {
        super(from, validatorClass, dataTypes);
    }


    @Override
    protected StandardTableData createData() {
        return new StandardTableData();
    }
}
