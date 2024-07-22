package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Response DTO for Juror common details on the Juror record.
 */
@SuppressWarnings("PMD.TooManyFields")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Juror detail common information for the Juror Record")
public class JurorDetailsCommonResponseDto {

    @Schema(name = "Owner", description = "Current owner")
    private String owner;

    @Schema(name = "LocCode", description = "Current locCode")
    @JsonProperty("loc_code")
    private String locCode;

    @Length(max = 10)
    @Schema(description = "Juror title")
    private String title;// optional field

    @NotNull
    @Length(max = 20)
    @Schema(description = "Juror first name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotNull
    @Length(max = 20)
    @Schema(description = "Juror last name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotNull
    @JsonProperty("jurorNumber")
    @Schema(name = "Juror number", description = "Jurors Number")
    private String jurorNumber;

    @NotNull
    @JsonProperty("jurorStatus")
    @Schema(name = "Juror Status", description = "Jurors status")
    private String jurorStatus;

    @NotNull
    @JsonProperty("poolNumber")
    @Schema(name = "Pool number", description = "The Pool number Juror belongs to")
    private String poolNumber;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("startDate")
    @Schema(name = "Start Date", description = "Service Start Date")
    private LocalDate startDate;

    @NotNull
    @JsonProperty("courtName")
    @Schema(name = "Court name", description = "Name of court Juror will attend")
    private String courtName;

    @JsonProperty("is_welsh_court")
    private boolean isWelshCourt;

    @JsonProperty("excusalRejected")
    @Schema(name = "Excusal Rejected flag", description = "Flag to indicate if an excusal was rejected for juror")
    private String excusalRejected;

    @JsonProperty("excusalCode")
    @Schema(name = "Excusal Code", description = "Excusal code indicating reason selected by the user")
    private String excusalCode;

    @JsonProperty("excusalDescription")
    @Schema(name = "Excusal description", description = "Description of excusal code", example = "Student")
    private String excusalDescription;

    @JsonProperty("deferredTo")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(name = "Deferred to date", description = "The date that the juror was deferred to")
    private LocalDate deferredTo;

    @JsonProperty("noDeferrals")
    @Schema(name = "Number of deferrals", description = "No. Deferrals")
    private Integer noDeferrals;

    @JsonProperty("deferralDate")
    @Schema(name = "Deferral date", description = "Deferral date")
    private LocalDate deferralDate;

    @JsonProperty("deferral_code")
    @Schema(name = "Deferral Code", description = "Code indicating deferral reason selected by the user")
    private String deferralCode;

    @JsonProperty("disqualify_code")
    @Schema(name = "Disqualification Code", description = "Code indicating disqualification reason")
    private String disqualifyCode;

    @JsonProperty("police_check")
    @Enumerated(EnumType.STRING)
    @Schema(name = "Police Check Status")
    private PoliceCheck policeCheck;

    @Length(max = 10)
    @Schema(description = "Pending Title")
    private String pendingTitle;

    @Length(max = 20)
    @Schema(description = "Pending First Name", required = true)
    private String pendingFirstName;

    @Length(max = 20)
    @Schema(description = "Pending Last Name", required = true)
    private String pendingLastName;

    @Schema(description = "check for juror manually created")
    private boolean manuallyCreated;

    @JsonProperty("response_entered")
    @Schema(name = "Response Entered", description = "Flag to indicate if a response has been entered for the juror")
    private Boolean responseEntered;

    /**
     * Initialise an instance of this DTO class using a JurorPool object to populate its properties.
     *
     * @param jurorPool an object representation of a JurorPool association record
     */
    @Autowired
    public JurorDetailsCommonResponseDto(JurorPool jurorPool,
                                         JurorStatusRepository jurorStatusRepository,
                                         PendingJurorRepository pendingJurorRepository,
                                         WelshCourtLocationRepository welshCourtLocationRepository) {
        Juror juror = jurorPool.getJuror();
        this.owner = jurorPool.getOwner();
        this.title = juror.getTitle();
        this.firstName = juror.getFirstName();
        this.lastName = juror.getLastName();
        this.jurorNumber = juror.getJurorNumber();
        this.excusalRejected = juror.getExcusalRejected();
        this.excusalCode = juror.getExcusalCode();
        this.noDeferrals = juror.getNoDefPos();
        this.poolNumber = jurorPool.getPoolNumber();
        this.startDate = jurorPool.getReturnDate();
        this.deferralDate = jurorPool.getDeferralDate();
        this.deferredTo = jurorPool.getDeferralDate() != null ? jurorPool.getDeferralDate() : null;
        this.deferralCode = jurorPool.getDeferralCode();
        this.disqualifyCode = juror.getDisqualifyCode();
        this.courtName = jurorPool.getCourt().getLocCourtName();
        this.isWelshCourt =
            welshCourtLocationRepository.existsByLocCode(jurorPool.getCourt().getLocCode());
        this.responseEntered = juror.getResponseEntered();

        if (this.excusalCode != null) {
            this.excusalDescription = ExcusalCodeEnum.fromCode(this.excusalCode).getDescription();
        }

        if (this.deferralCode != null) {
            // set the excusal description as front end needs it to display the deferral reason
            this.excusalDescription = ExcusalCodeEnum.fromCode(this.deferralCode).getDescription();
        }

        if (this.disqualifyCode != null) {
            // set the disqualification description as front end needs it to display the reason
            this.excusalDescription = DisqualifyCodeEnum.fromCode(this.disqualifyCode).getDescription();
        }

        if (jurorPool.getCourt() != null) {
            this.courtName = jurorPool.getCourt().getLocCourtName();
            this.locCode = jurorPool.getCourt().getLocCode();
        }

        Optional<JurorStatus> jurorStatusOpt = jurorStatusRepository.findById(jurorPool.getStatus().getStatus());
        jurorStatusOpt.ifPresent(status -> this.jurorStatus = status.getStatusDesc());

        this.policeCheck = juror.getPoliceCheck();
        setPendingNameChange(juror);

        this.manuallyCreated = pendingJurorRepository.findById(jurorPool.getJurorNumber()).isPresent();
    }

    private void setPendingNameChange(Juror juror) {
        if (juror != null) {
            this.pendingTitle = juror.getPendingTitle();
            this.pendingFirstName = juror.getPendingFirstName();
            this.pendingLastName = juror.getPendingLastName();
        }
    }
}
