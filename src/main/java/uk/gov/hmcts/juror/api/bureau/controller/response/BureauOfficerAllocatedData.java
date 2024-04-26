package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class BureauOfficerAllocatedData implements Serializable {

    @JsonProperty("login")
    @Schema(description = "Staff login", example = "John")
    private String login;

    @JsonProperty("name")
    @Schema(description = "Staff  name", example = "Joanna Powers")
    private String name;

    @JsonProperty("nonUrgent")
    @Schema(description = "Number of non-urgent responses assigned", example = "15")
    private Long nonUrgent;


    @JsonProperty("urgent")
    @Schema(description = "Number of urgent responses assigned", example = "15")
    private Long urgent;

    @JsonProperty("allReplies")
    @Schema(description = "Number of all responses assigned", example = "15")
    private Long allReplies;


    @Builder(builderMethodName = "staffAllocationResponseBuilder")
    public BureauOfficerAllocatedData(String login, String name, Long nonUrgent, Long urgent, Long all) {
        this.login = login;
        this.name = name;
        this.urgent = urgent;
        this.nonUrgent = nonUrgent;
        this.allReplies = all;
    }
}
