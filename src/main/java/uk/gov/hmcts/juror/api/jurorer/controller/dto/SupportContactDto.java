package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Support contact information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Support contact details")
public class SupportContactDto {

    @JsonProperty("email")
    @Schema(description = "Support email address", example = "juror.support@hmcts.gov.uk")
    private String email;

    @JsonProperty("phone")
    @Schema(description = "Support phone number", example = "0300 456 1024")
    private String phone;

    @JsonProperty("hours")
    @Schema(description = "Support hours", example = "Monday to Friday, 9am to 5pm")
    private String hours;
}
