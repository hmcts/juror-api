package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserCourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports"
})
class UserRepositoryImplTest {

    private UserRepositoryImpl userRepository;
    private JPAQueryFactory queryFactory;

    @BeforeEach
    void beforeEach() {
        queryFactory = mock(JPAQueryFactory.class);
        EntityManager entityManager = mock(EntityManager.class);
        userRepository = spy(new UserRepositoryImpl(entityManager));
        doReturn(queryFactory).when(userRepository).getQueryFactory();
    }

    @Nested
    @DisplayName("public PaginatedList<UserDetailsDto> messageSearch(UserSearchDto search)")
    class MessageSearch {
        private final LocalDateTime localDateTime = LocalDateTime.now().minusDays(1);
        private final CourtLocation courtLocation = mockCourtLocation("403", "403", "name1", CourtType.MAIN);

        private CourtLocation mockCourtLocation(String owner, String locCode, String name, CourtType type) {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getOwner()).thenReturn(owner);
            when(courtLocation.getLocCode()).thenReturn(locCode);
            when(courtLocation.getName()).thenReturn(name);
            when(courtLocation.getType()).thenReturn(type);
            return courtLocation;
        }

        @Test
        void positiveNoFilter() {
            JPAQuery<User> jpaQuery = mock(JPAQuery.class,
                withSettings().defaultAnswer(RETURNS_SELF).verboseLogging());

            when(queryFactory.select(QUser.user)).thenReturn(jpaQuery);

            QueryResults<User> queryResult = mock(QueryResults.class);
            doReturn(queryResult).when(jpaQuery).fetchResults();

            CourtLocation courtLocation1 = mockCourtLocation("400", "400", "name1", CourtType.MAIN);
            CourtLocation courtLocation2 = mockCourtLocation("400", "401", "name2", CourtType.SATELLITE);
            CourtLocation courtLocation3 = mockCourtLocation("403", "403", "name1", CourtType.MAIN);

            doReturn(List.of(courtLocation1, courtLocation2)).when(userRepository).getCourtsByOwner("400");
            doReturn(List.of(courtLocation3)).when(userRepository).getCourtsByOwner("403");

            User user1 = User.builder()
                .username("userName1")
                .email("email1")
                .name("name1")
                .active(true)
                .lastLoggedIn(LocalDateTime.now().minusDays(1))
                .userType(UserType.COURT)
                .roles(Set.of(Role.MANAGER))
                .courts(Set.of(
                        CourtLocation.builder().owner("400").build(),
                        CourtLocation.builder().owner("403").build()
                    )
                )
                .build();
            User user2 = User.builder()
                .username("userName2")
                .email("email2")
                .name("name2")
                .active(false)
                .lastLoggedIn(LocalDateTime.now().minusDays(2))
                .userType(UserType.COURT)
                .roles(Set.of())
                .courts(Set.of(
                        CourtLocation.builder().owner("400").build()
                    )
                )
                .build();
            User user3 = User.builder()
                .username("userName3")
                .email("email3")
                .name("name3")
                .active(true)
                .lastLoggedIn(LocalDateTime.now().minusDays(1))
                .userType(UserType.BUREAU)
                .roles(Set.of(Role.MANAGER, Role.TEAM_LEADER))
                .courts(Set.of(
                        CourtLocation.builder().owner("400").build()
                    )
                )
                .build();
            doReturn(List.of(user1, user2, user3)).when(queryResult).getResults();

            doReturn(3L).when(queryResult).getTotal();

            PaginatedList<UserDetailsDto> expectedResponse = new PaginatedList<>();
            expectedResponse.setCurrentPage(1L);
            expectedResponse.setTotalItems(3L);
            expectedResponse.setTotalPages(1L);
            expectedResponse.setData(List.of(
                UserDetailsDto.builder()
                    .username("userName1")
                    .email("email1")
                    .name("name1")
                    .isActive(true)
                    .lastSignIn(user1.getLastLoggedIn())
                    .userType(UserType.COURT)
                    .roles(Set.of(Role.MANAGER))
                    .courts(List.of(
                        new UserCourtDto(List.of(courtLocation1, courtLocation2)),
                        new UserCourtDto(List.of(courtLocation3))
                    ))
                    .build(),
                UserDetailsDto.builder()
                    .username("userName2")
                    .email("email2")
                    .name("name2")
                    .isActive(false)
                    .lastSignIn(user2.getLastLoggedIn())
                    .userType(UserType.COURT)
                    .roles(Set.of())
                    .courts(List.of(
                        new UserCourtDto(List.of(courtLocation1, courtLocation2))
                    ))
                    .build(),
                UserDetailsDto.builder()
                    .username("userName3")
                    .email("email3")
                    .name("name3")
                    .isActive(true)
                    .lastSignIn(user3.getLastLoggedIn())
                    .userType(UserType.BUREAU)
                    .roles(Set.of(Role.MANAGER, Role.TEAM_LEADER))
                    .courts(List.of(
                        new UserCourtDto(List.of(courtLocation1, courtLocation2))
                    ))
                    .build()
            ));


            assertThat(userRepository.messageSearch(
                UserSearchDto.builder()
                    .pageLimit(25)
                    .pageNumber(1)
                    .build())
            ).isEqualTo(expectedResponse);


            verify(userRepository, times(1)).getQueryFactory();
            verify(userRepository, times(3)).getCourtsByOwner("400");
            verify(userRepository, times(1)).getCourtsByOwner("403");

            verify(queryFactory, times(1)).select(QUser.user);
            verify(jpaQuery, times(1)).from(QUser.user);
            verify(jpaQuery, times(1)).fetchResults();
            verify(jpaQuery, times(1)).limit(25);
            verify(jpaQuery, times(1)).offset(0);
            verify(jpaQuery, times(1)).orderBy(
                new OrderSpecifier<?>[]{QUser.user.name.asc()});

            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }

        private JPAQuery<User> standardSetup() {
            JPAQuery<User> jpaQuery = mock(JPAQuery.class,
                withSettings().defaultAnswer(RETURNS_SELF).verboseLogging());

            when(queryFactory.select(any(Expression.class))).thenReturn(jpaQuery);

            QueryResults<User> queryResult = mock(QueryResults.class);
            doReturn(queryResult).when(jpaQuery).fetchResults();

            doReturn(List.of(courtLocation)).when(userRepository).getCourtsByOwner("403");

            User user1 = User.builder()
                .username("userName1")
                .email("email1")
                .name("name1")
                .active(true)
                .lastLoggedIn(localDateTime)
                .userType(UserType.COURT)
                .roles(Set.of(Role.MANAGER))
                .courts(Set.of(
                        CourtLocation.builder().owner("403").build()
                    )
                )
                .build();
            doReturn(List.of(user1)).when(queryResult).getResults();

            doReturn(3L).when(queryResult).getTotal();
            return jpaQuery;
        }

        private void standardValidation(JPAQuery<User> jpaQuery, PaginatedList<UserDetailsDto> results) {
            PaginatedList<UserDetailsDto> expectedResponse = new PaginatedList<>();
            expectedResponse.setCurrentPage(1L);
            expectedResponse.setTotalItems(3L);
            expectedResponse.setTotalPages(1L);
            expectedResponse.setData(List.of(
                UserDetailsDto.builder()
                    .username("userName1")
                    .email("email1")
                    .name("name1")
                    .isActive(true)
                    .lastSignIn(localDateTime)
                    .userType(UserType.COURT)
                    .roles(Set.of(Role.MANAGER))
                    .courts(List.of(
                        new UserCourtDto(List.of(courtLocation))
                    ))
                    .build()
            ));
            assertThat(results).isEqualTo(expectedResponse);

            verify(userRepository, times(1)).getQueryFactory();
            verify(userRepository, times(1)).getCourtsByOwner("403");

            verify(queryFactory, times(1)).select(QUser.user);
            verify(jpaQuery, times(1)).from(QUser.user);
            verify(jpaQuery, times(1)).fetchResults();
            verify(jpaQuery, times(1)).limit(25);
            verify(jpaQuery, times(1)).offset(0);
        }

        @Test
        void positiveUserNameFilter() {
            JPAQuery<User> jpaQuery = standardSetup();

            standardValidation(jpaQuery, userRepository.messageSearch(
                UserSearchDto.builder()
                    .userName("name1")
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()));

            verify(jpaQuery, times(1))
                .where(QUser.user.name.containsIgnoreCase("name1"));
            verify(jpaQuery, times(1)).orderBy(
                new OrderSpecifier<?>[]{QUser.user.name.asc()});

            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }

        @Test
        void positiveCourtFilter() {
            JPAQuery<User> jpaQuery = standardSetup();

            standardValidation(jpaQuery, userRepository.messageSearch(
                UserSearchDto.builder()
                    .court("400")
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()));

            verify(jpaQuery, times(1))
                .where(QUser.user.courts.any().locCode.eq("400"));
            verify(jpaQuery, times(1)).orderBy(
                new OrderSpecifier<?>[]{QUser.user.name.asc()});

            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }

        @Test
        void positiveUserTypeFilter() {
            JPAQuery<User> jpaQuery = standardSetup();

            standardValidation(jpaQuery, userRepository.messageSearch(
                UserSearchDto.builder()
                    .userType(UserType.BUREAU)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()));

            verify(jpaQuery, times(1))
                .where(QUser.user.userType.eq(UserType.BUREAU));
            verify(jpaQuery, times(1)).orderBy(
                new OrderSpecifier<?>[]{QUser.user.name.asc()});

            verifyNoMoreInteractions(queryFactory, jpaQuery);

        }

        @Test
        void positiveIsOnlyActiveFilter() {
            JPAQuery<User> jpaQuery = standardSetup();

            standardValidation(jpaQuery, userRepository.messageSearch(
                UserSearchDto.builder()
                    .onlyActive(true)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()));

            verify(jpaQuery, times(1))
                .where(QUser.user.active.isTrue());
            verify(jpaQuery, times(1)).orderBy(
                new OrderSpecifier<?>[]{QUser.user.name.asc()});

            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }


        @Test
        void positiveSortMethodChange() {
            JPAQuery<User> jpaQuery = standardSetup();

            standardValidation(jpaQuery, userRepository.messageSearch(
                UserSearchDto.builder()
                    .sortMethod(SortMethod.DESC)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()));


            verify(jpaQuery, times(1)).orderBy(
                new OrderSpecifier<?>[]{QUser.user.name.desc()});

            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }

        @Test
        void positiveSortFieldChange() {
            JPAQuery<User> jpaQuery = standardSetup();

            standardValidation(jpaQuery, userRepository.messageSearch(
                UserSearchDto.builder()
                    .sortField(UserSearchDto.SortField.EMAIL)
                    .sortMethod(SortMethod.DESC)
                    .pageLimit(25)
                    .pageNumber(1)
                    .build()));


            verify(jpaQuery, times(1)).orderBy(
                new OrderSpecifier<?>[]{QUser.user.email.desc()});
            verify(jpaQuery, times(1)).orderBy(
                new OrderSpecifier<?>[]{QUser.user.name.asc()});

            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }
    }

    @Nested
    @DisplayName("List<CourtLocation> getCourtsByOwner(String owner)")
    class GetCourtsByOwner {
        @Test
        void positiveTypical() {
            JPAQuery<CourtLocation> jpaQuery = mock(JPAQuery.class,
                withSettings().defaultAnswer(RETURNS_SELF).verboseLogging());

            when(queryFactory.select(QCourtLocation.courtLocation)).thenReturn(jpaQuery);

            List<CourtLocation> courtList = List.of(
                mock(CourtLocation.class),
                mock(CourtLocation.class),
                mock(CourtLocation.class)
            );
            when(jpaQuery.fetch()).thenReturn(courtList);


            assertThat(userRepository.getCourtsByOwner(TestConstants.VALID_COURT_LOCATION))
                .isEqualTo(courtList);


            verify(queryFactory, times(1)).select(QCourtLocation.courtLocation);
            verify(jpaQuery, times(1)).from(QCourtLocation.courtLocation);
            verify(jpaQuery, times(1)).where(QCourtLocation.courtLocation.owner.eq(TestConstants.VALID_COURT_LOCATION));
            verify(jpaQuery, times(1)).fetch();
            verifyNoMoreInteractions(queryFactory, jpaQuery);
        }
    }

}
