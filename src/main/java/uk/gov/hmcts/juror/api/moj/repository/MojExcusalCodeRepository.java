package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;

import java.util.List;

/**
 * Excusal Code Repository interface.
 */
@Repository
public interface MojExcusalCodeRepository extends ReadOnlyRepository<ExcusalCode, String> {

    List<ExcusalCode> findByForDeferral(boolean forDeferral);

    List<ExcusalCode> findByForExcusal(boolean forExcusal);

}
