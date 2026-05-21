package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.ExpenseNumericLimit;

import java.math.BigDecimal;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RequestDefaultExpensesDto {



    @ExpenseNumericLimit
    private BigDecimal financialLoss;


    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    private LocalTime travelTime;


    @ExpenseNumericLimit
    private int distanceTraveledMiles;


    @Length(max = 20)
    private String smartCardNumber;

    @JsonProperty("food_and_drink")
    boolean hasFoodAndDrink;
}
