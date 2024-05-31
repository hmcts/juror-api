package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TrialSearch implements IsPageable {

    private boolean isActive;

    private String trialNumber;

    @Min(1)
    private long pageNumber;

    @Min(1)
    private long pageLimit;

    private SortMethod sortMethod;

    private TrialSearch.SortField sortField;

    public void apply(JPAQuery<Trial> query) {
        if (isActive) {
            query.where(QTrial.trial.trialEndDate.isNull());
        }

        if (trialNumber != null) {
            query.where(QTrial.trial.trialNumber.startsWith(trialNumber));
        }
    }


    @Getter
    public enum SortField implements SortMethod.HasComparableExpression {
        TRIAL_NUMBER(QTrial.trial.trialNumber),
        NAMES(QTrial.trial.description),
        TRIAL_TYPE(QTrial.trial.trialType),
        COURT_NAME(QTrial.trial.courtLocation.name),
        COURT_CODE(QTrial.trial.courtLocation.locCode),
        COURTROOM(QTrial.trial.courtroom.description),
        JUDGE(QTrial.trial.judge.name),
        START_DATE(QTrial.trial.trialStartDate),
        END_DATE(QTrial.trial.trialEndDate),
        JUROR_REQUESTED(QTrial.trial.jurorRequested),
        JUROR_SENT(QTrial.trial.jurorsSent),
        ANONYMOUS(QTrial.trial.anonymous);

        private final Expression<? extends Comparable<?>> comparableExpression;

        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }
}
