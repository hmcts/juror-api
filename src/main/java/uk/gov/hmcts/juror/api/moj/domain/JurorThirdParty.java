package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Length;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@Entity
@Table(name = "juror_third_party", schema = "juror_mod")
@Getter
@Setter
@Audited
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JurorThirdParty {

    @Id
    @NotNull
    @Column(name = "juror_number")
    private String jurorNumber;

    @Column(name = "first_name")
    @Length(max = 50)
    private String firstName;

    @Column(name = "last_name")
    @Length(max = 50)
    private String lastName;

    @Column(name = "relationship")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String relationship;

    @Column(name = "main_phone")
    @Length(max = 50)
    private String mainPhone;

    @Column(name = "other_phone")
    @Length(max = 50)
    private String otherPhone;

    @Column(name = "email_address")
    @Length(max = 254)
    private String emailAddress;

    @Column(name = "reason")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 1250)
    private String reason;

    @Column(name = "other_reason")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 1250)
    private String otherReason;

    @Column(name = "contact_juror_by_phone")
    private boolean contactJurorByPhone;

    @Column(name = "contact_juror_by_email")
    private boolean contactJurorByEmail;

    public JurorThirdParty(String jurorNumber) {
        this.jurorNumber = jurorNumber;
    }
}
