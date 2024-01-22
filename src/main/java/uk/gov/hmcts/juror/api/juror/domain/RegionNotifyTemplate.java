package uk.gov.hmcts.juror.api.juror.domain;

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
 * JUROR_DIGITAL.REGION_NOTIFY_TEMPLATE Table.
 */
@Entity
@Table(name = "REGION_NOTIFY_TEMPLATE", schema = "JUROR_DIGITAL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j

public class RegionNotifyTemplate implements Serializable {

    @Id
    @Column(name = "REGION_TEMPLATE_ID")
    @Length(max = 3)
    private Integer regionTemplateId;

    @Column(name = "TEMPLATE_NAME")
    @Length(max = 30)
    private String templateName;

    @Column(name = "REGION_ID")
    @Length(max = 5)
    private String regionId;

    @Column(name = "TRIGGERED_TEMPLATE_ID")
    @Length(max = 100)
    private String triggeredTemplateId;

    @Column(name = "LEGACY_TEMPLATE_ID")
    private Integer legacyTemplateId;

    @Column(name = "NOTIFY_TEMPLATE_ID")
    @Length(max = 1000)
    private String notifyTemplateId;

    @Column(name = "MESSAGE_FORMAT")
    @Length(max = 10)
    private String messageFormat;

    @Column(name = "WELSH_LANGUAGE")
    @Length(max = 1)
    private String welsh;

}
