package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Response DTO when requesting pool summoning data for court location & pool type")
public class SummoningProgressResponseDto {

    @Schema(description = "A list of pool monitoring stats by week up to 8 weeks")
    List<WeekFilter> statsByWeek;

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WeekFilter {
        @Schema(description = "Pool monitoring stats")
        List<SummoningProgressStats> stats;

        @Schema(description = "The start date of the week")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startOfWeek;
    }

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SummoningProgressStats {
        @Schema(description = "Unique number to identify pool")
        public String poolNumber;

        @Schema(description = "Service start date for pool")
        @JsonFormat(pattern = "yyyy-MM-dd")
        public LocalDate serviceStartDate;

        @Schema(description = "Total number of jurors requested by the court for the bureau to summon")
        public int requested;

        @Schema(description = "Total number of jurors summoned by the bureau")
        public int summoned;

        @Schema(description = "Total number of summoned jurors confirmed as responded")
        public int confirmed;

        @Schema(description = "Difference between total number requested and confirmed")
        public int balance;

        @Schema(description = "Total number who have responded negatively and are therefore unavailable, for example "
            + "deferred, excused or disqualified etc.")
        public int unavailable;

//TODO - delete this commented code if not needed for a court user journey for pool metrics
//        @JsonProperty("courtSupplied")
//        @Schema(description = "Court Supplied")
//        public int courtSupplied;
    }
}
