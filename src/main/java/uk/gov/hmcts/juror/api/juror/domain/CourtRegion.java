package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * JUROR_DIGITAL.COURT_REGION Table.
 */
@Entity
@Table(name = "COURT_REGION", schema = "JUROR_DIGITAL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class CourtRegion implements Serializable {
    @Id
    @Column(name = "REGION_ID")
    @Length(max = 5)
    private String regionId;

    @Column(name = "REGION_NAME")
    @Length(max = 30)
    private String regionName;

    @Column(name = "NOTIFY_ACCOUNT_KEY")
    @Length(max = 100)
    private String notifyAccountKey;

    /**
     * @see RegionNotifyTemplate
     */

    @OneToMany(mappedBy = "regionId")
    @Builder.Default
    private List<RegionNotifyTemplate> regionNotifyTemplates = new ArrayList<>();


}

