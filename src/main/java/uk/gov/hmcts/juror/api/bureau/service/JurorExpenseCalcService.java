package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorExpensesCalcResults;

public interface JurorExpenseCalcService {

    JurorExpensesCalcResults getExpensesCalcResults(JurorExpensesCalcRequestDto jurorExpensesCalcRequestDto);

}
