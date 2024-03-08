package uk.gov.hmcts.juror.api.moj.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class RepositoryUtils {
    private RepositoryUtils() {

    }

    @Transactional(readOnly = true)
    public static <T, I extends Serializable> T retrieveFromDatabase(I id, ReadOnlyRepository<T, I> repository)
        throws IllegalArgumentException {
        Optional<T> opt = repository.findById(id);
        if (opt.isPresent()) {
            log.debug("Database record exists for the ID: " + id);
            return opt.get();
        } else {
            throw new IllegalArgumentException("Unable to find a valid record in the database for: " + id);
        }
    }

    @Transactional(readOnly = true)
    public static <T> T retrieveFromDatabase(String id, CrudRepository<T, String> repository)
        throws IllegalArgumentException {
        Optional<T> opt = repository.findById(id);
        return unboxOptionalRecord(opt, id);
    }

    @Transactional(readOnly = true)
    public static <T, I extends Serializable> List<T> retrieveAllRecordsFromDatabase(
        ReadOnlyRepository<T, I> repository)
        throws IllegalArgumentException {
        Iterable<T> rows = repository.findAll();
        List<T> rowsList = new ArrayList<>();
        rows.forEach(rowsList::add);
        return rowsList;
    }

    /**
     * Generic utility method to safely unbox a database query result where the returned object is wrapped in an
     * Optional container.
     *
     * @param repoResult database query result wrapped in an optional container
     * @param id         Unique ID or description of the expected result object
     * @return the unboxed object from the database result
     */
    public static <T> T unboxOptionalRecord(Optional<T> repoResult, String id) {
        if (repoResult.isPresent()) {
            log.debug("Database record exists for the ID: " + id);
            return repoResult.get();
        }
        throw new MojException.NotFound(
            String.format("Unable to find a valid record in the database for %s", id),
            null
        );
    }
}
