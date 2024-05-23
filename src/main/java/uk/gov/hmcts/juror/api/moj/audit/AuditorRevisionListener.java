package uk.gov.hmcts.juror.api.moj.audit;

import org.hibernate.envers.RevisionListener;
import uk.gov.hmcts.juror.api.moj.domain.RevisionInfo;

import java.util.Optional;


public class AuditorRevisionListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof RevisionInfo revisionInfo) {
            Optional<String> auditor = AuditorResolver.getCurrentAuditorGlobal();
            auditor.ifPresent(revisionInfo::setChangedBy);
        }
    }
}
