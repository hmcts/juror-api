package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcTravelModeData;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorExpensesCalcResults;
import uk.gov.hmcts.juror.api.bureau.exception.JurorExpenseCalcException;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesPublic;
import uk.gov.hmcts.juror.api.moj.repository.ExpenseRatesPublicRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class JurorExpenseCalcServiceImpl implements JurorExpenseCalcService {

    private static final String TRAVEL_BICYCLE_PER_MILE_KEY = "TRAVEL_BICYCLE_PER_MILE";
    private static final String TRAVEL_MOTORCYCLE_PER_MILE_KEY = "TRAVEL_MOTORCYCLE_PER_MILE";
    private static final String TRAVEL_CAR_PER_MILE_KEY = "TRAVEL_CAR_PER_MILE";
    private static final String SUBSISTENCE_PER_DAY_KEY = "SUBSISTENCE_PER_DAY";
    private static final String EARNING_TEN_DAYS_FOUR_HRS_MORE_KEY = "EARNING_TEN_DAYS_FOUR_HRS_MORE";
    private static final String EARNING_TEN_DAYS_FOUR_HRS_LESS_KEY = "EARNING_TEN_DAYS_FOUR_HRS_LESS";

    private final ExpenseRatesPublicRepository expensesRatesRepository;

    @Autowired
    public JurorExpenseCalcServiceImpl(
        final ExpenseRatesPublicRepository expensesRatesRepository) {
        Assert.notNull(expensesRatesRepository, "ExpenseRatesPublicRepository cannot be null");
        this.expensesRatesRepository = expensesRatesRepository;
    }

    @Override
    public JurorExpensesCalcResults getExpensesCalcResults(JurorExpensesCalcRequestDto jurorExpensesCalcRequestDto) {
        log.debug("Called Service : JurorExpenseCalcServiceImpl.getExpensesCalcResults() ");

        if (null == jurorExpensesCalcRequestDto.getLooseIncome()
            || null == jurorExpensesCalcRequestDto.getExtraCosts()
            || null == jurorExpensesCalcRequestDto.getTravellingModes()) {
            log.error("Calculation cannot be performed. Request Params missing.");
            throw new JurorExpenseCalcException.MissingParamsInRequest();
        }

        //1. Load Expenses Rates
        List<ExpenseRatesPublic> expenseRates = Lists.newArrayList(expensesRatesRepository.findAll());

        if (expenseRates == null || expenseRates.isEmpty()) {
            log.error("Calculation cannot be performed. Missing Rates information.");
            throw new JurorExpenseCalcException.FailedToRetrieveRateData();
        }

        //      - Call method to transpose into a map. (Stream).
        final Map<String, Float> expensesRatesMap = expenseRates.stream()
            .collect(Collectors.toMap(ExpenseRatesPublic::getExpenseType, ExpenseRatesPublic::getRate));

        if (!validateRatesKeys(expensesRatesMap)) {
            log.error("Calculation cannot be performed. Missing some keys information.");
            throw new JurorExpenseCalcException.FailedToRetrieveRateData();
        }


        //2. Call method to CalculateCalcResults (Return JurorExpenseCalcResults (DTO).
        return calculateResults(jurorExpensesCalcRequestDto, expensesRatesMap);
    }

    private JurorExpensesCalcResults calculateResults(JurorExpensesCalcRequestDto jurorExpensesCalcRequestDto,
                                                      Map<String, Float> expensesRatesMap) {
        JurorExpensesCalcResults expenseResults = new JurorExpensesCalcResults();

        // Calculate Loss Of Earnings.
        expenseResults.setDailyLossOfEarningsTotal(calculateLossOfEarnings(
            jurorExpensesCalcRequestDto,
            expensesRatesMap.get(EARNING_TEN_DAYS_FOUR_HRS_MORE_KEY)
        ));

        log.debug(
            "Loss of Earnings (Total) : dailyLossOfEarningsTotal {} ",
            expenseResults.getDailyLossOfEarningsTotal()
        );

        if (expenseResults.getDailyLossOfEarningsTotal() > expensesRatesMap.get(EARNING_TEN_DAYS_FOUR_HRS_MORE_KEY)) {
            expenseResults.setDailyLossOfEarningsClaim(round(
                expensesRatesMap.get(EARNING_TEN_DAYS_FOUR_HRS_MORE_KEY),
                2
            ));
        } else {
            expenseResults.setDailyLossOfEarningsClaim(round(expenseResults.getDailyLossOfEarningsTotal(), 2));
        }

        log.debug(
            "Loss of Earnings (Claim) : dailyLossOfEarningsClaim {} ",
            expenseResults.getDailyLossOfEarningsClaim()
        );

        // Calculate Travel Costs.
        expenseResults.setTravellingModes(calculateTravelCosts(jurorExpensesCalcRequestDto, expensesRatesMap));
        expenseResults.setDailyTravelTotal(calculateTravelCostsTotal(expenseResults.getTravellingModes()));

        log.debug("Travel Costs             : dailyTravelTotal {} ", expenseResults.getDailyTravelTotal());

        // Calculate Susbsistence.
        expenseResults.setSubsistence(round(expensesRatesMap.get(SUBSISTENCE_PER_DAY_KEY), 2));

        // Provide Loss Of Earning maxmimum rate.
        expenseResults.setLossOfEarningsTenDaysFourHrsMore(expensesRatesMap.get(EARNING_TEN_DAYS_FOUR_HRS_MORE_KEY));
        expenseResults.setLossOfEarningsTenDaysFourHrsLess(expensesRatesMap.get(EARNING_TEN_DAYS_FOUR_HRS_LESS_KEY));

        // Calculate Daily Total Cost
        expenseResults.setDailyTotal(round(
            expenseResults.getDailyLossOfEarningsClaim()
                + expenseResults.getDailyTravelTotal()
                + expenseResults.getSubsistence(), 2));

        log.debug("Subsistence              : subsistence {} ", expenseResults.getSubsistence());
        log.debug("==== Total Daily         : dailyTotal {}  ", expenseResults.getDailyTotal());

        return expenseResults;
    }

    private Float calculateLossOfEarnings(JurorExpensesCalcRequestDto jurorExpensesCalcRequestDto,
                                          Float loeTenDaysFourHrsMoreRate) {

        Float dailyLossOfEarningTotal = 0f;
        if (jurorExpensesCalcRequestDto.getLooseIncome()) {
            if (jurorExpensesCalcRequestDto.getIncomeExceedsThreshold()) {
                dailyLossOfEarningTotal += loeTenDaysFourHrsMoreRate;
            } else {
                dailyLossOfEarningTotal += jurorExpensesCalcRequestDto.getDailyEarnings();
            }
        }

        if (jurorExpensesCalcRequestDto.getExtraCosts() && jurorExpensesCalcRequestDto.getExtraCostsAmount() > 0) {
            dailyLossOfEarningTotal = dailyLossOfEarningTotal + jurorExpensesCalcRequestDto.getExtraCostsAmount();
        }

        return round(dailyLossOfEarningTotal, 2);
    }

    private List<JurorExpensesCalcTravelModeData> calculateTravelCosts(
        JurorExpensesCalcRequestDto jurorExpensesCalcRequestDto,
        Map<String, Float> expensesRatesMap) {

        List<JurorExpensesCalcTravelModeData> travelModeCostList = new ArrayList<>();
        for (JurorExpensesCalcTravelModeData travelMode : jurorExpensesCalcRequestDto.getTravellingModes()) {

            switch (travelMode.getModeOfTravel()) {
                case "bicycle":
                    travelMode.setDailyCost(round(travelMode.getDailyMiles() * expensesRatesMap.get(
                        TRAVEL_BICYCLE_PER_MILE_KEY), 2));
                    travelMode.setRatePerMile(expensesRatesMap.get(TRAVEL_BICYCLE_PER_MILE_KEY));
                    break;
                case "car":
                    travelMode.setDailyCost(round(travelMode.getDailyMiles() * expensesRatesMap.get(
                        TRAVEL_CAR_PER_MILE_KEY), 2));
                    travelMode.setRatePerMile(expensesRatesMap.get(TRAVEL_CAR_PER_MILE_KEY));
                    break;
                case "motorcycle":
                    travelMode.setDailyCost(round(travelMode.getDailyMiles() * expensesRatesMap.get(
                        TRAVEL_MOTORCYCLE_PER_MILE_KEY), 2));
                    travelMode.setRatePerMile(expensesRatesMap.get(TRAVEL_MOTORCYCLE_PER_MILE_KEY));
                    break;
                case "public":
                    travelMode.setRatePerMile(0f);
                    break;
                default: //walk
                    travelMode.setDailyCost(0f);
                    travelMode.setRatePerMile(0f);
            }
            travelModeCostList.add(travelMode);
        }
        return travelModeCostList;
    }

    private Float calculateTravelCostsTotal(List<JurorExpensesCalcTravelModeData> travelModes) {

        Float travelTotal = 0f;
        for (JurorExpensesCalcTravelModeData travelMode : travelModes) {
            log.trace("     {} : dailyCost {} ", travelMode.getModeOfTravel(), travelMode.getDailyCost());
            travelTotal += travelMode.getDailyCost();
        }
        return round(travelTotal, 2);
    }


    private boolean validateRatesKeys(Map<String, Float> expenseRates) {

        return expenseRates.containsKey(EARNING_TEN_DAYS_FOUR_HRS_MORE_KEY)
            && expenseRates.containsKey(EARNING_TEN_DAYS_FOUR_HRS_LESS_KEY)
            && expenseRates.containsKey(SUBSISTENCE_PER_DAY_KEY)
            && expenseRates.containsKey(TRAVEL_CAR_PER_MILE_KEY)
            && expenseRates.containsKey(TRAVEL_BICYCLE_PER_MILE_KEY)
            && expenseRates.containsKey(TRAVEL_MOTORCYCLE_PER_MILE_KEY);
    }


    private static float round(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        DecimalFormat df = new DecimalFormat("##0.00");
        BigDecimal bd = new BigDecimal(df.format(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}
