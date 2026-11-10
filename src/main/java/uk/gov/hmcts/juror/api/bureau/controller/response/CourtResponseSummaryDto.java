package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Summary returning most important fields of relative to juror response")
public class CourtResponseSummaryDto extends AbstractResponseSummaryDto {

    /**
     * Preserves the original all-arguments constructor for existing callers.
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public CourtResponseSummaryDto(
        String jurorNumber, String title, String firstName, String lastName, String postcode, String courtName,
        String courtCode, String replyMethod, String processingStatus, Boolean residency, Boolean mentalHealthAct,
        Boolean bail, Boolean convictions, String deferralDate, Boolean deferral, String excusalReason,
        Boolean excusal, String poolNumber, Boolean urgent, Boolean slaOverdue, LocalDate dateReceived,
        StaffDto assignedStaffMember, Integer version, LocalDateTime completedAt) {
        super(
            jurorNumber, title, firstName, lastName, postcode, courtName, courtCode, replyMethod,
            processingStatus, residency, mentalHealthAct, bail, convictions, deferralDate, deferral,
            excusalReason, excusal, poolNumber, urgent, slaOverdue, dateReceived, assignedStaffMember,
            version, completedAt);
    }
}
