package uk.gov.hmcts.juror.api.moj.audit.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.Address;
import uk.gov.hmcts.juror.api.moj.domain.RevisionInfo;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@Entity
@Table(name = "juror_audit", schema = "juror_mod")
@NoArgsConstructor
@Getter
@AllArgsConstructor
@SuperBuilder
@ToString
public class JurorAudit extends Address {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "revision", referencedColumnName = "revision_number",
        insertable = false, updatable = false, nullable = false)
    @Id
    private RevisionInfo revisionInfo;


    @NotBlank
    @Column(name = "juror_number")
    @Pattern(regexp = JUROR_NUMBER)
    @Length(max = 9)
    private String jurorNumber;



    @Column(name = "title")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String title;

    @Column(name = "first_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String firstName;

    @Column(name = "last_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String lastName;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "h_phone")
    @Length(max = 15)
    private String phoneNumber;

    @Column(name = "w_phone")
    @Length(max = 15)
    private String workPhone;

    @Column(name = "w_ph_local")
    @Length(max = 4)
    private String workPhoneExtension;


    @Length(max = 20)
    @Column(name = "smart_card_number")
    private String smartCardNumber;


    @Column(name = "sort_code")
    @Length(max = 6)
    private String sortCode;

    @Column(name = "bank_acct_name")
    @Length(max = 18)
    private String bankAccountName;

    @Column(name = "bank_acct_no")
    @Length(max = 8)
    private String bankAccountNumber;

    @Column(name = "bldg_soc_roll_no")
    @Length(max = 18)
    private String buildingSocietyRollNumber;


    @Column(name = "m_phone")
    @Length(max = 15)
    private String altPhoneNumber;

    @Length(max = 254)
    @Column(name = "h_email")
    private String email;


    @Column(name = "pending_title")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String pendingTitle;

    @Column(name = "pending_first_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String pendingFirstName;

    @Column(name = "pending_last_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String pendingLastName;


    @Column(name = "claiming_subsistence_allowance")
    private boolean claimingSubsistenceAllowance;
}
