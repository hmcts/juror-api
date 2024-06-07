package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.config.Settings;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
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

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Tuple> findLetters(ReissueLetterListRequestDto request, Consumer<JPAQuery<Tuple>> queryConsumer) {
        JPAQueryFactory queryFactory = getQueryFactory();

        JPAQuery<Tuple> query = queryFactory.selectDistinct(
                request.getLetterType().getReissueDataTypes().stream()
                    .map(ReissueLetterService.DataType::getExpression)
                    .toArray(Expression[]::new))
            .from(JUROR);  // must query Juror table for every letter type

        query.join(JUROR_POOL).on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber));

        if (request.getLetterType().equals(LetterType.SUMMONED_REMINDER)) {
            // for this letter type need to ensure any letters not printed (i.e. don't exist in bulk table) are
            // retrieved
            query.leftJoin(BULK_PRINT_DATA).on(JUROR.jurorNumber.eq(BULK_PRINT_DATA.jurorNo)
                .and(BULK_PRINT_DATA.formAttribute.formType.in(request.getLetterType().getFormCodes().stream()
                    .map(FormCode::getCode).toList())));
        } else {
            query.leftJoin(BULK_PRINT_DATA).on(JUROR.jurorNumber.eq(BULK_PRINT_DATA.jurorNo));
        }

        if (queryConsumer != null) {
            queryConsumer.accept(query);
        }

        final Set<Class<? extends EntityPathBase<?>>> entityPathBaseSet = request.getLetterType()
            .getReissueDataTypes().stream()
            .map(ReissueLetterService.DataType::getEntityPaths)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        if (entityPathBaseSet.contains(QJurorHistory.class)) {
            query.join(QJurorHistory.jurorHistory)
                .on(JUROR.jurorNumber.eq(QJurorHistory.jurorHistory.jurorNumber));

            query.where(QJurorHistory.jurorHistory.poolNumber.eq(JUROR_POOL.pool.poolNumber));

            if (request.getLetterType().equals(LetterType.DEFERRAL_REFUSED)) {
                query.where(QJurorHistory.jurorHistory.historyCode.eq(HistoryCodeMod.NON_DEFERRED_LETTER));
            }
        }

        if (!request.getLetterType().equals(LetterType.SUMMONED_REMINDER)) {
            query.where(BULK_PRINT_DATA.formAttribute.formType.in(request.getLetterType().getFormCodes().stream()
                .map(FormCode::getCode).toList()));
        }
        query.where(JUROR_POOL.isActive.eq(true));
        query.where(JUROR_POOL.owner.eq(SecurityUtil.BUREAU_OWNER));

        addFilters(query,request);

        return query
            .orderBy(BULK_PRINT_DATA.creationDate.desc())
            .orderBy(JUROR.jurorNumber.asc()).fetch();
    }

    private void addFilters(JPAQuery<Tuple> query, ReissueLetterListRequestDto request) {
        if (request.getJurorNumber() != null) {
            query.where(JUROR.jurorNumber.eq(request.getJurorNumber()));
        } else if (request.getPoolNumber() != null) {
            query.where(JUROR_POOL.pool.poolNumber.eq(request.getPoolNumber()));
        } else if (Boolean.TRUE.equals(request.getShowAllQueued())) {
            if  (request.getLetterType().equals(LetterType.SUMMONED_REMINDER)) {
                query.where(BULK_PRINT_DATA.formAttribute.formType.in(request.getLetterType().getFormCodes().stream()
                    .map(FormCode::getCode).toList()));
            } else {
                query.where(BULK_PRINT_DATA.extractedFlag.isNull().or(BULK_PRINT_DATA.extractedFlag.eq(false)));
            }
        } else if (request.getJurorName() != null) {
            query.where(QJuror.juror.firstName.concat(" ").concat(QJuror.juror.lastName).toLowerCase()
                .likeIgnoreCase("%" + request.getJurorName().toLowerCase(Settings.LOCALE) + "%"));
        } else if (request.getJurorPostcode() != null) {
            query.where(QJuror.juror.postcode.toLowerCase()
                .eq(request.getJurorPostcode().toLowerCase(Settings.LOCALE)));
        } else {
            throw new MojException.InternalServerError("Invalid criteria provided for letter search",
                null);
        }
    }

    @Override
    public Optional<BulkPrintData> findByJurorNumberFormCodeDatePrinted(String jurorNumber, String formCode,
                                                                        LocalDate datePrinted) {
        JPAQueryFactory queryFactory = getQueryFactory();

        JPAQuery<BulkPrintData> query = queryFactory.selectFrom(BULK_PRINT_DATA)
            .where(BULK_PRINT_DATA.jurorNo.eq(jurorNumber))
            .where(BULK_PRINT_DATA.formAttribute.formType.eq(formCode))
            .where(BULK_PRINT_DATA.creationDate.eq(datePrinted));

        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    public Optional<BulkPrintData> findByJurorNumberFormCodeAndPending(String jurorNumber, String formCode) {
        JPAQueryFactory queryFactory = getQueryFactory();

        JPAQuery<BulkPrintData> query = queryFactory.selectFrom(BULK_PRINT_DATA)
            .where(BULK_PRINT_DATA.jurorNo.eq(jurorNumber))
            .where(BULK_PRINT_DATA.formAttribute.formType.eq(formCode))
            .where(BULK_PRINT_DATA.extractedFlag.isNull().or(BULK_PRINT_DATA.extractedFlag.eq(false)));

        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    public Optional<BulkPrintData> findByJurorNumberFormCodeAndExtracted(String jurorNumber, String formCode,
                                                                         Boolean extracted) {
        JPAQueryFactory queryFactory = getQueryFactory();

        JPAQuery<BulkPrintData> query = queryFactory.selectFrom(BULK_PRINT_DATA)
            .where(BULK_PRINT_DATA.jurorNo.eq(jurorNumber))
            .where(BULK_PRINT_DATA.formAttribute.formType.eq(formCode))
            .where(BULK_PRINT_DATA.extractedFlag.eq(extracted));

        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    public List<BulkPrintData> findByJurorNoAndFormTypeAndCreationDateAndExtractedFlag(String jurorNumber,
                                                                                       String formType,
                                                                                       LocalDate creationDate,
                                                                                       boolean extractedFlag) {
        JPAQueryFactory queryFactory = getQueryFactory();

        JPAQuery<BulkPrintData> query = queryFactory.selectFrom(BULK_PRINT_DATA)
            .where(BULK_PRINT_DATA.jurorNo.eq(jurorNumber))
            .where(BULK_PRINT_DATA.formAttribute.formType.eq(formType))
            .where(BULK_PRINT_DATA.creationDate.eq(creationDate))
            .where(BULK_PRINT_DATA.extractedFlag.eq(extractedFlag));

        return query.fetch();
    }
}
