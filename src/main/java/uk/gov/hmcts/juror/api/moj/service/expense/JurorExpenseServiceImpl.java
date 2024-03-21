package uk.gov.hmcts.juror.api.moj.service.expense;

import jakarta.persistence.EntityManager;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
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
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesDto;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotals;
import uk.gov.hmcts.juror.api.moj.domain.PaymentData;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
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
import uk.gov.hmcts.juror.api.moj.repository.JurorExpenseTotalsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PaymentDataRepository;
import uk.gov.hmcts.juror.api.moj.service.ApplicationSettingService;
import uk.gov.hmcts.juror.api.moj.service.FinancialAuditService;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.PAGE_SIZE;
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
    "PMD.LawOfDemeter"
})
public class JurorExpenseServiceImpl implements JurorExpenseService {

    private final JurorExpenseTotalsRepository jurorExpenseTotalsRepository;
    private final AppearanceRepository appearanceRepository;
    private final JurorRepository jurorRepository;
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
    public Page<UnpaidExpenseSummaryResponseDto> getUnpaidExpensesForCourtLocation(
        String locCode, LocalDate minDate, LocalDate maxDate,
        int pageNumber, String sortBy, SortDirection sortOrder) {

        SecurityUtil.validateCourtLocationPermitted(locCode);

        Sort sort = SortDirection.DESC.equals(sortOrder)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);

        List<JurorExpenseTotals> jurorExpenseTotalsList;

        long totalUnpaidAttendances = 0;
        if (minDate != null && maxDate != null) {
            totalUnpaidAttendances =
                jurorExpenseTotalsRepository.countUnpaidByCourtLocationCodeAndAppearanceDate(locCode, minDate, maxDate);
            jurorExpenseTotalsList = jurorExpenseTotalsRepository
                .findUnpaidByCourtLocationCodeAndAppearanceDate(locCode, minDate, maxDate, pageable);
        } else {
            totalUnpaidAttendances =
                jurorExpenseTotalsRepository.countByCourtLocationCodeAndTotalUnapprovedGreaterThan(locCode,
                    0);
            jurorExpenseTotalsList = jurorExpenseTotalsRepository.findUnpaidByCourtLocationCode(locCode, pageable);
        }

        List<UnpaidExpenseSummaryResponseDto> dtoList = new ArrayList<>();
        for (JurorExpenseTotals jurorExpenseTotals : jurorExpenseTotalsList) {
            dtoList.add(createUnpaidAttendanceSummaryItem(jurorExpenseTotals));
        }

        return new PageImpl<>(dtoList, pageable, totalUnpaidAttendances);
    }

    @Override
    @Transactional(readOnly = true)
    public DefaultExpenseResponseDto getDefaultExpensesForJuror(String jurorNumber) {

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, jurorNumber);

        DefaultExpenseResponseDto dto = new DefaultExpenseResponseDto();

        dto.setJurorNumber(juror.getJurorNumber());
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
        saveAppearancesWithExpenseRateIdUpdate(appearances);
    }

    @Transactional
    @Override
    public void applyDefaultExpenses(Appearance appearance, Juror juror) {
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
    public void setDefaultExpensesForJuror(RequestDefaultExpensesDto dto) {

        String owner = SecurityUtil.getActiveOwner();

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, dto.getJurorNumber());

        juror.setMileage(dto.getDistanceTraveledMiles());
        juror.setFinancialLoss(dto.getFinancialLoss());
        juror.setSmartCardNumber(dto.getSmartCardNumber());
        juror.setTravelTime(dto.getTravelTime());
        juror.setClaimingSubsistenceAllowance(dto.isHasFoodAndDrink());

        jurorRepository.save(juror);

        if (dto.isOverwriteExistingDraftExpenses()) {
            applyDefaultExpenses(appearanceRepository
                .findAllByJurorNumberAndAppearanceStageInAndCourtLocationOwnerAndIsDraftExpenseTrueOrderByAttendanceDateDesc(
                    dto.getJurorNumber(),
                    Set.of(AppearanceStage.EXPENSE_ENTERED), owner));
        }
    }

    /**
     * Submit one or more draft expense records (for a single juror) for approval. This will generate the financial
     * audit number for the batch and update the is_draft_expense flag.
     *
     * @param dto request body include the juror number and pool number and a list of attendance dates to identify the
     *            appearance records which need to be submitted for approval.
     */
    @Override
    @Transactional
    public void submitDraftExpensesForApproval(ExpenseItemsDto dto) {
        log.trace("Enter submitDraftExpensesForApproval");

        // query the database to retrieve appearance records for a given juror number and pool number (from the
        // request body) - then filter the result set down to just those records where the attendance date matches a
        // date in the request dto
        List<Appearance> appearances = getAppearances(dto);

        // update each expense record to assign the financial audit details object
        // and update the is_draft_expense property to false (marking the batch of expenses as ready for approval)
        for (Appearance appearance : appearances) {
            log.debug("Submitting appearance with attendance date: ${} for approval",
                appearance.getAttendanceDate().toString());
            appearance.setDraftExpense(false);
        }

        Appearance firstAppearance = appearances.get(0);
        CourtLocation courtLocation = firstAppearance.getCourtLocation();
        FinancialAuditDetails financialAuditDetails =
            financialAuditService.createFinancialAuditDetail(dto.getJurorNumber(),
                courtLocation.getLocCode(),
                FinancialAuditDetails.Type.FOR_APPROVAL, appearances);

        appearances.forEach(appearance ->
            jurorHistoryService.createExpenseForApprovalHistory(financialAuditDetails,
                appearance));

        saveAppearancesWithExpenseRateIdUpdate(appearances);
        log.trace("Exit submitDraftExpensesForApproval");
    }

    Appearance getAppearance(String jurorNumber, String poolNumber, LocalDate attendanceDate) {
        Optional<Appearance> appearanceOptional =
            appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(
                jurorNumber,
                poolNumber,
                attendanceDate);
        if (appearanceOptional.isEmpty()) {
            throw new MojException.NotFound("No appearance record found for juror: " + jurorNumber
                + " on day: " + attendanceDate, null);
        }
        return appearanceOptional.get();
    }

    Appearance getDraftAppearance(String jurorNumber, String poolNumber, LocalDate attendanceDate) {
        Optional<Appearance> appearanceOptional =
            appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDateAndIsDraftExpenseTrue(
                jurorNumber,
                poolNumber,
                attendanceDate);
        if (appearanceOptional.isEmpty()) {
            throw new MojException.NotFound("No draft appearance record found for juror: " + jurorNumber
                + " on day: " + attendanceDate, null);
        }
        return appearanceOptional.get();
    }


    boolean isAttendanceDay(Appearance appearance) {
        return !AttendanceType.NON_ATTENDANCE.equals(appearance.getAttendanceType());
    }

    @Transactional
    DailyExpenseResponse updateExpenseInternal(Appearance appearance,
                                               DailyExpense request) {
        DailyExpenseResponse dailyExpenseResponse = new DailyExpenseResponse();

        DailyExpenseTime time = request.getTime();
        appearance.setPayCash(request.getPayCash());
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
    public GetEnteredExpenseResponse getEnteredExpense(String jurorNumber, String poolNumber, LocalDate dateOfExpense) {
        Appearance appearance = getAppearance(jurorNumber, poolNumber, dateOfExpense);

        return GetEnteredExpenseResponse.builder()
            .noneAttendanceDay(appearance.getNonAttendanceDay())
            .dateOfExpense(appearance.getAttendanceDate())
            .stage(appearance.getAppearanceStage())
            .totalDue(appearance.getTotalDue())
            .totalPaid(appearance.getTotalPaid())
            .payCash(appearance.isPayCash())
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
    public DailyExpenseResponse updateDraftExpense(String jurorNumber, DailyExpense request) {
        if (request.getApplyToAllDays() == null || request.getApplyToAllDays().isEmpty()) {
            Appearance appearance = getDraftAppearance(jurorNumber, request.getPoolNumber(),
                request.getDateOfExpense());
            return updateExpenseInternal(appearance, request);
        }

        applyToAll(appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber,
            request.getPoolNumber()), request);
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
                appearances.forEach(appearance -> appearance.setPayCash(request.getPayCash()));
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
        if (!appearance.isExpenseDetailsValid()) {
            throw new MojException.BusinessRuleViolation(
                "Updated expense values cannot be less than the paid amount",
                MojException.BusinessRuleViolation.ErrorCode.EXPENSE_VALUES_REDUCED_LESS_THAN_PAID);
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
    public CombinedSimplifiedExpenseDetailDto getSimplifiedExpense(JurorNumberAndPoolNumberDto request,
                                                                   ExpenseType type) {
        List<Appearance> appearances =
            appearanceRepository.findAllByJurorNumberAndPoolNumber(request.getJurorNumber(), request.getPoolNumber())
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

    SimplifiedExpenseDetailDto mapCombinedSimplifiedExpenseDetailDto(Appearance appearance) {
        return SimplifiedExpenseDetailDto.builder()
            .attendanceDate(appearance.getAttendanceDate())
            .financialAuditNumber(appearance.getFinancialAuditDetails().getFinancialAuditNumber())
            .attendanceType(appearance.getAttendanceType())
            .financialLoss(appearance.getTotalFinancialLossDue())
            .travel(appearance.getTotalTravelDue())
            .foodAndDrink(getOrZero(appearance.getSubsistenceDue()))
            .smartcard(getOrZero(appearance.getSmartCardAmountDue()))
            .totalDue(appearance.getTotalDue())
            .totalPaid(appearance.getTotalPaid())
            .balanceToPay(appearance.getBalanceToPay())
            .auditCreatedOn(appearance.getFinancialAuditDetails().getCreatedOn())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CombinedExpenseDetailsDto<ExpenseDetailsDto> getExpenses(String jurorNumber, String poolNumber,
                                                                    List<LocalDate> dates) {
        CombinedExpenseDetailsDto<ExpenseDetailsDto> result = getExpenses(appearanceRepository
            .findAllByJurorNumberAndPoolNumberAndAttendanceDateIn(jurorNumber, poolNumber, dates));
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
    public CombinedExpenseDetailsDto<ExpenseDetailsDto> getDraftExpenses(String jurorNumber, String poolNumber) {
        return getExpenses(
            appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber)
                .stream()
                .filter(appearance -> AppearanceStage.EXPENSE_ENTERED.equals(appearance.getAppearanceStage()))
                .sorted(Comparator.comparing(Appearance::getAttendanceDate))
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public CombinedExpenseDetailsDto<ExpenseDetailsForTotals> calculateTotals(CalculateTotalExpenseRequestDto dto) {
        CombinedExpenseDetailsDto<ExpenseDetailsForTotals> responseDto = new CombinedExpenseDetailsDto<>(true);
        dto.getExpenseList().stream().map(dailyExpense -> {
            Appearance appearance =
                getAppearance(dto.getJurorNumber(), dto.getPoolNumber(), dailyExpense.getDateOfExpense());
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
    public ExpenseCount countExpenseTypes(String jurorNumber, String poolNumber) {
        List<Appearance> appearances = appearanceRepository.findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);
        final String owner = SecurityUtil.getActiveOwner();
        if (!appearances.isEmpty() && appearances.stream()
            .noneMatch(appearance -> owner.equals(appearance.getCourtLocation().getOwner()))) {
            throw new MojException.Forbidden("User cannot access this juror pool", null);
        }
        return ExpenseCount.builder()
            .totalDraft(appearances.stream().filter(appearance ->
                appearance.isDraftExpense() && AppearanceStage.EXPENSE_ENTERED.equals(
                    appearance.getAppearanceStage())).count())
            .totalForApproval(appearances.stream().filter(appearance ->
                !appearance.isDraftExpense() && AppearanceStage.EXPENSE_ENTERED.equals(
                    appearance.getAppearanceStage())).count())
            .totalForReapproval(appearances.stream().filter(appearance ->
                !appearance.isDraftExpense() && AppearanceStage.EXPENSE_EDITED.equals(
                    appearance.getAppearanceStage())).count())
            .totalApproved(appearances.stream().filter(appearance ->
                !appearance.isDraftExpense() && AppearanceStage.EXPENSE_AUTHORISED.equals(
                    appearance.getAppearanceStage())).count())
            .build();
    }

    @Override
    @Transactional
    public void updateExpense(String jurorNumber, ExpenseType type, List<DailyExpense> request) {
        List<Appearance> appearances = request.stream()
            .map(dailyExpense -> {
                Appearance appearance = getAppearance(jurorNumber, dailyExpense.getPoolNumber(),
                    dailyExpense.getDateOfExpense());

                if (!type.isApplicable(appearance)) {
                    throw new MojException.BusinessRuleViolation("Expense for this day is not of type: " + type.name(),
                        MojException.BusinessRuleViolation.ErrorCode.WRONG_EXPENSE_TYPE);
                }

                updateExpenseInternal(appearance, dailyExpense);

                if (AppearanceStage.EXPENSE_AUTHORISED.equals(appearance.getAppearanceStage())) {
                    appearance.setAppearanceStage(AppearanceStage.EXPENSE_EDITED);
                }

                return appearance;
            }).toList();


        Appearance firstAppearance = appearances.get(0);
        FinancialAuditDetails financialAuditDetails =
            financialAuditService.createFinancialAuditDetail(jurorNumber,
                firstAppearance.getCourtLocation().getLocCode(),
                FinancialAuditDetails.Type.EDIT,
                appearances);

        appearances.forEach(appearance -> jurorHistoryService.createExpenseEditHistory(
            financialAuditDetails,
            appearance
        ));
    }


    List<Appearance> getAppearances(ExpenseItemsDto dto) {
        List<Appearance> appearances = appearanceRepository.findAllByJurorNumberAndPoolNumber(dto.getJurorNumber(),
            dto.getPoolNumber()).stream().filter(appearance ->
            dto.getAttendanceDates().contains(appearance.getAttendanceDate())).toList();
        if (appearances.isEmpty() || appearances.size() != dto.getAttendanceDates().size()) {
            throw new MojException.NotFound(
                String.format("One or more appearance records not found for Juror Number: %s, "
                    + "Pool Number: %s and Attendance Dates provided", dto.getJurorNumber(), dto.getPoolNumber()),
                null);
        }
        return appearances;
    }

    @Override
    @Transactional
    public void apportionSmartCard(ApportionSmartCardRequest dto) {
        List<Appearance> appearances = getAppearances(dto);

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
        pendingApprovals.addAll(mapAppearancesToPendingApproval(
            appearanceRepository
                .findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
                    locCode,
                    AppearanceStage.EXPENSE_ENTERED,
                    PaymentMethod.CASH.equals(paymentMethod)),
            false,
            fromInclusive, toInclusive
        ));
        //For Re-Approval
        pendingApprovals.addAll(mapAppearancesToPendingApproval(
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

    List<PendingApproval> mapAppearancesToPendingApproval(List<Appearance> appearances,
                                                          boolean isReapproval,
                                                          LocalDate fromInclusive, LocalDate toInclusive) {
        Map<PendingApprovalMapId, List<Appearance>> approvalMapIdListMap = new HashMap<>();

        appearances.forEach(appearance -> {
            PendingApprovalMapId pendingApprovalMapId = new PendingApprovalMapId(appearance.getJurorNumber(),
                appearance.getPoolNumber());
            approvalMapIdListMap.computeIfAbsent(pendingApprovalMapId, k -> new ArrayList<>()).add(appearance);
        });


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
            .map(appearances1 -> mapAppearancesToPendingApprovalSinglePool(appearances1, isReapproval))
            .toList();
    }

    PendingApproval mapAppearancesToPendingApprovalSinglePool(List<Appearance> appearances,
                                                              boolean isReapproval) {
        final String jurorNumber = appearances.get(0).getJurorNumber();
        final Juror juror = getJuror(jurorNumber);

        return PendingApproval.builder()
            .jurorNumber(jurorNumber)
            .poolNumber(appearances.get(0).getPoolNumber())
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
    public void approveExpenses(ApproveExpenseDto dto) {
        ApproveExpenseDto.ApprovalType approvalType = dto.getApprovalType();
        List<Appearance> appearances = appearanceRepository.findAllByJurorNumberAndPoolNumber(dto.getJurorNumber(),
                dto.getPoolNumber()).stream()
            .filter(appearance -> appearance.getCourtLocation().getOwner().equals(SecurityUtil.getActiveOwner()))
            .filter(appearance -> dto.getCashPayment().equals(appearance.isPayCash()))
            .filter(approvalType::isApplicable).toList();

        if (appearances.isEmpty()) {
            throw new MojException.NotFound(String.format("No appearance records found for Juror Number: %s, "
                    + "Pool Number: %s and approval type %s", dto.getJurorNumber(), dto.getPoolNumber(),
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
        FinancialAuditDetails financialAuditDetails =
            financialAuditService.createFinancialAuditDetail(dto.getJurorNumber(),
                firstAppearance.getCourtLocation().getLocCode(),
                dto.getCashPayment()
                    ? FinancialAuditDetails.Type.APPROVED_CASH
                    : FinancialAuditDetails.Type.APPROVED_BACS,
                appearances);

        LocalDate latestAppearanceDate = appearances.stream()
            .map(Appearance::getAttendanceDate)
            .max(Comparator.naturalOrder())
            .get();
        if (dto.getCashPayment()) {
            jurorHistoryService.createExpenseApproveCash(
                dto.getJurorNumber(),
                dto.getPoolNumber(),
                financialAuditDetails,
                latestAppearanceDate,
                totalToApprove
            );
        } else {
            paymentDataRepository.save(createPaymentData(dto.getJurorNumber(),
                firstAppearance.getCourtLocation(), appearances));

            jurorHistoryService.createExpenseApproveBacs(
                dto.getJurorNumber(),
                dto.getPoolNumber(),
                financialAuditDetails,
                latestAppearanceDate,
                totalToApprove
            );
        }
        appearances.forEach(this::approveAppearance);
        saveAppearancesWithExpenseRateIdUpdate(appearances);
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

        return PaymentData.builder()
            .courtLocation(courtLocation)
            .creationDate(LocalDateTime.now())
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
                    () -> new MojException.InternalServerError("Payment Auth Code not found in application settings",
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
            .noneMatch(financialAuditDetails -> financialAuditDetails.getCreatedBy().getUsername().equals(username));
    }


    boolean validateAppearanceVersionNumber(List<Appearance> appearances,
                                            List<ApproveExpenseDto.DateToRevision> dateToRevision) {
        if (appearances.size() != dateToRevision.size()) {
            return false;
        }
        Map<LocalDate, Long> dateToRevisionMap =
            dateToRevision.stream().collect(Collectors.toMap(ApproveExpenseDto.DateToRevision::getAttendanceDate,
                ApproveExpenseDto.DateToRevision::getVersion));

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

    private UnpaidExpenseSummaryResponseDto createUnpaidAttendanceSummaryItem(JurorExpenseTotals jurorExpenseTotals) {
        return UnpaidExpenseSummaryResponseDto.builder()
            .jurorNumber(jurorExpenseTotals.getJurorNumber())
            .poolNumber(jurorExpenseTotals.getPoolNumber())
            .firstName(jurorExpenseTotals.getFirstName())
            .lastName(jurorExpenseTotals.getLastName())
            .totalUnapproved(jurorExpenseTotals.getTotalUnapproved())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLongTrialDay(String jurorNumber, String poolNumber, LocalDate localDate) {
        List<LocalDate> localDates = new ArrayList<>();
        localDates.add(localDate);
        localDates.addAll(
            appearanceRepository.findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber)
                .stream()
                .map(Appearance::getAttendanceDate)
                .distinct()
                .toList());
        return localDates.stream().distinct().sorted(Comparator.naturalOrder())
            .toList()
            .indexOf(localDate) >= 10;//>= as 0 indexed
    }

    void saveAppearancesWithExpenseRateIdUpdate(Collection<Appearance> appearances) {
        ExpenseRates expenseRates = getCurrentExpenseRates(false);
        appearances.forEach(appearance -> appearance.setExpenseRates(expenseRates));
        appearanceRepository.saveAll(appearances);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseRates getCurrentExpenseRates(boolean onlyFinancialLossLimit) {
        ExpenseRates expenseRates = expenseRatesRepository.getCurrentRates();
        if(onlyFinancialLossLimit){
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
        expenseRates.setRatesEffectiveFrom(LocalDate.now());
        expenseRatesRepository.save(expenseRates);
    }
}
