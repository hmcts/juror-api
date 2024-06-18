package uk.gov.hmcts.juror.api.moj.service;

import java.util.List;

public interface UndeliverableResponseService {
    /**
     * Mark a Juror as Undeliverable with specific code.
     */
    void markAsUndeliverable(List<String> jurorNumber);
}
