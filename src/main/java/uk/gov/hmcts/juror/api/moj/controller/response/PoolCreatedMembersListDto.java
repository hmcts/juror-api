package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for listing members of a created Pool.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Pool created list response")
public class PoolCreatedMembersListDto {

    @JsonProperty("poolMembers")
    @Schema(description = "List of pool members")
    private List<JurorPoolDataDto> data = new ArrayList<>();

    @AllArgsConstructor
    @Getter
    @Schema(description = "Pool Member data")
    @ToString
    public static class JurorPoolDataDto {

        @JsonProperty("jurorNumber")
        @Schema(name = "Juror number", description = "Jurors Number")
        private String jurorNumber;

        @JsonProperty("firstName")
        @Schema(name = "First name", description = "Jurors first name")
        private String firstName;

        @JsonProperty("lastname")
        @Schema(name = "Last name", description = "Jurors last name")
        private String lastName;

        @JsonProperty("postCode")
        @Schema(name = "Jurors postcode", description = "Jurors postcode")
        private String postcode;

        @JsonProperty("owner")
        @Schema(name = "Owner", description = "Owner")
        private String owner;

        @JsonProperty("status")
        @Schema(name = "Status", description = "Status of Juror")
        private String status;

        @JsonProperty("startDate")
        @Schema(name = "start Date", description = "Start date of Juror service")
        private LocalDate startDate;

        /**
         * Initialise an instance of this DTO class using a PoolMember object to populate its properties.
         *
         * @param jurorPool an object representation of a PoolMember record from the database
         */
        public JurorPoolDataDto(JurorPool jurorPool) {
            Juror juror = jurorPool.getJuror();
            this.jurorNumber = juror.getJurorNumber();
            this.firstName = juror.getFirstName();
            this.lastName = juror.getLastName();
            this.postcode = juror.getPostcode();
            this.owner = jurorPool.getOwner().equals("400") ? "Bureau" : "Court";
            this.startDate = jurorPool.getReturnDate();
            this.status = jurorPool.getStatus().getStatusDesc();
        }

    }

}
