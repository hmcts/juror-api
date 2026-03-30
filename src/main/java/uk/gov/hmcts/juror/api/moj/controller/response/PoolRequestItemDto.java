package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Pool request item response")
public class PoolRequestItemDto implements Serializable {

    @JsonProperty("poolNumber")
    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @JsonProperty("newRequest")
    @Schema(name = "New Request", description = "New Request flag")
    private Character newRequest;

    @JsonProperty("courtName")
    @Schema(name = "Court Name", description = "Pool Request Court Name")
    private String courtName;

    @JsonProperty("locCode")
    @Schema(name = "Location Code ", description = "Pool Request Location Code")
    private String locCode;

    @JsonProperty("attendTime")
    @Schema(name = "Attend Time", description = "Pool Request Attend Time")
    private LocalDateTime attendTime;

    @JsonProperty("additionalSummons")
    @Schema(name = "Additional Summons", description = "Pool Request Additional Summons")
    private Integer additionalSummons;

    @JsonProperty("courtSupplied")
    @Schema(name = "Court Supplied", description = "Members of this Pool supplied by the Court")
    private Integer courtSupplied;

    @JsonProperty("noRequested")
    @Schema(name = "Number Requested", description = "Pool Request Number Requested from the Bureau")
    private Integer noRequested;

    @JsonProperty("totalRequired")
    @Schema(name = "Total Number Required", description = "Total Number Required for this Pool (including Court "
        + "Supplied)")
    private Integer totalRequired;

    @JsonProperty("lastUpdate")
    @Schema(name = "Last Updated", description = "Pool Request Last Updated")
    private LocalDateTime lastUpdate;

    @JsonProperty("nextDate")
    @Schema(name = "Next Date", description = "Pool Request Next Date")
    private LocalDate nextDate;

    @JsonProperty("returnDate")
    @Schema(name = "Return Date", description = "Pool Request Return Date")
    private LocalDate returnDate;

    public void initPoolRequestItemDto(PoolRequest poolRequest) {
        this.setPoolNumber(poolRequest.getPoolNumber());
        this.setNewRequest(poolRequest.getNewRequest());
        if (poolRequest.getCourtLocation() != null) {
            this.setCourtName(poolRequest.getCourtLocation().getLocCourtName());
            this.setLocCode(poolRequest.getCourtLocation().getLocCode());
        }
        this.setAttendTime(poolRequest.getAttendTime());
        this.setAdditionalSummons(poolRequest.getAdditionalSummons());
        this.setNoRequested(poolRequest.getNumberRequested());
        this.setLastUpdate(poolRequest.getLastUpdate());
        this.setNextDate(poolRequest.getReturnDate());//Candidate for tech-debt downstream would require FE changes
        // so not done at this stage
        this.setReturnDate(poolRequest.getReturnDate());
    }

}
