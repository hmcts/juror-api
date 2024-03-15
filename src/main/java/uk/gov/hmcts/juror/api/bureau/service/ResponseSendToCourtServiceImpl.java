package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseSendToCourtController;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Slf4j
@Service
public class ResponseSendToCourtServiceImpl implements ResponseSendToCourtService {

    private final JurorDigitalResponseRepositoryMod responseRepository;
    private final JurorResponseAuditRepositoryMod responseAuditRepository;
    private final EntityManager entityManager;
    private final AssignOnUpdateService assignOnUpdateService;


    @Autowired
    public ResponseSendToCourtServiceImpl(final JurorDigitalResponseRepositoryMod responseRepository,
                                          final JurorResponseAuditRepositoryMod responseAuditRepository,
                                          final EntityManager entityManager,
                                          final AssignOnUpdateService assignOnUpdateService) {
        Assert.notNull(responseRepository, "JurorDigitalResponseRepositoryMod cannot be null");
        Assert.notNull(responseAuditRepository, "JurorResponseAuditRepositoryMod cannot be null");
        Assert.notNull(entityManager, "EntityManager cannot be null");
        this.responseRepository = responseRepository;
        this.responseAuditRepository = responseAuditRepository;
        this.entityManager = entityManager;
        this.assignOnUpdateService = assignOnUpdateService;

    }


    @Override
    @Transactional
    public boolean sendResponseToCourt(final String jurorId,
                                       final ResponseSendToCourtController.SendToCourtDto sendToCourtDto,
                                       String login) {

        DigitalResponse savedResponse = responseRepository.findByJurorNumber(jurorId);
        log.debug("Starting send to court response status  update for juror {}", jurorId);


        if (savedResponse != null) {

            //detach the entity so that it will have to reattached by hibernate on save trigger optimistic locking.
            entityManager.detach(savedResponse);

            // set optimistic lock version from UI
            log.debug("Version: DB={}, UI={}", savedResponse.getVersion(), sendToCourtDto.getVersion());
            savedResponse.setVersion(sendToCourtDto.getVersion());


            if (savedResponse.getProcessingComplete()) {
                log.debug("Unable to update status for juror {} response as processing is already complete.", jurorId);
                throw new ResponseAlreadyCompleted(jurorId);

            } else if (savedResponse.getProcessingStatus() != ProcessingStatus.CLOSED) {

                log.debug("Updating juror '{}' status to '{}'.", jurorId, ProcessingStatus.CLOSED);

                savedResponse.setProcessingComplete(Boolean.TRUE);
                savedResponse.setCompletedAt(LocalDateTime.now());
                savedResponse.setProcessingStatus(ProcessingStatus.CLOSED);

                //assign current staff if staff is un assigned.

                if (null == savedResponse.getStaff()) {
                    assignOnUpdateService.assignToCurrentLogin(savedResponse, login);
                }

                responseRepository.save(savedResponse);// save the completed response to Juror Digital DB*/

                log.debug("response status updated for juror {}", jurorId);

                log.debug("updating response audit for juror {}", jurorId);

                //audit response status change
                responseAuditRepository.save(JurorResponseAuditMod.builder()
                    .jurorNumber(jurorId)
                    .login(login)
                    .oldProcessingStatus(savedResponse.getProcessingStatus())
                    .newProcessingStatus(ProcessingStatus.CLOSED)
                    .build());
                log.debug("response audit updated for juror {}", jurorId);
            }
        } else {
            log.error("No juror response found for juror number {}", jurorId);
            throw new JurorResponseNotFoundException("No juror response found");
        }

        return true;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ResponseAlreadyCompleted extends RuntimeException {
        public ResponseAlreadyCompleted(final String jurorId) {
            super(String.format("Unable to update status for Juror %s as Juror's response processing is already"
                + " completed, further changes should be made in Juror Legacy", jurorId));
        }
    }

}
