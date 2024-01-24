package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.letter.ExcusalLetterMod;
import uk.gov.hmcts.juror.api.moj.repository.ExcuseLetterRepository;

@Slf4j
@Service
@Qualifier("excusalLetterServiceImpl")
public class ExcusalLetterServiceImpl extends LetterServiceImpl<ExcusalLetterMod,
    ExcuseLetterRepository> {

    public ExcusalLetterServiceImpl(@NonNull ExcuseLetterRepository excuseLetterRepository) {
        super(excuseLetterRepository);
    }

    @Override
    public ExcusalLetterMod createNewLetter(String owner, String jurorNumber) {
        ExcusalLetterMod excusalLetterMod = new ExcusalLetterMod();
        excusalLetterMod.setOwner(owner);
        excusalLetterMod.setJurorNumber(jurorNumber);
        return excusalLetterMod;
    }
}
