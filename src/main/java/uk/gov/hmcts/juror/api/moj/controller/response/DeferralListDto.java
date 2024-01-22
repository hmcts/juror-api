package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DeferralListDto {
    @JsonProperty("deferrals")
    @Schema(description = "List of deferrals")
    private List<DeferralListDataDto> deferrals;

    @AllArgsConstructor
    @Getter
    @Schema(description = "Deferrals data")
    @ToString
    public static class DeferralListDataDto {

        @JsonProperty("courtLocation")
        @Schema(description = "Court Location")
        private String courtLocation;

        @JsonProperty("jurorNumber")
        @Schema(description = "Juror Number")
        private String jurorNumber;

        @JsonProperty("firstName")
        @Schema(description = "First Name")
        private String firstName;

        @JsonProperty("lastName")
        @Schema(description = "Last name")
        private String lastName;

        @JsonProperty("poolNumber")
        @Schema(description = "Pool number")
        private String poolNumber;

        @JsonProperty("deferredTo")
        @Schema(description = "Date deferred to")
        private LocalDate deferredTo;
    }
}
