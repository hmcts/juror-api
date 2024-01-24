package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Table(name = "t_pending_juror_status", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Immutable
public class PendingJurorStatus implements Serializable {

    @Id
    @Column(name = "code")
    @NotNull
    private Character code;

    @Column(name = "description")
    @Size(max = 60)
    private String description;

}
