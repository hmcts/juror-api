package uk.gov.hmcts.juror.api.bureau.domain;


import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Repository interface for read-only reference data.
 *
 * @param <T>  entity type (implicit from JDK5 onwards)
 * @param <ID> type of entity's ID (implicit from JDK5 onwards)
 */
@NoRepositoryBean
public interface ReadOnlyRepository<T, ID> extends Repository<T, ID> {

    Optional<T> findById(ID id);

    Iterable<T> findAll();
}
