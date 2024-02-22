package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.math.BigDecimal;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RequestDefaultExpensesDto {

    @JsonProperty("juror_number")
    @Schema(description = "9-digit numeric string to uniquely identify a juror")
    @JurorNumber
    private String jurorNumber;

    //TODO check if can be null or not
    @JsonProperty("financial_loss")
    private BigDecimal financialLoss;

    @JsonProperty("travel_time")
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    private LocalTime travelTime;

    @JsonProperty("mileage")
    private int distanceTraveledMiles;

    @JsonProperty("smart_card")
    private String smartCardNumber;

    @JsonProperty("apply_to_all_draft")
    boolean overwriteExistingDraftExpenses;

    @JsonProperty("food_and_drink")
    boolean hasFoodAndDrink;
}