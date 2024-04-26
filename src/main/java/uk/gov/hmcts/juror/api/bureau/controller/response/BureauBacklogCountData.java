package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data of backlog responses count.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Backlog count")
public class BureauBacklogCountData implements Serializable {

    @JsonProperty("nonUrgent")
    @Schema(description = "Total non urgent")
    private Long nonUrgent;

    @JsonProperty("urgent")
    @Schema(description = "Total urgent")
    private Long urgent;

    @JsonProperty("allReplies")
    @Schema(description = "All replies")
    private Long allReplies;


}
