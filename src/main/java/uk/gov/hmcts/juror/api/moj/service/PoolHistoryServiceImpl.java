package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolHistoryListDto;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PoolHistoryServiceImpl implements PoolHistoryService {

    @NonNull
    private PoolHistoryRepository poolHistoryRepository;
    @NonNull
    private PoolRequestRepository poolRequestRepository;


    @Override
    public PoolHistoryListDto getPoolHistoryListData(BureauJwtPayload payload, String poolNumber) {
        final String owner = payload.getOwner();
        log.debug(
            "Begin processing get pool history details for pool {} with user {} and owner {}", poolNumber,
            payload.getLogin(), owner);

        //checking pool request is valid and exists
        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase(poolNumber, poolRequestRepository);

        checkAccessForCurrentUser(owner, poolRequest);

        List<PoolHistory> poolHistoryList =
            poolHistoryRepository.findPoolHistorySincePoolCreated(poolNumber);
        List<PoolHistoryListDto.PoolHistoryDataDto> poolHistoryDataDtoList = new ArrayList<>();

        if (!poolHistoryList.isEmpty()) {
            for (PoolHistory poolHistory : poolHistoryList) {
                poolHistoryDataDtoList.add(new PoolHistoryListDto.PoolHistoryDataDto(
                    poolHistory,
                    poolHistory.getHistoryCode().getDescription()
                ));
            }
        }

        return new PoolHistoryListDto(poolHistoryDataDtoList);
    }

    private void checkAccessForCurrentUser(String owner, PoolRequest poolRequest) {
        if (!owner.equalsIgnoreCase(JurorDigitalApplication.JUROR_OWNER) && !poolRequest.getOwner().equals(owner)) {
            throw new MojException.Forbidden("Court user does not have access to this pool request",
                                             null);
        }
    }

}
