package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;

import java.io.Serializable;

@Data
@Schema(description = "Details on a Juror's employments with the CJS")
public class CJSEmploymentDto implements Serializable {

    @Schema(description = "Juror number")
    private String jurorNumber;

    @Schema(description = "CJS Employer")
    private String employer;

    @Schema(description = "Details of the CJS employment")
    private String details;

    public CJSEmploymentDto(final JurorResponseCjsEmployment bureauJurorCJS) {
        this.jurorNumber = bureauJurorCJS.getJurorNumber();
        this.employer = bureauJurorCJS.getCjsEmployer();
        this.details = bureauJurorCJS.getCjsEmployerDetails();
    }
}
