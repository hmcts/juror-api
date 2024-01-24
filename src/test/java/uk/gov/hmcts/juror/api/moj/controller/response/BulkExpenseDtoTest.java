package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseEntryDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.util.List;

public class BulkExpenseDtoTest extends AbstractValidatorTest {

    private BulkExpenseDto createValidBulkExpenseDto() {
        BulkExpenseDto bulkExpenseDto = new BulkExpenseDto();
        bulkExpenseDto.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
        bulkExpenseDto.setType(AppearanceStage.EXPENSE_ENTERED);
        bulkExpenseDto.setMileage(1);
        bulkExpenseDto.setExpenses(List.of(BulkExpenseEntryDtoTest.createValid()));
        bulkExpenseDto.setTotals(new TotalExpenseDto());
        return bulkExpenseDto;
    }

    @Nested
    class JurorNumber extends AbstractValidationFieldTestString<BulkExpenseDto> {
        protected JurorNumber() {
            super("jurorNumber");
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("12345678", "^\\d{9}$", null);
        }

        @Override
        protected void setField(BulkExpenseDto baseObject, String value) {
            baseObject.setJurorNumber(value);
        }

        @Override
        protected BulkExpenseDto createValidObject() {
            return createValidBulkExpenseDto();
        }
    }

    @Nested
    class JurorVersion extends AbstractValidationFieldTestNumeric<BulkExpenseDto, Long> {
        protected JurorVersion() {
            super("jurorVersion");
            addNotRequiredTest(1L);
        }


        @Override
        protected void setField(BulkExpenseDto baseObject, Long value) {
            baseObject.setJurorVersion(value);
        }

        @Override
        protected BulkExpenseDto createValidObject() {
            return createValidBulkExpenseDto();
        }

        @Override
        protected Long toNumber(String value) {
            return Long.parseLong(value);
        }
    }

    @Nested
    class TypeTest extends AbstractValidationFieldTestBase<BulkExpenseDto, AppearanceStage> {
        protected TypeTest() {
            super("type");
            addRequiredTest(null);
        }

        @Override
        protected void setField(BulkExpenseDto baseObject, AppearanceStage value) {
            baseObject.setType(value);
        }

        @Override
        protected BulkExpenseDto createValidObject() {
            return createValidBulkExpenseDto();
        }
    }

    @Nested
    class Mileage extends AbstractValidationFieldTestNumeric<BulkExpenseDto, Integer> {
        protected Mileage() {
            super("mileage");
            addRequiredTest(null);
            addMustBePositive(null);
        }

        @Override
        protected void setField(BulkExpenseDto baseObject, Integer value) {
            baseObject.setMileage(value);
        }

        @Override
        protected BulkExpenseDto createValidObject() {
            return createValidBulkExpenseDto();
        }

        @Override
        protected Integer toNumber(String value) {
            return Integer.parseInt(value);
        }
    }

    @Nested
    class Expenses extends AbstractValidationFieldTestBase<BulkExpenseDto, List<BulkExpenseEntryDto>> {
        protected Expenses() {
            super("expenses");
            addRequiredTest(null);
        }

        @Override
        protected void setField(BulkExpenseDto baseObject, List<BulkExpenseEntryDto> value) {
            baseObject.setExpenses(value);
        }

        @Override
        protected BulkExpenseDto createValidObject() {
            return createValidBulkExpenseDto();
        }
    }

    @Nested
    class Totals extends AbstractValidationFieldTestBase<BulkExpenseDto, TotalExpenseDto> {
        protected Totals() {
            super("totals");
            addRequiredTest(null);
        }


        @Override
        protected void setField(BulkExpenseDto baseObject, TotalExpenseDto value) {
            baseObject.setTotals(value);
        }

        @Override
        protected BulkExpenseDto createValidObject() {
            return createValidBulkExpenseDto();
        }
    }
}
