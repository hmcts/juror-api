package uk.gov.hmcts.juror.api.moj.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditorResolver implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return getCurrentAuditorGlobal();
    }

    public static Optional<String> getCurrentAuditorGlobal() {
        if (SecurityUtil.hasBureauJwtPayload()) {
            return Optional.of(SecurityUtil.getUsername());
        }
        if (SecurityUtil.hasPublicJwtPayload()) {
            return Optional.of("AUTO");
        }
        return Optional.empty();
    }
}
