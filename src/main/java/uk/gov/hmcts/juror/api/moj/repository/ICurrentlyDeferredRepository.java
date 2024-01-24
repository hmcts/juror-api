package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

import java.util.List;

/**
 * Custom Repository definition for the Currently Deferred entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface ICurrentlyDeferredRepository {

    List<Tuple> getDeferralsByCourtLocationCode(BureauJWTPayload payload, String courtLocation);

}
