package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.DATE_FORMAT;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing juror detail and completion date")
public class JurorDetailsDto {

    @JsonProperty("juror_number")
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jurorNumber;

    @JsonProperty("pool_number")
    @Schema(description = "Pool number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String poolNumber;

    @JsonProperty("first_name")
    @Schema(description = "The Juror's first name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @JsonProperty("last_name")
    @Schema(description = "The Juror's last name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @JsonProperty("postcode")
    @Schema(description = "The Juror's postcode")
    private String postCode;

    @JsonProperty("completion_date")
    @Schema(description = "The Juror's completion date")
    @JsonFormat(pattern = DATE_FORMAT)
    private LocalDate completionDate;
}
