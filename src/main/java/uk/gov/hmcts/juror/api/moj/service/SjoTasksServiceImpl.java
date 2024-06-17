package uk.gov.hmcts.juror.api.moj.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.FailedToAttendListResponse;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SjoTasksServiceImpl implements SjoTasksService {

    private final JurorPoolRepository jurorPoolRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorHistoryService jurorHistoryService;


    @Override
    @Transactional
    public void undoFailedToAttendStatus(String jurorNumber, String poolNumber) {
        JurorPool jurorPool = jurorPoolRepository
            .findByJurorJurorNumberAndIsActiveAndOwner(jurorNumber, true, SecurityUtil.getActiveOwner());

        if (jurorPool == null) {
            throw new MojException.NotFound("No Failed To Attend juror pool found for Juror number "
                + jurorNumber, null);
        } else if (jurorPool.getStatus().getStatus() != IJurorStatus.FAILED_TO_ATTEND) {
            throw new MojException.BusinessRuleViolation(
                "Juror status must be failed to attend in order to undo the failed to attend status.",
                JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND);
        }

        jurorPool.setStatus(
            RepositoryUtils.retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository)
        );

        jurorHistoryService.createUndoFailedToAttendHistory(jurorPool);
        jurorPoolRepository.save(jurorPool);
    }
}
