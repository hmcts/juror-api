package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorRecordFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import java.util.List;

/**
 * Custom Repository definition for the Juror entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IJurorRepository {

    Juror findByJurorNumberAndIsActiveAndCourt(String jurorNumber, boolean isActive, CourtLocation locCode);

    List<Juror> findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(List<String> jurorNumbers,
                                                                               boolean isActive, String poolNumber,
                                                                               CourtLocation court,
                                                                               List<Integer> status);

    JPAQuery<Tuple> fetchFilteredJurorRecords(JurorRecordFilterRequestQuery query);
}
