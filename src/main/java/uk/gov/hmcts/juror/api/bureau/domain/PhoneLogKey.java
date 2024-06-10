package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link PhoneLog}.
 */
@EqualsAndHashCode
@Deprecated(forRemoval = true)
public class PhoneLogKey implements Serializable {
    private String jurorNumber;
    private String owner;
    private Date startCall;
}
