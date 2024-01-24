package uk.gov.hmcts.juror.api.moj.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import uk.gov.hmcts.juror.api.moj.domain.letter.Letter;
import uk.gov.hmcts.juror.api.moj.domain.letter.LetterId;

/**
 * Abstract repository interface for Letter related entities.
 *
 * @param <T> Generic entity extended from the abstract Letter base class
 */
@NoRepositoryBean
public interface LetterRepository<T extends Letter> extends CrudRepository<T, LetterId> {

}
