package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;

@Repository
@Transactional
public interface JurorPaperResponseRepositoryMod extends JurorResponseRepositoryMod<PaperResponse> {
    PaperResponse findByJurorNumberAndReplyType(String jurorNumber, ReplyType replyType);

    default PaperResponse findByJurorNumber(String jurorNumber) {
        return findByJurorNumberAndReplyType(jurorNumber, new ReplyType("Paper", null));
    }
}
