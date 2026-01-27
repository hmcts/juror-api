package uk.gov.hmcts.juror.api.jurorer.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;

@Repository
public interface LaUserRepository extends CrudRepository<LaUser, String>, QuerydslPredicateExecutor<LaUser> {

    LaUser findByUsername(String username);

    boolean existsByUsername(String username);
}
