package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Address;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@MappedSuperclass
@Table(name = "juror_response", schema = "juror_mod")
@Getter
@SuperBuilder
@Setter
@ToString(exclude = {"reasonableAdjustments", "cjsEmployments", "juror"})// lazy init fields
@EqualsAndHashCode(callSuper = true, exclude = {"cjsEmployments", "reasonableAdjustments", "staff", "juror"})
public class AbstractJurorResponse extends Address implements Serializable {

    @Id
    @Column(name = "juror_number")
    @Pattern(regexp = JUROR_NUMBER)
    @Length(max = 9)
    @NotNull
    private String jurorNumber;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juror_number", referencedColumnName = "juror_number", insertable = false, updatable = false,
        nullable = false)
    private Juror juror;

    @Column(name = "date_received")
    @NotNull
    private LocalDateTime dateReceived;

    @Column(name = "title")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String title;

    @Column(name = "first_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String firstName;

    @Column(name = "last_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String lastName;

    @Column(name = "processing_status")
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.NONE)
    private ProcessingStatus processingStatus = ProcessingStatus.TODO;

    @LocalDateOfBirth
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * Juror phone number.
     */
    @Column(name = "phone_number")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String phoneNumber;

    /**
     * Juror alternative number.
     */
    @Column(name = "alt_phone_number")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String altPhoneNumber;

    /**
     * Juror email address.
     */
    @Column(name = "email")
    @Length(max = 254)
    @Pattern(regexp = ValidationConstants.EMAIL_ADDRESS_REGEX)
    private String email;

    @Column(name = "residency")
    private Boolean residency;

    @Column(name = "mental_health_act")
    private Boolean mentalHealthAct;

    @Column(name = "bail")
    private Boolean bail;

    @Column(name = "convictions")
    private Boolean convictions;

    @Column(name = "thirdparty_fname")
    private String thirdPartyFName;

    @Column(name = "thirdparty_lname")
    private String thirdPartyLName;

    @Column(name = "relationship")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String relationship;

    @Column(name = "main_phone")
    private String mainPhone;

    @Column(name = "other_phone")
    private String otherPhone;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "thirdparty_reason")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 1000)
    private String thirdPartyReason;

    @Column(name = "thirdparty_other_reason")
    @Pattern(regexp = NO_PIPES_REGEX)
    private String thirdPartyOtherReason;

    @Column(name = "juror_phone_details")
    @Builder.Default
    private Boolean jurorPhoneDetails = Boolean.TRUE;

    @Column(name = "juror_email_details")
    @Builder.Default
    private Boolean jurorEmailDetails = Boolean.TRUE;



    @Column(name = "reasonable_adjustments_arrangements")
    @Length(max = ValidationConstants.REASONABLE_ADJUSTMENT_MESSAGE_LENGTH_MAX)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String reasonableAdjustmentsArrangements;

    @Column(name = "processing_complete")
    private Boolean processingComplete = Boolean.FALSE;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_login")
    private User staff;


    /**
     * Contact log for the juror of this response.
     */
    @OneToMany(mappedBy = "jurorNumber")
    private List<ContactLog> contactLog = new ArrayList<>();


    /**
     * List of {@link JurorReasonableAdjustment} entities associated with this entity.
     */
    @OneToMany(mappedBy = "jurorNumber")
    private List<JurorReasonableAdjustment> reasonableAdjustments = new ArrayList<>();

    /**
     * List of {@link JurorResponseCjsEmployment} entities associated with this entity.
     */
    @OneToMany(mappedBy = "jurorNumber")
    private List<JurorResponseCjsEmployment> cjsEmployments = new ArrayList<>();
    /**
     * Flag that this response is urgent.
     */
    @Column(name = "urgent")
    private boolean urgent;

    /**
     * Flag this response as welsh language.
     */
    @Column(name = "welsh")
    private Boolean welsh = Boolean.FALSE;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "staff_assignment_date")
    private LocalDate staffAssignmentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "reply_type")
    @Getter
    @Setter
    private ReplyType replyType;

    protected AbstractJurorResponse() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public boolean isClosed() {
        return getProcessingStatus().equals(ProcessingStatus.CLOSED);
    }

    public void setProcessingStatus(JurorResponseAuditRepositoryMod jurorResponseAuditRepository,
                                    ProcessingStatus processingStatus) {
        String username;
        if (SecurityUtil.hasBureauJwtPayload()) {
            username = SecurityUtil.getActiveLogin();
        } else {
            username = SecurityUtil.AUTO_USER;
        }
        jurorResponseAuditRepository.save(JurorResponseAuditMod.builder()
            .jurorNumber(jurorNumber)
            .login(username)
            .oldProcessingStatus(this.processingStatus)
            .newProcessingStatus(processingStatus)
            .build());
        this.processingStatus = processingStatus;
    }
}
