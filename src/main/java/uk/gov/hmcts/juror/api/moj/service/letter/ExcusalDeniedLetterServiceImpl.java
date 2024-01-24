package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.letter.ExcusalDeniedLetterMod;
import uk.gov.hmcts.juror.api.moj.repository.ExcuseDeniedLetterRepository;

@Slf4j
@Service
@Qualifier("excusalDeniedLetterServiceImpl")
public class ExcusalDeniedLetterServiceImpl extends LetterServiceImpl<ExcusalDeniedLetterMod,
    ExcuseDeniedLetterRepository> {

    public ExcusalDeniedLetterServiceImpl(@NonNull ExcuseDeniedLetterRepository excuseDeniedLetterRepository) {
        super(excuseDeniedLetterRepository);
    }

    @Override
    public ExcusalDeniedLetterMod createNewLetter(String owner, String jurorNumber) {
        ExcusalDeniedLetterMod excusalDeniedLetterMod = new ExcusalDeniedLetterMod();
        excusalDeniedLetterMod.setOwner(owner);
        excusalDeniedLetterMod.setJurorNumber(jurorNumber);
        return excusalDeniedLetterMod;
    }
}
