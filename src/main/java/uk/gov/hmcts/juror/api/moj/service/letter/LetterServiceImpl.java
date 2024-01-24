package uk.gov.hmcts.juror.api.moj.service.letter;

import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.juror.api.moj.domain.letter.Letter;
import uk.gov.hmcts.juror.api.moj.domain.letter.LetterId;
import uk.gov.hmcts.juror.api.moj.repository.LetterRepository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public abstract class LetterServiceImpl<T extends Letter, R extends LetterRepository<T>> implements LetterService<T> {

    @NonNull
    protected final R letterRepository;

    /**
     * Persist an instance of a Letter - either a new instantiation if this letter has not previously been
     * printed, or, potentially retrieve and update a previous record if the letter requires re-printing.
     */
    @Transactional
    @Override
    public void enqueueLetter(T letter) {
        letterRepository.save(letter);
    }

    /**
     * Either create a new instantiation of a letter, if this letter has not previously been printed, or, potentially
     * retrieve a previous record to be updated if the letter requires re-printing.
     *
     * @param owner       3 digit numerical string to identify whether the record is owned by a specific court or
     *                    the bureau
     * @param jurorNumber unique identifier for a citizen being summonsed for jury service
     */
    @Override
    public T getLetterToEnqueue(String owner, String jurorNumber) {
        return getLetter(new LetterId(owner, jurorNumber));
    }

    /**
     * Create a new letter and add it to the queue only when no existing letter is present for the given
     * owner and juror number (LetterId composite key). If any existing letter record is present for the given
     * owner and juror number, the record remains unchanged.
     *
     * @param owner       3-digit numeric string representing the court location this record belongs to
     * @param jurorNumber 9-digit numeric string representing a juror record
     */
    @Override
    @Transactional
    public void enqueueNewLetter(String owner, String jurorNumber) {
        LetterId letterId = new LetterId(owner, jurorNumber);

        Optional<T> letterOpt = findExistingRecord(letterId);

        if (letterOpt.isPresent()) {
            log.trace(String.format("Existing record found for juror: %s, no new letter created", jurorNumber));
        } else {
            log.trace(String.format(
                "No existing record found for juror: %s, creating a new record",
                letterId.getJurorNumber()
            ));
            letterRepository.save(createNewLetter(letterId.getOwner(), letterId.getJurorNumber()));
        }
    }

    public abstract T createNewLetter(String owner, String jurorNumber);

    private Optional<T> findExistingRecord(LetterId letterId) {
        log.trace(String.format("Check if a letter has previously been sent to juror: %s", letterId.getJurorNumber()));
        return letterRepository.findById(letterId);
    }

    private T getLetter(LetterId letterId) {
        T letter;
        Optional<T> letterOpt = findExistingRecord(letterId);

        if (letterOpt.isPresent()) {
            log.trace(String.format(
                "Existing record found for juror: %s, updating the existing record",
                letterId.getJurorNumber()
            ));
            letter = letterOpt.get();
            letter.setPrinted(null);
            letter.setDatePrinted(null);
        } else {
            log.trace(String.format(
                "No existing record found for juror: %s, creating a new record",
                letterId.getJurorNumber()
            ));
            letter = createNewLetter(letterId.getOwner(), letterId.getJurorNumber());
        }

        return letter;
    }

}
