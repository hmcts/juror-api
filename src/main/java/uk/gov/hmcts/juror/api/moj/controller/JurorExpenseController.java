package uk.gov.hmcts.juror.api.moj.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.GetEnteredExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.CombinedSimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseCount;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDetailsForTotals;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApproval;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/expenses", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Expenses")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@IsCourtUser
@SuppressWarnings("PMD.ExcessiveImports")
public class JurorExpenseController {

    private final JurorExpenseService jurorExpenseService;
    private final BulkService bulkService;

    @GetMapping("/unpaid-summary/{locCode}")
    @Operation(summary = "/api/v1/moj/expenses/{locCode} - Retrieve a list of jurors with outstanding unpaid "
        + "expenses. List will always be filtered by court location (using location code) and additionally can be "
        + "filtered by supplying minimum and maximum dates to return only jurors with appearances in the provided "
        + "date range")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<UnpaidExpenseSummaryResponseDto>> getUnpaidExpensesForCourtLocation(
        @PathVariable @CourtLocationCode @Valid
        @Parameter(description = "Court Location Code", required = true) String locCode,
        @RequestParam(value = "min_date", required = false) LocalDate minDate,
        @RequestParam(value = "max_date", required = false) LocalDate maxDate,
        @RequestParam("page_number") @PathVariable("pageNumber") int pageNumber,
        @RequestParam("sort_by") @PathVariable("sortBy") String sortBy,
        @RequestParam("sort_order") @PathVariable("sortOrder") SortDirection sortOrder) {

        Page<UnpaidExpenseSummaryResponseDto> responseDto =
            jurorExpenseService.getUnpaidExpensesForCourtLocation(locCode,
                minDate, maxDate, pageNumber, sortBy, sortOrder);

        return ResponseEntity.ok().body(responseDto);
    }

    @PostMapping("/{juror_number}/edit/{type}")
    @Operation(summary = "/{juror_number}/edit/{type} - "
        + "Updates a jurors expenses for a given day.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @IsCourtUser
    public ResponseEntity<Void> postEditDailyExpense(
        @Valid @Parameter(description = "Valid expense type", required = true)
        @PathVariable("type") @NotNull ExpenseType type,
        @Parameter(description = "9-digit numeric string to identify the juror") @PathVariable(name = "juror_number")
        @JurorNumber @Valid String jurorNumber,
        @Validated(DailyExpense.EditDay.class)
        @Valid @RequestBody @NotNull List<DailyExpense> request
    ) {
        jurorExpenseService.updateExpense(jurorNumber, type, request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{juror_number}/draft/attended_day")
    @Operation(summary = "/api/v1/moj/expenses/{juror_number}/draft/attended_day - "
        + "Updates a jurors expenses for a given day.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @IsCourtUser
    public ResponseEntity<DailyExpenseResponse> postDraftAttendedDayDailyExpense(
        @Parameter(description = "9-digit numeric string to identify the juror") @PathVariable(name = "juror_number")
        @JurorNumber @Valid String jurorNumber,
        @Validated(DailyExpense.AttendanceDay.class)
        @Valid @RequestBody @NotNull DailyExpense request
    ) {
        return ResponseEntity.ok(jurorExpenseService.updateDraftExpense(jurorNumber, request));
    }

    @PostMapping("/{juror_number}/draft/non_attended_day")
    @Operation(summary = "/api/v1/moj/expenses/{juror_number}/draft/non_attended_day - "
        + "Updates a jurors expenses for a given day.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @IsCourtUser
    public ResponseEntity<DailyExpenseResponse> postDraftNonAttendedDayDailyExpense(
        @Parameter(description = "9-digit numeric string to identify the juror") @PathVariable(name = "juror_number")
        @JurorNumber @Valid String jurorNumber,
        @Validated(DailyExpense.NonAttendanceDay.class)
        @Valid @RequestBody @NotNull DailyExpense request
    ) {
        return ResponseEntity.ok(jurorExpenseService.updateDraftExpense(jurorNumber, request));
    }

    @PostMapping("/entered")
    @Operation(summary = "/api/v1/moj/expenses/entered - "
        + "Get a jurors entered expense details for a given day.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @IsCourtUser
    public ResponseEntity<GetEnteredExpenseResponse> getEnteredExpenseDetails(
        @Valid @RequestBody @NotNull GetEnteredExpenseRequest request
    ) {
        return ResponseEntity.ok(jurorExpenseService.getEnteredExpense(
            request.getJurorNumber(),
            request.getPoolNumber(),
            request.getDateOfExpense()));
    }

    @GetMapping("/default-summary/{juror_number}")
    @Operation(summary = "/api/v1/moj/expenses/default-summary - Retrieve default expenses details"
        + "and persists them to juror and appearance tables ")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DefaultExpenseResponseDto> getDefaultExpenses(
        @Valid @JurorNumber @Parameter(description = "Valid juror number", required = true)
        @PathVariable("juror_number") String jurorNumber) {
        DefaultExpenseResponseDto responseDto = jurorExpenseService.getDefaultExpensesForJuror(jurorNumber);
        return ResponseEntity.ok().body(responseDto);
    }

    @PostMapping("/set-default-expenses/{juror_number}")
    @Operation(summary = "/api/v1/moj/expenses/set-default-expenses - Update default expense details for juror and "
        + "appearance and persists them to database ")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> setDefaultExpenses(
        @Valid @JurorNumber @Parameter(description = "Valid juror number", required = true)
        @PathVariable("juror_number") String jurorNumber,
        @Valid @RequestBody RequestDefaultExpensesDto dto) {
        jurorExpenseService.setDefaultExpensesForJuror(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/submit-for-approval")
    @Operation(summary = "/api/v1/moj/expenses/submit-for-approval - "
        + "submit one or many draft expense records for approval (for a single juror)")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> submitDraftExpensesForApproval(@Valid @RequestBody ExpenseItemsDto dto) {
        jurorExpenseService.submitDraftExpensesForApproval(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/approve")
    @Operation(summary = "/api/v1/moj/expenses/approve - "
        + "Approve all expense records of a given type (for a single juror)")
    @IsCourtUser
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> approveExpenses(@Valid @RequestBody List<ApproveExpenseDto> dto) {
        bulkService.processVoid(dto, jurorExpenseService::approveExpenses);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/view/{type}")
    @Operation(summary = "/api/v1/moj/expenses/view/{type} - "
        + "Get a jurors entered simplified expense detail for a given day and type.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @IsCourtUser
    public ResponseEntity<CombinedSimplifiedExpenseDetailDto> getSimplifiedExpenseDetails(
        @Valid @Parameter(description = "Valid expense type", required = true)
        @PathVariable("type") @NotNull ExpenseType type,
        @Valid @RequestBody @NotNull JurorNumberAndPoolNumberDto request
    ) {
        return ResponseEntity.ok(jurorExpenseService.getSimplifiedExpense(request, type));
    }

    @GetMapping("/draft/{juror_number}/{pool_number}")
    @Operation(summary = "/api/v1/moj/expenses/draft/{juror_number}/{pool_number} - "
        + "Get a list of all of a jurors draft expenses")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> getDraftExpenses(
        @PathVariable("juror_number") @Valid @NotBlank @JurorNumber String jurorNumber,
        @PathVariable("pool_number") @Valid @NotBlank @PoolNumber String poolNumber
    ) {
        return ResponseEntity.ok(jurorExpenseService.getDraftExpenses(
            jurorNumber,
            poolNumber));
    }

    @GetMapping("/counts/{juror_number}/{pool_number}")
    @Operation(summary = "/api/v1/moj/expenses/count/{juror_number}/{pool_number} - "
        + "Get the count of each type of expense for a juror and pool number.")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<ExpenseCount> getCounts(
        @PathVariable("juror_number") @Valid @NotBlank @JurorNumber String jurorNumber,
        @PathVariable("pool_number") @Valid @NotBlank @PoolNumber String poolNumber
    ) {
        return ResponseEntity.ok(jurorExpenseService.countExpenseTypes(
            jurorNumber,
            poolNumber));
    }

    @PostMapping("/calculate/totals")
    @Operation(summary = "/api/v1/moj/expenses/calculate/totals - "
        + "Calculate the total expenses for a juror for the given input")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsForTotals>> calculateTotals(
        @Validated(DailyExpense.EditDay.class)
        @Valid @RequestBody CalculateTotalExpenseRequestDto dto) {
        return ResponseEntity.ok(jurorExpenseService.calculateTotals(dto));
    }

    @GetMapping("/approval/{loc_code}/{payment_method}")
    @Operation(summary = "/api/v1/moj/expenses/approval/{juror_number}/{payment_method} - "
        + "Get a list of all of a jurors expenses that are pending approval/re-approval")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH)
    public ResponseEntity<List<PendingApproval>> getExpensesForApproval(
        @P("loc_code") @PathVariable("loc_code") @Valid @NotBlank
        @Pattern(regexp = ValidationConstants.LOCATION_CODE) String locCode,
        @PathVariable("payment_method") @Valid @NotNull PaymentMethod paymentMethod,
        @JsonFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "from", required = false) LocalDate fromInclusive,
        @JsonFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "to", required = false) LocalDate toInclusive
    ) {
        return ResponseEntity.ok(jurorExpenseService.getExpensesForApproval(
            locCode,
            paymentMethod,
            fromInclusive, toInclusive));
    }
}
