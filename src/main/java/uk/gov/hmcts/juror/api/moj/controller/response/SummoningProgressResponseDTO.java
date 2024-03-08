package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Response DTO when requesting pool summoning data for court location & pool type")
public class SummoningProgressResponseDto {
    @JsonProperty("statsByWeek")
    @Schema(description = "A list of pool monitoring stats by week up to 8 weeks")
    List<WeekFilter> statsByWeek;

    @Getter
    @Setter
    public static class WeekFilter {
        @JsonProperty("stats")
        @Schema(description = "Pool monitoring stats")
        List<SummoningProgressStats> stats;

        @JsonProperty("startOfWeek")
        @Schema(description = "The start date of the week")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startOfWeek;
    }

    @Getter
    @Setter
    public static class SummoningProgressStats {
        @JsonProperty("poolNumber")
        @Schema(description = "Unique number to identify pool")
        public String poolNumber;

        @JsonProperty("serviceStartDate")
        @Schema(description = "Service start date for pool")
        @JsonFormat(pattern = "yyyy-MM-dd")
        public LocalDate serviceStartDate;

        @JsonProperty("requested")
        @Schema(description = "Total number of jurors requested by the court for the bureau to summon")
        public int requested;

        @JsonProperty("summoned")
        @Schema(description = "Total number of jurors summoned by the bureau")
        public int summoned;

        @JsonProperty("confirmed")
        @Schema(description = "Total number of summoned jurors confirmed as responded")
        public int confirmed;

        @JsonProperty("balance")
        @Schema(description = "Difference between total number requested and confirmed")
        public int balance;

        @JsonProperty
        @Schema(description = "Total number who have responded negatively and are therefore unavailable, for example "
            + "deferred, excused or disqualified etc.")
        public int unavailable;

//TODO - delete this commented code if not needed for a court user journey for pool metrics
//        @JsonProperty("courtSupplied")
//        @Schema(description = "Court Supplied")
//        public int courtSupplied;
    }
}
