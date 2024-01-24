package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;


/**
 * juror_mod.region_notify_template table entity.
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
@Table(name = "region_notify_template", schema = "juror_mod")
public class RegionNotifyTemplateMod implements Serializable {

    @Id
    @Column(name = "region_template_id")
    @Length(max = 3)
    private Integer regionTemplateId;

    @Column(name = "template_name")
    @Length(max = 30)
    private String templateName;

    @Column(name = "region_id")
    @Length(max = 5)
    private String regionId;

    @Column(name = "triggered_template_id")
    @Length(max = 100)
    private String triggeredTemplateId;

    @Column(name = "legacy_template_id")
    private Integer legacyTemplateId;

    @Column(name = "notify_template_id")
    @Length(max = 1000)
    private String notifyTemplateId;

    @Column(name = "message_format")
    @Length(max = 10)
    private String messageFormat;

    @Column(name = "welsh_language")
    @Length(max = 1)
    private String welsh;

}
