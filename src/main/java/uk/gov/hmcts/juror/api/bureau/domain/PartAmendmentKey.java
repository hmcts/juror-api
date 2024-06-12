package uk.gov.hmcts.juror.api.bureau.domain;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * Composite key for {@link PartAmendment}.
 */
@EqualsAndHashCode
@Deprecated(forRemoval = true)
public class PartAmendmentKey implements Serializable {
    private String jurorNumber;
    private String owner;
    private Date editdate;
    private String editUserId;
    private String title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private String postcode;
    private String poolNumber;
}
