package uk.gov.hmcts.juror.api.moj.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.enumeration.jurorresponse.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Response DTO for Juror details on the Juror record.
 */
@SuppressWarnings("PMD.TooManyFields")
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Juror detail information for the Juror Record")
public class JurorDetailsResponseDto {

    @NotNull
    @Length(max = 35)
    @Schema(description = "Juror address line 1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressLineOne;

    @Length(max = 35)
    @Schema(description = "Juror address line 2", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressLineTwo;

    @Length(max = 35)
    @Schema(description = "Juror address line 3", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressLineThree;

    @Length(max = 35)
    @Schema(description = "Juror address line 4")
    private String addressTown;// optional field

    @Length(max = 35)
    @Schema(description = "Juror address line 5")
    private String addressCounty;// optional field

    @NotNull
    @Length(max = 8)
    @Schema(description = "Juror address post code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressPostcode;

    //  @NotNull - could be null if juror has not responded
    @Schema(description = "Juror date of birth")
    private LocalDate dateOfBirth;

    @Schema(description = "Juror primary telephone number")
    private String primaryPhone;

    @Schema(description = "Juror secondary telephone number")
    private String secondaryPhone;

    @Length(max = 254)
    @Schema(description = "Juror email address")
    private String emailAddress;

    /**
     * Permissible values are:
     * <p/>
     * null - not checked yet
     * C - checked
     * U - unchecked i.e. the check couldn't be completed
     */
    @Schema(description = "Police check flag")
    private Character phoenixChecked;

    @Schema(description = "Details of person who replies on behalf of the Juror")
    private JurorResponseDto.ThirdParty thirdParty;

    @Schema(description = "Is this response Welsh language?")
    private Boolean welsh = Boolean.FALSE;

    @Schema(description = "Common details for every Juror record")
    private JurorDetailsCommonResponseDto commonDetails;

    @Length(max = 50)
    @Schema(description = "Reply method")
    private String replyMethod;

    @Length(max = 50)
    @Schema(description = "Reply processing status")
    private String replyProcessingStatus;

    @Length(max = 1)
    @Schema(description = "Code value for Reasonable adjustments")
    private String specialNeed;

    @Length(max = 60)
    @Schema(description = "Description of Reasonable adjustments")
    private String specialNeedDescription;

    @Length(max = ValidationConstants.REASONABLE_ADJUSTMENT_MESSAGE_LENGTH_MAX)
    @Schema(description = "Reasonable adjustments message")
    private String specialNeedMessage;

    @Size(min = 8, max = 8)
    @Schema(name = "Optic Reference", description = "Eight digit Optic Reference Number for Juror")
    private String opticReference;


    /**
     * Initialise an instance of this DTO class using a JurorPool object to populate its properties.
     *
     * @param jurorPool an object representation of a JurorPool record from the database
     */
    @Autowired
    public JurorDetailsResponseDto(JurorPool jurorPool,
                                   JurorStatusRepository jurorStatusRepository,
                                   WelshCourtLocationRepository welshCourtLocationRepository,
                                   PendingJurorRepository pendingJurorRepository) {

        Juror juror = jurorPool.getJuror();

        this.commonDetails = new JurorDetailsCommonResponseDto(jurorPool, jurorStatusRepository,
            pendingJurorRepository, welshCourtLocationRepository);

        this.dateOfBirth = juror.getDateOfBirth();

        setPhoneNumbers(juror.getPhoneNumber(), juror.getAltPhoneNumber(), juror.getWorkPhone());

        this.emailAddress = juror.getEmail();

        this.addressLineOne = juror.getAddressLine1();
        this.addressLineTwo = juror.getAddressLine2();
        this.addressLineThree = juror.getAddressLine3();
        this.addressTown = juror.getAddressLine4();
        this.addressCounty = juror.getAddressLine5();
        this.addressPostcode = juror.getPostcode();

        this.phoenixChecked = PoliceCheck.isChecked(juror.getPoliceCheck());
        this.welsh = juror.getWelsh();

        if (juror.getReasonableAdjustmentCode() != null) {
            this.specialNeed = juror.getReasonableAdjustmentCode();
            Arrays.stream(ReasonableAdjustmentsEnum.values())
                .filter(sn -> sn.getCode().equalsIgnoreCase(juror.getReasonableAdjustmentCode())).findFirst()
                .ifPresent(s -> this.specialNeedDescription = s.getDescription());
        }
        this.specialNeedMessage = juror.getReasonableAdjustmentMessage();
        this.opticReference = juror.getOpticRef();
    }

    /**
     * The logic for setting the primary and secondary phone numbers is as follows:
     * <p/>
     * if we have all three numbers, mobile, home and work, use mobile as primary and home as secondary
     * if we have two numbers, mobile and either home/work, use mobile as primary and the other as secondary
     * if we have two numbers, home and work, use home as primary and work as secondary
     * otherwise we have just one number and use that as primary.
     */
    private void setPhoneNumbers(String homePhone, String mobilePhone, String workPhone) {

        if (mobilePhone != null) {
            this.primaryPhone = mobilePhone;
            if (homePhone != null) {
                this.secondaryPhone = homePhone;
            } else if (workPhone != null) {
                this.secondaryPhone = workPhone;
            }
        } else if (homePhone != null) {
            this.primaryPhone = homePhone;
            if (workPhone != null) {
                this.secondaryPhone = workPhone;
            }
        } else if (workPhone != null) {
            this.primaryPhone = workPhone;
        }

    }

}
