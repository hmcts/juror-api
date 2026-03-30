package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;

import java.io.Serializable;

@Data
@Schema(description = "Details on any reasonable adjustment  a Juror has")
public class ReasonableAdjustmentDto implements Serializable {

    @Schema(description = "Juror number")
    private String jurorNumber;

    @Schema(description = "Reasonable adjustment code")
    private String code;

    @Schema(description = "Reasonable adjustment description")
    private String description;

    @Schema(description = "Reasonable adjustment details")
    private String detail;

    public ReasonableAdjustmentDto(final JurorReasonableAdjustment bureauReasonableAdjustment) {
        this.jurorNumber = bureauReasonableAdjustment.getJurorNumber();
        this.code = bureauReasonableAdjustment.getReasonableAdjustment().getCode();
        this.description = bureauReasonableAdjustment.getReasonableAdjustment().getDescription();
        this.detail = bureauReasonableAdjustment.getReasonableAdjustmentDetail();
    }
}
