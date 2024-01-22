package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Entity representing standing data for a court location.
 */
@Getter
@Setter
@Entity
@EqualsAndHashCode(exclude = {"courtRegion"})
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
    private String name;


    @Column(name = "loc_court_name")
    private String locCourtName;

    @Column(name = "loc_attend_time")
    private String courtAttendTime;

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
     * Court postcode.
     */
    @Column(name = "loc_zip")
    private String postcode;

    /**
     * Court phone number.
     */
    @Column(name = "loc_phone")
    private String locPhone;

    /**
     * Court jury officer phone contact details.
     */
    @Column(name = "jury_officer_phone")
    private String juryOfficerPhone;

    /**
     * Court Location Address with Name.
     */
    @Column(name = "location_address")
    private String locationAddress;

    /**
     * Court location yield value.
     */
    @Column(name = "yield")
    private BigDecimal yield;

    /**
     * Court location voters lock value.
     */
    @Column(name = "voters_lock")
    private Integer votersLock;

    /**
     * Foreign Key to JUROR_DIGITAL.COURT_REGION.
     */
    @ManyToOne
    @JoinColumn(name = "region_id")
    private CourtRegion courtRegion;

    @Column(name = "term_of_service")
    private String insertIndicators;

    @Column(name = "tdd_phone")
    private String courtFaxNo;

    @Column(name = "loc_signature")
    private String signatory;

    @OneToMany(mappedBy = "courtLocation")
    private List<PoolRequest> poolRequests;

    /**
     * Court Location Owner - for primary courts this is the same as location code,
     * for satellite courts, this will be the location code of this court location's primary court.
     */
    @Column(name = "owner")
    private String owner;

}
