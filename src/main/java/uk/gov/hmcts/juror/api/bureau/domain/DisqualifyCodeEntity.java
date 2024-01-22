package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * Entity representing list of reasons a juror can be disqualified.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DIS_CODE", schema = "JUROR_DIGITAL_USER")
@Immutable
public class DisqualifyCodeEntity implements Serializable {
    @Id
    @Column(name = "DISQ_CODE")
    @Length(max = 1)
    private String disqualifyCode;

    @Column(name = "DESCRIPTION")
    @Length(max = 60)
    @NotBlank
    private String description;
}
