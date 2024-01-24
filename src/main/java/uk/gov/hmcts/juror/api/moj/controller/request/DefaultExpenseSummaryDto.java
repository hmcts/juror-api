package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DefaultExpenseSummaryDto {

    @JsonProperty("juror_number")
    @Schema(description = "9-digit numeric string to uniquely identify a juror")
    @JurorNumber
    private String jurorNumber;

    @JsonProperty("financial_loss")
    private Double financialLoss;

    @JsonProperty("travel_time")
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    private LocalTime travelTime;

    @JsonProperty("mileage")
    private int distanceTraveledMiles;

    @JsonProperty("smart_card")
    private String smartCardNumber;

    @JsonProperty("amount_spent")
    @Schema(description = "Total spent for smart card")
    private Double totalSmartCardSpend;
}
