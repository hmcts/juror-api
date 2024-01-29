package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IReissueLetterRepositoryImpl implements IReissueLetterRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QBulkPrintData BULK_PRINT_DATA = QBulkPrintData.bulkPrintData;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;

    @Override
    public List<Tuple> findLetters(ReissueLetterListRequestDto request, Consumer<JPAQuery<Tuple>> queryConsumer) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Tuple> query = queryFactory.selectDistinct(
            request.getLetterType().getReissueDataTypes().stream()
                .map(ReissueLetterService.DataType::getExpression)
                .toArray(Expression[]::new)
        ).from(JUROR);  // must query Juror table for every letter type

        Set<Class<? extends EntityPathBase<?>>> entityPathBaseSet = request.getLetterType()
            .getReissueDataTypes().stream()
            .map(ReissueLetterService.DataType::getEntityPaths)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        if (entityPathBaseSet.contains(QJurorPool.class) || request.getPoolNumber() != null) {
            query.join(JUROR_POOL).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));
        }

        // must have this join for every letter type except for Summons letters - tbc later
        query.join(BULK_PRINT_DATA).on(JUROR.jurorNumber.eq(BULK_PRINT_DATA.jurorNo));

        if (queryConsumer != null) {
            queryConsumer.accept(query);
        }

        query.where(BULK_PRINT_DATA.formAttribute.formType.in(request.getLetterType().getFormCodes().stream()
            .map(FormCode::getCode).toList()));
        query.where(JUROR_POOL.owner.eq(SecurityUtil.BUREAU_OWNER));

        //TODO use include new search criteria / validate, e.g. Name, Postcode etc
        if (request.getJurorNumber() != null) {
            query.where(JUROR.jurorNumber.eq(request.getJurorNumber()));
        } else if (request.getPoolNumber() != null) {
            query.where(JUROR_POOL.pool.poolNumber.eq(request.getPoolNumber()));
        } else {
            query.where(BULK_PRINT_DATA.creationDate.eq(LocalDate.now()));
        }
        return query
            .orderBy(BULK_PRINT_DATA.creationDate.desc())
            .orderBy(JUROR.jurorNumber.asc()).fetch();
    }

    @Override
    public Optional<BulkPrintData> findByJurorNumberFormCodeDatePrinted(String jurorNumber, String formCode,
                                                                        LocalDate datePrinted) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<BulkPrintData> query = queryFactory.selectFrom(BULK_PRINT_DATA)
            .where(BULK_PRINT_DATA.jurorNo.eq(jurorNumber))
            .where(BULK_PRINT_DATA.formAttribute.formType.eq(formCode))
            .where(BULK_PRINT_DATA.creationDate.eq(datePrinted));

        return Optional.ofNullable(query.fetchOne());
    }

}
