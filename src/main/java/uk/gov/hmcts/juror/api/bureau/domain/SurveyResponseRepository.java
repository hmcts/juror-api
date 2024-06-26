package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository for {@link SurveyResponse}.
 */
@Repository
public interface SurveyResponseRepository extends CrudRepository<SurveyResponse, SurveyResponseKey> {

    List<SurveyResponse> findBySurveyResponseDateBetween(
        Date summonsMonthStart,
        Date summonsMonthEnd);
}
