package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link NotifyTemplateMapping}.
 */
@Repository

public interface NotifyTemplateMappingRepository extends CrudRepository<NotifyTemplateMapping, String>,
    QuerydslPredicateExecutor<NotifyTemplateMapping> {

    /**
     * Find template by templateName.
     *
     * @return NotifyTemplateMapping for the given template name.
     */
    //List<NotifyTemplateMapping> findByTemplateName(String templateName);

    NotifyTemplateMapping findByTemplateName(String templateName);
}
