package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralDeniedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"unchecked", "PMD.TooManyMethods"})
class DeferralDeniedLetterListRepositoryImplTest {

    private static final String OWNER = TestConstants.VALID_COURT_LOCATION;
    private static final String JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER;
    private static final String POSTCODE = TestConstants.VALID_POSTCODE;
    private static final String SECOND_JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER_2;
    private static final String THIRD_JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER_3;
    private static final String OTHER_UNPRINTED_JUROR_NUMBER = "111111111";
    private static final String UNKNOWN_JUROR_NUMBER = "999999999";
    private static final String POOL_NUMBER = TestConstants.VALID_POOL_NUMBER;

    private JPAQuery<Tuple> jpaQuery;
    private JPAQueryFactory queryFactory;
    private MockedConstruction<JPAQueryFactory> queryFactories;
    private JPAQuery<Tuple> printedQuery;
    private JPAQueryFactory printedQueryFactory;

    private static final QJurorPool POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QJurorHistory HISTORY = QJurorHistory.jurorHistory;
    private static final LocalDate REFUSAL_DATE = LocalDate.of(2026, 10, 12);
    private static final Expression<?>[] PROJECTION = {
        QPoolRequest.poolRequest.poolNumber, JUROR.jurorNumber, JUROR.firstName, JUROR.lastName,
        JUROR.postcode, POOL.deferralDate, QJurorStatus.jurorStatus.statusDesc,
        HISTORY.dateCreatedDateOnly, HISTORY.otherInformation, POOL.isActive
    };
    private static final Expression<?>[] PRINT_PROJECTION = {
        QPoolRequest.poolRequest.poolNumber, JUROR.jurorNumber, HISTORY.dateCreated
    };

    private final DeferralDeniedLetterListRepositoryImpl repository = new DeferralDeniedLetterListRepositoryImpl();

    @BeforeEach
    void beforeEach() {
        jpaQuery = Mockito.mock(JPAQuery.class);
        Mockito.when(jpaQuery.from(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.join(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.leftJoin(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.on(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.where(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.orderBy(Mockito.any(OrderSpecifier.class))).thenReturn(jpaQuery);
        printedQuery = Mockito.mock(JPAQuery.class);
        Mockito.when(printedQuery.from(Mockito.any(EntityPath.class))).thenReturn(printedQuery);
        Mockito.when(printedQuery.join(Mockito.any(EntityPath.class))).thenReturn(printedQuery);
        Mockito.when(printedQuery.leftJoin(Mockito.any(EntityPath.class))).thenReturn(printedQuery);
        Mockito.when(printedQuery.on(Mockito.any(Predicate.class))).thenReturn(printedQuery);
        Mockito.when(printedQuery.where(Mockito.any(Predicate.class))).thenReturn(printedQuery);
        Mockito.when(printedQuery.orderBy(Mockito.any(OrderSpecifier.class))).thenReturn(printedQuery);
        queryFactories = Mockito.mockConstruction(JPAQueryFactory.class, (factory, context) -> {
            if (context.getCount() == 1) {
                queryFactory = factory;
                Mockito.when(factory.select(Mockito.any(Expression[].class))).thenReturn(jpaQuery);
            } else {
                printedQueryFactory = factory;
                Mockito.when(factory.select(Mockito.any(Expression[].class))).thenReturn(printedQuery);
            }
        });
    }

    @AfterEach
    void afterEach() {
        queryFactories.close();
    }

    @Test
    void buildsRefusalAndPrintedDateQueriesForActiveCourt() {
        final InOrder order = Mockito.inOrder(jpaQuery);
        List<Tuple> rows = List.of(row(JUROR_NUMBER, REFUSAL_DATE));
        Mockito.when(jpaQuery.fetch()).thenReturn(rows);
        Mockito.when(printedQuery.fetch()).thenReturn(List.of());

        repository.findJurorsEligibleForDeferralDeniedLetter(
            CourtLetterSearchCriteria.builder().build(), OWNER);

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery, Mockito.times(1)).where(Mockito.any(Predicate.class));
        Mockito.verify(queryFactory).select(PROJECTION);
        Mockito.verify(printedQueryFactory).select(PRINT_PROJECTION);
        Mockito.verify(jpaQuery).join(HISTORY);
        Mockito.verify(jpaQuery).on(HISTORY.jurorNumber.eq(JUROR.jurorNumber)
                .and(HISTORY.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(HISTORY.historyCode.eq(HistoryCodeMod.DEFERRED_POOL_MEMBER))
                .and(HISTORY.otherInformation.contains("Deferral Denied"))
                .and(HISTORY.dateCreatedDateOnly.after(JUROR.bureauTransferDate)));
        order.verify(jpaQuery).orderBy(HISTORY.dateCreatedDateOnly.desc(),
            POOL.juror.jurorNumber.asc());
        Mockito.verify(printedQuery).join(HISTORY);
        Mockito.verify(printedQuery).on(HISTORY.jurorNumber.eq(JUROR.jurorNumber)
                .and(HISTORY.historyCode.eq(HistoryCodeMod.NON_DEFERRED_LETTER))
                .and(HISTORY.dateCreatedDateOnly.after(JUROR.bureauTransferDate)));
        Mockito.verify(printedQuery).where(JUROR.excusalRejected.eq(ExcusalCodeEnum.Z.getCode())
            .and(POOL.isActive.isTrue())
            .and(POOL.pool.poolNumber.in(List.of(POOL_NUMBER)))
            .and(POOL.owner.eq(OWNER)));
        Mockito.verify(printedQuery, Mockito.times(1)).where(Mockito.any(Predicate.class));
    }

    @Test
    void appliesSearchFilters() {
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());
        Mockito.when(printedQuery.fetch()).thenReturn(List.of());

        assertThat(repository.findJurorsEligibleForDeferralDeniedLetter(CourtLetterSearchCriteria.builder()
            .jurorNumber("1234").jurorName("John Doe").postcode(" te1 4st ").poolNumber("123").build(),
            OWNER)).isEmpty();

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(POOL.juror.jurorNumber.startsWith("1234"));
        Mockito.verify(jpaQuery).where(
            POOL.juror.firstName.concat(" ").concat(POOL.juror.lastName).containsIgnoreCase("John Doe"));
        Mockito.verify(jpaQuery).where(POOL.juror.postcode.trim().eq(POSTCODE));
        Mockito.verify(jpaQuery).where(POOL.pool.poolNumber.startsWith("123"));
        Mockito.verify(jpaQuery, Mockito.times(5)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void ignoresAbsentFilters(String value) {
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());
        Mockito.when(printedQuery.fetch()).thenReturn(List.of());

        assertThat(repository.findJurorsEligibleForDeferralDeniedLetter(
            new CourtLetterSearchCriteria(value, value, value, value, false),
            OWNER)).isEmpty();

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery, Mockito.times(1)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @CsvSource({"-1,false", "0,false", "1,false", "-1,true", "0,true", "1,true"})
    void handlesPrintDatesBeforeOnAndAfterRefusal(int daysAfterRefusal, boolean includePrinted) {
        LocalDateTime datePrinted = REFUSAL_DATE.plusDays(daysAfterRefusal).atTime(10, 30);
        List<Tuple> rows = List.of(row(JUROR_NUMBER, REFUSAL_DATE));
        Mockito.when(jpaQuery.fetch()).thenReturn(rows);
        List<Tuple> printedRows = List.of(printedRow(JUROR_NUMBER, datePrinted));
        Mockito.when(printedQuery.fetch()).thenReturn(printedRows);

        List<DeferralDeniedLetterList> results = repository.findJurorsEligibleForDeferralDeniedLetter(
            CourtLetterSearchCriteria.builder().includePrinted(includePrinted).build(),
            OWNER);

        if (!includePrinted && daysAfterRefusal >= 0) {
            assertThat(results).isEmpty();
        } else {
            assertThat(results).singleElement().satisfies(letter -> {
                assertThat(letter.getPoolNumber()).isEqualTo(POOL_NUMBER);
                assertThat(letter.getJurorNumber()).isEqualTo(JUROR_NUMBER);
                assertThat(letter.getFirstName()).isEqualTo("John");
                assertThat(letter.getLastName()).isEqualTo("Doe");
                assertThat(letter.getPostcode()).isEqualTo(POSTCODE);
                assertThat(letter.getStatus()).isEqualTo("Responded");
                assertThat(letter.isActive()).isTrue();
                assertThat(letter.getRefusalDate()).isEqualTo(REFUSAL_DATE);
                assertThat(letter.getOtherInformation()).isEqualTo("Deferral Denied: work commitments");
                assertThat(letter.getDatePrinted()).isEqualTo(daysAfterRefusal >= 0 ? datePrinted : null);
            });
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void keepsMostRecentRefusalRegardlessOfRowOrder(boolean latestFirst) {
        Tuple latest = row(JUROR_NUMBER, REFUSAL_DATE);
        Tuple older = row(JUROR_NUMBER, REFUSAL_DATE.minusDays(7));
        Mockito.when(jpaQuery.fetch()).thenReturn(latestFirst ? List.of(latest, older) : List.of(older, latest));
        Mockito.when(printedQuery.fetch()).thenReturn(List.of());

        List<DeferralDeniedLetterList> results = repository.findJurorsEligibleForDeferralDeniedLetter(
            CourtLetterSearchCriteria.builder().build(), OWNER);

        assertThat(results).singleElement().extracting(DeferralDeniedLetterList::getRefusalDate)
            .isEqualTo(REFUSAL_DATE);
    }

    @Test
    void sortsUnprintedFirstThenPrintedDatesDescendingAndIgnoresUnknownJurors() {
        List<Tuple> rows = List.of(row(JUROR_NUMBER, REFUSAL_DATE), row(SECOND_JUROR_NUMBER, REFUSAL_DATE),
            row(THIRD_JUROR_NUMBER, REFUSAL_DATE), row(OTHER_UNPRINTED_JUROR_NUMBER, REFUSAL_DATE));
        Mockito.when(jpaQuery.fetch()).thenReturn(rows);
        List<Tuple> printedRows = List.of(printedRow(JUROR_NUMBER, REFUSAL_DATE.atTime(10, 0)),
            printedRow(SECOND_JUROR_NUMBER, REFUSAL_DATE.plusDays(1).atTime(10, 0)),
            printedRow(UNKNOWN_JUROR_NUMBER, REFUSAL_DATE.atTime(10, 0)));
        Mockito.when(printedQuery.fetch()).thenReturn(printedRows);

        List<DeferralDeniedLetterList> results = repository.findJurorsEligibleForDeferralDeniedLetter(
            CourtLetterSearchCriteria.builder().includePrinted(true).build(), OWNER);

        assertThat(results.subList(0, 2)).extracting(DeferralDeniedLetterList::getJurorNumber)
            .containsExactlyInAnyOrder(THIRD_JUROR_NUMBER, OTHER_UNPRINTED_JUROR_NUMBER);
        assertThat(results.subList(2, 4)).extracting(DeferralDeniedLetterList::getJurorNumber)
            .containsExactly(SECOND_JUROR_NUMBER, JUROR_NUMBER);
    }

    private Tuple row(String jurorNumber, LocalDate date) {
        Tuple tuple = Mockito.mock(Tuple.class);
        Mockito.when(tuple.get(QPoolRequest.poolRequest.poolNumber)).thenReturn(POOL_NUMBER);
        Mockito.when(tuple.get(JUROR.jurorNumber)).thenReturn(jurorNumber);
        Mockito.when(tuple.get(JUROR.firstName)).thenReturn("John");
        Mockito.when(tuple.get(JUROR.lastName)).thenReturn("Doe");
        Mockito.when(tuple.get(JUROR.postcode)).thenReturn(POSTCODE);
        Mockito.when(tuple.get(POOL.deferralDate)).thenReturn(null);
        Mockito.when(tuple.get(QJurorStatus.jurorStatus.statusDesc)).thenReturn("Responded");
        Mockito.when(tuple.get(HISTORY.dateCreatedDateOnly)).thenReturn(date);
        Mockito.when(tuple.get(HISTORY.otherInformation)).thenReturn("Deferral Denied: work commitments");
        Mockito.when(tuple.get(POOL.isActive)).thenReturn(true);

        return tuple;
    }

    private Tuple printedRow(String jurorNumber, LocalDateTime date) {
        Tuple tuple = Mockito.mock(Tuple.class);
        Mockito.when(tuple.get(QPoolRequest.poolRequest.poolNumber)).thenReturn(POOL_NUMBER);
        Mockito.when(tuple.get(JUROR.jurorNumber)).thenReturn(jurorNumber);
        Mockito.when(tuple.get(HISTORY.dateCreated)).thenReturn(date);

        return tuple;
    }

    private BooleanExpression basePredicate() {
        return JUROR.excusalRejected.eq(ExcusalCodeEnum.Z.getCode()).and(POOL.isActive.isTrue())
            .and(POOL.owner.eq(OWNER));
    }
}
