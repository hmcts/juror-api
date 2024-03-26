package uk.gov.hmcts.juror.api.moj.report;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class AbstractStandardReportTest {

    private PoolRequestRepository poolRequestRepository;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        this.poolRequestRepository = mock(PoolRequestRepository.class);
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void afterEach() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
    }


//    @Nested
//    @DisplayName("public Map.Entry<String, Object> getDataFromReturnType(Tuple tuple, DataType dataType)")
//    class GetDataFromReturnType {
//
//        DataType dataType;
//        Expression<?> expression;
//        Tuple tuple;
//        private static final String ID = "SomeId";
//
//        <T> void setupNoReturnTypes(T value) {
//            dataType = mock(DataType.class);
//            when(dataType.getReturnTypes()).thenReturn(null);
//            when(dataType.getId()).thenReturn(ID);
//            expression = mock(Expression.class);
//            doReturn(expression).when(dataType).getExpression();
//            tuple = mock(Tuple.class);
//            doReturn(value).when(tuple).get(expression);
//        }
//
//        @Test
//        void positiveNoReturnTypesString() {
//            setupNoReturnTypes("SomeValue");
//            assertThat(createReport().getDataFromReturnType(tuple, dataType))
//                .isEqualTo(Map.entry(ID, "SomeValue"));
//        }
//
//        @Test
//        void positiveNoReturnTypesLong() {
//            setupNoReturnTypes(123L);
//            assertThat(createReport().getDataFromReturnType(tuple, dataType))
//                .isEqualTo(Map.entry(ID, 123L));
//        }
//
//        @Test
//        void positiveNoReturnTypesLocalDate() {
//            setupNoReturnTypes(LocalDate.of(2023, 1, 2));
//            assertThat(createReport().getDataFromReturnType(tuple, dataType))
//                .isEqualTo(Map.entry(ID, "2023-01-02"));
//        }
//
//        @Test
//        void positiveNoReturnTypesLocalTime() {
//            setupNoReturnTypes(LocalTime.of(11, 2));
//            assertThat(createReport().getDataFromReturnType(tuple, dataType))
//                .isEqualTo(Map.entry(ID, "11:02:00"));
//        }
//
//        @Test
//        void positiveNoReturnTypesLocalDateTime() {
//            setupNoReturnTypes(LocalDateTime.of(2023, 1, 2, 11, 2));
//            assertThat(createReport().getDataFromReturnType(tuple, dataType))
//                .isEqualTo(Map.entry(ID, "2023-01-02T11:02:00"));
//        }
//
//        @Test
//        void positiveHasReturnTypes() {
//            tuple = mock(Tuple.class);
//            dataType = mock(DataType.class);
//            when(dataType.getId()).thenReturn(ID);
//
//            DataType subDataType1 = mock(DataType.class);
//            doReturn("subDataTypeId1").when(subDataType1).getId();
//            Expression<?> subExpression1 = mock(Expression.class);
//            doReturn(subExpression1).when(subDataType1).getExpression();
//            doReturn("Value1").when(tuple).get(subExpression1);
//
//            DataType subDataType2 = mock(DataType.class);
//            doReturn("subDataTypeId2").when(subDataType2).getId();
//            Expression<?> subExpression2 = mock(Expression.class);
//            doReturn(subExpression2).when(subDataType2).getExpression();
//            doReturn(123L).when(tuple).get(subExpression2);
//
//            DataType subDataType3 = mock(DataType.class);
//            doReturn("subDataTypeId3").when(subDataType3).getId();
//            Expression<?> subExpression3 = mock(Expression.class);
//            doReturn(subExpression3).when(subDataType3).getExpression();
//            doReturn("Value3").when(tuple).get(subExpression3);
//
//            doReturn(expression).when(dataType).getExpression();
//
//            when(dataType.getReturnTypes()).thenReturn(new DataType[]{subDataType1, subDataType2, subDataType3});
//
//
//            assertThat(createReport().getDataFromReturnType(tuple, dataType))
//                .isEqualTo(Map.entry(ID, Map.of(
//                    subDataType1.getId(), "Value1",
//                    subDataType2.getId(), 123L,
//                    subDataType3.getId(), "Value3"
//                )));
//        }
//    }
}
