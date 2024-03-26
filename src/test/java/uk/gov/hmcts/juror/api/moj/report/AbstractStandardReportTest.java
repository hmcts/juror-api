package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class AbstractStandardReportTest {

    private PoolRequestRepository poolRequestRepository;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;


    @Test
    void positiveConstructor() {
        AbstractStandardReport report = new AbstractStandardReportTestImpl(
            poolRequestRepository,
            QJuror.juror,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.CONTACT_DETAILS,
            DataType.STATUS);

        assertThat(report.poolRequestRepository).isEqualTo(poolRequestRepository);
        assertThat(report.from).isEqualTo(QJuror.juror);
        assertThat(report.dataTypes).containsExactly(DataType.JUROR_NUMBER, DataType.FIRST_NAME,
            DataType.CONTACT_DETAILS, DataType.STATUS);
        assertThat(report.effectiveDataTypes)
            .containsExactly(DataType.JUROR_NUMBER, DataType.FIRST_NAME, DataType.MAIN_PHONE,
                DataType.OTHER_PHONE, DataType.WORK_PHONE, DataType.EMAIL, DataType.STATUS);
        assertThat(report.requiredTables).containsExactly(QJuror.juror, QJurorPool.jurorPool);
    }

    @Test
    void positiveGetTableData() {
        AbstractStandardReport report = createReport();
        List<LinkedHashMap<String, Object>> tableData = new ArrayList<>();
        doReturn(tableData).when(report).getTableDataAsList(new ArrayList<>());

        List<Tuple> tupleData = new ArrayList<>();
        assertThat(report.getTableData(tupleData)).isEqualTo(tableData);
        verify(report).getTableDataAsList(tupleData);
    }

    @Test
    void positiveCreateBlankResponse() {
        AbstractStandardReport report = createReport();
        StandardReportResponse response = report.createBlankResponse();
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(new StandardReportResponse());
    }


    private AbstractStandardReport createReport() {
        return spy(new AbstractStandardReportTestImpl(
            poolRequestRepository,
            QJuror.juror,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME));
    }

    private static class AbstractStandardReportTestImpl extends AbstractStandardReport {


        public AbstractStandardReportTestImpl(PoolRequestRepository poolRequestRepository, EntityPath<?> from,
                                              DataType... dataType) {
            super(poolRequestRepository, from, dataType);
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
            AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
