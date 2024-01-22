package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

import java.util.List;

/**
 * Response DTO for staff list endpoint.
 *
 * @see uk.gov.hmcts.juror.api.bureau.controller.BureauStaffController#getAll(BureauJwtAuthentication)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Staff list response")
public class StaffListDto {

    @JsonProperty("data")
    @Schema(description = "response data")
    private StaffListDataDto data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Staff list data")
    public static class StaffListDataDto {

        @JsonProperty("activeStaff")
        @Schema(description = "Active staff members")
        private List<StaffDto> activeStaff;

        @JsonProperty("inactiveStaff")
        @Schema(description = "inactive staff members")
        private List<StaffDto> inactiveStaff;
    }
}

