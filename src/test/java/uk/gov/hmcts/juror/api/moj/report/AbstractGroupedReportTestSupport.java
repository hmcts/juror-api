package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractGroupedReportTestSupport
    <R extends AbstractGroupedReport>
    extends AbstractReportTestSupport<GroupedTableData, R> {


    private final IReportGroupBy groupBy;

    public AbstractGroupedReportTestSupport(EntityPath<?> from,
                                            Class<?> validatorClass,
                                            IReportGroupBy groupBy,
                                            IDataType... dataTypes) {
        super(from, validatorClass, AbstractGroupedReport.combine(groupBy, dataTypes));
        this.groupBy = groupBy;
    }

    @Test
    @Override
    void positiveConstructor() {
        super.positiveConstructor();
        assertThat(report.groupBy).isEqualTo(groupBy);
        //Other fields don't need testing as they will be tested via the dedicated AbstractReportTest
    }

    @Override
    protected GroupedTableData createData() {
        return new GroupedTableData();
    }
}
