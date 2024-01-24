package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorStatusValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CompleteServiceServiceImpl implements CompleteServiceService {

    private final JurorPoolRepository jurorPoolRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorRepository jurorRepository;
    private final JurorHistoryService jurorHistoryService;


    @Override
    @Transactional
    public void completeService(String poolNumber,
                                CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto) {
        for (String jurorNumber : completeServiceJurorNumberListDto.getJurorNumbers()) {
            completeService(poolNumber, jurorNumber, completeServiceJurorNumberListDto.getCompletionDate());
        }
    }

    @Override
    @Transactional
    public void completeDismissedJurorsService(CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto) {
        for (String jurorNumber : completeServiceJurorNumberListDto.getJurorNumbers()) {
            completeDismissedJurorsService(jurorNumber, completeServiceJurorNumberListDto.getCompletionDate());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CompleteServiceValidationResponseDto validateCanCompleteService(String poolNumber,
                                                                           JurorNumberListDto jurorNumberListDto) {
        CompleteServiceValidationResponseDto completeServiceValidationResponseDto =
            new CompleteServiceValidationResponseDto();
        for (String jurorNumber : jurorNumberListDto.getJurorNumbers()) {
            JurorPool jurorPool = getJurorPool(poolNumber, jurorNumber);
            JurorStatusValidationResponseDto jurorStatusValidationResponseDto =
                createJurorStatusValidationResponseDto(jurorPool);

            if (isJurorValidForCompletion(jurorPool)) {
                completeServiceValidationResponseDto.addValid(jurorStatusValidationResponseDto);
            } else {
                completeServiceValidationResponseDto.addInvalidNotResponded(jurorStatusValidationResponseDto);
            }
        }
        return completeServiceValidationResponseDto;
    }

    private void completeService(String poolNumber, String jurorNumber, LocalDate completionDate) {
        JurorPool jurorPool = getJurorPool(poolNumber, jurorNumber);
        if (!isJurorValidForCompletion(jurorPool)) {
            throw new MojException.BusinessRuleViolation(
                "Juror number " + jurorNumber + " is not in a valid state to complete "
                    + "service", MojException.BusinessRuleViolation.ErrorCode.COMPLETE_SERVICE_JUROR_IN_INVALID_STATE);
        }

        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.COMPLETED, jurorStatusRepository));

        Juror juror = jurorPool.getJuror();
        juror.setCompletionDate(completionDate);

        jurorHistoryService.createCompleteServiceHistory(jurorPool);
        jurorRepository.save(juror);
        jurorPoolRepository.save(jurorPool);
    }

    private void completeDismissedJurorsService(String jurorNumber, LocalDate completionDate) {
        // should be only one active and responded juror with this juror number
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndStatusAndIsActive(jurorNumber,
            RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository), true);

        if (jurorPool == null) {
            throw new MojException.NotFound("Juror number " + jurorNumber + " not found in database", null);
        }

        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.COMPLETED, jurorStatusRepository));
        jurorPool.setNextDate(null);
        jurorPool.setOnCall(false);

        Juror juror = jurorPool.getJuror();
        juror.setCompletionDate(completionDate);

        jurorHistoryService.createCompleteServiceHistory(jurorPool);
        jurorRepository.save(juror);
        jurorPoolRepository.save(jurorPool);
    }

    private JurorStatusValidationResponseDto createJurorStatusValidationResponseDto(JurorPool jurorPool) {
        Juror juror = jurorPool.getJuror();
        return JurorStatusValidationResponseDto.builder()
            .jurorNumber(juror.getJurorNumber())
            .firstName(juror.getFirstName())
            .lastName(juror.getLastName())
            .status(jurorPool.getStatus().getStatus())
            .build();
    }

    private JurorPool getJurorPool(String poolNumber,
                                   String jurorNumber) {
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
        if (jurorPool == null) {
            throw new MojException.NotFound("Juror number " + jurorNumber + " not found in pool " + poolNumber, null);
        }
        return jurorPool;
    }

    private boolean isJurorValidForCompletion(JurorPool jurorPool) {
        return jurorPool.getStatus().getStatus() == IJurorStatus.RESPONDED;
    }
}
