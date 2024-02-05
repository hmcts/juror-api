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
import uk.gov.hmcts.juror.api.moj.controller.request.DefaultExpenseSummaryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseApplyToAllDays;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFoodAndDrink;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseEntryDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarning;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
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
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    Integer getJurorDefaultMileage(String jurorNumber) {
        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
        if (juror == null) {
            throw new MojException.NotFound("Juror not found: " + jurorNumber, null);
        }
        return juror.getMileage();
    }

    @Override
    @Transactional(readOnly = true)
    public BulkExpenseDto getBulkDraftExpense(String jurorNumber, String poolNumber) {
        return getBulkExpense(jurorNumber, appearanceRepository
            .findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber)
            .stream().filter(Appearance::getIsDraftExpense).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BulkExpenseDto getBulkExpense(String jurorNumber, long financialAuditNumber) {
        return getBulkExpense(jurorNumber, appearanceRepository
            .findAllByJurorNumberAndFinancialAuditDetailsId(jurorNumber, financialAuditNumber)
            .stream().filter(appearance -> !appearance.getIsDraftExpense()).toList()
        );
    }

    BulkExpenseDto getBulkExpense(String jurorNumber, List<Appearance> appearances) {
        if (appearances.isEmpty()) {
            throw new MojException.NotFound("No appearances found", null);
        }

        AppearanceStage stage = getAppearanceStage(appearances);
        if (!Set.of(AppearanceStage.EXPENSE_EDITED,
            AppearanceStage.EXPENSE_ENTERED,
            AppearanceStage.EXPENSE_AUTHORISED).contains(stage)) {
            throw new MojException.InternalServerError("Invalid appearance stage type: " + stage, null);
        }
        BulkExpenseDto bulkExpenseDto = new BulkExpenseDto();

        FinancialAuditDetails financialAuditDetails = appearances.get(0).getFinancialAuditDetails();
        if (financialAuditDetails != null) {
            if (financialAuditDetails.getSubmittedBy() != null) {
                bulkExpenseDto.setSubmittedBy(financialAuditDetails.getSubmittedBy().getUsername());
            }
            bulkExpenseDto.setSubmittedOn(financialAuditDetails.getSubmittedOn());
            if (financialAuditDetails.getApprovedBy() != null) {
                bulkExpenseDto.setApprovedBy(financialAuditDetails.getApprovedBy().getUsername());
            }
            bulkExpenseDto.setApprovedOn(financialAuditDetails.getApprovedOn());
            //TODO confirm if this should show for edited stage
            bulkExpenseDto.setJurorVersion(financialAuditDetails.getJurorRevisionWhenApproved());
        }
        bulkExpenseDto.setType(stage);
        List<BulkExpenseEntryDto> expenseDtos = getBulkExpenseDtoEntities(appearances);
        return bulkExpenseDto.setExpenses(expenseDtos)
            .setTotals(calculateBulkExpenseTotals(expenseDtos, appearances))
            .setJurorNumber(jurorNumber)
            .setMileage(getJurorDefaultMileage(jurorNumber));
    }

    List<BulkExpenseEntryDto> getBulkExpenseDtoEntities(List<Appearance> appearances) {
        Stream<BulkExpenseEntryDto> bulkExpenseEntryDtoStream = appearances.stream()
            .map(appearance -> {
                BulkExpenseEntryDto bulkExpenseEntryDto = BulkExpenseEntryDto.fromAppearance(appearance);
                if (AppearanceStage.EXPENSE_EDITED.equals(appearance.getAppearanceStage())) {
                    bulkExpenseEntryDto.setOriginalValue(
                        BulkExpenseEntryDto.fromAppearance(
                            getLastAuditForAppearanceWhereStage(appearance,
                                AppearanceStage.EXPENSE_AUTHORISED).getEntity()));
                }
                return bulkExpenseEntryDto;
            });
        return bulkExpenseEntryDtoStream
            .sorted(Comparator.comparing(BulkExpenseEntryDto::getAppearanceDate))
            .toList();
    }


    TotalExpenseDto calculateBulkExpenseTotals(List<BulkExpenseEntryDto> expenseDtos,
                                               List<Appearance> appearances) {
        TotalExpenseDto totalExpenseDto = (TotalExpenseDto) expenseDtos
            .stream()
            .map(ExpenseDto.class::cast)
            .reduce(new TotalExpenseDto(), ExpenseDto::addExpenseDto);
        totalExpenseDto.setTotalDays(appearances.size());
        //Total of all paid total
        BigDecimal totalPaid = appearances.stream()
            .map(Appearance::getTotalPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDue = appearances.stream()
            .map(Appearance::getTotalDue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal totalAmount = totalDue.add(totalPaid);
        totalExpenseDto.setTotalAmountPaidToDate(totalPaid);
        totalExpenseDto.setTotalAmount(totalAmount);
        totalExpenseDto.setBalanceToPay(totalAmount.subtract(totalPaid));
        return totalExpenseDto;
    }

    Revision<Long, Appearance> getLastAuditForAppearanceWhereStage(Appearance appearance,
                                                                   AppearanceStage appearanceStage) {
        //TODO revisit / optimise with RevisionUtil
        Optional<Revision<Long, Appearance>> appearanceRevision = appearanceRepository.findRevisions(
                new AppearanceId(
                    appearance.getJurorNumber(),
                    appearance.getAttendanceDate(),
                    appearance.getCourtLocation()
                )
            ).stream()
            .sorted((o1, o2) -> o2.getRequiredRevisionInstant().compareTo(o1.getRequiredRevisionInstant()))
            .filter(revision -> appearanceStage.equals(revision.getEntity().getAppearanceStage())).findFirst();

        if (appearanceRevision.isEmpty()) {
            throw new MojException.NotFound("No appearance history found with stage: " + appearanceStage + " for "
                + "appearance: " + appearance.getIdString(), null);
        }
        return appearanceRevision.get();
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
    public void setDefaultExpensesForJuror(DefaultExpenseSummaryDto dto) {
        Juror juror = JurorUtils.getActiveJurorRecord(jurorRepository, dto.getJurorNumber());

        juror.setSmartCard(dto.getSmartCardNumber());
        juror.setMileage(dto.getDistanceTraveledMiles());
        juror.setSmartCard(dto.getSmartCardNumber());
        juror.setTravelTime(calculateTravelTime(dto.getTravelTime()));
        juror.setAmountSpent(dto.getTotalSmartCardSpend());
        juror.setFinancialLoss(dto.getFinancialLoss());
        jurorRepository.save(juror);
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

        // create a single financial audit details object with a new audit number generated for this batch of expenses
        FinancialAuditDetails auditDetails = FinancialAuditDetails.builder()
            .submittedBy(userRepository.findByUsername(SecurityUtil.getActiveLogin()))
            .submittedOn(LocalDateTime.now())
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
        final Integer milesTraveled = travel.getMilesTraveled();

        BiFunction<TravelMethod, Boolean, BigDecimal> calculateTravelCost = (travelMethod, traveledBy) -> {
            if (traveledBy == null || !traveledBy) {
                return null;
            }
            BigDecimal travelRate = travelMethod.getRate(appearance.getCourtLocation(), travel);
            return travelRate.multiply(BigDecimal.valueOf(Optional.ofNullable(milesTraveled).orElse(0)));
        };

        appearance.setTraveledByCar(travel.getTraveledByCar());
        appearance.setJurorsTakenCar(travel.getJurorsTakenCar());
        appearance.setCarDue(calculateTravelCost.apply(TravelMethod.CAR, travel.getTraveledByCar()));

        appearance.setTraveledByMotorcycle(travel.getTraveledByMotorcycle());
        appearance.setJurorsTakenMotorcycle(travel.getJurorsTakenMotorcycle());
        appearance.setMotorcycleDue(
            calculateTravelCost.apply(TravelMethod.MOTERCYCLE, travel.getTraveledByMotorcycle()));

        appearance.setTraveledByBicycle(travel.getTraveledByBicycle());
        appearance.setBicycleDue(calculateTravelCost.apply(TravelMethod.BICYCLE, travel.getTraveledByBicycle()));


        appearance.setMilesTraveled(milesTraveled);

        appearance.setParkingDue(travel.getParking());
        appearance.setPublicTransportDue(travel.getPublicTransport());
        appearance.setHiredVehicleDue(travel.getTaxi());
    }

    void updateDraftFoodAndDrinkExpense(Appearance appearance, DailyExpenseFoodAndDrink foodAndDrink) {
        if (foodAndDrink == null) {
            return;
        }
        CourtLocation courtLocation = appearance.getCourtLocation();
        FoodDrinkClaimType claimType = foodAndDrink.getFoodAndDrinkClaimType();

        BigDecimal substanceRate = claimType.getRate(courtLocation);
        appearance.setSubsistenceDue(substanceRate);
        appearance.setSmartCardAmountDue(foodAndDrink.getSmartCardAmount());
        appearance.setFoodAndDrinkClaimType(claimType);
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
