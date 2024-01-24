package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Staff backlog responses")
public class BureauOfficerAllocatedResponses implements Serializable {

    @Schema(description = "Bureau Officer Backlog Data")
    @JsonProperty("bureauOfficerAllocatedReplies")
    private List<BureauOfficerAllocatedData> data;

    @JsonProperty("bureauBacklogCount")
    private BureauBacklogCountData bureauBacklogCount;

}
