package uk.gov.hmcts.juror.api.juror.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for the Juror Holiday date picker endpoints.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Juror Holiday date picker.")
public class JurorHolidaysResponseDto implements Serializable {

    @Schema(description = "List of Matching Holiday dates")
    private MatchingHolidayDates matchingHolidayDates;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "List of Matching Holiday dates")
    public static class MatchingHolidayDates implements Serializable {


        //   @Schema(description = "Holiday Date picker Matches", requiredMode = Schema.RequiredMode.REQUIRED)
        //   private Date holidaysDate;

        //    @Schema(description = "Holidays Date picker No Matches message response")
        //    private String noHolidaysDateMatches;

        @Schema(description = "Public Holiday Dates", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Holidays> publicHolidayDates;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class Holidays implements Serializable {
        @JsonProperty("date")
        @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
        private LocalDate date;

        @JsonProperty("is_public_holiday")
        private boolean publicHoliday;

        @JsonProperty("loc_code")
        private String locCode;

        public static List<Holidays> from(List<uk.gov.hmcts.juror.api.juror.domain.Holidays> publicHolidaysDates) {
            return publicHolidaysDates.stream()
                .map(holiday -> {
                    String locCode = null;
                    if(holiday.getCourtLocation() != null){
                        locCode = holiday.getCourtLocation().getLocCode();
                    }
                    return Holidays.builder()
                        .date(holiday.getHoliday())
                        .publicHoliday(holiday.getPublicHoliday())
                        .locCode(locCode)
                        .build();
                })
                .toList();
        }
    }
}
