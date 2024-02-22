package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcTravelModeData;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorExpensesCalcResults;
import uk.gov.hmcts.juror.api.bureau.domain.ExpensesRates;
import uk.gov.hmcts.juror.api.bureau.domain.ExpensesRatesRepository;
import uk.gov.hmcts.juror.api.bureau.exception.JurorExpenseCalcException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorExpenseCalcServiceImplTest {

    private JurorExpensesCalcRequestDto jurorExpensesCalcRequestDto;
    private JurorExpensesCalcResults jurorExpensesCalcResults;
    private List<ExpensesRates> expensesRates = new ArrayList<>();

    @Mock
    private ExpensesRatesRepository expensesratesRepository;

    @InjectMocks
    private JurorExpenseCalcServiceImpl jurorExpenseCalcService;

    @Before
    public void setUp() throws Exception {

        expensesRates = new ArrayList<>(Arrays.asList(
            ExpensesRates.builder().expenseType("TRAVEL_BICYCLE_PER_MILE").rate(0.096f).build(),
            ExpensesRates.builder().expenseType("TRAVEL_MOTORCYCLE_PER_MILE").rate(0.314f).build(),
            ExpensesRates.builder().expenseType("TRAVEL_CAR_PER_MILE").rate(0.314f).build(),
            ExpensesRates.builder().expenseType("SUBSISTENCE_PER_DAY").rate(5.71f).build(),
            ExpensesRates.builder().expenseType("EARNING_TEN_DAYS_FOUR_HRS_MORE").rate(64.95f).build(),
            ExpensesRates.builder().expenseType("EARNING_TEN_DAYS_FOUR_HRS_LESS").rate(32.47f).build()
        ));

    }

    @Test
    public void retrieveExpensesCalcResults_HappyPath() {
        given(expensesratesRepository.findAll()).willReturn(expensesRates);

        jurorExpensesCalcRequestDto = JurorExpensesCalcRequestDto.builder()
            .looseIncome(true).incomeExceedsThreshold(true).dailyEarnings(0f)
            .extraCosts(true).extraCostsAmount(25f).parking(true)
            .travellingModes(Arrays.asList(
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("car").dailyMiles(2.5f)
                    .dailyCost(0f).build(),
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("public")
                    .dailyCost(10f).build()
            )).build();

        jurorExpensesCalcResults = jurorExpenseCalcService.getExpensesCalcResults(jurorExpensesCalcRequestDto);
        assertThat(jurorExpensesCalcResults).isNotNull();
    }

    @Test(expected = JurorExpenseCalcException.FailedToRetrieveRateData.class)
    public void retrieveExpensesCalcResults_NoRateData() {
        given(expensesratesRepository.findAll()).willReturn(expensesRates = new ArrayList<>());

        jurorExpensesCalcRequestDto = JurorExpensesCalcRequestDto.builder()
            .looseIncome(true).incomeExceedsThreshold(true).dailyEarnings(0f)
            .extraCosts(true).extraCostsAmount(25f).parking(true)
            .travellingModes(Arrays.asList(
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("car").dailyMiles(2.5f)
                    .dailyCost(0f).build(),
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("public")
                    .dailyCost(10f).build()
            )).build();

        jurorExpensesCalcResults = jurorExpenseCalcService.getExpensesCalcResults(jurorExpensesCalcRequestDto);
    }


    @Test(expected = JurorExpenseCalcException.MissingParamsInRequest.class)
    public void retrieveExpensesCalcResults_MissingParam() {

        //missing travellingModes
        jurorExpensesCalcRequestDto = JurorExpensesCalcRequestDto.builder()
            .looseIncome(true).incomeExceedsThreshold(true).dailyEarnings(0f)
            .extraCosts(true).extraCostsAmount(25f).parking(true)
            .build();

        jurorExpensesCalcResults = jurorExpenseCalcService.getExpensesCalcResults(jurorExpensesCalcRequestDto);
    }

}