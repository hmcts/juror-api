package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.controller.response.jurorresponse.IJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO of entire Juror response in the Juror Digital bureau screens.
 *
 * @see uk.gov.hmcts.juror.api.bureau.domain
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "All Details available on a juror in the system ")
@SuppressWarnings("PMD.TooManyFields")
//TODO: validation
public class BureauJurorDetailDto implements Serializable, IJurorResponse {

    @Schema(description = "Juror number")
    private String jurorNumber;

    @Schema(description = "Optimistic locking version.", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer version;

    @Schema(description = "Person title")
    private String title;

    @Schema(description = "Updated title (if any)")
    private String newTitle;

    @Schema(description = "Juror firstname")
    private String firstName;

    @Schema(description = "Juror firstname updated")
    private String newFirstName;

    @Schema(description = "Juror lastname")
    private String lastName;

    @Schema(description = "Juror lastname updated")
    private String newLastName;

    @Schema(name = "Reply method", description = "Reply method is either PAPER or DIGITAL", requiredMode =
        Schema.RequiredMode.REQUIRED)
    @NotNull
    private ReplyMethod replyMethod;

    @Schema(description = "Processing status of response")
    private String processingStatus;

    @Schema(description = "Date/time completed")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime completedAt;

    @Schema(description = "Status index number")
    private Long status;

    @Schema(description = "Juror address")
    private String jurorAddress1;

    @Schema(description = "Juror address updated")
    private String newJurorAddress1;

    @Schema(description = "Juror address line 2")
    private String jurorAddress2;

    @Schema(description = "Juror address line 2 updated")
    private String newJurorAddress2;

    @Schema(description = "Juror address line 3")
    private String jurorAddress3;

    @Schema(description = "Juror address line 3 updated")
    private String newJurorAddress3;

    @Schema(description = "Juror address line 4")
    private String jurorAddress4;

    @Schema(description = "Juror address line 4 updated")
    private String newJurorAddress4;

    @Schema(description = "Juror address line 5")
    private String jurorAddress5;

    @Schema(description = "Juror address line 5 updated")
    private String newJurorAddress5;

    @Schema(description = "Juror address line 6")
    private String jurorAddress6;

    @Schema(description = "Juror address line 6 updated")
    private String newJurorAddress6;

    @Schema(description = "Juror postcode")
    private String jurorPostcode;

    @Schema(description = "Juror new postcode")
    private String newJurorPostcode;

    /**
     * Date and time allocated slot CJ-7820-64.
     */
    @Schema(description = "Juror court date")
    private LocalDate hearingDate;

    @Schema(description = "Juror court time")
    private String hearingTime;

    /**
     * timezone = "Europe/London" added by Baharak Askarikeya - 06/08/19.
     * to fix the Daylight Saving Time issue - JDB-4096.
     */
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "Europe/London")
    @Schema(description = "Pool court date")
    private Date poolDate;

    /**
     * Court name.
     */
    @Schema(description = "Court name")
    private String courtName;

    /**
     * Court location name.
     */
    @Schema(description = "Court location name")
    private String courtLocName;

    /**
     * Court address line 1.
     */
    @Schema(description = "Court address line1")
    private String courtAddress1;

    /**
     * Court address line 2.
     */
    @Schema(description = "Court address line2")
    private String courtAddress2;

    /**
     * Court address line 3.
     */
    @Schema(description = "Court address line3")
    private String courtAddress3;

    /**
     * Court address line 4.
     */
    @Schema(description = "Court address line4")
    private String courtAddress4;

    /**
     * Court address line 5.
     */
    @Schema(description = "Court address line5")
    private String courtAddress5;

    /**
     * Court address line 6.
     * Note: Possibly unused field.
     */
    @Schema(description = "Court address line6")
    private String courtAddress6;

    /**
     * Court postcode.
     */
    @Schema(description = "Court postcode")
    private String courtPostcode;

    /**
     * Juror's pool id.
     */
    @Schema(description = "Juror pool number")
    private String poolNumber;

    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "Phone number")
    private String phoneNumber;

    @Schema(description = "Phone number updated")
    private String newPhoneNumber;

    @Schema(description = "Alternative phone number")
    private String altPhoneNumber;

    @Schema(description = "Alternative phone number updated")
    private String newAltPhoneNumber;

    @Schema(description = "Juror date of birth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Schema(description = "Juror date of birth updated")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newDateOfBirth;

    @Schema(description = "Date response was received")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReceived;

    @Schema(description = "Juror email address")
    private String email;

    @Schema(description = "Juror email address updated")
    private String newEmail;

    @Schema(description = "Third party first name")
    private String thirdPartyFirstName;

    @Schema(description = "Third party last name")
    private String thirdPartyLastName;

    @Schema(description = "Third party reason")
    private String thirdPartyReason;

    @Schema(description = "Third party other reason")
    private String thirdPartyOtherReason;

    @Schema(description = "Third party main phone number")
    private String thirdPartyMainPhoneNumber;

    @Schema(description = "Third party alternate phone number")
    private String thirdPartyAlternatePhoneNumber;

    @Schema(description = "Third party email address")
    private String thirdPartyEmailAddress;

    @Schema(description = "Third party's relationship to juror")
    private String thirdPartyRelationship;

    @Schema(description = "Juror residency")
    private Boolean residency;

    @Schema(description = "Juror residency description")
    private String residencyDetail;

    @Schema(description = "Mental Health Act option selected")
    private Boolean mentalHealthAct;

    @Schema(description = "Description about mental health act")
    private String mentalHealthActDetails;

    @Schema(description = "Bail option selected")
    private Boolean bail;

    @Schema(description = "Details about bail")
    private String bailDetails;

    @Schema(description = "Conviction option selected")
    private Boolean convictions;

    @Schema(description = "Details about convictions")
    private String convictionsDetails;

    @Schema(description = "Deferral")
    private Boolean deferral;

    @Schema(description = "Reason about deferring jury duties")
    private String deferralReason;

    /**
     * A text description of a date - not an actual date type.
     */
    @Schema(description = "Deferred court date")
    private String deferralDate;

    @Schema(description = "Details about any special arrangements or needs")
    private String specialNeedsArrangements;

    @Schema(description = "Excusal")
    private Boolean excusal;

    @Schema(description = "Details about jury duty excusal")
    private String excusalReason;

    @Schema(description = "Whether the juror's email details should be used (false = use third party details)")
    private Boolean useJurorEmailDetails = Boolean.TRUE;

    @Schema(description = "Whether the juror's phone details should be used (false = use third party details)")
    private Boolean useJurorPhoneDetails = Boolean.TRUE;

    /**
     * Staff member assigned to this response.
     *
     * @see User
     */
    @Schema(description = "Staff member assigned to this response")
    private StaffDto assignedStaffMember;

    /**
     * The date of the staff assignment (a single day).
     */
    @Schema(description = "Date of staff assignment")
    private Date staffAssignmentDate;

    /**
     * Should the user be allowed to re-assign this response.
     *
     * @implNote This allows us to flag if a user is unable to update this assignment.
     */
    @Schema(description = "Should the user be allowed to re-assign this response.")
    private Boolean assignmentAllowed = Boolean.FALSE;

    @Schema(description = "List of phone logs")
    private List<ContactLogDto> phoneLogs;

    @Schema(description = "List of Jurors CJS Employments")
    private List<CjsEmploymentDto> cjsEmployments;

    @Schema(description = "List of Jurors special needs")
    private List<ReasonableAdjustmentDto> specialNeeds;

    /**
     * Flag that this response is urgent.
     */
    @Schema(description = "Urgent status flag for response")
    private Boolean urgent;

    /**
     * Flag this response as overdue.
     */
    @Schema(description = "Overdue status flag for response")
    private Boolean slaOverdue = Boolean.FALSE;

    /**
     * Flag this response as welsh language.
     */
    @Schema(description = "Flag whether the pool member has requested correspondence in Welsh")
    private Boolean welsh = Boolean.FALSE;

    /**
     * Flag this as a response for a Welsh Court Location.
     */
    @Schema(description = "Flag whether the pool member has been summoned to a Welsh court Location")
    private boolean welshCourt;

    @JsonProperty("current_owner")
    @Schema(name = "Current Owner", description = "Current owner (3 digit code) of the juror record")
    private String currentOwner;

    public BureauJurorDetailDto(final ModJurorDetail jurorDetails) {
        this.jurorNumber = jurorDetails.getJurorNumber();
        this.version = jurorDetails.getVersion();
        this.title = jurorDetails.getTitle();
        this.newTitle = jurorDetails.getNewTitle();
        this.firstName = jurorDetails.getFirstName();
        this.newFirstName = jurorDetails.getNewFirstName();
        this.lastName = jurorDetails.getLastName();
        this.newLastName = jurorDetails.getNewLastName();
        this.replyMethod = "Paper".equals(jurorDetails.getReplyType()) ? ReplyMethod.PAPER :
            ReplyMethod.DIGITAL;
        this.processingStatus = jurorDetails.getProcessingStatus();
        this.completedAt = jurorDetails.getCompletedAt();
        this.status = jurorDetails.getStatus();
        this.jurorAddress1 = jurorDetails.getJurorAddress1();
        this.newJurorAddress1 = jurorDetails.getNewJurorAddress1();
        this.jurorAddress2 = jurorDetails.getJurorAddress2();
        this.newJurorAddress2 = jurorDetails.getNewJurorAddress2();
        this.jurorAddress3 = jurorDetails.getJurorAddress3();
        this.newJurorAddress3 = jurorDetails.getNewJurorAddress3();
        this.jurorAddress4 = jurorDetails.getJurorAddress4();
        this.newJurorAddress4 = jurorDetails.getNewJurorAddress4();
        this.jurorAddress5 = jurorDetails.getJurorAddress5();
        this.newJurorAddress5 = jurorDetails.getNewJurorAddress5();
        this.jurorPostcode = jurorDetails.getJurorPostcode();
        this.newJurorPostcode = jurorDetails.getNewJurorPostcode();
        this.hearingDate = jurorDetails.getHearingDate();
        this.hearingTime = jurorDetails.getHearingTime();
        this.poolDate = jurorDetails.getPoolDate();
        this.courtName = jurorDetails.getCourtName();
        this.courtLocName = jurorDetails.getCourtLocName();
        this.courtAddress1 = jurorDetails.getCourtAddress1();
        this.courtAddress2 = jurorDetails.getCourtAddress2();
        this.courtAddress3 = jurorDetails.getCourtAddress3();
        this.courtAddress4 = jurorDetails.getCourtAddress4();
        this.courtAddress5 = jurorDetails.getCourtAddress5();
        this.courtAddress6 = jurorDetails.getCourtAddress6();
        this.courtPostcode = jurorDetails.getCourtPostcode();
        this.poolNumber = jurorDetails.getPoolNumber();
        this.notes = jurorDetails.getNotes();
        this.phoneNumber = jurorDetails.getPhoneNumber();
        this.newPhoneNumber = jurorDetails.getNewPhoneNumber();
        this.altPhoneNumber = jurorDetails.getAltPhoneNumber();
        this.newAltPhoneNumber = jurorDetails.getNewAltPhoneNumber();
        this.dateOfBirth = jurorDetails.getDateOfBirth();
        this.newDateOfBirth = jurorDetails.getNewDateOfBirth();
        this.dateReceived = jurorDetails.getDateReceived();
        this.email = jurorDetails.getEmail();
        this.newEmail = jurorDetails.getNewEmail();
        this.thirdPartyFirstName = jurorDetails.getThirdPartyFirstName();
        this.thirdPartyLastName = jurorDetails.getThirdPartyLastName();
        this.thirdPartyReason = jurorDetails.getThirdPartyReason();
        this.thirdPartyOtherReason = jurorDetails.getThirdPartyOtherReason();
        this.thirdPartyMainPhoneNumber = jurorDetails.getThirdPartyMainPhoneNumber();
        this.thirdPartyAlternatePhoneNumber = jurorDetails.getThirdPartyAlternatePhoneNumber();
        this.thirdPartyEmailAddress = jurorDetails.getThirdPartyEmailAddress();
        this.thirdPartyRelationship = jurorDetails.getThirdPartyRelationship();
        this.residency = jurorDetails.getResidency();
        this.residencyDetail = jurorDetails.getResidencyDetail();
        this.mentalHealthAct = jurorDetails.getMentalHealthAct();
        this.mentalHealthActDetails = jurorDetails.getMentalHealthActDetails();
        this.bail = jurorDetails.getBail();
        this.bailDetails = jurorDetails.getBailDetails();
        this.convictions = jurorDetails.getConvictions();
        this.convictionsDetails = jurorDetails.getConvictionsDetails();
        this.deferral = jurorDetails.getDeferral();
        this.deferralReason = jurorDetails.getDeferralReason();
        this.deferralDate = jurorDetails.getDeferralDate();
        this.specialNeedsArrangements = jurorDetails.getReasonableAdjustmentsArrangements();
        this.excusal = jurorDetails.getExcusal();
        this.excusalReason = jurorDetails.getExcusalReason();
        this.useJurorEmailDetails = jurorDetails.getUseJurorEmailDetails();
        this.useJurorPhoneDetails = jurorDetails.getUseJurorPhoneDetails();
        try {
            this.assignedStaffMember = jurorDetails.getAssignedStaffMember() != null
                ? new StaffDto(jurorDetails.getAssignedStaffMember())
                : null;
        } catch (Exception e) {
            log.error("Error setting assigned staff member for response for juror {}",
                jurorDetails.getJurorNumber() + " -- " + e.getMessage());
        }

        this.staffAssignmentDate = jurorDetails.getStaffAssignmentDate();
        this.assignmentAllowed = jurorDetails.getAssignmentAllowed();
        this.phoneLogs = jurorDetails.getContactLogs().stream().map(ContactLogDto::new).collect(Collectors.toList());
        this.cjsEmployments =
            jurorDetails.getCjsEmployments().stream().map(CjsEmploymentDto::new).collect(Collectors.toList());
        this.specialNeeds =
            jurorDetails.getReasonableAdjustments().stream().map(ReasonableAdjustmentDto::new).collect(Collectors.toList());
        this.urgent = jurorDetails.getUrgent();
        this.slaOverdue = jurorDetails.getSlaOverdue();
        this.welsh = jurorDetails.getWelsh();
        this.currentOwner = jurorDetails.getOwner();
    }

}
