package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.bureau.domain.QBureauJurorCjs;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.within;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings({
    "unchecked",
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports",
    "PMD.CouplingBetweenObjects"
})
class AbstractReportTest {

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

    @Nested
    class ClassToJoinTest {
        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void jurorToJurorPool() {
            assertThat(AbstractReport.CLASS_TO_JOIN.containsKey(QJuror.juror)).isTrue();
            Map<EntityPath<?>, Predicate[]> map = AbstractReport.CLASS_TO_JOIN.get(QJuror.juror);

            assertThat(map.containsKey(QJurorPool.jurorPool)).isTrue();
            assertThat(map.get(QJurorPool.jurorPool)).isEqualTo(
                new Predicate[]{QJuror.juror.eq(QJurorPool.jurorPool.juror)}
            );
        }

        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void jurorToAppearance() {
            assertThat(AbstractReport.CLASS_TO_JOIN.containsKey(QJuror.juror)).isTrue();
            Map<EntityPath<?>, Predicate[]> map = AbstractReport.CLASS_TO_JOIN.get(QJuror.juror);

            assertThat(map.containsKey(QJurorPool.jurorPool)).isTrue();
            assertThat(map.get(QAppearance.appearance)).isEqualTo(
                new Predicate[]{QJuror.juror.jurorNumber.eq(QAppearance.appearance.jurorNumber)}
            );
        }

        @Test
        void jurorPoolToJuror() {
            assertThat(AbstractReport.CLASS_TO_JOIN.containsKey(QJurorPool.jurorPool)).isTrue();
            Map<EntityPath<?>, Predicate[]> map = AbstractReport.CLASS_TO_JOIN.get(QJurorPool.jurorPool);

            assertThat(map.containsKey(QJuror.juror)).isTrue();
            assertThat(map.get(QJuror.juror)).isEqualTo(
                new Predicate[]{QJurorPool.jurorPool.juror.eq(QJuror.juror)}
            );
        }

        @Test
        void appearanceToJuror() {
            assertThat(AbstractReport.CLASS_TO_JOIN.containsKey(QAppearance.appearance)).isTrue();
            Map<EntityPath<?>, Predicate[]> map = AbstractReport.CLASS_TO_JOIN.get(QAppearance.appearance);

            assertThat(map.containsKey(QJuror.juror)).isTrue();
            assertThat(map.get(QJuror.juror)).isEqualTo(
                new Predicate[]{QAppearance.appearance.jurorNumber.eq(QJuror.juror.jurorNumber)}
            );
        }

        @Test
        void appearanceToJurorPool() {
            assertThat(AbstractReport.CLASS_TO_JOIN.containsKey(QAppearance.appearance)).isTrue();
            Map<EntityPath<?>, Predicate[]> map = AbstractReport.CLASS_TO_JOIN.get(QAppearance.appearance);

            assertThat(map.containsKey(QJurorPool.jurorPool)).isTrue();
            assertThat(map.get(QJurorPool.jurorPool)).isEqualTo(
                new Predicate[]{QJurorPool.jurorPool.juror.jurorNumber.eq(QAppearance.appearance.jurorNumber),
                    QJurorPool.jurorPool.pool.poolNumber.eq(QAppearance.appearance.poolNumber)}
            );
        }
    }


    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void positiveConstructorTest() {
        AbstractReport report = new AbstractReportTestImpl(
            poolRequestRepository, QJuror.juror, DataType.JUROR_NUMBER, DataType.FIRST_NAME,
            DataType.CONTACT_DETAILS, DataType.STATUS
        );
        assertThat(report.poolRequestRepository).isEqualTo(poolRequestRepository);
        assertThat(report.from).isEqualTo(QJuror.juror);
        assertThat(report.dataTypes).containsExactly(DataType.JUROR_NUMBER, DataType.FIRST_NAME,
            DataType.CONTACT_DETAILS, DataType.STATUS);
        assertThat(report.effectiveDataTypes)
            .containsExactly(DataType.JUROR_NUMBER, DataType.FIRST_NAME, DataType.MAIN_PHONE,
                DataType.OTHER_PHONE, DataType.WORK_PHONE, DataType.EMAIL, DataType.STATUS);
        assertThat(report.requiredTables).containsExactly(QJuror.juror, QJurorPool.jurorPool);
    }

    @Nested
    @DisplayName("String getName()")
    class GetName {
        @Test
        void positiveTypical() {
            assertThat(createReport().getName()).isEqualTo("AbstractReportTestImpl");
        }
    }

    @Nested
    @DisplayName("public StandardReportResponse getStandardReportResponse(StandardReportRequest request)")
    class GetStandardReportResponse {
        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveTypical() {
            AbstractReport report = createReport();
            StandardReportRequest request = mock(StandardReportRequest.class);
            List<Tuple> data = mock(List.class);
            StandardReportResponse.TableData tableData = mock(StandardReportResponse.TableData.class);

            doReturn(data).when(report).getData(request);
            doReturn(tableData).when(report).tupleToTableData(data);


            Map<String, StandardReportResponse.DataTypeValue> headingsResponse = Map.of(
                "testValue1",
                mock(StandardReportResponse.DataTypeValue.class),
                "testValue2",
                mock(StandardReportResponse.DataTypeValue.class)
            );
            doReturn(headingsResponse).when(report).getHeadings(request, tableData);


            StandardReportResponse response = report.getStandardReportResponse(request);

            assertThat(response).isNotNull();
            assertThat(response.getTableData()).isEqualTo(tableData);
            assertThat(response.getHeadings()).isNotNull();
            assertThat(response.getHeadings()).hasSize(3);
            assertThat(response.getHeadings()).containsAllEntriesOf(headingsResponse);
            assertThat(response.getHeadings()).containsKey("report_created");
            StandardReportResponse.DataTypeValue reportCreated = response.getHeadings().get("report_created");
            assertThat(reportCreated).isNotNull();
            assertThat(reportCreated.getDataType()).isEqualTo(LocalDateTime.class.getSimpleName());
            LocalDateTime createdAt =
                LocalDateTime.parse(String.valueOf(reportCreated.getValue()), DateTimeFormatter.ISO_DATE_TIME);
            assertThat(createdAt).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));


            verify(report, times(1))
                .getData(request);
            verify(report, times(1))
                .tupleToTableData(data);
            verify(report, times(1))
                .getHeadings(request, tableData);
        }
    }


    @Nested
    @DisplayName("StandardReportResponse.TableData tupleToTableData(List<Tuple> data)")
    class TupleToTableData {
        @Test
        void positiveTypical() {
            List<Tuple> data = List.of(
                mock(Tuple.class),
                mock(Tuple.class),
                mock(Tuple.class)
            );

            AbstractReport report = createReport(
                QJuror.juror,
                DataType.JUROR_NUMBER,
                DataType.FIRST_NAME,
                DataType.LAST_NAME
            );


            StandardReportResponse.TableData.Heading heading1 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId")
                .build();
            doReturn(heading1)
                .when(report)
                .getHeading(DataType.JUROR_NUMBER);

            StandardReportResponse.TableData.Heading heading2 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId2")
                .build();
            doReturn(heading2)
                .when(report)
                .getHeading(DataType.FIRST_NAME);

            StandardReportResponse.TableData.Heading heading3 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId3")
                .build();
            doReturn(heading3)
                .when(report)
                .getHeading(DataType.LAST_NAME);

            doReturn(
                new HelperEntry<>("testId1_1", "testValue1_1"),
                new HelperEntry<>("testId1_2", "testValue1_2"),
                new HelperEntry<>("testId1_3", "testValue1_3"))
                .when(report).getDataFromReturnType(any(), eq(DataType.JUROR_NUMBER));
            doReturn(
                new HelperEntry<>("testId2_1", "testValue2_1"),
                new HelperEntry<>("testId2_2", "testValue2_2"),
                new HelperEntry<>("testId2_3", "testValue2_3"))
                .when(report).getDataFromReturnType(any(), eq(DataType.FIRST_NAME));

            doReturn(
                new HelperEntry<>("testId3_1", "testValue3_1"),
                new HelperEntry<>("testId3_2", "testValue3_2"),
                new HelperEntry<>("testId3_3", "testValue3_3"))
                .when(report).getDataFromReturnType(any(), eq(DataType.LAST_NAME));

            StandardReportResponse.TableData tableData = report.tupleToTableData(data);


            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(3);
            assertThat(tableData.getHeadings()).containsAll(
                List.of(heading1, heading2, heading3)
            );
            assertThat(tableData.getData()).hasSize(3);
            assertThat(tableData.getData()).containsAll(
                List.of(
                    new HelperMap()
                        .add("testId1_1", "testValue1_1")
                        .add("testId2_1", "testValue2_1")
                        .add("testId3_1", "testValue3_1"),
                    new HelperMap()
                        .add("testId1_2", "testValue1_2")
                        .add("testId2_2", "testValue2_2")
                        .add("testId3_2", "testValue3_2"),
                    new HelperMap()
                        .add("testId1_3", "testValue1_3")
                        .add("testId2_3", "testValue2_3")
                        .add("testId3_3", "testValue3_3")
                )
            );
            verify(report, times(1))
                .getHeading(DataType.JUROR_NUMBER);
            verify(report, times(1))
                .getHeading(DataType.FIRST_NAME);
            verify(report, times(1))
                .getHeading(DataType.LAST_NAME);
            verify(report, times(1))
                .getDataFromReturnType(data.get(0), DataType.JUROR_NUMBER);
            verify(report, times(1))
                .getDataFromReturnType(data.get(1), DataType.JUROR_NUMBER);
            verify(report, times(1))
                .getDataFromReturnType(data.get(2), DataType.JUROR_NUMBER);
            verify(report, times(1))
                .getDataFromReturnType(data.get(0), DataType.FIRST_NAME);
            verify(report, times(1))
                .getDataFromReturnType(data.get(1), DataType.FIRST_NAME);
            verify(report, times(1))
                .getDataFromReturnType(data.get(2), DataType.FIRST_NAME);
            verify(report, times(1))
                .getDataFromReturnType(data.get(0), DataType.LAST_NAME);
            verify(report, times(1))
                .getDataFromReturnType(data.get(1), DataType.LAST_NAME);
            verify(report, times(1))
                .getDataFromReturnType(data.get(2), DataType.LAST_NAME);
        }

        @Test
        void positiveNullValue() {
            List<Tuple> data = List.of(
                mock(Tuple.class),
                mock(Tuple.class),
                mock(Tuple.class)
            );

            AbstractReport report = createReport(
                QJuror.juror,
                DataType.JUROR_NUMBER,
                DataType.FIRST_NAME,
                DataType.LAST_NAME
            );


            StandardReportResponse.TableData.Heading heading1 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId")
                .build();
            doReturn(heading1)
                .when(report)
                .getHeading(DataType.JUROR_NUMBER);

            StandardReportResponse.TableData.Heading heading2 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId2")
                .build();
            doReturn(heading2)
                .when(report)
                .getHeading(DataType.FIRST_NAME);

            StandardReportResponse.TableData.Heading heading3 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId3")
                .build();
            doReturn(heading3)
                .when(report)
                .getHeading(DataType.LAST_NAME);

            doReturn(
                new HelperEntry<>("testId1_1", null),
                new HelperEntry<>("testId1_2", "testValue1_2"),
                new HelperEntry<>("testId1_3", "testValue1_3"))
                .when(report).getDataFromReturnType(any(), eq(DataType.JUROR_NUMBER));
            doReturn(
                new HelperEntry<>("testId2_1", "testValue2_1"),
                new HelperEntry<>("testId2_2", "testValue2_2"),
                new HelperEntry<>("testId2_3", null))
                .when(report).getDataFromReturnType(any(), eq(DataType.FIRST_NAME));

            doReturn(
                new HelperEntry<>("testId3_1", "testValue3_1"),
                new HelperEntry<>("testId3_2", null),
                new HelperEntry<>("testId3_3", "testValue3_3"))
                .when(report).getDataFromReturnType(any(), eq(DataType.LAST_NAME));

            StandardReportResponse.TableData tableData = report.tupleToTableData(data);


            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(3);
            assertThat(tableData.getHeadings()).containsAll(
                List.of(heading1, heading2, heading3)
            );
            assertThat(tableData.getData()).hasSize(3);
            assertThat(tableData.getData()).containsAll(
                List.of(
                    new HelperMap()
                        .add("testId2_1", "testValue2_1")
                        .add("testId3_1", "testValue3_1"),
                    new HelperMap()
                        .add("testId1_2", "testValue1_2")
                        .add("testId2_2", "testValue2_2"),
                    new HelperMap()
                        .add("testId1_3", "testValue1_3")
                        .add("testId3_3", "testValue3_3")
                )
            );
        }

        @Test
        void positiveMapEmptyValue() {
            List<Tuple> data = List.of(
                mock(Tuple.class),
                mock(Tuple.class),
                mock(Tuple.class)
            );

            AbstractReport report = createReport(
                QJuror.juror,
                DataType.JUROR_NUMBER,
                DataType.FIRST_NAME,
                DataType.LAST_NAME
            );


            StandardReportResponse.TableData.Heading heading1 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId")
                .build();
            doReturn(heading1)
                .when(report)
                .getHeading(DataType.JUROR_NUMBER);

            StandardReportResponse.TableData.Heading heading2 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId2")
                .build();
            doReturn(heading2)
                .when(report)
                .getHeading(DataType.FIRST_NAME);

            StandardReportResponse.TableData.Heading heading3 = StandardReportResponse.TableData.Heading.builder()
                .dataType(String.class.getSimpleName())
                .id("testId3")
                .build();
            doReturn(heading3)
                .when(report)
                .getHeading(DataType.LAST_NAME);

            doReturn(
                new HelperEntry<>("testId1_1", Map.of()),
                new HelperEntry<>("testId1_2", "testValue1_2"),
                new HelperEntry<>("testId1_3", "testValue1_3"))
                .when(report).getDataFromReturnType(any(), eq(DataType.JUROR_NUMBER));
            doReturn(
                new HelperEntry<>("testId2_1", "testValue2_1"),
                new HelperEntry<>("testId2_2", "testValue2_2"),
                new HelperEntry<>("testId2_3", "testValue2_3"))
                .when(report).getDataFromReturnType(any(), eq(DataType.FIRST_NAME));

            doReturn(
                new HelperEntry<>("testId3_1", "testValue3_1"),
                new HelperEntry<>("testId3_2", "testValue3_2"),
                new HelperEntry<>("testId3_3", "testValue3_3"))
                .when(report).getDataFromReturnType(any(), eq(DataType.LAST_NAME));

            StandardReportResponse.TableData tableData = report.tupleToTableData(data);


            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(3);
            assertThat(tableData.getHeadings()).containsAll(
                List.of(heading1, heading2, heading3)
            );
            assertThat(tableData.getData()).hasSize(3);
            assertThat(tableData.getData()).containsAll(
                List.of(
                    new HelperMap()
                        .add("testId2_1", "testValue2_1")
                        .add("testId3_1", "testValue3_1"),
                    new HelperMap()
                        .add("testId1_2", "testValue1_2")
                        .add("testId2_2", "testValue2_2")
                        .add("testId3_2", "testValue3_2"),
                    new HelperMap()
                        .add("testId1_3", "testValue1_3")
                        .add("testId2_3", "testValue2_3")
                        .add("testId3_3", "testValue3_3")
                )
            );
        }

        static class HelperMap extends LinkedHashMap<String, Object> {

            HelperMap add(String key, Object value) {
                this.put(key, value);
                return this;
            }
        }

        @AllArgsConstructor
        @Getter
        @Setter
        static class HelperEntry<K, V> implements Map.Entry<K, V> {

            private K key;
            private V value;

            @Override
            public V setValue(V value) {
                this.value = value;
                return value;
            }
        }
    }


    @Nested
    @DisplayName("StandardReportResponse.TableData.Heading getHeading(DataType dataType)")
    class GetHeading {
        @Test
        void positiveTypical() {
            DataType dataType = mock(DataType.class);
            when(dataType.getId()).thenReturn("testId");
            when(dataType.getDisplayName()).thenReturn("Test Display Name");
            doReturn(String.class).when(dataType).getDataType();

            assertThat(createReport().getHeading(dataType))
                .isEqualTo(StandardReportResponse.TableData.Heading.builder()
                    .id("testId")
                    .name("Test Display Name")
                    .dataType(String.class.getSimpleName())
                    .build());
        }

        @Test
        void positiveHasReturnTypes() {
            DataType dataType = mock(DataType.class);
            when(dataType.getId()).thenReturn("testId");
            when(dataType.getDisplayName()).thenReturn("Test Display Name");
            doReturn(List.class).when(dataType).getDataType();


            DataType subDataType1 = mock(DataType.class);
            when(subDataType1.getId()).thenReturn("subId1");
            when(subDataType1.getDisplayName()).thenReturn("SubName1");
            doReturn(String.class).when(subDataType1).getDataType();

            DataType subDataType2 = mock(DataType.class);
            when(subDataType2.getId()).thenReturn("subId2");
            when(subDataType2.getDisplayName()).thenReturn("SubName2");
            doReturn(String.class).when(subDataType2).getDataType();

            DataType subDataType3 = mock(DataType.class);
            when(subDataType3.getId()).thenReturn("subId3");
            when(subDataType3.getDisplayName()).thenReturn("SubName3");
            doReturn(Long.class).when(subDataType3).getDataType();

            when(dataType.getReturnTypes()).thenReturn(
                new DataType[]{subDataType1, subDataType2, subDataType3}
            );

            assertThat(createReport().getHeading(dataType))
                .isEqualTo(StandardReportResponse.TableData.Heading.builder()
                    .id("testId")
                    .name("Test Display Name")
                    .dataType(List.class.getSimpleName())
                    .headings(
                        List.of(
                            StandardReportResponse.TableData.Heading.builder()
                                .id("subId1")
                                .name("SubName1")
                                .dataType(String.class.getSimpleName())
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("subId2")
                                .name("SubName2")
                                .dataType(String.class.getSimpleName())
                                .build(),
                            StandardReportResponse.TableData.Heading.builder()
                                .id("subId3")
                                .name("SubName3")
                                .dataType(Long.class.getSimpleName())
                                .build()

                        )
                    )
                    .build());
        }
    }


    @Nested
    @DisplayName("public Map.Entry<String, Object> getDataFromReturnType(Tuple tuple, DataType dataType)")
    class GetDataFromReturnType {

        DataType dataType;
        Expression<?> expression;
        Tuple tuple;
        private static final String ID = "SomeId";

        <T> void setupNoReturnTypes(T value) {
            dataType = mock(DataType.class);
            when(dataType.getReturnTypes()).thenReturn(null);
            when(dataType.getId()).thenReturn(ID);
            expression = mock(Expression.class);
            doReturn(expression).when(dataType).getExpression();
            tuple = mock(Tuple.class);
            doReturn(value).when(tuple).get(expression);
        }

        @Test
        void positiveNoReturnTypesString() {
            setupNoReturnTypes("SomeValue");
            assertThat(createReport().getDataFromReturnType(tuple, dataType))
                .isEqualTo(Map.entry(ID, "SomeValue"));
        }

        @Test
        void positiveNoReturnTypesLong() {
            setupNoReturnTypes(123L);
            assertThat(createReport().getDataFromReturnType(tuple, dataType))
                .isEqualTo(Map.entry(ID, 123L));
        }

        @Test
        void positiveNoReturnTypesLocalDate() {
            setupNoReturnTypes(LocalDate.of(2023, 1, 2));
            assertThat(createReport().getDataFromReturnType(tuple, dataType))
                .isEqualTo(Map.entry(ID, "2023-01-02"));
        }

        @Test
        void positiveNoReturnTypesLocalTime() {
            setupNoReturnTypes(LocalTime.of(11, 2));
            assertThat(createReport().getDataFromReturnType(tuple, dataType))
                .isEqualTo(Map.entry(ID, "11:02:00"));
        }

        @Test
        void positiveNoReturnTypesLocalDateTime() {
            setupNoReturnTypes(LocalDateTime.of(2023, 1, 2, 11, 2));
            assertThat(createReport().getDataFromReturnType(tuple, dataType))
                .isEqualTo(Map.entry(ID, "2023-01-02T11:02:00"));
        }

        @Test
        void positiveHasReturnTypes() {
            tuple = mock(Tuple.class);
            dataType = mock(DataType.class);
            when(dataType.getId()).thenReturn(ID);

            DataType subDataType1 = mock(DataType.class);
            doReturn("subDataTypeId1").when(subDataType1).getId();
            Expression<?> subExpression1 = mock(Expression.class);
            doReturn(subExpression1).when(subDataType1).getExpression();
            doReturn("Value1").when(tuple).get(subExpression1);

            DataType subDataType2 = mock(DataType.class);
            doReturn("subDataTypeId2").when(subDataType2).getId();
            Expression<?> subExpression2 = mock(Expression.class);
            doReturn(subExpression2).when(subDataType2).getExpression();
            doReturn(123L).when(tuple).get(subExpression2);

            DataType subDataType3 = mock(DataType.class);
            doReturn("subDataTypeId3").when(subDataType3).getId();
            Expression<?> subExpression3 = mock(Expression.class);
            doReturn(subExpression3).when(subDataType3).getExpression();
            doReturn("Value3").when(tuple).get(subExpression3);

            doReturn(expression).when(dataType).getExpression();

            when(dataType.getReturnTypes()).thenReturn(new DataType[]{subDataType1, subDataType2, subDataType3});


            assertThat(createReport().getDataFromReturnType(tuple, dataType))
                .isEqualTo(Map.entry(ID, Map.of(
                    subDataType1.getId(), "Value1",
                    subDataType2.getId(), 123L,
                    subDataType3.getId(), "Value3"
                )));
        }
    }

    @Nested
    @DisplayName("List<Tuple> getData(StandardReportRequest request)")
    class GetData {
        @Test
        void positiveTypical() {
            JPAQuery<Tuple> query = mock(JPAQuery.class);
            StandardReportRequest request = mock(StandardReportRequest.class);
            AbstractReport report = createReport();

            doReturn(query).when(report).getQuery();
            doNothing().when(report).addJoins(any());
            doNothing().when(report)
                .preProcessQuery(any(), any());
            List<Tuple> result = mock(List.class);
            doReturn(result).when(query).fetch();

            assertThat(report.getData(request)).isEqualTo(result);


            verify(report, times(1)).addJoins(query);
            verify(report, times(1)).getQuery();
            verify(report, times(1)).getData(request);
            verify(report, times(1)).preProcessQuery(query, request);
            verify(query, times(1)).fetch();

            verifyNoMoreInteractions(query, report, request);

        }
    }

    @Nested
    @DisplayName("JPAQuery<Tuple> getQuery()")
    class GetQuery {
        @Test
        void positiveTypical() {
            AbstractReport report = createReport(QJuror.juror, DataType.FIRST_NAME, DataType.STATUS);
            JPAQueryFactory queryFactory = mock(JPAQueryFactory.class);

            doReturn(queryFactory).when(report).getQueryFactory();

            JPAQuery<Tuple> query = mock(JPAQuery.class,
                withSettings().defaultAnswer(RETURNS_SELF));
            doReturn(query).when(queryFactory).select(any(Expression[].class));
            assertThat(report.getQuery()).isEqualTo(query);

            verify(queryFactory).select(
                QJuror.juror.firstName,
                QJurorPool.jurorPool.status.statusDesc
            );
            verify(query).from(QJuror.juror);
            verifyNoMoreInteractions(query, queryFactory);
        }
    }

    @Nested
    @DisplayName("void addJoins(JPAQuery<Tuple> query)")
    class AddJoins {
        @Test
        void positiveTypical() {
            JPAQuery<Tuple> query = mock(JPAQuery.class, withSettings()
                .defaultAnswer(RETURNS_SELF));
            AbstractReport report =
                createReport(QJuror.juror, DataType.FIRST_NAME, DataType.LAST_NAME, DataType.STATUS);
            report.addJoins(query);

            verify(query, times(1))
                .join(QJurorPool.jurorPool);
            verify(query, times(1))
                .on(new Predicate[]{QJurorPool.jurorPool.juror.eq(QJuror.juror)});
            verifyNoMoreInteractions(query);
        }

        @Test
        void positiveRequiredTableIsSameAsFrom() {
            JPAQuery<Tuple> query = mock(JPAQuery.class, withSettings()
                .defaultAnswer(RETURNS_SELF));
            AbstractReport report =
                createReport(QJuror.juror, DataType.FIRST_NAME, DataType.LAST_NAME);
            report.addJoins(query);
            verifyNoInteractions(query);
        }

        @Test
        void negativePrimaryMissingJoinMapping() {
            JPAQuery<Tuple> query = mock(JPAQuery.class, withSettings()
                .defaultAnswer(RETURNS_SELF));

            DataType dataType = mock(DataType.class);
            when(dataType.getRequiredTables()).thenReturn(List.of(QBureauJurorCjs.bureauJurorCjs));

            MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
                () -> createReport(
                    QJuror.juror,
                    dataType
                ).addJoins(query),
                "Expected exception to be thrown when primary join is not found");

            assertThat(exception.getMessage()).isEqualTo("No join found for bureauJurorCjs");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void negativeSecondaryMissingJoinMapping() {
            JPAQuery<Tuple> query = mock(JPAQuery.class, withSettings()
                .defaultAnswer(RETURNS_SELF));


            EntityPath<?> from = mock(EntityPath.class);

            MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
                () -> createReport(
                    from,
                    DataType.FIRST_NAME
                ).addJoins(query),
                "Expected exception to be thrown when primary join is not found");

            assertThat(exception.getMessage()).isEqualTo("Not Implemented yet: juror from " + from.getClass());
            assertThat(exception.getCause()).isNull();
        }
    }


    @Nested
    @DisplayName("List<DataType> getDataType(DataType dataType)")
    class GetDataType {
        @Test
        void positiveTypicalNullReturnTypes() {
            assertThat(createReport().getDataType(DataType.FIRST_NAME))
                .isEqualTo(List.of(DataType.FIRST_NAME));
        }

        @Test
        void positiveTypicalHasReturnTypes() {
            assertThat(createReport().getDataType(DataType.CONTACT_DETAILS))
                .isEqualTo(List.of(DataType.MAIN_PHONE, DataType.OTHER_PHONE, DataType.WORK_PHONE, DataType.EMAIL));
        }
    }


    @Nested
    @DisplayName("public void addGroupBy(JPAQuery<Tuple> query, DataType... dataTypes)")
    class AddGroupBy {
        @Test
        void positiveTypical() {
            JPAQuery<Tuple> query = mock(JPAQuery.class);
            AbstractReport report = createReport();
            report.addGroupBy(query,
                DataType.JUROR_NUMBER,
                DataType.FIRST_NAME,
                DataType.CONTACT_DETAILS);

            verify(query, times(1)).groupBy(
                QJuror.juror.jurorNumber,
                QJuror.juror.firstName,
                QJuror.juror.phoneNumber,
                QJuror.juror.altPhoneNumber,
                QJuror.juror.workPhone,
                QJuror.juror.email
            );
        }
    }


    @Nested
    @DisplayName("void checkOwnership(PoolRequest poolRequest, boolean allowBureau)")
    class CheckOwnership {
        @Test
        void positiveSameOwnerCourt() {
            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequest.getOwner()).thenReturn("415");
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn("415");
            securityUtilMockedStatic.when(SecurityUtil::isBureau).thenReturn(false);


            assertDoesNotThrow(() -> createReport().checkOwnership(poolRequest, false),
                "No exception should be thrown when same owner");
        }

        @Test
        void negativeDifferentOwnerCourt() {
            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequest.getOwner()).thenReturn("415");
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn("414");
            securityUtilMockedStatic.when(SecurityUtil::isBureau).thenReturn(false);

            MojException.Forbidden exception = assertThrows(MojException.Forbidden.class,
                () -> createReport().checkOwnership(poolRequest, false),
                "Expected exception to be thrown when different owner");

            assertThat(exception.getMessage()).isEqualTo("User not allowed to access this pool");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void positiveDifferentOwnerBureauWithFlag() {
            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequest.getOwner()).thenReturn("415");
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn("400");
            securityUtilMockedStatic.when(SecurityUtil::isBureau).thenReturn(true);

            assertDoesNotThrow(() -> createReport().checkOwnership(poolRequest, true),
                "No exception should be thrown when different owner with bureau flag");
        }

        @Test
        void negativeDifferentOwnerBureauWithoutFlag() {
            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequest.getOwner()).thenReturn("415");
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn("400");
            securityUtilMockedStatic.when(SecurityUtil::isBureau).thenReturn(true);

            MojException.Forbidden exception = assertThrows(MojException.Forbidden.class,
                () -> createReport().checkOwnership(poolRequest, false),
                "Expected exception to be thrown when different owner");

            assertThat(exception.getMessage()).isEqualTo("User not allowed to access this pool");
            assertThat(exception.getCause()).isNull();
        }
    }


    @Nested
    @DisplayName("HashMap<String, StandardReportResponse.DataTypeValue> loadStandardPoolHeaders("
        + "        StandardReportRequest request, boolean ownerMustMatch, boolean allowBureau)")
    class LoadStandardPoolHeaders {
        @Test
        void positiveTypical() {
            PoolRequest poolRequest = mock(PoolRequest.class);

            AbstractReport report = createReport();
            doReturn(poolRequest).when(report).getPoolRequest(any());
            PoolType poolType = mock(PoolType.class);
            when(poolType.getDescription()).thenReturn("Pool Type desc");
            when(poolRequest.getPoolType()).thenReturn(poolType);
            LocalDate returnDate = LocalDate.of(2023, 2, 1);

            when(poolRequest.getReturnDate()).thenReturn(returnDate);

            StandardReportRequest request = mock(StandardReportRequest.class);
            when(request.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);

            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getName()).thenReturn("Court Name");
            when(courtLocation.getLocCode()).thenReturn("LOC");
            when(poolRequest.getCourtLocation()).thenReturn(courtLocation);

            assertThat(report.loadStandardPoolHeaders(
                request, false, false))
                .isEqualTo(Map.of(
                    "pool_number",
                    StandardReportResponse.DataTypeValue.builder()
                        .displayName("Pool Number")
                        .dataType(String.class.getSimpleName())
                        .value(TestConstants.VALID_POOL_NUMBER)
                        .build(),
                    "pool_type", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Pool Type")
                        .dataType(String.class.getSimpleName())
                        .value("Pool Type desc")
                        .build(),
                    "service_start_date", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Service Start Date")
                        .dataType(LocalDate.class.getSimpleName())
                        .value("2023-02-01")
                        .build(),
                    "court_name", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Court Name")
                        .dataType(String.class.getSimpleName())
                        .value("Court Name (LOC)")
                        .build()

                ));

            verify(report, times(1))
                .getPoolRequest(TestConstants.VALID_POOL_NUMBER);
            verify(request, times(2)).getPoolNumber();
            verify(poolRequest, times(1)).getPoolType();
            verify(poolType, times(1)).getDescription();
            verify(poolRequest, times(1)).getReturnDate();
            verify(courtLocation, times(1)).getName();
            verify(courtLocation, times(1)).getLocCode();
            verify(poolRequest, times(2)).getCourtLocation();
            verify(report, never()).checkOwnership(any(), anyBoolean());
            verifyNoMoreInteractions(poolRequest, poolType, courtLocation, request);
        }

        @Test
        void positiveTypicalOwnersMustMatch() {
            PoolRequest poolRequest = mock(PoolRequest.class);

            AbstractReport report = createReport();
            doReturn(poolRequest).when(report).getPoolRequest(any());
            PoolType poolType = mock(PoolType.class);
            when(poolType.getDescription()).thenReturn("Pool Type desc");
            when(poolRequest.getPoolType()).thenReturn(poolType);
            LocalDate returnDate = LocalDate.of(2023, 2, 1);

            when(poolRequest.getReturnDate()).thenReturn(returnDate);

            StandardReportRequest request = mock(StandardReportRequest.class);
            when(request.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            doNothing().when(report).checkOwnership(any(), anyBoolean());
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getName()).thenReturn("Court Name");
            when(courtLocation.getLocCode()).thenReturn("LOC");
            when(poolRequest.getCourtLocation()).thenReturn(courtLocation);

            assertThat(report.loadStandardPoolHeaders(
                request, true, false))
                .isEqualTo(Map.of(
                    "pool_number",
                    StandardReportResponse.DataTypeValue.builder()
                        .displayName("Pool Number")
                        .dataType(String.class.getSimpleName())
                        .value(TestConstants.VALID_POOL_NUMBER)
                        .build(),
                    "pool_type", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Pool Type")
                        .dataType(String.class.getSimpleName())
                        .value("Pool Type desc")
                        .build(),
                    "service_start_date", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Service Start Date")
                        .dataType(LocalDate.class.getSimpleName())
                        .value("2023-02-01")
                        .build(),
                    "court_name", StandardReportResponse.DataTypeValue.builder()
                        .displayName("Court Name")
                        .dataType(String.class.getSimpleName())
                        .value("Court Name (LOC)")
                        .build()

                ));

            verify(report, times(1))
                .getPoolRequest(TestConstants.VALID_POOL_NUMBER);
            verify(request, times(2)).getPoolNumber();
            verify(poolRequest, times(1)).getPoolType();
            verify(poolType, times(1)).getDescription();
            verify(poolRequest, times(1)).getReturnDate();
            verify(courtLocation, times(1)).getName();
            verify(courtLocation, times(1)).getLocCode();
            verify(poolRequest, times(2)).getCourtLocation();
            verify(report, times(1)).checkOwnership(poolRequest, false);
            verifyNoMoreInteractions(poolRequest, poolType, courtLocation, request);
        }
    }

    @Nested
    @DisplayName("PoolRequest getPoolRequest(String poolNumber)")
    class GetPoolRequest {
        @Test
        void positiveTypical() {
            PoolRequest poolRequest = mock(PoolRequest.class);
            when(poolRequestRepository.findByPoolNumber(TestConstants.VALID_POOL_NUMBER))
                .thenReturn(Optional.of(poolRequest));
            assertThat(createReport().getPoolRequest(TestConstants.VALID_POOL_NUMBER))
                .isEqualTo(poolRequest);
        }

        @Test
        void negativeNotFound() {
            when(poolRequestRepository.findByPoolNumber(TestConstants.VALID_POOL_NUMBER))
                .thenReturn(Optional.empty());

            MojException.NotFound notFoundException =
                assertThrows(MojException.NotFound.class,
                    () -> createReport().getPoolRequest(TestConstants.VALID_POOL_NUMBER),
                    "Expected exception to be thrown when pool not found");
            assertThat(notFoundException.getMessage()).isEqualTo("Pool not found");
            assertThat(notFoundException.getCause()).isNull();
        }
    }


    private AbstractReport createReport(EntityPath<?> from, DataType... dataTypes) {
        return spy(new AbstractReportTestImpl(poolRequestRepository,
            from, dataTypes));
    }

    private AbstractReport createReport() {
        return createReport(QJuror.juror, DataType.JUROR_NUMBER);
    }

    private class AbstractReportTestImpl extends AbstractReport {

        public AbstractReportTestImpl(PoolRequestRepository poolRequestRepository,
                                      EntityPath<?> from, DataType... dataType) {
            super(poolRequestRepository, from, dataType);
        }

        @Override
        public Class<?> getRequestValidatorClass() {
            throw new UnsupportedOperationException();//Only use don service layer
        }

        @Override
        protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
            fail("TODO");
        }

        @Override
        public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            StandardReportResponse.TableData tableData) {
            return new HashMap<>();
        }
    }
}
