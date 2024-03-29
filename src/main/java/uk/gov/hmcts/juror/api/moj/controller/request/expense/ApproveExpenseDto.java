package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Valid
@SuperBuilder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApproveExpenseDto extends JurorNumberAndPoolNumberDto {

    @NotNull
    private ApprovalType approvalType;

    @JsonProperty("is_cash_payment")
    @NotNull
    private Boolean cashPayment;

    @JsonProperty("revisions")
    @NotEmpty
    private List<@NotNull DateToRevision> dateToRevisions;


    @Data
    @Builder
    public static class DateToRevision {
        @JsonProperty("attendance_date")
        @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
        @NotNull
        private LocalDate attendanceDate;
        @NotNull
        private Long version;
    }

    public enum ApprovalType {
        FOR_APPROVAL(appearance -> !appearance.isDraftExpense()
            && AppearanceStage.EXPENSE_ENTERED.equals(appearance.getAppearanceStage())),
        FOR_REAPPROVAL(appearance -> !appearance.isDraftExpense()
            && AppearanceStage.EXPENSE_EDITED.equals(appearance.getAppearanceStage()));
        @SuppressWarnings("PMD.LinguisticNaming")
        private final Function<Appearance, Boolean> isApplicableFunction;

        ApprovalType(Function<Appearance, Boolean> isApplicableFunction) {
            this.isApplicableFunction = isApplicableFunction;
        }

        public boolean isApplicable(Appearance appearance) {
            return isApplicableFunction.apply(appearance);
        }
    }


}
