package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_data", schema = "juror_mod")
@NoArgsConstructor
@Getter
@Builder
@Setter
@AllArgsConstructor
@SuppressWarnings("PMD.TooManyFields")
public class PaymentData {
    @Id
    @NotNull
    @GeneratedValue(generator = "payment_data_unique_id_gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "payment_data_unique_id_gen",
        schema = "juror_mod",
        sequenceName = "payment_data_unique_id_seq",
        allocationSize = 1)
    private long uniqueId;

    @ManyToOne
    @JoinColumn(name = "loc_code", nullable = false)
    @NotNull
    private CourtLocation courtLocation;


    @NotNull
    @Column(name = "creation_date")
    private LocalDateTime creationDateTime;

    @NotNull
    @Column(name = "expense_total")
    private BigDecimal expenseTotal;

    @Column(name = "juror_number")
    @NotNull
    @Length(max = 9)
    private String jurorNumber;

    @Column(name = "invoice_id", insertable = false, updatable = false)
    @SequenceGenerator(name = "payment_data_invoice_number_gen",
        schema = "juror_mod",
        sequenceName = "payment_data_invoice_number_seq",
        allocationSize = 1)
    @GeneratedValue(generator = "payment_data_invoice_number_gen", strategy = GenerationType.SEQUENCE)
    private String invoiceId;

    @Column(name = "bank_sort_code")
    @Length(max = 6)
    private String bankSortCode;

    @Column(name = "bank_ac_name")
    @Length(max = 18)
    private String bankAccountName;

    @Column(name = "bank_ac_number")
    @Length(max = 8)
    private String bankAccountNumber;

    @Column(name = "build_soc_number")
    @Length(max = 18)
    private String buildingSocietyNumber;


    @Column(name = "address_line_1")
    @Length(max = 35)
    private String addressLine1;

    @Column(name = "address_line_2")
    @Length(max = 35)
    private String addressLine2;

    @Column(name = "address_line_3")
    @Length(max = 35)
    private String addressLine3;

    @Column(name = "address_line_4")
    @Length(max = 35)
    private String addressLine4;

    @Column(name = "address_line_5")
    @Length(max = 35)
    private String addressLine5;

    @Column(name = "postcode")
    @Length(max = 10)
    private String postcode;

    @Column(name = "auth_code")
    @NotNull
    @Length(max = 9)
    private String authCode;

    @Column(name = "juror_name")
    @NotNull
    @Length(max = 50)
    private String jurorName;

    @Column(name = "loc_cost_centre")
    @NotNull
    @Length(max = 5)
    private String locCostCentre;

    @Column(name = "travel_total")
    private BigDecimal travelTotal;

    @Column(name = "subsistence_total")
    private BigDecimal subsistenceTotal;

    @Column(name = "financial_loss_total")
    private BigDecimal financialLossTotal;

    @Column(name = "expense_file_name")
    @Length(max = 30)
    private String expenseFileName;

    @Column(name = "extracted")
    @NotNull
    private boolean extracted;
}
