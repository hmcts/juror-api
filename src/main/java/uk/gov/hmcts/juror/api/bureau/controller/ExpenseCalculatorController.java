package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorExpensesCalcResults;
import uk.gov.hmcts.juror.api.bureau.service.JurorExpenseCalcService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/expenseCalculator", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExpenseCalculatorController {

    private final JurorExpenseCalcService jurorExpenseCalcService;

    @Autowired
    public ExpenseCalculatorController(JurorExpenseCalcService jurorExpenseCalcService) {
        Assert.notNull(jurorExpenseCalcService, " JurorExpenseCalcService cannot be null");
        this.jurorExpenseCalcService = jurorExpenseCalcService;
    }

    @PostMapping("/estimate")
    @Operation(summary = "/expenseCalculator/estimate",
        description = "Retrieve the estimated expenses for a juror.")
    public ResponseEntity<JurorExpensesCalcResults> getCalculatedExpenses(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody JurorExpensesCalcRequestDto jurorExpensesCalcRequestDto) {

        return ResponseEntity.ok().body(jurorExpenseCalcService.getExpensesCalcResults(jurorExpensesCalcRequestDto));
    }

}
