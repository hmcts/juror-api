package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Application settings domain.
 */
@Repository
@Deprecated(forRemoval = true)
public interface AppSettingsRepository extends ReadOnlyRepository<AppSettings, String>,
    QuerydslPredicateExecutor<AppSettings> {
}
