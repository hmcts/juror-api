package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.NameDetails;
import uk.gov.hmcts.juror.api.moj.controller.response.PaymentDetails;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.util.List;
import java.util.function.BiConsumer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FilterableJurorDetailsRequestDto {

    @JurorNumber
    @NotBlank
    @JsonProperty("juror_number")
    private String jurorNumber;

    @JsonProperty("juror_version")
    @Positive
    private Long jurorVersion;

    @JsonProperty("include")
    @NotEmpty
    private List<@NotNull IncludeType> include;


    public enum IncludeType {
        PAYMENT_DETAILS((dto, context) -> dto.setPaymentDetails(PaymentDetails.from(context.juror()))),
        NAME_DETAILS((dto, context) -> dto.setNameDetails(NameDetails.from(context.juror()))),
        ADDRESS_DETAILS((dto, context) -> dto.setAddress(JurorAddressDto.from(context.juror())));

        private final BiConsumer<FilterableJurorDetailsResponseDto, FilterContext> filter;

        IncludeType(BiConsumer<FilterableJurorDetailsResponseDto, FilterContext> filter) {
            this.filter = filter;
        }

        public void apply(FilterableJurorDetailsResponseDto dto, FilterContext context) {
            this.filter.accept(dto, context);
        }
    }


    public record FilterContext(Juror juror) {
    }
}
