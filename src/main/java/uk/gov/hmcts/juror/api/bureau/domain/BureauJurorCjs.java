package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

/**
 * Entity for Juror response Criminal Justice System employment details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "JUROR_RESPONSE_CJS_EMPLOYMENT", schema = "JUROR_DIGITAL")
@Builder
@Deprecated(forRemoval = true)
public class BureauJurorCjs implements Serializable {
    private static final String GENERATOR_NAME = "CJS_EMPLOYMENT_SEQ_GENERATOR";

    @Id
    @SequenceGenerator(name = GENERATOR_NAME, sequenceName = "JUROR_DIGITAL.CJS_EMPLOYMENT_SEQ", allocationSize = 1)
    @GeneratedValue(generator = GENERATOR_NAME)
    private Long id;

    @Column(name = "JUROR_NUMBER")
    @Pattern.List({
        @Pattern(regexp = JUROR_NUMBER),
        @Pattern(regexp = NO_PIPES_REGEX)
    })
    private String jurorNumber;

    @NotEmpty
    @Length(max = 1000)
    @Column(name = "CJS_EMPLOYER")
    private String employer;

    @Length(max = 1000)
    @Column(name = "CJS_EMPLOYER_DETAILS")
    private String details;
}

