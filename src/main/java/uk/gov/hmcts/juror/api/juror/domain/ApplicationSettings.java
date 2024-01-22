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
 * Entity representing settings used by the application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_settings", schema = "juror_mod")
@Immutable
@Builder
public class ApplicationSettings implements Serializable {

    @Id
    @Length(max = 80)
    @Column(name = "setting")
    private String setting;

    @NotEmpty
    @Length(max = 200)
    @Column(name = "value")
    private String value;

}
