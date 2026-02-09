package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaJwtDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaUserDetailsDto;
import uk.gov.hmcts.juror.api.jurorer.domain.LaRoles;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.repository.LaUserRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.JwtService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.juror.api.moj.service.JwtServiceImpl.timeUnitToMilliseconds;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LaUserServiceImpl implements LaUserService {

    private final LaUserRepository userRepository;
    private final LocalAuthorityRepository localAuthorityRepository;
    private final JwtService jwtService;

    @Value("${jwt.secret.er-portal}")
    private String erPortalSecret;

    @Value("${jwt.expiry.er-portal}")
    private String erPortalExpiry;

    @Override
    @Transactional
    public LaJwtDto createJwt(String email) {
        LaUser user = findUserByUsername(email);

        if (user == null) {
            throw new MojException.NotFound("User not found", null);
        }

        if (Boolean.FALSE == user.isActive()) {
            throw new MojException.Forbidden("User is not active", null);
        }

        Map<String, Object> claims = new ConcurrentHashMap<>();

        LocalAuthority localAuthority = user.getLocalAuthority();

        claims.put("username", user.getUsername());
        claims.put("laCode", localAuthority.getLaCode());
        claims.put("laName", localAuthority.getLaName());
        claims.put("role", List.of(LaRoles.LA_USER.toString()));

        user.setLastLoggedIn(LocalDateTime.now());
        userRepository.save(user);

        String jwt = jwtService.generateJwtToken(user.getUsername(),
            "juror-api",
            null,
            timeUnitToMilliseconds(erPortalExpiry),
                jwtService.getSigningKey(erPortalSecret),
            claims);

        return new LaJwtDto(jwt);
    }


    @Override
    @Transactional(readOnly = true)
    public LaUser findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
            () -> new MojException.NotFound("User not found", null)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LaUserDetailsDto getLaUserDetails(String laCode) {

        // check that the laCode matches the regex  "^\d{3}$"
        if (!laCode.matches("^\\d{3}$")) {
            throw new MojException.BadRequest("Invalid laCode format", null);
        }

        // check if user making request is authorised to view users for the laCode
        if (!laCode.equals(SecurityUtil.getActiveLaCode())) {
            throw new MojException.Forbidden("User does not have access", null);
        }

        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(laCode).orElseThrow(
            () -> new MojException.NotFound("Local Authority not found", null)
        );

        if (Boolean.FALSE == localAuthority.getActive()) {
            throw new MojException.Forbidden("Local Authority not active", null);
        }

        List<LaUser> users = userRepository.findByLocalAuthority(localAuthority);

        // build the user details dto using the list of users
        List<LaUserDetailsDto.LaUserDetails> userDetailsList = users.stream().map(user ->
            LaUserDetailsDto.LaUserDetails.builder()
                .username(user.getUsername())
                .laCode(localAuthority.getLaCode())
                .isActive(user.isActive())
                .lastSignIn(user.getLastLoggedIn())
                .build()
        ).toList();

        return new LaUserDetailsDto(userDetailsList);

    }

    @Override
    public List<LaUser> findUsersByLaCode(String laCode) {
        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(laCode).orElseThrow(
            () -> new MojException.NotFound("Local Authority not found", null)
        );

        return userRepository.findByLocalAuthority(localAuthority);
    }

    @Override
    public void saveLaUser(LaUser laUser) {
        userRepository.save(laUser);
    }

}
