package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.DateOfBirth;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * View of the legacy JUROR.POOL table data where OWNER <> 400 and IS_ACTIVE = 'Y' </>.
 * H_EMAIL or M_PHONE exists and H_EMAIL AND M_PHONE exists
 * but not where H_EMAIL AND M_PHONE not exists
 */

@Entity
@Table(name = "POOL_COURT", schema = "JUROR_DIGITAL_USER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"poolExtend", "court"})
@Slf4j
public class PoolCourt implements Serializable {
    /**
     * Default value for varchar fields if null.
     *
     * @see #prePersist()
     */

    private static final String SINGLE_SPACE_CHARACTER = " ";
    /**
     * Default pool status applied if null.
     *
     * @see #prePersist()
     */
    private static final long DEFAULT_POOL_STATUS = 1L;

    @Id
    @Column(name = "PART_NO")
    @Pattern.List({
        @Pattern(regexp = JUROR_NUMBER),
        @Pattern(regexp = NO_PIPES_REGEX)
    })
    @Length(max = 9)
    @NotNull
    private String jurorNumber;

    @Column(name = "OWNER")
    private String owner;

    @Column(name = "DATE_EXCUS")
    private Date excusalDate;


    @Column(name = "EXC_CODE")
    private String excusalCode;

    @Column(name = "ACC_EXC")
    private String excusalRejected;

    @Column(name = "DATE_DISQ")
    private Date disqualifyDate;

    @Column(name = "DISQ_CODE")
    private String disqualifyCode;

    @Column(name = "TITLE")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String title;

    @Column(name = "FNAME")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotEmpty
    private String firstName;

    @Column(name = "LNAME")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotEmpty
    private String lastName;

    @Column(name = "STATUS")
    private Long status;

    @Column(name = "ADDRESS")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotEmpty
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
    private String address6;

    @Column(name = "ZIP")
    @Pattern.List({
        @Pattern(regexp = NO_PIPES_REGEX),
        @Pattern(regexp = POSTCODE_REGEX)
    })
    @Length(max = 10)
    private String postcode;

    /*
     * RET_DATE is not nullable. check if changing the logic of the hearing date column!
     * Note that the NEXT_DATE is used by deferral/excusal processing so make sure to protect that logic!
     */
    @Column(name = "NEXT_DATE")
    private Date hearingDate;

    @Column(name = "POOL_NO")
    @Length(max = 9)
    @NotEmpty
    private String poolNumber;

    @Column(name = "DOB")
    @DateOfBirth
    private Date dateOfBirth;

    @Column(name = "H_PHONE")
    private String phoneNumber;

    @Column(name = "M_PHONE")
    private String altPhoneNumber;

    @Column(name = "H_EMAIL")
    @Length(max = 254)
    private String email;

    @Column(name = "W_PHONE")
    private String workPhone;

    @NotEmpty
    @Size(min = 1, max = 1)
    @Column(name = "RESPONDED")
    private String responded;


    @Column(name = "USER_EDTQ")
    @Length(max = 20)
    private String userEdtq;

    @Column(name = "PHOENIX_DATE", insertable = false, updatable = false)
    private Date phoenixDate;

    @Column(name = "DEF_DATE")
    private Date deferralDate;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "NO_DEF_POS")
    private Long noDefPos;

    @ManyToOne
    @JoinColumn(name = "LOC_CODE")
    private CourtLocation court;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PART_NO")
    private PoolExtend poolExtend;

    @Length(max = 2000)
    @Column(name = "NOTES")
    private String notes;

    public static final String RESPONDED = "Y";
    public static final String NOT_RESPONDED = "N";

    @Column(name = "WELSH")
    // @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private String welsh;

    @Column(name = "SPEC_NEED")
    private String specialNeed;

    @Column(name = "SERVICE_COMP_COMMS_STATUS")
    private String serviceCompCommsStatus;

    /**
     * Flag that this is read only.
     */
    @Column(name = "READ_ONLY")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean readOnly;

    @Column(name = "NOTIFICATIONS")
    private Integer notifications;

    @Column(name = "POLICE_CHECK")
    private String policeCheck;

    @Column(name = "COMPLETION_DATE")
    private Date completionDate;

    @PrePersist
    @PreUpdate
    private void prePersist() throws ParseException {
        if (this.title == null) {
            this.title = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted title");
        }
        if (this.status == null) {
            this.status = DEFAULT_POOL_STATUS;
            log.trace("Defaulted status");
        }
        if (this.address2 == null) {
            this.address2 = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted address2");
        }
        if (this.address3 == null) {
            this.address3 = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted address3");
        }
        if (this.address4 == null) {
            this.address4 = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted address4");
        }
        if (this.address5 == null) {
            this.address5 = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted address5");
        }
        if (this.postcode == null) {
            this.postcode = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted postcode");
        }
        if (this.phoneNumber == null) {
            this.phoneNumber = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted phoneNumber");
        }

        if (this.workPhone == null) {
            this.workPhone = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted workPhone");
        }
        if (this.userEdtq == null) {
            this.userEdtq = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted userEdtq");
        }
        if (this.notes == null) {
            this.notes = SINGLE_SPACE_CHARACTER;
            log.trace("Defaulted notes");
        }


        // Process deferral and excusal dates to avoid timezone issues JDB-2973
        this.deferralDate = adjustTimeOnDate(this.deferralDate);


    }

    private static Date adjustTimeOnDate(final Date date) {
        if (null == date) {
            return null;
        }
        return Date.from(ZonedDateTime.ofInstant(
            date.toInstant().plus(6, ChronoUnit.HOURS),
            ZoneId.systemDefault()
        ).with(LocalTime.of(6, 0)).toInstant());
    }


}
