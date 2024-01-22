package uk.gov.hmcts.juror.api.juror.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Composite key for {@link ExcusalDeniedLetter}.
 */
@EqualsAndHashCode
public class ExcusalDeniedLetterKey implements Serializable {
    private String owner;
    private String jurorNumber;
}
