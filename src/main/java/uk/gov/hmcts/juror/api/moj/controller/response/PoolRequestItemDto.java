package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@Schema(description = "Pool request item response")
public class PoolRequestItemDto implements Serializable {


    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @Schema(name = "New Request", description = "New Request flag")
    private Character newRequest;

    @Schema(name = "Court Name", description = "Pool Request Court Name")
    private String courtName;

    @Schema(name = "Location Code ", description = "Pool Request Location Code")
    private String locCode;

    @Schema(name = "Attend Time", description = "Pool Request Attend Time")
    private LocalDateTime attendTime;

    @Schema(name = "Additional Summons", description = "Pool Request Additional Summons")
    private Integer additionalSummons;

    @Schema(name = "Court Supplied", description = "Members of this Pool supplied by the Court")
    private Integer courtSupplied;

    @Schema(name = "Number Requested", description = "Pool Request Number Requested from the Bureau")
    private Integer noRequested;

    @Schema(name = "Total Number Required", description = "Total Number Required for this Pool (including Court "
        + "Supplied)")
    private Integer totalRequired;

    @Schema(name = "Last Updated", description = "Pool Request Last Updated")
    private LocalDateTime lastUpdate;

    @Schema(name = "Next Date", description = "Pool Request Next Date")
    private LocalDate nextDate;

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
