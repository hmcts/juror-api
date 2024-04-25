package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Reissue Bureau letters response DTO")
public class ReissueLetterReponseDto implements Serializable {

    @JsonProperty("juror_list")
    @Schema(description = "List of jurors with updated status")
    private List<@NotNull ReissueLetterResponseData> jurors;

    @Getter
    @Builder
    public static class ReissueLetterResponseData {

        @JsonProperty(value = "juror_number", required = true)
        @JurorNumber
        @NotBlank
        @Schema(description = "Unique juror number")
        private String jurorNumber;

        @JsonProperty("first_name")
        @Length(max = 20)
        @Schema(description = "Juror first name")
        private String firstName;

        @JsonProperty("last_name")
        @Length(max = 20)
        @Schema(description = "Juror last name")
        private String lastName;

        @JsonProperty("juror_status")
        @Schema(description = "Juror status")
        private JurorStatus jurorStatus;

    }

}