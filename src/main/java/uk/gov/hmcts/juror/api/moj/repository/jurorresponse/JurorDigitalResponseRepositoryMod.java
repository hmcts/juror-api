package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;

@Repository
public interface JurorDigitalResponseRepositoryMod extends JurorResponseRepositoryMod<DigitalResponse>,
    QuerydslPredicateExecutor<DigitalResponse> {
    DigitalResponse findByJurorNumberAndReplyType(String jurorNumber, ReplyType replyType);

    default DigitalResponse findByJurorNumber(String jurorNumber) {
        return findByJurorNumberAndReplyType(jurorNumber, new ReplyType("Digital", null));
    }

}
