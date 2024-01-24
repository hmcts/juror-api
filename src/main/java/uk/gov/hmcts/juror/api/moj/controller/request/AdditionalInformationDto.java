package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Missing information related to summons response")
public class AdditionalInformationDto {

    @JsonProperty("jurorNumber")
    @JurorNumber
    @Schema(name = "Juror number", description = "Juror number of juror", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String jurorNumber;

    @JsonProperty("replyMethod")
    @Schema(name = "Reply method", description = "Reply method is either PAPER or DIGITAL", requiredMode =
        Schema.RequiredMode.REQUIRED)
    @NotNull
    private ReplyMethod replyMethod;

    @JsonProperty("informationRequired")
    @Schema(name = "Information required", description = "Array of information missing in the response")
    @NotEmpty
    private List<MissingInformation> missingInformation;
}