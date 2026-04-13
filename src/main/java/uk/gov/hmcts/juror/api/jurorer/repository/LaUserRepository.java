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

    // there could be multiple users with the same email address but different local authorities
    List<LaUser> findByUsernameIgnoreCase(String username);

    // there should only be one user with the same email address and local authority
    Optional<LaUser> findByUsernameIgnoreCaseAndLocalAuthority(String username, LocalAuthority localAuthority);

    List<LaUser> findByLocalAuthority(LocalAuthority localAuthority);

    Optional<LaUser> findFirstByLocalAuthorityAndLastLoggedInNotNullOrderByLastLoggedInDesc(
                                                                                LocalAuthority localAuthority);

}
