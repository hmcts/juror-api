package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Application settings domain.
 */
@Repository
public interface AppSettingsRepository extends ReadOnlyRepository<AppSettings, String>,
    QuerydslPredicateExecutor<AppSettings> {
}
