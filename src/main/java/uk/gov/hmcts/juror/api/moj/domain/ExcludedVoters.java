package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

@Entity
@Table(name = "excluded_voters", schema = "juror_mod")
@NoArgsConstructor
@IdClass(ExcludedVotersId.class)
@Getter
@Setter
public class ExcludedVoters implements Serializable {

    @Id
    @Column(name = "firstname")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String firstName;

    @Id
    @Column(name = "lastname")
    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String lastName;

    @Id
    @Column(name = "address_line1")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String address1;

    @Id
    @Column(name = "postcode")
    @Pattern(regexp = POSTCODE_REGEX)
    @Length(max = 10)
    @NotBlank
    private String postcode;

}
