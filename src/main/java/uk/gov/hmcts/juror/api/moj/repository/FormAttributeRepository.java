package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.FormAttribute;

/**
 * Repository for {@link FormAttribute}.
 */
@Repository
public interface FormAttributeRepository extends JpaRepository<FormAttribute, String>,
    QuerydslPredicateExecutor<FormAttribute> {

}
