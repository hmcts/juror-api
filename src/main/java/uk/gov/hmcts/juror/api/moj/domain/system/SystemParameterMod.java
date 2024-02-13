package uk.gov.hmcts.juror.api.moj.domain.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_parameter", schema = "juror_mod")
@Immutable
public class SystemParameterMod implements Serializable {

    @Id
    @Column(name = "sp_id")
    private Integer id;

    @Column(name = "sp_desc")
    private String description;

    @Column(name = "sp_value")
    private String value;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

}
