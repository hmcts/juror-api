package uk.gov.hmcts.juror.api.moj.controller.request.letter.court;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;

import java.io.Serializable;
import java.util.List;


@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PrintLettersRequestDto implements Serializable {
    @NotNull
    CourtLetterType letterType;

    @NotNull
    @Schema(name = "Juror information", description = "List of Juror numbers to request print data for")
    List<String> jurorNumbers;

}
