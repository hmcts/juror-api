package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties(value = {"total_due", "total_paid", "total_outstanding"})
public class ExpenseDetailsWithOriginalDto extends ExpenseDetailsWithTotalsDto {

    @JsonIgnoreProperties(value = {"total_due", "total_paid", "total_outstanding"})
    private ExpenseDetailsWithTotalsDto original;

    public ExpenseDetailsWithOriginalDto(Appearance appearance, Appearance originalAppearance) {
        super(appearance);
        if (originalAppearance != null) {
            this.original = new ExpenseDetailsWithTotalsDto(originalAppearance);
        }
    }
}
