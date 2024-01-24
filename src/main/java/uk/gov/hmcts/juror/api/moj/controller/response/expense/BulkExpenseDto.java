package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BulkExpenseDto {

    @JsonProperty("juror_number")
    @NotBlank
    @JurorNumber
    private String jurorNumber;

    @JsonProperty("juror_version")
    @Nullable
    private Long jurorVersion;

    @JsonProperty("submitted_by")
    @Nullable
    private String submittedBy;

    @JsonProperty("submitted_on")
    private LocalDateTime submittedOn;

    @JsonProperty("approved_by")
    @Nullable
    private String approvedBy;

    @JsonProperty("approved_on")
    private LocalDateTime approvedOn;

    @JsonProperty("type")
    @NotNull
    private AppearanceStage type;

    @JsonProperty("mileage")
    @NotNull
    @Positive
    private Integer mileage;

    @JsonProperty("expenses")
    @NotNull
    private List<@NotNull BulkExpenseEntryDto> expenses;

    @JsonProperty("totals")
    @NotNull
    private TotalExpenseDto totals;
}
