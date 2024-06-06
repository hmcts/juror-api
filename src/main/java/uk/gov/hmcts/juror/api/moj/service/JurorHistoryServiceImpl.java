package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.config.security.IsSeniorCourtUser;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.THistoryCodeRepository;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorHistoryServiceImpl implements JurorHistoryService {
    private static final String SYSTEM_USER_ID = "SYSTEM";
    private final JurorHistoryRepository jurorHistoryRepository;
    private final THistoryCodeRepository tHistoryCodeRepository;
    private final Clock clock;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static HashMap<String, String> mapper;

    public HashMap<String, String> getHistoryCodeMap() {
        if (null != mapper) {
            return mapper;
        }

        mapper = new HashMap<>();
        tHistoryCodeRepository.findAll().forEach((tHistoryCode ->
            mapper.put(tHistoryCode.getHistoryCode(), tHistoryCode.getDescription())));

        return mapper;
    }

    public String getHistoryDescription(String historyCode) {
        return getHistoryCodeMap().get(historyCode);
    }

    @Override
    public void createPoliceCheckDisqualifyHistory(JurorPool jurorPool) {
        registerHistorySystem(jurorPool, HistoryCodeMod.POLICE_CHECK_FAILED, "Failed");
        registerHistorySystem(jurorPool, HistoryCodeMod.DISQUALIFY_POOL_MEMBER, "Disqualify - E");
    }

    @Override
    public void createPoliceCheckQualifyHistory(JurorPool jurorPool, boolean isChecked) {
        registerHistorySystem(jurorPool, HistoryCodeMod.POLICE_CHECK_COMPLETE,
            isChecked
                ? "Passed"
                : "Unchecked - timed out"
        );
    }

    @Override
    public void createPoliceCheckInProgressHistory(JurorPool jurorPool) {
        registerHistorySystem(jurorPool, HistoryCodeMod.POLICE_CHECK_REQUEST, "Check requested");
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void createSendMessageHistory(String jurorNumber, String poolNumber, String otherInfo) {
        registerHistory(jurorNumber, poolNumber, HistoryCodeMod.NOTIFY_MESSAGE_REQUESTED,
            otherInfo,
            SecurityUtil.getActiveLogin());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void createCompleteServiceHistory(JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();
        if (juror.getCompletionDate() == null) {
            throw new MojException.InternalServerError("To create a complete service history entry. "
                + "The juror record must contain a completion date for juror " + juror.getJurorNumber(), null);
        }
        registerHistoryLoginUser(jurorPool, HistoryCodeMod.COMPLETE_SERVICE,
            "Completed service on " + dateFormatter.format(juror.getCompletionDate()));
    }

    @Override
    public void createUncompleteServiceHistory(JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();
        if (juror.getCompletionDate() != null) {
            throw new MojException.InternalServerError("To uncomplete a service history entry. "
                + "The juror record must not contain a completion date for juror " + juror.getJurorNumber(), null);
        }
        registerHistoryLoginUser(jurorPool, HistoryCodeMod.COMPLETE_SERVICE,
            "Completion date removed");
    }

    @Override
    public void createDeferredLetterHistory(JurorPool jurorPool) {
        if (jurorPool.getDeferralDate() == null || jurorPool.getDeferralCode() == null) {
            throw new MojException.InternalServerError("A deferred juror_pool record should exist for "
                + "the juror relating to the original pool they were summoned to and deferred from", null);
        }

        registerHistoryLoginUserAdditionalInfo(jurorPool, HistoryCodeMod.DEFERRED_LETTER,
            "Deferral Letter Printed", jurorPool.getDeferralDate(), jurorPool.getDeferralCode());
    }

    @Override
    public void createEditBankSortCodeHistory(String jurorNumber) {
        registerHistory(jurorNumber, null, HistoryCodeMod.CHANGE_PERSONAL_DETAILS, "Bank Sort Code Changed",
            SecurityUtil.getActiveLogin());
    }

    @Override
    public void createEditBankAccountNameHistory(String jurorNumber) {
        registerHistory(jurorNumber, null, HistoryCodeMod.CHANGE_PERSONAL_DETAILS, "Bank Account Name Changed",
            SecurityUtil.getActiveLogin());
    }

    @Override
    public void createEditBankAccountNumberHistory(String jurorNumber) {
        registerHistory(jurorNumber, null, HistoryCodeMod.CHANGE_PERSONAL_DETAILS, "Bank Acct No Changed",
            SecurityUtil.getActiveLogin());
    }

    @PreAuthorize("isAuthenticated()")
    public void createExpenseForApprovalHistory(FinancialAuditDetails financialAuditDetails,
                                                Appearance appearance) {
        registerHistoryWithAdditionalInfo(appearance.getJurorNumber(),
            appearance.getPoolNumber(),
            HistoryCodeMod.APPEARANCE_PAYMENTS,
            BigDecimalUtils.currencyFormat(appearance.getTotalDue()),
            SecurityUtil.getActiveLogin(),
            appearance.getAttendanceDate(),
            financialAuditDetails.getFinancialAuditNumber());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void createExpenseEditHistory(FinancialAuditDetails financialAuditDetails,
                                         Appearance appearance) {
        registerHistoryWithAdditionalInfo(appearance.getJurorNumber(),
            appearance.getPoolNumber(),
            HistoryCodeMod.EDIT_PAYMENTS,
            BigDecimalUtils.currencyFormat(appearance.getTotalDue()),
            SecurityUtil.getActiveLogin(),
            appearance.getAttendanceDate(),
            financialAuditDetails.getFinancialAuditNumber());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void createExpenseApproveCash(String jurorNumber,
                                         FinancialAuditDetails financialAuditDetails,
                                         LocalDate latestAppearanceDate,
                                         BigDecimal totalAmount) {
        registerHistoryWithAdditionalInfo(
            jurorNumber,
            null,
            HistoryCodeMod.CASH_PAYMENT_APPROVAL,
            BigDecimalUtils.currencyFormat(totalAmount),
            SecurityUtil.getActiveLogin(),
            latestAppearanceDate,
            financialAuditDetails.getFinancialAuditNumber());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void createExpenseApproveBacs(String jurorNumber,
                                         FinancialAuditDetails financialAuditDetails,
                                         LocalDate latestAppearanceDate,
                                         BigDecimal totalAmount) {
        registerHistoryWithAdditionalInfo(jurorNumber,
            null,
            HistoryCodeMod.ARAMIS_EXPENSES_FILE_CREATED,
            BigDecimalUtils.currencyFormat(totalAmount),
            SecurityUtil.getActiveLogin(),
            latestAppearanceDate,
            financialAuditDetails.getFinancialAuditNumber());
    }

    @Override
    @IsCourtUser
    public void createFailedToAttendHistory(JurorPool jurorPool) {
        if (jurorPool.getStatus().getStatus() != IJurorStatus.FAILED_TO_ATTEND) {
            throw new MojException.InternalServerError("To create a failed to attend history entry. "
                + "The juror pool must have the status of failed to attend", null);
        }
        registerHistoryLoginUser(jurorPool, HistoryCodeMod.FAILED_TO_ATTEND,
            "FTA after responding");
    }

    @Override
    @IsSeniorCourtUser
    public void createUndoFailedToAttendHistory(JurorPool jurorPool) {
        registerHistoryLoginUser(jurorPool, HistoryCodeMod.FAILED_TO_ATTEND,
            "FTA status removed");
    }

    @Override
    @IsSeniorCourtUser
    public void createPendingJurorAuthorisedHistory(JurorPool jurorPool) {
        registerHistoryLoginUser(jurorPool, HistoryCodeMod.PENDING_JUROR_AUTHORISED,
            "Pending juror authorised");
    }

    @Override
    public void createPoliceCheckInsufficientInformationHistory(JurorPool jurorPool) {
        registerHistorySystem(jurorPool, HistoryCodeMod.INSUFFICIENT_INFORMATION, "Insufficient Information");
    }

    @Override
    public void createSummonsReminderLetterHistory(JurorPool jurorPool) {
        registerHistorySystem(jurorPool, HistoryCodeMod.NON_RESPONDED_LETTER, "Reminder letter printed");
    }

    @Override
    public void createConfirmationLetterHistory(JurorPool jurorPool, String otherInfo) {
        registerHistorySystem(jurorPool, HistoryCodeMod.RESPONDED_LETTER, otherInfo);
    }

    @Override
    public void createWithdrawHistory(JurorPool jurorPool, String otherInfo) {
        registerHistorySystem(jurorPool, HistoryCodeMod.WITHDRAWAL_LETTER, otherInfo);
    }

    @Override
    public void createIdentityConfirmedHistory(JurorPool jurorPool) {
        registerHistorySystem(jurorPool, HistoryCodeMod.CHECK_ID, "Id confirmed");
    }

    public void createPostponementLetterHistory(JurorPool jurorPool, String confirmationLetter) {
        if (jurorPool.getDeferralDate() == null || !jurorPool.getDeferralCode().equals("P")) {
            throw new MojException.InternalServerError("A postponed juror_pool record should exist for "
                + "the juror relating to the original pool they were summoned to and postponed from", null);
        }

        registerHistoryLoginUserAdditionalInfo(jurorPool, HistoryCodeMod.POSTPONED_LETTER,
            "Postponement Letter Printed", jurorPool.getDeferralDate(), jurorPool.getDeferralCode());

    }

    public void createSummonsLetterHistory(JurorPool jurorPool, String otherInfo) {
        registerHistorySystem(jurorPool, HistoryCodeMod.PRINT_SUMMONS, otherInfo);
    }

    public void createJuryAttendanceHistory(JurorPool jurorPool, String otherInfo) {
        registerHistory(jurorPool, HistoryCodeMod.JURY_ATTENDANCE, otherInfo, SecurityUtil.getActiveLogin());
    }

    public void createPoolAttendanceHistory(JurorPool jurorPool, String otherInfo) {
        registerHistory(jurorPool, HistoryCodeMod.POOL_ATTENDANCE, otherInfo, SecurityUtil.getActiveLogin());
    }

    private void save(JurorHistory jurorHistory) {
        jurorHistoryRepository.save(jurorHistory);
    }

    private void registerHistoryLoginUser(JurorPool jurorPool, HistoryCodeMod historyCode, String info) {
        registerHistory(jurorPool, historyCode, info, SecurityUtil.getActiveLogin());
    }

    private void registerHistoryLoginUserAdditionalInfo(JurorPool jurorPool, HistoryCodeMod historyCode, String info,
                                                        LocalDate otherInfoDate, String otherInfoRef) {
        registerHistoryWithAdditionalInfo(jurorPool, historyCode, info, SecurityUtil.getActiveLogin(), otherInfoDate,
            otherInfoRef);
    }

    private void registerHistorySystem(JurorPool jurorPool, HistoryCodeMod historyCode, String info) {
        registerHistory(jurorPool, historyCode, info, SYSTEM_USER_ID);
    }

    private void registerHistory(JurorPool jurorPool, HistoryCodeMod historyCode, String info, String userId) {
        registerHistory(jurorPool.getJurorNumber(), jurorPool.getPoolNumber(),
            historyCode, info, userId);
    }

    private void registerHistory(String jurorNumber, String poolNumber, HistoryCodeMod historyCode, String info,
                                 String userId) {
        log.debug("Creating part history for juror {} with code {} and info {} for userId {}",
            jurorNumber, historyCode, info, userId);

        save(JurorHistory.builder()
            .poolNumber(poolNumber)
            .jurorNumber(jurorNumber)
            .dateCreated(LocalDateTime.now(clock))
            .createdBy(userId)
            .historyCode(historyCode)
            .otherInformation(info)
            .build());
    }

    private void registerHistoryWithAdditionalInfo(JurorPool jurorPool, HistoryCodeMod historyCode, String info,
                                                   String userId, LocalDate otherInfoDate, String otherInfoRef) {
        registerHistoryWithAdditionalInfo(
            jurorPool.getJurorNumber(),
            jurorPool.getPoolNumber(),
            historyCode,
            info,
            userId,
            otherInfoDate,
            otherInfoRef
        );
    }

    private void registerHistoryWithAdditionalInfo(String jurorNumber, String poolNumber,
                                                   HistoryCodeMod historyCode,
                                                   String info,
                                                   String userId, LocalDate otherInfoDate, String otherInfoRef) {
        log.debug("Creating part history for juror {} with code {} and info {} for userId {}",
            jurorNumber, historyCode, info, userId);

        save(JurorHistory.builder()
            .poolNumber(poolNumber)
            .jurorNumber(jurorNumber)
            .dateCreated(LocalDateTime.now(clock))
            .createdBy(userId)
            .historyCode(historyCode)
            .otherInformation(info)
            .otherInformationDate(otherInfoDate)
            .otherInformationRef(otherInfoRef)
            .build());
    }
}
