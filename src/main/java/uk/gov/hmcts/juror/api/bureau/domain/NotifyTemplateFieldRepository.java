package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link NotifyTemplateField}.
 */
@Repository
public interface NotifyTemplateFieldRepository extends ReadOnlyRepository<NotifyTemplateField, Long>,
    QuerydslPredicateExecutor<NotifyTemplateField> {

    /**
     * Retrieve the Template Fields for a given notify template Id.
     *
     * @param templateId - notify template id
     * @return List of NotifyTemplateField detail objects.
     */
    List<NotifyTemplateField> findByTemplateId(String templateId);
}

