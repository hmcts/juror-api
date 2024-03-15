package uk.gov.hmcts.juror.api.juror.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;


/**
 * DTO for the public frontend digital response to represent data from {@link uk.gov.hmcts.juror.api.moj.domain.Juror}.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Juror details")
public class JurorDetailDto {

    @Schema(description = "The juror's reference number in the juror pool")
    @JsonProperty("jurorNumber")
    private String jurorNumber;

    @Schema(description = "The juror's title")
    @JsonProperty("title")
    private String title;

    @Schema(description = "The juror's first name")
    @JsonProperty("firstName")
    private String firstName;

    @Schema(description = "The juror's last name")
    @JsonProperty("lastName")
    private String lastName;

    @Schema(description = "The processing status of the juror summons")
    @JsonProperty("processingStatus")
    private Integer processingStatus;

    @Schema
    @JsonProperty("address")
    private String address;

    @Schema
    @JsonProperty("address2")
    private String address2;

    @Schema
    @JsonProperty("address3")
    private String address3;

    @Schema
    @JsonProperty("address4")
    private String address4;

    @Schema
    @JsonProperty("address5")
    private String address5;

    /**
     * Juror address 6.
     *
     * @deprecated Unused field in business logic.
     */
    @Deprecated
    @Schema
    @JsonProperty("address6")
    private String address6;

    @Schema(description = "The juror's postcode")
    @JsonProperty("postcode")
    private String postcode;

    /**
     * Date and time allocated slot CJ-7820-64.
     */
    @Schema(description = "The date of the hearing the juror has been summoned to")
    @JsonProperty("hearingDate")
    private LocalDate hearingDate;

    @Schema(description = "Court location code")
    @JsonProperty("locCode")
    private String locCode;

    /**
     * Court name.
     */
    @Schema(description = "Court name")
    @JsonProperty("courtName")
    private String courtName;


    @Schema
    @JsonProperty("locCourtName")
    private String locCourtName;

    @Schema(description = "The time the juror should attend the court on the hearing date")
    @JsonProperty("courtAttendTime")
    private String courtAttendTime;

    /**
     * Court address line 1.
     */
    @Schema(description = "Court address line 1")
    @JsonProperty("courtAddress1")
    private String courtAddress1;

    /**
     * Court address line 2.
     */
    @Schema(description = "Court address line 2")
    @JsonProperty("courtAddress2")
    private String courtAddress2;

    /**
     * Court address line 3.
     */
    @Schema(description = "Court address line 3")
    @JsonProperty("courtAddress3")
    private String courtAddress3;

    @Schema(description = "Court address line 4")
    @JsonProperty("courtAddress4")
    private String courtAddress4;

    @Schema(description = "Court address line 5")
    @JsonProperty("courtAddress5")
    private String courtAddress5;

    /**
     * Court address line 6.<br>
     *
     * @deprecated Unused field in business logic
     */
    @Deprecated
    @Schema(description = "Court address line 6 (not used)")
    @JsonProperty("courtAddress6")
    private String courtAddress6;

    /**
     * Court postcode.
     */
    @Schema(description = "Court postcode")
    @JsonProperty("courtPostcode")
    private String courtPostcode;
}
