package uk.gov.hmcts.juror.api.bureau.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * View of a Juror response in the Juror Digital bureau officer context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BUREAU_JUROR_DETAIL", schema = "JUROR_DIGITAL_USER")
@Schema(description = "All Details available on a juror in the system ")
@SuppressWarnings("PMD.TooManyFields")
@EqualsAndHashCode(exclude = "changeLogs")
public class BureauJurorDetail implements Serializable {
    @Id
    @Column(name = "PART_NO")
    @Schema(description = "Juror number")
    private String jurorNumber;

    @Schema(description = "Optimistic locking version.", requiredMode = Schema.RequiredMode.REQUIRED)
    @Version
    private Integer version;

    @Column(name = "title")
    @Schema(description = "Person title")
    private String title;

    @Column(name = "new_title")
    @Schema(description = "Updated title (if any)")
    private String newTitle;

    @Column(name = "FNAME")
    @Schema(description = "Juror firstname")
    private String firstName;

    @Column(name = "new_first_name")
    @Schema(description = "Juror firstname updated")
    private String newFirstName;

    @Column(name = "LNAME")
    @Schema(description = "Juror lastname")
    private String lastName;

    @Column(name = "new_last_name")
    @Schema(description = "Juror lastname updated")
    private String newLastName;

    @Column(name = "PROCESSING_STATUS")
    @Schema(description = "Processing status of response")
    private String processingStatus;

    @Column(name = "PROCESSING_COMPLETE")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean processingComplete = Boolean.FALSE;

    @Column(name = "COMPLETED_AT")
    private Date completedAt;

    @Column(name = "STATUS")
    @Schema(description = "Status index number")
    private Long status;

    @Column(name = "ADDRESS")
    @Schema(description = "Juror address")
    private String jurorAddress1;

    @Column(name = "NEW_ADDRESS")
    @Schema(description = "Juror address updated")
    private String newJurorAddress1;

    @Column(name = "ADDRESS2")
    @Schema(description = "Juror address line 2")
    private String jurorAddress2;

    @Column(name = "NEW_ADDRESS2")
    @Schema(description = "Juror address line 2 updated")
    private String newJurorAddress2;

    @Column(name = "ADDRESS3")
    @Schema(description = "Juror address line 3")
    private String jurorAddress3;

    @Column(name = "NEW_ADDRESS3")
    @Schema(description = "Juror address line 3 updated")
    private String newJurorAddress3;

    @Column(name = "ADDRESS4")
    @Schema(description = "Juror address line 4")
    private String jurorAddress4;

    @Column(name = "NEW_ADDRESS4")
    @Schema(description = "Juror address line 4 updated")
    private String newJurorAddress4;

    @Column(name = "ADDRESS5")
    @Schema(description = "Juror address line 5")
    private String jurorAddress5;

    @Column(name = "NEW_ADDRESS5")
    @Schema(description = "Juror address line 5 updated")
    private String newJurorAddress5;

    @Column(name = "ADDRESS6")
    @Schema(description = "Juror address line 6")
    private String jurorAddress6;

    @Column(name = "NEW_ADDRESS6")
    @Schema(description = "Juror address line 6 updated")
    private String newJurorAddress6;

    @Column(name = "ZIP")
    @Schema(description = "Juror postcode")
    private String jurorPostcode;

    @Column(name = "NEW_ZIP")
    @Schema(description = "Juror new postcode")
    private String newJurorPostcode;

    /**
     * Date and time allocated slot CJ-7820-64.
     */
    @Column(name = "NEXT_DATE")
    @Schema(description = "Juror court date")
    private Date hearingDate;

    @Column(name = "LOC_ATTEND_TIME")
    @Schema(description = "Juror court time")
    private String hearingTime;

    @Column(name = "RET_DATE")
    @Schema(description = "Pool court date")
    private Date poolDate;

    /**
     * Court name.
     */
    @Column(name = "LOC_NAME")
    @Schema(description = "Court name")
    private String courtName;

    /**
     * Court location name.
     */
    @Column(name = "LOC_COURT_NAME")
    @Schema(description = "Court location name")
    private String courtLocName;

    /**
     * Court code.
     */
    @Column(name = "LOC_CODE")
    @Schema(description = "Court code")
    private String courtCode;

    /**
     * ß
     * Court address line 1.
     */
    @Column(name = "LOC_ADDRESS1")
    @Schema(description = "Court address line1")
    private String courtAddress1;

    /**
     * Court address line 2.
     */
    @Column(name = "LOC_ADDRESS2")
    @Schema(description = "Court address line2")
    private String courtAddress2;

    /**
     * Court address line 3.
     */
    @Column(name = "LOC_ADDRESS3")
    @Schema(description = "Court address line3")
    private String courtAddress3;

    /**
     * Court address line 4.
     */
    @Column(name = "LOC_ADDRESS4")
    @Schema(description = "Court address line4")
    private String courtAddress4;

    /**
     * Court address line 5.
     */
    @Column(name = "LOC_ADDRESS5")
    @Schema(description = "Court address line5")
    private String courtAddress5;

    /**
     * Court address line 6.<br>
     * Note: Possibly unused field.
     */
    @Column(name = "LOC_ADDRESS6")
    @Schema(description = "Court address line6")
    private String courtAddress6;

    /**
     * Court postcode.
     */
    @Column(name = "LOC_ZIP")
    @Schema(description = "Court postcode")
    private String courtPostcode;

    /**
     * Juror's pool id.
     */
    @Column(name = "POOL_NO")
    @Schema(description = "Juror pool number")
    private String poolNumber;

    @Column(name = "READ_ONLY")
    @Schema(description = "Read only status for pool record")
    private String readOnly;

    @Column(name = "NOTES")
    @Schema(description = "Notes")
    private String notes;

    @Column(name = "phone_number")
    @Schema(description = "Phone number")
    private String phoneNumber;

    @Column(name = "new_phone_number")
    @Schema(description = "Phone number updated")
    private String newPhoneNumber;

    @Column(name = "alt_phone_number")
    @Schema(description = "Alternative phone number")
    private String altPhoneNumber;

    @Column(name = "new_alt_phone_number")
    @Schema(description = "Alternative phone number updated")
    private String newAltPhoneNumber;

    @Column(name = "dob")
    @Schema(description = "Juror date of birth")
    private Date dateOfBirth;

    @Column(name = "new_dob")
    @Schema(description = "Juror date of birth updated")
    private Date newDateOfBirth;

    @Column(name = "date_received")
    @Schema(description = "Date response was received")
    private Date dateReceived;

    @Column(name = "email")
    @Schema(description = "Juror email address")
    private String email;

    @Column(name = "new_email")
    @Schema(description = "Juror email address updated")
    private String newEmail;

    @Column(name = "thirdparty_fname")
    private String thirdPartyFirstName;

    @Column(name = "thirdparty_lname")
    private String thirdPartyLastName;

    @Column(name = "thirdparty_reason")
    private String thirdPartyReason;

    @Column(name = "thirdparty_other_reason")
    private String thirdPartyOtherReason;

    @Column(name = "main_phone")
    @Schema(description = "Third party main phone number")
    private String thirdPartyMainPhoneNumber;

    @Column(name = "other_phone")
    @Schema(description = "Third party alternate phone number")
    private String thirdPartyAlternatePhoneNumber;

    @Column(name = "email_address")
    @Schema(description = "Third party email address")
    private String thirdPartyEmailAddress;

    @Column(name = "relationship")
    @Schema(description = "Third party's relationship to juror")
    private String thirdPartyRelationship;

    @Column(name = "residency")
    @Schema(description = "Juror residency")
    private String residency;

    @Column(name = "residency_detail")
    @Schema(description = "Juror residency description")
    private String residencyDetail;

    @Column(name = "mental_health_act")
    @Schema(description = "Mental Health Act option selected")
    private String mentalHealthAct;

    @Column(name = "mental_health_act_details")
    @Schema(description = "Description about mental health act")
    private String mentalHealthActDetails;

    @Column(name = "bail")
    @Schema(description = "Bail option selected")
    private String bail;

    @Column(name = "bail_details")
    @Schema(description = "Details about bail")
    private String bailDetails;

    @Column(name = "convictions")
    @Schema(description = "Conviction option selected")
    private String convictions;

    @Column(name = "convictions_details")
    @Schema(description = "Details about convictions")
    private String convictionsDetails;

    @Column(name = "deferral_reason")
    @Schema(description = "Reason about deferring jury duties")
    private String deferralReason;

    /**
     * A text description of a date - not an actual date type.
     */
    @Column(name = "deferral_date")
    @Schema(description = "Deferred court date")
    private String deferralDate;

    @Column(name = "special_needs_arrangements")
    @Schema(description = "Details about any special arrangements or needs")
    private String specialNeedsArrangements;

    @Column(name = "excusal_reason")
    @Schema(description = "Details about jury duty excusal")
    private String excusalReason;

    @Column(name = "juror_email_details")
    @Schema(description = "Whether the juror's email details should be used (false = use third party details)")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    @NotNull
    private Boolean useJurorEmailDetails = Boolean.TRUE;

    @Column(name = "juror_phone_details")
    @Schema(description = "Whether the juror's phone details should be used (false = use third party details)")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    @NotNull
    private Boolean useJurorPhoneDetails = Boolean.TRUE;

    /**
     * Staff member assigned to this response.
     *
     * @since Sprint 12
     */
    @ManyToOne
    @JoinColumn(name = "STAFF_LOGIN")
    private User assignedStaffMember;

    /**
     * The date of the staff assignment (a single day).
     *
     * @since Sprint 12
     */
    @Column(name = "STAFF_ASSIGNMENT_DATE")
    private Date staffAssignmentDate;
    /**
     * Should the user be allowed to re-assign this response.
     *
     * @implNote This allows us to flag if a user is unable to update this assignment.
     */
    @Schema(description = "Should the user be allowed to re-assign this response.")
    @Transient
    private Boolean assignmentAllowed = Boolean.FALSE;

    @OneToMany(mappedBy = "jurorNumber")
    private List<PhoneLog> phoneLogs = new ArrayList<>();

    @OneToMany(mappedBy = "jurorNumber")
    private List<BureauJurorCjs> cjsEmployments = new ArrayList<>();


    @OneToMany(mappedBy = "jurorNumber")
    private List<BureauJurorSpecialNeed> specialNeeds = new ArrayList<>();

    /**
     * Flag that this response is urgent.
     */
    @Column(name = "URGENT")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean urgent;

    /**
     * Flag this response as super urgent.
     */
    @Column(name = "SUPER_URGENT")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean superUrgent;

    /**
     * Flag this response as welsh language.
     */
    @Column(name = "WELSH")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean welsh = Boolean.FALSE;

    /**
     * Flag this response as overdue.
     *
     * @see uk.gov.hmcts.juror.api.bureau.service.UrgencyService#flagSlaOverdueForResponse(BureauJurorDetail)
     */
    @Transient
    private Boolean slaOverdue = Boolean.FALSE;

    @OneToMany(mappedBy = "jurorNumber")
    private Set<ChangeLog> changeLogs = new HashSet<>();
}
