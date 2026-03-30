package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.time.LocalDateTime;

/**
 * Inbound request data when logging contact between the court's and bureau officers relating to a Juror Record.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Contact log request")
public class ContactLogRequestDto {

    @JsonProperty("jurorNumber")
    @Size(min = 9, max = 9)
    @JurorNumber
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "9 digit numeric string to identify a juror",
        example = "123456789")
    private String jurorNumber;

    @JsonProperty("startCall")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The date and time contact was made (call "
        + "started/email sent)",
        example = "2021-08-20 09:12:34")
    private LocalDateTime startCall;

    @JsonProperty("enquiryType")
    @Size(min = 2, max = 2)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Two character identifier for the enquiry type"
        + " of this call/email",
        example = "GE")
    private String enquiryType;

    @JsonProperty("notes")
    @Size(max = 2000)
    @Schema(description = "The information discussed as part of this call/email")
    private String notes;

    @JsonProperty("repeatEnquiry")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
        description = "identifies whether this call/email relates to a previous enquiry (for reporting purposes)")
    private boolean repeatEnquiry;

}
