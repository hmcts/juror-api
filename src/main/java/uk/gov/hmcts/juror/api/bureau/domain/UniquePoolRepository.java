package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link UniquePool} entities.
 *
 * @since JDB-2042
 */
@Repository
@Deprecated(forRemoval = true)
public interface UniquePoolRepository extends ReadOnlyRepository<UniquePool, String> {
}
