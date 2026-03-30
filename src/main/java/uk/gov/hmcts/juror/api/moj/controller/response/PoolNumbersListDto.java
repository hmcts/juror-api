package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Pool numbers list response")
public class PoolNumbersListDto {

    @JsonProperty("poolNumbers")
    @Schema(description = "List of pool numbers")
    private List<PoolNumbersDataDto> data;

    @AllArgsConstructor
    @Getter
    @Schema(description = "Pool numbers data")
    @ToString
    public static class PoolNumbersDataDto {

        @JsonProperty("poolNumber")
        @Schema(name = "Pool number", description = "A pool number")
        private String poolNumber;

        @JsonProperty("attendanceDate")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(name = "Attendance date", description = "The attendance date for this pool")
        private LocalDate attendanceDate;

    }

}
