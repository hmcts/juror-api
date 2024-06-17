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

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SjoTasksServiceImpl implements SjoTasksService {

    private final JurorPoolRepository jurorPoolRepository;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorHistoryService jurorHistoryService;

    @Override
    @Transactional
    public PaginatedList<FailedToAttendListResponse> search(JurorPoolSearch request) {
        String owner = SecurityUtil.getActiveOwner();

        PaginatedList<FailedToAttendListResponse> failedToAttendJurors = jurorPoolRepository
            .findJurorPoolsBySearch(request, owner,
                jurorPoolJPQLQuery -> jurorPoolJPQLQuery.where(
                    QJurorPool.jurorPool.status.status.eq(IJurorStatus.FAILED_TO_ATTEND)),
                jurorPool -> {
                    Juror juror = jurorPool.getJuror();

                    return FailedToAttendListResponse.builder()
                        .jurorNumber(jurorPool.getJurorNumber())
                        .poolNumber(jurorPool.getPoolNumber())
                        .firstName(juror.getFirstName())
                        .lastName(juror.getLastName())
                        .postcode(juror.getPostcode())
                    .build();
                }, 500L);

        if (failedToAttendJurors == null || failedToAttendJurors.isEmpty()) {
            throw new MojException.NotFound("No Failed To Attend jurors found.", null);
        }

        return failedToAttendJurors;
    }

    @Override
    @Transactional
    public void undoFailedToAttendStatus(String jurorNumber, String poolNumber) {
        JurorStatus jurorStatusFailedToAttend = RepositoryUtils
            .retrieveFromDatabase(IJurorStatus.FAILED_TO_ATTEND, jurorStatusRepository);
        JurorStatus jurorStatusResponded = RepositoryUtils
            .retrieveFromDatabase(IJurorStatus.RESPONDED, jurorStatusRepository);

        JurorPool jurorPool = jurorPoolRepository
            .findByJurorJurorNumberAndPoolPoolNumberAndStatus(jurorNumber, poolNumber, jurorStatusFailedToAttend);

        if (jurorPool == null) {
            throw new MojException.NotFound("No Failed To Attend juror pool found for Juror number "
                + jurorNumber, null);
        }

        jurorPool.setStatus(jurorStatusResponded);
        jurorHistoryService.createUndoFailedToAttendHistory(jurorPool);
        jurorPoolRepository.save(jurorPool);
    }

}
