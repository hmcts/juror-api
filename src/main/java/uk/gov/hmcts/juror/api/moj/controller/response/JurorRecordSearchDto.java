package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.util.List;

/**
 * Response DTO for listing Juror record search results.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Juror record search response")
public class JurorRecordSearchDto {

    @JsonProperty("jurorRecordSearchData")
    @Schema(description = "List of Juror record search results")
    private List<JurorRecordSearchDataDto> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Juror Record search result data")
    public static class JurorRecordSearchDataDto {

        @NotNull
        @JsonProperty("jurorNumber")
        @Schema(name = "Juror number", description = "Jurors Number")
        private String jurorNumber;

        @NotNull
        @Length(max = 20)
        @Schema(description = "Juror first name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String firstName;

        @NotNull
        @Length(max = 20)
        @Schema(description = "Juror last name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String lastName;

        @NotNull
        @Length(max = 8)
        @Schema(description = "Juror address post code", requiredMode = Schema.RequiredMode.REQUIRED)
        private String addressPostcode;

        @JsonProperty("poolNumber")
        @Schema(name = "Pool number", description = "The unique number for a pool request")
        private String poolNumber;

        @JsonProperty("courtName")
        @Schema(name = "Court name", description = "Name for a given court location")
        private String courtName;

        @JsonProperty("locCode")
        @Schema(name = "Court Location Code", description = "3 digit numeric String to identify a Court Location")
        private String courtLocationCode;

        /**
         * Initialise an instance of this DTO class using a JurorPool object to populate its properties.
         *
         * @param jurorPool an object representation of a JurorPool record from the database
         */
        public JurorRecordSearchDataDto(JurorPool jurorPool) {
            this.jurorNumber = jurorPool.getJurorNumber();
            this.poolNumber = jurorPool.getPoolNumber();
            this.courtName = jurorPool.getCourt().getName();
            this.courtLocationCode = jurorPool.getCourt().getLocCode();

            Juror juror = jurorPool.getJuror();
            this.firstName = juror.getFirstName();
            this.lastName = juror.getLastName();
            this.addressPostcode = juror.getPostcode();

        }

    }

}
