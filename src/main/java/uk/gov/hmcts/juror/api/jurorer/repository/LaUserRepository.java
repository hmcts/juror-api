package uk.gov.hmcts.juror.api.jurorer.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;

import java.util.List;
import java.util.Optional;

@Repository
public interface LaUserRepository extends CrudRepository<LaUser, String>, QuerydslPredicateExecutor<LaUser> {

    Optional<LaUser> findByUsername(String username);

    List<LaUser> findByLocalAuthority(LocalAuthority localAuthority);

    Optional<LaUser> findFirstByLocalAuthorityAndLastLoggedInNotNullOrderByLastLoggedInDesc(
                                                                                LocalAuthority localAuthority);

}
