package uk.gov.hmcts.juror.api.moj.service.expense;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseApplyToAllDays;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFoodAndDrink;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarning;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotals;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.TravelMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorExpenseTotalsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.service.RevisionService;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.PAGE_SIZE;
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
    private final FinancialAuditDetailsRepository financialAuditDetailsRepository;
    private final UserRepository userRepository;
    private final RevisionService revisionService;

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
    public DefaultExpenseResponseDto getDefaultExpensesForJuror(String jurorNumber) {

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, jurorNumber);

        DefaultExpenseResponseDto dto = new DefaultExpenseResponseDto();

        dto.setJurorNumber(juror.getJurorNumber());
        dto.setFinancialLoss(juror.getFinancialLoss());
        dto.setTravelTime(juror.getTravelTime());
        dto.setDistanceTraveledMiles(juror.getMileage());
        dto.setSmartCardNumber(juror.getSmartCard());
        dto.setTotalSmartCardSpend(juror.getAmountSpent());

        return dto;
    }

    @Override
    public void setDefaultExpensesForJuror(RequestDefaultExpensesDto dto) {

        String owner = SecurityUtil.getActiveOwner();

        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, dto.getJurorNumber());

        juror.setMileage(dto.getDistanceTraveledMiles());
        juror.setFinancialLoss(dto.getFinancialLoss());
        juror.setAmountSpent(dto.getTotalSmartCardSpend());
        juror.setSmartCard(dto.getSmartCardNumber());
        juror.setTravelTime(dto.getTravelTime());

        jurorRepository.save(juror);

        if (dto.isOverwriteExistingDraftExpenses()) {
            List<Appearance> appearances = appearanceRepository
                .findAllByJurorNumberAndAppearanceStageInAndCourtLocationOwnerAndIsDraftExpenseTrueOrderByAttendanceDateDesc(
                    dto.getJurorNumber(),
                    Set.of(AppearanceStage.APPEARANCE_CONFIRMED, AppearanceStage.EXPENSE_ENTERED)
                    , owner);

            if (appearances == null || appearances.isEmpty()) {
                log.info("No appearances found with matching criteria");
                return;
            }

            int numberOfDays =
                (int) (appearances.size() -
                    appearances.stream().filter(appearance -> appearance.getNonAttendanceDay()).count());

            BigDecimal smartCardAmount = dto.getTotalSmartCardSpend();

            BigDecimal amountPerDay = calculateAmountPerDay(smartCardAmount, numberOfDays);

            BigDecimal finalDayOffset =
                smartCardAmount.subtract(amountPerDay.multiply(BigDecimal.valueOf(numberOfDays)));

            Appearance lastAttendanceDay = null;

            for (Appearance appearance : appearances) {

                appearance.setTravelTime(dto.getTravelTime());

                PayAttendanceType currentPayAttendanceType = calculatePayAttendanceType(appearance.getEffectiveTime());

                appearance.setPayAttendanceType(currentPayAttendanceType);

                updateMilesTraveledAndTravelDue(appearance, dto.getDistanceTraveledMiles());

                if (!dto.isHasFoodAndDrink()) {
                    updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.NONE);
                } else {
                    if (dto.getTravelTime().isBefore(LocalTime.of(10, 0, 0))) {
                        updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.LESS_THAN_1O_HOURS);
                    } else if (dto.getTravelTime().isAfter(LocalTime.of(9, 59))) {
                        updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.MORE_THAN_10_HOURS);
                    }
                }

                if (isAttendanceDay(appearance)) {

                    appearance.setSmartCardAmountDue(amountPerDay);
                    lastAttendanceDay = appearance;

                }

                if (dto.getFinancialLoss() == null || BigDecimal.ZERO.equals(dto.getFinancialLoss())) {
                    appearance.setLossOfEarningsDue(null);
                } else if (PayAttendanceType.FULL_DAY.equals(appearance.getPayAttendanceType())) {
                    appearance.setLossOfEarningsDue(dto.getFinancialLoss());
                } else {
                    appearance.setLossOfEarningsDue(dto.getFinancialLoss().divide(
                        BigDecimal.valueOf(2)));
                }

                validateAndUpdateFinancialLossExpenseLimit(appearance);
            }

            if (lastAttendanceDay != null) {
                lastAttendanceDay.setSmartCardAmountDue(amountPerDay.add(finalDayOffset));
            }
            appearanceRepository.saveAll(appearances);
        }

    }

    BigDecimal calculateAmountPerDay(BigDecimal amount, int days) {

        if (days == 0) {
            throw new MojException.NotFound("No days found for juror at court", null);
        }

        BigDecimal result = amount.divide(BigDecimal.valueOf(days), RoundingMode.HALF_UP);
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private PayAttendanceType calculatePayAttendanceType(LocalTime totalTimeForDay) {
        PayAttendanceType payAttendanceType;

        if (totalTimeForDay.isAfter(LocalTime.of(3, 59))) {
            payAttendanceType = PayAttendanceType.FULL_DAY;
        } else {
            payAttendanceType = PayAttendanceType.HALF_DAY;
        }
        return payAttendanceType;
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
        List<Appearance> appearances = appearanceRepository.findAllByJurorNumberAndPoolNumber(dto.getJurorNumber(),
            dto.getPoolNumber()).stream().filter(appearance ->
            dto.getAttendanceDates().contains(appearance.getAttendanceDate())).toList();

        if (appearances.isEmpty()) {
            throw new MojException.NotFound(String.format("No appearance records found for Juror Number: %s, "
                + "Pool Number: %s and Attendance Dates provided", dto.getJurorNumber(), dto.getPoolNumber()), null);
        }
        Appearance firstAppearance = appearances.get(0);
        String jurorNumber = firstAppearance.getJurorNumber();
        CourtLocation courtLocation = firstAppearance.getCourtLocation();

        Revision<Long, CourtLocation> courtRevision =
            revisionService.getLatestCourtRevision(courtLocation.getLocCode());
        Revision<Long, Juror> jurorRevision =
            revisionService.getLatestJurorRevision(jurorNumber);

        // create a single financial audit details object with a new audit number generated for this batch of expenses
        FinancialAuditDetails auditDetails = FinancialAuditDetails.builder()
            .createdBy(userRepository.findByUsername(SecurityUtil.getActiveLogin()))
            .createdOn(LocalDateTime.now())
            .type(FinancialAuditDetails.Type.FOR_APPROVAL)
            .jurorRevision(jurorRevision.getRequiredRevisionNumber())
            .courtLocationRevision(courtRevision.getRequiredRevisionNumber())
            .build();

        financialAuditDetailsRepository.save(auditDetails);

        // update each expense record to assign the financial audit details object
        // and update the is_draft_expense property to false (marking the batch of expenses as ready for approval)
        for (Appearance appearance : appearances) {
            log.debug("Submitting appearance with attendance date: ${} for approval",
                appearance.getAttendanceDate().toString());
            appearance.setFinancialAuditDetails(auditDetails);
            appearance.setIsDraftExpense(false);
            appearanceRepository.save(appearance);
        }

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

    DailyExpenseResponse updateDraftExpenseInternal(Appearance appearance,
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
        checkTotalExpensesAreNotLessThan0(appearance);
        //If time pay attendance changes validate financial loss limit if not already validated
        if (time != null
            && time.getPayAttendance() != null
            && request.getFinancialLoss() == null) {
            validateAndUpdateFinancialLossExpenseLimit(appearance);
        }

        appearanceRepository.save(appearance);
        return dailyExpenseResponse;
    }

    @Override
    public GetEnteredExpenseResponse getEnteredExpense(String jurorNumber, String poolNumber, LocalDate dateOfExpense) {
        Appearance appearance = getAppearance(jurorNumber, poolNumber, dateOfExpense);

        return GetEnteredExpenseResponse.builder()
            .dateOfExpense(appearance.getAttendanceDate())
            .stage(appearance.getAppearanceStage())
            .totalDue(appearance.getTotalDue())
            .totalPaid(appearance.getTotalPaid())
            .payCash(appearance.getPayCash())
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
    public DailyExpenseResponse updateDraftExpense(String jurorNumber,
                                                   DailyExpense request) {
        Appearance appearance = getDraftAppearance(jurorNumber, request.getPoolNumber(), request.getDateOfExpense());
        DailyExpenseResponse dailyExpenseResponse = updateDraftExpenseInternal(appearance, request);

        if (request.getApplyToAllDays() != null && !request.getApplyToAllDays().isEmpty()) {
            applyToAll(appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber,
                request.getPoolNumber()), request);
        }
        return dailyExpenseResponse;
    }

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
            (appearance, dailyExpense) -> checkTotalExpensesAreNotLessThan0(appearance));

        appearances.forEach(
            appearance -> updateAppearanceConsumer.forEach(
                appearanceDailyExpenseAttendanceDayBiConsumer -> appearanceDailyExpenseAttendanceDayBiConsumer.accept(
                    appearance, request)));
        appearanceRepository.saveAll(appearances);
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

    boolean validateTotalExpense(Appearance appearance) {
        return BigDecimalUtils.isGreaterThanOrEqualTo(appearance.getTotalDue(), BigDecimal.ZERO);
    }

    FinancialLossWarning validateAndUpdateFinancialLossExpenseLimit(Appearance appearance) {
        CourtLocation courtLocation = appearance.getCourtLocation();
        PayAttendanceType attendanceType = appearance.getPayAttendanceType();
        boolean isLongTrial = Boolean.TRUE.equals(appearance.isLongTrialDay());
        BigDecimal financialLossLimit = switch (attendanceType) {
            case FULL_DAY -> isLongTrial
                ? courtLocation.getLimitFinancialLossFullDayLongTrial()
                : courtLocation.getLimitFinancialLossFullDay();
            case HALF_DAY -> isLongTrial
                ? courtLocation.getLimitFinancialLossHalfDayLongTrial()
                : courtLocation.getLimitFinancialLossHalfDay();
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

    @Override
    //Must be called after jurors taken by X and juror traveled by X are set
    public void updateMilesTraveledAndTravelDue(Appearance appearance, Integer milesTraveled) {
        appearance.setMilesTraveled(milesTraveled);
        final Integer jurorsByCar = appearance.getJurorsTakenCar();
        final Integer jurorsByMotorcycle = appearance.getJurorsTakenMotorcycle();
        final CourtLocation courtLocation = appearance.getCourtLocation();

        BiFunction<TravelMethod, Boolean, BigDecimal> calculateTravelCost = (travelMethod, traveledBy) -> {
            if (traveledBy == null || !traveledBy) {
                return null;
            }
            BigDecimal travelRate = travelMethod.getRate(
                courtLocation, jurorsByCar, jurorsByMotorcycle);
            return travelRate.multiply(BigDecimal.valueOf(Optional.ofNullable(milesTraveled).orElse(0)));
        };

        appearance.setCarDue(calculateTravelCost.apply(TravelMethod.CAR, appearance.getTraveledByCar()));
        appearance.setMotorcycleDue(
            calculateTravelCost.apply(TravelMethod.MOTERCYCLE, appearance.getTraveledByMotorcycle()));
        appearance.setBicycleDue(calculateTravelCost.apply(TravelMethod.BICYCLE, appearance.getTraveledByBicycle()));
    }

    @Override
    public void updateFoodDrinkClaimType(Appearance appearance, FoodDrinkClaimType claimType) {
        CourtLocation courtLocation = appearance.getCourtLocation();
        BigDecimal substanceRate = claimType.getRate(courtLocation);
        appearance.setSubsistenceDue(substanceRate);
        appearance.setFoodAndDrinkClaimType(claimType);
    }

    void updateDraftFoodAndDrinkExpense(Appearance appearance, DailyExpenseFoodAndDrink foodAndDrink) {
        if (foodAndDrink == null) {
            return;
        }
        updateFoodDrinkClaimType(appearance, foodAndDrink.getFoodAndDrinkClaimType());
        appearance.setSmartCardAmountDue(foodAndDrink.getSmartCardAmount());
    }


    private Double calculateTravelTime(LocalTime travelTime) {

        return travelTime.getHour() + travelTime.getMinute() / 60.0;
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
}
