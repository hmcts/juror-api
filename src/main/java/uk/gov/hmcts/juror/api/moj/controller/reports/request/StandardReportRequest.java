package uk.gov.hmcts.juror.api.moj.controller.reports.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StandardReportRequest {

    @NotBlank(groups = AbstractReport.Validators.AbstractRequestValidator.class)
    @NotBlank
    @Schema(allowableValues = {
        //Standard
        "CurrentPoolStatusReport",
        "DeferredListByDateReport",
        "NextAttendanceDayReport",
        "NonRespondedReport",
        "PostponedListByPoolReport",
        "UndeliverableListReport",
        "PanelSummaryReport",
        "IncompleteServiceReport",
        "PanelListDetailedReport",
        "AbaccusReport",
        "JuryListReport",
        "PoolStatusReport",
        "PersonAttendingSummaryReport",
        "OnCallReport",
        "PanelMembersStatusReport",
        //Grouped
        "AbsencesReport",
        "PostponedListByDateReport",
        "UnpaidAttendanceSummaryReport",
        "ReasonableAdjustmentsReport",
        "PersonAttendingDetailReport",
        "UnpaidAttendanceReportDetailedReport",
        "JurorExpenditureReportLowLevelReport",
        "JurorExpenditureReportMidLevelReport",
        "JurorExpenditureReportHighLevelReport",
        "SummonedRespondedReport",
        "UnconfirmedAttendanceReport"
    })
    private String reportType;

    @PoolNumber(groups = AbstractReport.Validators.RequirePoolNumber.class)
    @NotNull(groups = AbstractReport.Validators.RequirePoolNumber.class)
    private String poolNumber;

    @JurorNumber(groups = AbstractReport.Validators.RequiredJurorNumber.class)
    @NotNull(groups = AbstractReport.Validators.RequiredJurorNumber.class)
    private String jurorNumber;


    @NotBlank(groups = AbstractReport.Validators.RequireTrialNumber.class)
    @Length(max = 16, groups = AbstractReport.Validators.RequireTrialNumber.class)
    private String trialNumber;

    @NotNull(groups = AbstractReport.Validators.RequireFromDate.class)
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate fromDate;

    @NotNull(groups = AbstractReport.Validators.RequireToDate.class)
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate toDate;

    @NotNull(groups = AbstractReport.Validators.RequireDate.class)
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate date;

    @NotNull(groups = AbstractReport.Validators.RequireLocCode.class)
    @CourtLocationCode(groups = AbstractReport.Validators.RequireLocCode.class)
    private String locCode;

    @NotNull(groups = AbstractReport.Validators.RequireIncludeSummoned.class)
    private Boolean includeSummoned;
}
