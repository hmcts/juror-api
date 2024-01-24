package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link JurorCommsPrintFiles}.
 */
@Repository
public interface JurorCommsPrintFilesRepository extends ReadOnlyRepository<JurorCommsPrintFiles,
    JurorCommsPrintFilesKey>,
    QuerydslPredicateExecutor<JurorCommsPrintFiles> {
}
