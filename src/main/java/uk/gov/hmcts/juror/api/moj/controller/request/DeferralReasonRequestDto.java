package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "payload for processing a deferral request")
public class DeferralReasonRequestDto {

    @JsonProperty("deferralDate")
    @Schema(description = "Selected deferral date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public LocalDate deferralDate;

    @JsonProperty("excusalReasonCode")
    @Schema(description = "Deferral reason code", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(max = 2)
    @NotEmpty
    @NotNull
    public String excusalReasonCode;

    @JsonProperty("poolNumber")
    @Schema(description = "Active pool number for selected date")
    public String poolNumber;

    @JsonProperty("replyMethod")
    @Schema(description = "Reply method type (PAPER/DIGITAL)")
    public ReplyMethod replyMethod;

}
