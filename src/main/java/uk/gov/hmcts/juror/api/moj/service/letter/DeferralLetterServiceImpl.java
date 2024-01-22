package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.letter.DeferralLetter;
import uk.gov.hmcts.juror.api.moj.repository.DeferralLetterRepository;


@Slf4j
@Service
@Qualifier("deferralLetterServiceImpl")
public class DeferralLetterServiceImpl extends LetterServiceImpl<DeferralLetter, DeferralLetterRepository> {

    public DeferralLetterServiceImpl(@NonNull DeferralLetterRepository letterRepository) {
        super(letterRepository);
    }

    @Override
    public DeferralLetter createNewLetter(String owner, String jurorNumber) {
        DeferralLetter deferralLetter = new DeferralLetter();
        deferralLetter.setOwner(owner);
        deferralLetter.setJurorNumber(jurorNumber);
        return deferralLetter;
    }

}
