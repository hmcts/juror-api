package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.AppSetting;

@Repository
public interface AppSettingRepository extends ReadOnlyRepository<AppSetting, String>,
    QuerydslPredicateExecutor<AppSetting> {

}
