package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * juror_mod notify_template_field table entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notify_template_field", schema = "juror_mod")
@EqualsAndHashCode
public class NotifyTemplateFieldMod implements Serializable {

    @Id
    @NotNull
    @Column(name = "id", unique = true)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "template_id")
    @NotEmpty
    private String templateId;

    @Size(max = 40)
    @NotNull
    @Column(name = "template_field")
    @NotEmpty
    private String templateField;

    @Size(max = 4)
    @Column(name = "position_from")
    private Integer positionFrom;

    @Size(max = 4)
    @Column(name = "position_to")
    private Integer positionTo;

    @Column(name = "mapper_object")
    @Enumerated(EnumType.STRING)
    private NotifyTemplateMapperMod mapperObject;
}
