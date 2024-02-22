package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings("PMD.TooManyMethods")
class ExpenseValuesDtoTest {
    @Test
    void positiveGetTotal() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setLossOfEarnings(BigDecimal.valueOf(1.1));
        expenseValuesDto.setExtraCare(BigDecimal.valueOf(1.2));
        expenseValuesDto.setOther(BigDecimal.valueOf(1.3));
        expenseValuesDto.setPublicTransport(BigDecimal.valueOf(1.4));
        expenseValuesDto.setTaxi(BigDecimal.valueOf(1.5));
        expenseValuesDto.setMotorcycle(BigDecimal.valueOf(1.6));
        expenseValuesDto.setCar(BigDecimal.valueOf(1.7));
        expenseValuesDto.setBicycle(BigDecimal.valueOf(1.8));
        expenseValuesDto.setParking(BigDecimal.valueOf(1.9));
        expenseValuesDto.setFoodAndDrink(BigDecimal.valueOf(2.0));
        expenseValuesDto.setSmartCard(BigDecimal.valueOf(2.1));

        assertThat(expenseValuesDto.getTotal()).isEqualTo(BigDecimal.valueOf(13.4));
    }

    @Test
    void positiveGetLossOfEarnings() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setLossOfEarnings(BigDecimal.valueOf(1.1));
        assertThat(expenseValuesDto.getLossOfEarnings()).isEqualTo(BigDecimal.valueOf(1.1));
    }

    @Test
    void positiveGetLossOfEarningsNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setLossOfEarnings(null);
        assertThat(expenseValuesDto.getLossOfEarnings()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetExtraCare() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setExtraCare(BigDecimal.valueOf(1.2));
        assertThat(expenseValuesDto.getExtraCare()).isEqualTo(BigDecimal.valueOf(1.2));
    }

    @Test
    void positiveGetExtraCareNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setExtraCare(null);
        assertThat(expenseValuesDto.getExtraCare()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetOther() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setOther(BigDecimal.valueOf(1.3));
        assertThat(expenseValuesDto.getOther()).isEqualTo(BigDecimal.valueOf(1.3));
    }

    @Test
    void positiveGetOtherNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setOther(null);
        assertThat(expenseValuesDto.getOther()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetPublicTransport() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setPublicTransport(BigDecimal.valueOf(1.4));
        assertThat(expenseValuesDto.getPublicTransport()).isEqualTo(BigDecimal.valueOf(1.4));
    }

    @Test
    void positiveGetPublicTransportNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setPublicTransport(null);
        assertThat(expenseValuesDto.getPublicTransport()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetTaxi() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setTaxi(BigDecimal.valueOf(1.5));
        assertThat(expenseValuesDto.getTaxi()).isEqualTo(BigDecimal.valueOf(1.5));
    }

    @Test
    void positiveGetTaxiNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setTaxi(null);
        assertThat(expenseValuesDto.getTaxi()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetMotorcycle() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setMotorcycle(BigDecimal.valueOf(1.6));
        assertThat(expenseValuesDto.getMotorcycle()).isEqualTo(BigDecimal.valueOf(1.6));
    }

    @Test
    void positiveGetMotorcycleNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setMotorcycle(null);
        assertThat(expenseValuesDto.getMotorcycle()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetCar() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setCar(BigDecimal.valueOf(1.7));
        assertThat(expenseValuesDto.getCar()).isEqualTo(BigDecimal.valueOf(1.7));
    }

    @Test
    void positiveGetCarNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setCar(null);
        assertThat(expenseValuesDto.getCar()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetBicycle() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setBicycle(BigDecimal.valueOf(1.8));
        assertThat(expenseValuesDto.getBicycle()).isEqualTo(BigDecimal.valueOf(1.8));
    }

    @Test
    void positiveGetBicycleNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setBicycle(null);
        assertThat(expenseValuesDto.getBicycle()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetParking() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setParking(BigDecimal.valueOf(1.9));
        assertThat(expenseValuesDto.getParking()).isEqualTo(BigDecimal.valueOf(1.9));
    }

    @Test
    void positiveGetParkingNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setParking(null);
        assertThat(expenseValuesDto.getParking()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetFoodAndDrink() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setFoodAndDrink(BigDecimal.valueOf(2.0));
        assertThat(expenseValuesDto.getFoodAndDrink()).isEqualTo(BigDecimal.valueOf(2.0));
    }

    @Test
    void positiveGetFoodAndDrinkNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setFoodAndDrink(null);
        assertThat(expenseValuesDto.getFoodAndDrink()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetSmartCard() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setSmartCard(BigDecimal.valueOf(2.1));
        assertThat(expenseValuesDto.getSmartCard()).isEqualTo(BigDecimal.valueOf(2.1));
    }

    @Test
    void positiveGetSmartCardNull() {
        ExpenseValuesDto expenseValuesDto = new ExpenseValuesDto();
        expenseValuesDto.setSmartCard(null);
        assertThat(expenseValuesDto.getSmartCard()).isEqualTo(BigDecimal.ZERO);
    }
}
