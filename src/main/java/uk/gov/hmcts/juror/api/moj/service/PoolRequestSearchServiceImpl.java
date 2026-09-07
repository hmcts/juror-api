package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestSearchListDto;
import uk.gov.hmcts.juror.api.moj.domain.FilterCoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QCoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QPoolType;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.repository.CoronerPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PoolRequestSearchServiceImpl implements PoolRequestSearchService {

    private static final long MAX_PAGE_SIZE = 25L;

    @NonNull
    private final PoolRequestRepository poolRequestRepository;
    @NonNull
    private final CoronerPoolRepository coronerPoolRepository;

    @Override
    public PoolRequestSearchListDto searchForPoolRequest(PoolSearchRequestDto poolSearchRequestDto,
                                                         List<String> courts) {

        validateBasicSearchCriteria(poolSearchRequestDto.getPoolNumber(), poolSearchRequestDto.getLocCode(),
            poolSearchRequestDto.getServiceStartDate());

        JPAQuery<Tuple> query = poolRequestRepository.selectFromPoolRequest();
        buildQuery(query, poolSearchRequestDto, courts);
        query.limit(MAX_PAGE_SIZE).offset(poolSearchRequestDto.getOffset() * MAX_PAGE_SIZE);

        long queryStartTime = System.currentTimeMillis();
        QueryResults<Tuple> queryResults = query.fetchResults();
        long totalResults = queryResults.getTotal();
        long queryFinishTime = System.currentTimeMillis();
        long elapsedTimeSeconds = (queryFinishTime - queryStartTime) / 1000;

        log.trace("Pool Search request: {}", poolSearchRequestDto);
        log.debug("Pool Search took {} seconds to return {} results", elapsedTimeSeconds, totalResults);

        List<Tuple> results = queryResults.getResults();
        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> resultsData = new ArrayList<>();

        for (Tuple result : results) {
            resultsData.add(new PoolRequestSearchListDto.PoolRequestSearchDataDto(result));
        }

        return new PoolRequestSearchListDto(resultsData, totalResults);
    }

    @Override
    public PaginatedList<FilterCoronerPool> searchForCoronerPools(CoronerPoolFilterRequestQuery query) {
        return PaginationUtil.toPaginatedList(
            coronerPoolRepository.fetchFilteredCoronerPools(query),
            query,
            query.getSortField(),
            query.getSortMethod(),
            tuple -> {
                FilterCoronerPool.FilterCoronerPoolBuilder builder = FilterCoronerPool.builder()
                    .poolNumber(tuple.get(QCoronerPool.coronerPool.poolNumber))
                    .courtName(tuple.get(QCoronerPool.coronerPool.courtLocation.locCourtName))
                    .requestedDate(tuple.get(QCoronerPool.coronerPool.requestDate))
                    .requestedBy(tuple.get(QCoronerPool.coronerPool.name));
                return builder.build();
            },
            ValidationConstants.MAX_ITEMS
        );
    }

    private boolean isStringEmpty(String param) {
        return param == null || param.isEmpty();
    }

    private void validateBasicSearchCriteria(String poolNumber, String locCode, LocalDate serviceStartDate) {
        if (isStringEmpty(poolNumber) && isStringEmpty(locCode) && serviceStartDate == null) {
            throw new IllegalArgumentException("At least one basic search criteria is required to conduct a Pool "
                + "Search");
        }
    }

    private void buildQuery(JPAQuery<Tuple> query, PoolSearchRequestDto poolSearchRequestDto, List<String> courts) {
        evaluatePoolNumberParameter(query, poolSearchRequestDto.getPoolNumber());
        evaluateCourtLocationParameter(query, poolSearchRequestDto.getLocCode(),
            poolSearchRequestDto.getPoolNumber(), courts);
        evaluateStartDateParameter(query, poolSearchRequestDto.getServiceStartDate());
        evaluateAdvancedSearchCriteria(query, poolSearchRequestDto);

        if (poolSearchRequestDto.getSortColumn() != null && poolSearchRequestDto.getSortDirection() != null) {
            log.trace("Sort order specified as {} {}", poolSearchRequestDto.getSortColumn(),
                poolSearchRequestDto.getSortDirection());
            applySorting(query, poolSearchRequestDto);
        } else {
            log.trace("No sort order specified, using default: order by Service Start Date, Descending");
            poolRequestRepository.orderByDateColumn(query, QPoolRequest.poolRequest.returnDate, SortDirection.DESC);
        }
    }

    private void evaluatePoolNumberParameter(JPAQuery<Tuple> query, String poolNumber) {
        log.trace("Enter evaluatePoolNumberParameter");

        final int fullPoolNumberLength = 9;

        if (!isStringEmpty(poolNumber)) {
            log.debug("Pool Number supplied as Pool Request Search parameter: {}", poolNumber);
            if (poolNumber.length() == fullPoolNumberLength) {
                poolRequestRepository.addPoolNumberPredicate(query, poolNumber);
            } else {
                poolRequestRepository.addPartialPoolNumberPredicate(query, poolNumber);
            }
        }

        log.trace("Exit evaluatePoolNumberParameter");
    }

    private void evaluateCourtLocationParameter(JPAQuery<Tuple> query, String locCode, String poolNumber,
                                                List<String> courts) {
        log.trace("Enter evaluateCourtLocationParameter");

        if (isStringEmpty(locCode)) {
            if (!courts.isEmpty()) {
                log.debug("User has search results filtered based on court access: {}", courts);
                poolRequestRepository.addCourtUserPredicate(query, courts);
            }
        } else {
            log.debug("Court Location Code supplied as Pool Request Search parameter: {}", locCode);
            poolRequestRepository.addCourtLocationPredicate(query, locCode);

            if (isStringEmpty(poolNumber)) {
                log.debug(
                    "No pool number has been specified - using court location to add a partial pool number predicate");
                poolRequestRepository.addPartialPoolNumberPredicate(query, locCode);
            }

        }

        log.trace("Exit evaluateCourtLocationParameter");
    }

    private void evaluateStartDateParameter(JPAQuery<Tuple> query, LocalDate startDate) {
        log.trace("Enter evaluateStartDateParameter");

        if (startDate != null) {
            log.debug("Service Start Date supplied as Pool Request Search parameter: {}", startDate);
            poolRequestRepository.addServiceStartDatePredicate(query, startDate);
        }

        log.trace("Exit evaluateStartDateParameter");
    }

    private void evaluateAdvancedSearchCriteria(JPAQuery<Tuple> query, PoolSearchRequestDto poolSearchRequestDto) {

        log.trace("Enter evaluateAdvancedSearchCriteria");

        List<PoolSearchRequestDto.PoolStatus> poolStatus = poolSearchRequestDto.getPoolStatus();
        List<PoolSearchRequestDto.PoolStage> poolStage = poolSearchRequestDto.getPoolStage();
        List<String> poolTypeCode = poolSearchRequestDto.getPoolType();
        final Boolean isNilPool = poolSearchRequestDto.getIsNilPool();

        if (isListPopulated(poolStatus) && poolStatus.size() < PoolSearchRequestDto.PoolStatus.values().length) {
            log.debug("Pool Status supplied as Pool Request Search parameter: {}", poolStatus);
            poolRequestRepository.addPoolStatusPredicate(query, poolStatus);
        }
        if (isListPopulated(poolStage) && poolStage.size() < PoolSearchRequestDto.PoolStage.values().length) {
            log.debug("Pool Stage supplied as Pool Request Search parameter: {}", poolStage);
            poolRequestRepository.addPoolStagePredicate(query, poolStage);
        }
        if (isListPopulated(poolTypeCode)) {
            log.debug("Pool Type supplied as Pool Request Search parameter: {}", poolTypeCode);
            poolRequestRepository.addPoolTypePredicate(query, poolTypeCode);
        }

        if (isNilPool != null) {
            log.debug("Nil Pool supplied as Pool Request Search parameter: {}", isNilPool);
            poolRequestRepository.addNilPoolPredicate(query, isNilPool);
        }

        log.trace("Exit evaluateAdvancedSearchCriteria");
    }

    private boolean isListPopulated(List<?> list) {
        return list != null && !list.isEmpty();
    }


    @SuppressWarnings("PMD.ExhaustiveSwitchHasDefault")
    private void applySorting(JPAQuery<Tuple> query, PoolSearchRequestDto poolSearchRequestDto) {
        switch (poolSearchRequestDto.getSortColumn()) {
            case POOL_NO -> poolRequestRepository.orderByStringColumn(query, QPoolRequest.poolRequest.poolNumber,
                poolSearchRequestDto.getSortDirection());
            case COURT_NAME -> poolRequestRepository.orderByStringColumn(query, QCourtLocation.courtLocation.name,
                poolSearchRequestDto.getSortDirection());
            case POOL_STAGE -> poolRequestRepository.orderByPoolStage(query, poolSearchRequestDto.getSortDirection());
            case POOL_STATUS -> poolRequestRepository.orderByPoolStatus(query, poolSearchRequestDto.getSortDirection());
            case POOL_TYPE -> poolRequestRepository.orderByStringColumn(query, QPoolType.poolType1.description,
                poolSearchRequestDto.getSortDirection());
            case START_DATE -> poolRequestRepository.orderByDateColumn(query, QPoolRequest.poolRequest.returnDate,
                poolSearchRequestDto.getSortDirection());
            default -> {
                // no sorting required
            }
        }
    }


}
