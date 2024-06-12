package uk.gov.hmcts.juror.api.bureau.domain;


import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Repository interface for read-only reference data.
 *
 * @param <T>  entity type (implicit from JDK5 onwards)
 * @param <I> type of entity's ID (implicit from JDK5 onwards)
 */
@NoRepositoryBean
@Deprecated(forRemoval = true)
public interface ReadOnlyRepository<T, I> extends Repository<T, I> {

    Optional<T> findById(I id);

    Iterable<T> findAll();
}
