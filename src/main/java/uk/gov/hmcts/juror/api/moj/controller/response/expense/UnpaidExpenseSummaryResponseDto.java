package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Schema(description = "Summary of jurors with unpaid appearance expenses")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UnpaidExpenseSummaryResponseDto {

    @Schema(description = "9-digit numeric string to uniquely identify a juror")
    @JurorNumber
    String jurorNumber;

    @Schema(description = "9-digit numeric string to uniquely identify a pool request")
    @PoolNumber
    String poolNumber;

    @Schema(description = "Juror's first name")
    String firstName;

    @Schema(description = "Juror's last name")
    String lastName;

    @Schema(description = "Juror's last attendance date")
    LocalDate lastAttendanceDate;

    @Schema(description = "Sum of all unapproved expense items for a given juror in a given pool")
    BigDecimal totalUnapproved;

}
