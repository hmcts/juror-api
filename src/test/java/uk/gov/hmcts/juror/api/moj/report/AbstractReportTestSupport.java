package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.ExcessiveImports",
})
public abstract class AbstractReportTestSupport<
    T, R extends AbstractReport<T>> {

    private final EntityPath<?> from;
    private final IDataType[] dataTypes;
    private final Class<?> validatorClass;
    protected R report;
    private PoolRequestRepository poolRequestRepository;
    @Setter
    private boolean hasPoolRepository = true;

    private final Validator validator;

    public abstract R createReport(PoolRequestRepository poolRequestRepository);

    public AbstractReportTestSupport(EntityPath<?> from,
                                     Class<?> validatorClass, IDataType... dataTypes) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
        validatorFactory.close();
        this.poolRequestRepository = mock(PoolRequestRepository.class);
        this.from = from;
        this.dataTypes = dataTypes.clone();
        this.validatorClass = validatorClass;
    }

    @BeforeEach
    public void beforeEach() {
        this.poolRequestRepository = mock(PoolRequestRepository.class);
        this.report = spy(createReport(poolRequestRepository));
        doNothing().when(report).addGroupBy(any(), any());
    }

    protected abstract StandardReportRequest getValidRequest();

    @Test
    void negativeRequestMissingReportType() {
        StandardReportRequest request = getValidRequest();
        request.setReportType(null);
        assertValidationFails(request, new ValidationFailure("reportType", "must not be blank"));
    }

    protected final List<ConstraintViolation<StandardReportRequest>> validateRequest(StandardReportRequest request) {
        return new ArrayList<>(validator.validate(request, validatorClass));
    }


    @Test
    void positiveConstructor() {
        assertThat(report).isNotNull();
        if (hasPoolRepository) {
            assertThat(report.getPoolRequestRepository()).isEqualTo(poolRequestRepository);
        } else {
            assertThat(report.getPoolRequestRepository()).isNull();
        }
        assertThat(report.getFrom()).isEqualTo(from);
        assertThat(report.getDataTypes()).containsExactly(dataTypes);
        //Other fields don't need testing as they will be tested via the dedicated AbstractReportTest
    }

    public abstract void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request);

    @Test
    @SuppressWarnings({
        "unchecked",
        "PMD.JUnitTestsShouldIncludeAssert",
    })
    final void positivePreProcessQueryTypical() {
        JPAQuery<Tuple> query = mock(JPAQuery.class,
            withSettings().defaultAnswer(RETURNS_SELF));
        StandardReportRequest request = getValidRequest();
        positivePreProcessQueryTypical(query, request);
        verify(report, times(1))
            .preProcessQuery(query, request);
        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveGetRequestValidatorClass() {
        assertThat(report.getRequestValidatorClass()).isEqualTo(validatorClass);
    }

    public abstract Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<T> tableData,
        T data);

    @Test
    @SuppressWarnings("unchecked")
    final void positiveGetHeadingsTypical() {
        StandardReportRequest request = spy(getValidRequest());
        AbstractReportResponse.TableData<T> tableData = mock(AbstractReportResponse.TableData.class);
        T data = spy(createData());
        doReturn(data).when(tableData).getData();
        Map<String, AbstractReportResponse.DataTypeValue> standardPoolMappings = getStandardPoolHeaders();
        doReturn(standardPoolMappings).when(report).loadStandardPoolHeaders(request, true, true);
        Map<String, AbstractReportResponse.DataTypeValue> headings =
            positiveGetHeadingsTypical(request, tableData, data);
        //Is set via getStandardReportResponse so should not be set here
        assertThat(headings).isNotNull().doesNotContainKey("report_created");
        verify(report, times(1)).getHeadings(request, tableData);
    }

    protected abstract T createData();

    protected final Map<String, AbstractReportResponse.DataTypeValue> getStandardPoolHeaders() {
        return new ConcurrentHashMap<>(
            Map.of(
                "pool_number", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Pool Number")
                    .dataType("String")
                    .value(TestConstants.VALID_POOL_NUMBER)
                    .build(),
                "pool_type", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Pool Type")
                    .dataType("String")
                    .value("CROWN COURT")
                    .build(),
                "service_start_date", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Service Start Date")
                    .dataType("LocalDate")
                    .value(DateTimeFormatter.ISO_DATE.format(LocalDate.of(2023, 1, 1)))
                    .build(),
                "court_name", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType("String")
                    .value("CHESTER (415)")
                    .build()
            )
        );
    }

    protected final Map.Entry<String, AbstractReportResponse.DataTypeValue> getCourtNameEntry() {
        return new AbstractMap.SimpleEntry<>("court_name", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value("CHESTER (415)")
            .build());
    }

    protected final void assertHeadingContains(Map<String, AbstractReportResponse.DataTypeValue> actualMap,
                                               Map<String, AbstractReportResponse.DataTypeValue> expectedData) {
        assertHeadingContains(actualMap, null, false, expectedData);
    }

    protected final void assertHeadingContains(Map<String, AbstractReportResponse.DataTypeValue> actualMap,
                                               StandardReportRequest request,
                                               boolean hasStandardPoolHeaders,
                                               Map<String, AbstractReportResponse.DataTypeValue> expectedData) {
        HashMap<String, AbstractReportResponse.DataTypeValue> standardPoolMappings = new HashMap<>(expectedData);
        if (hasStandardPoolHeaders) {
            standardPoolMappings.putAll(getStandardPoolHeaders());
        }
        assertThat(actualMap).hasSize(standardPoolMappings.size());
        assertThat(actualMap).containsExactlyInAnyOrderEntriesOf(standardPoolMappings);
        if (hasStandardPoolHeaders) {
            verify(report, times(1)).loadStandardPoolHeaders(request, true, true);
        }
    }

    protected void assertValidationFails(StandardReportRequest request, ValidationFailure... validationFailures) {
        List<ConstraintViolation<StandardReportRequest>> violations = validateRequest(request);
        assertThat(violations).hasSize(validationFailures.length);
        assertThat(violations.stream()
            .map(ValidationFailure::new)
            .toList())
            .isEqualTo(List.of(validationFailures));
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class ValidationFailure {
        private final String property;
        private final String message;

        public ValidationFailure(ConstraintViolation<StandardReportRequest> standardReportRequestConstraintViolation) {
            this.property = standardReportRequestConstraintViolation.getPropertyPath().toString();
            this.message = standardReportRequestConstraintViolation.getMessage();
        }
    }
}
