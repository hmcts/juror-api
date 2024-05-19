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
        if (SecurityUtil.hasBureauJwtPayload()) {
            return Optional.of(SecurityUtil.getUsername());
        }
        return Optional.empty();
    }

}
