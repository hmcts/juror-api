package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetter;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AssignOnUpdateServiceMod;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyMergeService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.juror.api.moj.domain.IJurorStatus.DISQUALIFIED;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.getJurorDigitalResponse;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.getJurorPaperResponse;
import static uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils.checkOwnershipForCurrentUser;
import static uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils.getActiveJurorPoolForUser;
import static uk.gov.hmcts.juror.api.moj.utils.JurorResponseUtils.createMinimalPaperSummonsRecord;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DisqualifyJurorServiceImpl implements DisqualifyJurorService {

    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;

    @NonNull
    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;

    @NotNull
    private final JurorResponseAuditRepository jurorResponseAuditRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    @NonNull
    private final DisqualificationLetterRepository disqualificationLetterRepository;
    @NotNull
    private final AssignOnUpdateServiceMod assignOnUpdateService;
    @NotNull
    private final SummonsReplyMergeService summonsReplyMergeService;

    @Override
    public DisqualifyReasonsDto getDisqualifyReasons(BureauJwtPayload payload) {
        log.trace("Api service method getDisqualifyReasons() started to retrieve disqualification reasons");
        List<DisqualifyReasonsDto.DisqualifyReasons> disqualifyReasons = new ArrayList<>();

        // Build the response DTO
        for (DisqualifyCodeEnum disqualifyCodeEnum : DisqualifyCodeEnum.values()) {
            DisqualifyReasonsDto.DisqualifyReasons disqualifyReason = DisqualifyReasonsDto.DisqualifyReasons
                .builder()
                .code(disqualifyCodeEnum.getCode())
                .description(disqualifyCodeEnum.getDescription())
                .heritageCode(disqualifyCodeEnum.getHeritageCode())
                .heritageDescription(disqualifyCodeEnum.getHeritageDescription())
                .build();
            disqualifyReasons.add(disqualifyReason);
        }

        log.trace(
            "Api service method getDisqualifyReasons() finished.  Service returned {} disqualification reasons",
            disqualifyReasons.size()
        );
        return DisqualifyReasonsDto.builder().disqualifyReasons(disqualifyReasons).build();
    }

    @Override
    @Transactional
    public void disqualifyJuror(String jurorNumber, DisqualifyJurorDto disqualifyJurorDto, BureauJwtPayload payload) {

        log.trace("Juror Number {} - Api service method disqualifyJuror() started with code {}", jurorNumber,
            disqualifyJurorDto.getCode());

        AbstractJurorResponse jurorResponse;

        //Check if the current user has access to the Juror record
        final JurorPool jurorPool = checkOfficerIsAuthorisedToAccessJurorRecord(jurorNumber, payload);

        //Get the existing juror response for the appropriate reply method, and map to the generic juror response pojo
        if (disqualifyJurorDto.getReplyMethod().equals(ReplyMethod.PAPER)) {
            jurorResponse = getJurorPaperResponse(jurorNumber, jurorPaperResponseRepository);
        } else {
            jurorResponse = getJurorDigitalResponse(jurorNumber, jurorDigitalResponseRepository);
        }

        //Check the status of the juror response to ensure only responses in the correct status can be updated
        checkJurorResponseStatus(jurorResponse);

        //Set the new status - need to copy the old status as required for the auditing steps
        final ProcessingStatus oldProcessingStatus = setJurorResponseProcessingStatus(jurorResponse);

        //Save the updated juror response
        if (disqualifyJurorDto.getReplyMethod().equals(ReplyMethod.PAPER)) {
            saveJurorPaperResponse(payload.getLogin(), (PaperResponse) jurorResponse);
        } else {
            saveJurorDigitalResponse(payload.getLogin(), oldProcessingStatus, (DigitalResponse) jurorResponse);
        }

        //Update juror pool record to reflect juror has been disqualified
        saveJurorPoolRecord(jurorPool, jurorResponse.getJurorNumber(), disqualifyJurorDto.getCode(),
            payload.getLogin());

        //Create audit history record to reflect changes to juror pool record related to disqualification
        createAuditHistory(payload.getLogin(), jurorNumber, jurorPool.getPoolNumber(), disqualifyJurorDto.getCode());

        //Queue request for a letter to be sent to the juror (disq_lett)
        queueRequestForDisqLetter(jurorNumber, disqualifyJurorDto.getCode());

        log.trace("Juror {} - Api service method disqualifyJuror() finished.  Juror disqualified with code {}",
            jurorNumber, disqualifyJurorDto.getCode());
    }

    @Override
    @Transactional
    public void disqualifyJurorDueToAgeOutOfRange(String jurorNumber,
                                                  BureauJwtPayload bureauJwtPayload) {

        final JurorPool jurorPool = checkOfficerIsAuthorisedToAccessJurorRecord(jurorNumber, bureauJwtPayload);
        DigitalResponse digitalResponse = jurorDigitalResponseRepository.findByJurorNumber(jurorNumber);
        PaperResponse paperResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);

        if (null != digitalResponse) {
            checkJurorResponseStatus(digitalResponse);
            final ProcessingStatus oldProcessingStatus = setJurorResponseProcessingStatus(digitalResponse);
            digitalResponse.setProcessingComplete(true);
            digitalResponse.setCompletedAt(LocalDateTime.now());
            saveJurorDigitalResponse(bureauJwtPayload.getLogin(), oldProcessingStatus, digitalResponse);
            processDisqualification(jurorPool, digitalResponse, bureauJwtPayload, DisqualifyCodeEnum.A);

        } else if (null != paperResponse) {
            checkJurorResponseStatus(paperResponse);
            paperResponse.setProcessingComplete(true);
            paperResponse.setProcessingStatus(ProcessingStatus.CLOSED);
            paperResponse.setCompletedAt(LocalDateTime.now());
            saveJurorPaperResponse(bureauJwtPayload.getLogin(),
                paperResponse);
            processDisqualification(jurorPool, paperResponse, bureauJwtPayload, DisqualifyCodeEnum.A);

        } else {
            Juror juror = jurorPool.getJuror();
            PaperResponse minimalPaperResponse = createMinimalPaperSummonsRecord(juror, "Disqualification due to age.");
            jurorPaperResponseRepository.save(minimalPaperResponse);
            saveJurorPaperResponse(bureauJwtPayload.getLogin(),
                minimalPaperResponse);
            processDisqualification(jurorPool, minimalPaperResponse, bureauJwtPayload, DisqualifyCodeEnum.A);
        }
    }


    private void processDisqualification(JurorPool jurorPool, AbstractJurorResponse response,
                                         BureauJwtPayload bureauJwtPayload, DisqualifyCodeEnum disqualifyCodeEnum) {
        saveJurorPoolRecord(jurorPool, response.getJurorNumber(), disqualifyCodeEnum,
            bureauJwtPayload.getLogin());
        createAuditHistory(bureauJwtPayload.getLogin(), response.getJurorNumber(), jurorPool.getPoolNumber(),
            disqualifyCodeEnum);
        queueRequestForDisqLetter(response.getJurorNumber(), disqualifyCodeEnum);
        log.trace("Juror {} - Api service method disqualifyJuror() finished.  Juror disqualified with code {}",
            response.getJurorNumber(), disqualifyCodeEnum);
    }

    private void queueRequestForDisqLetter(String jurorNumber, DisqualifyCodeEnum disqualifyCode) {
        log.trace("Juror {} - Service method queueRequestForDisqLetter() invoked", jurorNumber);
        DisqualificationLetter disqualificationLetter = new DisqualificationLetter();
        disqualificationLetter.setJurorNumber(jurorNumber);
        disqualificationLetter.setDisqCode(disqualifyCode.getHeritageCode());
        disqualificationLetter.setDateDisq(Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));

        disqualificationLetterRepository.save(disqualificationLetter);
    }

    private void createAuditHistory(String userId, String jurorNumber, String poolNumber,
                                    DisqualifyCodeEnum disqualificationCode) {
        log.trace("Juror {} - Service method createAuditHistory() invoked", jurorNumber);

        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorNumber)
            .historyCode(HistoryCodeMod.DISQUALIFY_POOL_MEMBER)
            .createdBy(userId)
            .poolNumber(poolNumber)
            .otherInformation("Code " + disqualificationCode.getHeritageCode())
            .build();
        jurorHistoryRepository.save(jurorHistory);
    }

    private void saveJurorPoolRecord(JurorPool jurorPool,
                                     String jurorNumber,
                                     DisqualifyCodeEnum disqualificationCode,
                                     String login) {
        log.trace("Juror {} - Service method updateJurorPoolRecord() invoked", jurorNumber);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(DISQUALIFIED);

        Juror juror = jurorPool.getJuror();
        juror.setResponded(true);
        juror.setDisqualifyDate(LocalDate.now(ZoneId.systemDefault()));
        juror.setDisqualifyCode(disqualificationCode.getHeritageCode());
        juror.setUserEdtq(login);

        jurorPool.setNextDate(null);
        jurorPool.setUserEdtq(login);
        jurorPool.setStatus(jurorStatus);

        jurorPoolRepository.save(jurorPool);
    }

    private void saveJurorDigitalResponse(String officerUsername,
                                          ProcessingStatus oldProcessingStatus,
                                          DigitalResponse jurorResponse) {
        log.trace("Juror {} - Service method saveJurorDigitalResponse() invoked", jurorResponse.getJurorNumber());


        //If no staff assigned, assign current login. Paper responses are not assigned to individuals because
        // (currently)
        //the concept of workflow does not exist for a paper response.
        if (isNull(jurorResponse.getStaff())) {
            assignOnUpdateService.assignToCurrentLogin(jurorResponse, officerUsername);

        }

        //Merge the updated juror digital response
        summonsReplyMergeService.mergeDigitalResponse(jurorResponse, officerUsername);

        //Create an audit entry to reflect a change to the digital response related to disqualification
        jurorResponseAuditRepository.save(JurorResponseAudit.builder()
            .jurorNumber(jurorResponse.getJurorNumber())
            .login(officerUsername)
            .oldProcessingStatus(oldProcessingStatus)
            .newProcessingStatus(jurorResponse.getProcessingStatus())
            .build());
    }

    private void saveJurorPaperResponse(String officerUsername, PaperResponse paperResponse) {
        log.trace("Juror {} - Service method saveJurorPaperResponse() invoked", paperResponse.getJurorNumber());
        summonsReplyMergeService.mergePaperResponse(paperResponse, officerUsername);
    }

    private ProcessingStatus setJurorResponseProcessingStatus(AbstractJurorResponse jurorResponse) {
        log.trace(
            "Juror {} - Service method setJurorResponseProcessingStatus() invoked",
            jurorResponse.getJurorNumber()
        );

        ProcessingStatus oldProcessingStatus = jurorResponse.getProcessingStatus();
        jurorResponse.setProcessingStatus(ProcessingStatus.CLOSED);

        return oldProcessingStatus;
    }

    private void checkJurorResponseStatus(AbstractJurorResponse jurorResponse) {
        log.trace("Juror {} - Service method checkJurorResponseStatus() invoked", jurorResponse.getJurorNumber());

        if ((jurorResponse.getProcessingComplete()).equals(Boolean.TRUE)) {
            final String message = String.format(
                "Juror: %s - Juror cannot be disqualified because the response was completed on %s",
                jurorResponse.getJurorNumber(),
                jurorResponse.getCompletedAt()
            );
            throw new MojException.BadRequest(message, null);
        }
    }

    private JurorPool checkOfficerIsAuthorisedToAccessJurorRecord(String jurorNumber, BureauJwtPayload payload) {
        log.trace("Juror {} - Service method checkOfficerIsAuthorisedToAccessJurorRecord() invoked", jurorNumber);

        JurorPool jurorPool = getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber, payload.getOwner());
        checkOwnershipForCurrentUser(jurorPool, payload.getOwner());

        return jurorPool;
    }
}
