package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Notify Template Field entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "NOTIFY_TEMPLATE_FIELD", schema = "JUROR_DIGITAL")
@EqualsAndHashCode
public class NotifyTemplateField implements Serializable {

    @Id
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Size(max = 50)
    @Column(name = "TEMPLATE_ID", nullable = false)
    @NotEmpty
    private String templateId;

    @Size(max = 40)
    @Column(name = "TEMPLATE_FIELD", nullable = false)
    @NotEmpty
    private String templateField;

    @NotNull
    @Size(max = 80)
    @Column(name = "DATABASE_FIELD", nullable = false)
    @NotEmpty
    private String databaseField;

    @Size(max = 4)
    @Column(name = "POSITION_FROM")
    private Integer positionFrom;

    @Size(max = 4)
    @Column(name = "POSITION_TO")
    private Integer positionTo;

    @Size(max = 4)
    @Column(name = "FIELD_LENGTH")
    private Integer fieldLength;

    @Column(name = "CONVERT_TO_DATE")
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    @Builder.Default
    private Boolean convertToDate = Boolean.FALSE;

    @Size(max = 60)
    @Column(name = "JD_CLASS_NAME")
    private String jdClassName;

    @Size(max = 60)
    @Column(name = "JD_CLASS_PROPERTY")
    private String jdClassProperty;

    @Version
    private Integer version;

}
