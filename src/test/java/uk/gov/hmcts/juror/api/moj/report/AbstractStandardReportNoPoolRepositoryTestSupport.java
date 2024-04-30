package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.types.EntityPath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractStandardReportNoPoolRepositoryTestSupport <R extends AbstractStandardReport>
    extends AbstractReportTestSupport<List<LinkedHashMap<String, Object>>, R> {

    private final EntityPath<?> from;
    private final DataType[] dataTypes;

    public AbstractStandardReportNoPoolRepositoryTestSupport(EntityPath<?> from,
                                             Class<?> validatorClass, DataType... dataTypes) {
        super(from, validatorClass, dataTypes);
        this.from = from;
        this.dataTypes = dataTypes;
    }

    @Override
    void positiveConstructor() {
        assertThat(report).isNotNull();
        assertThat(report.getFrom()).isEqualTo(from);
        assertThat(report.getDataTypes()).containsExactly(dataTypes);
    }

    @Override
    protected List<LinkedHashMap<String, Object>> createData() {
        return new ArrayList<>();
    }
}
