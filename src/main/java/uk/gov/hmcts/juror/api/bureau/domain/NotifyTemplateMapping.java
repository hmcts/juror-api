package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Notify Template Mapping entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "NOTIFY_TEMPLATE_MAPPING", schema = "JUROR_DIGITAL")
@Deprecated(forRemoval = true)
public class NotifyTemplateMapping implements Serializable {
    @Id
    @Size(max = 50)
    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Size(max = 40)
    @Column(name = "TEMPLATE_NAME", nullable = false, unique = true)
    private String templateName;

    @NotNull
    @Size(max = 60)
    @Column(name = "NOTIFY_NAME")
    private String notifyName;

    @Size(max = 6)
    @Column(name = "FORM_TYPE")
    private String formType;

    @Size(max = 2)
    @Column(name = "NOTIFICATION_TYPE")
    private Integer notificationType;

    @Version
    private Integer version;
}
