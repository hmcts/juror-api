package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;

import java.util.List;

@Repository
public interface VotersLocPostcodeTotalsRepository extends ReadOnlyRepository<VotersLocPostcodeTotals,
    VotersLocPostcodeTotals.VotersLocPostcodeTotalsId> {

    List<VotersLocPostcodeTotals> findByLocCode(String locCode);

}

