package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.validation.DigitalThirdPartyOtherReason;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@Entity
@SuperBuilder
@AllArgsConstructor
@Getter
@Setter
@DigitalThirdPartyOtherReason
@Table(name = "juror_response", schema = "juror_mod")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DigitalResponse extends AbstractJurorResponse {

    @Column(name = "residency")
    @NotNull
    @Builder.Default
    private Boolean residency = Boolean.TRUE;

    @Column(name = "bail")
    @NotNull
    @Builder.Default
    private Boolean bail = Boolean.FALSE;

    @Column(name = "convictions")
    @NotNull
    @Builder.Default
    private Boolean convictions = Boolean.FALSE;

    @Column(name = "mental_health_act")
    @NotNull
    @Builder.Default
    private Boolean mentalHealthAct = Boolean.FALSE;

    @Column(name = "residency_detail")
    private String residencyDetail;

    @Column(name = "mental_health_act_details")
    private String mentalHealthActDetails;

    @Column(name = "bail_details")
    private String bailDetails;

    @Column(name = "convictions_details")
    private String convictionsDetails;

    @Column(name = "deferral_reason")
    private String deferralReason;

    @Column(name = "deferral_date")
    private String deferralDate;

    @Column(name = "excusal_reason")
    private String excusalReason;

    @Column(name = "thirdparty_fname")
    private String thirdPartyFName;

    @Column(name = "thirdparty_lname")
    private String thirdPartyLName;

    @Column(name = "main_phone")
    private String mainPhone;

    @Column(name = "other_phone")
    private String otherPhone;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "thirdparty_other_reason")
    @Pattern(regexp = NO_PIPES_REGEX)
    private String thirdPartyOtherReason;

    @Column(name = "juror_phone_details")
    @Builder.Default
    private Boolean jurorPhoneDetails = Boolean.TRUE;

    @Column(name = "juror_email_details")
    @Builder.Default
    private Boolean jurorEmailDetails = Boolean.TRUE;

    /**
     * Holder for switching fields during a merge operation.
     */
    @Transient
    private String workPhone;

    public DigitalResponse() {
        super();
        super.setReplyType(new ReplyType("Digital", "Online response"));
    }
}
