package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateMappingMod;

/**
 * Repository for {@link NotifyTemplateMappingMod}.
 */
@Repository
public interface NotifyTemplateMappingRepositoryMod extends CrudRepository<NotifyTemplateMappingMod, Integer>,
    QuerydslPredicateExecutor<NotifyTemplateMappingMod> {

    /**
     * Find template by templateName.
     *
     * @return NotifyTemplateMapping for the given template name.
     */
    NotifyTemplateMappingMod findByTemplateName(String templateName);
}
