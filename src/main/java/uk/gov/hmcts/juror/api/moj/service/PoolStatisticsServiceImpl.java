package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolStatistics;
import uk.gov.hmcts.juror.api.moj.exception.PoolRequestException;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolStatisticsRepository;
import uk.gov.hmcts.juror.api.moj.utils.NumberUtils;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PoolStatisticsServiceImpl implements PoolStatisticsService {

    @NonNull
    private final PoolRequestRepository poolRequestRepository;
    @NonNull
    private final PoolStatisticsRepository poolStatisticsRepository;

    /**
     * Using information about the pool request and pool members, derive summary data and
     * populate a response object containing pool related statistics.
     *
     * @param poolNumber 9 digit numeric string to identify a specific pool request to retrieve statistics for
     * @return Pool Statistics summary and calculated values mapped in to a data transfer object
     */
    @Override
    public PoolSummaryResponseDto calculatePoolStatistics(String poolNumber) {
        PoolRequest poolRequest = getActivePoolRequest(poolNumber);
        PoolStatistics poolStatistics = getPoolStatistics(poolNumber);

        PoolSummaryResponseDto poolSummaryResponse = new PoolSummaryResponseDto();
        populatePoolDetailsData(poolSummaryResponse, poolRequest);
        populateBureauSummoningData(poolSummaryResponse, poolStatistics, poolRequest.getNumberRequested());
        populatePoolSummaryData(poolSummaryResponse, poolStatistics, poolRequest.getTotalNoRequired());
        populateAdditionalStatsData(poolSummaryResponse, poolStatistics, poolRequest);

        poolSummaryResponse.getPoolDetails().setCurrentOwner(poolRequest.getOwner());

        return poolSummaryResponse;
    }

    /**
     * Using a supplied pool number, find and return the active Pool Request record.
     *
     * @param poolNumber 9 digit numeric string to identify a specific pool request
     * @return and active Pool Request record
     */
    private PoolRequest getActivePoolRequest(String poolNumber) {
        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findByPoolNumber(poolNumber);
        if (!poolRequestOpt.isPresent()) {
            throw new PoolRequestException.PoolRequestNotFound(poolNumber);
        }
        return poolRequestOpt.get();
    }

    /**
     * Using a supplied pool number, query the POOL_STATS view to retrieve calculated information about members of a
     * pool.
     *
     * @param poolNumber 9 digit numeric string to identify a specific pool request
     * @return a POJO representing the calculated summary data in the POOL_STATS view
     */
    @Override
    public PoolStatistics getPoolStatistics(String poolNumber) {
        return poolStatisticsRepository.findById(poolNumber).orElse(new PoolStatistics());
    }

    private void populatePoolDetailsData(PoolSummaryResponseDto poolSummaryResponse, PoolRequest poolRequest) {
        PoolSummaryResponseDto.PoolDetails poolDetails = poolSummaryResponse.getPoolDetails();
        poolDetails.setPoolNumber(poolRequest.getPoolNumber());
        poolDetails.setNilPool(poolRequest.isNilPool());
        CourtLocation courtLocation = poolRequest.getCourtLocation();
        poolDetails.setCourtName(WordUtils.capitalizeFully(courtLocation.getName()));
        poolDetails.setCourtLocationCode(courtLocation.getLocCode());
        poolDetails.setPoolType(poolRequest.getPoolType().getDescription());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMM yyyy");
        poolDetails.setCourtStartDate(poolRequest.getReturnDate().format(formatter));

        poolDetails.setIsActive(poolRequestRepository.isActive(poolRequest.getPoolNumber()));
    }

    private void populateBureauSummoningData(PoolSummaryResponseDto poolSummaryResponse,
                                             PoolStatistics poolStatistics, Integer numberRequestedFromBureau) {
        PoolSummaryResponseDto.BureauSummoning bureauSummoning = poolSummaryResponse.getBureauSummoning();
        bureauSummoning.setRequestedFromBureau(numberRequestedFromBureau);

        bureauSummoning.setTotalSummoned(poolStatistics.getTotalSummoned());
        bureauSummoning.setConfirmedFromBureau(poolStatistics.getAvailable());
        bureauSummoning.setUnavailable(poolStatistics.getUnavailable());
        bureauSummoning.setUnresolved(poolStatistics.getUnresolved());

        int unboxedNoRequestedFromBureau = NumberUtils.unboxIntegerValues(numberRequestedFromBureau);
        int surplus = poolStatistics.getAvailable() - unboxedNoRequestedFromBureau;
        bureauSummoning.setSurplus(Math.max(surplus, 0));
    }

    private void populatePoolSummaryData(PoolSummaryResponseDto poolSummaryResponse,
                                         PoolStatistics poolStatistics, int totalNumberRequested) {
        PoolSummaryResponseDto.PoolSummary poolSummary = poolSummaryResponse.getPoolSummary();
        poolSummary.setRequiredPoolSize(totalNumberRequested);
        poolSummary.setCurrentPoolSize(poolStatistics.getCourtSupply() + poolStatistics.getAvailable());
    }

    private void populateAdditionalStatsData(PoolSummaryResponseDto poolSummaryResponse,
                                             PoolStatistics poolStatistics,
                                             PoolRequest poolRequest) {
        PoolSummaryResponseDto.AdditionalStatistics additionalStatistics =
            poolSummaryResponse.getAdditionalStatistics();
        additionalStatistics.setCourtSupply(poolStatistics.getCourtSupply());
        additionalStatistics.setTotalJurorsInPool(poolRequest.getJurorPools().size());
    }

}
