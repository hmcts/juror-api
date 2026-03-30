package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Coroner Pool request")
public class CoronerPoolRequestDto implements Serializable {

    @JsonProperty("courtCode")
    @NotNull
    @Size(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

    @JsonProperty("noRequested")
    @NotNull
    @Schema(name = "Jurors Requested", description = "No. of Jurors Requested for coroner court")
    private Integer noRequested;

    @JsonProperty("name")
    @NotEmpty
    @Schema(name = "name", description = "The requesters name")
    private String name;

    @JsonProperty("emailAddress")
    @NotEmpty
    @Length(max = 254)
    @Schema(description = "Email address of requester")
    private String emailAddress;

    @JsonProperty("phone")
    @Schema(description = "Phone number of requester (optional)")
    private String phone;

    @JsonProperty("requestDate")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The date this pool was requested")
    private LocalDate requestDate;

}