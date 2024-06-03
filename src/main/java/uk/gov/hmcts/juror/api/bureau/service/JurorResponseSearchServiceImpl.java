package uk.gov.hmcts.juror.api.bureau.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorResponseSearchRequest;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorResponseSearchResults;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetailQueries;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.repository.JurorDetailRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of {@link JurorResponseSearchService}.
 */
@Slf4j
@Service
public class JurorResponseSearchServiceImpl implements JurorResponseSearchService {

    private final AppSettingService appSettingService;
    private final JurorDetailRepositoryMod bureauJurorDetailRepository;
    private final BureauTransformsService bureauTransformsService;

    @Autowired
    public JurorResponseSearchServiceImpl(final AppSettingService appSettingService,
                                          final JurorDetailRepositoryMod bureauJurorDetailRepository,
                                          final BureauTransformsService bureauTransformsService) {
        Assert.notNull(appSettingService, "AppSettingService cannot be null.");
        Assert.notNull(bureauJurorDetailRepository, "JurorDetailRepositoryMod cannot be null.");
        Assert.notNull(bureauTransformsService, "BureauTransformsService cannot be null.");
        this.appSettingService = appSettingService;
        this.bureauJurorDetailRepository = bureauJurorDetailRepository;
        this.bureauTransformsService = bureauTransformsService;
    }

    @Override
    @Transactional(readOnly = true)
    public JurorResponseSearchResults searchForResponses(JurorResponseSearchRequest searchRequest,
                                                         boolean isTeamLeader) {

        if (noBureauOfficerFilters(searchRequest) && (!isTeamLeader || noTeamLeaderFilters(searchRequest))) {
            log.info("No search filters supplied, returning empty results list");
            return JurorResponseSearchResults.builder().responses(Collections.emptyList()).build();
        }

        final SearchResults bureauJurorDetails = retrieveResults(searchRequest, isTeamLeader);

        List<BureauResponseSummaryDto> results = bureauTransformsService.convertToDtos(bureauJurorDetails.getResults());
        return JurorResponseSearchResults.builder().responses(results)
            .meta(JurorResponseSearchResults.BureauSearchMetadata.builder()
                .max(bureauJurorDetails.getMaximumNumberOfMatches()).total(
                    bureauJurorDetails.getTotalNumberOfMatches()).build()).build();

    }

    private SearchResults retrieveResults(JurorResponseSearchRequest searchRequest, boolean isTeamLeader) {

        final int resultsLimit = getResultsLimit(isTeamLeader);

        final Predicate queryPredicate = toQueryPredicate(searchRequest, isTeamLeader);
        final Page<ModJurorDetail> bureauJurorDetails = bureauJurorDetailRepository.findAll(
            queryPredicate,
            PageRequest.of(
                0,
                resultsLimit,
                Sort.Direction.ASC,
                "dateReceived"
            )
        );

        return SearchResults.builder().results(bureauJurorDetails.getContent()).maximumNumberOfMatches(resultsLimit)
            .totalNumberOfMatches(
                bureauJurorDetails.getTotalElements()).build();
    }

    /**
     * Gets the configured results limit (or the default limit value) for a search.
     *
     * @param isTeamLeader whether the search is a team leader search
     * @return results limit
     */
    private Integer getResultsLimit(boolean isTeamLeader) {
        if (isTeamLeader) {
            final Integer configuredValue = appSettingService.getTeamLeaderSearchResultLimit();
            return configuredValue != null && configuredValue > 0
                ?
                configuredValue
                :
                    250;
        }
        final Integer configuredValue = appSettingService.getBureauOfficerSearchResultLimit();
        return configuredValue != null && configuredValue > 0
            ?
            configuredValue
            :
                1000;
    }

    /**
     * Converts a search request to a QueryDSL Predicate.
     *
     * @param searchRequestDto the search request to transform
     * @param isTeamLeader     is this a team leader search?
     * @return QueryDSL Predicate representing the filters specified in the search
     */
    private static Predicate toQueryPredicate(JurorResponseSearchRequest searchRequestDto, boolean isTeamLeader) {

        final List<BooleanExpression> filters = createFilters(searchRequestDto, isTeamLeader);

        BooleanExpression combinedFilter = filters.get(0);
        final int numberOfParameters = filters.size();
        if (numberOfParameters > 1) {
            for (int i = 1;
                 i < numberOfParameters;
                 i++) {
                combinedFilter = combinedFilter.and(filters.get(i));
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Searching using filter: {}", combinedFilter);
        }
        return combinedFilter;
    }

    /**
     * Creates QueryDSL expressions based on a search DTO.
     *
     * @param searchRequestDto the search request to transform
     * @param isTeamLeader     is this a team leader search?
     * @return QueryDSL BooleanExpressions representing the filters specified in the search
     */
    private static List<BooleanExpression> createFilters(JurorResponseSearchRequest searchRequestDto,
                                                         boolean isTeamLeader) {
        final List<BooleanExpression> filters = new LinkedList<>();

        filters.addAll(bureauOfficerFilters(searchRequestDto));
        if (isTeamLeader) {
            log.debug("Search request by team leader");
            filters.addAll(teamLeaderFilters(searchRequestDto));
        }

        return filters;
    }

    /**
     * Creates QueryDSL expressions for bureau officer.
     *
     * @param searchRequestDto the search request to transform
     * @return QueryDSL BooleanExpressions representing the bureau officer filters specified in the search
     */
    private static List<BooleanExpression> bureauOfficerFilters(JurorResponseSearchRequest searchRequestDto) {
        final List<BooleanExpression> filters = new LinkedList<>();

        if (searchRequestDto.getJurorNumber() != null) {
            filters.add(BureauJurorDetailQueries.byJurorNumber(searchRequestDto.getJurorNumber()));
        }

        if (searchRequestDto.getLastName() != null) {
            filters.add(BureauJurorDetailQueries.byLastName(searchRequestDto.getLastName()));
        }

        if (searchRequestDto.getPostCode() != null) {
            filters.add(BureauJurorDetailQueries.byPostcode(searchRequestDto.getPostCode()));
        }

        if (searchRequestDto.getPoolNumber() != null) {
            filters.add(BureauJurorDetailQueries.byPoolNumber(searchRequestDto.getPoolNumber()));
        }


        return filters;
    }

    /**
     * Creates QueryDSL expressions for team leader.
     *
     * @param searchRequestDto the search request to transform
     * @return QueryDSL BooleanExpressions representing the team leader filters specified in the search
     */
    private static List<BooleanExpression> teamLeaderFilters(JurorResponseSearchRequest searchRequestDto) {
        final List<BooleanExpression> filters = new LinkedList<>();
        if (Boolean.TRUE.equals(searchRequestDto.getUrgentsOnly())) {
            filters.add(BureauJurorDetailQueries.urgentsOnly());
        }
        if (searchRequestDto.getStaffAssigned() != null) {
            filters.add(BureauJurorDetailQueries.byMemberOfStaffAssigned(searchRequestDto.getStaffAssigned()));
        }
        if (searchRequestDto.getStatus() != null && !searchRequestDto.getStatus().isEmpty()) {
            filters.add(BureauJurorDetailQueries.byStatus(searchRequestDto.getStatus()));
        }
        if (searchRequestDto.getCourtCode() != null && !searchRequestDto.getCourtCode().isEmpty()) {
            filters.add(BureauJurorDetailQueries.byCourtCode(searchRequestDto.getCourtCode()));
        }
        return filters;
    }

    private boolean noBureauOfficerFilters(JurorResponseSearchRequest searchRequest) {
        return searchRequest.getJurorNumber() == null && searchRequest.getLastName() == null
            && searchRequest.getPostCode() == null && searchRequest.getPoolNumber() == null;
    }

    private boolean noTeamLeaderFilters(JurorResponseSearchRequest searchRequest) {
        return searchRequest.getStaffAssigned() == null && searchRequest.getStatus() == null
            && searchRequest.getUrgentsOnly() == null && searchRequest.getCourtCode() == null;
    }

    @Builder
    @Data
    private static class SearchResults {
        private final List<ModJurorDetail> results;
        private final Integer maximumNumberOfMatches;
        private final Long totalNumberOfMatches;
    }
}
