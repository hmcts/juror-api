package uk.gov.hmcts.juror.api.moj.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.juror.api.bureau.service.ResponseExcusalService;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;

import java.time.LocalDate;

/**
 * Response DTO for Juror Summons Reply on the Juror record.
 */

@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Juror overview information for the Juror Record")
public class JurorSummonsReplyResponseDto {

    @Length(max = 50)
    @Schema(description = "Reply status")
    private String replyStatus;

    @Schema(description = "Reply date")
    private LocalDate replyDate;

    @Length(max = 50)
    @Schema(description = "Reply method")
    private String replyMethod;

    @Schema(description = "Common details for every Juror record")
    private JurorDetailsCommonResponseDto commonDetails;

    /**
     * Initialise an instance of this DTO class using a JurorPool object to populate its properties.
     *
     * @param jurorPool an object representation of a JurorPool record from the database
     */
    @Autowired
    public JurorSummonsReplyResponseDto(JurorPool jurorPool,
                                        JurorStatusRepository jurorStatusRepository,
                                        ResponseExcusalService responseExcusalService) {
        this.commonDetails = new JurorDetailsCommonResponseDto(jurorPool, jurorStatusRepository,
            responseExcusalService);
    }
}
