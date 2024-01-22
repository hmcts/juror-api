package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.letter.DisqualificationLetterMod;
import uk.gov.hmcts.juror.api.moj.repository.DisqualifyLetterModRepository;


@Slf4j
@Service
@Qualifier("DisqualificationLetterServiceImpl")
public class DisqualificationLetterServiceImpl extends LetterServiceImpl<DisqualificationLetterMod,
    DisqualifyLetterModRepository> {

    public DisqualificationLetterServiceImpl(@NonNull DisqualifyLetterModRepository letterRepository) {
        super(letterRepository);
    }

    @Override
    public DisqualificationLetterMod createNewLetter(String owner, String jurorNumber) {
        DisqualificationLetterMod DisqualificationLetterMod = new DisqualificationLetterMod();
        DisqualificationLetterMod.setOwner(owner);
        DisqualificationLetterMod.setJurorNumber(jurorNumber);
        return DisqualificationLetterMod;
    }

}
