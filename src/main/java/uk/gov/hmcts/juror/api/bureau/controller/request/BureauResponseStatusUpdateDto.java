package uk.gov.hmcts.juror.api.bureau.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

/**
 * Request wrapper object for Bureau Officer's updating the status of a Juror response.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BureauResponseStatusUpdateDto {
    private String jurorNumber;
    private ProcessingStatus status;
    /**
     * Version number of the record as checked out in the UI by the officer. Optimistic lock.
     */
    private Integer version;
}
