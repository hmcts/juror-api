package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusEnum;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Simple details of juror(s) results")
public class JurorSimpleDetailsResponseDto implements Serializable {

    @JsonProperty("juror_details")
    @Schema(name = "juror details", description = "List of juror simple details")
    private List<SimpleDetails> jurorDetails;


    @Setter
    @Getter
    @Builder
    @Schema(description = "Simple details of juror(s) results")
    public static class SimpleDetails implements Serializable {

        @JsonProperty("juror_number")
        @Schema(name = "Juror Number", description = "9-digit numeric string to identify a juror")
        private String jurorNumber;

        @JsonProperty("status")
        @Schema(name = "Juror status", description = "Juror status")
        private JurorStatusEnum status;

        @JsonProperty("first_name")
        @Schema(name = "First Name", description = "Juror's first name for display purposes"
            + "(transfer/reassign)")
        private String firstName;

        @JsonProperty("last_name")
        @Schema(name = "Last Name", description = "Juror's last name for display purposes"
            + "(transfer/reassign)")
        private String lastName;

    }

}
