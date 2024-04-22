package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "payload for processing a reassign or transfer request")
public class JurorManagementRequestDto {

    @JsonProperty("sourcePoolNumber")
    @Schema(name = "Source Pool number", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "9-digit Pool number to reassign the Jurors from")
    @Size(min = 9, max = 9)
    @NumericString
    private String sourcePoolNumber;

    @JsonProperty("sourceCourtLocCode")
    @Schema(name = "Source court location code", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "3-digit court location code to move the Jurors from")
    @Size(min = 3, max = 3)
    @NumericString
    private String sourceCourtLocCode;

    @JsonProperty("jurorNumbers")
    @Size(min = 1)
    @Schema(name = "Juror numbers", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "Array of Juror numbers to move to new pool")
    private List<String> jurorNumbers;

    // This will not be required for court users if they create a new pool
    @JsonProperty("receivingPoolNumber")
    @Schema(name = "receiving Pool number",
        description = "The receiving Pool number to move the Jurors to")
    @Size(min = 9, max = 9)
    @NumericString
    private String receivingPoolNumber;

    @JsonProperty("receivingCourtLocCode")
    @Schema(name = "receiving court location code", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "3-digit court location code to move the Jurors to")
    @Size(min = 3, max = 3)
    @NumericString
    private String receivingCourtLocCode;

    @JsonProperty("targetServiceStartDate")
    @Schema(name = "Service Start Date", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "New Pool's requested start date (at the receiving court)")
    private LocalDate serviceStartDate;

    @JsonProperty("sendingCourtLocCode")
    @Schema(name = "Sending Court Location Code", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "3-digit numeric string to uniquely identify the sending court's location code")
    @Size(min = 3, max = 3)
    @NumericString
    private String sendingCourtLocCode;

    @JsonProperty("deferralMaintenance")
    @Schema(description = "Deferral is occurring from deferral maintenance")
    public Boolean deferralMaintenance;

    //constructor to handle transfer jurors requests
    public JurorManagementRequestDto(String sourcePoolNumber, String sendingCourtLocCode, String receivingCourtLocCode,
                                     LocalDate serviceStartDate, List<String> jurorNumbers) {
        this.sourcePoolNumber = sourcePoolNumber;
        this.sendingCourtLocCode = sendingCourtLocCode;
        this.sourceCourtLocCode = sendingCourtLocCode;
        this.receivingCourtLocCode = receivingCourtLocCode;
        this.serviceStartDate = serviceStartDate;
        this.jurorNumbers = jurorNumbers;

    }

    //constructor to handle reassign jurors requests
    public JurorManagementRequestDto(String sourcePoolNumber, String sourceCourtLocCode, List<String> jurorNumbers,
                                     String receivingPoolNumber, String receivingCourtLocCode,
                                     LocalDate serviceStartDate) {
        this.sourcePoolNumber = sourcePoolNumber;
        this.sourceCourtLocCode = sourceCourtLocCode;
        this.jurorNumbers = jurorNumbers;
        this.receivingPoolNumber = receivingPoolNumber;
        this.receivingCourtLocCode = receivingCourtLocCode;
        this.serviceStartDate = serviceStartDate;

    }

}
