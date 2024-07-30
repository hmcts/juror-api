package uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Retrieve attendance (appearance) details")
public class RetrieveAttendanceDetailsDto {
    @JsonProperty("commonData")
    @Schema(description = "Common data relating to updating attendance details")
    private CommonData commonData;

    @JsonProperty("juror")
    @Schema(description = "List of jurors to retrieve attendance details of")
    private List<String> juror;

    @JsonProperty("juror_in_waiting")
    private boolean jurorInWaiting;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Schema(description = "Common data relating to attendances")
    public static class CommonData {
        @JsonProperty("tag")
        @NotNull
        @Schema(description = "Enum values for filtering the search criteria, for example CHECKED_IN, CHECKED_OUT")
        private RetrieveAttendanceDetailsTag tag;

        @JsonProperty("attendanceDate")
        @NotNull
        @Schema(description = "Attendance date for jury service")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate attendanceDate;

        @JsonProperty("locationCode")
        @NotBlank
        @Size(min = 3, max = 3)
        @NumericString
        @Schema(description = "Unique 3 digit code to identify a court location")
        private String locationCode;
    }
}
