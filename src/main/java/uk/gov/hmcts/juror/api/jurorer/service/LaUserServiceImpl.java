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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.juror.api.moj.service.JwtServiceImpl.getSigningKey;
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
        claims.put("role", LaRoles.LA_USER.toString());

        user.setLastLoggedIn(LocalDateTime.now());
        userRepository.save(user);

        String jwt = jwtService.generateJwtToken(user.getUsername(),
            "juror-api",
            null,
            timeUnitToMilliseconds(erPortalExpiry),
            getSigningKey(erPortalSecret),
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
        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(laCode).orElseThrow(
            () -> new MojException.NotFound("Local Authority not found", null)
        );

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


}
