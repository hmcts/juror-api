package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AssignOnUpdateServiceMod;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyMergeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
@SuppressWarnings("PMD.TooManyImports")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DisqualifyJurorServiceImpl implements DisqualifyJurorService {

    private final JurorPoolRepository jurorPoolRepository;
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    private final JurorResponseAuditRepositoryMod jurorResponseAuditRepository;
    private final AssignOnUpdateServiceMod assignOnUpdateService;
    private final SummonsReplyMergeService summonsReplyMergeService;
    private final PrintDataService printDataService;
    private final JurorHistoryService jurorHistoryService;

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

        //Check if the current user has access to the Juror record
        final JurorPool jurorPool = checkOfficerIsAuthorisedToAccessJurorRecord(jurorNumber, payload);

        if (!ReplyMethod.NONE.equals(disqualifyJurorDto.getReplyMethod())) {

            AbstractJurorResponse jurorResponse = null;

            // Get the existing juror response for the appropriate reply method, and map to the generic juror
            // response pojo
            if (ReplyMethod.PAPER.equals(disqualifyJurorDto.getReplyMethod())) {
                jurorResponse = getJurorPaperResponse(jurorNumber, jurorPaperResponseRepository);
            } else if (ReplyMethod.DIGITAL.equals(disqualifyJurorDto.getReplyMethod())) {
                jurorResponse = getJurorDigitalResponse(jurorNumber, jurorDigitalResponseRepository);
            }

            if (jurorResponse == null) {
                throw new MojException.NotFound("Juror response not found", null);
            }

            //Check the status of the juror response to ensure only responses in the correct status can be updated
            if (Boolean.FALSE.equals(jurorResponse.getProcessingComplete())) {

                log.debug("Juror {} - Juror response is not complete, updating response", jurorNumber);

                //Set the new status - need to copy the old status as required for the auditing steps
                final ProcessingStatus oldProcessingStatus = setJurorResponseProcessingStatus(jurorResponse);

                //Save the updated juror response
                if (ReplyMethod.PAPER.equals(disqualifyJurorDto.getReplyMethod())) {
                    saveJurorPaperResponse(payload.getLogin(), (PaperResponse) jurorResponse);
                } else if (ReplyMethod.DIGITAL.equals(disqualifyJurorDto.getReplyMethod())) {
                    saveJurorDigitalResponse(payload.getLogin(), oldProcessingStatus, (DigitalResponse) jurorResponse);
                }
            }
        }

        //Update juror pool record to reflect juror has been disqualified
        saveJurorPoolRecord(jurorPool, jurorPool.getJurorNumber(), disqualifyJurorDto.getCode(),
            payload.getLogin());

        //Create audit history record to reflect changes to juror pool record related to disqualification
        jurorHistoryService.createDisqualifyHistory(jurorPool, disqualifyJurorDto.getCode().getCode());

        if (JurorDigitalApplication.JUROR_OWNER.equals(payload.getOwner())) {
            //Queue request for a letter to be sent to the juror
            printDataService.printWithdrawalLetter(jurorPool);
        }

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
        jurorHistoryService.createDisqualifyHistory(jurorPool, disqualifyCodeEnum.getCode());

        if (JurorDigitalApplication.JUROR_OWNER.equals(bureauJwtPayload.getOwner())) {
            // TODO need to check if this is the right letter to send
            printDataService.printWithdrawalLetter(jurorPool);
        }

        log.trace("Juror {} - Api service method disqualifyJuror() finished.  Juror disqualified with code {}",
            response.getJurorNumber(), disqualifyCodeEnum);
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
        jurorResponseAuditRepository.save(JurorResponseAuditMod.builder()
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
