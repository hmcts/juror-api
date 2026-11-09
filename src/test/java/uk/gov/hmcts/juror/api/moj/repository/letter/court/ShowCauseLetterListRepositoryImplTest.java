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
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ShowCauseLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class ShowCauseLetterListRepositoryImplTest {

    private static final String OWNER = TestConstants.VALID_COURT_LOCATION;
    private static final String JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER;
    private static final String POSTCODE = TestConstants.VALID_POSTCODE;
    private static final String POOL_NUMBER = TestConstants.VALID_POOL_NUMBER;

    private JPAQuery<Tuple> jpaQuery;
    private JPAQueryFactory queryFactory;
    private MockedConstruction<JPAQueryFactory> queryFactories;

    private static final QJurorPool POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QJurorHistory HISTORY = QJurorHistory.jurorHistory;
    private static final QAppearance APPEARANCE = QAppearance.appearance;
    private static final Expression<?>[] PROJECTION = {
        QPoolRequest.poolRequest.poolNumber, JUROR.jurorNumber, JUROR.firstName, JUROR.lastName,
        JUROR.postcode, QJurorStatus.jurorStatus.statusDesc, HISTORY.dateCreated,
        POOL.isActive, APPEARANCE.attendanceDate
    };

    private final ShowCauseLetterListRepositoryImpl repository = new ShowCauseLetterListRepositoryImpl();

    @BeforeEach
    void beforeEach() {
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
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void selectsAbsentJurorsAndHandlesPrintedLetters(boolean includePrinted) {
        final InOrder order = Mockito.inOrder(jpaQuery);
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());

        assertThat(repository.findJurorsEligibleForShowCauseLetter(
            CourtLetterSearchCriteria.builder().includePrinted(includePrinted).build(),
            OWNER)).isEmpty();

        Mockito.verify(jpaQuery).where(basePredicate());
        if (!includePrinted) {
            Mockito.verify(jpaQuery).where(HISTORY.dateCreated.isNull());
        }
        Mockito.verify(jpaQuery, Mockito.times(includePrinted ? 1 : 2)).where(Mockito.any(Predicate.class));
        Mockito.verify(queryFactory).select(PROJECTION);
        Mockito.verify(jpaQuery).join(APPEARANCE);
        Mockito.verify(jpaQuery).on(APPEARANCE.jurorNumber.eq(JUROR.jurorNumber));
        Mockito.verify(jpaQuery).leftJoin(HISTORY);
        Mockito.verify(jpaQuery).on(HISTORY.jurorNumber.eq(JUROR.jurorNumber)
                .and(HISTORY.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(HISTORY.historyCode.eq(HistoryCodeMod.SHOW_CAUSE_LETTER)));
        if (includePrinted) {
            order.verify(jpaQuery).orderBy(
                new CaseBuilder().when(HISTORY.dateCreated.isNull()).then(0).otherwise(1).asc());
            order.verify(jpaQuery).orderBy(APPEARANCE.attendanceDate.desc());
            order.verify(jpaQuery).orderBy(JUROR.jurorNumber.asc());
            Mockito.verify(jpaQuery, Mockito.times(3)).orderBy(Mockito.any(OrderSpecifier.class));
        } else {
            order.verify(jpaQuery).orderBy(APPEARANCE.attendanceDate.desc());
            order.verify(jpaQuery).orderBy(JUROR.jurorNumber.asc());
            Mockito.verify(jpaQuery, Mockito.times(2)).orderBy(Mockito.any(OrderSpecifier.class));
        }
    }

    @Test
    void appliesSearchFilters() {
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());
        CourtLetterSearchCriteria criteria = CourtLetterSearchCriteria.builder().jurorNumber("1234")
            .jurorName("John Doe").postcode("  te1 4st  ").poolNumber("123").build();

        repository.findJurorsEligibleForShowCauseLetter(criteria, OWNER);

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(JUROR.jurorNumber.startsWith("1234"));
        Mockito.verify(jpaQuery).where(
            JUROR.firstName.concat(" ").concat(POOL.juror.lastName).containsIgnoreCase("John Doe"));
        Mockito.verify(jpaQuery).where(JUROR.postcode.trim().eq(POSTCODE));
        Mockito.verify(jpaQuery).where(QPoolRequest.poolRequest.poolNumber.startsWith("123"));
        Mockito.verify(jpaQuery).where(HISTORY.dateCreated.isNull());
        Mockito.verify(jpaQuery, Mockito.times(6)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void ignoresAbsentFilters(String value) {
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());
        CourtLetterSearchCriteria criteria = new CourtLetterSearchCriteria(value, value, value, value, false);

        repository.findJurorsEligibleForShowCauseLetter(criteria, OWNER);

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(HISTORY.dateCreated.isNull());
        Mockito.verify(jpaQuery, Mockito.times(2)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mapsAbsentDateAndPrintedDate(boolean printed) {
        LocalDate absentDate = LocalDate.of(2026, 10, 12);
        LocalDateTime printedDate = printed ? absentDate.atTime(10, 30) : null;
        Tuple tuple = Mockito.mock(Tuple.class);
        Mockito.when(tuple.get(QPoolRequest.poolRequest.poolNumber)).thenReturn(POOL_NUMBER);
        Mockito.when(tuple.get(JUROR.jurorNumber)).thenReturn(JUROR_NUMBER);
        Mockito.when(tuple.get(JUROR.firstName)).thenReturn("John");
        Mockito.when(tuple.get(JUROR.lastName)).thenReturn("Doe");
        Mockito.when(tuple.get(JUROR.postcode)).thenReturn(POSTCODE);
        Mockito.when(tuple.get(QJurorStatus.jurorStatus.statusDesc)).thenReturn("Responded");
        Mockito.when(tuple.get(HISTORY.dateCreated)).thenReturn(printedDate);
        Mockito.when(tuple.get(POOL.isActive)).thenReturn(true);
        Mockito.when(tuple.get(APPEARANCE.attendanceDate)).thenReturn(absentDate);

        Mockito.when(jpaQuery.fetch()).thenReturn(List.of(tuple));

        List<ShowCauseLetterList> results = repository.findJurorsEligibleForShowCauseLetter(
            CourtLetterSearchCriteria.builder().includePrinted(printed).build(), OWNER);

        assertThat(results).singleElement().satisfies(letter -> {
            assertThat(letter.getPoolNumber()).isEqualTo(POOL_NUMBER);
            assertThat(letter.getJurorNumber()).isEqualTo(JUROR_NUMBER);
            assertThat(letter.getFirstName()).isEqualTo("John");
            assertThat(letter.getLastName()).isEqualTo("Doe");
            assertThat(letter.getPostcode()).isEqualTo(POSTCODE);
            assertThat(letter.getStatus()).isEqualTo("Responded");
            assertThat(letter.isActive()).isTrue();
            assertThat(letter.getAbsentDate()).isEqualTo(absentDate);
            assertThat(letter.getDatePrinted()).isEqualTo(printedDate);
        });
    }

    private BooleanExpression basePredicate() {
        return POOL.isActive.isTrue().and(POOL.status.status.in(List.of(IJurorStatus.RESPONDED,
            IJurorStatus.FAILED_TO_ATTEND)).and(APPEARANCE.noShow.isTrue())
            .and(APPEARANCE.attendanceType.eq(AttendanceType.ABSENT))
            .and(POOL.owner.eq(OWNER)));
    }
}
