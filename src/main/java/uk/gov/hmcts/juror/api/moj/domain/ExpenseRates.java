package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "expense_rates", schema = "juror_mod")
@SuppressWarnings("PMD.TooManyFields")
public class ExpenseRates {

    @Column(name = "id")
    @Id
    private long id;

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

    @Column(name = "rate_subsistence_standard")
    private BigDecimal subsistenceRateStandard;
    @Column(name = "rate_subsistence_long_day")
    private BigDecimal subsistenceRateLongDay;
}
