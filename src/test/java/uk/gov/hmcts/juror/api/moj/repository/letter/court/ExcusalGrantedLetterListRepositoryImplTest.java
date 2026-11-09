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
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"unchecked", "PMD.ExcessiveImports"})
class ExcusalGrantedLetterListRepositoryImplTest {

    private static final String OWNER = TestConstants.VALID_COURT_LOCATION;
    private static final String JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER;
    private static final String POSTCODE = TestConstants.VALID_POSTCODE;
    private static final String SECOND_JUROR_NUMBER = TestConstants.VALID_JUROR_NUMBER_2;
    private static final String POOL_NUMBER = TestConstants.VALID_POOL_NUMBER;

    private JPAQuery<Tuple> jpaQuery;
    private JPAQueryFactory queryFactory;
    private MockedConstruction<JPAQueryFactory> queryFactories;
    private MockedStatic<SecurityUtil> security;

    private static final QJurorPool POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QJurorHistory HISTORY = QJurorHistory.jurorHistory;
    private static final LocalDate EXCUSAL_DATE = LocalDate.of(2026, 10, 12);
    private static final Expression<?>[] PROJECTION = {
        POOL.pool.poolNumber, JUROR.jurorNumber, JUROR.firstName, JUROR.lastName, JUROR.postcode,
        QJurorStatus.jurorStatus.statusDesc, JUROR.excusalDate, HISTORY.dateCreated,
        POOL.isActive, QExcusalCode.excusalCode.description
    };

    private final ExcusalGrantedLetterListRepositoryImpl repository = new ExcusalGrantedLetterListRepositoryImpl();

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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void selectsExcusedJurorsAndHandlesPrintedLetters(boolean includePrinted) {
        final InOrder order = Mockito.inOrder(jpaQuery);
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());

        assertThat(repository.findJurorsEligibleForExcusalGrantedLetter(
            CourtLetterSearchCriteria.builder().includePrinted(includePrinted).build())).isEmpty();

        BooleanExpression excused = JUROR.excusalDate.isNotNull().and(POOL.isActive.isTrue());
        Mockito.verify(queryFactory).select(PROJECTION);
        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(includePrinted
            ? excused.or(HISTORY.dateCreated.isNotNull()) : excused.and(HISTORY.dateCreated.isNull()));
        Mockito.verify(jpaQuery, Mockito.times(2)).where(Mockito.any(Predicate.class));
        Mockito.verify(jpaQuery).join(QExcusalCode.excusalCode);
        Mockito.verify(jpaQuery).on(QExcusalCode.excusalCode.code.eq(JUROR.excusalCode));
        Mockito.verify(jpaQuery).leftJoin(HISTORY);
        Mockito.verify(jpaQuery).on(HISTORY.jurorNumber.eq(JUROR.jurorNumber)
                .and(HISTORY.poolNumber.eq(QPoolRequest.poolRequest.poolNumber))
                .and(HISTORY.historyCode.eq(HistoryCodeMod.EXCUSED_LETTER))
                .and(HISTORY.dateCreatedDateOnly.after(JUROR.bureauTransferDate)));
        if (includePrinted) {
            order.verify(jpaQuery).orderBy(
                new CaseBuilder().when(HISTORY.dateCreated.isNull()).then(0).otherwise(1).asc());
            order.verify(jpaQuery).orderBy(POOL.juror.jurorNumber.asc());
            order.verify(jpaQuery).orderBy(JUROR.excusalDate.desc());
            Mockito.verify(jpaQuery, Mockito.times(3)).orderBy(Mockito.any(OrderSpecifier.class));
        } else {
            order.verify(jpaQuery).orderBy(POOL.juror.jurorNumber.asc());
            order.verify(jpaQuery).orderBy(JUROR.excusalDate.desc());
            Mockito.verify(jpaQuery, Mockito.times(2)).orderBy(Mockito.any(OrderSpecifier.class));
        }
    }

    @Test
    void appliesSearchFilters() {
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());

        repository.findJurorsEligibleForExcusalGrantedLetter(CourtLetterSearchCriteria.builder()
            .jurorNumber("1234").jurorName("John Doe").postcode(" te1 4st ").poolNumber("123").build());

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(POOL.juror.jurorNumber.startsWith("1234"));
        Mockito.verify(jpaQuery).where(
            POOL.juror.firstName.concat(" ").concat(POOL.juror.lastName).containsIgnoreCase("John Doe"));
        Mockito.verify(jpaQuery).where(POOL.juror.postcode.trim().eq(POSTCODE));
        Mockito.verify(jpaQuery).where(POOL.pool.poolNumber.startsWith("123"));
        Mockito.verify(jpaQuery).where(
            JUROR.excusalDate.isNotNull().and(POOL.isActive.isTrue()).and(HISTORY.dateCreated.isNull()));
        Mockito.verify(jpaQuery, Mockito.times(6)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void ignoresAbsentFilters(String value) {
        Mockito.when(jpaQuery.fetch()).thenReturn(List.of());

        repository.findJurorsEligibleForExcusalGrantedLetter(
            new CourtLetterSearchCriteria(value, value, value, value, false));

        Mockito.verify(jpaQuery).where(basePredicate());
        Mockito.verify(jpaQuery).where(
            JUROR.excusalDate.isNotNull().and(POOL.isActive.isTrue()).and(HISTORY.dateCreated.isNull()));
        Mockito.verify(jpaQuery, Mockito.times(2)).where(Mockito.any(Predicate.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mapsExcusalFields(boolean printed) {
        LocalDateTime datePrinted = printed ? EXCUSAL_DATE.atTime(10, 30) : null;
        List<Tuple> rows = List.of(row(JUROR_NUMBER, datePrinted));
        Mockito.when(jpaQuery.fetch()).thenReturn(rows);

        List<ExcusalGrantedLetterList> results = repository.findJurorsEligibleForExcusalGrantedLetter(
            CourtLetterSearchCriteria.builder().includePrinted(printed).build());

        assertThat(results).singleElement().satisfies(letter -> {
            assertThat(letter.getPoolNumber()).isEqualTo(POOL_NUMBER);
            assertThat(letter.getJurorNumber()).isEqualTo(JUROR_NUMBER);
            assertThat(letter.getFirstName()).isEqualTo("John");
            assertThat(letter.getLastName()).isEqualTo("Doe");
            assertThat(letter.getPostcode()).isEqualTo(POSTCODE);
            assertThat(letter.getStatus()).isEqualTo("Excused");
            assertThat(letter.isActive()).isTrue();
            assertThat(letter.getDateExcused()).isEqualTo(EXCUSAL_DATE);
            assertThat(letter.getReason()).isEqualTo("Work commitments");
            assertThat(letter.getDatePrinted()).isEqualTo(datePrinted);
        });
    }

    @Test
    void returnsOneEntryPerJurorWhenJoinsProduceDuplicateRows() {
        Tuple first = row(JUROR_NUMBER, null);
        List<Tuple> rows = List.of(first, first, row(SECOND_JUROR_NUMBER, null));
        Mockito.when(jpaQuery.fetch()).thenReturn(rows);

        List<ExcusalGrantedLetterList> results = repository.findJurorsEligibleForExcusalGrantedLetter(
            CourtLetterSearchCriteria.builder().build());

        assertThat(results).extracting(ExcusalGrantedLetterList::getJurorNumber)
            .containsExactlyInAnyOrder(JUROR_NUMBER, SECOND_JUROR_NUMBER);
    }

    private Tuple row(String jurorNumber, LocalDateTime printed) {
        Tuple tuple = Mockito.mock(Tuple.class);
        Mockito.when(tuple.get(POOL.pool.poolNumber)).thenReturn(POOL_NUMBER);
        Mockito.when(tuple.get(JUROR.jurorNumber)).thenReturn(jurorNumber);
        Mockito.when(tuple.get(JUROR.firstName)).thenReturn("John");
        Mockito.when(tuple.get(JUROR.lastName)).thenReturn("Doe");
        Mockito.when(tuple.get(JUROR.postcode)).thenReturn(POSTCODE);
        Mockito.when(tuple.get(QJurorStatus.jurorStatus.statusDesc)).thenReturn("Excused");
        Mockito.when(tuple.get(JUROR.excusalDate)).thenReturn(EXCUSAL_DATE);
        Mockito.when(tuple.get(HISTORY.dateCreated)).thenReturn(printed);
        Mockito.when(tuple.get(POOL.isActive)).thenReturn(true);
        Mockito.when(tuple.get(QExcusalCode.excusalCode.description)).thenReturn("Work commitments");

        return tuple;
    }

    private BooleanExpression basePredicate() {
        return POOL.status.status.eq(IJurorStatus.EXCUSED).and(POOL.owner.eq(OWNER));
    }
}
