package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Application settings domain.
 */
@Repository
public interface AppSettingRepository extends ReadOnlyRepository<AppSetting, String>,
    QuerydslPredicateExecutor<AppSetting> {
}
