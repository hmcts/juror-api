package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDto;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpenseDtoTest extends AbstractValidatorTest {

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
    void positiveAddExpenseDto(){
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

        assertEquals(expectedExpenseDto,origionalExpenseDto.addExpenseDto(expenseDtoToAdd),
            "Expenses should add correctly");
    }


    class AbstractExpenseDtoTest extends AbstractValidatorTest.AbstractValidationFieldTestBase<ExpenseDto, BigDecimal> {

        private final BiConsumer<ExpenseDto, BigDecimal> setFieldConsumer;

        protected AbstractExpenseDtoTest(String fieldName, BiConsumer<ExpenseDto, BigDecimal> setFieldConsumer) {
            super(fieldName);
            this.setFieldConsumer = setFieldConsumer;
        }


        @Override
        protected void setField(ExpenseDto baseObject, BigDecimal value) {
            setFieldConsumer.accept(baseObject, value);
        }

        @Override
        protected ExpenseDto createValidObject() {
            return new ExpenseDto();
        }
    }

    @Nested
    class PublicTransport extends AbstractExpenseDtoTest {
        protected PublicTransport() {
            super("publicTransport", ExpenseDto::setPublicTransport);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class TaxiTest extends AbstractExpenseDtoTest {
        protected TaxiTest() {
            super("taxi", ExpenseDto::setTaxi);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class Motorcycle extends AbstractExpenseDtoTest {
        protected Motorcycle() {
            super("motorcycle", ExpenseDto::setMotorcycle);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class CarTest extends AbstractExpenseDtoTest {
        protected CarTest() {
            super("car", ExpenseDto::setCar);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class Bicycle extends AbstractExpenseDtoTest {
        protected Bicycle() {
            super("bicycle", ExpenseDto::setBicycle);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class Parking extends AbstractExpenseDtoTest {
        protected Parking() {
            super("parking", ExpenseDto::setParking);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class FoodAndDrink extends AbstractExpenseDtoTest {
        protected FoodAndDrink() {
            super("foodAndDrink", ExpenseDto::setFoodAndDrink);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class LossOfEarnings extends AbstractExpenseDtoTest {
        protected LossOfEarnings() {
            super("lossOfEarnings", ExpenseDto::setLossOfEarnings);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class ExtraCare extends AbstractExpenseDtoTest {
        protected ExtraCare() {
            super("extraCare", ExpenseDto::setExtraCare);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class Other extends AbstractExpenseDtoTest {
        protected Other() {
            super("other", ExpenseDto::setOther);
            addRequiredTest(new FieldTestSupport(null));
        }
    }

    @Nested
    class SmartCard extends AbstractExpenseDtoTest {
        protected SmartCard() {
            super("smartCard", ExpenseDto::setSmartCard);
            addRequiredTest(new FieldTestSupport(null));
        }
    }
}
