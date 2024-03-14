package uk.gov.hmcts.juror.api.moj.domain;

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
 * Entity representing settings used by the application workflow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_setting", schema = "juror_mod")
@Immutable
@Builder

public class AppSetting implements Serializable {
    @Id
    @Length(max = 80)
    private String setting;

    @NotEmpty
    @Length(max = 200)
    private String value;
}
