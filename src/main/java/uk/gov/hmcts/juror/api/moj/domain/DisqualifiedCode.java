package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;
import uk.gov.hmcts.juror.api.moj.domain.system.HasEnabled;

import java.io.Serializable;

@Entity
@Table(name = "t_disq_code", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DisqualifiedCode implements HasCodeAndDescription<String>, HasEnabled, Serializable {

    @Id
    @NotNull
    @Column(name = "disq_code")
    private String disqualifiedCode;

    @Column(name = "description")
    private String description;

    @Column(name = "enabled")
    private boolean enabled;

    @Override
    public String getCode() {
        return this.disqualifiedCode;
    }
}
