package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.JurorResponseRetrieveRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.JurorResponseRetrieveResponseDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.juror.api.JurorDigitalApplication.JUROR_OWNER;
import static uk.gov.hmcts.juror.api.moj.utils.SecurityUtil.TEAM_LEADER_LEVEL;
import static uk.gov.hmcts.juror.api.moj.utils.converters.ConversionUtils.toProperCase;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorResponseRetrieveServiceImpl implements JurorResponseRetrieveService {
    private static final int SEARCH_RESULT_LIMIT_BUREAU_OFFICER = 100;
    private static final int SEARCH_RESULT_LIMIT_TEAM_LEADER = 250;

    @Autowired
    private JurorResponseCommonRepositoryMod jurorResponseRepository;

    @Override
    @Transactional(readOnly = true)
    public JurorResponseRetrieveResponseDto retrieveJurorResponse(JurorResponseRetrieveRequestDto request,
                                                                  BureauJwtPayload payload) {
        boolean isBureauUser = payload.getOwner().equalsIgnoreCase(JUROR_OWNER);
        boolean isTeamLeader = payload.getStaff().getRank().equals(TEAM_LEADER_LEVEL);

        // validations
        validateRequest(request, isBureauUser, isTeamLeader);

        // retrieve results from the repository for the given criteria
        List<Tuple> queryResponse = retrieveJurorResponseDetails(request, isTeamLeader);

        // map query response to dto response
        return mapQueryResponseToDtoResponse(queryResponse);
    }

    private JurorResponseRetrieveResponseDto mapQueryResponseToDtoResponse(List<Tuple> tuples) {
        List<JurorResponseRetrieveResponseDto.JurorResponseDetails> records = new ArrayList<>();

        for (Tuple tuple : tuples) {
            JurorResponseRetrieveResponseDto.JurorResponseDetails dataRecord =
                JurorResponseRetrieveResponseDto.JurorResponseDetails.builder()
                    .jurorNumber(tuple.get(0, String.class))
                    .firstName(tuple.get(1, String.class))
                    .lastName(tuple.get(2, String.class))
                    .postcode(tuple.get(3, String.class))
                    .replyStatus(tuple.get(4, ProcessingStatus.class))
                    .dateReceived(tuple.get(5, LocalDateTime.class))
                    .officerAssigned(tuple.get(6, String.class))
                    .poolNumber(tuple.get(7, String.class))
                    .courtName(toProperCase(requireNonNull(tuple.get(8, String.class))))
                    .replyType(tuple.get(9, String.class))
                    .build();
            records.add(dataRecord);
        }

        return JurorResponseRetrieveResponseDto.builder().records(records).recordCount(tuples.size()).build();
    }

    private void validateRequest(JurorResponseRetrieveRequestDto request, boolean isBureauUser, boolean isTeamLeader) {
        if (!isBureauUser) {
            throw new MojException.Forbidden("This service is for Bureau users only",
                null);
        } else if (!noTeamLeaderFilters(request) && !isTeamLeader) {
            throw new MojException.Forbidden("Advanced search is only available to team leaders",
                null);
        } else if (noBureauOfficerFilters(request) && (!isTeamLeader || noTeamLeaderFilters(request))) {
            throw new MojException.BadRequest("No search filters supplied", null);
        }
    }

    private boolean noBureauOfficerFilters(JurorResponseRetrieveRequestDto request) {
        return request.getJurorNumber() == null
            && request.getLastName() == null
            && request.getPoolNumber() == null;
    }

    private boolean noTeamLeaderFilters(JurorResponseRetrieveRequestDto request) {
        return request.getOfficerAssigned() == null
            && request.getProcessingStatus() == null
            && (request.getIsUrgent() == null || request.getIsUrgent().equals(FALSE));
    }

    private List<Tuple> retrieveJurorResponseDetails(JurorResponseRetrieveRequestDto request, boolean isTeamLeader) {
        return jurorResponseRepository.retrieveJurorResponseDetails(request, isTeamLeader,
            getResultsLimit(isTeamLeader));
    }

    private int getResultsLimit(boolean isTeamLeader) {
        return isTeamLeader ? SEARCH_RESULT_LIMIT_TEAM_LEADER : SEARCH_RESULT_LIMIT_BUREAU_OFFICER;
    }
}
