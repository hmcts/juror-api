package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolMemberFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Arrays;

import static uk.gov.hmcts.juror.api.moj.repository.IJurorPoolRepository.ATTENDANCE;
import static uk.gov.hmcts.juror.api.moj.repository.IJurorPoolRepository.CHECKED_IN_TODAY;


@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.TooManyMethods"
})
@ExtendWith(MockitoExtension.class)
class JurorPoolRepositoryImplTest {

    private static final QPanel PANEL = QPanel.panel;
    private static final QJuror JUROR = QJuror.juror;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QAppearance APPEARANCE = QAppearance.appearance;
    private static final QJurorStatus JUROR_STATUS = QJurorStatus.jurorStatus;

    @Mock
    private JPAQueryFactory queryFactory;
    @Mock
    private JPAQuery jpaQuery;

    private JurorPoolRepositoryImpl jurorPoolRepository;

    @BeforeEach
    void beforeEach() {
        jurorPoolRepository = Mockito.spy(new JurorPoolRepositoryImpl());
        Mockito.doReturn(queryFactory).when(jurorPoolRepository).getQueryFactory();

        Mockito.when(jpaQuery.where(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
    }

    @Nested
    class FetchThinPoolMembers {
        @BeforeEach
        void beforeEach() {
            Mockito.when(queryFactory.select(Mockito.any(Expression.class))).thenReturn(jpaQuery);
            Mockito.when(jpaQuery.from(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        }

        @Test
        void happyPath() {
            final String poolNumber = "12345678";
            final String owner = "415";

            Mockito.when(jpaQuery.fetch()).thenReturn(Arrays.asList("415"));

            jurorPoolRepository.fetchThinPoolMembers(poolNumber, owner);

            Mockito.verify(queryFactory, Mockito.times(1)).select(JUROR_POOL.juror.jurorNumber);
            Mockito.verify(jpaQuery, Mockito.times(1)).from(JUROR_POOL);
            Mockito.verify(jpaQuery, Mockito.times(1)).where(JUROR_POOL.pool.poolNumber.eq(poolNumber)
                .and(JUROR_POOL.owner.eq(owner))
                .and(JUROR_POOL.isActive.isTrue()));
            Mockito.verify(jpaQuery, Mockito.times(2)).fetch();
            Mockito.verifyNoMoreInteractions(jpaQuery);
        }

        @Test
        void happyPathForBureauPool() {
            final String poolNumber = "12345678";
            final String owner = "415";

            Mockito.when(jpaQuery.fetch()).thenReturn(Arrays.asList(SecurityUtil.BUREAU_OWNER));

            jurorPoolRepository.fetchThinPoolMembers(poolNumber, owner);

            Mockito.verify(queryFactory, Mockito.times(1)).select(JUROR_POOL.juror.jurorNumber);
            Mockito.verify(jpaQuery, Mockito.times(1)).from(JUROR_POOL);
            Mockito.verify(jpaQuery, Mockito.times(1)).where(JUROR_POOL.pool.poolNumber.eq(poolNumber)
                .and(JUROR_POOL.owner.eq(owner))
                .and(JUROR_POOL.isActive.isTrue()));
            Mockito.verify(jpaQuery, Mockito.times(2)).fetch();
            Mockito.verifyNoMoreInteractions(jpaQuery);
        }

        @Test
        void happyPathForBureauUser() {
            final String poolNumber = "12345678";
            final String owner = SecurityUtil.BUREAU_OWNER;

            Mockito.when(jpaQuery.fetch()).thenReturn(Arrays.asList("415"));

            jurorPoolRepository.fetchThinPoolMembers(poolNumber, owner);

            Mockito.verify(queryFactory, Mockito.times(1)).select(JUROR_POOL.juror.jurorNumber);
            Mockito.verify(jpaQuery, Mockito.times(1)).from(JUROR_POOL);
            Mockito.verify(jpaQuery, Mockito.times(1)).where(JUROR_POOL.pool.poolNumber.eq(poolNumber)
                .and(JUROR_POOL.owner.eq(owner))
                .and(JUROR_POOL.isActive.isTrue()));
            Mockito.verify(jpaQuery, Mockito.times(2)).fetch();
            Mockito.verifyNoMoreInteractions(jpaQuery);
        }

        @Test
        void poolNotFound() {
            final String poolNumber = "12345678";
            final String owner = "415";

            Mockito.when(jpaQuery.fetch()).thenReturn(Arrays.asList());

            Assertions.assertThatExceptionOfType(MojException.NotFound.class)
                .isThrownBy(() -> jurorPoolRepository.fetchThinPoolMembers(poolNumber, owner));
        }

        @Test
        void poolNotAllowed() {
            final String poolNumber = "12345678";
            final String owner = "415";

            Mockito.when(jpaQuery.fetch()).thenReturn(Arrays.asList("416"));

            Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
                .isThrownBy(() -> jurorPoolRepository.fetchThinPoolMembers(poolNumber, owner));
        }
    }


    @Nested
    class FetchFilteredPoolMembers {
        @BeforeEach
        void beforeEach() {
            Mockito.when(queryFactory.from(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
            Mockito.when(jpaQuery.join(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
            Mockito.when(jpaQuery.on(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
            Mockito.when(jpaQuery.leftJoin(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
            Mockito.when(jpaQuery.select(Mockito.any(Expression[].class))).thenReturn(jpaQuery);
            Mockito.when(jpaQuery.distinct()).thenReturn(jpaQuery);
        }

        @Test
        void happyPath() {
            final String ownerId = "415";
            final LocalDate today = LocalDate.now();
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().build(), ownerId);
            Mockito.verify(queryFactory, Mockito.times(1)).from(JUROR_POOL);
            Mockito.verify(jpaQuery, Mockito.times(1)).join(JUROR);
            Mockito.verify(jpaQuery, Mockito.times(1))
                .on(JUROR_POOL.juror.eq(JUROR));
            Mockito.verify(jpaQuery, Mockito.times(1)).leftJoin(APPEARANCE);
            Mockito.verify(jpaQuery, Mockito.times(1))
                .on(JUROR.jurorNumber.eq(APPEARANCE.jurorNumber)
                    .and(APPEARANCE.attendanceDate.eq(today)));
            Mockito.verify(jpaQuery, Mockito.times(1)).leftJoin(JUROR_STATUS);
            Mockito.verify(jpaQuery, Mockito.times(1))
                .on(JUROR_POOL.status.status.eq(JUROR_STATUS.status));
            Mockito.verify(jpaQuery, Mockito.times(1)).leftJoin(PANEL);
            Mockito.verify(jpaQuery, Mockito.times(1))
                .on(JUROR.eq(PANEL.juror));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR_POOL.isActive.isTrue());
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR_POOL.pool.poolNumber.eq("41500000"));

            Mockito.verify(jpaQuery, Mockito.times(1)).select(
                Mockito.eq(JUROR_POOL.juror.jurorNumber),
                Mockito.eq(JUROR_POOL.juror.firstName),
                Mockito.eq(JUROR_POOL.juror.lastName),
                Mockito.eq(JUROR_POOL.juror.postcode),
                Mockito.eq(new CaseBuilder()
                    .when(Expressions.asBoolean(JUROR_POOL.onCall.eq(true)))
                    .then(PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL.getKeyString())
                    .when(PANEL.result.eq(PanelResult.JUROR))
                    .then(PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL.getKeyString())
                    .when(Expressions.booleanOperation(
                        Ops.AND,
                        APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        APPEARANCE.attendanceDate.eq(LocalDate.now())
                    ))
                    .then(PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE.getKeyString())
                    .when(JUROR_POOL.nextDate.eq(LocalDate.now()))
                    .then(PoolMemberFilterRequestQuery.AttendanceEnum.OTHER.getKeyString())
                    .otherwise("").max()
                    .as(ATTENDANCE)),
                Mockito.eq(Expressions.booleanOperation(
                    Ops.AND,
                    APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                    APPEARANCE.attendanceDate.eq(LocalDate.now())
                ).as(CHECKED_IN_TODAY)),
                Mockito.eq(APPEARANCE.timeIn),
                Mockito.eq(JUROR_POOL.nextDate),
                Mockito.eq(JUROR_STATUS.statusDesc));
            Mockito.verify(jpaQuery, Mockito.times(1)).groupBy(
                Mockito.eq(JUROR_POOL.juror.jurorNumber),
                Mockito.eq(JUROR_POOL.juror.firstName),
                Mockito.eq(JUROR_POOL.juror.lastName),
                Mockito.eq(JUROR_POOL.juror.postcode),
                Mockito.eq(CHECKED_IN_TODAY),
                Mockito.eq(APPEARANCE.timeIn),
                Mockito.eq(JUROR_POOL.nextDate),
                Mockito.eq(JUROR_STATUS.statusDesc));

            Mockito.verify(jurorPoolRepository, Mockito.times(1)).getAttendanceCase();
            Mockito.verify(jurorPoolRepository, Mockito.times(2)).getCheckedInBoolean();
        }


        @Test
        void checkJurorOwner() {
            String ownerId = "415";
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().build(), ownerId);
            Mockito.verify(jpaQuery, Mockito.times(1)).where(JUROR_POOL.owner.eq(ownerId));

            ownerId = SecurityUtil.BUREAU_OWNER;
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().build(), ownerId);
            Mockito.verify(jpaQuery, Mockito.times(1)).where(JUROR_POOL.owner.eq(ownerId));
        }

        @Test
        void noFilters() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().build(), "");
            Mockito.verify(jpaQuery, Mockito.times(3))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR.jurorNumber.like("123456%"));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR.firstName.like("TEST%"));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR.lastName.like("PERSON%"));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.booleanOperation(
                    Ops.AND,
                    APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                    APPEARANCE.attendanceDate.eq(LocalDate.now())
                ));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR_POOL.nextDate.isNotNull());
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR_POOL.nextDate.isNull());
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR_STATUS.statusDesc.in(Arrays.asList("Status1", "Status2")));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR_POOL.onCall.eq(true)
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.FALSE
                    .or(PANEL.result.eq(PanelResult.JUROR))
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.FALSE
                    .or(Expressions.FALSE)
                    .or(Expressions.booleanOperation(
                        Ops.AND,
                        APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        APPEARANCE.attendanceDate.eq(LocalDate.now())))
                    .or(Expressions.FALSE));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.FALSE
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE)
                    .or(JUROR_POOL.nextDate.eq(LocalDate.now())
                        .and(JUROR_POOL.onCall.ne(true))
                        .and(PANEL.result.ne(PanelResult.JUROR))
                        .and(APPEARANCE.appearanceStage.ne(AppearanceStage.CHECKED_IN))));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR_POOL.onCall.eq(true)
                    .or(PANEL.result.eq(PanelResult.JUROR))
                    .or(Expressions.booleanOperation(
                        Ops.AND,
                        APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        APPEARANCE.attendanceDate.eq(LocalDate.now())))
                    .or(JUROR_POOL.nextDate.eq(LocalDate.now())));

        }

        @Test
        void filterByJurorNumber() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().jurorNumber("123456").build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR.jurorNumber.like("123456%"));
        }

        @Test
        void filterByFirstName() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().firstName("Test").build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR.firstName.startsWithIgnoreCase("Test"));
        }

        @Test
        void filterByLastName() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().lastName("Person").build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR.lastName.startsWithIgnoreCase("Person"));
        }

        @Test
        void dontFilterByFalseCheckedIn() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().checkedIn(false).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(3))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.booleanOperation(
                    Ops.AND,
                    APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                    APPEARANCE.attendanceDate.eq(LocalDate.now())
                ));
        }

        @Test
        void filterByCheckedIn() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().checkedIn(true).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(Expressions.booleanOperation(
                    Ops.AND,
                    APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                    APPEARANCE.attendanceDate.eq(LocalDate.now())
                ));
        }

        @Test
        void filterByNextDue() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder()
                .nextDue(Arrays.asList("set")).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR_POOL.nextDate.isNotNull());
        }

        @Test
        void filterByNextDueFalse() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder()
                .nextDue(Arrays.asList("notSet")).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR_POOL.nextDate.isNull());
        }

        @Test
        void filterByBothNextDueValues() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder()
                .nextDue(Arrays.asList("set", "notSet")).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(3))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR_POOL.nextDate.isNull());
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(JUROR_POOL.nextDate.isNotNull());
        }

        @Test
        void filterByStatus() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().statuses(
                Arrays.asList("Status1", "Status2")).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR_STATUS.statusDesc.in(Arrays.asList("Status1", "Status2")));
        }

        @Test
        void filterByAttendance() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().attendance(Arrays.asList(
                PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL)).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR_POOL.onCall.eq(true)
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE));

            Mockito.clearInvocations(jpaQuery);

            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().attendance(Arrays.asList(
                PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL)).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(Expressions.FALSE
                    .or(PANEL.result.eq(PanelResult.JUROR))
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE));

            Mockito.clearInvocations(jpaQuery);

            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().attendance(Arrays.asList(
                PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE)).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(Expressions.FALSE
                    .or(Expressions.FALSE)
                    .or(Expressions.booleanOperation(
                        Ops.AND,
                        APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        APPEARANCE.attendanceDate.eq(LocalDate.now())))
                    .or(Expressions.FALSE));

            Mockito.clearInvocations(jpaQuery);

            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().attendance(
                Arrays.asList(PoolMemberFilterRequestQuery.AttendanceEnum.OTHER)).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(Expressions.FALSE
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE)
                    .or(JUROR_POOL.nextDate.eq(LocalDate.now())
                        .and(JUROR_POOL.onCall.ne(true))
                        .and(PANEL.result.ne(PanelResult.JUROR))
                        .and(APPEARANCE.appearanceStage.ne(AppearanceStage.CHECKED_IN))));

            Mockito.clearInvocations(jpaQuery);

            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().attendance(Arrays.asList(
                PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL,
                PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL,
                PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE,
                PoolMemberFilterRequestQuery.AttendanceEnum.OTHER)).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(JUROR_POOL.onCall.eq(true)
                    .or(PANEL.result.eq(PanelResult.JUROR))
                    .or(Expressions.booleanOperation(
                        Ops.AND,
                        APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        APPEARANCE.attendanceDate.eq(LocalDate.now())))
                    .or(JUROR_POOL.nextDate.eq(LocalDate.now())
                        .and(JUROR_POOL.onCall.ne(true))
                        .and(PANEL.result.ne(PanelResult.JUROR))
                        .and(APPEARANCE.appearanceStage.ne(AppearanceStage.CHECKED_IN))));
        }
    }

    private PoolMemberFilterRequestQuery.PoolMemberFilterRequestQueryBuilder getSearchBuilder() {
        return PoolMemberFilterRequestQuery.builder().pageNumber(1).pageLimit(1).poolNumber("41500000");
    }
}
