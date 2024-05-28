package uk.gov.hmcts.juror.api.moj.controller.reports.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.validation.dto.ConditionalDtoValidation;
import uk.gov.hmcts.juror.api.validation.ValidateIf;
import uk.gov.hmcts.juror.api.validation.ValidateIfTrigger;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConditionalDtoValidation(
    conditionalProperty = "searchBy", values = {"POOL"},
    requiredProperties = {"poolNumber"},
    message = "poolNumber is required for POOL search")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ValidateIfTrigger(classToValidate = JurySummoningMonitorReportRequest.class)
public class JurySummoningMonitorReportRequest {

    @Pattern(regexp = "^(POOL|COURT)$")
    @NotBlank
    private String searchBy;

    private String poolNumber;

    private List<String> courtLocCodes;

    private boolean allCourts;

    @ValidateIf(fields = {"poolNumber"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT, shape = JsonFormat.Shape.STRING)
    private LocalDate fromDate;

    @ValidateIf(fields = {"poolNumber"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.EXCLUDE)
    @ValidateIf(fields = {"poolNumber"},
        condition = ValidateIf.Condition.NONE_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT, shape = JsonFormat.Shape.STRING)
    private LocalDate toDate;

}