package uk.gov.hmcts.juror.api.bureau.exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception wrapper for Optimistic Locking errors thrown by persistence.
 */
@ResponseStatus(HttpStatus.CONFLICT)//409
public class BureauOptimisticLockingException extends RuntimeException {
    public BureauOptimisticLockingException(OptimisticLockingFailureException olfe) {
        super(olfe);
    }
}
