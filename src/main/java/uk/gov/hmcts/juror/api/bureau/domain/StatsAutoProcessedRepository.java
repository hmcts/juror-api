package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository for {@link StatsAutoProcessed}.
 */
@Repository
public interface StatsAutoProcessedRepository extends CrudRepository<StatsAutoProcessed, Date> {

    List<StatsAutoProcessed> findByProcessedDateBetween(
        Date processedDateStart,
        Date processedDateEnd);
}
