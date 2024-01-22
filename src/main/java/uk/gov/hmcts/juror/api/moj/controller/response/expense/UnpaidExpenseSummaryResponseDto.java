package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Schema(description = "Summary of jurors with unpaid appearance expenses")
public class UnpaidExpenseSummaryResponseDto {

    @JsonProperty("juror_number")
    @Schema(description = "9-digit numeric string to uniquely identify a juror")
    @JurorNumber
    String jurorNumber;

    @JsonProperty("pool_number")
    @Schema(description = "9-digit numeric string to uniquely identify a pool request")
    @PoolNumber
    String poolNumber;

    @JsonProperty("first_name")
    @Schema(description = "Juror's first name")
    String firstName;

    @JsonProperty("last_name")
    @Schema(description = "Juror's last name")
    String lastName;

    @JsonProperty("total_unapproved")
    @Schema(description = "Sum of all unapproved expense items for a given juror in a given pool")
    BigDecimal totalUnapproved;

}