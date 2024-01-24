package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for staff roster response.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Staff roster of all active staff")
public class StaffRosterResponseDto {
    @Schema(description = "Active staff members")
    private List<StaffDto> data;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Active staff member roster entry")
    public static class StaffDto {
        @Schema(description = "Staff member login name")
        private String login;
        @Schema(description = "Staff member descriptive name", example = "Joe Bloggs")
        private String name;
    }
}
