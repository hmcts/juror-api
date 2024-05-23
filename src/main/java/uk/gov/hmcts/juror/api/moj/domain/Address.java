package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Audited
public class Address implements Serializable {

    @Column(name = "address_line_1")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String addressLine1;

    @Column(name = "address_line_2")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String addressLine2;

    @Column(name = "address_line_3")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String addressLine3;

    @Column(name = "address_line_4")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String addressLine4;

    @Column(name = "address_line_5")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String addressLine5;

    @Column(name = "postcode")
    @Length(max = 10)
    @Pattern(regexp = POSTCODE_REGEX)
    private String postcode;


    public String getCombinedAddressExcludingPostcode() {
        return addressLine1 + ","
            + addressLine2 + ","
            + addressLine3 + ","
            + addressLine4 + ","
            + addressLine5;
    }
}
