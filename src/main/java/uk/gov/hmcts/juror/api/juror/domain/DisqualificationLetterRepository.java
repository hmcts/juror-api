package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link DisqualificationLetter}.
 */
@Repository
public interface DisqualificationLetterRepository extends JpaRepository<DisqualificationLetter, String> {

    List<DisqualificationLetter> findByJurorNumber(String jurorNumber);
}
