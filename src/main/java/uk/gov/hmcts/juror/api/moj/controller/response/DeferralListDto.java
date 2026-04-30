package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Deferrals list response")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeferralListDto {
    @Schema(description = "List of deferrals")
    private List<DeferralListDataDto> deferrals;

    @AllArgsConstructor
    @Getter
    @Schema(description = "Deferrals data")
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DeferralListDataDto {

        @Schema(description = "Court Location")
        private String courtLocation;

        @Schema(description = "Juror Number")
        private String jurorNumber;

        @Schema(description = "First Name")
        private String firstName;

        @Schema(description = "Last name")
        private String lastName;

        @Schema(description = "Pool number")
        private String poolNumber;

        @Schema(description = "Date deferred to")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate deferredTo;
    }
}
