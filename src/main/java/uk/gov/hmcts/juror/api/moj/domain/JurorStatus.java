package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import uk.gov.hmcts.juror.api.moj.domain.system.HasActive;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;

import java.io.Serializable;

@Entity
@Table(name = "t_juror_status", schema = "juror_mod")
@NoArgsConstructor
@Getter
@Setter
@Immutable
public class JurorStatus implements HasCodeAndDescription<Integer>, HasActive, Serializable {

    @Id
    @Column(name = "status")
    @NotNull
    private int status;

    @Column(name = "status_desc")
    private String statusDesc;

    @Column(name = "active")
    private boolean active;

    @Override
    public Integer getCode() {
        return this.status;
    }

    @Override
    public String getDescription() {
        return this.statusDesc;
    }

    @Override
    public Boolean getActive() {
        return this.active;
    }
}
