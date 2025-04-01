package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

@Entity
@IdClass(Voters.VotersId.class)
@Table(name = "voters", schema = "juror_mod")
@NoArgsConstructor
@Getter
@Setter
public class Voters implements Serializable {

    @Id
    @Column(name = "LOC_CODE")
    @Length(max = 3)
    private String locCode;

    @Id
    @Column(name = "PART_NO")
    @Pattern(regexp = JUROR_NUMBER)
    @Length(max = 9)
    @NotNull
    private String jurorNumber;

    @Column(name = "REGISTER_LETT")
    @Length(max = 5)
    private String registerLett;

    @Column(name = "POLL_NUMBER")
    @Length(max = 5)
    private String pollNumber;

    @Column(name = "NEW_MARKER")
    @Length(max = 1)
    private String newMarker;

    @Column(name = "TITLE")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String title;

    @Column(name = "FNAME")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotNull
    private String firstName;

    @Column(name = "LNAME")
    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotNull
    private String lastName;

    @Column(name = "DOB")
    @LocalDateOfBirth
    private LocalDate dateOfBirth;

    @Column(name = "ADDRESS")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotNull
    private String address;

    @Column(name = "ADDRESS2")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String address2;

    @Column(name = "ADDRESS3")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String address3;

    @Column(name = "ADDRESS4")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String address4;

    @Column(name = "ADDRESS5")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String address5;

    @Column(name = "ADDRESS6")
    @Null
    private String address6 = null;

    @Column(name = "ZIP")
    @Pattern(regexp = POSTCODE_REGEX)
    @Length(max = 10)
    private String postcode;

    @Column(name = "postcode_start", updatable = false, insertable = false)
    private String postcodeStart;

    @Column(name = "DATE_SELECTED1")
    private Date dateSelected1;

    @Column(name = "REC_NUM")
    private Integer recNumber;

    @Size(max = 1)
    @Column(name = "PERM_DISQUAL")
    private String permDisqual;

    @Size(max = 1)
    @Column(name = "FLAGS")
    private String flags;

    @Column(name = "SOURCE_ID")
    @Length(max = 1)
    private String sourceId;

    @EqualsAndHashCode
    @Setter
    @Getter
    public static class VotersId implements Serializable {

        private String locCode;
        private String jurorNumber;

    }

}
