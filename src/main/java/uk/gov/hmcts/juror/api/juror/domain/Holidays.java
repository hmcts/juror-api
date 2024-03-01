package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * JUROR_DIGITAL_USER.HOLIDAYS
 */

@Entity
@Table(name = "holiday", schema = "juror_mod", uniqueConstraints = {@UniqueConstraint(columnNames = {"owner",
    "holiday"})})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class Holidays implements Serializable {

    private static final String GENERATOR_NAME = "holiday_sequence_gen";

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = GENERATOR_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = GENERATOR_NAME, schema = "juror_mod", sequenceName = "holiday_id_seq", allocationSize = 1)
    public long id;

    @Column(name = "holiday")
    @NotNull
    private LocalDate holiday;

    @Column(name = "description")
    @Length(max = 30)
    @NotEmpty
    private String description;

    @Column(name = "owner")
    @Length(max = 3)
    private String owner;

    @Column(name = "public")
    @NotNull
    private Boolean publicHoliday;
}