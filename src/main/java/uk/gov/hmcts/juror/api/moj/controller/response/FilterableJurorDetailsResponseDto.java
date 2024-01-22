package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAddressDto;
import uk.gov.hmcts.juror.api.validation.JurorNumber;


/**
 * Response DTO for Juror details on the Juror record.
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Schema(description = "Juror detail information for the Juror Record")
public class FilterableJurorDetailsResponseDto {

    @JsonProperty("juror_number")
    @JurorNumber
    @NotBlank
    private String jurorNumber;

    @JsonProperty("juror_version")
    @Positive
    private Long jurorVersion;

    @JsonProperty("name")
    private NameDetails nameDetails;

    @JsonProperty("payment_details")
    private PaymentDetails paymentDetails;

    @JsonProperty("address")
    private JurorAddressDto address;
}