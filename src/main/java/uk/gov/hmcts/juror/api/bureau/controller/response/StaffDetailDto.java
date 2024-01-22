package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

/**
 * DTO for the 'single staff member detail' endpoint
 *
 * @see uk.gov.hmcts.juror.api.bureau.controller.BureauStaffController#getOne(String, BureauJwtAuthentication)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Staff member detail")
public class StaffDetailDto {

    @JsonProperty("data")
    @Schema(description = "response data")
    private StaffDto data;
}
