package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateFieldMod;

import java.util.List;

/**
 * Repository for {@link NotifyTemplateFieldMod}.
 */
@Repository
public interface NotifyTemplateFieldRepositoryMod extends ReadOnlyRepository<NotifyTemplateFieldMod, Integer>,
    QuerydslPredicateExecutor<NotifyTemplateFieldMod> {

    /**
     * Retrieve the Template Fields for a given notify template Id.
     *
     * @param templateId - notify template id
     * @return List of NotifyTemplateFieldMod detail objects.
     */
    List<NotifyTemplateFieldMod> findByTemplateId(String templateId);

    //TODO tmp
    @Query(value = "SELECT distinct template_id FROM juror_mod.notify_template_field",
        nativeQuery = true)
    List<String> tmpQuery();
}

