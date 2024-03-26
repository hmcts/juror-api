package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QModJurorDetail;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.deleteWhitespace;

/**
 * QueryDSL queries for BureauJurorDetail view.
 *
 * @since JDB-1971
 */
public class BureauJurorDetailQueries {

    private static final String INWARD_CODE_FRAGMENT = "^([0-9])([A-Z]{0,2})$";
    private static final String OUTWARD_CODE_FRAGMENT = "^([A-Z]{0,2})([0-9]{1,2})$";
    private static final String TODO = "TODO";
    private static final String YES = "Y";
    private static final String OWNER_IS_BUREAU = "400";


    //  private static final QBureauJurorDetail bureauJurorDetail = QBureauJurorDetail.bureauJurorDetail;
    private static final QModJurorDetail bureauJurorDetail = QModJurorDetail.modJurorDetail;

    private BureauJurorDetailQueries() {
    }

    /**
     * Juror number.
     * must be a full and exact match. Case in-sensitive.
     * Changed for JDB-3192.
     *
     * @param jurorNumber juror number to search for
     * @return QueryDSL filter.
     */
    public static BooleanExpression byJurorNumber(final String jurorNumber) {
        return bureauJurorDetail.jurorNumber.eq(jurorNumber);
    }

    /**
     * Last name.
     * the characters entered in the search field must match the last name for the Juror in the response they gave.
     * It can be a full or partial match but and for a partial match the letters entered must be present somewhere
     * in the name i.e. searching with 'mit' would return Smith and Mitchell. Case in-sensitive.
     * Changed to full match : for JDB-3192.
     *
     * @param lastName last name to search for
     * @return QueryDSL filterß
     */
    public static BooleanExpression byLastName(final String lastName) {
        return bureauJurorDetail.newLastName.equalsIgnoreCase(lastName);
    }

    /**
     * Pool number.
     * must be a full and exact match.
     *
     * @param poolNumber pool number to search for.
     * @return QueryDSL filter.
     */
    public static BooleanExpression byPoolNumber(final String poolNumber) {
        return bureauJurorDetail.poolNumber.eq(poolNumber);
    }

    /**
     * court location.
     * Added by: SG: JDB-3133.
     * must be a full and exact match.
     *
     * @param courtCode location code to search for.
     * @return QueryDSL filter.
     */
    public static BooleanExpression byCourtCode(final String courtCode) {
        return bureauJurorDetail.courtCode.eq(courtCode);
    }

    /**
     * Postcode.
     * the characters entered in the search field must match the postcode in the Juror's address in the response they
     * gave. It can be a full or partial match. To be a match the specified set of letters must exist somewhere in the
     * postcode e.g. a summons with the postcode KT6 4EX would be returned if KT4 or 4EX were entered. Case
     * in-sensitive.
     *
     * @param postcode postcode to search for .
     * @return QueryDSL filter .
     */
    public static BooleanExpression byPostcode(final String postcode) {
        /*
        NB: This is a workaround for the fact that postcodes in the database may or may not contain spaces.
         */
        final String trimmedPostcode = postcode.toUpperCase().trim();
        //Removed wildcard search - JDB 3192
        BooleanExpression query = bureauJurorDetail.newJurorPostcode.matches(trimmedPostcode);


        query = appendToQuery(bureauJurorDetail.newJurorPostcode, query, buildPostcodeMatchers(trimmedPostcode));
        return query;
    }

    public static BooleanExpression byAssignmentAndProcessingStatus(String staffLogin, List<String> statuses) {
        return byMemberOfStaffAssigned(staffLogin)
            .and(byStatus(statuses));
    }

    public static BooleanExpression byCompletedAt(String staffLogin, LocalDateTime startOfSearchPeriod,
                                                  LocalDateTime endOfSearchPeriod) {
        return byMemberOfStaffAssigned(staffLogin)
            .and(byStatus(Collections.singletonList(ProcessingStatus.CLOSED.name())))
            .and(bureauJurorDetail.completedAt.between(startOfSearchPeriod, endOfSearchPeriod));
    }

    /**
     * Builds matchers to allow for postcode variation.
     * Specifically, postcodes in the database may contain or not contain a space, and the user input varies in the same
     * way. So for example the values "G1 1RD" and "G11RD" are equivalent, and a user search (full or partial) for one
     * must also match the other.
     * Exact Match for JDB-3192. No wildcards.
     *
     * @param postcode postcode to generate matchers for.
     * @return list of SQL matchers.
     */
    static Iterable<String> buildPostcodeMatchers(String postcode) {
        Set<String> matchers = new HashSet<>();

        // If the user entered a space, also search for "without a space" values
        final String spacelessPostcode = StringUtils.containsWhitespace(postcode)
            ?
            deleteWhitespace(postcode)
            :
                postcode;
        if (!spacelessPostcode.equals(postcode)) {
            matchers.add(spacelessPostcode);
        }

        final int inputLength = spacelessPostcode.length();
        for (int i = 3;
             i <= inputLength && i <= 7;
             i++) {
            for (int pivot = Math.max(1, inputLength - 3);
                 pivot < inputLength;
                 pivot++) {
                final String leftToken = spacelessPostcode.substring(0, pivot);
                final String rightToken = spacelessPostcode.substring(pivot, inputLength);
                final String newToken = leftToken + " " + rightToken;
                if (validOutwardCodeFragment(leftToken) && validInwardCode(rightToken) && !newToken.equals(postcode)) {
                    matchers.add(newToken);
                }
            }
        }
        return matchers;
    }

    /**
     * Checks if a token (superficially) looks like a fragment of an outward code.
     * (an outward code is the 'left part' of a UK postcode).
     *
     * @param token token to check.
     * @return whether it could be an outward code fragment.
     */
    private static boolean validOutwardCodeFragment(final String token) {
        return token.matches(OUTWARD_CODE_FRAGMENT);
    }

    /**
     * Checks if a token (superficially) looks like a fragment of an inward code.
     * (an inward code is the 'right part' of a UK postcode).
     *
     * @param token token to check.
     * @return whether it could be an outward code fragment.
     */
    private static boolean validInwardCode(final String token) {
        return token.matches(INWARD_CODE_FRAGMENT);
    }

    private static BooleanExpression appendToQuery(StringPath column, BooleanExpression query,
                                                   Iterable<String> newQueries) {
        BooleanExpression newQuery = query;
        for (String newQueryString : newQueries) {
            newQuery = newQuery.or(column.matches(newQueryString));
        }
        return newQuery;
    }

    /**
     * Urgents only.
     * Urgents means either urgent or super urgent responses.
     *
     * @return QueryDSL filter.
     */
    public static BooleanExpression urgentsOnly() {
        return bureauJurorDetail.urgent.isTrue().or(bureauJurorDetail.superUrgent.isTrue());
    }

    /**
     * Staff assigned.
     *
     * @return QueryDSL filter.
     */
    public static BooleanExpression byMemberOfStaffAssigned(String staffMemberLogin) {
        return bureauJurorDetail.assignedStaffMember.username.equalsIgnoreCase(staffMemberLogin);
    }

    /**
     * Status.
     *
     * @return QueryDSL filter.
     */
    public static BooleanExpression byStatus(List<String> statuses) {
        return bureauJurorDetail.processingStatus.in(statuses);
    }

    /**
     * Enables QueryDSL results to be sorted in ascending date order.
     *
     * @return QueryDSL order specifier.
     * @since JDB-2142.
     */
    public static OrderSpecifier dateReceivedAscending() {
        return bureauJurorDetail.dateReceived.asc();
    }


    /**
     * Matches records where READ_ONLY is equal 'Y'.
     *
     */

    public static BooleanExpression byReadOnly() {
        return bureauJurorDetail.owner.ne(OWNER_IS_BUREAU);

    }

    /**
     * Matches records where POOL.STATUS is SUMMONED.
     *
     */

    public static BooleanExpression byPoolStatusSummoned() {
        return bureauJurorDetail.status.eq((long) IJurorStatus.SUMMONED);
    }

    /**
     * Matches records where POOL.STATUS is NOT equal to RESPONDED.
     *
     */

    public static BooleanExpression byPoolStatusNotSummoned() {
        return bureauJurorDetail.status.ne((long) IJurorStatus.SUMMONED);
    }

    /**
     * Matches records where POOL.STATUS is equal to SUMMONED and READ_ONLY = Y.
     *
     */

    public static BooleanExpression byPoolStatusSummonedAndReadOnly() {
        return byPoolStatusSummoned().and(byReadOnly());
    }


    /**
     * Matches records where that need to be closed.
     * jurorDetail@return
     */

    public static BooleanExpression jurorResponsesForClosing() {
        return processingStatusToDo().and((byPoolStatusNotSummoned()).or(byPoolStatusSummonedAndReadOnly()));

    }

    /**
     * Processing Status not equal TODO.
     */
    public static BooleanExpression processingStatusToDo() {
        return bureauJurorDetail.processingStatus.eq(TODO);
    }

}