package uk.gov.hmcts.juror.api.moj.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApportionSmartCardRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.AttendanceDates;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.GetEnteredExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.CombinedSimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseCount;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDetailsForTotals;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApprovalList;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.SummaryExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/expenses/{loc_code}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Expenses")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@PreAuthorize("(" + SecurityUtil.LOC_CODE_AUTH + " and " + SecurityUtil.COURT_AUTH + ")")
@SuppressWarnings("PMD.ExcessiveImports")
public class JurorExpenseController {

    private final JurorExpenseService jurorExpenseService;
    private final BulkService bulkService;


    @GetMapping("/{juror_number}/{type}/view/simplified")
    @Operation(summary = "Get a jurors entered simplified expense detail for a given day and type.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<CombinedSimplifiedExpenseDetailDto> getSimplifiedExpenseDetails(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber,
        @Valid @Parameter(description = "Valid expense type", required = true)
        @PathVariable("type") @NotNull ExpenseType type
    ) {
        return ResponseEntity.ok(jurorExpenseService.getSimplifiedExpense(locCode, jurorNumber, type));
    }

    @PutMapping("/{juror_number}/{type}/edit")
    @Operation(summary = "Updates a jurors expenses for a given day.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<List<DailyExpenseResponse>> postEditDailyExpense(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable("type") @NotNull ExpenseType type,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber,
        @Valid @RequestBody @NotNull List<DailyExpense> request
    ) {
        if (ExpenseType.DRAFT.equals(type)) {
            return ResponseEntity.ok(bulkService.process(request,
                dailyExpense -> jurorExpenseService.updateDraftExpense(locCode, jurorNumber, dailyExpense)));
        } else {
            jurorExpenseService.updateExpense(locCode, jurorNumber, type, request);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{juror_number}/entered")
    @Operation(summary = "Get a jurors entered expense details for a given day.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<List<GetEnteredExpenseResponse>> getEnteredExpenseDetails(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber,
        @Valid @RequestBody @NotNull GetEnteredExpenseRequest request
    ) {
        return ResponseEntity.ok(bulkService.process(request.getExpenseDates(),
            localDate -> jurorExpenseService.getEnteredExpense(
                locCode,
                jurorNumber,
                localDate)));
    }

    @PatchMapping("/{juror_number}/smartcard")
    @Operation(summary = "Apportion a smartcard value across a number of days.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> apportionSmartCard(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber,
        @Valid @RequestBody @NotNull ApportionSmartCardRequest request
    ) {
        jurorExpenseService.apportionSmartCard(locCode, jurorNumber, request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{juror_number}/submit-for-approval")
    @Operation(summary = "submit one or many draft expense records for approval (for a single juror)")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> submitDraftExpensesForApproval(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber,
        @Valid @RequestBody AttendanceDates dto) {
        jurorExpenseService.submitDraftExpensesForApproval(locCode, jurorNumber, dto.getAttendanceDates());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/{juror_number}/DRAFT/view")
    @Operation(summary = "Get a list of all of a jurors draft expenses")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> getDraftExpenses(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber
    ) {
        return ResponseEntity.ok(jurorExpenseService.getDraftExpenses(
            locCode,
            jurorNumber));
    }

    @PostMapping("/{juror_number}/view")
    @Operation(summary = "Get a list of a jurors expenses for given dates")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> getExpenses(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber,
        @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
        @RequestBody @Valid @NotEmpty
        List<@NotNull LocalDate> dates
    ) {
        return ResponseEntity.ok(jurorExpenseService.getExpenses(
            locCode,
            jurorNumber,
            dates));
    }

    @GetMapping("/{juror_number}/counts")
    @Operation(summary = "Get the count of each type of expense for a juror and pool number.")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ExpenseCount> getCounts(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber
    ) {
        return ResponseEntity.ok(jurorExpenseService.countExpenseTypes(
            locCode,
            jurorNumber));
    }

    @PostMapping("/{juror_number}/calculate/totals")
    @Operation(summary = "Calculate the total expenses for a juror for the given input")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsForTotals>> calculateTotals(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable(name = "juror_number") @JurorNumber @Valid String jurorNumber,
        @Validated(DailyExpense.CalculateTotals.class)
        @Valid @RequestBody CalculateTotalExpenseRequestDto dto) {
        return ResponseEntity.ok(jurorExpenseService.calculateTotals(locCode, jurorNumber, dto));
    }

    @GetMapping("/{payment_method}/pending-approval")
    @Operation(summary = "Get a list of all of a jurors expenses that are pending approval/re-approval")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PendingApprovalList> getExpensesForApproval(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable("payment_method") @Valid @NotNull PaymentMethod paymentMethod,
        @JsonFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "from", required = false) LocalDate fromInclusive,
        @JsonFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "to", required = false) LocalDate toInclusive
    ) {
        return ResponseEntity.ok(jurorExpenseService.getExpensesForApproval(
            locCode,
            paymentMethod,
            fromInclusive, toInclusive));
    }

    @PostMapping("/{payment_method}/approve")
    @Operation(summary = "Approve all expense records of a given type (for a single juror)")
    @PreAuthorize("(" + SecurityUtil.LOC_CODE_AUTH
        + " and " + SecurityUtil.COURT_AUTH
        + " and " + SecurityUtil.IS_MANAGER + ")")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> approveExpenses(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @PathVariable("payment_method") @Valid @NotNull PaymentMethod paymentMethod,
        @Valid @RequestBody List<ApproveExpenseDto> dto) {
        bulkService.processVoid(dto,
            approveExpenseDto -> jurorExpenseService.approveExpenses(locCode, paymentMethod, approveExpenseDto));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{juror-number}/summary/totals")
    @Operation(summary = "Summarise the total expenses for a juror in draft, for approval and approved "
        + "in a given pool.")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SummaryExpenseDetailsDto> calculateSummaryTotals(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @P("juror-number") @PathVariable("juror-number") @Valid @NotBlank @JurorNumber String jurorNumber) {
        return ResponseEntity.ok(jurorExpenseService.calculateSummaryTotals(locCode, jurorNumber));
    }


    @GetMapping("/unpaid-summary")
    @Operation(summary = "Retrieve a list of jurors with outstanding unpaid "
        + "expenses. List will always be filtered by court location (using location code) and additionally can be "
        + "filtered by supplying minimum and maximum dates to return only jurors with appearances in the provided "
        + "date range")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<UnpaidExpenseSummaryResponseDto>> getUnpaidExpensesForCourtLocation(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
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

    @GetMapping("/{juror_number}/default-expenses")
    @Operation(summary = "Retrieve default expenses details and persists them to juror and appearance tables ")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DefaultExpenseResponseDto> getDefaultExpenses(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @Valid @JurorNumber @Parameter(description = "Valid juror number", required = true)
        @PathVariable("juror_number") String jurorNumber) {
        DefaultExpenseResponseDto responseDto = jurorExpenseService.getDefaultExpensesForJuror(jurorNumber);
        return ResponseEntity.ok().body(responseDto);
    }

    @PostMapping("/{juror_number}/default-expenses")
    @Operation(summary = "Update default expense details for juror and appearance and persists them to database ")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> setDefaultExpenses(
        @PathVariable("loc_code") @CourtLocationCode @Valid @P("loc_code") String locCode,
        @P("juror_number") @PathVariable("juror_number") @Valid @NotBlank @JurorNumber String jurorNumber,
        @Valid @RequestBody RequestDefaultExpensesDto dto) {
        jurorExpenseService.setDefaultExpensesForJuror(jurorNumber, dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
