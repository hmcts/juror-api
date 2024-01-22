package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * juror_mod notify_template_mapping table entity..
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notify_template_mapping", schema = "juror_mod")
public class NotifyTemplateMappingMod implements Serializable {

    @Id
    @NotNull
    @Size(max = 50)
    @Column(name = "template_id")
    public String templateId;

    @NotEmpty
    @Size(max = 40)
    @Column(name = "template_name")
    private String templateName;

    @NotNull
    @Size(max = 60)
    @Column(name = "notify_name")
    private String notifyName;

    @JoinColumn(name = "form_type")
    @ManyToOne
    private FormAttribute formType;

    @Size(max = 2)
    @Column(name = "notification_type")
    private Integer notificationType;

    @Column(name = "version")
    private Integer version;

}
