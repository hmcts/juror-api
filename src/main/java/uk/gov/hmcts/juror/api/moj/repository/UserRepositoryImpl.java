package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserCourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;

import java.util.Comparator;
import java.util.List;

@NoArgsConstructor
public class UserRepositoryImpl implements IUserRepository {
    @PersistenceContext
    EntityManager entityManager;


    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public PaginatedList<UserDetailsDto> messageSearch(UserSearchDto search) {
        JPAQuery<Tuple> query = getQueryFactory()
            .select(QUser.user,
                QUser.user.roles.contains(Role.MANAGER).as("is_manager"),
                QUser.user.roles.contains(Role.SENIOR_JUROR_OFFICER).as("is_senior_juror_officer"))
            .from(QUser.user);

        if (search.getUserName() != null) {
            query.where(QUser.user.name.containsIgnoreCase(search.getUserName()));
        }

        if (search.getCourt() != null) {
            query.where(QUser.user.courts.any().locCode.eq(search.getCourt()));
        }
        if (search.getUserType() != null) {
            query.where(QUser.user.userType.eq(search.getUserType()));
        }
        if (search.isOnlyActive()) {
            query.where(QUser.user.active.isTrue());
        }

        return PaginationUtil.toPaginatedList(query, search,
            UserSearchDto.SortField.NAME,
            SortMethod.ASC,
            tuple -> {
                User user = tuple.get(QUser.user);
                return UserDetailsDto.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .name(user.getName())
                    .isActive(user.isActive())
                    .lastSignIn(user.getLastLoggedIn())
                    .userType(user.getUserType())
                    .roles(user.getRoles())
                    .courts(
                        user.getCourts().stream()
                            .map(courtLocation ->
                                new UserCourtDto(getCourtsByOwner(courtLocation.getOwner())))
                            .sorted(Comparator.comparing(o -> o.getPrimaryCourt().getLocCode()))
                            .toList()
                    )
                    .build();
            },
            null);
    }

    List<CourtLocation> getCourtsByOwner(String owner) {
        JPAQueryFactory queryFactory = getQueryFactory();
        return queryFactory
            .select(QCourtLocation.courtLocation)
            .from(QCourtLocation.courtLocation)
            .where(QCourtLocation.courtLocation.owner.eq(owner))
            .fetch();
    }

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
