package uk.gov.hmcts.juror.api.moj.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class JurorPoolServiceImpl implements JurorPoolService {

    private final PoolRequestRepository poolRequestRepository;
    private final JurorPoolRepository jurorPoolRepository;

    @Override
    public PoolRequest getPoolRequest(String poolNumber) {
        return poolRequestRepository.findByPoolNumber(poolNumber)
            .orElseThrow(() -> new MojException.NotFound(
                "Pool not found: " + poolNumber, null));
    }

    @Override
    public boolean hasPoolWithLocCode(String jurorNumber, List<String> locCodes) {
        return jurorPoolRepository.hasPoolWithLocCode(jurorNumber, locCodes);
    }

    @Override
    public PaginatedList<JurorDetailsDto> search(JurorPoolSearch search) {
        PaginatedList<JurorDetailsDto> jurorDetailsDtoList =
            jurorPoolRepository.findJurorPoolsBySearch(search, SecurityUtil.getActiveOwner(),
                jurorPoolJPQLQuery -> jurorPoolJPQLQuery.where(
                    QJurorPool.jurorPool.status.status.eq(search.getJurorStatus())),
                jurorPool -> {
                    Juror juror = jurorPool.getJuror();
                    return JurorDetailsDto.builder()
                        .jurorNumber(jurorPool.getJurorNumber())
                        .poolNumber(jurorPool.getPoolNumber())
                        .firstName(juror.getFirstName())
                        .lastName(juror.getLastName())
                        .postCode(juror.getPostcode())
                        .completionDate(juror.getCompletionDate())
                        .build();
                },
                500L);

        if (jurorDetailsDtoList == null || jurorDetailsDtoList.isEmpty()) {
            throw new MojException.NotFound("No juror pools found that meet your search criteria.", null);
        }

        return jurorDetailsDtoList;
    }

    @Override
    public JurorPool getJurorPoolFromUser(String jurorNumber) {
        return Optional.ofNullable(jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                jurorNumber, true, SecurityUtil.getActiveOwner()))
            .orElseThrow(() -> new MojException.NotFound("Juror not found: " + jurorNumber, null));
    }

    @Override
    public JurorPool getLastJurorPoolForJuror(String locCode, String jurorNumber) {
        List<JurorPool> jurorPools = getJurorPools(locCode, jurorNumber).stream().sorted(
            Comparator.comparing(JurorPool::getDateCreated).reversed()).toList();

        Optional<JurorPool> jurorPool = jurorPools.stream().filter(JurorPool::getIsActive).findFirst();
        if (jurorPool.isPresent()) {
            return jurorPool.orElse(null);
        } else if (!jurorPools.isEmpty()) {
            return jurorPools.get(0);
        }
        throw new MojException.NotFound("Juror not found: " + jurorNumber, null);
    }

    private List<JurorPool> getJurorPools(String locCode, String jurorNumber) {
        return jurorPoolRepository.findByPoolCourtLocationLocCodeAndJurorJurorNumber(locCode, jurorNumber);
    }
}
