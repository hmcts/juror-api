package uk.gov.hmcts.juror.api.moj.controller.request.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Request to send bureau emails to a list of jurors")
public class BureauEmailRequestDto {

    @JsonProperty("juror_emails")
    @NotEmpty(message = "At least one juror email detail must be provided")
    @Valid
    @Schema(description = "List of juror/contact/template details to send emails for",
        requiredMode = Schema.RequiredMode.REQUIRED)
    private List<JurorEmailDetail> jurorEmails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Juror number, contact email, and template to use for a single bureau email")
    public static class JurorEmailDetail {

        @JsonProperty("juror_number")
        @NotBlank
        @Pattern(regexp = JUROR_NUMBER, message = "Invalid juror number")
        @Schema(description = "Juror number", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
        private String jurorNumber;

        @JsonProperty("email")
        @NotBlank
        @Email(message = "Invalid email address")
        @Length(max = 254)
        @Schema(description = "Contact email address to send the notification to",
            requiredMode = Schema.RequiredMode.REQUIRED)
        private String email;

        @JsonProperty("email_template_name")
        @NotNull
        @Schema(description = "Which Notify template to use for this email",
            requiredMode = Schema.RequiredMode.REQUIRED)
        private EmailTemplateName emailTemplateName;
    }
}
