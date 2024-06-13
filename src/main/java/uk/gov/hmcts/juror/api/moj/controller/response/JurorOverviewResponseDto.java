package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.IdCheckCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.jurorresponse.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Response DTO for Juror overview on the Juror record.
 */

@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Juror overview information for the Juror Record")
public class JurorOverviewResponseDto {

    @Length(max = 50)
    @Schema(description = "Reply status")
    private String replyStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Reply date")
    private LocalDate replyDate;

    @Length(max = 50)
    @Schema(description = "Reply method")
    private String replyMethod;
    @Length(max = 1)
    @Schema(description = "Code value for Reasonable adjustments")
    private String specialNeed;

    @Length(max = 60)
    @Schema(description = "Description of Reasonable adjustments")
    private String specialNeedDescription;

    @Length(max = 60)
    @Schema(description = "Reasonable adjustments message")
    private String specialNeedMessage;

    @Size(min = 8, max = 8)
    @Schema(name = "Optic Reference", description = "Eight digit Optic Reference Number for Juror")
    private String opticReference;

    @Schema(description = "Common details for every Juror record")
    private JurorDetailsCommonResponseDto commonDetails;

    private long absences;
    private long trials;
    private long attendances;

    @Schema(description = "Welsh flag")
    private Boolean welshLanguageRequired;

    @Schema(description = "Identity check code")
    private IdCheckCodeEnum idCheckCode;


    /**
     * Initialise an instance of this DTO class using a JurorPool object to populate its properties.
     *
     * @param jurorPool an object representation of a JurorPool record from the database
     */
    @Autowired
    public JurorOverviewResponseDto(JurorPool jurorPool,
                                    JurorStatusRepository jurorStatusRepository,
                                    PanelRepository panelRepository,
                                    AppearanceRepository appearanceRepository,
                                    PendingJurorRepository pendingJurorRepository) {
        this.commonDetails = new JurorDetailsCommonResponseDto(jurorPool, jurorStatusRepository,
            pendingJurorRepository);

        List<Appearance> appearanceList = appearanceRepository
            .findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageIn(
                SecurityUtil.getLocCode(),
                jurorPool.getJurorNumber(),
                Set.of(AppearanceStage.EXPENSE_ENTERED,
                    AppearanceStage.EXPENSE_AUTHORISED,
                    AppearanceStage.EXPENSE_EDITED)
            );
        this.attendances = appearanceList.stream()
            .filter(appearance -> !AttendanceType.ABSENT.equals(appearance.getAttendanceType()))
            .count();
        this.absences = appearanceList.stream()
            .filter(appearance -> AttendanceType.ABSENT.equals(appearance.getAttendanceType()))
            .count();
        this.trials = panelRepository.countByJurorJurorNumberAndTrialCourtLocationLocCode(
            jurorPool.getJurorNumber(),
            SecurityUtil.getLocCode());
        Juror juror = jurorPool.getJuror();

        this.opticReference = juror.getOpticRef();
        this.welshLanguageRequired = juror.getWelsh();
        this.idCheckCode = IdCheckCodeEnum.getIdCheckCodeEnum(jurorPool.getIdChecked());

        if (juror.getReasonableAdjustmentCode() != null) {
            this.specialNeed = juror.getReasonableAdjustmentCode();
            this.specialNeedMessage = juror.getReasonableAdjustmentMessage();
            Arrays.stream(ReasonableAdjustmentsEnum.values())
                .filter(sn -> sn.getCode().equalsIgnoreCase(juror.getReasonableAdjustmentCode())).findFirst()
                .ifPresent(s -> this.specialNeedDescription = s.getDescription());
        }
    }
}
