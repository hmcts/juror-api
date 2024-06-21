package uk.gov.hmcts.juror.api.moj.domain.messages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.juror.api.config.Settings;
import uk.gov.hmcts.juror.api.juror.domain.QPool;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorSearch;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MessageSearch implements IsPageable {

    @JsonProperty("filters")
    private List<Filter> filters;

    @JsonProperty("juror_search")
    @Valid
    private JurorSearch jurorSearch;

    @JsonProperty("pool_number")
    private String poolNumber;

    @JsonProperty("next_due_at_court_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDueAtCourt;

    @JsonProperty("date_deferred_to")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDeferredTo;

    @JsonProperty("trial_number")
    private String trialNumber;

    @JsonProperty("only_deferrals_in_court")
    @CourtLocationCode
    private String onlyDeferralsInCourt;

    @Min(1)
    @JsonProperty("page_number")
    private long pageNumber;

    @Min(1)
    @JsonProperty("page_limit")
    private long pageLimit;

    @JsonProperty("sort_method")
    private SortMethod sortMethod;

    @JsonProperty("sort_field")
    private SortField sortField;

    public void apply(JPQLQuery<Tuple> query) {
        applyJurorSearch(query, this.getJurorSearch());
        if (this.getPoolNumber() != null) {
            query.where(QJurorPool.jurorPool.pool.poolNumber.startsWith(this.getPoolNumber()));
        }

        if (this.getTrialNumber() != null) {
            query.where(QTrial.trial.trialNumber.eq(this.getTrialNumber()));
        }

        if (this.getNextDueAtCourt() != null) {
            query.where(QJurorPool.jurorPool.nextDate.eq(this.getNextDueAtCourt()));
        }

        if (this.getDateDeferredTo() != null) {
            query.where(QJurorPool.jurorPool.deferralDate.eq(this.getDateDeferredTo()));
        }
        if (this.getOnlyDeferralsInCourt() != null) {
            query.where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(this.getOnlyDeferralsInCourt()));
            addFilter(Filter.SHOW_ONLY_DEFERRED);
        }
        applyMessageFilter(query, this.getFilters());
    }

    public void addFilter(Filter filter) {
        if (this.getFilters() == null) {
            this.filters = new ArrayList<>();
        }
        this.getFilters().add(filter);
    }

    void applyJurorSearch(JPQLQuery<Tuple> query, JurorSearch jurorSearch) {
        if (jurorSearch != null) {
            if (jurorSearch.getJurorName() != null) {
                query.where(QJuror.juror.firstName.concat(" ").concat(QJuror.juror.lastName).toLowerCase()
                    .contains(jurorSearch.getJurorName().toLowerCase(Settings.LOCALE)));
            }

            if (jurorSearch.getJurorNumber() != null) {
                query.where(QJuror.juror.jurorNumber.startsWith(jurorSearch.getJurorNumber()));
            }

            if (jurorSearch.getPostcode() != null) {
                query.where(QJuror.juror.postcode.toLowerCase()
                    .eq(jurorSearch.getPostcode().toLowerCase(Settings.LOCALE)));
            }
        }
    }

    void applyMessageFilter(JPQLQuery<Tuple> query, List<MessageSearch.Filter> filters) {
        if (filters == null) {
            return;
        }
        List<BooleanExpression> or = new ArrayList<>();
        List<BooleanExpression> and = new ArrayList<>();

        filters.stream()
            .distinct()
            .forEach(filter -> {
                if (filter.isInclude) {
                    or.add(filter.getExpression());
                } else {
                    and.add(filter.getExpression());
                }
            });

        if (!or.isEmpty()) {
            query.where(or.stream().reduce(BooleanExpression::or).get());
        }
        if (!and.isEmpty()) {
            query.where(and.stream().reduce(BooleanExpression::and).get());
        }
    }

    @Getter
    public enum SortField implements SortMethod.HasComparableExpression {
        JUROR_NUMBER(QJuror.juror.jurorNumber),
        FIRST_NAME(QJuror.juror.firstName),
        LAST_NAME(QJuror.juror.lastName),
        EMAIL(QJuror.juror.email),
        PHONE(QJuror.juror.phoneNumber),
        POOL_NUMBER(QPool.pool.poolNumber),
        STATUS(QJurorPool.jurorPool.status.status),
        TRIAL_NUMBER(QTrial.trial.trialNumber),
        ON_CALL(QJurorPool.jurorPool.onCall),
        NEXT_DUE_AT_COURT_DATE(QJurorPool.jurorPool.nextDate),
        DATE_DEFERRED_TO(QJurorPool.jurorPool.deferralDate),
        COMPLETION_DATE(QJuror.juror.completionDate),
        WELSH_LANGUAGE(QJuror.juror.welsh);

        private final Expression<? extends Comparable<?>> comparableExpression;

        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }


    @Getter
    public enum Filter {
        INCLUDE_ON_CALL(true, QJurorPool.jurorPool.onCall.eq(true)),
        INCLUDE_FAILED_TO_ATTEND(true, QJurorPool.jurorPool.status.status.eq(IJurorStatus.FAILED_TO_ATTEND)),
        INCLUDE_DEFERRED(true, QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)),
        INCLUDE_JURORS_AND_PANELLED(true,
            QJurorPool.jurorPool.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR)),
        INCLUDE_COMPLETED(true, QJurorPool.jurorPool.status.status.eq(IJurorStatus.COMPLETED)),
        INCLUDE_TRANSFERRED(true, QJurorPool.jurorPool.status.status.eq(IJurorStatus.TRANSFERRED)),
        INCLUDE_DISQUALIFIED_AND_EXCUSED(true, QJurorPool.jurorPool.status.status.in(IJurorStatus.DISQUALIFIED,
            IJurorStatus.EXCUSED)),
        SHOW_ONLY_ON_CALL(false, QJurorPool.jurorPool.onCall.eq(true)),
        SHOW_ONLY_FAILED_TO_ATTEND(false, QJurorPool.jurorPool.status.status.eq(IJurorStatus.FAILED_TO_ATTEND)),
        SHOW_ONLY_DEFERRED(false, QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)),
        SHOW_ONLY_RESPONDED(false, QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED));

        private final boolean isInclude;
        private final BooleanExpression expression;

        Filter(boolean isInclude, BooleanExpression expression) {
            this.isInclude = isInclude;
            this.expression = expression;
        }
    }
}
