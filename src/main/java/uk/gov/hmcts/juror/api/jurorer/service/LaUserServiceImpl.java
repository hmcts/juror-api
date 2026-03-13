package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.ExportLaEmailAddressResponseDto;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.juror.api.moj.service.JwtServiceImpl.timeUnitToMilliseconds;
import static uk.gov.hmcts.juror.api.validation.LaCodeValidator.isValidLaCode;

@Slf4j
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
    @Transactional(readOnly = true)
    public List<LocalAuthority> getLocalAuthorities(String email) {

        // read the user by email and get the local authority
        List<LaUser> user = userRepository.findByUsername(email);

        if (user.isEmpty()) {
            throw new MojException.NotFound("User not found", null);
        }

        return user.stream()
            .map(LaUser::getLocalAuthority)
            .filter(la -> Boolean.TRUE.equals(la.getActive()))
            .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public LaJwtDto createJwt(String email, String laCode) {
        final List<LaUser> userList = findUserByUsername(email);

        LaUser user = userList.stream()
            .filter(u -> u.getLocalAuthority().getLaCode().equals(laCode))
            .findFirst()
            .orElse(null);

        if (user == null) {
            throw new MojException.NotFound("User not found", null);
        }

        if (Boolean.FALSE.equals(user.isActive())) {
            throw new MojException.Forbidden("User is not active", null);
        }

        Map<String, Object> claims = new ConcurrentHashMap<>();

        final LocalAuthority localAuthority = user.getLocalAuthority();

        final List<String> userLaCodes = userList.stream()
            .map(u -> u.getLocalAuthority().getLaCode())
            .toList();

        claims.put("username", user.getUsername());
        claims.put("laCode", localAuthority.getLaCode());
        claims.put("laName", localAuthority.getLaName());
        claims.put("role", List.of(LaRoles.LA_USER.toString()));
        claims.put("localAuthorities", userLaCodes);

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
    public List<LaUser> findUserByUsername(String username) {
        return userRepository.findByUsername(username);

    }

    @Override
    @Transactional(readOnly = true)
    public LaUser findUserByUsernameAndLa(String username, String laCode) {
        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(laCode).orElseThrow(
            () -> new MojException.NotFound("Local Authority not found", null)
        );
        return userRepository.findByUsernameAndLocalAuthority(username, localAuthority).orElseThrow(
            () -> new MojException.NotFound("User not found", null)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LaUserDetailsDto getLaUserDetails(String laCode) {

        // check that the laCode matches the regex  "^\d{3}$"
        isValidLaCode(laCode);

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

    @Override
    public Optional<LaUser> findLastLoggedInUserByLaCode(String laCode) {
        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(laCode).orElseThrow(
            () -> new MojException.NotFound("Local Authority not found", null)
        );

        return userRepository.findFirstByLocalAuthorityAndLastLoggedInNotNullOrderByLastLoggedInDesc(localAuthority);
    }

    @Override
    @Transactional(readOnly = true)
    public ExportLaEmailAddressResponseDto getAllLaEmailAddresses(boolean activeOnly) {
        log.info("Exporting all LA email addresses (activeOnly: {})", activeOnly);

        // Get all Local Authorities and sort by name
        List<LocalAuthority> allLocalAuthorities = new ArrayList<>();
        localAuthorityRepository.findAll().forEach(allLocalAuthorities::add);

        allLocalAuthorities.sort(Comparator.comparing(LocalAuthority::getLaName));

        List<ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto> localAuthorityEmailsList =
            allLocalAuthorities.stream()
                .map(la -> {
                    // Get all users for this LA
                    List<LaUser> users = userRepository.findByLocalAuthority(la);

                    // Filter users based on activeOnly parameter
                    List<ExportLaEmailAddressResponseDto.EmailAddressDto> emailAddresses;

                    if (activeOnly) {
                        // Only include active users
                        emailAddresses = users.stream()
                            .filter(LaUser::isActive)  // Filter to active users only
                            .map(user -> ExportLaEmailAddressResponseDto.EmailAddressDto.builder()
                                .username(user.getUsername())
                                .active(user.isActive())
                                .build())
                            .sorted(Comparator.comparing(ExportLaEmailAddressResponseDto.EmailAddressDto::getUsername))
                            .collect(Collectors.toList());
                    } else {
                        // Include all users (active and inactive)
                        emailAddresses = users.stream()
                            .map(user -> ExportLaEmailAddressResponseDto.EmailAddressDto.builder()
                                .username(user.getUsername())
                                .active(user.isActive())
                                .build())
                            .sorted(Comparator.comparing(ExportLaEmailAddressResponseDto.EmailAddressDto::getUsername))
                            .collect(Collectors.toList());
                    }

                    return ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto.builder()
                        .laCode(la.getLaCode())
                        .laName(la.getLaName())
                        .isActive(la.getActive())
                        .emailAddresses(emailAddresses)
                        .build();
                })
                .collect(Collectors.toList());

        log.info("Exported {} Local Authorities with email addresses (activeOnly: {})",
                 localAuthorityEmailsList.size(), activeOnly);

        return ExportLaEmailAddressResponseDto.builder()
            .localAuthorities(localAuthorityEmailsList)
            .build();
    }
}
