package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/**
 * View of the legacy juror POOL_TYPE table data.
 */
@Entity
@Table(name = "t_pool_type", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Immutable
public class PoolType implements Serializable {

    @Id
    @NotNull
    @Column(name = "POOL_TYPE")
    private String poolType;

    @NotNull
    @Column(name = "POOL_TYPE_DESC")
    private String description;

}
