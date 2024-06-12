package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * MandatoryKpi DTO for the dashboard.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@SuppressWarnings("PMD.TooManyFields")
@Schema(description = "Dashboard mandatory kpis.")
public class DashboardMandatoryKpiData implements Serializable {

    // responses over time

    @Schema(description = "Paper responses over time.")
    private ResponseMethod paperResponsesOverTime;

    @Schema(description = "Paper responses over time.")
    private ResponseMethod onlineResponsesOverTime;

    @Schema(description = "All responses over time.")
    private ResponseMethod allResponsesOverTime;

    // totals

    @Schema(description = "total paper responses.")
    @Min(0)
    private Integer paperResponsesTotal;

    @Schema(description = "total online responses.")
    @Min(0)
    private Integer onlineResponsesTotal;

    @Schema(description = "total responses")
    @Min(0)
    private Integer responsesTotal;

    @Schema(description = "total summoned")
    @Min(0)
    private Integer summonedTotal;

    // percentages - channel take up

    @Schema(description = "% paper take up")
    @Min(0)
    private Float percentPaperTakeUp;                // paperResponsesTotal / responsesTotal

    @Schema(description = "% online take up")
    @Min(0)
    private Float percentOnlineTakeUp;               // onlineResponseTotal / responsesTotal


    // percentages - responses by time over total responses

    @Schema(description = "% responses within 7 days")
    @Min(0)
    private Float percentResponsesWithin7days;               // allResponsesOverTime.within7days / responsesTotal

    @Schema(description = "% responses within 14 days")
    @Min(0)
    private Float percentResponsesWithin14days;               // allResponsesOverTime.within14days / responsesTotal

    @Schema(description = "% responses within 21 days")
    @Min(0)
    private Float percentResponsesWithin21days;               // allResponsesOverTime.within21days / responsesTotal

    @Schema(description = "% responses over 21 days")
    @Min(0)
    private Float percentResponsesOver21days;               // allResponsesOverTime.over21days / responsesTotal

    // percentages - digital responses by time over total responses by time

    @Schema(description = "% digital responses within 7 days")
    @Min(0)
    private Float percentOnlineResponsesWithin7days;               // onlineResponsesOverTime.within7days /
    // allResponsesOverTime.within7days

    @Schema(description = "% digital responses within 14 days")
    @Min(0)
    private Float percentOnlineResponsesWithin14days;               // onlineResponsesOverTime.within14days /
    // allResponsesOverTime.within14days

    @Schema(description = "% digital responses within 21 days")
    @Min(0)
    private Float percentOnlineResponsesWithin21days;               // onlineResponsesOverTime.within21days /
    // allResponsesOverTime.within21days

    @Schema(description = "% digital responses over 21 days")
    @Min(0)
    private Float percentOnlineResponsesOver21days;               // onlineResponsesOverTime.over21days /
    // allResponsesOverTime.over21days


    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Counts over periods for a specific response method.")
    public static class ResponseMethod implements Serializable {

        @Schema(description = "Responses within 7 days.")
        @Min(0)
        private Integer within7days;

        @Schema(description = "Responses within 14 days.")
        @Min(0)
        private Integer within14days;

        @Schema(description = "Responses within 21 days.")
        @Min(0)

        private Integer within21days;
        @Schema(description = "Responses over 21 days.")
        @Min(0)
        private Integer over21days;

    }

}
