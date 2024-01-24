package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.time.Clock;
import java.util.Date;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PartHistoryServiceImpl implements PartHistoryService {
    private static final String USER_ID = "SYSTEM";
    private final PartHistRepository partHistRepository;
    private final Clock clock;

    @Override
    public void createPoliceCheckDisqualifyPartHistory(JurorPool jurorPool) {
        registerPartHistory(jurorPool, PartHistoryCode.ELECTRONIC_POLICE_CHECK_DISQUALIFY, "Failed");
        registerPartHistory(jurorPool, PartHistoryCode.DISQUALIFY_POOL_MEMBER, "Disqualify - E");
    }

    @Override
    public void createPoliceCheckQualifyPartHistory(JurorPool jurorPool, boolean isChecked) {
        registerPartHistory(jurorPool, PartHistoryCode.ELECTRONIC_POLICE_CHECK_QUALIFY,
            isChecked ? "Passed" : "Unchecked - timed out"
        );
    }

    @Override
    public void createPoliceCheckInProgressPartHistory(JurorPool jurorPool) {
        registerPartHistory(jurorPool, PartHistoryCode.ELECTRONIC_POLICE_CHECK_REQUEST, "Check requested");
    }

    @Override
    public void createPoliceCheckInsufficientInformationPartHistory(JurorPool jurorPool) {
        registerPartHistory(jurorPool, PartHistoryCode.INSUFFICIENT_INFORMATION, "Insufficient Information");
    }

    private void save(PartHist partHist) {
        partHistRepository.save(partHist);
    }

    private void registerPartHistory(JurorPool jurorPool, PartHistoryCode partHistoryCode, String info) {
        log.debug("Creating part history for juror {} with code {} and info {}", jurorPool.getJurorNumber(),
            partHistoryCode, info);
        PartHist partHist = new PartHist();
        partHist.setOwner(jurorPool.getOwner());
        partHist.setPoolNumber(jurorPool.getPoolNumber());
        partHist.setJurorNumber(jurorPool.getJuror().getJurorNumber());
        partHist.setDatePart(Date.from(clock.instant()));
        partHist.setUserId(USER_ID);
        partHist.setHistoryCode(partHistoryCode.getCode());
        partHist.setInfo(info);
        save(partHist);
    }
}
