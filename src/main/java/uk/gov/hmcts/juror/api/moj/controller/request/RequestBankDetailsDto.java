package uk.gov.hmcts.juror.api.moj.controller.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Valid
@EqualsAndHashCode
public class RequestBankDetailsDto {

    @JsonProperty("juror_number")
    @JurorNumber
    @NotNull
    private String jurorNumber;

    @JsonProperty("account_number")
    @Pattern(regexp = "^\\d{8}$")
    @NotNull
    private String accountNumber;

    @JsonProperty("sort_code")
    @Pattern(regexp = "^\\d{6}$")
    @NotNull
    private String sortCode;

    @JsonProperty("account_holder_name")
    @Length(max = 18)
    @NotEmpty
    private String accountHolderName;
}
