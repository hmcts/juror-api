package uk.gov.hmcts.juror.api.moj.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.List;

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
}
