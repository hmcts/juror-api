package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Coroner Pool details response")
public class CoronerPoolItemDto implements Serializable {

    @JsonProperty("poolNumber")
    @NotEmpty
    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @JsonProperty("courtName")
    @NotEmpty
    @Schema(name = "Court Name", description = "Pool Request Court Name")
    private String courtName;

    @JsonProperty("locCode")
    @NotEmpty
    @Schema(name = "Location Code ", description = "Pool Request Location Code")
    private String locCode;

    @JsonProperty("noRequested")
    @NotNull
    @Schema(name = "Number Requested",
        description = "Pool Request Number Requested from the Bureau")
    private Integer noRequested;

    @JsonProperty("totalAdded")
    @Schema(name = "Total Number Added to Pool",
        description = "Total Number of citizens added to this Pool")
    private Integer totalAdded;

    @JsonProperty("name")
    @NotEmpty
    @Schema(name = "name", description = "The requesters name")
    private String name;

    @JsonProperty("emailAddress")
    @Length(max = 254)
    @Schema(description = "Email address of requester")
    private String emailAddress;

    @JsonProperty("phone")
    @Schema(description = "Phone number of requester (optional)")
    private String phone;

    @JsonProperty("dateRequested")
    @Schema(description = "The date the pool was requested")
    private LocalDate dateRequested;

    @JsonProperty("coronerDetailsList")
    @Schema(description = "List of coroner pool members")
    List<CoronerDetails> coronerDetailsList;

    @Builder
    @Getter
    @AllArgsConstructor
    public static class CoronerDetails {

        @JsonProperty("jurorNumber")
        @NotEmpty
        @Schema(description = "Juror number of pool member")
        private String jurorNumber;

        @JsonProperty("title")
        @Length(max = 20)
        @NotEmpty
        @Schema(description = "title of pool member")
        private String title;

        @JsonProperty("firstName")
        @Length(max = 20)
        @NotEmpty
        @Schema(description = "first name of pool member")
        private String firstName;

        @JsonProperty("lastName")
        @Length(max = 25)
        @NotEmpty
        @Schema(description = "last name of pool member")
        private String lastName;

        @JsonProperty("addressLineOne")
        @Schema(description = "line one of pool member's address")
        private String addressLineOne;

        @JsonProperty("addressLineTwo")
        @Schema(description = "line two of pool member's address")
        private String addressLineTwo;

        @JsonProperty("addressLineThree")
        @Schema(description = "line three of pool member's address")
        private String addressLineThree;

        @JsonProperty("addressLineFour")
        @Schema(description = "line four of pool member's address")
        private String addressLineFour;

        @JsonProperty("addressLineFive")
        @Schema(description = "line five of pool member's address")
        private String addressLineFive;

        @JsonProperty("addressLineSix")
        @Schema(description = "line six of pool member's address")
        private String addressLineSix;

        @JsonProperty("postcode")
        @Length(max = 10)
        @NotEmpty
        @Schema(description = "postcode of pool member")
        private String postcode;
    }

}
