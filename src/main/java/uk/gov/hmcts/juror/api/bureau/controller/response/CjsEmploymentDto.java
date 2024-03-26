package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;

import java.io.Serializable;

@Data
@Schema(description = "Details on a Juror's employments with the CJS")
public class CjsEmploymentDto implements Serializable {

    @Schema(description = "Juror number")
    private String jurorNumber;

    @Schema(description = "CJS Employer")
    private String employer;

    @Schema(description = "Details of the CJS employment")
    private String details;

    public CjsEmploymentDto(final JurorResponseCjsEmployment bureauJurorCjs) {
        this.jurorNumber = bureauJurorCjs.getJurorNumber();
        this.employer = bureauJurorCjs.getCjsEmployer();
        this.details = bureauJurorCjs.getCjsEmployerDetails();
    }
}
