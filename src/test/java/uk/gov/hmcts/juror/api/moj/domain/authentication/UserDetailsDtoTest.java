package uk.gov.hmcts.juror.api.moj.domain.authentication;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class UserDetailsDtoTest extends AbstractValidatorTest<UserDetailsDto> {

    private static MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @AfterAll
    static void afterAll() {
        securityUtilMockedStatic.close();
    }

    @Override
    protected UserDetailsDto createValidObject() {
        return UserDetailsDto.builder()
            .username("username")
            .email(TestConstants.VALID_EMAIL)
            .name("name")
            .isActive(true)
            .build();
    }

    @Nested
    class UsernameTest extends AbstractValidationFieldTestString {
        protected UsernameTest() {
            super("username", UserDetailsDto::setUsername);
            addNotBlankTest(null);
        }
    }

    @Nested
    class EmailTest extends AbstractValidationFieldTestString {
        protected EmailTest() {
            super("email", UserDetailsDto::setEmail);
            addNotBlankTest(null);
        }
    }

    @Nested
    class NameTest extends AbstractValidationFieldTestString {
        protected NameTest() {
            super("name", UserDetailsDto::setName);
            addNotBlankTest(null);
        }
    }

    @Nested
    class IsActiveTest extends AbstractValidationFieldTestBoolean {
        protected IsActiveTest() {
            super("isActive", UserDetailsDto::setIsActive);
            addRequiredTest(null);
        }
    }

    @Test
    void positiveConstructorTest() {
        LocalDateTime lastLoggedIn = LocalDateTime.now();
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.getEmail()).thenReturn("email");
        when(user.getName()).thenReturn("name");
        when(user.isActive()).thenReturn(true);
        when(user.getLastLoggedIn()).thenReturn(lastLoggedIn);
        when(user.getUserType()).thenReturn(UserType.COURT);
        when(user.getRoles()).thenReturn(Set.of(Role.MANAGER, Role.SENIOR_JUROR_OFFICER));

        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::isManager).thenReturn(true);

        List<UserCourtDto> courts = List.of(
            UserCourtDtoTest.getValidObject(),
            UserCourtDtoTest.getValidObject()
        );
        assertThat(new UserDetailsDto(user, courts))
            .isEqualTo(
                UserDetailsDto.builder()
                    .username("username")
                    .email("email")
                    .name("name")
                    .isActive(true)
                    .lastSignIn(lastLoggedIn)
                    .userType(UserType.COURT)
                    .roles(Set.of(Role.MANAGER, Role.SENIOR_JUROR_OFFICER))
                    .courts(courts)
                    .build()
            );


    }
}
