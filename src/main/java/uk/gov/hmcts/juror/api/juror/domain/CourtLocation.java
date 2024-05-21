package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.gov.hmcts.juror.api.moj.domain.CourtRegionMod;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Entity representing standing data for a court location.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@ToString(exclude = "assemblyRoom")
@EqualsAndHashCode(exclude = {"courtRegion", "assemblyRoom"})
@Table(name = "court_location", schema = "juror_mod")
@SuppressWarnings("PMD.TooManyFields")
public class CourtLocation implements Serializable {

    @Id
    @Column(name = "loc_code")
    private String locCode;

    /**
     * Court name.
     */
    @Column(name = "loc_name")
    @NotAudited
    private String name;

    @Column(name = "cost_centre")
    @NotAudited
    private String costCentre;


    @Column(name = "loc_court_name")
    @NotAudited
    private String locCourtName;

    @Column(name = "loc_attend_time")
    @NotAudited
    private LocalTime courtAttendTime;

    /**
     * Court address line 1.
     */
    @Column(name = "loc_address1")
    @NotAudited
    private String address1;

    /**
     * Court address line 2.
     */
    @Column(name = "loc_address2")
    @NotAudited
    private String address2;

    /**
     * Court address line 3.
     */
    @Column(name = "loc_address3")
    @NotAudited
    private String address3;

    /**
     * Court address line 4.
     */
    @Column(name = "loc_address4")
    @NotAudited
    private String address4;

    /**
     * Court address line 5.
     */
    @Column(name = "loc_address5")
    @NotAudited
    private String address5;

    /**
     * Court address line 6.<br>
     * Note: Possibly unused field.
     */
    @Column(name = "loc_address6")
    @NotAudited
    private String address6;

    /**
     * Court postcode.
     */
    @Column(name = "loc_zip")
    @NotAudited
    private String postcode;

    /**
     * Court phone number.
     */
    @Column(name = "loc_phone")
    @NotAudited
    private String locPhone;

    /**
     * Court jury officer phone contact details.
     */
    @Column(name = "jury_officer_phone")
    @NotAudited
    private String juryOfficerPhone;

    /**
     * Court Location Address with Name.
     */
    @Column(name = "location_address")
    @NotAudited
    private String locationAddress;

    /**
     * Court location yield value.
     */
    @Column(name = "yield")
    @NotAudited
    private BigDecimal yield;

    /**
     * Court location voters lock value.
     */
    @Column(name = "voters_lock")
    @NotAudited
    private Integer votersLock;


    @ManyToOne
    @JoinColumn(name = "region_id")
    @NotAudited
    private CourtRegionMod courtRegion;

    @Column(name = "term_of_service")
    @NotAudited
    private String insertIndicators;

    @Column(name = "tdd_phone")
    @NotAudited
    private String courtFaxNo;

    @Column(name = "loc_signature")
    @NotAudited
    private String signatory;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assembly_room", referencedColumnName = "id")
    @NotAudited
    private Courtroom assemblyRoom;

    @OneToMany(mappedBy = "courtLocation")
    @NotAudited
    private List<PoolRequest> poolRequests;

    /**
     * Court Location Owner - for primary courts this is the same as location code,
     * for satellite courts, this will be the location code of this court location's primary court.
     */
    @Column(name = "owner")
    @NotAudited
    private String owner;


    @Column(name = "public_transport_soft_limit")
    private BigDecimal publicTransportSoftLimit;

    @Column(name = "taxi_soft_limit")
    private BigDecimal taxiSoftLimit;

    public CourtType getType() {
        return owner.equals(locCode) ? CourtType.MAIN : CourtType.SATELLITE;
    }

    public boolean isPrimaryCourt() {
        return CourtType.MAIN.equals(getType());
    }

    public String getNameWithLocCode() {
        return this.getName() + " (" + this.getLocCode() + ")";
    }
}
