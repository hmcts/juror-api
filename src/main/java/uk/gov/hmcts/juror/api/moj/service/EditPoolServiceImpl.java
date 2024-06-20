package uk.gov.hmcts.juror.api.moj.service;

import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolEditRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.PoolComment;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.PoolEditException;
import uk.gov.hmcts.juror.api.moj.repository.PoolCommentRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EditPoolServiceImpl implements EditPoolService {

    @NonNull
    private final PoolRequestRepository poolRequestRepository;
    @NonNull
    private final PoolHistoryRepository poolHistoryRepository;
    @NonNull
    private final PoolCommentRepository poolCommentRepository;

    @Override
    public void editPool(BureauJwtPayload payload, PoolEditRequestDto poolEditRequestDto) {
        String payloadOwner = payload.getOwner();

        if (payloadOwner.equals(JurorDigitalApplication.JUROR_OWNER)) {
            editPoolJurorsRequested(payload, poolEditRequestDto);
        } else {
            editPoolTotalCapacity(payload, poolEditRequestDto);
        }
    }

    @Transactional
    public void editPoolJurorsRequested(BureauJwtPayload payload, PoolEditRequestDto poolEditRequestDto) {
        String poolNumber = poolEditRequestDto.getPoolNumber();
        log.trace(String.format("Enter editPoolJurorsRequested for Pool Number: %s", poolNumber));

        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findByPoolNumber(poolNumber);

        if (poolRequestOpt.isPresent()) {

            PoolRequest poolRequest = poolRequestOpt.get();
            final Integer noRequested = poolEditRequestDto.getNoRequested();

            validateNoRequested(noRequested, poolNumber, payload.getLogin());

            final int currentNoRequested = poolRequest.getNumberRequested();

            // no need to save number requested if it hasn't changed
            if (currentNoRequested != noRequested) {
                poolRequest.setNumberRequested(noRequested);
                poolRequestRepository.saveAndFlush(poolRequest);
                String otherInformation =
                    "Jurors Requested changed from " + currentNoRequested + " to " + noRequested
                        + "\nReason for change: " + poolEditRequestDto.getReasonForChange();
                updatePoolHistory(payload, poolNumber, otherInformation);
            }

            savePoolComments(payload, poolRequest, noRequested, poolEditRequestDto.getReasonForChange());
        }

        log.trace(String.format("Edited a pool request with Pool Number: %s", poolNumber));
    }

    private void updatePoolHistory(BureauJwtPayload payload, String poolNumber, String otherInformation) {
        poolHistoryRepository.save(new PoolHistory(poolNumber, LocalDateTime.now(), HistoryCode.PREQ,
            payload.getLogin(), otherInformation
        ));
    }

    private void savePoolComments(BureauJwtPayload payload, PoolRequest pool, int noRequested,
                                  String reasonForChange) {
        PoolComment poolComment = new PoolComment();
        poolComment.setUserId(payload.getLogin());
        poolComment.setPool(pool);
        poolComment.setNumberRequested(noRequested);
        poolComment.setComment(reasonForChange);
        poolComment.setLastUpdate(LocalDateTime.now());
        poolCommentRepository.save(poolComment);
    }

    private void validateNoRequested(Integer noRequested, String poolNumber, String login) {

        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findById(poolNumber);

        // Don't want to set a null number requested or number requested to be more than total
        // number required or negative
        int totalNoRequired = poolRequestOpt.map(PoolRequest::getTotalNoRequired).orElse(0);

        if (noRequested == null || noRequested > totalNoRequired || noRequested < 0) {
            final String updateType = "Number Requested";
            throw new PoolEditException.InvalidNoUpdate(login, poolNumber, updateType);
        }
    }

    @Transactional
    public void editPoolTotalCapacity(BureauJwtPayload payload, PoolEditRequestDto poolEditRequestDto) {
        String poolNumber = poolEditRequestDto.getPoolNumber();
        log.trace(String.format("Enter editPoolTotalCapacity for Pool Number: %s", poolNumber));

        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findByPoolNumber(poolNumber);

        if (poolRequestOpt.isPresent()) {

            PoolRequest poolRequest = poolRequestOpt.get();

            // court users can update the total capacity of a pool request at any time but must have
            // access to the pool request's court location
            if (!SecurityUtil.getCourts().contains(poolRequest.getCourtLocation().getLocCode())) {
                throw new PoolEditException.CannotEditPoolRequest(payload.getLogin(), poolNumber);
            }

            final int noRequested = poolRequest.getNumberRequested();
            final Integer totalRequired = poolEditRequestDto.getTotalRequired();

            validateTotalRequested(totalRequired, noRequested, poolNumber, payload.getLogin());

            final int currentTotalNoRequired = poolRequest.getTotalNoRequired();

            // no need to save total number required if it hasn't changed
            if (currentTotalNoRequired != totalRequired) {
                poolRequest.setTotalNoRequired(totalRequired);
                poolRequest.setLastUpdate(LocalDateTime.now());
                poolRequestRepository.saveAndFlush(poolRequest);
                String otherInformation = "Total Req was " + currentTotalNoRequired;
                updatePoolHistory(payload, poolNumber, otherInformation);
            }

            savePoolComments(payload, poolRequest, totalRequired, poolEditRequestDto.getReasonForChange());
            log.trace(String.format("Edited a pool request with Pool Number: %s", poolNumber));
        }
        log.trace(String.format("Exit editPoolTotalCapacity for Pool Number: %s", poolNumber));
    }

    private void validateTotalRequested(Integer totalRequested, int noRequested, String poolNumber, String login) {

        // Don't want to set a null number total required or number requested to be more than total
        // number required or negative
        if (totalRequested == null || totalRequested < noRequested || totalRequested < 0) {
            final String updateType = "Total Required";
            throw new PoolEditException.InvalidNoUpdate(login, poolNumber, updateType);
        }
    }

}
