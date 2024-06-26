package uk.gov.hmcts.juror.api.moj.service.audit;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.audit.dto.JurorAudit;
import uk.gov.hmcts.juror.api.moj.audit.dto.QJurorAudit;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JurorAuditServiceImpl implements JurorAuditService {

    @PersistenceContext
    EntityManager entityManager;

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Override
    public List<JurorAudit> getAllAuditsFor(List<String> jurorNumbers) {
        return getQueryFactory()
            .selectFrom(QJurorAudit.jurorAudit)
            .where(QJurorAudit.jurorAudit.jurorNumber.in(jurorNumbers))
            .orderBy(QJurorAudit.jurorAudit.revisionInfo.timestamp.asc())
            .fetch();
    }

    @Override
    public List<JurorAudit> getAllAuditsChangedBetweenAndHasCourt(LocalDate fromDate, LocalDate toDate,
                                                                  List<String> locCodes) {
        return getQueryFactory()
            .selectFrom(QJurorAudit.jurorAudit)
            .where(QJurorAudit.jurorAudit.revisionInfo.timestamp.between(
                DateUtils.toEpochMilli(fromDate.atTime(LocalTime.MIN)),
                DateUtils.toEpochMilli(toDate.atTime(LocalTime.MAX))))
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.jurorNumber.eq(QJurorAudit.jurorAudit.jurorNumber))
            .where(QJurorPool.jurorPool.pool.courtLocation.owner.eq(SecurityUtil.getActiveOwner()))
            .orderBy(QJurorAudit.jurorAudit.revisionInfo.timestamp.asc())
            .fetch();
    }


    @Override
    public JurorAudit getPreviousJurorAudit(JurorAudit juror) {

        List<JurorAudit> data = getQueryFactory()
            .selectFrom(QJurorAudit.jurorAudit)
            .where(QJurorAudit.jurorAudit.jurorNumber.eq(juror.getJurorNumber()))
            .where(QJurorAudit.jurorAudit.revisionInfo.timestamp.lt(juror.getRevisionInfo().getTimestamp()))
            .orderBy(QJurorAudit.jurorAudit.revisionInfo.timestamp.desc())
            .fetch();
        if (data.isEmpty()) {
            return null;
        }
        return data.get(0);
    }

    @Override
    public List<String> getAllPoolAuditsForDay(LocalDate date) {
        return getQueryFactory()
            .selectDistinct(QAppearance.appearance.attendanceAuditNumber)
            .from(QAppearance.appearance)
            .where(QAppearance.appearance.locCode.eq(SecurityUtil.getLocCode()))
            .where(QAppearance.appearance.attendanceDate.eq(date))
            .where(QAppearance.appearance.attendanceAuditNumber.isNotNull())
            .where(QAppearance.appearance.attendanceAuditNumber.startsWith("P"))
            .orderBy(QAppearance.appearance.attendanceAuditNumber.asc())
            .fetch();
    }
}
