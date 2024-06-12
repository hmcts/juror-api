package uk.gov.hmcts.juror.api.moj.service.expense;

import jakarta.persistence.EntityManager;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.service.UserService;
import uk.gov.hmcts.juror.api.juror.domain.ApplicationSettings;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApportionSmartCardRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.UnpaidExpenseSummaryRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseApplyToAllDays;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFoodAndDrink;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.CombinedSimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseCount;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDetailsForTotals;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarning;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApproval;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApprovalList;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.SimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.SummaryExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesDto;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PaymentData;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.TravelMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.ExpenseRatesRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PaymentDataRepository;
import uk.gov.hmcts.juror.api.moj.service.ApplicationSettingService;
import uk.gov.hmcts.juror.api.moj.service.FinancialAuditService;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.ValidationService;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CAN_NOT_APPROVE_MORE_THAN_LIMIT;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO;
import static uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils.getOrZero;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.GodClass",
    "PMD.TooManyMethods",
    "PMD.CyclomaticComplexity"
})
public class JurorExpenseServiceImpl implements JurorExpenseService {

    private final ValidationService validationService;
    private final AppearanceRepository appearanceRepository;
    private final JurorRepository jurorRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final FinancialAuditService financialAuditService;
    private final UserService userService;
    private final JurorHistoryService jurorHistoryService;
    private final PaymentDataRepository paymentDataRepository;
    private final ApplicationSettingService applicationSettingService;
    private final EntityManager entityManager;
    private final ExpenseRatesRepository expenseRatesRepository;

    Integer getJurorDefaultMileage(String jurorNumber) {
        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
        if (juror == null) {
            throw new MojException.NotFound("Juror not found: " + jurorNumber, null);
        }
        return juror.getMileage();
    }

    AppearanceStage getAppearanceStage(List<Appearance> appearances) {
        AppearanceStage firstAppearanceStage = appearances.get(0).getAppearanceStage();

        //Get all unique appearance stages
        Set<AppearanceStage> optionalAppearance = appearances.stream()
            .map(Appearance::getAppearanceStage)
            .collect(Collectors.toSet());


        //If only one stage all stages must align as such, return the first stage
        if (optionalAppearance.size() == 1) {
            return firstAppearanceStage;
        }

        //If two stages, and one stage is EXPENSE_EDITED then return edited stage else throw exception
        if (optionalAppearance.size() == 2 && optionalAppearance.contains(AppearanceStage.EXPENSE_EDITED)) {
            return AppearanceStage.EXPENSE_EDITED;
        } else {
            throw new MojException.InternalServerError(
                "Appearances must be of the same type  (Unless EXPENSE_EDITED)", null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DefaultExpenseResponseDto getDefaultExpensesForJuror(String jurorNumber) {

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, jurorNumber);

        DefaultExpenseResponseDto dto = new DefaultExpenseResponseDto();

        dto.setFinancialLoss(juror.getFinancialLoss());
        dto.setTravelTime(juror.getTravelTime());
        dto.setDistanceTraveledMiles(juror.getMileage());
        dto.setSmartCardNumber(juror.getSmartCardNumber());
        dto.setClaimingSubsistenceAllowance(juror.isClaimingSubsistenceAllowance());

        return dto;
    }


    @Override
    @Transactional
    public void applyDefaultExpenses(List<Appearance> appearances) {
        if (appearances == null || appearances.isEmpty()) {
            log.info("No appearances found with matching criteria");
            return;
        }

        appearances.forEach(appearance -> applyDefaultExpenses(appearance, getJuror(appearance.getJurorNumber())));
        List<Appearance> filteredAppearances = appearances.stream()
            .filter(appearance -> !AttendanceType.ABSENT.equals(appearance.getAttendanceType())).toList();

        saveAppearancesWithExpenseRateIdUpdate(filteredAppearances);
    }

    @Transactional
    @Override
    public void applyDefaultExpenses(Appearance appearance, Juror juror) {
        if (AttendanceType.ABSENT.equals(appearance.getAttendanceType())) {
            return;
        }

        appearance.setTravelTime(juror.getTravelTime());
        appearance.setPayAttendanceType(calculatePayAttendanceType(appearance));

        boolean isLongDay = appearance.getEffectiveTime().isAfter(LocalTime.of(10, 0, 0));
        updateMilesTraveledAndTravelDue(appearance, juror.getMileage());
        if (isAttendanceDay(appearance) && juror.isClaimingSubsistenceAllowance()) {
            updateFoodDrinkClaimType(appearance,
                isLongDay ? FoodDrinkClaimType.MORE_THAN_10_HOURS : FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS);
        } else {
            updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.NONE);
        }
        if (juror.getFinancialLoss() == null) {
            appearance.setLossOfEarningsDue(null);
        } else if (PayAttendanceType.FULL_DAY.equals(appearance.getPayAttendanceType())) {
            appearance.setLossOfEarningsDue(juror.getFinancialLoss());
        } else {
            appearance.setLossOfEarningsDue(juror.getFinancialLoss()
                .divide(new BigDecimal("2.00000"), RoundingMode.CEILING));
        }
        validateAndUpdateFinancialLossExpenseLimit(appearance);
    }

    PayAttendanceType calculatePayAttendanceType(Appearance appearance) {
        return appearance.isFullDay() ? PayAttendanceType.FULL_DAY : PayAttendanceType.HALF_DAY;
    }

    @Override
    @Transactional
    @SuppressWarnings("checkstyle:LineLength")
    public void setDefaultExpensesForJuror(String jurorNumber, RequestDefaultExpensesDto dto) {

        final String owner = SecurityUtil.getActiveOwner();

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, jurorNumber);

        juror.setMileage(dto.getDistanceTraveledMiles());
        juror.setFinancialLoss(dto.getFinancialLoss());
        juror.setSmartCardNumber(dto.getSmartCardNumber());
        juror.setTravelTime(dto.getTravelTime());
        juror.setClaimingSubsistenceAllowance(dto.isHasFoodAndDrink());

        jurorRepository.save(juror);

        if (dto.isOverwriteExistingDraftExpenses()) {
            applyDefaultExpenses(appearanceRepository
                .findAllByJurorNumberAndAppearanceStageInAndCourtLocationOwnerAndIsDraftExpenseTrueOrderByAttendanceDateDesc(
                    jurorNumber,
                    Set.of(AppearanceStage.EXPENSE_ENTERED), owner));
        }
    }

    /**
     * Submit one or more draft expense records (for a single juror) for approval. This will generate the financial
     * audit number for the batch and update the is_draft_expense flag.
     */
    @Override
    @Transactional
    public void submitDraftExpensesForApproval(String locCode, String jurorNumber, List<LocalDate> attendanceDates) {
        log.trace("Enter submitDraftExpensesForApproval");

        // query the database to retrieve appearance records for a given juror number and pool number (from the
        // request body) - then filter the result set down to just those records where the attendance date matches a
        // date in the request dto
        List<Appearance> appearances = getAppearances(locCode, jurorNumber, attendanceDates);

        if (appearances.isEmpty()) {
            throw new MojException.NotFound("No appearances found for juror: " + jurorNumber, null);
        }
        if (appearances.stream()
            .anyMatch(appearance -> !appearance.isDraftExpense()
                || !AppearanceStage.EXPENSE_ENTERED.equals(appearance.getAppearanceStage()))) {
            throw new MojException.BusinessRuleViolation(
                "All appearances must be in draft and have stage EXPENSE_ENTERED",
                MojException.BusinessRuleViolation.ErrorCode.INVALID_APPEARANCES_STATUS);
        }

        // update each expense record to assign the financial audit details object
        // and update the is_draft_expense property to false (marking the batch of expenses as ready for approval)
        for (Appearance appearance : appearances) {
            log.debug("Submitting appearance with attendance date: ${} for approval",
                appearance.getAttendanceDate().toString());
            appearance.setDraftExpense(false);
        }
        updateExpenseRatesId(appearances);
        FinancialAuditDetails financialAuditDetails =
            financialAuditService.createFinancialAuditDetail(
                jurorNumber,
                locCode,
                FinancialAuditDetails.Type.FOR_APPROVAL, appearances);

        appearances.forEach(appearance ->
            jurorHistoryService.createExpenseForApprovalHistory(financialAuditDetails,
                appearance));

        log.trace("Exit submitDraftExpensesForApproval");
    }

    Appearance getAppearance(String locCode, String jurorNumber, LocalDate attendanceDate) {
        Optional<Appearance> appearanceOptional =
            appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                locCode,
                jurorNumber,
                attendanceDate);
        if (appearanceOptional.isEmpty()) {
            throw new MojException.NotFound("No appearance record found for juror: " + jurorNumber
                + " on day: " + attendanceDate, null);
        }
        return appearanceOptional.get();
    }

    Appearance getDraftAppearance(String locCode, String jurorNumber, LocalDate attendanceDate) {
        Optional<Appearance> appearanceOptional =
            appearanceRepository
                .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDateAndIsDraftExpense(
                    locCode,
                    jurorNumber,
                    attendanceDate,
                    true);
        if (appearanceOptional.isEmpty()) {
            throw new MojException.NotFound("No draft appearance record found for juror: " + jurorNumber
                + " on day: " + attendanceDate, null);
        }
        return appearanceOptional.get();
    }


    boolean isAttendanceDay(Appearance appearance) {
        return !AttendanceType.NON_ATTENDANCE.equals(appearance.getAttendanceType())
            && !AttendanceType.NON_ATTENDANCE_LONG_TRIAL.equals(appearance.getAttendanceType());
    }

    @Transactional
    DailyExpenseResponse updateExpenseInternal(Appearance appearance,
                                               DailyExpense request) {
        DailyExpenseResponse dailyExpenseResponse = new DailyExpenseResponse();

        DailyExpenseTime time = request.getTime();
        appearance.setPayCash(PaymentMethod.CASH.equals(request.getPaymentMethod()));
        updateDraftTimeExpense(appearance, time);
        dailyExpenseResponse.setFinancialLossWarning(
            updateDraftFinancialLossExpense(appearance, request.getFinancialLoss()));

        if (isAttendanceDay(appearance)) {
            appearance.setTravelTime(time.getTravelTime());
            updateDraftTravelExpense(appearance, request.getTravel());
            updateDraftFoodAndDrinkExpense(appearance, request.getFoodAndDrink());
        }
        //If time pay attendance changes validate financial loss limit if not already validated
        if (time != null
            && time.getPayAttendance() != null
            && request.getFinancialLoss() == null) {
            validateAndUpdateFinancialLossExpenseLimit(appearance);
        }
        validateExpense(appearance);
        return dailyExpenseResponse;
    }


    @Override
    @Transactional(readOnly = true)
    public GetEnteredExpenseResponse getEnteredExpense(String locCode, String jurorNumber,
                                                       LocalDate dateOfExpense) {
        Appearance appearance = getAppearance(locCode, jurorNumber, dateOfExpense);

        return GetEnteredExpenseResponse.builder()
            .noneAttendanceDay(appearance.getNonAttendanceDay())
            .dateOfExpense(appearance.getAttendanceDate())
            .stage(appearance.getAppearanceStage())
            .totalDue(appearance.getTotalDue())
            .totalPaid(appearance.getTotalPaid())
            .paymentMethod(PaymentMethod.fromPayCash(appearance.isPayCash()))
            .time(getTimeFromAppearance(appearance))
            .financialLoss(getFinancialLossFromAppearance(appearance))
            .travel(getTravelFromAppearance(appearance))
            .foodAndDrink(getFoodAndDrinkFromAppearance(appearance))
            .build();
    }

    DailyExpenseFoodAndDrink getFoodAndDrinkFromAppearance(Appearance appearance) {
        return DailyExpenseFoodAndDrink.builder()
            .foodAndDrinkClaimType(appearance.getFoodAndDrinkClaimType())
            .smartCardAmount(appearance.getSmartCardAmountDue())
            .build();
    }

    DailyExpenseTravel getTravelFromAppearance(Appearance appearance) {
        return DailyExpenseTravel.builder()
            .traveledByCar(appearance.getTraveledByCar())
            .jurorsTakenCar(appearance.getJurorsTakenCar())
            .traveledByMotorcycle(appearance.getTraveledByMotorcycle())
            .jurorsTakenMotorcycle(appearance.getJurorsTakenMotorcycle())
            .traveledByBicycle(appearance.getTraveledByBicycle())
            .milesTraveled(appearance.getMilesTraveled())
            .parking(appearance.getParkingDue())
            .publicTransport(appearance.getPublicTransportDue())
            .taxi(appearance.getHiredVehicleDue())
            .build();
    }

    DailyExpenseFinancialLoss getFinancialLossFromAppearance(Appearance appearance) {
        return DailyExpenseFinancialLoss.builder()
            .lossOfEarningsOrBenefits(appearance.getLossOfEarningsDue())
            .extraCareCost(appearance.getChildcareDue())
            .otherCosts(appearance.getMiscAmountDue())
            .otherCostsDescription(appearance.getMiscDescription())
            .build();
    }

    GetEnteredExpenseResponse.DailyExpenseTimeEntered getTimeFromAppearance(Appearance appearance) {
        return GetEnteredExpenseResponse.DailyExpenseTimeEntered.builder()
            .timeSpentAtCourt(appearance.getTimeSpentAtCourt())
            .payAttendance(appearance.getPayAttendanceType())
            .travelTime(appearance.getTravelTime())
            .build();
    }

    @Transactional
    @Override
    public DailyExpenseResponse updateDraftExpense(String locCode, String jurorNumber, DailyExpense request) {
        if (request.getApplyToAllDays() == null || request.getApplyToAllDays().isEmpty()) {
            Appearance appearance = getDraftAppearance(locCode, jurorNumber,
                request.getDateOfExpense());
            if (!isAttendanceDay(appearance)) {
                validationService.validate(request, DailyExpense.NonAttendanceDay.class);
            } else {
                validationService.validate(request, DailyExpense.AttendanceDay.class);
            }
            return updateExpenseInternal(appearance, request);
        }

        applyToAll(appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrue(
            locCode, jurorNumber), request);
        return new DailyExpenseResponse();
    }

    @Override
    @Transactional
    public FinancialLossWarning validateAndUpdateFinancialLossExpenseLimit(Appearance appearance) {
        ExpenseRates expenseRates = getCurrentExpenseRates(false);
        PayAttendanceType attendanceType = appearance.getPayAttendanceType();
        boolean isLongTrial = Boolean.TRUE.equals(appearance.isLongTrialDay());
        BigDecimal financialLossLimit = switch (attendanceType) {
            case FULL_DAY -> isLongTrial
                ? expenseRates.getLimitFinancialLossFullDayLongTrial()
                : expenseRates.getLimitFinancialLossFullDay();
            case HALF_DAY -> isLongTrial
                ? expenseRates.getLimitFinancialLossHalfDayLongTrial()
                : expenseRates.getLimitFinancialLossHalfDay();
        };
        BigDecimal effectiveLossOfEarnings = getOrZero(appearance.getLossOfEarningsDue());
        BigDecimal effectiveExtraCareCost = getOrZero(appearance.getChildcareDue());
        BigDecimal effectiveOtherCost = getOrZero(appearance.getMiscAmountDue());
        BigDecimal total = BigDecimal.ZERO
            .add(effectiveLossOfEarnings)
            .add(effectiveExtraCareCost)
            .add(effectiveOtherCost);

        if (BigDecimalUtils.isGreaterThan(total, financialLossLimit)) {
            BigDecimal difference = total.subtract(financialLossLimit);
            if (BigDecimalUtils.isGreaterThanOrEqualTo(
                effectiveOtherCost, difference)) {
                effectiveOtherCost = effectiveOtherCost.subtract(difference);
            } else {
                difference = difference.subtract(effectiveOtherCost);
                effectiveOtherCost = BigDecimal.ZERO;

                if (BigDecimalUtils.isGreaterThanOrEqualTo(
                    effectiveExtraCareCost, difference)) {
                    effectiveExtraCareCost = effectiveExtraCareCost.subtract(difference);
                } else {
                    difference = difference.subtract(effectiveExtraCareCost);
                    effectiveExtraCareCost = BigDecimal.ZERO;
                    effectiveLossOfEarnings = effectiveLossOfEarnings.subtract(difference);
                }
            }

            appearance.setLossOfEarningsDue(effectiveLossOfEarnings);
            appearance.setChildcareDue(effectiveExtraCareCost);
            appearance.setMiscAmountDue(effectiveOtherCost);

            return new FinancialLossWarning(
                appearance.getAttendanceDate(),
                total,
                financialLossLimit,
                attendanceType,
                appearance.isLongTrialDay(),
                "The amount you entered will automatically be recalculated to limit the juror's loss to "
                    + BigDecimalUtils.currencyFormat(financialLossLimit)
            );
        }
        return null;
    }

    @Transactional
    void applyToAll(List<Appearance> appearances, DailyExpense request) {
        List<BiConsumer<Appearance, DailyExpense>> updateAppearanceConsumer = new ArrayList<>();
        AtomicBoolean hasFinancialLoss = new AtomicBoolean(false);
        request.getApplyToAllDays().forEach(dailyExpenseApplyToAllDays -> {
            if (DailyExpenseApplyToAllDays.TRAVEL_COSTS.equals(dailyExpenseApplyToAllDays)) {
                updateAppearanceConsumer.add((appearance, dailyExpense) -> {
                    if (isAttendanceDay(appearance)) {
                        updateDraftTravelExpense(appearance, request.getTravel());
                    }
                });
            }
            if (DailyExpenseApplyToAllDays.PAY_CASH.equals(dailyExpenseApplyToAllDays)) {
                appearances.forEach(appearance -> appearance.setPayCash(
                    PaymentMethod.CASH.equals(request.getPaymentMethod())));
            }

            final DailyExpenseFinancialLoss financialLoss = request.getFinancialLoss();
            if (financialLoss != null) {
                if (DailyExpenseApplyToAllDays.LOSS_OF_EARNINGS.equals(dailyExpenseApplyToAllDays)) {
                    hasFinancialLoss.set(true);
                    updateAppearanceConsumer.add((appearance, dailyExpense) ->
                        appearance.setLossOfEarningsDue(financialLoss.getLossOfEarningsOrBenefits()));
                }
                if (DailyExpenseApplyToAllDays.EXTRA_CARE_COSTS.equals(dailyExpenseApplyToAllDays)) {
                    hasFinancialLoss.set(true);
                    updateAppearanceConsumer.add((appearance, dailyExpense) -> appearance.setChildcareDue(
                        financialLoss.getExtraCareCost()));
                }
                if (DailyExpenseApplyToAllDays.OTHER_COSTS.equals(dailyExpenseApplyToAllDays)) {
                    hasFinancialLoss.set(true);
                    updateAppearanceConsumer.add((appearance, dailyExpense) -> {
                        appearance.setMiscAmountDue(financialLoss.getOtherCosts());
                        appearance.setMiscDescription(financialLoss.getOtherCostsDescription());
                    });
                }
            }
        });

        if (hasFinancialLoss.get()) {
            updateAppearanceConsumer.add(
                (appearance, dailyExpense) -> validateAndUpdateFinancialLossExpenseLimit(appearance));
        }

        updateAppearanceConsumer.add(
            (appearance, dailyExpense) -> validateExpense(appearance));

        appearances.forEach(
            appearance -> updateAppearanceConsumer.forEach(
                appearanceDailyExpenseAttendanceDayBiConsumer -> appearanceDailyExpenseAttendanceDayBiConsumer.accept(
                    appearance, request)));
        saveAppearancesWithExpenseRateIdUpdate(appearances);
    }

    void updateDraftTimeExpense(Appearance appearance, DailyExpenseTime time) {
        if (time == null) {
            return;
        }
        appearance.setPayAttendanceType(time.getPayAttendance());
    }

    void checkTotalExpensesAreNotLessThan0(Appearance appearance) {
        if (!validateTotalExpense(appearance)) {
            throw new MojException.BusinessRuleViolation(
                "Total expenses cannot be less than £0. For Day " + appearance.getAttendanceDate(),
                EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
        }
    }

    void checkDueIsMoreThanPaid(Appearance appearance) {
        Map<String, Object> errors = appearance.getExpensesWhereDueIsLessThenPaid();
        if (!errors.isEmpty()) {
            throw new MojException.BusinessRuleViolation(
                "Updated expense values cannot be less than the paid amount",
                MojException.BusinessRuleViolation.ErrorCode.EXPENSE_VALUES_REDUCED_LESS_THAN_PAID, errors);
        }
    }

    void validateExpense(Appearance appearance) {
        checkTotalExpensesAreNotLessThan0(appearance);
        checkDueIsMoreThanPaid(appearance);
    }

    boolean validateTotalExpense(Appearance appearance) {
        return BigDecimalUtils.isGreaterThanOrEqualTo(appearance.getTotalDue(), BigDecimal.ZERO);
    }

    @Transactional
    FinancialLossWarning updateDraftFinancialLossExpense(Appearance appearance,
                                                         DailyExpenseFinancialLoss financialLoss) {
        if (financialLoss == null) {
            return null;
        }
        appearance.setLossOfEarningsDue(financialLoss.getLossOfEarningsOrBenefits());
        appearance.setChildcareDue(financialLoss.getExtraCareCost());
        appearance.setMiscAmountDue(financialLoss.getOtherCosts());
        appearance.setMiscDescription(financialLoss.getOtherCostsDescription());
        return validateAndUpdateFinancialLossExpenseLimit(appearance);
    }

    void updateDraftTravelExpense(Appearance appearance, DailyExpenseTravel travel) {
        if (travel == null) {
            return;
        }

        appearance.setTraveledByCar(travel.getTraveledByCar());
        appearance.setJurorsTakenCar(travel.getJurorsTakenCar());

        appearance.setTraveledByMotorcycle(travel.getTraveledByMotorcycle());
        appearance.setJurorsTakenMotorcycle(travel.getJurorsTakenMotorcycle());

        appearance.setTraveledByBicycle(travel.getTraveledByBicycle());
        updateMilesTraveledAndTravelDue(appearance, travel.getMilesTraveled());

        appearance.setParkingDue(travel.getParking());
        appearance.setPublicTransportDue(travel.getPublicTransport());
        appearance.setHiredVehicleDue(travel.getTaxi());
    }

    //Must be called after jurors taken by X and juror traveled by X are set
    void updateMilesTraveledAndTravelDue(Appearance appearance, Integer milesTraveled) {
        appearance.setMilesTraveled(milesTraveled);
        final Integer jurorsByCar = appearance.getJurorsTakenCar();
        final Integer jurorsByMotorcycle = appearance.getJurorsTakenMotorcycle();
        final ExpenseRates expenseRates = getCurrentExpenseRates(false);

        BiFunction<TravelMethod, Boolean, BigDecimal> calculateTravelCost = (travelMethod, traveledBy) -> {
            if (traveledBy == null || !traveledBy) {
                return null;
            }
            BigDecimal travelRate = travelMethod.getRate(
                expenseRates, jurorsByCar, jurorsByMotorcycle);
            return travelRate.multiply(BigDecimal.valueOf(Optional.ofNullable(milesTraveled).orElse(0)));
        };

        appearance.setCarDue(calculateTravelCost.apply(TravelMethod.CAR, appearance.getTraveledByCar()));
        appearance.setMotorcycleDue(
            calculateTravelCost.apply(TravelMethod.MOTERCYCLE, appearance.getTraveledByMotorcycle()));
        appearance.setBicycleDue(calculateTravelCost.apply(TravelMethod.BICYCLE, appearance.getTraveledByBicycle()));
    }

    void updateFoodDrinkClaimType(Appearance appearance, FoodDrinkClaimType claimType) {
        final ExpenseRates expenseRates = getCurrentExpenseRates(false);
        BigDecimal substanceRate =
            Optional.ofNullable(claimType).orElse(FoodDrinkClaimType.NONE).getRate(expenseRates);
        appearance.setSubsistenceDue(substanceRate);
        appearance.setFoodAndDrinkClaimType(claimType);
    }

    @Override
    @Transactional(readOnly = true)
    public CombinedSimplifiedExpenseDetailDto getSimplifiedExpense(String locCode, String jurorNumber,
                                                                   ExpenseType type) {
        List<Appearance> appearances =
            appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(locCode, jurorNumber)
                .stream()
                .filter(type::isApplicable)
                .sorted(Comparator.comparing(Appearance::getAttendanceDate))
                .toList();
        if (appearances.isEmpty()) {
            return new CombinedSimplifiedExpenseDetailDto();
        }
        final String owner = SecurityUtil.getActiveOwner();
        if (appearances.stream().noneMatch(appearance -> owner.equals(appearance.getCourtLocation().getOwner()))) {
            throw new MojException.Forbidden("User cannot access this juror pool", null);
        }
        CombinedSimplifiedExpenseDetailDto combinedSimplifiedExpenseDetailDto =
            new CombinedSimplifiedExpenseDetailDto();
        appearances.forEach(
            appearance -> combinedSimplifiedExpenseDetailDto.addSimplifiedExpenseDetailDto(
                mapCombinedSimplifiedExpenseDetailDto(appearance)));
        return combinedSimplifiedExpenseDetailDto;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("LineLength")
    public CombinedExpenseDetailsDto<ExpenseDetailsDto> getExpenses(String locCode, String jurorNumber,
                                                                    List<LocalDate> dates) {
        CombinedExpenseDetailsDto<ExpenseDetailsDto> result = getExpenses(appearanceRepository
            .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateInOrderByAttendanceDate(locCode, jurorNumber,
                dates));

        if (result.getExpenseDetails().size() != dates.size()) {
            throw new MojException.NotFound("Not all dates found", null);
        }
        return result;
    }

    CombinedExpenseDetailsDto<ExpenseDetailsDto> getExpenses(List<Appearance> appearances) {
        if (appearances.isEmpty()) {
            return new CombinedExpenseDetailsDto<>();
        }
        final String owner = SecurityUtil.getActiveOwner();
        if (appearances.stream().noneMatch(appearance -> owner.equals(appearance.getCourtLocation().getOwner()))) {
            throw new MojException.Forbidden("User cannot access this juror pool", null);
        }
        CombinedExpenseDetailsDto<ExpenseDetailsDto> combinedExpenseDetailsDto = new CombinedExpenseDetailsDto<>();
        appearances.forEach(
            appearance -> combinedExpenseDetailsDto.addExpenseDetail(mapAppearanceToExpenseDetailsDto(appearance)));
        return combinedExpenseDetailsDto;
    }

    @Override
    @Transactional(readOnly = true)
    public SummaryExpenseDetailsDto calculateSummaryTotals(String locCode, String jurorNumber) {

        final String owner = SecurityUtil.getActiveOwner();

        // check if the user has access to the juror pool for juror
        JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber, owner);

        List<Appearance> appearances = appearanceRepository
            .findAllByCourtLocationLocCodeAndJurorNumber(locCode, jurorNumber);

        if (appearances.isEmpty()) {
            throw new MojException.NotFound("No appearances found for juror: " + jurorNumber, null);
        }

        SummaryExpenseDetailsDto summaryExpenseDetailsDto = new SummaryExpenseDetailsDto();

        for (Appearance appearance : appearances) {
            if (appearance.isDraftExpense()) {
                // calculate the total for draft
                if (appearance.getTotalDue() != null) {
                    summaryExpenseDetailsDto.addToTotalDraft(appearance.getTotalDue());
                }
            } else {

                // calculate the total approved
                if (appearance.getTotalPaid() != null) {
                    summaryExpenseDetailsDto.addToTotalApproved(
                        appearance.getTotalPaid());
                }

                // calculate the total for approval
                if (appearance.getTotalPaid() != null && appearance.getTotalDue() != null) {
                    summaryExpenseDetailsDto.addToTotalForApproval(
                        appearance.getTotalDue().subtract(appearance.getTotalPaid()));
                } else if (appearance.getTotalDue() != null) {
                    summaryExpenseDetailsDto.addToTotalForApproval(appearance.getTotalDue());
                }
            }
        }

        return summaryExpenseDetailsDto;
    }

    /**
     * This method should only be called if the appearance stage is EXPENSE_ENTERED or expense is draft.
     * Violating this constraint may lead to data integrity issues.
     * (Not validated here as consuming methods already perform this validation)
     */
    @Override
    @Transactional
    public void realignExpenseDetails(Appearance appearance, boolean isDeleted) {
        if (isDeleted || AttendanceType.ABSENT.equals(appearance.getAttendanceType())) {
            appearance.clearExpenses(true);
        } else if (!isAttendanceDay(appearance)) {
            appearance.clearTravelExpenses(true);
            appearance.clearFoodAndDrinkExpenses(true);
        }

        if (appearance.isDraftExpense()) {
            applyDefaultExpenses(appearance, getJuror(appearance.getJurorNumber()));
            updateExpenseRatesId(List.of(appearance));
        } else {
            if (Objects.equals(AppearanceStage.EXPENSE_ENTERED, appearance.getAppearanceStage())) {

                FinancialAuditDetails financialAuditDetails =
                    financialAuditService.createFinancialAuditDetail(appearance.getJurorNumber(),
                        appearance.getCourtLocation().getLocCode(),
                        FinancialAuditDetails.Type.FOR_APPROVAL_EDIT,
                        List.of(appearance));

                jurorHistoryService.createExpenseEditHistory(
                    financialAuditDetails,
                    appearance
                );
            }
        }
    }

    @Override
    public PaginatedList<UnpaidExpenseSummaryResponseDto> getUnpaidExpensesForCourtLocation(
        String locCode,
        UnpaidExpenseSummaryRequestDto search) {
        return appearanceRepository.findUnpaidExpenses(locCode, search);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("LineLength")
    public CombinedExpenseDetailsDto<ExpenseDetailsDto> getDraftExpenses(String locCode, String jurorNumber) {
        return getExpenses(
            appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageAndIsDraftExpenseTrueOrderByAttendanceDate(
                locCode, jurorNumber, AppearanceStage.EXPENSE_ENTERED));
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public CombinedExpenseDetailsDto<ExpenseDetailsForTotals> calculateTotals(
        String locCode, String jurorNumber, CalculateTotalExpenseRequestDto dto) {
        CombinedExpenseDetailsDto<ExpenseDetailsForTotals> responseDto = new CombinedExpenseDetailsDto<>(true);
        dto.getExpenseList().stream().map(dailyExpense -> {
            Appearance appearance =
                getAppearance(locCode, jurorNumber, dailyExpense.getDateOfExpense());
            entityManager.detach(appearance);//Detach from session to avoid updating the appearance

            if (dailyExpense.shouldPullFromDatabase()) {
                return new ExpenseDetailsForTotals(appearance);
            }
            DailyExpenseResponse dailyExpenseResponse = updateExpenseInternal(appearance, dailyExpense);
            ExpenseDetailsForTotals result = new ExpenseDetailsForTotals(appearance);
            result.setFinancialLossApportionedApplied(dailyExpenseResponse.getFinancialLossWarning() != null);
            return result;
        }).forEach(responseDto::addExpenseDetail);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseCount countExpenseTypes(String locCode, String jurorNumber) {
        List<Appearance> appearances =
            appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(locCode, jurorNumber);
        final String owner = SecurityUtil.getActiveOwner();
        if (!appearances.isEmpty() && appearances.stream()
            .noneMatch(appearance -> owner.equals(appearance.getCourtLocation().getOwner()))) {
            throw new MojException.Forbidden("User cannot access this juror pool", null);
        }
        return ExpenseCount.builder()
            .totalDraft(appearances.stream()
                .filter(ExpenseType.DRAFT::isApplicable)
                .count())
            .totalForApproval(appearances.stream()
                .filter(ExpenseType.FOR_APPROVAL::isApplicable)
                .count())
            .totalForReapproval(appearances.stream()
                .filter(ExpenseType.FOR_REAPPROVAL::isApplicable)
                .count())
            .totalApproved(appearances.stream()
                .filter(ExpenseType.APPROVED::isApplicable)
                .count())
            .build();
    }


    @Override
    @Transactional
    public void updateExpense(String locCode, String jurorNumber, ExpenseType type, List<DailyExpense> request) {
        boolean isDraft = ExpenseType.DRAFT.equals(type);
        List<Appearance> appearances = request.stream()
            .map(dailyExpense -> {
                Appearance appearance = getAppearance(locCode, jurorNumber, dailyExpense.getDateOfExpense());

                if (!type.isApplicable(appearance)) {
                    throw new MojException.BusinessRuleViolation(
                        "Expense for this day is not of type: " + type.name(),
                        MojException.BusinessRuleViolation.ErrorCode.WRONG_EXPENSE_TYPE);
                }
                if (isDraft) {
                    updateDraftExpense(locCode, jurorNumber, dailyExpense);
                } else {
                    updateExpenseInternal(appearance, dailyExpense);
                }
                if (AppearanceStage.EXPENSE_AUTHORISED.equals(appearance.getAppearanceStage())) {
                    appearance.setAppearanceStage(AppearanceStage.EXPENSE_EDITED);
                }

                return appearance;
            }).toList();

        if (isDraft) {
            return;
        }

        Appearance firstAppearance = appearances.get(0);
        FinancialAuditDetails financialAuditDetails =
            financialAuditService.createFinancialAuditDetail(jurorNumber,
                firstAppearance.getCourtLocation().getLocCode(),
                type.toEditType(),
                appearances);

        appearances.forEach(appearance -> jurorHistoryService.createExpenseEditHistory(
            financialAuditDetails,
            appearance
        ));
    }

    SimplifiedExpenseDetailDto mapCombinedSimplifiedExpenseDetailDto(Appearance appearance) {
        FinancialAuditDetails financialAuditDetails = financialAuditService.findFromAppearance(appearance);
        return SimplifiedExpenseDetailDto.builder()
            .attendanceDate(appearance.getAttendanceDate())
            .financialAuditNumber(financialAuditDetails.getFinancialAuditNumber())
            .attendanceType(appearance.getAttendanceType())
            .financialLoss(appearance.getTotalFinancialLossDue())
            .travel(appearance.getTotalTravelDue())
            .foodAndDrink(getOrZero(appearance.getSubsistenceDue()))
            .smartcard(getOrZero(appearance.getSmartCardAmountDue()))
            .totalDue(appearance.getTotalDue())
            .totalPaid(appearance.getTotalPaid())
            .balanceToPay(appearance.getBalanceToPay())
            .auditCreatedOn(financialAuditDetails.getCreatedOn())
            .build();
    }


    List<Appearance> getAppearances(String locCode, String jurorNumber, List<LocalDate> dates) {
        List<Appearance> appearances =
            appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateInOrderByAttendanceDate(
                locCode, jurorNumber, dates);
        if (appearances.isEmpty() || appearances.size() != dates.size()) {
            throw new MojException.NotFound(
                String.format("One or more appearance records not found for Loc code: %s, "
                    + "Juror Number: %s and Attendance Dates provided", locCode, jurorNumber),
                null);
        }
        return appearances;
    }

    @Override
    @Transactional
    public void apportionSmartCard(String locCode, String jurorNumber, ApportionSmartCardRequest dto) {
        List<Appearance> appearances = getAppearances(locCode, jurorNumber, dto.getDates());

        final BigDecimal amountPerAppearance = dto.getSmartCardAmount()
            .divide(BigDecimal.valueOf(appearances.size()), 2, RoundingMode.HALF_UP);
        final BigDecimal lastDayOffSet = dto.getSmartCardAmount()
            .subtract(amountPerAppearance.multiply(BigDecimal.valueOf(appearances.size() - 1)));

        final String owner = SecurityUtil.getActiveOwner();
        if (appearances.stream().noneMatch(appearance -> owner.equals(appearance.getCourtLocation().getOwner()))) {
            throw new MojException.Forbidden("User cannot access this juror pool", null);
        }
        if (appearances.stream().anyMatch(appearance -> !appearance.isDraftExpense())) {
            throw new MojException.BusinessRuleViolation("Can not apportion smart card for non-draft days",
                MojException.BusinessRuleViolation.ErrorCode.APPORTION_SMART_CARD_NON_DRAFT_DAYS);
        }

        appearances.stream().limit(appearances.size() - 1)
            .forEach(appearance -> {
                appearance.setSmartCardAmountDue(amountPerAppearance);
                validateExpense(appearance);
            });

        Appearance lastAppearance = appearances.get(appearances.size() - 1);
        lastAppearance.setSmartCardAmountDue(lastDayOffSet);
        validateExpense(lastAppearance);
        saveAppearancesWithExpenseRateIdUpdate(appearances);
    }

    @Override
    @Transactional(readOnly = true)
    public PendingApprovalList getExpensesForApproval(String locCode, PaymentMethod paymentMethod,
                                                      LocalDate fromInclusive, LocalDate toInclusive) {

        List<PendingApproval> pendingApprovals = new ArrayList<>();
        //For approval
        pendingApprovals.addAll(mapAppearancesToPendingApproval(locCode,
            appearanceRepository
                .findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
                    locCode,
                    AppearanceStage.EXPENSE_ENTERED,
                    PaymentMethod.CASH.equals(paymentMethod)),
            false,
            fromInclusive, toInclusive
        ));
        //For Re-Approval
        pendingApprovals.addAll(mapAppearancesToPendingApproval(locCode,
            appearanceRepository
                .findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
                    locCode,
                    AppearanceStage.EXPENSE_EDITED,
                    PaymentMethod.CASH.equals(paymentMethod)),
            true,
            fromInclusive, toInclusive
        ));

        PendingApprovalList pendingApprovalList = new PendingApprovalList();
        pendingApprovalList.setPendingApproval(pendingApprovals.stream()
            .sorted(Comparator.comparing(JurorNumberAndPoolNumberDto::getJurorNumber)
                .thenComparing(JurorNumberAndPoolNumberDto::getPoolNumber))
            .toList());

        pendingApprovalList.setTotalPendingBacs(appearanceRepository
            .countPendingApproval(
                locCode,
                false));

        pendingApprovalList.setTotalPendingCash(appearanceRepository
            .countPendingApproval(
                locCode,
                true));

        return pendingApprovalList;
    }

    List<PendingApproval> mapAppearancesToPendingApproval(
        String locCode, List<Appearance> appearances,
        boolean isReapproval,
        LocalDate fromInclusive, LocalDate toInclusive) {
        Map<String, List<Appearance>> approvalMapIdListMap = new HashMap<>();

        appearances.forEach(
            appearance -> approvalMapIdListMap.computeIfAbsent(appearance.getJurorNumber(), k -> new ArrayList<>())
                .add(appearance));

        return approvalMapIdListMap.values().stream()
            .filter(appearances1 -> {
                if (fromInclusive != null) {
                    return appearances1.stream()
                        .anyMatch(appearance -> appearance.getAttendanceDate().isAfter(fromInclusive)
                            || appearance.getAttendanceDate().isEqual(fromInclusive));
                }
                return true;
            })
            .filter(appearances1 -> {
                if (toInclusive != null) {
                    return appearances1.stream()
                        .anyMatch(appearance -> appearance.getAttendanceDate().isBefore(toInclusive)
                            || appearance.getAttendanceDate().isEqual(toInclusive));
                }
                return true;
            })
            .map(appearances1 -> mapAppearancesToPendingApprovalSinglePool(locCode, appearances1, isReapproval))
            .toList();
    }

    PendingApproval mapAppearancesToPendingApprovalSinglePool(
        String locCode, List<Appearance> appearances, boolean isReapproval) {
        final String jurorNumber = appearances.get(0).getJurorNumber();
        final Juror juror = getJuror(jurorNumber);

        return PendingApproval.builder()
            .jurorNumber(jurorNumber)
            .poolNumber(getActiveJurorPool(locCode, juror.getJurorNumber()).getPoolNumber())
            .firstName(juror.getFirstName())
            .lastName(juror.getLastName())
            .amountDue(appearances.stream()
                .map(Appearance::getTotalChanged)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .expenseType(isReapproval ? ExpenseType.FOR_REAPPROVAL : ExpenseType.FOR_APPROVAL)
            .canApprove(validateUserCanApprove(appearances))
            .dateToRevisions(
                appearances.stream()
                    .map(appearance -> ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(appearance.getAttendanceDate())
                        .version(appearance.getVersion())
                        .build()).toList()
            ).build();
    }

    private JurorPool getActiveJurorPool(String locCode, String jurorNumber) {
        JurorPool jurorPool =
            jurorPoolRepository.findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(locCode,
                jurorNumber);
        if (jurorPool == null) {
            throw new MojException.NotFound("No active pool found for juror: " + jurorNumber, null);
        }
        return jurorPool;
    }

    //This method is here to support unit testing
    ExpenseDetailsDto mapAppearanceToExpenseDetailsDto(Appearance appearance) {
        return new ExpenseDetailsDto(appearance);
    }


    @EqualsAndHashCode
    @RequiredArgsConstructor
    static class PendingApprovalMapId {
        private final String jurorNumber;
        private final String poolNumber;
    }

    @Override
    @Transactional
    @PreAuthorize(SecurityUtil.IS_MANAGER)
    public void approveExpenses(String locCode, PaymentMethod paymentMethod, ApproveExpenseDto dto) {
        ApproveExpenseDto.ApprovalType approvalType = dto.getApprovalType();
        List<Appearance> appearances = appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(
                locCode, dto.getJurorNumber())
            .stream()
            .filter(paymentMethod::isApplicable)
            .filter(approvalType::isApplicable).toList();

        if (appearances.isEmpty()) {
            throw new MojException.NotFound(String.format("No appearance records found for Loc code: %s, "
                    + "Juror Number: %s and approval type %s", locCode, dto.getJurorNumber(),
                dto.getApprovalType().name()), null);
        }
        if (!validateAppearanceVersionNumber(appearances, dto.getDateToRevisions())) {
            throw new MojException.BusinessRuleViolation("Revisions do not align",
                MojException.BusinessRuleViolation.ErrorCode.DATA_OUT_OF_DATE);
        }
        if (!validateUserCanApprove(appearances)) {
            throw new MojException.BusinessRuleViolation("User cannot approve an expense they have edited",
                MojException.BusinessRuleViolation.ErrorCode.CAN_NOT_APPROVE_OWN_EDIT);
        }
        BigDecimal totalToApprove = appearances.stream()
            .map(Appearance::getTotalChanged)
            .reduce(BigDecimal.ZERO,
                BigDecimal::add);
        User user = userService.findByUsername(SecurityUtil.getActiveLogin());
        BigDecimal userLimit = getOrZero(user.getApprovalLimit());
        if (BigDecimalUtils.isGreaterThan(totalToApprove, userLimit)) {
            throw new MojException.BusinessRuleViolation(
                "User cannot approve expenses over " + BigDecimalUtils.currencyFormat(userLimit),
                CAN_NOT_APPROVE_MORE_THAN_LIMIT);
        }
        Appearance firstAppearance = appearances.get(0);
        if (!PaymentMethod.CASH.equals(paymentMethod)) {
            paymentDataRepository.save(createPaymentData(dto.getJurorNumber(),
                firstAppearance.getCourtLocation(), appearances));
        }
        appearances.forEach(this::approveAppearance);
        saveAppearancesWithExpenseRateIdUpdate(appearances);


        FinancialAuditDetails financialAuditDetails =
            financialAuditService.createFinancialAuditDetail(dto.getJurorNumber(),
                firstAppearance.getCourtLocation().getLocCode(),
                dto.getApprovalType().toApproveType(PaymentMethod.CASH.equals(paymentMethod)),
                appearances);

        LocalDate latestAppearanceDate = appearances.stream()
            .map(Appearance::getAttendanceDate)
            .max(Comparator.naturalOrder())
            .get();

        if (PaymentMethod.CASH.equals(paymentMethod)) {
            jurorHistoryService.createExpenseApproveCash(
                dto.getJurorNumber(),
                financialAuditDetails,
                latestAppearanceDate,
                totalToApprove
            );
        } else {
            jurorHistoryService.createExpenseApproveBacs(
                dto.getJurorNumber(),
                financialAuditDetails,
                latestAppearanceDate,
                totalToApprove
            );
        }
    }

    void approveAppearance(Appearance appearance) {
        appearance.setAppearanceStage(AppearanceStage.EXPENSE_AUTHORISED);
        appearance.setPublicTransportPaid(appearance.getPublicTransportDue());
        appearance.setHiredVehiclePaid(appearance.getHiredVehicleDue());
        appearance.setMotorcyclePaid(appearance.getMotorcycleDue());
        appearance.setCarPaid(appearance.getCarDue());
        appearance.setBicyclePaid(appearance.getBicycleDue());
        appearance.setParkingPaid(appearance.getParkingDue());
        appearance.setSubsistencePaid(appearance.getSubsistenceDue());
        appearance.setLossOfEarningsPaid(appearance.getLossOfEarningsDue());
        appearance.setChildcarePaid(appearance.getChildcareDue());
        appearance.setMiscAmountPaid(appearance.getMiscAmountDue());
        appearance.setSmartCardAmountPaid(appearance.getSmartCardAmountDue());
    }


    PaymentData createPaymentData(String jurorNumber, CourtLocation courtLocation,
                                  List<Appearance> appearances) {
        Juror juror = getJuror(jurorNumber);

        BigDecimal travelTotal = appearances.stream()
            .map(Appearance::getTravelTotalChanged)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal substanceTotal = appearances.stream()
            .map(Appearance::getSubsistenceTotalChanged)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal financialLossTotal = appearances.stream()
            .map(Appearance::getFinancialLossTotalChanged)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal smartCard = appearances.stream()
            .map(Appearance::getSmartCardTotalChanged)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (BigDecimalUtils.isGreaterThan(smartCard, BigDecimal.ZERO)) {
            BigDecimal difference = smartCard;
            if (BigDecimalUtils.isGreaterThanOrEqualTo(substanceTotal, difference)) {
                substanceTotal = substanceTotal.subtract(difference);
            } else {
                difference = difference.subtract(substanceTotal);
                substanceTotal = BigDecimal.ZERO;

                if (BigDecimalUtils.isGreaterThanOrEqualTo(travelTotal, difference)) {
                    travelTotal = travelTotal.subtract(difference);
                } else {
                    difference = difference.subtract(travelTotal);
                    travelTotal = BigDecimal.ZERO;
                    financialLossTotal = financialLossTotal.subtract(difference);
                }
            }
        } else {
            substanceTotal = substanceTotal.add(smartCard.abs());
        }

        return PaymentData.builder()
            .courtLocation(courtLocation)
            .creationDateTime(LocalDateTime.now())
            .expenseTotal(travelTotal
                .add(substanceTotal)
                .add(financialLossTotal))
            .jurorNumber(jurorNumber)
            .bankSortCode(juror.getSortCode())
            .bankAccountName(juror.getBankAccountName())
            .bankAccountNumber(juror.getBankAccountNumber())
            .buildingSocietyNumber(juror.getBuildingSocietyRollNumber())
            .addressLine1(juror.getAddressLine1())
            .addressLine2(juror.getAddressLine2())
            .addressLine3(juror.getAddressLine3())
            .addressLine4(juror.getAddressLine4())
            .addressLine5(juror.getAddressLine5())
            .postcode(juror.getPostcode())
            .authCode(applicationSettingService.getAppSetting(ApplicationSettings.Setting.PAYMENT_AUTH_CODE)
                .orElseThrow(
                    () -> new MojException.InternalServerError(
                        "Payment Auth Code not found in application settings",
                        null))
                .getValue())
            .jurorName(juror.getName())
            .locCostCentre(courtLocation.getCostCentre())
            .travelTotal(travelTotal)
            .subsistenceTotal(substanceTotal)
            .financialLossTotal(financialLossTotal)
            .expenseFileName(null)
            .extracted(false)
            .build();
    }

    Juror getJuror(String jurorNumber) {
        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
        if (juror == null) {
            throw new MojException.NotFound("Juror not found: " + jurorNumber, null);
        }
        return juror;
    }


    boolean validateUserCanApprove(List<Appearance> appearances) {
        Set<FinancialAuditDetails> financialAuditDetailsSet = appearances.stream()
            .map(financialAuditService::getFinancialAuditDetails)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
        String username = SecurityUtil.getActiveLogin();
        return financialAuditDetailsSet.stream()
            .noneMatch(
                financialAuditDetails -> financialAuditDetails.getCreatedBy().getUsername().equals(username));
    }


    boolean validateAppearanceVersionNumber(List<Appearance> appearances,
                                            List<ApproveExpenseDto.DateToRevision> dateToRevision) {
        if (appearances.size() != dateToRevision.size()) {
            return false;
        }
        Map<LocalDate, Long> dateToRevisionMap =
            dateToRevision.stream()
                .collect(Collectors.toMap(
                    ApproveExpenseDto.DateToRevision::getAttendanceDate,
                    ApproveExpenseDto.DateToRevision::getVersion
                ));

        return appearances.stream().noneMatch(appearance -> {
            Long revisionNumber = dateToRevisionMap.get(appearance.getAttendanceDate());
            Long latestRevision = appearance.getVersion();
            return !latestRevision.equals(revisionNumber);
        });
    }

    void updateDraftFoodAndDrinkExpense(Appearance appearance, DailyExpenseFoodAndDrink foodAndDrink) {
        if (foodAndDrink == null) {
            return;
        }
        updateFoodDrinkClaimType(appearance, foodAndDrink.getFoodAndDrinkClaimType());
        appearance.setSmartCardAmountDue(foodAndDrink.getSmartCardAmount());
    }


    @Override
    @Transactional(readOnly = true)
    public boolean isLongTrialDay(String locCode, String jurorNumber, LocalDate localDate) {
        List<LocalDate> localDates = new ArrayList<>();
        localDates.add(localDate);
        localDates.addAll(
            appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(locCode, jurorNumber)
                .stream()
                .map(Appearance::getAttendanceDate)
                .distinct()
                .toList());
        return localDates.stream().distinct().sorted(Comparator.naturalOrder())
            .toList()
            .indexOf(localDate) >= 10;//>= as 0 indexed
    }


    void updateExpenseRatesId(Collection<Appearance> appearances) {
        ExpenseRates expenseRates = getCurrentExpenseRates(false);
        appearances.forEach(appearance -> appearance.setExpenseRates(expenseRates));
    }

    void saveAppearancesWithExpenseRateIdUpdate(Collection<Appearance> appearances) {
        updateExpenseRatesId(appearances);
        appearanceRepository.saveAll(appearances);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseRates getCurrentExpenseRates(boolean onlyFinancialLossLimit) {
        ExpenseRates expenseRates = expenseRatesRepository.getCurrentRates();
        if (onlyFinancialLossLimit) {
            expenseRates = ExpenseRates.builder()
                .limitFinancialLossFullDay(expenseRates.getLimitFinancialLossFullDay())
                .limitFinancialLossHalfDay(expenseRates.getLimitFinancialLossHalfDay())
                .limitFinancialLossFullDayLongTrial(expenseRates.getLimitFinancialLossFullDayLongTrial())
                .limitFinancialLossHalfDayLongTrial(expenseRates.getLimitFinancialLossHalfDayLongTrial())
                .build();
        }
        return expenseRates;
    }

    @Override
    @Transactional()
    public void updateExpenseRates(ExpenseRatesDto expenseRatesDto) {
        ExpenseRates expenseRates = expenseRatesDto.toEntity();
        expenseRatesRepository.save(expenseRates);
    }
}
