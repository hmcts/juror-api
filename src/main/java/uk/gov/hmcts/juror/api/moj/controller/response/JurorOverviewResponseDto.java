package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.IdCheckCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.jurorresponse.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    @Length(max = ValidationConstants.REASONABLE_ADJUSTMENT_MESSAGE_LENGTH_MAX)
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
    @JsonProperty("checked_in_today_time")
    @JsonFormat(pattern = ValidationConstants.TIME_FORMAT)
    private LocalTime checkedInTodayTime;

    @Schema(description = "Overseas flag")
    private Boolean livingOverseas;

    private String location;

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
                                    PendingJurorRepository pendingJurorRepository,
                                    WelshCourtLocationRepository welshCourtLocationRepository) {
        this.commonDetails = new JurorDetailsCommonResponseDto(jurorPool, jurorStatusRepository,
            pendingJurorRepository, welshCourtLocationRepository);


        List<Appearance> appearanceList = appearanceRepository
            .findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageIn(
                SecurityUtil.getLocCode(),
                jurorPool.getJurorNumber(),
                Set.of(AppearanceStage.EXPENSE_ENTERED,
                    AppearanceStage.EXPENSE_AUTHORISED,
                    AppearanceStage.EXPENSE_EDITED)
            );

        Optional<Appearance> todayAppearance =
            appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                jurorPool.getCourt().getLocCode(),
                jurorPool.getJurorNumber(),
                LocalDate.now());

        if (todayAppearance.isPresent() && todayAppearance.get().getTimeIn() != null) {
            this.checkedInTodayTime = todayAppearance.get().getTimeIn();
        }

        if (todayAppearance.isPresent() && todayAppearance.get().getTimeOut() != null) {
            this.location = null;
        } else {
            this.location = getLocationFromPanel(panelRepository, jurorPool, this.checkedInTodayTime != null);
        }


        this.attendances = appearanceList.stream()
            .filter(appearance -> !AttendanceType.ABSENT.equals(appearance.getAttendanceType()))
            .filter(appearance -> !AttendanceType.NON_ATTENDANCE.equals(appearance.getAttendanceType()))
            .filter(appearance -> !AttendanceType.NON_ATTENDANCE_LONG_TRIAL.equals(appearance.getAttendanceType()))
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
        this.livingOverseas = juror.getLivingOverseas();

        if (juror.getReasonableAdjustmentCode() != null) {
            this.specialNeed = juror.getReasonableAdjustmentCode();
            this.specialNeedMessage = juror.getReasonableAdjustmentMessage();
            Arrays.stream(ReasonableAdjustmentsEnum.values())
                .filter(sn -> sn.getCode().equalsIgnoreCase(juror.getReasonableAdjustmentCode())).findFirst()
                .ifPresent(s -> this.specialNeedDescription = s.getDescription());
        }
    }

    @JsonIgnore
    private String getLocationFromPanel(PanelRepository panelRepository, JurorPool jurorPool, boolean hasAppearance) {
        Optional<String> locationFromPanel = getActivePanel(panelRepository, jurorPool)
            .map(panel -> panel.getTrial().getCourtroom().getDescription());

        if (locationFromPanel.isPresent() || !hasAppearance) {
            return locationFromPanel.orElse(null);
        }
        return Optional.ofNullable(jurorPool.getCourt().getAssemblyRoom())
            .map(Courtroom::getDescription)
            .orElse(null);
    }

    private Optional<Panel> getActivePanel(PanelRepository panelRepository, JurorPool jurorPool) {
        return Optional.ofNullable(panelRepository.findActivePanelByCourtGroup(
            SecurityUtil.getCourts(),
            jurorPool.getJurorNumber()));
    }
}
