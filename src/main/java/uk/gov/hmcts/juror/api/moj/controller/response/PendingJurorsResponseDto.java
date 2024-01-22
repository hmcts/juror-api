package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;

import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Pending Jurors response DTO")
public class PendingJurorsResponseDto {

    @JsonProperty("pending_jurors_response_data")
    @Schema(description = "List of pending juror records either awaiting approval or rejected")
    private List<PendingJurorsResponseDto.PendingJurorsResponseData> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "Pending juror response data")
    public static class PendingJurorsResponseData {
        @JsonProperty("juror_number")
        @Pattern(regexp = JUROR_NUMBER)
        @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        private String jurorNumber;

        @JsonProperty("first_name")
        @NotEmpty
        @Pattern(regexp = NO_PIPES_REGEX)
        @Length(max = 20)
        @Schema(description = "Juror first name")
        private String firstName;

        @JsonProperty("last_name")
        @NotEmpty
        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror last name")
        private String lastName;

        @JsonProperty("postcode")
        @NotBlank
        @Length(max = 8)
        @Pattern(regexp = POSTCODE_REGEX)
        @Schema(description = "Postcode")
        private String postcode;

        @JsonProperty("notes")
        @Schema(description = "Juror notes")
        private String notes;

        @JsonProperty("pending_juror_status")
        @NotNull
        @Schema(description = "Pending juror status")
        private PendingJurorStatus pendingJurorStatus;

    }
}