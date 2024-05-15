package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class AbstractJurorExpenditureReportTest {

    private static final String EXPENSE_TOTAL_PAID_DATA_TYPE_ID = "expense_total_paid_data_type_id";

    private GroupedTableData mockGroupedTableData(BigDecimal value) {
        GroupedTableData groupedTableData = mock(GroupedTableData.class);
        when(groupedTableData.getOrDefault(EXPENSE_TOTAL_PAID_DATA_TYPE_ID, BigDecimal.ZERO))
            .thenReturn(value);
        return groupedTableData;
    }

    @Test
    void positiveAddTotalHeaderTableData() {
        Map<String, AbstractReportResponse.DataTypeValue> map = mock(Map.class);
        List<GroupedTableData> groupedTableDataList = List.of(
            mockGroupedTableData(new BigDecimal("1.01")),
            mockGroupedTableData(new BigDecimal("2.02")),
            mockGroupedTableData(new BigDecimal("3.03"))
        );

        GroupedTableData parentGroupedTableData = mock(GroupedTableData.class);
        when(parentGroupedTableData.getAllDataItemsIfExist("heading_key_to_search_for"))
            .thenReturn(groupedTableDataList);

        AbstractReportResponse.TableData<GroupedTableData> tableData = mock(AbstractReportResponse.TableData.class);
        when(tableData.getData()).thenReturn(parentGroupedTableData);
        AbstractJurorExpenditureReport report = spy(new TestAbstractJurorExpenditureReportTest());

        BigDecimal expectedValue = new BigDecimal("6.06");
        doReturn(expectedValue).when(report).addTotalHeader(anyMap(), any(), any(), any());

        assertThat(report.addTotalHeader(
            map,
            tableData,
            "heading_key_to_search_for",
            "header_id",
            "Header Text"
        )).isEqualTo(expectedValue);

        verify(report, times(1))
            .addTotalHeader(map, expectedValue, "header_id", "Header Text");

    }

    @Test
    void positiveAddTotalHeaderValue() {
        AbstractJurorExpenditureReport report = new TestAbstractJurorExpenditureReportTest();

        Map<String, AbstractReportResponse.DataTypeValue> map = mock(Map.class);

        BigDecimal amount = new BigDecimal("123.45");
        assertThat(
            report.addTotalHeader(map, amount,
                "heading_id", "Heading Id")
        ).isEqualTo(amount);

        verify(map, times(1))
            .put("heading_id",
                GroupedReportResponse.DataTypeValue.builder()
                    .displayName("Heading Id")
                    .dataType("String")
                    .value("£123.45")
                    .build()
            );
    }

    static class TestAbstractJurorExpenditureReportTest extends AbstractJurorExpenditureReport {

        public TestAbstractJurorExpenditureReportTest() {
            super(new IDataType() {
                @Override
                public String getId() {
                    return EXPENSE_TOTAL_PAID_DATA_TYPE_ID;
                }

                @Override
                public String name() {
                    return "";
                }

                @Override
                public List<EntityPath<?>> getRequiredTables() {
                    return List.of();
                }

                @Override
                public String getDisplayName() {
                    return "";
                }

                @Override
                public Class<?> getDataType() {
                    return null;
                }

                @Override
                public Expression<?> getExpression() {
                    return null;
                }

                @Override
                public IDataType[] getReturnTypes() {
                    return new IDataType[0];
                }
            }, mock(CourtLocationService.class), true);
        }
    }
}
