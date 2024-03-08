package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.dsl.BooleanExpression;

import java.time.LocalDate;

@SuppressWarnings("PMD.LawOfDemeter")
public abstract class CurrentlyDeferredQueries {

    private static BooleanExpression filterByOwner(String owner) {
        QCurrentlyDeferred currentlyDeferred = QCurrentlyDeferred.currentlyDeferred;
        return currentlyDeferred.owner.eq(owner);
    }

    public static BooleanExpression filterByCourtLocation(String locCode) {
        QCurrentlyDeferred currentlyDeferred = QCurrentlyDeferred.currentlyDeferred;
        return currentlyDeferred.locCode.eq(locCode);
    }

    private static BooleanExpression filterByDeferredDate(LocalDate deferredTo) {
        QCurrentlyDeferred currentlyDeferred = QCurrentlyDeferred.currentlyDeferred;
        return currentlyDeferred.deferredTo.eq(deferredTo);
    }

    /**
     * Filter currently deferred records using the Owner, location code and deferred to date.
     *
     * @param owner      3-digit code to identify whether the deferral record is owned by the courts or the bureau
     * @param locCode    3-digit code to identify which court location the deferral belongs to
     * @param deferredTo the date the juror deferred to
     * @return Predicate
     */
    public static BooleanExpression filterByCourtAndDate(String owner, String locCode, LocalDate deferredTo) {
        return filterByOwner(owner).and(filterByCourtLocation(locCode)).and(filterByDeferredDate(deferredTo));
    }
}
