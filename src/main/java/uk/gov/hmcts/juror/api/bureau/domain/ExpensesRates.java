package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * Entity representing expenses rates informatiomn used for the expenses calculator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "EXPENSES_RATES", schema = "JUROR_DIGITAL")
@Immutable
@Builder
public class ExpensesRates implements Serializable {
    @Id
    @Length(max = 80)
    private String expenseType;

    @NotEmpty
    private Float rate;
}
