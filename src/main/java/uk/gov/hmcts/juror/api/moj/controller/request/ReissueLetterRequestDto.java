package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Reissue Juror letters request")
public class ReissueLetterRequestDto implements Serializable {

    @JsonProperty("letters_list")
    @NotEmpty
    @Schema(description = "List of letters to be reissued", required = true)
    private List<@NotNull ReissueLetterRequestData> letters;

    @Getter
    @Builder
    public static class ReissueLetterRequestData {

        @JsonProperty(value = "juror_number", required = true)
        @JurorNumber
        @NotBlank
        @Schema(description = "Unique juror number")
        private String jurorNumber;

        @JsonProperty(value = "form_code", required = true)
        @NotBlank
        @Schema(name = "letter type", description = "Code indicating the type of letter to be sent")
        private String formCode;

        @JsonProperty(value = "date_printed", required = true)
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "The date the letter was printed", example = "2024-01-31")
        private LocalDate datePrinted;

    }

}