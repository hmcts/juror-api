package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * Entity representing history code descriptions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_history_code", schema = "juror_mod")
@Immutable
@Builder
public class THistoryCode implements Serializable {
    @Id
    @Length(max = 4)
    @Column(name = "history_code")
    private String historyCode;

    @NotEmpty
    @Length(max = 40)
    @Column(name = "description")
    private String description;
}
