package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Court location data")
@ToString
public class CourtLocationDataDto {

    @JsonProperty("locationCode")
    @Schema(name = "locationCode", description = "3 digit identification code for a court location")
    private String locationCode;

    @JsonProperty("locationName")
    @Schema(name = "locationName", description = "The court name")
    private String locationName;

    @JsonProperty("attendanceTime")
    @Schema(name = "attendanceTime", description = "The default start time for a court location")
    private String attendanceTime;

    @JsonProperty("owner")
    @Schema(name = "owner", description = "3 digit identification code for a primary court location which owns this "
        + "court location")
    private String owner;

    /**
     * Initialise an instance of this DTO class using a CourtLocation object to populate its properties.
     *
     * @param courtLocation an object representation of a CourtLocation record from the database
     */
    public CourtLocationDataDto(CourtLocation courtLocation) {
        this.locationCode = courtLocation.getLocCode();
        this.locationName = courtLocation.getName();
        this.attendanceTime = DataUtils.asStringHHmm(courtLocation.getCourtAttendTime());
        this.owner = courtLocation.getOwner();
    }

}
