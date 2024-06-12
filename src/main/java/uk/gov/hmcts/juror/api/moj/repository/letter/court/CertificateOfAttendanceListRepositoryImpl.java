package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.CertificateOfAttendanceLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.QCertificateOfAttendanceLetterList;

import java.util.List;

public class CertificateOfAttendanceListRepositoryImpl implements ICertificateOfAttendanceListRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QCertificateOfAttendanceLetterList CERTIFICATE_OF_ATTENDANCE_LETTER_LIST =
        QCertificateOfAttendanceLetterList.certificateOfAttendanceLetterList;

    @Override
    public List<CertificateOfAttendanceLetterList> findJurorsEligibleForCertificateOfAcceptanceLetter(
        CourtLetterSearchCriteria searchCriteria, String owner) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<CertificateOfAttendanceLetterList> jpaQuery =
            queryFactory.selectFrom(CERTIFICATE_OF_ATTENDANCE_LETTER_LIST)
                .where(CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.owner.equalsIgnoreCase(owner));

        filterEligibleLetterSearchCriteria(jpaQuery, searchCriteria);

        orderCertificateOfAttendanceQueryResults(jpaQuery, searchCriteria.includePrinted());

        return jpaQuery.fetch();
    }


    private void filterEligibleLetterSearchCriteria(JPAQuery<CertificateOfAttendanceLetterList> jpaQuery,
                                                    CourtLetterSearchCriteria courtLetterSearchCriteria) {
        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorNumber())) {
            jpaQuery.where(
                CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.jurorNumber.startsWith(courtLetterSearchCriteria.jurorNumber()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.jurorName())) {
            jpaQuery.where(CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.firstName.concat(" ")
                .concat(CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.lastName)
                .containsIgnoreCase(courtLetterSearchCriteria.jurorName()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.postcode())) {
            jpaQuery.where(
                CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.postcode.trim()
                    .equalsIgnoreCase(courtLetterSearchCriteria.postcode()
                        .trim()));
        }

        if (!StringUtils.isEmpty(courtLetterSearchCriteria.poolNumber())) {
            jpaQuery.where(
                CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.poolNumber.startsWith(courtLetterSearchCriteria.poolNumber()));
        }

        if (!courtLetterSearchCriteria.includePrinted()) {
            jpaQuery.where(CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.datePrinted.isNull());
        }
    }

    private void orderCertificateOfAttendanceQueryResults(JPAQuery<CertificateOfAttendanceLetterList> jpaQuery,
                                                 boolean isIncludePrinted) {

        if (isIncludePrinted) {
            jpaQuery.orderBy(new CaseBuilder()
                .when(CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.datePrinted.isNull())
                .then(0)
                .otherwise(1)
                .asc());
        }
        jpaQuery.orderBy(CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.datePrinted.desc());
        jpaQuery.orderBy(CERTIFICATE_OF_ATTENDANCE_LETTER_LIST.jurorNumber.asc());
    }
}

