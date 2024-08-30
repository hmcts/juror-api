package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UndeliverableResponseServiceImpl implements UndeliverableResponseService {
    private final JurorHistoryService jurorHistoryService;
    private final JurorPoolService jurorPoolService;

    @Override
    @Transactional
    public void markAsUndeliverable(List<String> jurorNumbers) {

        final String username = SecurityUtil.getUsername();
        final String owner = SecurityUtil.getActiveOwner();

        for (String jurorNumber : jurorNumbers) {
            log.debug("Begin processing mark as undeliverable for juror {} by user {}", jurorNumber, username);

            JurorPool jurorPool = jurorPoolService.getJurorPoolFromUser(jurorNumber);
            JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.UNDELIVERABLE);

            jurorPool.setStatus(jurorStatus);
            jurorPool.setUserEdtq(username);
            jurorPool.setNextDate(null);
            jurorHistoryService.createUndeliveredSummonsHistory(jurorPool);
            jurorPoolService.save(jurorPool);
        }
    }
}
