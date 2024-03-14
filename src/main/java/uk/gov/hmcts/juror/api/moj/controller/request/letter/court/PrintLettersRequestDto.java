package uk.gov.hmcts.juror.api.moj.controller.request.letter.court;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.validation.dto.ConditionalDtoValidation;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ConditionalDtoValidation(
    conditionalProperty = "letterType", values = {"SHOW_CAUSE"},
    requiredProperties = {"showCauseDate", "showCauseTime"},
    message = "Show cause date and time are required for Show Cause letter")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PrintLettersRequestDto implements Serializable {
    @NotNull
    CourtLetterType letterType;

    @NotNull
    @Schema(name = "juror_numbers", description = "List of Juror numbers to request print data for")
    List<String> jurorNumbers;

    @Schema(description = "The date juror is ordered to attend before the court to explain they should not be fined.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate showCauseDate;

    @Schema(description = "The time juror is ordered to attend before the court to explain they should not be fined.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime showCauseTime;
}


