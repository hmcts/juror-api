package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import uk.gov.hmcts.juror.api.moj.domain.ICourtLocation;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing standing data for a Welsh court location.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Table(name = "welsh_court_location", schema = "juror_mod")
public class WelshCourtLocation implements Serializable, ICourtLocation {

    @Id
    @Column(name = "loc_code")
    private String locCode;

    /**
     * Court name.
     */
    @Column(name = "loc_name")
    private String locCourtName;

    /**
     * Court address line 1.
     */
    @Column(name = "loc_address1")
    private String address1;

    /**
     * Court address line 2.
     */
    @Column(name = "loc_address2")
    private String address2;

    /**
     * Court address line 3.
     */
    @Column(name = "loc_address3")
    private String address3;

    /**
     * Court address line 4.
     */
    @Column(name = "loc_address4")
    private String address4;

    /**
     * Court address line 5.
     */
    @Column(name = "loc_address5")
    private String address5;

    /**
     * Court address line 6.<br>
     * Note: Possibly unused field.
     */
    @Column(name = "loc_address6")
    private String address6;

    /**
     * Court Location Address with Name.
     */
    @Column(name = "location_address")
    private String locationAddress;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @PrePersist
    private void prePersist() {
        preUpdate();
    }

    @PreUpdate
    private void preUpdate() {
        lastUpdate = LocalDateTime.now();
    }

}
