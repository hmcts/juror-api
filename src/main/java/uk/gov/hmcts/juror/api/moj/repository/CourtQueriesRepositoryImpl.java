package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.domain.QCourtCatchmentArea;

import java.util.List;

/**
 * Custom Repository implementation for extracting data related to courts.
 */
@Slf4j
@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class CourtQueriesRepositoryImpl implements CourtQueriesRepository {

    private static final QCourtCatchmentArea COURT_CATCHMENT = QCourtCatchmentArea.courtCatchmentArea;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<CourtLocationDataDto> getCourtDetailsFilteredByPostcode(String firstHalfOfPostcode) {
        log.trace("First half of postcode {} - Query method getCourtDetailsFilteredByPostcode() started",
            firstHalfOfPostcode);

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory
            .select(COURT_CATCHMENT.locCode,
                COURT_LOCATION.name,
                COURT_LOCATION.owner
            )
            .from(COURT_CATCHMENT)
            .innerJoin(COURT_LOCATION)
            .on(COURT_CATCHMENT.locCode.eq(COURT_LOCATION.locCode))
            .where(COURT_CATCHMENT.postcode.eq(firstHalfOfPostcode))
            .orderBy(COURT_CATCHMENT.locCode.asc())
            .fetch()
            .stream()
            .map(tuple -> new CourtLocationDataDto(
                tuple.get(COURT_CATCHMENT.locCode),
                WordUtils.capitalizeFully(tuple.get(COURT_LOCATION.name)),
                null, tuple.get(COURT_LOCATION.owner)))
            .toList();
    }
}
