package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsWithOriginalDto;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsSimpleDto;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDateTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FinancialAuditReportResponse {

    private String financialAuditNumber;
    private FilterableJurorDetailsResponseDto jurorDetails;
    private FilterableJurorDetailsResponseDto originalJurorDetails;

    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    private LocalDateTime submittedAt;
    private UserDetailsSimpleDto submittedBy;

    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    private LocalDateTime approvedAt;
    private UserDetailsSimpleDto approvedBy;

    private CombinedExpenseDetailsDto<ExpenseDetailsWithOriginalDto> expenses;

}
