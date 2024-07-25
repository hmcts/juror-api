package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalDecision;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.ExcusalResponseException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Excusal Response service.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ExcusalResponseServiceImpl implements ExcusalResponseService {

    @NonNull
    private final ExcusalCodeRepository excusalCodeRepository;
    @NonNull
    private final JurorRepository jurorRepository;
    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @NonNull
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final SummonsReplyMergeService mergeService;
    @NonNull
    private final JurorStatusRepository jurorStatusRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    @NonNull
    private final PrintDataService printDataService;
    private final JurorHistoryService jurorHistoryService;
    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepositoryMod;

    @Override
    @Transactional
    public void respondToExcusalRequest(BureauJwtPayload payload, ExcusalDecisionDto excusalDecisionDto,
                                        String jurorNumber) {

        final String login = payload.getLogin();
        final String owner = payload.getOwner();
        log.info(String.format("Processing excusal request for Juror %s, by user %s", jurorNumber, login));

        checkExcusalCodeIsValid(excusalDecisionDto.getExcusalReasonCode());

        JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository, jurorNumber);

        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

        if (excusalDecisionDto.getReplyMethod() != null) {
            if (excusalDecisionDto.getReplyMethod().equals(ReplyMethod.PAPER)) {
                if (null == jurorPaperResponseRepository.findByJurorNumber(jurorNumber)) {
                    // There are scenarios where a juror may not have a paper response when excused from the juror
                    // record
                    log.info(String.format("No Paper response found for Juror %s when processing excusal request",
                        jurorNumber));
                } else {
                    setPaperResponseProcessingStatusToClosed(payload, jurorNumber);
                }
            } else {
                setDigitalResponseProcessingStatusToClosed(payload, jurorNumber);
            }
        }

        if (excusalDecisionDto.getExcusalDecision().equals(ExcusalDecision.GRANT)) {
            grantExcusalForJuror(payload, excusalDecisionDto, jurorPool);
            if (!ExcusalCodeEnum.D.getCode().equals(excusalDecisionDto.getExcusalReasonCode())
                && SecurityUtil.BUREAU_OWNER.equals(owner)) {
                // Only generate letter for non-deceased jurors and Bureau users
                sendExcusalLetter(jurorPool, jurorNumber, excusalDecisionDto.getExcusalReasonCode(), login);
            }
        } else {
            refuseExcusalForJuror(payload, excusalDecisionDto, jurorPool);
        }
    }

    public void checkExcusalCodeIsValid(String excusalCode) {
        log.info(String.format("Checking excusal code %s is valid", excusalCode));

        List<String> excusalCodes = new ArrayList<>();
        // Extract just the excusal code from the ExcusalCodeEntity objects stored in ExcusalCodeRepository
        RepositoryUtils.retrieveAllRecordsFromDatabase(excusalCodeRepository)
            .forEach(excusalCodeEntity -> excusalCodes.add(excusalCodeEntity.getCode()));

        if (excusalCodes.isEmpty()) {
            log.info("Unable to retrieve list of excusal codes from database");
            throw new ExcusalResponseException.UnableToRetrieveExcusalCodeList();
        }

        if (!excusalCodes.contains(excusalCode)) {
            log.info(String.format("Excusal code %s is invalid", excusalCode));
            throw new ExcusalResponseException.InvalidExcusalCode(excusalCode);
        }
    }

    private void setPaperResponseProcessingStatusToClosed(BureauJwtPayload payload, String jurorNumber) {

        log.info(String.format("Locating PAPER response for Juror %s", jurorNumber));
        PaperResponse jurorPaperResponse = DataUtils.getJurorPaperResponse(jurorNumber, jurorPaperResponseRepository);

        if (jurorPaperResponse.isClosed()) {
            return; //Closed records are static as such we should not update
        }

        log.info(String.format("PAPER response found for Juror %s", jurorNumber));

        jurorPaperResponse.setProcessingStatus(jurorResponseAuditRepositoryMod, ProcessingStatus.CLOSED);

        if (jurorPaperResponse.getStaff() == null) {
            log.info(String.format("No staff assigned to PAPER response for Juror %s", jurorNumber));
            User staff = userRepository.findByUsername(payload.getLogin());
            jurorPaperResponse.setStaff(staff);
            log.info(String.format("Assigned current user to PAPER response for Juror %s", jurorNumber));
        }

        mergeService.mergePaperResponse(jurorPaperResponse, payload.getLogin());

        log.info(String.format("PAPER response for Juror %s successfully closed", jurorNumber));
    }

    private void setDigitalResponseProcessingStatusToClosed(BureauJwtPayload payload, String jurorNumber) {

        log.info(String.format("Locating DIGITAL response for Juror %s", jurorNumber));
        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);

        if (jurorResponse.isClosed()) {
            return; //Closed records are static as such we should not update
        }

        log.info(String.format("DIGITAL response found for Juror %s", jurorNumber));

        jurorResponse.setProcessingStatus(jurorResponseAuditRepositoryMod, ProcessingStatus.CLOSED);

        if (jurorResponse.getStaff() == null) {
            log.info(String.format("No staff assigned to DIGITAL response for Juror %s", jurorNumber));
            User staff = userRepository.findByUsername(payload.getLogin());
            jurorResponse.setStaff(staff);
            log.info(String.format("Assigned current user to DIGITAL response for Juror %s", jurorNumber));
        }

        mergeService.mergeDigitalResponse(jurorResponse, payload.getLogin());

        log.info(String.format("DIGITAL response for Juror %s successfully closed", jurorNumber));
    }

    private void grantExcusalForJuror(BureauJwtPayload payload, ExcusalDecisionDto excusalDecisionDto,
                                      JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();

        log.info(String.format("Processing officer decision to grant excusal for Juror %s", juror.getJurorNumber()));

        juror.setResponded(true);
        juror.setExcusalDate(LocalDate.now());
        juror.setExcusalCode(excusalDecisionDto.getExcusalReasonCode());
        juror.setUserEdtq(payload.getLogin());
        jurorRepository.save(juror);

        jurorPool.setUserEdtq(payload.getLogin());
        jurorPool.setStatus(getPoolStatus(IJurorStatus.EXCUSED));
        jurorPool.setNextDate(null);
        jurorPoolRepository.save(jurorPool);

        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.EXCUSE_POOL_MEMBER)
            .createdBy(payload.getLogin())
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation("Add Excuse - " + excusalDecisionDto.getExcusalReasonCode())
            .build();

        jurorHistoryRepository.save(jurorHistory);
    }

    private void refuseExcusalForJuror(BureauJwtPayload payload, ExcusalDecisionDto excusalDecisionDto,
                                       JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();
        log.info(String.format("Processing officer decision to refuse excusal for Juror %s", juror.getJurorNumber()));

        juror.setResponded(true);
        if(jurorPool.getStatus().getStatus() != IJurorStatus.EXCUSED) {
            juror.setExcusalRejected(excusalDecisionDto.getExcusalReasonCode());
            juror.setExcusalDate(null);
        }
        juror.setUserEdtq(payload.getLogin());
        juror.setExcusalRejected("Y");
        jurorRepository.save(juror);

        jurorPool.setUserEdtq(payload.getLogin());
        jurorPoolRepository.save(jurorPool);

        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.EXCUSE_POOL_MEMBER)
            .createdBy(payload.getLogin())
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation("Refuse Excuse")
            .build();

        jurorHistoryRepository.save(jurorHistory);

        jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorPool.getJurorNumber())
            .dateCreated(LocalDateTime.now())
            .createdBy(payload.getLogin())
            .historyCode(HistoryCodeMod.RESPONDED_POSITIVELY)
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation(JurorHistory.RESPONDED)
            .build();

        jurorHistoryRepository.save(jurorHistory);

        // bureau only - queue letter for xerox
        if (payload.getOwner().equals("400")) {
            printDataService.printExcusalDeniedLetter(jurorPool);

            jurorHistoryService.createNonExcusedLetterHistory(jurorPool,"Refused Excusal");
        }

    }

    private void sendExcusalLetter(JurorPool jurorPool, String jurorNumber, String excusalCode, String login) {
        log.info(String.format("Preparing an excusal letter for Juror %s", jurorNumber));

        printDataService.printExcusalLetter(jurorPool);

        jurorHistoryService.createExcusedLetter(jurorPool);

        log.info(String.format("Excusal letter enqueued for Juror %s", jurorNumber));
    }

    private JurorStatus getPoolStatus(int poolStatusId) {
        return RepositoryUtils.retrieveFromDatabase(poolStatusId, jurorStatusRepository);
    }
}
