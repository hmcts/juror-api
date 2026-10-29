package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Keep query construction, mapping and duplicate-selection scenarios together for this repository.
@SuppressWarnings({"PMD.TooManyMethods", "unchecked", "PMD.ExcessiveImports"})
class DeferralGrantedLetterListRepositoryImplTest {

    private static final String OWNER = TestConstants.VALID_COURT_LOCATION;
    private static final String JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER;
    private static final String POSTCODE = TestConstants.VALID_POSTCODE;
    private static final String POOL_NUMBER = TestConstants.VALID_POOL_NUMBER;
    private static final String SECOND_JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER_2;
    private static final LocalDate DEFERRAL_DATE = LocalDate.of(2026, 11, 2);
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QJurorHistory HISTORY = QJurorHistory.jurorHistory;
    private static final QExcusalCode EXCUSAL_CODE = QExcusalCode.excusalCode;
    private static final Expression<?>[] PROJECTION = {
        JUROR_POOL.pool.poolNumber, JUROR.jurorNumber, JUROR.firstName, JUROR.lastName,
        JUROR.postcode, JUROR_POOL.deferralDate, QJurorStatus.jurorStatus.statusDesc,
        HISTORY.dateCreated, JUROR_POOL.isActive, EXCUSAL_CODE.description
    };

    private final DeferralGrantedLetterListRepositoryImpl repository = new DeferralGrantedLetterListRepositoryImpl();
    private JPAQuery<Tuple> jpaQuery;
    private JPAQueryFactory queryFactory;
    private MockedConstruction<JPAQueryFactory> queryFactories;
    private MockedStatic<SecurityUtil> security;

    @BeforeEach
    void beforeEach() {
        security = Mockito.mockStatic(SecurityUtil.class);
        security.when(SecurityUtil::getActiveOwner).thenReturn(OWNER);
        jpaQuery = Mockito.mock(JPAQuery.class);
        Mockito.when(jpaQuery.from(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.join(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.leftJoin(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.on(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.where(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.orderBy(Mockito.any(OrderSpecifier.class))).thenReturn(jpaQuery);
        queryFactories = Mockito.mockConstruction(JPAQueryFactory.class, (factory, context) -> {
            queryFactory = factory;
            Mockito.when(factory.select(Mockito.any(Expression[].class))).thenReturn(jpaQuery);
        });
    }

    @AfterEach
    void afterEach() {
        queryFactories.close();
        security.close();
    }

    @Test
    void buildsQueryForDeferredJurorsOwnedByActiveCourt() {
        findLetters(CourtLetterSearchCriteria.builder().build(), List.of());

        Mockito.verify(queryFactory).select(PROJECTION);
        Mockito.verify(jpaQuery).from(JUROR_POOL);
        Mockito.verify(jpaQuery).join(JUROR);
        Mockito.verify(jpaQuery).on(JUROR.eq(JUROR_POOL.juror));
        Mockito.verify(jpaQuery).join(QPoolRequest.poolRequest);
        Mockito.verify(jpaQuery).on(QPoolRequest.poolRequest.eq(JUROR_POOL.pool));
        Mockito.verify(jpaQuery).join(QJurorStatus.jurorStatus);
        Mockito.verify(jpaQuery).on(QJurorStatus.jurorStatus.eq(JUROR_POOL.status));
        Mockito.verify(jpaQuery).join(EXCUSAL_CODE);
        Mockito.verify(jpaQuery).on(EXCUSAL_CODE.code.eq(JUROR_POOL.deferralCode));
        Mockito.verify(jpaQuery).leftJoin(HISTORY);
        Mockito.verify(jpaQuery).on(HISTORY.jurorNumber.eq(JUROR.jurorNumber)
            .and(HISTORY.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
            .and(HISTORY.historyCode.eq(HistoryCodeMod.DEFERRED_LETTER))
            .and(HISTORY.otherInformationDate.eq(JUROR_POOL.deferralDate))
            .and(HISTORY.dateCreatedDateOnly.after(JUROR.bureauTransferDate)));
        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(currentlyDeferred().and(HISTORY.dateCreated.isNull()));
        Mockito.verify(jpaQuery).fetch();
    }

    @Test
    void appliesSearchFiltersAndNormalisesPostcode() {
        CourtLetterSearchCriteria criteria = CourtLetterSearchCriteria.builder()
            .jurorNumber("1234").jurorName("John Doe").postcode("  te1 4st  ").poolNumber("123").build();

        findLetters(criteria, List.of());

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(JUROR_POOL.juror.jurorNumber.startsWith("1234"));
        Mockito.verify(jpaQuery).where(JUROR_POOL.juror.firstName.concat(" ").concat(JUROR_POOL.juror.lastName)
            .containsIgnoreCase("John Doe"));
        Mockito.verify(jpaQuery).where(JUROR_POOL.juror.postcode.trim().eq(POSTCODE));
        Mockito.verify(jpaQuery).where(JUROR_POOL.pool.poolNumber.startsWith("123"));
        Mockito.verify(jpaQuery).where(currentlyDeferred().and(HISTORY.dateCreated.isNull()));
        Mockito.verify(jpaQuery, Mockito.times(6)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void ignoresAbsentSearchFilters(String value) {
        CourtLetterSearchCriteria criteria = new CourtLetterSearchCriteria(value, value, value, value, false);

        findLetters(criteria, List.of());

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(currentlyDeferred().and(HISTORY.dateCreated.isNull()));
        Mockito.verify(jpaQuery, Mockito.times(2)).where(Mockito.any(Predicate.class));
    }

    @Test
    void excludesPrintedLettersAndOrdersByJurorThenLatestDeferral() {
        final InOrder order = Mockito.inOrder(jpaQuery);
        findLetters(CourtLetterSearchCriteria.builder().includePrinted(false).build(), List.of());

        Mockito.verify(jpaQuery).where(currentlyDeferred().and(HISTORY.dateCreated.isNull()));
        order.verify(jpaQuery).orderBy(JUROR_POOL.juror.jurorNumber.asc());
        order.verify(jpaQuery).orderBy(JUROR_POOL.deferralDate.desc());
        Mockito.verify(jpaQuery, Mockito.times(2)).orderBy(Mockito.any(OrderSpecifier.class));
    }

    @Test
    void includesPreviouslyPrintedLettersAndOrdersUnprintedFirst() {
        final InOrder order = Mockito.inOrder(jpaQuery);
        findLetters(CourtLetterSearchCriteria.builder().includePrinted(true).build(), List.of());

        Mockito.verify(jpaQuery).where(currentlyDeferred().or(HISTORY.dateCreated.isNotNull()));
        order.verify(jpaQuery).orderBy(new CaseBuilder()
            .when(HISTORY.dateCreated.isNull()).then(0).otherwise(1).asc());
        order.verify(jpaQuery).orderBy(JUROR_POOL.juror.jurorNumber.asc());
        order.verify(jpaQuery).orderBy(JUROR_POOL.deferralDate.desc());
        Mockito.verify(jpaQuery, Mockito.times(3)).orderBy(Mockito.any(OrderSpecifier.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mapsAllSelectedFields(boolean printed) {
        LocalDateTime datePrinted = printed ? LocalDateTime.of(2026, 10, 15, 10, 30) : null;
        Tuple tuple = createTuple(JUROR_NUMBER, DEFERRAL_DATE, datePrinted, !printed, "Work commitments");

        List<DeferralGrantedLetterList> results = findLetters(
            CourtLetterSearchCriteria.builder().includePrinted(printed).build(), List.of(tuple));

        assertThat(results).singleElement().satisfies(letter -> {
            assertThat(letter.getPoolNumber()).isEqualTo(POOL_NUMBER);
            assertThat(letter.getJurorNumber()).isEqualTo(JUROR_NUMBER);
            assertThat(letter.getFirstName()).isEqualTo("John");
            assertThat(letter.getLastName()).isEqualTo("Doe");
            assertThat(letter.getPostcode()).isEqualTo(POSTCODE);
            assertThat(letter.getStatus()).isEqualTo("Deferred");
            assertThat(letter.getDeferralDate()).isEqualTo(DEFERRAL_DATE);
            assertThat(letter.getDeferralReason()).isEqualTo("Work commitments");
            assertThat(letter.getDatePrinted()).isEqualTo(datePrinted);
            assertThat(letter.isActive()).isEqualTo(!printed);
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void keepsLatestDeferralForEachJurorRegardlessOfRowOrder(boolean latestFirst) {
        Tuple older = createTuple(JUROR_NUMBER, DEFERRAL_DATE.minusDays(7), null, true, "Previous reason");
        Tuple latest = createTuple(JUROR_NUMBER, DEFERRAL_DATE, null, true, "Latest reason");
        Tuple otherJuror = createTuple(SECOND_JUROR_NUMBER, DEFERRAL_DATE, null, true, "Other juror");
        List<Tuple> rows = latestFirst ? List.of(latest, older, otherJuror) : List.of(older, latest, otherJuror);

        List<DeferralGrantedLetterList> results = findLetters(CourtLetterSearchCriteria.builder().build(), rows);

        assertThat(results).extracting(DeferralGrantedLetterList::getJurorNumber)
            .containsExactlyInAnyOrder(JUROR_NUMBER, SECOND_JUROR_NUMBER);
        assertThat(results).filteredOn(letter -> JUROR_NUMBER.equals(letter.getJurorNumber()))
            .singleElement().satisfies(letter -> {
                assertThat(letter.getDeferralDate()).isEqualTo(DEFERRAL_DATE);
                assertThat(letter.getDeferralReason()).isEqualTo("Latest reason");
            });
    }

    @Test
    void removesDuplicatesWithSameDeferralDate() {
        Tuple tuple = createTuple(JUROR_NUMBER, DEFERRAL_DATE, null, true, "Work commitments");

        List<DeferralGrantedLetterList> results = findLetters(
            CourtLetterSearchCriteria.builder().build(), List.of(tuple, tuple));

        assertThat(results).singleElement()
            .extracting(DeferralGrantedLetterList::getJurorNumber).isEqualTo(JUROR_NUMBER);
    }

    @Test
    void returnsEmptyListWhenNoJurorsMatch() {
        assertThat(findLetters(CourtLetterSearchCriteria.builder().build(), List.of())).isEmpty();
    }

    private List<DeferralGrantedLetterList> findLetters(CourtLetterSearchCriteria criteria, List<Tuple> rows) {
        Mockito.when(jpaQuery.fetch()).thenReturn(rows);
        return repository.findJurorsEligibleForDeferralGrantedLetter(criteria);
    }

    private Tuple createTuple(String jurorNumber, LocalDate deferralDate, LocalDateTime datePrinted,
                              boolean active, String reason) {
        Tuple tuple = Mockito.mock(Tuple.class);
        Mockito.when(tuple.get(JUROR_POOL.pool.poolNumber)).thenReturn(POOL_NUMBER);
        Mockito.when(tuple.get(JUROR.jurorNumber)).thenReturn(jurorNumber);
        Mockito.when(tuple.get(JUROR.firstName)).thenReturn("John");
        Mockito.when(tuple.get(JUROR.lastName)).thenReturn("Doe");
        Mockito.when(tuple.get(JUROR.postcode)).thenReturn(POSTCODE);
        Mockito.when(tuple.get(JUROR_POOL.deferralDate)).thenReturn(deferralDate);
        Mockito.when(tuple.get(QJurorStatus.jurorStatus.statusDesc)).thenReturn("Deferred");
        Mockito.when(tuple.get(HISTORY.dateCreated)).thenReturn(datePrinted);
        Mockito.when(tuple.get(JUROR_POOL.isActive)).thenReturn(active);
        Mockito.when(tuple.get(EXCUSAL_CODE.description)).thenReturn(reason);
        return tuple;
    }

    private BooleanExpression basePredicate() {
        return JUROR_POOL.status.status.eq(IJurorStatus.DEFERRED)
            .and(EXCUSAL_CODE.code.ne(ExcusalCodeEnum.P.getCode()))
            .and(JUROR_POOL.owner.eq(OWNER));
    }

    private BooleanExpression currentlyDeferred() {
        return JUROR_POOL.deferralDate.isNotNull().and(JUROR_POOL.isActive.isTrue());
    }
}
