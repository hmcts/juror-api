package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.letter.ConfirmationLetter;
import uk.gov.hmcts.juror.api.moj.repository.ConfirmationLetterRepository;

/**
 * When a citizen has responded to a summons, they should be placed in the queue to receive a confirmation letter
 * The queue is handled by inserting records in to a database table (CONFIRM_LETT) then overnight batch jobs run to
 * process the relevant records.
 * For deferred jurors, they may have already received a confirmation letter for their previous summons and response,
 * so when they are added to a new pool, then  their previous record will be updated
 * (reset the print flag and date printed value to null)
 */
@Service
@Slf4j
@Qualifier("confirmationLetterServiceImpl")
public class ConfirmationLetterServiceImpl extends LetterServiceImpl<ConfirmationLetter, ConfirmationLetterRepository> {

    public ConfirmationLetterServiceImpl(@NonNull ConfirmationLetterRepository letterRepository) {
        super(letterRepository);
    }

    @Override
    public ConfirmationLetter createNewLetter(String owner, String jurorNumber) {
        ConfirmationLetter confirmationLetter = new ConfirmationLetter();
        confirmationLetter.setOwner(owner);
        confirmationLetter.setJurorNumber(jurorNumber);
        return confirmationLetter;
    }

}
