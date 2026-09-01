package uk.gov.hmcts.juror.api.jurorer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.ExportLaEmailAddressResponseDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaJwtDto;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.repository.LaUserRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.JwtService;
import uk.gov.hmcts.juror.api.moj.service.JwtServiceImpl;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class LaUserServiceImplTest {

    @Mock
    private LaUserRepository userRepository;

    @Mock
    private LocalAuthorityRepository localAuthorityRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LaUserServiceImpl laUserService;

    @Test
    void testGetLocalAuthoritiesHappy() {

        String email = "testemail@localauth1.gov.uk";
        LocalAuthority localAuthority = LocalAuthority.builder()
            .laCode("001")
            .laName("Local Authority 1")
            .active(true)
            .build();

        List<LaUser> laUsers = List.of(
            LaUser.builder()
                .username(email)
                .localAuthority(localAuthority)
                .build());

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(laUsers);

        List<LocalAuthority> localAuthorities = laUserService.getLocalAuthorities(email);

        assertNotNull(localAuthorities);
        assertEquals(1, localAuthorities.size());
        assertEquals("001", localAuthorities.get(0).getLaCode());

    }

    @Test
    void testGetLocalAuthoritiesUserNotFound() {

        String email = "testemail@localauth1.gov.uk";

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(List.of());

        MojException.NotFound exception =
            assertThrows(
                MojException.NotFound.class,
                () -> laUserService.getLocalAuthorities(email),
                "Should throw an error when user is not found"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("User not found");

        verify(userRepository).findByUsernameIgnoreCase(email);

    }

    @Test
    void createJwtHappy() {

        String email = "testemail@localauth1.gov.uk";

        LocalAuthority localAuthority = LocalAuthority.builder()
            .laCode("001")
            .laName("Local Authority 1")
            .active(true)
            .build();

        LaUser laUser = LaUser.builder()
                .username(email)
                .active(true)
                .localAuthority(localAuthority)
                .build();

        when(userRepository.findByUsernameIgnoreCase(email))
            .thenReturn(List.of(laUser));
        when(jwtService.getSigningKey(anyString())).thenReturn(null);

        try (MockedStatic<JwtServiceImpl> mocked = Mockito.mockStatic(JwtServiceImpl.class)) {
            mocked.when(() -> JwtServiceImpl.timeUnitToMilliseconds(anyString()))
                .thenReturn(3_600_000L);
            when(jwtService.generateJwtToken(anyString(),anyString(), any(), anyLong(), any(),
                                             anyMap())).thenReturn("jwt-token");


            LocalDateTime before = LocalDateTime.now();
            LaJwtDto dto = laUserService.createJwt(email, "001");
            LocalDateTime after = LocalDateTime.now();

            assertNotNull(dto);
            assertEquals("jwt-token", dto.getJwt());
            assertThat(laUser.getLastLoggedIn()).isBetween(before, after);
        }

        verify(userRepository).findByUsernameIgnoreCase(email);
        verify(jwtService).getSigningKey(null);

        Map<String, Object> expectedClaims = Map.of(
            "laCode", "001",
            "role", List.of("LA_USER"),
            "laName", "Local Authority 1",
            "username", "testemail@localauth1.gov.uk",
            "localAuthorities", List.of("001"));

        verify(jwtService).generateJwtToken("testemail@localauth1.gov.uk", "juror-api", null, 0L, null, expectedClaims);
        verify(userRepository).save(laUser);
    }


    @Test
    void createJwtUserNotFound() {

        String email = "testemail@localauth1.gov.uk";

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(List.of());

        MojException.NotFound exception =
            assertThrows(
                MojException.NotFound.class,
                () -> laUserService.createJwt(email, "001"),
                "Should throw an error when user is not found"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("User not found");

        verify(userRepository).findByUsernameIgnoreCase(email);
        verifyNoInteractions(jwtService);
    }

    @Test
    void findUserByUsername() {

        String email = "testemail@localauth1.gov.uk";

        LocalAuthority localAuthority = LocalAuthority.builder()
            .laCode("001")
            .laName("Local Authority 1")
            .active(true)
            .build();

        List<LaUser> laUsers = List.of(
            LaUser.builder()
                .username(email)
                .localAuthority(localAuthority)
                .build());

        when(userRepository.findByUsernameIgnoreCase(email)).thenReturn(laUsers);

        List<LaUser> laUserList = laUserService.findUserByUsername(email);

        assertThat(laUserList).isNotEmpty();
        assertThat(laUserList).hasSize(1);
        assertThat(laUserList.get(0).getUsername()).isEqualTo(email);
        assertThat(laUserList.get(0).getLocalAuthority().getLaCode()).isEqualTo("001");

        verify(userRepository).findByUsernameIgnoreCase(email);
    }

    @Test
    void findUserByUsernameAndLaHappy() {

        String email = "testemail@localauth1.gov.uk";

        LocalAuthority localAuthority = LocalAuthority.builder()
            .laCode("001")
            .laName("Local Authority 1")
            .active(true)
            .build();

        LaUser laUser = LaUser.builder()
                .username(email)
                .localAuthority(localAuthority)
                .build();

        when(userRepository.findByUsernameIgnoreCaseAndLocalAuthority(email, localAuthority))
            .thenReturn(Optional.of(laUser));
        when(localAuthorityRepository.findByLaCode("001")).thenReturn(Optional.of(localAuthority));

        laUserService.findUserByUsernameAndLa(email, "001");

        verify(userRepository).findByUsernameIgnoreCaseAndLocalAuthority(email, localAuthority);
        verify(localAuthorityRepository).findByLaCode("001");

    }

    @Test
    void testGetLaUserDetailsInvalidLaCode() {

        String laCode = "00efd1";

        MojException.BadRequest exception =
            assertThrows(
                MojException.BadRequest.class,
                () -> laUserService.getLaUserDetails(laCode),
                "Should throw an error when invalid LA code is provided"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("Invalid LA code format: " + laCode);

    }

    @Test
    void testGetLaUserDetailsDifferentLaCodeToUser() {

        String laCode = "001";

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getActiveLaCode).thenReturn("002");
            MojException.Forbidden exception =
                assertThrows(
                    MojException.Forbidden.class,
                    () -> laUserService.getLaUserDetails(laCode),
                    "Should throw an error when user has a different LA code to the one provided"
                );

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("User does not have access");
        }

    }


    @Test
    void findLastLoggedInUserByLaCodeLaCodeNotFound() {

        String laCode = "001";

        when(localAuthorityRepository.findByLaCode(laCode)).thenReturn(Optional.empty());

        MojException.NotFound exception =
            assertThrows(
                MojException.NotFound.class,
                () -> laUserService.findLastLoggedInUserByLaCode(laCode),
                "Should throw an error when user is not found"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("Local Authority not found");

    }

    @Test
    void getAllLaEmailAddressesActiveOnlySortsLocalAuthoritiesAndActiveEmailAddresses() {
        LocalAuthority westminster = localAuthority("003", "Westminster", true);
        LocalAuthority birmingham = localAuthority("001", "Birmingham", true);
        LocalAuthority aberdeen = localAuthority("002", "Aberdeen", false);

        when(localAuthorityRepository.findAll()).thenReturn(List.of(westminster, birmingham, aberdeen));
        when(userRepository.findByLocalAuthority(aberdeen)).thenReturn(List.of(
            laUser("zuser@aberdeen.gov.uk", true, aberdeen),
            laUser("auser@aberdeen.gov.uk", true, aberdeen),
            laUser("inactive@aberdeen.gov.uk", false, aberdeen)
        ));
        when(userRepository.findByLocalAuthority(birmingham)).thenReturn(List.of(
            laUser("zuser@birmingham.gov.uk", true, birmingham),
            laUser("inactive@birmingham.gov.uk", false, birmingham),
            laUser("auser@birmingham.gov.uk", true, birmingham)
        ));
        when(userRepository.findByLocalAuthority(westminster)).thenReturn(List.of(
            laUser("inactive@westminster.gov.uk", false, westminster)
        ));

        ExportLaEmailAddressResponseDto response = laUserService.getAllLaEmailAddresses(true);

        assertThat(response.getLocalAuthorities())
            .extracting(ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto::getLaName)
            .containsExactly("Aberdeen", "Birmingham", "Westminster");

        ExportLaEmailAddressResponseDto.LocalAuthorityEmailsDto aberdeenEmails =
            response.getLocalAuthorities().get(0);
        assertThat(aberdeenEmails.getLaCode()).isEqualTo("002");
        assertThat(aberdeenEmails.getIsActive()).isFalse();
        assertThat(aberdeenEmails.getEmailAddresses())
            .extracting(ExportLaEmailAddressResponseDto.EmailAddressDto::getUsername)
            .containsExactly("auser@aberdeen.gov.uk", "zuser@aberdeen.gov.uk");
        assertThat(aberdeenEmails.getEmailAddresses())
            .extracting(ExportLaEmailAddressResponseDto.EmailAddressDto::getActive)
            .containsExactly(true, true);

        assertThat(response.getLocalAuthorities().get(1).getEmailAddresses())
            .extracting(ExportLaEmailAddressResponseDto.EmailAddressDto::getUsername)
            .containsExactly("auser@birmingham.gov.uk", "zuser@birmingham.gov.uk");
        assertThat(response.getLocalAuthorities().get(2).getEmailAddresses()).isEmpty();
    }

    @Test
    void getAllLaEmailAddressesIncludesInactiveUsersWhenActiveOnlyIsFalse() {
        LocalAuthority localAuthority = localAuthority("001", "Birmingham", true);

        when(localAuthorityRepository.findAll()).thenReturn(List.of(localAuthority));
        when(userRepository.findByLocalAuthority(localAuthority)).thenReturn(List.of(
            laUser("zuser@birmingham.gov.uk", false, localAuthority),
            laUser("auser@birmingham.gov.uk", true, localAuthority)
        ));

        ExportLaEmailAddressResponseDto response = laUserService.getAllLaEmailAddresses(false);

        assertThat(response.getLocalAuthorities()).hasSize(1);
        assertThat(response.getLocalAuthorities().get(0).getEmailAddresses())
            .extracting(
                ExportLaEmailAddressResponseDto.EmailAddressDto::getUsername,
                ExportLaEmailAddressResponseDto.EmailAddressDto::getActive
            )
            .containsExactly(
                tuple("auser@birmingham.gov.uk", true),
                tuple("zuser@birmingham.gov.uk", false)
            );
    }

    private LocalAuthority localAuthority(String laCode, String laName, boolean active) {
        return LocalAuthority.builder()
            .laCode(laCode)
            .laName(laName)
            .active(active)
            .build();
    }

    private LaUser laUser(String username, boolean active, LocalAuthority localAuthority) {
        return LaUser.builder()
            .username(username)
            .active(active)
            .localAuthority(localAuthority)
            .build();
    }
}
