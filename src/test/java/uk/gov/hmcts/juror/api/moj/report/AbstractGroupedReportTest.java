package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
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

@SuppressWarnings("PMD.LawOfDemeter")
class AbstractGroupedReportTest {

    private PoolRequestRepository poolRequestRepository;

    @BeforeEach
    void beforeEach() {
        this.poolRequestRepository = mock(PoolRequestRepository.class);
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void positiveConstructor() {
        AbstractGroupedReport report = new AbstractStandardReportTestImpl(
            poolRequestRepository,
            QJuror.juror,
            DataType.JUROR_NUMBER,
            false,
            DataType.FIRST_NAME,
            DataType.CONTACT_DETAILS,
            DataType.STATUS);

        assertThat(report.groupBy).isEqualTo(DataType.JUROR_NUMBER);
        assertThat(report.removeGroupByFromResponse).isEqualTo(false);
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
        DataType[] dataTypes = AbstractGroupedReport.combine(DataType.JUROR_NUMBER,
            new DataType[]{DataType.FIRST_NAME, DataType.CONTACT_DETAILS});
        assertThat(dataTypes).containsExactly(DataType.JUROR_NUMBER, DataType.FIRST_NAME, DataType.CONTACT_DETAILS);
    }

    @Test
    void positiveCombineNoAdditional() {
        DataType[] dataTypes = AbstractGroupedReport.combine(DataType.JUROR_NUMBER,
            new DataType[]{});
        assertThat(dataTypes).containsExactly(DataType.JUROR_NUMBER);
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void positiveGetTableData() {
        final List<Tuple> data = List.of(mock(Tuple.class), mock(Tuple.class));

        List<LinkedHashMap<String, Object>> tableData = new ArrayList<>();
        tableData.add(new ReportLinkedMap<String, Object>()
            .add(DataType.JUROR_NUMBER.getId(), "1231")
            .add(DataType.FIRST_NAME.getId(), "John1")
            .add(DataType.LAST_NAME.getId(), "Doe1"));
        tableData.add(new ReportLinkedMap<String, Object>()
            .add(DataType.JUROR_NUMBER.getId(), "1232")
            .add(DataType.FIRST_NAME.getId(), "John2")
            .add(DataType.LAST_NAME.getId(), "Doe2"));
        tableData.add(new ReportLinkedMap<String, Object>()
            .add(DataType.JUROR_NUMBER.getId(), "1231")
            .add(DataType.FIRST_NAME.getId(), "John1.1")
            .add(DataType.LAST_NAME.getId(), "Doe1.1"));
        tableData.add(new ReportLinkedMap<String, Object>()
            .add(DataType.JUROR_NUMBER.getId(), "1233")
            .add(DataType.FIRST_NAME.getId(), "John3")
            .add(DataType.LAST_NAME.getId(), "Doe3"));


        AbstractGroupedReport report = createReport();
        doReturn(tableData).when(report).getTableDataAsList(data);

        Map<String, List<LinkedHashMap<String, Object>>> result = report.getTableData(data);
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get("1231")).hasSize(2)
            .contains(tableData.get(0), tableData.get(2));
        assertThat(result.get("1232")).hasSize(1)
            .contains(tableData.get(1));
        assertThat(result.get("1233")).hasSize(1)
            .contains(tableData.get(3));
        verify(report).getTableDataAsList(data);
    }

    @Test
    void positiveCreateBlankResponse() {
        AbstractGroupedReport report = createReport();
        GroupedReportResponse response = report.createBlankResponse();
        assertThat(response).isNotNull();
        assertThat(response.getGroupBy()).isEqualTo(DataType.JUROR_NUMBER);
        assertThat(response).isEqualTo(new GroupedReportResponse(DataType.JUROR_NUMBER));
    }

    private AbstractGroupedReport createReport() {
        return createReport(false);
    }

    private AbstractGroupedReport createReport(boolean removeGroupByFromResponse) {
        return spy(new AbstractStandardReportTestImpl(
            poolRequestRepository,
            QJuror.juror,
            DataType.JUROR_NUMBER,
            removeGroupByFromResponse,
            DataType.FIRST_NAME,
            DataType.LAST_NAME));
    }


    private static class AbstractStandardReportTestImpl extends AbstractGroupedReport {

        public AbstractStandardReportTestImpl(PoolRequestRepository poolRequestRepository, EntityPath<?> from,
                                              DataType groupBy,
                                              boolean removeGroupByFromResponse, DataType... dataType) {
            super(poolRequestRepository, from, groupBy, removeGroupByFromResponse, dataType);
        }

        @Override
        public Class<?> getRequestValidatorClass() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
            throw new UnsupportedOperationException("Not implemented");

        }

        @Override
        public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            AbstractReportResponse.TableData<Map<String, List<LinkedHashMap<String, Object>>>> tableData) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
