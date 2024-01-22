package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.letter.CertLetter;
import uk.gov.hmcts.juror.api.moj.repository.letter.CertLetterRepository;


@Slf4j
@Service
@Qualifier("certLetterServiceImpl")
public class CertLetterServiceImpl extends LetterServiceImpl<CertLetter, CertLetterRepository> {

    public CertLetterServiceImpl(@NonNull CertLetterRepository letterRepository) {
        super(letterRepository);
    }

    @Override
    public CertLetter createNewLetter(String owner, String jurorNumber) {
        CertLetter certLetter = new CertLetter();
        certLetter.setOwner(owner);
        certLetter.setJurorNumber(jurorNumber);
        return certLetter;
    }

}
