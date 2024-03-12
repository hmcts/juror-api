package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "juror_expense_totals", schema = "juror_mod")
@IdClass(JurorExpenseTotalsId.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Immutable
public class JurorExpenseTotals implements Serializable {

    @Id
    @Column(name = "juror_number")
    private String jurorNumber;

    @Id
    @Column(name = "pool_number")
    private String poolNumber;

    @Column(name = "loc_code")
    private String courtLocationCode;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "travel_unapproved")
    private BigDecimal travelUnapproved;

    @Column(name = "travel_approved")
    private BigDecimal travelApproved;

    @Column(name = "financial_loss_unapproved")
    private BigDecimal financialLossUnapproved;

    @Column(name = "financial_loss_approved")
    private BigDecimal financialLossApproved;

    @Column(name = "subsistence_unapproved")
    private BigDecimal subsistenceUnapproved;

    @Column(name = "subsistence_approved")
    private BigDecimal subsistenceApproved;

    @Column(name = "smart_card_spend_total")
    private BigDecimal smartCardSpendTotal;

    @Column(name = "total_unapproved")
    private BigDecimal totalUnapproved;

    @Column(name = "total_approved")
    private BigDecimal totalApproved;

    @Column(name = "pending_approval_count")
    private long pendingApprovalCount;

}
