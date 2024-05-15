package uk.gov.hmcts.juror.api.moj.service.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@SuppressWarnings("unchecked")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JurorAuditServiceImpl implements JurorAuditService {

    @PersistenceContext
    EntityManager entityManager;

    private final JurorPoolService jurorPoolService;

    @Override
    public List<Juror> getAllAuditsFor(List<String> jurorNumbers) {
        return (List<Juror>) AuditReaderFactory.get(entityManager)
            .createQuery()
            .forRevisionsOfEntity(Juror.class, true, true)
            .add(AuditEntity.property("jurorNumber").in(jurorNumbers))
            .addOrder(AuditEntity.property("lastUpdate").desc())
            .getResultList();
    }

    @Override
    public List<Juror> getAllAuditsChangedBetweenAndHasCourt(LocalDate fromDate, LocalDate toDate,
                                                             List<String> locCodes) {
        List<Juror> jurors = (List<Juror>) AuditReaderFactory.get(entityManager)
            .createQuery()
            .forRevisionsOfEntity(Juror.class, true, true)
            .add(AuditEntity.property("lastUpdate").between(
                fromDate.atTime(LocalTime.MIN),
                toDate.atTime(LocalTime.MAX)))
            .getResultList();

        List<String> allowedJurorNumbers = jurors
            .stream()
            .map(Juror::getJurorNumber)
            .distinct()
            .filter(juror -> jurorPoolService.hasPoolWithLocCode(juror, locCodes))
            .toList();

        return jurors
            .stream()
            .filter(juror -> allowedJurorNumbers.contains(juror.getJurorNumber()))
            .toList();
    }

    @Override
    public Juror getNextJurorAudit(Juror juror) {
        List<Juror> jurors = (List<Juror>) AuditReaderFactory.get(entityManager)
            .createQuery()
            .forRevisionsOfEntity(Juror.class, true, true)
            .add(AuditEntity.property("jurorNumber").eq(juror.getJurorNumber()))
            .add(AuditEntity.property("lastUpdate").gt(juror.getLastUpdate()))
            .addOrder(AuditEntity.property("lastUpdate").asc())
            .setMaxResults(1)
            .getResultList();
        if (jurors.isEmpty()) {
            return null;
        }
        return jurors.get(0);
    }

    @Override
    public Juror getPreviousJurorAudit(Juror juror) {
        List<Juror> jurors = (List<Juror>) AuditReaderFactory.get(entityManager)
            .createQuery()
            .forRevisionsOfEntity(Juror.class, true, true)
            .add(AuditEntity.property("jurorNumber").eq(juror.getJurorNumber()))
            .add(AuditEntity.property("lastUpdate").lt(juror.getLastUpdate()))
            .addOrder(AuditEntity.property("lastUpdate").desc())
            .setMaxResults(1)
            .getResultList();
        if (jurors.isEmpty()) {
            return null;
        }
        return jurors.get(0);
    }
}
