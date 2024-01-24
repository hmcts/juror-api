package uk.gov.hmcts.juror.api.moj.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PoolRequestUtils {

    /**
     * Retrieve the active copy of a pool record (where read_only = false) from a database query.
     *
     * @param poolRequestRepository JPA interface to the database to generate and execute SQL queries
     * @param poolNumber            9-digit numeric string to identify a pool request
     * @return a single PoolRequest object
     */
    public static PoolRequest getActivePoolRecord(PoolRequestRepository poolRequestRepository, String poolNumber) {
        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findByPoolNumber(poolNumber);
        log.debug("Retrieving active pool record for pool number {}", poolNumber);
        if (!poolRequestOpt.isPresent()) {
            throw new MojException.NotFound(
                String.format("No Pool Record found for Pool Number: %s", poolNumber),
                null
            );
        }

        log.debug("retrieved pool record for pool number {}", poolNumber);
        return poolRequestOpt.get();
    }

}
