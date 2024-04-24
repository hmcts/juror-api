package uk.gov.hmcts.juror.api.bureau.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

/**
 * Implementation for Authentication service interface for bureau authentication operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BureauAuthenticationServiceImpl implements BureauAuthenticationService {

    @Override
    public boolean userIsTeamLeader(BureauJwtAuthentication auth) {
        if (auth.getPrincipal() == null || !(auth.getPrincipal() instanceof BureauJwtPayload)) {
            log.error(
                "User is not authenticated with a {} token, unable to check for team leader status",
                BureauJwtPayload.class
            );
            return false;
        }
        final BureauJwtPayload token = (BureauJwtPayload) auth.getPrincipal();
        return token.getStaff() != null && token.getStaff().getRank() != null && token.getStaff().getRank().equals(1);
    }

    @Override
    public String getUsername(BureauJwtAuthentication auth) {
        return auth.getPrincipal() == null || !(auth.getPrincipal() instanceof BureauJwtPayload)
            ? null : ((BureauJwtPayload) auth.getPrincipal()).getLogin();
    }

    @Override
    public String getOwner(BureauJwtAuthentication auth) {
        return auth.getPrincipal() == null || !(auth.getPrincipal() instanceof BureauJwtPayload)
            ? null : ((BureauJwtPayload) auth.getPrincipal()).getOwner();
    }
}
