package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * A list of selected juror(s) to be moved to an active pool.
 */
@Getter
@Setter
@Schema(description = "Deferral Allocation request")
public class DeferralAllocateRequestDto {

    @JsonProperty("poolNumber")
    @NotNull
    @Schema(description = "Selected pool number", requiredMode = Schema.RequiredMode.REQUIRED)
    public String poolNumber;

    @JsonProperty("jurors")
    @NotNull
    @Schema(description = "A List of juror number(s)")
    public List<String> jurors;
}
