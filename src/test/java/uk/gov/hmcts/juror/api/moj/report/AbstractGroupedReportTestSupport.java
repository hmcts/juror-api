package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractGroupedReportTestSupport
    <R extends AbstractGroupedReport>
    extends AbstractReportTestSupport<Map<String, List<LinkedHashMap<String, Object>>>, R> {
    private final DataType groupBy;
    private final boolean removeGroupByFromResponse;

    public AbstractGroupedReportTestSupport(EntityPath<?> from,
                                            Class<?> validatorClass,
                                            DataType groupBy,
                                            boolean removeGroupByFromResponse,
                                            DataType... dataTypes) {
        super(from, validatorClass, AbstractGroupedReport.combine(groupBy, dataTypes));
        this.removeGroupByFromResponse = removeGroupByFromResponse;
        this.groupBy = groupBy;
    }

    @Test
    @Override
    void positiveConstructor() {
        super.positiveConstructor();
        assertThat(report.removeGroupByFromResponse).isEqualTo(removeGroupByFromResponse);
        assertThat(report.groupBy).isEqualTo(groupBy);
        //Other fields don't need testing as they will be tested via the dedicated AbstractReportTest
    }

    @Override
    protected Map<String, List<LinkedHashMap<String, Object>>> createData() {
        return new HashMap<>();
    }
}
