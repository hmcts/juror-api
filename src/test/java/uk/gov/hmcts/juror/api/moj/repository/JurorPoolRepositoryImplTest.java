package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
import uk.gov.hmcts.juror.api.moj.domain.QJurorTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
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
    @Mock
    private JPAQueryFactory queryFactory;
    @Mock
    private JPAQuery jpaQuery;

    private JurorPoolRepositoryImpl jurorPoolRepository;

    @BeforeEach
    void beforeEach() {
        jurorPoolRepository = Mockito.spy(new JurorPoolRepositoryImpl());
        Mockito.doReturn(queryFactory).when(jurorPoolRepository).getQueryFactory();

        Mockito.when(queryFactory.from(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.join(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.on(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.leftJoin(Mockito.any(EntityPath.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.where(Mockito.any(Predicate.class))).thenReturn(jpaQuery);
        Mockito.when(jpaQuery.select(Mockito.any(Expression[].class))).thenReturn(jpaQuery);
    }

    @Nested
    class FetchFilteredPoolMembers {
        @Test
        void coreFunctionality() {
            final String ownerId = "415";
            final LocalDate today = LocalDate.now();
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().build(), ownerId);
            Mockito.verify(queryFactory, Mockito.times(1)).from(QJurorPool.jurorPool);
            Mockito.verify(jpaQuery, Mockito.times(1)).join(QJuror.juror);
            Mockito.verify(jpaQuery, Mockito.times(1))
                .on(QJurorPool.jurorPool.juror.jurorNumber.eq(QJuror.juror.jurorNumber));
            Mockito.verify(jpaQuery, Mockito.times(1)).leftJoin(QAppearance.appearance);
            Mockito.verify(jpaQuery, Mockito.times(1))
                .on(QJurorPool.jurorPool.juror.jurorNumber.eq(QAppearance.appearance.jurorNumber)
                    .and(QAppearance.appearance.attendanceDate.eq(today)));
            Mockito.verify(jpaQuery, Mockito.times(1)).leftJoin(QJurorStatus.jurorStatus);
            Mockito.verify(jpaQuery, Mockito.times(1))
                .on(QJurorPool.jurorPool.status.status.eq(QJurorStatus.jurorStatus.status));
            Mockito.verify(jpaQuery, Mockito.times(1)).leftJoin(QJurorTrial.jurorTrial);
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJurorPool.jurorPool.isActive.isTrue());
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJurorPool.jurorPool.pool.poolNumber.eq("41500000"));

            Mockito.verify(jpaQuery, Mockito.times(1))
                .select(Mockito.eq(QJurorPool.jurorPool.juror.jurorNumber),
                    Mockito.eq(QJurorPool.jurorPool.juror.firstName),
                    Mockito.eq(QJurorPool.jurorPool.juror.lastName),
                    Mockito.eq(QJurorPool.jurorPool.juror.postcode),
                    Mockito.eq(new CaseBuilder()
                        .when(Expressions.asBoolean(QJurorPool.jurorPool.onCall.eq(true)))
                        .then(PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL.getKeyString())
                        .when(QJurorTrial.jurorTrial.result.eq("J"))
                        .then(PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL.getKeyString())
                        .when(Expressions.booleanOperation(
                            Ops.AND,
                            QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                            QAppearance.appearance.attendanceDate.eq(LocalDate.now())
                        ))
                        .then(PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE.getKeyString())
                        .when(QJurorPool.jurorPool.nextDate.eq(LocalDate.now()))
                        .then(PoolMemberFilterRequestQuery.AttendanceEnum.OTHER.getKeyString())
                        .otherwise("")
                        .as(ATTENDANCE)),
                    Mockito.eq(Expressions.booleanOperation(
                        Ops.AND,
                        QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        QAppearance.appearance.attendanceDate.eq(LocalDate.now())
                    ).as(CHECKED_IN_TODAY)),
                    Mockito.eq(QAppearance.appearance.timeIn),
                    Mockito.eq(QJurorPool.jurorPool.nextDate),
                    Mockito.eq(QJurorStatus.jurorStatus.statusDesc));

            Mockito.verify(jurorPoolRepository, Mockito.times(1)).getAttendanceCase();
            Mockito.verify(jurorPoolRepository, Mockito.times(2)).getCheckedInBoolean();
        }


        @Test
        void checkJurorOwner() {
            String ownerId = "415";
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().build(), ownerId);
            Mockito.verify(jpaQuery, Mockito.times(1)).where(QJurorPool.jurorPool.owner.eq(ownerId));

            ownerId = SecurityUtil.BUREAU_OWNER;
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().build(), ownerId);
            Mockito.verify(jpaQuery, Mockito.times(1)).where(QJurorPool.jurorPool.owner.eq(ownerId));
        }

        @Test
        void noFilters() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().build(), "");
            Mockito.verify(jpaQuery, Mockito.times(3))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(QJuror.juror.jurorNumber.like("123456%"));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(QJuror.juror.firstName.like("TEST%"));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(QJuror.juror.lastName.like("PERSON%"));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.booleanOperation(
                    Ops.AND,
                    QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                    QAppearance.appearance.attendanceDate.eq(LocalDate.now())
                ));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(QJurorPool.jurorPool.nextDate.isNotNull());
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(QJurorPool.jurorPool.nextDate.isNull());
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(QJurorStatus.jurorStatus.statusDesc.in(Arrays.asList("Status1", "Status2")));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(QJurorPool.jurorPool.onCall.eq(true)
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.FALSE
                    .or(QJurorTrial.jurorTrial.result.eq("J"))
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.FALSE
                    .or(Expressions.FALSE)
                    .or(Expressions.booleanOperation(
                        Ops.AND,
                        QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        QAppearance.appearance.attendanceDate.eq(LocalDate.now())))
                    .or(Expressions.FALSE));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.FALSE
                    .or(Expressions.FALSE)
                    .or(Expressions.FALSE)
                    .or(QJurorPool.jurorPool.nextDate.eq(LocalDate.now())
                        .and(QJurorPool.jurorPool.onCall.ne(true))
                        .and(QJurorTrial.jurorTrial.result.ne("J"))
                        .and(QAppearance.appearance.appearanceStage.ne(AppearanceStage.CHECKED_IN))));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(QJurorPool.jurorPool.onCall.eq(true)
                    .or(QJurorTrial.jurorTrial.result.eq("J"))
                    .or(Expressions.booleanOperation(
                        Ops.AND,
                        QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        QAppearance.appearance.attendanceDate.eq(LocalDate.now())))
                    .or(QJurorPool.jurorPool.nextDate.eq(LocalDate.now())));

        }

        @Test
        void filterByJurorNumber() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().jurorNumber("123456").build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJuror.juror.jurorNumber.like("123456%"));
        }

        @Test
        void filterByFirstName() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().firstName("Test").build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJuror.juror.firstName.like("TEST%"));
        }

        @Test
        void filterByLastName() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().lastName("Person").build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJuror.juror.lastName.like("PERSON%"));
        }

        @Test
        void dontFilterByFalseCheckedIn() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().checkedIn(false).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(3))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(0))
                .where(Expressions.booleanOperation(
                    Ops.AND,
                    QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                    QAppearance.appearance.attendanceDate.eq(LocalDate.now())
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
                    QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                    QAppearance.appearance.attendanceDate.eq(LocalDate.now())
                ));
        }

        @Test
        void filterByNextDue() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().nextDue(true).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJurorPool.jurorPool.nextDate.isNotNull());
        }

        @Test
        void filterByNextDueFalse() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().nextDue(false).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJurorPool.jurorPool.nextDate.isNull());
        }

        @Test
        void filterByStatus() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().statuses(
                Arrays.asList("Status1", "Status2")).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJurorStatus.jurorStatus.statusDesc.in(Arrays.asList("Status1", "Status2")));
        }

        @Test
        void filterByAttendance() {
            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().attendance(Arrays.asList(
                PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL)).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJurorPool.jurorPool.onCall.eq(true)
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
                    .or(QJurorTrial.jurorTrial.result.eq("J"))
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
                        QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        QAppearance.appearance.attendanceDate.eq(LocalDate.now())))
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
                    .or(QJurorPool.jurorPool.nextDate.eq(LocalDate.now())
                        .and(QJurorPool.jurorPool.onCall.ne(true))
                        .and(QJurorTrial.jurorTrial.result.ne("J"))
                        .and(QAppearance.appearance.appearanceStage.ne(AppearanceStage.CHECKED_IN))));

            Mockito.clearInvocations(jpaQuery);

            jurorPoolRepository.fetchFilteredPoolMembers(getSearchBuilder().attendance(Arrays.asList(
                PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL,
                PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL,
                PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE,
                PoolMemberFilterRequestQuery.AttendanceEnum.OTHER)).build(), "");
            Mockito.verify(jpaQuery, Mockito.times(4))
                .where(Mockito.any(Predicate.class));
            Mockito.verify(jpaQuery, Mockito.times(1))
                .where(QJurorPool.jurorPool.onCall.eq(true)
                    .or(QJurorTrial.jurorTrial.result.eq("J"))
                    .or(Expressions.booleanOperation(
                        Ops.AND,
                        QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        QAppearance.appearance.attendanceDate.eq(LocalDate.now())))
                    .or(QJurorPool.jurorPool.nextDate.eq(LocalDate.now())
                        .and(QJurorPool.jurorPool.onCall.ne(true))
                        .and(QJurorTrial.jurorTrial.result.ne("J"))
                        .and(QAppearance.appearance.appearanceStage.ne(AppearanceStage.CHECKED_IN))));
        }
    }

    private PoolMemberFilterRequestQuery.PoolMemberFilterRequestQueryBuilder getSearchBuilder() {
        return PoolMemberFilterRequestQuery.builder().pageNumber(1).pageLimit(1).poolNumber("41500000");
    }
}
