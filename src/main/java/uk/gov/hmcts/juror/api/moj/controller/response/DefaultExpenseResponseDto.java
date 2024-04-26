package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DefaultExpenseResponseDto {

    @JsonProperty("financial_loss")
    private BigDecimal financialLoss;

    @JsonProperty("travel_time")
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    private LocalTime travelTime;

    @JsonProperty("mileage")
    private Integer distanceTraveledMiles;

    @JsonProperty("smart_card")
    private String smartCardNumber;

    @JsonProperty("claiming_subsistence_allowance")
    private boolean claimingSubsistenceAllowance;
}