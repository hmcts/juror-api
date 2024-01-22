package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "POOL_EXTEND", schema = "JUROR_DIGITAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolExtend implements Serializable {
    @Id
    @Column(name = "PART_NO")
    private String jurorNumber;

    @Column(name = "IS_LOCKED")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private Boolean isLocked;
}
