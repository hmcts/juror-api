package uk.gov.hmcts.juror.api.moj.domain;

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
 * juror_mod.court_region table entity.
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
@Table(name = "court_region", schema = "juror_mod")
public class CourtRegionMod implements Serializable {

    @Id
    @Column(name = "region_id")
    @Length(max = 5)
    private String regionId;

    @Column(name = "region_name")
    @Length(max = 30)
    private String regionName;

    @Column(name = "notify_account_key")
    @Length(max = 100)
    private String notifyAccountKey;

    @OneToMany(mappedBy = "regionId")
    @Builder.Default
    private List<RegionNotifyTemplateMod> regionNotifyTemplates = new ArrayList<>();

}

