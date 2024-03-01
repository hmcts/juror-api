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
import org.hibernate.annotations.Immutable;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;

import java.io.Serializable;

@Entity
@Table(name = "t_id_check", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Immutable
public class IdCheckCode implements HasCodeAndDescription<String>, Serializable {

    @Id
    @NotNull
    @Column(name = "id_check")
    private String code;

    @Column(name = "description")
    private String description;
}
