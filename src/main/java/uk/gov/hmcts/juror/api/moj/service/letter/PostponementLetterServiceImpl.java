package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.letter.PostponementLetter;
import uk.gov.hmcts.juror.api.moj.repository.letter.PostponementLetterRepository;

@Slf4j
@Service
@Qualifier("postponementLetterServiceImpl")
public class PostponementLetterServiceImpl extends LetterServiceImpl<PostponementLetter, PostponementLetterRepository> {

    public PostponementLetterServiceImpl(@NonNull PostponementLetterRepository letterRepository) {
        super(letterRepository);
    }

    @Override
    public PostponementLetter createNewLetter(String owner, String jurorNumber) {
        PostponementLetter postponementLetter = new PostponementLetter();
        postponementLetter.setOwner(owner);
        postponementLetter.setJurorNumber(jurorNumber);
        return postponementLetter;
    }

}
