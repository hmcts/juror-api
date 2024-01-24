package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.time.LocalDate;
import java.util.List;


@AllArgsConstructor
@Getter
@Schema(description = "Transfer Jurors request data")
public class TransferJurorsRequestDto {

    @JsonProperty("sourcePoolNumber")
    @Schema(name = "Source Pool Number", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "9-digit numeric string to identify the source pool request number")
    @Size(min = 9, max = 9)
    @NumericString
    private String sourcePoolNumber;

    @JsonProperty("sendingCourtLocCode")
    @Schema(name = "Sending Court Location Code", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "3-digit numeric string to uniquely identify the sending court's location code")
    @Size(min = 3, max = 3)
    @NumericString
    private String sendingCourtLocCode;

    @JsonProperty("receivingCourtLocCode")
    @Schema(name = "Receiving Court Location Code", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "3-digit numeric string to uniquely identify the receiving court's location code")
    @Size(min = 3, max = 3)
    @NumericString
    private String receivingCourtLocCode;

    @JsonProperty("targetServiceStartDate")
    @Schema(name = "Service Start Date", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "New Pool's requested start date (at the receiving court)")
    private LocalDate serviceStartDate;

    @JsonProperty("jurorNumbers")
    @Schema(name = "Juror Numbers", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "List of unique juror numbers")
    @Size(min = 1)
    private List<String> jurorNumbers;

}
