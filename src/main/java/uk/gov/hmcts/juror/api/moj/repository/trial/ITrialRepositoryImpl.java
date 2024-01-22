package uk.gov.hmcts.juror.api.moj.repository.trial;

import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;

import java.util.List;

@SuppressWarnings("PMD.LawOfDemeter")
public class ITrialRepositoryImpl implements ITrialRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QTrial TRIAL = QTrial.trial;

    @Override

    public List<Trial> getListOfTrialsForCourtLocations(List<String> locCode, boolean isActiveFilter,
                                                        Pageable pageable) {

        Querydsl querydsl = new Querydsl(entityManager, new PathBuilderFactory().create(Trial.class));
        JPQLQuery<Trial> query = new JPAQuery<>(entityManager);

        query.select(TRIAL).from(TRIAL).where(TRIAL.courtLocation.locCode.in(locCode));

        if (isActiveFilter) {
            query.where(TRIAL.trialEndDate.isNull());
        }

        return querydsl.applyPagination(pageable, query).fetch();
    }

    @Override
    public Long getTotalTrialsForCourtLocations(List<String> locCode, boolean isActiveFilter) {
        JPQLQuery<Trial> query = new JPAQuery<>(entityManager);

        query.select(TRIAL).from(TRIAL).where(TRIAL.courtLocation.locCode.in(locCode));

        if (isActiveFilter) {
            query.where(TRIAL.trialEndDate.isNull());
        }
        return query.fetchCount();
    }

}
