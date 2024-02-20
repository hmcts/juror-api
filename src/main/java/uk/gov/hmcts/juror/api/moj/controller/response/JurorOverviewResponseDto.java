package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.juror.api.bureau.service.ResponseExcusalService;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.SpecialNeeds;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;

import java.time.LocalDate;
import java.util.Arrays;

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

    @Schema(description = "Welsh flag")
    private Boolean welshLanguageRequired;



    /**
     * Initialise an instance of this DTO class using a JurorPool object to populate its properties.
     *
     * @param jurorPool an object representation of a JurorPool record from the database
     */
    @Autowired
    public JurorOverviewResponseDto(JurorPool jurorPool,
                                    JurorStatusRepository jurorStatusRepository,
                                    ResponseExcusalService responseExcusalService,
                                    PendingJurorRepository pendingJurorRepository) {
        this.commonDetails = new JurorDetailsCommonResponseDto(jurorPool, jurorStatusRepository,
            responseExcusalService, pendingJurorRepository);

        Juror juror = jurorPool.getJuror();
        this.opticReference = juror.getOpticRef();
        this.welshLanguageRequired = juror.getWelsh();

        if (juror.getReasonableAdjustmentCode() != null) {
            this.specialNeed = juror.getReasonableAdjustmentCode();
            this.specialNeedMessage = juror.getReasonableAdjustmentMessage();
            Arrays.stream(SpecialNeeds.values())
                .filter(sn -> sn.getCode().equalsIgnoreCase(juror.getReasonableAdjustmentCode())).findFirst()
                .ifPresent(s -> this.specialNeedDescription = s.getDescription());
        }
    }
}
