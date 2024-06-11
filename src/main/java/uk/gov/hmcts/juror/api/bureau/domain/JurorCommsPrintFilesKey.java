package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Composite key for {@link JurorCommsPrintFiles}.
 */
@EqualsAndHashCode
@Deprecated(forRemoval = true)
public class JurorCommsPrintFilesKey implements Serializable {
    private String jurorNumber;
    private String printFileName;
    private Date creationDate;
}
