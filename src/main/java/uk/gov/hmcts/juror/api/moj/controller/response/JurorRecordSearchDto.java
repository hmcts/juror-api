package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Schema(description = "Juror record search response")
public class JurorRecordSearchDto {


    @Schema(description = "List of Juror record search results")
    private List<JurorRecordSearchDataDto> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Juror Record search result data")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class JurorRecordSearchDataDto {

        @NotNull
        @Schema(name = "Juror number", description = "Jurors Number")
        @SuppressWarnings({"PMD.ImmutableField"}) // final may not work as possibly not initialised
        private String jurorNumber;

        @NotNull
        @Length(max = 20)
        @Schema(description = "Juror first name", requiredMode = Schema.RequiredMode.REQUIRED)
        @SuppressWarnings({"PMD.ImmutableField"}) // final may not work as possibly not initialised
        private String firstName;

        @NotNull
        @Length(max = 20)
        @Schema(description = "Juror last name", requiredMode = Schema.RequiredMode.REQUIRED)
        @SuppressWarnings({"PMD.ImmutableField"}) // final may not work as possibly not initialised
        private String lastName;

        @NotNull
        @Length(max = 8)
        @Schema(description = "Juror address post code", requiredMode = Schema.RequiredMode.REQUIRED)
        @SuppressWarnings({"PMD.ImmutableField"}) // final may not work as possibly not initialised
        private String addressPostcode;

        @Schema(name = "Pool number", description = "The unique number for a pool request")
        @SuppressWarnings({"PMD.ImmutableField"}) // final may not work as possibly not initialised
        private String poolNumber;

        @Schema(name = "Court name", description = "Name for a given court location")
        @SuppressWarnings({"PMD.ImmutableField"}) // final may not work as possibly not initialised
        private String courtName;

        @Schema(name = "Court Location Code", description = "3 digit numeric String to identify a Court Location")
        @SuppressWarnings({"PMD.ImmutableField"}) // final may not work as possibly not initialised
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
