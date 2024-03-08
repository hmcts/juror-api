package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPendingJuror;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class IPendingJurorRepositoryImpl implements IPendingJurorRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QPendingJuror PENDING_JUROR = QPendingJuror.pendingJuror;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;

    @SuppressWarnings("PMD.LawOfDemeter")
    @Override
    public List<PendingJurorsResponseDto.PendingJurorsResponseData> findPendingJurorsForCourt(
        String locCode,
        PendingJurorStatus status) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Tuple> query = queryFactory.select(
                PENDING_JUROR.jurorNumber.as("juror_number"),
                PENDING_JUROR.firstName.as("first_name"),
                PENDING_JUROR.lastName.as("last_name"),
                PENDING_JUROR.status.as("pending_status"),
                PENDING_JUROR.notes.as("notes"),
                PENDING_JUROR.postcode.as("postcode")
            )
            .from(PENDING_JUROR)
            .join(POOL_REQUEST)
            .on(PENDING_JUROR.poolNumber.eq(POOL_REQUEST.poolNumber))
            .where(POOL_REQUEST.courtLocation.locCode.eq(locCode));

        if (status != null) {
            query = query.where(PENDING_JUROR.status.eq(status));
        }

        List<Tuple> tuples = query.orderBy(PENDING_JUROR.jurorNumber.asc()).fetch();

        List<PendingJurorsResponseDto.PendingJurorsResponseData> pendingJurorList = new ArrayList<>();

        for (Tuple tuple : tuples) {
            PendingJurorsResponseDto.PendingJurorsResponseData pendingJuror =
                PendingJurorsResponseDto.PendingJurorsResponseData.builder()
                    .jurorNumber(tuple.get(0, String.class))
                    .firstName(tuple.get(1, String.class))
                    .lastName(tuple.get(2, String.class))
                    .pendingJurorStatus(tuple.get(3, PendingJurorStatus.class))
                    .notes(tuple.get(4, String.class))
                    .postcode(tuple.get(5, String.class))
                    .build();
            pendingJurorList.add(pendingJuror);
        }

        return pendingJurorList;
    }
}
