package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpenseDtoTest extends AbstractValidatorTest<ExpenseDto> {

    @Test
    void positiveGetTotal() {
        ExpenseDto expenseDto = ExpenseDto.builder()
            .publicTransport(new BigDecimal("1.22"))
            .taxi(new BigDecimal("10.00"))
            .motorcycle(new BigDecimal("100.00"))
            .car(new BigDecimal("1000.00"))
            .bicycle(new BigDecimal("10000.00"))
            .parking(new BigDecimal("100000.00"))
            .foodAndDrink(new BigDecimal("1000000.00"))
            .extraCare(new BigDecimal("10000000.00"))
            .other(new BigDecimal("100000000.00"))
            .smartCard(new BigDecimal("0.11"))
            .build();
        assertEquals(new BigDecimal("111111111.11"), expenseDto.getTotal(),
            "Total should match");
    }

    @Test
    void positiveAddExpenseDto() {
        ExpenseDto origionalExpenseDto = ExpenseDto.builder()
            .publicTransport(new BigDecimal("1.00"))
            .taxi(new BigDecimal("2.00"))
            .motorcycle(new BigDecimal("3.00"))
            .car(new BigDecimal("4.00"))
            .bicycle(new BigDecimal("5.00"))
            .parking(new BigDecimal("6.00"))
            .foodAndDrink(new BigDecimal("7.00"))
            .extraCare(new BigDecimal("8.00"))
            .other(new BigDecimal("9.00"))
            .smartCard(new BigDecimal("10.00"))
            .build();

        ExpenseDto expenseDtoToAdd = ExpenseDto.builder()
            .publicTransport(new BigDecimal("1.01"))
            .taxi(new BigDecimal("2.02"))
            .motorcycle(new BigDecimal("3.03"))
            .car(new BigDecimal("4.04"))
            .bicycle(new BigDecimal("5.05"))
            .parking(new BigDecimal("6.06"))
            .foodAndDrink(new BigDecimal("7.07"))
            .extraCare(new BigDecimal("8.08"))
            .other(new BigDecimal("9.09"))
            .smartCard(new BigDecimal("10.10"))
            .build();

        ExpenseDto expectedExpenseDto = ExpenseDto.builder()
            .publicTransport(new BigDecimal("2.01"))
            .taxi(new BigDecimal("4.02"))
            .motorcycle(new BigDecimal("6.03"))
            .car(new BigDecimal("8.04"))
            .bicycle(new BigDecimal("10.05"))
            .parking(new BigDecimal("12.06"))
            .foodAndDrink(new BigDecimal("14.07"))
            .extraCare(new BigDecimal("16.08"))
            .other(new BigDecimal("18.09"))
            .smartCard(new BigDecimal("20.10"))
            .build();

        assertEquals(expectedExpenseDto, origionalExpenseDto.addExpenseDto(expenseDtoToAdd),
            "Expenses should add correctly");
    }

    @Override
    protected ExpenseDto createValidObject() {
        return new ExpenseDto();
    }

    @Nested
    class PublicTransport extends AbstractValidationFieldTestBigDecimal {
        protected PublicTransport() {
            super("publicTransport", ExpenseDto::setPublicTransport);
            addRequiredTest(null);
        }
    }

    @Nested
    class TaxiTest extends AbstractValidationFieldTestBigDecimal {
        protected TaxiTest() {
            super("taxi", ExpenseDto::setTaxi);
            addRequiredTest(null);
        }
    }

    @Nested
    class Motorcycle extends AbstractValidationFieldTestBigDecimal {
        protected Motorcycle() {
            super("motorcycle", ExpenseDto::setMotorcycle);
            addRequiredTest(null);
        }
    }

    @Nested
    class CarTest extends AbstractValidationFieldTestBigDecimal {
        protected CarTest() {
            super("car", ExpenseDto::setCar);
            addRequiredTest(null);
        }
    }

    @Nested
    class Bicycle extends AbstractValidationFieldTestBigDecimal {
        protected Bicycle() {
            super("bicycle", ExpenseDto::setBicycle);
            addRequiredTest(null);
        }
    }

    @Nested
    class Parking extends AbstractValidationFieldTestBigDecimal {
        protected Parking() {
            super("parking", ExpenseDto::setParking);
            addRequiredTest(null);
        }
    }

    @Nested
    class FoodAndDrink extends AbstractValidationFieldTestBigDecimal {
        protected FoodAndDrink() {
            super("foodAndDrink", ExpenseDto::setFoodAndDrink);
            addRequiredTest(null);
        }
    }

    @Nested
    class LossOfEarnings extends AbstractValidationFieldTestBigDecimal {
        protected LossOfEarnings() {
            super("lossOfEarnings", ExpenseDto::setLossOfEarnings);
            addRequiredTest(null);
        }
    }

    @Nested
    class ExtraCare extends AbstractValidationFieldTestBigDecimal {
        protected ExtraCare() {
            super("extraCare", ExpenseDto::setExtraCare);
            addRequiredTest(null);
        }
    }

    @Nested
    class Other extends AbstractValidationFieldTestBigDecimal {
        protected Other() {
            super("other", ExpenseDto::setOther);
            addRequiredTest(null);
        }
    }

    @Nested
    class SmartCard extends AbstractValidationFieldTestBigDecimal {
        protected SmartCard() {
            super("smartCard", ExpenseDto::setSmartCard);
            addRequiredTest(null);
        }
    }
}
