package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupByResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class AbstractGroupedReportTest {

    private PoolRequestRepository poolRequestRepository;

    @BeforeEach
    void beforeEach() {
        this.poolRequestRepository = mock(PoolRequestRepository.class);
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void positiveConstructor() {
        IReportGroupBy groupBy = createGroupBy();
        AbstractGroupedReport report = new AbstractStandardReportTestImpl(
            poolRequestRepository,
            QJuror.juror,
            groupBy,
            DataType.FIRST_NAME,
            DataType.CONTACT_DETAILS,
            DataType.STATUS);

        assertThat(report.groupBy).isEqualTo(groupBy);
        assertThat(report.poolRequestRepository).isEqualTo(poolRequestRepository);
        assertThat(report.from).isEqualTo(QJuror.juror);
        assertThat(report.dataTypes).containsExactly(DataType.JUROR_NUMBER, DataType.FIRST_NAME,
            DataType.CONTACT_DETAILS, DataType.STATUS);
        assertThat(report.effectiveDataTypes)
            .containsExactly(DataType.JUROR_NUMBER, DataType.FIRST_NAME, DataType.MAIN_PHONE,
                DataType.OTHER_PHONE, DataType.WORK_PHONE, DataType.EMAIL, DataType.STATUS);
        assertThat(report.requiredTables).contains(QJuror.juror, QJurorPool.jurorPool);
    }

    @Test
    void positiveCombine() {
        IReportGroupBy groupBy = createGroupBy();
        IDataType[] dataTypes = AbstractGroupedReport.combine(groupBy,
            DataType.FIRST_NAME, DataType.CONTACT_DETAILS);
        assertThat(dataTypes).containsExactly(DataType.JUROR_NUMBER, DataType.FIRST_NAME, DataType.CONTACT_DETAILS);
    }

    @Test
    void positiveCombineNoAdditional() {
        IReportGroupBy groupBy = createGroupBy();
        IDataType[] dataTypes = AbstractGroupedReport.combine(groupBy);
        assertThat(dataTypes).containsExactly(DataType.JUROR_NUMBER);
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void positiveGetTableData() {
        final List<Tuple> data = List.of(mock(Tuple.class), mock(Tuple.class));

        List<LinkedHashMap<String, Object>> tableData = new ArrayList<>();
        tableData.add(new GroupedTableData()
            .setType(GroupedTableData.Type.DATA)
            .add(DataType.JUROR_NUMBER.getId(), "1231")
            .add(DataType.FIRST_NAME.getId(), "John1")
            .add(DataType.LAST_NAME.getId(), "Doe1"));
        tableData.add(new GroupedTableData()
            .setType(GroupedTableData.Type.DATA)
            .add(DataType.JUROR_NUMBER.getId(), "1232")
            .add(DataType.FIRST_NAME.getId(), "John2")
            .add(DataType.LAST_NAME.getId(), "Doe2"));
        tableData.add(new GroupedTableData()
            .setType(GroupedTableData.Type.DATA)
            .add(DataType.JUROR_NUMBER.getId(), "1231")
            .add(DataType.FIRST_NAME.getId(), "John1.1")
            .add(DataType.LAST_NAME.getId(), "Doe1.1"));
        tableData.add(new GroupedTableData()
            .setType(GroupedTableData.Type.DATA)
            .add(DataType.JUROR_NUMBER.getId(), "1233")
            .add(DataType.FIRST_NAME.getId(), "John3")
            .add(DataType.LAST_NAME.getId(), "Doe3"));


        AbstractGroupedReport report = createReport();
        doReturn(tableData).when(report).getTableDataAsList(data);

        GroupedTableData result = report.getTableData(data);
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(toLinkedHashMapList(result.get("1231"))).hasSize(2)
            .contains(tableData.get(0), tableData.get(2));
        assertThat(toLinkedHashMapList(result.get("1232"))).hasSize(1)
            .contains(tableData.get(1));
        assertThat(toLinkedHashMapList(result.get("1233"))).hasSize(1)
            .contains(tableData.get(3));
        verify(report).getTableDataAsList(data);
    }

    @SuppressWarnings("unchecked")//TODO
    private List<LinkedHashMap<String, Object>> toLinkedHashMapList(Object o) {
        return (List<LinkedHashMap<String, Object>>) o;
    }

    @Test
    void positiveCreateBlankResponse() {
        AbstractGroupedReport report = createReport();
        GroupedReportResponse response = report.createBlankResponse();
        assertThat(response).isNotNull();
        GroupByResponse groupByResponse = GroupByResponse.builder()
            .name(DataType.JUROR_NUMBER.name())
            .nested(null)
            .build();
        assertThat(response.getGroupBy()).isEqualTo(groupByResponse);
        assertThat(response).isEqualTo(new GroupedReportResponse(groupByResponse));
    }

    private AbstractGroupedReport createReport() {
        return createReport(createGroupBy());
    }

    private AbstractGroupedReport createReport(IReportGroupBy groupBy) {
        return spy(new AbstractStandardReportTestImpl(
            poolRequestRepository,
            QJuror.juror,
            groupBy,
            DataType.FIRST_NAME,
            DataType.LAST_NAME));
    }

    private IReportGroupBy createGroupBy() {
        return ReportGroupBy.builder()
            .dataType(DataType.JUROR_NUMBER)
            .build();
    }


    private static class AbstractStandardReportTestImpl extends AbstractGroupedReport {

        public AbstractStandardReportTestImpl(PoolRequestRepository poolRequestRepository, EntityPath<?> from,
                                              IReportGroupBy groupBy, DataType... dataType) {
            super(poolRequestRepository, from, groupBy, dataType);
        }

        @Override
        public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
            throw new UnsupportedOperationException("Not implemented");

        }

        @Override
        public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            AbstractReportResponse.TableData<GroupedTableData> tableData) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
