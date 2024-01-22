package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UndeliverableResponseServiceImpl implements UndeliverableResponseService {

    public static final int UNDELIVERABLE = 9;

    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;

    @Override
    @Transactional
    public void markAsUndeliverable(BureauJWTPayload payload, String jurorNumber) {
        final String owner = payload.getOwner();

        log.debug("Begin processing mark as undeliverable for juror {} by user {}", jurorNumber, payload.getLogin());

        // update juror record for each active entry
        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);

        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(UNDELIVERABLE);

        jurorPool.setStatus(jurorStatus);
        jurorPool.setUserEdtq(payload.getLogin());
        jurorPool.setNextDate(null);

        JurorHistory history = JurorHistory.builder()
            .jurorNumber(jurorNumber)
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.UNDELIVERED_SUMMONS)
            .createdBy(payload.getLogin())
            .poolNumber(jurorPool.getPoolNumber())
            .otherInformation(null)
            .build();

        jurorPoolRepository.save(jurorPool);
        jurorHistoryRepository.save(history);

    }
}
