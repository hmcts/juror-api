package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuppressWarnings("squid:NoSonar")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CombinedExpenseDetailsDto<T extends ExpenseDetailsDto> {


    private List<T> expenseDetails;

    private Total total;

    public CombinedExpenseDetailsDto() {
        expenseDetails = new ArrayList<>();
        total = new Total();
    }

    public void addExpenseDetail(T expenseDetailsDto) {
        expenseDetails.add(expenseDetailsDto);
        total.add(expenseDetailsDto);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Total extends ExpenseValuesDto {

        private int totalDays;

        public Total() {
            super();
            totalDays = 0;
        }

        public void add(ExpenseDetailsDto expenseDetailsDto) {
            totalDays++;
            lossOfEarnings = getLossOfEarnings().add(
                expenseDetailsDto.getLossOfEarnings());
            extraCare = getExtraCare().add(expenseDetailsDto.getExtraCare());
            other = getOther().add(expenseDetailsDto.getOther());
            publicTransport = getPublicTransport().add(expenseDetailsDto.getPublicTransport());
            taxi = getTaxi().add(expenseDetailsDto.getTaxi());
            motorcycle = getMotorcycle().add(expenseDetailsDto.getMotorcycle());
            car = getCar().add(expenseDetailsDto.getCar());
            bicycle = getBicycle().add(expenseDetailsDto.getBicycle());
            parking = getParking().add(expenseDetailsDto.getParking());
            foodAndDrink = getFoodAndDrink().add(expenseDetailsDto.getFoodAndDrink());
            smartCard = getSmartCard().add(expenseDetailsDto.getSmartCard());
        }
    }
}
