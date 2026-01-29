package uk.gov.hmcts.juror.api.jurorer.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;

import java.util.Optional;

@Repository
public interface LocalAuthorityRepository extends CrudRepository<LocalAuthority, String>,
                                                        QuerydslPredicateExecutor<LocalAuthority> {

    Optional<LocalAuthority> findByLaCode(String laCode);

}
