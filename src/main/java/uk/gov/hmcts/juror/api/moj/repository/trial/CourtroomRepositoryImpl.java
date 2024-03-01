package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.trial.QCourtroom;

import java.util.List;

public class CourtroomRepositoryImpl implements ICourtroomRepository {
    @PersistenceContext
    EntityManager entityManager;

    private static final QCourtroom Q_COURTROOM = QCourtroom.courtroom;

    private static final QCourtLocation Q_COURT_LOCATION = QCourtLocation.courtLocation;

    @Override
    @SuppressWarnings("PMD.LawOfDemeter") //PMD is complaining about the static property being accessed, so ignoring
    // as unable to sort this issue out (tried instantiating the objects and still didn't work)
    public List<Tuple> getCourtroomsForLocation(List<String> courts) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(
                Q_COURTROOM.id,
                Q_COURTROOM.courtLocation.owner,
                Q_COURTROOM.courtLocation.locCode,
                Q_COURTROOM.description,
                Q_COURTROOM.roomNumber,
                Q_COURT_LOCATION.name
            ).from(Q_COURTROOM)
            .where(Q_COURTROOM.courtLocation.locCode.in(courts))
            .fetch();
    }
}
