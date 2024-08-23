package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorCjs;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeed;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.utils.converters.YesNo10Converter;
import uk.gov.hmcts.juror.api.validation.DateOfBirth;
import uk.gov.hmcts.juror.api.validation.ThirdPartyOtherReason;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * Entity representing the view of juror responses to be saved to juror_digital.juror_response table directly from the
 * public front end only.
 */
@Entity
@Table(name = "JUROR_RESPONSE", schema = "JUROR_DIGITAL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ThirdPartyOtherReason
@ToString(exclude = {"specialNeeds", "cjsEmployments"})// lazy init fields
@EqualsAndHashCode(exclude = {"workPhone", "cjsEmployments", "specialNeeds", "staff"})
@Deprecated(forRemoval = true)
public class JurorResponse implements Serializable {
    /*
     * Constants for field names. MUST match exactly!
     */
    public static final String TITLE = "title";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String ADDRESS = "address";
    public static final String ADDRESS2 = "address2";
    public static final String ADDRESS3 = "address3";
    public static final String ADDRESS4 = "address4";
    public static final String ADDRESS5 = "address5";
    public static final String POSTCODE = "postcode";
    public static final String DOB = "dateOfBirth";
    /**
     * Juror's main phone number.
     *
     * @see #phoneNumber
     */
    public static final String PHONE_NUMBER = "phoneNumber";

    /**
     * Juror's alt phone number.
     *
     * @see #otherPhone
     */
    public static final String ALT_PHONE_NUMBER = "altPhoneNumber";
    /**
     * Juror email address.
     *
     * @see #email
     */
    public static final String EMAIL = "email";
    /*
     * Third party specific field names.
     */
    public static final String JUROR_PHONE_DETAILS = "jurorPhoneDetails";
    public static final String JUROR_EMAIL_DETAILS = "jurorEmailDetails";
    /**
     * Third party's main phone number.
     */
    public static final String THIRD_PARTY_MAIN_PHONE = "mainPhone";// TP phone
    /**
     * Third party's alt phone number.
     */
    public static final String THIRD_PARTY_OTHER_PHONE = "otherPhone";// TP mobile phone
    /**
     * Third party's email address.
     */
    public static final String THIRD_PARTY_EMAIL_ADDRESS = "emailAddress";// TP email
    public static final String THIRD_PARTY_FIRST_NAME = "thirdPartyFName";
    public static final String THIRD_PARTY_LAST_NAME = "thirdPartyLName";
    public static final String RELATIONSHIP = "relationship";
    public static final String THIRD_PARTY_REASON = "thirdPartyReason";
    public static final String THIRD_PARTY_OTHER_REASON = "thirdPartyOtherReason";

    public static final String EXCUSAL_REASON = "excusalReason";
    public static final String DEFERRAL_REASON = "deferralReason";
    public static final String DEFERRAL_DATE = "deferralDate";

    public static final String SPECIAL_NEEDS_ARRANGEMENTS = "specialNeedsArrangements";

    /**
     * Eligibility.
     */
    public static final String RESIDENCY = "residency";
    public static final String RESIDENCY_DETAIL = "residencyDetail";
    public static final String MENTAL_HEALTH_ACT = "mentalHealthAct";
    public static final String MENTAL_HEALTH_ACT_DETAILS = "mentalHealthActDetails";
    public static final String BAIL = "bail";
    public static final String BAIL_DETAILS = "bailDetails";
    public static final String CONVICTIONS = "convictions";
    public static final String CONVICTIONS_DETAILS = "convictionsDetails";

    public static final String CJS_EMPLOYMENTS = "cjsEmployments";

    @Id
    @Column(name = "JUROR_NUMBER")
    @Pattern(regexp = JUROR_NUMBER)
    @Length(max = 9)
    private String jurorNumber;

    @Version
    private Integer version;

    @Column(name = "DATE_RECEIVED")
    private Date dateReceived;

    @Column(name = "TITLE")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String title;

    @Column(name = "FIRST_NAME")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String firstName;

    @Column(name = "LAST_NAME")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String lastName;

    @NotEmpty
    @Column(name = "ADDRESS")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
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

    @Column(name = "ZIP")
    @Pattern.List({
        @Pattern(regexp = NO_PIPES_REGEX),
        @Pattern(regexp = POSTCODE_REGEX)
    })
    @Length(max = 10)
    private String postcode;

    @Column(name = "PROCESSING_STATUS")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.TODO;

    @DateOfBirth
    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    /**
     * Juror phone number.
     */
    @Column(name = "PHONE_NUMBER")
    @Length(max = 15)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String phoneNumber;

    /**
     * Juror alternative number.
     */
    @Column(name = "ALT_PHONE_NUMBER")
    @Length(max = 15)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String altPhoneNumber;

    /**
     * Juror email address.
     */
    @Column(name = "EMAIL")
    @Length(max = 254)
    @Pattern(regexp = ValidationConstants.EMAIL_ADDRESS_REGEX)
    private String email;

    @Column(name = "RESIDENCY")
    @Convert(converter = YesNo10Converter.class)
    @NotNull
    @Builder.Default
    private Boolean residency = Boolean.TRUE;

    @Column(name = "RESIDENCY_DETAIL")
    @Length(max = 1250)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String residencyDetail;

    @Column(name = "MENTAL_HEALTH_ACT")
    @NotNull
    @Convert(converter = YesNo10Converter.class)
    @Builder.Default
    private Boolean mentalHealthAct = Boolean.FALSE;

    @Column(name = "MENTAL_HEALTH_ACT_DETAILS")
    @Length(max = 2020)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String mentalHealthActDetails;

    @Column(name = "BAIL")
    @Convert(converter = YesNo10Converter.class)
    @Builder.Default
    private Boolean bail = Boolean.FALSE;

    @Column(name = "BAIL_DETAILS")
    @Length(max = 1250)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String bailDetails;

    @Column(name = "CONVICTIONS")
    @NotNull
    @Convert(converter = YesNo10Converter.class)
    @Builder.Default
    private Boolean convictions = Boolean.FALSE;

    @Column(name = "CONVICTIONS_DETAILS")
    @Length(max = 1250)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String convictionsDetails;

    @Column(name = "DEFERRAL_REASON")
    @Length(max = 1250)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String deferralReason;

    /**
     * This is a text description of dates available, not a date type itself.
     */
    @Column(name = "DEFERRAL_DATE")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 1000)
    private String deferralDate;

    @Column(name = "SPECIAL_NEEDS_ARRANGEMENTS")
    @Length(max = 1000)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String specialNeedsArrangements;

    @Column(name = "EXCUSAL_REASON")
    @Length(max = 1250)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String excusalReason;

    @Column(name = "PROCESSING_COMPLETE")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    @Builder.Default
    private Boolean processingComplete = Boolean.FALSE;

    @Column(name = "COMPLETED_AT")
    private Date completedAt;

    @Column(name = "THIRDPARTY_FNAME")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String thirdPartyFName;

    @Column(name = "THIRDPARTY_LNAME")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String thirdPartyLName;

    @Column(name = "RELATIONSHIP")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String relationship;

    /**
     * Third party phone.
     */
    @Column(name = "MAIN_PHONE")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String mainPhone;

    /**
     * Third party alt phone.
     */
    @Column(name = "OTHER_PHONE")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String otherPhone;

    /**
     * Third party email address.
     */
    @Column(name = "EMAIL_ADDRESS")
    @Length(max = 254)
    private String emailAddress;

    @Column(name = "THIRDPARTY_REASON")
    @Length(max = 1250)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String thirdPartyReason;

    @Column(name = "THIRDPARTY_OTHER_REASON")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 1250)
    private String thirdPartyOtherReason;

    /**
     * Whether the juror phone fields should be used when copying back to JUROR.
     * True for juror fields, false for third party fields.
     */
    @Column(name = "JUROR_PHONE_DETAILS")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    @Builder.Default
    private Boolean jurorPhoneDetails = Boolean.TRUE;

    /**
     * Whether the juror email fields should be used when copying back to JUROR
     * True for juror fields, false for third party fields.
     */
    @Column(name = "JUROR_EMAIL_DETAILS")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    @Builder.Default
    private Boolean jurorEmailDetails = Boolean.TRUE;

    /**
     * Staff member assigned to this response.
     *
     * @since Sprint 12
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAFF_LOGIN")
    private User staff;

    /**
     * The date of the staff assignment (a single day).
     *
     * @since Sprint 12
     */
    @Column(name = "STAFF_ASSIGNMENT_DATE")
    private Date staffAssignmentDate;

    /**
     * Holder for switching fields during a merge operation.
     */
    @Transient
    private String workPhone;

    /**
     * List of {@link BureauJurorSpecialNeed} entities associated with this entity.
     */
    @OneToMany(mappedBy = "jurorNumber")
    @Builder.Default
    private List<BureauJurorSpecialNeed> specialNeeds = new ArrayList<>();

    /**
     * List of {@link BureauJurorCjs} entities associated with this entity.
     */
    @OneToMany(mappedBy = "jurorNumber")
    @Builder.Default
    private List<BureauJurorCjs> cjsEmployments = new ArrayList<>();
    /**
     * Flag that this response is urgent.
     */
    @Column(name = "URGENT")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean urgent;

    /**
     * Flag this response as welsh language.
     */
    @Column(name = "WELSH")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    @Builder.Default
    private Boolean welsh = Boolean.FALSE;
}
