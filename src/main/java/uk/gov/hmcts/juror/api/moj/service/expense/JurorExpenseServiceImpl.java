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
import uk.gov.hmcts.juror.api.moj.controller.request.DefaultExpenseSummaryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseEntryDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotals;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorExpenseTotalsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.PAGE_SIZE;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
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
