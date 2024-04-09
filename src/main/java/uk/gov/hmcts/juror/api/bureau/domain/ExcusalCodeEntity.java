package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * Entity representing list of reasons a juror can be excused.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "EXC_CODE", schema = "JUROR_DIGITAL_USER")
@Immutable
public class ExcusalCodeEntity implements Serializable {
    @Id
    @Column(name = "EXC_CODE")
    @Length(max = 2)
    private String excusalCode;

    @NotEmpty
    @Length(max = 200)
    private String description;
}
