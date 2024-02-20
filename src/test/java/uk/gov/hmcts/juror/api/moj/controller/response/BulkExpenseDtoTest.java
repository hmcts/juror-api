package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseEntryDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.util.List;

class BulkExpenseDtoTest extends AbstractValidatorTest<BulkExpenseDto> {

    @Override
    protected BulkExpenseDto createValidObject() {
        BulkExpenseDto bulkExpenseDto = new BulkExpenseDto();
        bulkExpenseDto.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
        bulkExpenseDto.setType(AppearanceStage.EXPENSE_ENTERED);
        bulkExpenseDto.setMileage(1);
        bulkExpenseDto.setExpenses(List.of(BulkExpenseEntryDtoTest.createValid()));
        bulkExpenseDto.setTotals(new TotalExpenseDto());
        return bulkExpenseDto;
    }

    @Nested
    class JurorNumber extends AbstractValidationFieldTestString {
        protected JurorNumber() {
            super("jurorNumber", BulkExpenseDto::setJurorNumber);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("12345678", "^\\d{9}$", null);
        }
    }

    @Nested
    class JurorVersion extends AbstractValidationFieldTestLong {
        protected JurorVersion() {
            super("jurorVersion", BulkExpenseDto::setJurorVersion);
            addNotRequiredTest(1L);
        }
    }

    @Nested
    class TypeTest extends AbstractValidationFieldTestBase<AppearanceStage> {
        protected TypeTest() {
            super("type", BulkExpenseDto::setType);
            addRequiredTest(null);
        }
    }

    @Nested
    class Mileage extends AbstractValidationFieldTestInteger {
        protected Mileage() {
            super("mileage", BulkExpenseDto::setMileage);
            addRequiredTest(null);
            addMustBePositive(null);
        }
    }

    @Nested
    class Expenses extends AbstractValidationFieldTestBase<List<BulkExpenseEntryDto>> {
        protected Expenses() {
            super("expenses", BulkExpenseDto::setExpenses);
            addRequiredTest(null);
        }
    }

    @Nested
    class Totals extends AbstractValidationFieldTestBase<TotalExpenseDto> {
        protected Totals() {
            super("totals", BulkExpenseDto::setTotals);
            addRequiredTest(null);
        }
    }
}
