package uk.gov.hmcts.juror.api.moj.report.bespoke;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;
import uk.gov.hmcts.juror.api.moj.service.JurorServiceMod;
import uk.gov.hmcts.juror.api.moj.service.UserService;
import uk.gov.hmcts.juror.api.moj.service.audit.JurorAuditService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.Mockito.mock;

public abstract class AbstractJurorAmendmentReportTestSupport<R extends AbstractJurorAmendmentReport> {

    private final Class<?> validatorClass;

    protected JurorAuditService jurorAuditService;
    protected JurorServiceMod jurorService;
    protected UserService userService;

    public AbstractJurorAmendmentReportTestSupport(Class<?> validatorClass) {
        this.validatorClass = validatorClass;
    }


    @BeforeEach
    void beforeEach() {
        this.jurorAuditService = mock(JurorAuditService.class);
        this.jurorService = mock(JurorServiceMod.class);
        this.userService = mock(UserService.class);
    }

    protected abstract StandardReportRequest getValidRequest();

    public abstract R createReport();


    protected void assertValidationFails(StandardReportRequest request,
                                         AbstractReportTestSupport.ValidationFailure... validationFailures) {
        List<ConstraintViolation<StandardReportRequest>> violations = validateRequest(request);
        assertThat(violations).hasSize(validationFailures.length);
        assertThat(violations.stream()
            .map(AbstractReportTestSupport.ValidationFailure::new)
            .toList())
            .isEqualTo(List.of(validationFailures));
    }

    protected final List<ConstraintViolation<StandardReportRequest>> validateRequest(StandardReportRequest request) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        List<ConstraintViolation<StandardReportRequest>> data =
            new ArrayList<>(validator.validate(request, this.validatorClass));
        validatorFactory.close();
        return data;
    }

    @Test
    void negativeRequestMissingReportType() {
        StandardReportRequest request = getValidRequest();
        request.setReportType(null);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("reportType", "must not be blank"));
    }

    protected void validateReportCreated(Map<String, AbstractReportResponse.DataTypeValue> headings) {
        AbstractReportResponse.DataTypeValue reportCreated = headings.get("report_created");

        assertThat(reportCreated).isNotNull();
        assertThat(LocalDateTime.parse(reportCreated.getValue().toString()))
            .isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));
        assertThat(reportCreated.getDisplayName()).isEqualTo(null);
        assertThat(reportCreated.getDataType()).isEqualTo("LocalDateTime");

        headings.remove("report_created");
    }


}
