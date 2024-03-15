package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

import java.math.BigDecimal;

public class DailyExpenseTravelTest extends AbstractValidatorTest<DailyExpenseTravel> {


    public static DailyExpenseTravel getValidObject() {
        return DailyExpenseTravel.builder()
            .traveledByCar(true)
            .build();
    }

    @Override
    protected DailyExpenseTravel createValidObject() {
        return getValidObject();
    }

    @Nested
    class TraveledByCarTest extends AbstractValidationFieldTestBase<Boolean> {
        protected TraveledByCarTest() {
            super("traveledByCar", DailyExpenseTravel::setTraveledByCar);
            addNotRequiredTest(null);
        }
    }

    @Nested
    class JurorsTakenCarTest extends AbstractValidationFieldTestInteger {
        protected JurorsTakenCarTest() {
            super("jurorsTakenCar", DailyExpenseTravel::setJurorsTakenCar);
            addNotRequiredTest(null);
            addMin(0, null);
            addMax(1_000_000,null);
        }
    }

    @Nested
    class TraveledByMotorcycleTest extends AbstractValidationFieldTestBase<Boolean> {
        protected TraveledByMotorcycleTest() {
            super("traveledByMotorcycle", DailyExpenseTravel::setTraveledByMotorcycle);
            addNotRequiredTest(null);
        }
    }

    @Nested
    class JurorsTakenMotorcycleTest extends AbstractValidationFieldTestInteger {
        protected JurorsTakenMotorcycleTest() {
            super("jurorsTakenMotorcycle", DailyExpenseTravel::setJurorsTakenMotorcycle);
            addNotRequiredTest(null);
            addMin(0, null);
            addMax(1_000_000,null);
        }
    }

    @Nested
    class TraveledByBicycleTest extends AbstractValidationFieldTestBase<Boolean> {
        protected TraveledByBicycleTest() {
            super("traveledByBicycle", DailyExpenseTravel::setTraveledByBicycle);
            addNotRequiredTest(null);
        }
    }

    @Nested
    class MilesTraveledTest extends AbstractValidationFieldTestInteger {
        protected MilesTraveledTest() {
            super("milesTraveled", DailyExpenseTravel::setMilesTraveled);
            addNotRequiredTest(null);
            addMin(0, null);
            addMax(1_000_000,null);
        }
    }

    @Nested
    class ParkingTest extends AbstractValidationFieldTestBigDecimal {
        protected ParkingTest() {
            super("parking", DailyExpenseTravel::setParking);
            addNotRequiredTest(null);
            addMin(BigDecimal.ZERO, null);
            addMax(new BigDecimal("1000000"),null);
        }
    }

    @Nested
    class PublicTransportTest extends AbstractValidationFieldTestBigDecimal {
        protected PublicTransportTest() {
            super("publicTransport", DailyExpenseTravel::setPublicTransport);
            addNotRequiredTest(null);
            addMin(BigDecimal.ZERO, null);
            addMax(new BigDecimal("1000000"),null);
        }
    }

    @Nested
    class TaxiTest extends AbstractValidationFieldTestBigDecimal {
        protected TaxiTest() {
            super("taxi", DailyExpenseTravel::setTaxi);
            addNotRequiredTest(null);
            addMin(BigDecimal.ZERO, null);
            addMax(new BigDecimal("1000000"),null);
        }
    }
}
