package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeed;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;

import java.io.Serializable;

@Data
@Schema(description = "Details on any special needs a Juror has")
public class SpecialNeedDto implements Serializable {

    @Schema(description = "Juror number")
    private String jurorNumber;

    @Schema(description = "Special need code")
    private String code;

    @Schema(description = "Special need description")
    private String description;

    @Schema(description = "Special need details")
    private String detail;

    public SpecialNeedDto(final JurorReasonableAdjustment bureauJurorSpecialNeed) {
        this.jurorNumber = bureauJurorSpecialNeed.getJurorNumber();
        this.code = bureauJurorSpecialNeed.getReasonableAdjustment().getCode();
        this.description = bureauJurorSpecialNeed.getReasonableAdjustment().getDescription();
        this.detail = bureauJurorSpecialNeed.getReasonableAdjustmentDetail();
    }
}
