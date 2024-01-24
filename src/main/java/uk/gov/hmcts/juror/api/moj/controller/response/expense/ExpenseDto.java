package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode
public class ExpenseDto {

    @JsonProperty("public_transport")
    @Builder.Default
    @NotNull
    protected BigDecimal publicTransport = BigDecimal.ZERO;

    @JsonProperty("taxi")
    @Builder.Default
    @NotNull
    protected BigDecimal taxi = BigDecimal.ZERO;

    @JsonProperty("motorcycle")
    @Builder.Default
    @NotNull
    protected BigDecimal motorcycle = BigDecimal.ZERO;

    @JsonProperty("car")
    @Builder.Default
    @NotNull
    protected BigDecimal car = BigDecimal.ZERO;

    @JsonProperty("bicycle")
    @Builder.Default
    @NotNull
    protected BigDecimal bicycle = BigDecimal.ZERO;

    @JsonProperty("parking")
    @Builder.Default
    @NotNull
    protected BigDecimal parking = BigDecimal.ZERO;

    @JsonProperty("food_and_drink")
    @Builder.Default
    @NotNull
    protected BigDecimal foodAndDrink = BigDecimal.ZERO;

    @JsonProperty("loss_of_earnings")
    @Builder.Default
    @NotNull
    protected BigDecimal lossOfEarnings = BigDecimal.ZERO;

    @JsonProperty("extra_care")
    @Builder.Default
    @NotNull
    protected BigDecimal extraCare = BigDecimal.ZERO;

    @JsonProperty("other")
    @Builder.Default
    @NotNull
    protected BigDecimal other = BigDecimal.ZERO;

    @JsonProperty("smart_card")
    @Builder.Default
    @NotNull
    protected BigDecimal smartCard = BigDecimal.ZERO;

    @JsonProperty("total")
    public BigDecimal getTotal() {
        return publicTransport
            .add(taxi)
            .add(motorcycle)
            .add(car)
            .add(bicycle)
            .add(parking)
            .add(foodAndDrink)
            .add(lossOfEarnings)
            .add(extraCare)
            .add(other)
            .subtract(smartCard);
    }

    public ExpenseDto addExpenseDto(ExpenseDto expenseEntryDto) {
        this.publicTransport = this.publicTransport.add(expenseEntryDto.getPublicTransport());
        this.taxi = this.taxi.add(expenseEntryDto.getTaxi());
        this.motorcycle = this.motorcycle.add(expenseEntryDto.getMotorcycle());
        this.car = this.car.add(expenseEntryDto.getCar());
        this.bicycle = this.bicycle.add(expenseEntryDto.getBicycle());
        this.parking = this.parking.add(expenseEntryDto.getParking());
        this.foodAndDrink = this.foodAndDrink.add(expenseEntryDto.getFoodAndDrink());
        this.lossOfEarnings = this.lossOfEarnings.add(expenseEntryDto.getLossOfEarnings());
        this.extraCare = this.extraCare.add(expenseEntryDto.getExtraCare());
        this.other = this.other.add(expenseEntryDto.getOther());
        this.smartCard = this.smartCard.add(expenseEntryDto.getSmartCard());
        return this;
    }
}
