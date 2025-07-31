package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Allows the same data structure to be utilised by the MOJ package but
 * from a different source (JUROR_MOD.MOD_JUROR_DETAIL). A new view has been created to include the
 * {@link SummonsSnapshot} data, replacing the original data source for relevant fields (where applicable).
 * This will be used for viewing a digital summons reply for a juror record in the re-written modernisation app
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "mod_juror_detail", schema = "juror_mod")
@EqualsAndHashCode(exclude = "changeLogs")
public class ModJurorDetail implements Serializable {

    @Id
    @Column(name = "juror_number")
    private String jurorNumber;

    /**
     * Replicating existing Juror Digital behaviour (currently required by the front end).
     * Optimistic locking strategy to be updated to use ETag headers in the future.
     */
    @Version
    private Integer version;

    @Column(name = "title")
    private String title;

    @Column(name = "new_title")
    private String newTitle;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "new_first_name")
    private String newFirstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "new_last_name")
    private String newLastName;

    @Column(name = "reply_type")
    private String replyType;

    @Column(name = "processing_status")
    private String processingStatus;

    @NotNull
    @Column(name = "owner")
    @Length(max = 3)
    private String owner;

    @Column(name = "processing_complete")
    private Boolean processingComplete = Boolean.FALSE;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status")
    private Long status;

    @Column(name = "address")
    private String jurorAddress1;

    @Column(name = "new_address")
    private String newJurorAddress1;

    @Column(name = "address2")
    private String jurorAddress2;

    @Column(name = "new_address2")
    private String newJurorAddress2;

    @Column(name = "address3")
    private String jurorAddress3;

    @Column(name = "new_address3")
    private String newJurorAddress3;

    @Column(name = "address4")
    private String jurorAddress4;

    @Column(name = "new_address4")
    private String newJurorAddress4;

    @Column(name = "address5")
    private String jurorAddress5;

    @Column(name = "new_address5")
    private String newJurorAddress5;

    @Column(name = "zip")
    private String jurorPostcode;

    @Column(name = "new_zip")
    private String newJurorPostcode;

    @Column(name = "next_date")
    private LocalDate hearingDate;

    @Column(name = "loc_attend_time")
    private String hearingTime;

    @Column(name = "ret_date")
    private Date poolDate;

    @Column(name = "loc_name")
    private String courtName;

    @Column(name = "loc_court_name")
    private String courtLocName;

    @Column(name = "loc_code")
    private String courtCode;

    @Column(name = "loc_address1")
    private String courtAddress1;

    @Column(name = "loc_address2")
    private String courtAddress2;

    @Column(name = "loc_address3")
    private String courtAddress3;

    @Column(name = "loc_address4")
    private String courtAddress4;

    @Column(name = "loc_address5")
    private String courtAddress5;

    @Column(name = "loc_address6")
    private String courtAddress6;

    @Column(name = "loc_zip")
    private String courtPostcode;

    @Column(name = "pool_no")
    private String poolNumber;

    @Column(name = "notes")
    private String notes;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "new_phone_number")
    private String newPhoneNumber;

    @Column(name = "alt_phone_number")
    private String altPhoneNumber;

    @Column(name = "new_alt_phone_number")
    private String newAltPhoneNumber;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "new_dob")
    private LocalDate newDateOfBirth;

    @Column(name = "date_received")
    private LocalDate dateReceived;

    @Column(name = "email")
    private String email;

    @Column(name = "new_email")
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
    private String thirdPartyMainPhoneNumber;

    @Column(name = "other_phone")
    private String thirdPartyAlternatePhoneNumber;

    @Column(name = "email_address")
    private String thirdPartyEmailAddress;

    @Column(name = "relationship")
    private String thirdPartyRelationship;

    @Column(name = "residency")
    private Boolean residency;

    @Column(name = "residency_detail")
    private String residencyDetail;

    @Column(name = "mental_health_act")
    private Boolean mentalHealthAct;

    @Column(name = "mental_health_act_details")
    private String mentalHealthActDetails;

    @Column(name = "bail")
    private Boolean bail;

    @Column(name = "bail_details")
    private String bailDetails;

    @Column(name = "convictions")
    private Boolean convictions;

    @Column(name = "convictions_details")
    private String convictionsDetails;

    @Column(name = "deferral_reason")
    private String deferralReason;

    /**
     * A text description of a date - not an actual date type.
     */
    @Column(name = "deferral_date")
    private String deferralDate;

    @Column(name = "reasonable_adjustments_arrangements")
    private String reasonableAdjustmentsArrangements;

    @Column(name = "excusal_reason")
    private String excusalReason;

    /**
     * Whether the juror's email details should be used (false = use third party details).
     */
    @Column(name = "juror_email_details")
    @NotNull
    private Boolean useJurorEmailDetails = Boolean.TRUE;

    /**
     * Whether the juror's phone details should be used (false = use third party details).
     */
    @Column(name = "juror_phone_details")
    @NotNull
    private Boolean useJurorPhoneDetails = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_login")
    private User assignedStaffMember;

    @Column(name = "staff_assignment_date")
    private Date staffAssignmentDate;

    @Transient
    private Boolean assignmentAllowed = Boolean.FALSE;

    @OneToMany(mappedBy = "jurorNumber")
    private List<ContactLog> contactLogs = new ArrayList<>();

    @OneToMany(mappedBy = "jurorNumber")
    private List<JurorResponseCjsEmployment> cjsEmployments = new ArrayList<>();

    @OneToMany(mappedBy = "jurorNumber")
    private List<JurorReasonableAdjustment> reasonableAdjustments = new ArrayList<>();

    @Column(name = "urgent")
    private Boolean urgent;

    @Column(name = "welsh")
    private Boolean welsh = Boolean.FALSE;

    @Column(name = "welsh_court")
    private Boolean welshCourt = Boolean.FALSE;

    @Transient
    private Boolean slaOverdue = Boolean.FALSE;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "deferral")
    private Boolean deferral;

    @Column(name = "excusal")
    private Boolean excusal;


    public boolean isWelshCourt() {
        return this.welshCourt;
    }
}
