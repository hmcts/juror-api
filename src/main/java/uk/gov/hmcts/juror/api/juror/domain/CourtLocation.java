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
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity representing standing data for a court location.
 */
@Getter
@Setter
@Entity
@Audited
@EqualsAndHashCode(exclude = {"courtRegion"})
@ToString
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


    @Column(name = "loc_court_name")
    @NotAudited
    private String locCourtName;

    @Column(name = "loc_attend_time")
    @NotAudited
    private String courtAttendTime;

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

    /**
     * Foreign Key to JUROR_DIGITAL.COURT_REGION.
     */
    @ManyToOne
    @JoinColumn(name = "region_id")
    @NotAudited
    private CourtRegion courtRegion;

    @Column(name = "term_of_service")
    @NotAudited
    private String insertIndicators;

    @Column(name = "tdd_phone")
    @NotAudited
    private String courtFaxNo;

    @Column(name = "loc_signature")
    @NotAudited
    private String signatory;

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


    //Rates
    @Column(name = "rates_effective_from")
    private LocalDate ratesEffectiveFrom;

    @Column(name = "rate_per_mile_car_0_passengers")
    private BigDecimal carMileageRatePerMile0Passengers;
    @Column(name = "rate_per_mile_car_1_passengers")
    private BigDecimal carMileageRatePerMile1Passengers;
    @Column(name = "rate_per_mile_car_2_or_more_passengers")
    private BigDecimal carMileageRatePerMile2OrMorePassengers;


    @Column(name = "rate_per_mile_motorcycle_0_passengers")
    private BigDecimal motorcycleMileageRatePerMile0Passengers;
    @Column(name = "rate_per_mile_motorcycle_1_or_more_passengers")
    private BigDecimal motorcycleMileageRatePerMile1Passengers;


    @Column(name = "rate_per_mile_bike")
    private BigDecimal bikeRate;

    @Column(name = "limit_financial_loss_half_day")
    private BigDecimal limitFinancialLossHalfDay;
    @Column(name = "limit_financial_loss_full_day")
    private BigDecimal limitFinancialLossFullDay;
    @Column(name = "limit_financial_loss_half_day_long_trial")
    private BigDecimal limitFinancialLossHalfDayLongTrial;
    @Column(name = "limit_financial_loss_full_day_long_trial")
    private BigDecimal limitFinancialLossFullDayLongTrial;

    @Column(name = "rate_substance_standard")
    private BigDecimal substanceRateStandard;
    @Column(name = "rate_substance_long_day")
    private BigDecimal substanceRateLongDay;

    @Column(name = "public_transport_soft_limit")
    private BigDecimal publicTransportSoftLimit;
}
