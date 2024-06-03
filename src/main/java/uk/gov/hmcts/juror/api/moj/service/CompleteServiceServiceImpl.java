package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteJurorResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorStatusValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CompleteServiceServiceImpl implements CompleteServiceService {

    private final JurorPoolRepository jurorPoolRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorRepository jurorRepository;
    private final JurorHistoryService jurorHistoryService;


    @Override
    @Transactional(readOnly = true)
    public PaginatedList<CompleteJurorResponse> search(JurorPoolSearch search) {
        String owner = SecurityUtil.getActiveOwner();

        PaginatedList<CompleteJurorResponse> completeJurorResponses =
            jurorPoolRepository.findJurorPoolsBySearch(search, owner,
                jurorPoolJPQLQuery -> jurorPoolJPQLQuery.where(
                    QJurorPool.jurorPool.status.status.eq(IJurorStatus.COMPLETED)),
                jurorPool -> {
                    Juror juror = jurorPool.getJuror();
                    return CompleteJurorResponse.builder()
                        .jurorNumber(jurorPool.getJurorNumber())
                        .poolNumber(jurorPool.getPoolNumber())
                        .firstName(juror.getFirstName())
                        .lastName(juror.getLastName())
                        .postCode(juror.getPostcode())
                        .completionDate(juror.getCompletionDate())
                        .build();
                },
                500L);

        if (completeJurorResponses == null || completeJurorResponses.isEmpty()) {
            throw new MojException.NotFound("No complete juror pools found that meet your search criteria.", null);
        }
        return completeJurorResponses;
    }

    @Override
    public void uncompleteJurorsService(String jurorNumber, String poolNumber) {
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndStatus(jurorNumber,
            poolNumber, RepositoryUtils.retrieveFromDatabase(IJurorStatus.COMPLETED, jurorStatusRepository));

        if (jurorPool == null) {
            throw new MojException.NotFound("No complete juror pool found for Juror number " + jurorNumber, null);
        }
        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository));
        Juror juror = jurorPool.getJuror();
        juror.setCompletionDate(null);
        jurorHistoryService.createUncompleteServiceHistory(jurorPool);
        jurorRepository.save(juror);
        jurorPoolRepository.save(jurorPool);
    }

    @Override
    public void completeService(String poolNumber,
                                CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto) {
        List<String> ineligibleJurorNumbers = new ArrayList<>();
        for (String jurorNumber : completeServiceJurorNumberListDto.getJurorNumbers()) {
             JurorPool jurorPool = getJurorPool(poolNumber, jurorNumber);
            if(isJurorValidForCompletion(jurorPool)) {
                completeService(jurorPool, completeServiceJurorNumberListDto.getCompletionDate());
            } else {
                ineligibleJurorNumbers.add(jurorNumber);
            }
        }
        if(!ineligibleJurorNumbers.isEmpty()) {
            createErrorMessageForJurorsIneligibleForCompletion(ineligibleJurorNumbers);
        }
    }

    private void completeService(JurorPool jurorPool, LocalDate completionDate) {
        jurorPool.setStatus(RepositoryUtils.retrieveFromDatabase(IJurorStatus.COMPLETED, jurorStatusRepository));
        Juror juror = jurorPool.getJuror();
        juror.setCompletionDate(completionDate);

        jurorHistoryService.createCompleteServiceHistory(jurorPool);
        jurorRepository.save(juror);
        jurorPoolRepository.save(jurorPool);
    }

    private void createErrorMessageForJurorsIneligibleForCompletion(List<String> ineligibleJurorNumbers) {
        if(!ineligibleJurorNumbers.isEmpty()) {
            String jurorNumbers = ineligibleJurorNumbers.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

            throw new MojException.BusinessRuleViolation(
                "Unable to complete the service for the following juror number(s) due "
                    + "to invalid state: " + jurorNumbers,
                MojException.BusinessRuleViolation.ErrorCode.COMPLETE_SERVICE_JUROR_IN_INVALID_STATE);
        }
    }

    @Override
    @Transactional
    public void completeDismissedJurorsService(CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto) {
        for (String jurorNumber : completeServiceJurorNumberListDto.getJurorNumbers()) {
            completeDismissedJurorsService(jurorNumber, completeServiceJurorNumberListDto.getCompletionDate());
        }
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
        return !Set.of(IJurorStatus.TRANSFERRED, IJurorStatus.FAILED_TO_ATTEND, IJurorStatus.SUMMONED)
                .contains(jurorPool.getStatus().getStatus());
    }
}
