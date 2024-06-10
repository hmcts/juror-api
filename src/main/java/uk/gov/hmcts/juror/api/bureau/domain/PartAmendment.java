package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

/**
 * Entry in the part_amendments auditing column changes to the legacy POOL table rows.<br>
 * <b>Note:</b> There are complex rules around row/column mapping.  See JDB-1446 JDB-1447
 *
 * @implNote The all field @Id is required as the backing table does not express a primary key at all (JPA limitation).
 */
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@IdClass(PartAmendmentKey.class)
@Entity
@Table(name = "PART_AMENDMENTS", schema = "JUROR_DIGITAL_USER")
@Deprecated(forRemoval = true)
public class PartAmendment implements Serializable {
    @Id
    @Column(name = "PART_NO")
    @Pattern.List({
        @Pattern(regexp = JUROR_NUMBER),
        @Pattern(regexp = NO_PIPES_REGEX)
    })
    @Length(max = 9)
    @NotNull
    private String jurorNumber;

    @Id
    @Column(name = "OWNER")
    @NotNull
    @Length(max = 3)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Builder.Default
    private String owner = JurorDigitalApplication.JUROR_OWNER;

    @Id
    @Column(name = "EDIT_DATE")
    @NotNull
    @Builder.Default
    private Date editdate = Date.from(Instant.now());

    @Id
    @Column(name = "EDIT_USERID")
    @Length(max = 20)
    @NotNull
    private String editUserId;

    @Id
    @Column(name = "TITLE")
    @Length(max = 10)
    private String title;

    @Id
    @Column(name = "FNAME")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String firstName;

    @Id
    @Column(name = "LNAME")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String lastName;

    @Id
    @Column(name = "DOB")
    private LocalDate dateOfBirth;

    @Id
    @Column(name = "ADDRESS")
    @Length(max = 175)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String address;

    @Id
    @Column(name = "ZIP")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 10)
    private String postcode;

    @Id
    @Column(name = "POOL_NO")
    @Length(max = 9)
    private String poolNumber;

    @PrePersist
    @PreUpdate
    public void prePersist() {

        if (this.owner == null) {
            this.owner = JurorDigitalApplication.JUROR_OWNER;
            log.trace("Defaulted owner");
        }

        if (this.editdate == null) {
            this.editdate = Date.from(Instant.now());
            log.trace("Defaulted editDate");
        }
    }
}
