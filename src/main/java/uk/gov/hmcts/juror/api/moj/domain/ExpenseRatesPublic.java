package uk.gov.hmcts.juror.api.moj.domain;

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

@Immutable
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expense_rates_public",schema = "juror_mod")
public class ExpenseRatesPublic implements Serializable {
    @Id
    @Length(max = 80)
    private String expenseType;

    @NotEmpty
    private Float rate;
}
