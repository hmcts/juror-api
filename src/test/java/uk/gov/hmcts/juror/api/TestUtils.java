package uk.gov.hmcts.juror.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.mockito.MockedStatic;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public final class TestUtils {
    public static final ObjectMapper objectMapper;
    private static MockedStatic<SecurityUtil> SECURITY_UTIL_MOCK;

    static {
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    private TestUtils() {
    }

    public static String buildStringToLength(int length) {
        int count = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while (count < length) {
            stringBuilder.append('x');
            count++;
        }
        return stringBuilder.toString();
    }

    public static BureauJwtPayload createJwt(String owner, String username) {
        return createJwt(owner, username, "99");
    }

    public static BureauJwtPayload createJwt(String owner, String username, String userLevel) {
        return BureauJwtPayload.builder()
            .owner(owner)
            .login(username)
            .staff(staffBuilder(username, Integer.valueOf(userLevel), List.of("415", "400")))
            .userLevel(userLevel)
            .build();
    }

    public static BureauJwtPayload createJwt(String owner, String username, UserType userType, List<Role> roles) {
        return BureauJwtPayload.builder()
            .owner(owner)
            .userType(userType)
            .activeUserType(userType)
            .roles(roles)
            .login(username)
            .staff(staffBuilder(username, List.of(owner)))
            .build();
    }

    public static BureauJwtPayload createJwt(String owner, String username, String userLevel, List<String> courts) {
        UserType userType = "400".equals(owner) ? UserType.BUREAU : UserType.COURT;
        return BureauJwtPayload.builder()
            .owner(owner)
            .locCode(owner)
            .login(username)
            .activeUserType(userType)
            .userType(userType)
            .staff(staffBuilder(username, Integer.valueOf(userLevel), courts))
            .userLevel(userLevel)
            .build();
    }

    public static BureauJwtPayload.Staff staffBuilder(String staffName, Integer rank, List<String> courts) {
        return BureauJwtPayload.Staff.builder()
            .name(staffName)
            .rank(rank)
            .courts(courts)
            .build();
    }

    public static BureauJwtPayload.Staff staffBuilder(String staffName, List<String> courts) {
        return BureauJwtPayload.Staff.builder()
            .name(staffName)
            .courts(courts)
            .build();
    }

    public static boolean compareDateToLocalDate(Date date, LocalDate localDate) {
        LocalDate dateConverted = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return dateConverted.getYear() == localDate.getYear()
            && dateConverted.getMonth() == localDate.getMonth()
            && dateConverted.getDayOfMonth() == localDate.getDayOfMonth();
    }


    public static void compareLocalDates(LocalDate date1, LocalDate date2) {

        Assertions.assertThat(date1.getYear()).isEqualTo(date2.getYear());
        Assertions.assertThat(date1.getMonthValue()).isEqualTo(date2.getMonthValue());
        Assertions.assertThat(date1.getDayOfMonth()).isEqualTo(date2.getDayOfMonth());

    }

    /**
     * Serialise a java object into a JSON string.
     *
     * @param obj the Java object to be serialised in to a JSON string
     * @return a String containing the object structure and data in JSON format
     */
    @SneakyThrows
    public static String asJsonString(final Object obj) {
        try {
            return objectMapper.findAndRegisterModules().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set up a JWT for the current user and add to security context.
     *
     * @param owner    the owner of the current user
     * @param username the username of the current user
     * @param level    the level of the current user
     */
    public static void setupAuthentication(String owner, String username, String level) {
        BureauJwtPayload bureauJwtPayload = createJwt(owner, username, level);
        BureauJwtAuthentication bureauJwtAuthentication = mock(BureauJwtAuthentication.class);
        when(bureauJwtAuthentication.getPrincipal()).thenReturn(bureauJwtPayload);
        SecurityContextHolder.getContext().setAuthentication(bureauJwtAuthentication);
    }

    public static void setUpMockAuthentication(String owner, String username, String userLevel, List<String> courts) {

        BureauJwtAuthentication auth = mock(BureauJwtAuthentication.class);
        when(auth.getPrincipal())
            .thenReturn(TestUtils.createJwt(owner, username, userLevel, courts));

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterAll
    public static void afterAll() {
        if (SECURITY_UTIL_MOCK != null) {
            SECURITY_UTIL_MOCK.close();
            SECURITY_UTIL_MOCK = null;
        }
    }

    public static MockedStatic<SecurityUtil> getSecurityUtilMock() {
        if (SECURITY_UTIL_MOCK == null) {
            SECURITY_UTIL_MOCK = mockStatic(SecurityUtil.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        }
        return SECURITY_UTIL_MOCK;
    }

    public static void mockSecurityUtil(BureauJwtPayload payload) {
        MockedStatic<SecurityUtil> securityUtil = getSecurityUtilMock();
        securityUtil.when(SecurityUtil::getActiveUsersBureauPayload).thenReturn(payload);
    }

    public static void mockBureauUser() {
        mockSecurityUtil(BureauJwtPayload.builder()
            .owner("400")
            .locCode("400")
            .roles(Set.of())
            .userType(UserType.BUREAU)
            .activeUserType(UserType.BUREAU)
            .build());

    }

    public static void mockCourtUser(String owner) {
        mockCourtUser(owner, owner);
    }

    public static void mockCourtUser(String owner, String locCode) {
        mockSecurityUtil(BureauJwtPayload.builder()
            .owner(owner)
            .locCode(locCode)
            .roles(Set.of())
            .userType(UserType.COURT)
            .activeUserType(UserType.COURT)
            .build());
    }
}
