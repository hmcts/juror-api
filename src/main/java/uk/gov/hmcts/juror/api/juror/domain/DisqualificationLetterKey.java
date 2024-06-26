package uk.gov.hmcts.juror.api.juror.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Composite key for {@link DisqualificationLetter}.
 */
@EqualsAndHashCode
@Deprecated(forRemoval = true)
public class DisqualificationLetterKey implements Serializable {
    private String owner;
    private String jurorNumber;
}
