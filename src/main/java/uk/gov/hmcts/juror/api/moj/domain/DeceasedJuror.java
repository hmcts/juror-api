package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Length;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

@NoArgsConstructor
@Getter
@Setter
@Audited
@AllArgsConstructor
public class DeceasedJuror {

    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String firstName;

    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String lastName;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String addressLine1;

    @Length(max = 10)
    @Pattern(regexp = POSTCODE_REGEX)
    @Setter(AccessLevel.NONE)
    //Must be uppercase
    private String postcode;
}
