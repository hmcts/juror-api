package uk.gov.hmcts.juror.api.moj.service.letter;

import uk.gov.hmcts.juror.api.moj.domain.letter.Letter;

public interface LetterService<L extends Letter> {

    String PAPER_REPLY_METHOD = "Paper";
    String DIGITAL_REPLY_METHOD = "Digital";

    void enqueueLetter(L letter);

    L getLetterToEnqueue(String owner, String jurorNumber);

    void enqueueNewLetter(String owner, String jurorNumber);
}
