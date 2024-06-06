package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuppressWarnings("PMD.TooManyFields")
@Schema(description = "Summary returning most important fields of relative to juror response")
public class BureauResponseSummaryDto {
    @Schema(description = "Juror number")
    private String jurorNumber;
    @Schema(description = "Juror title")
    private String title;
    @Schema(description = "Juror firstname")
    private String firstName;
    @Schema(description = "Juror lastname")
    private String lastName;
    @Schema(description = "Juror postcode")
    private String postcode;
    @Schema(description = "Name of court assigned to juror")
    private String courtName;
    @Schema(description = "court code")
    private String courtCode;
    @Schema(description = "Juror Reply method, Paper or Digital")
    private String replyMethod;
    @Schema(description = "Response processing status")
    private String processingStatus;
    @Schema(description = "Juror residency")
    private Boolean residency;
    @Schema(description = "Mental Health Act option selected")
    private Boolean mentalHealthAct;
    @Schema(description = "Bail option selected")
    private Boolean bail;
    @Schema(description = "Conviction option selected")
    private Boolean convictions;
    @Schema(description = "Reason about deferring jury duties")
    private String deferralDate;
    @Schema(description = "Details about jury duty excusal")
    private String excusalReason;
    @Schema(description = "Juror pool number")
    private String poolNumber;
    @Schema(description = "Response flagged as urgent")
    private Boolean urgent;
    @Schema(description = "SLA is expired")
    private Boolean slaOverdue;
    @Schema(description = "Response received date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReceived;
    @Schema(description = "Assigned staff member")
    private StaffDto assignedStaffMember;
    @Schema(description = "Optimistic locking version.", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer version;
    @Schema(description = "The time that processing was completed at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

}
