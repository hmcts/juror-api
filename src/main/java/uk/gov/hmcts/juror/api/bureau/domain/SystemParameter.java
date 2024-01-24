package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.Date;

/**
 * Domain entity for system parameter rows.
 */
@Entity
@Table(name = "system_parameter", schema = "juror_mod")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Immutable
public class SystemParameter implements Serializable {
    @Id
    @Column(name = "sp_id")
    private Integer spId;
    @Column(name = "sp_desc")
    private String spDesc;
    @Column(name = "sp_value")
    private String spValue;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_date")
    private Date createdDate;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_date")
    private Date updatedDate;
}
