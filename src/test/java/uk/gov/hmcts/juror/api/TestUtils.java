package uk.gov.hmcts.juror.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestUtils {

    public static final ObjectMapper objectMapper;

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

    public static BureauJWTPayload createJwt(String owner, String username) {
        return createJwt(owner, username, "99");
    }

    public static BureauJWTPayload createJwt(String owner, String username, String userLevel) {
        return BureauJWTPayload.builder()
            .owner(owner)
            .login(username)
            .staff(staffBuilder(username,Integer.valueOf(userLevel), List.of("415","400")))
            .userLevel(userLevel)
            .daysToExpire(89)
            .passwordWarning(false)
            .build();
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    public static BureauJWTPayload.Staff staffBuilder(String staffName, Integer rank, List<String> courts) {
        return BureauJWTPayload.Staff.builder()
            .name(staffName)
            .rank(rank)
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
        BureauJWTPayload bureauJwtPayload = createJwt(owner, username, level);
        BureauJwtAuthentication bureauJwtAuthentication = mock(BureauJwtAuthentication.class);
        when(bureauJwtAuthentication.getPrincipal()).thenReturn(bureauJwtPayload);
        SecurityContextHolder.getContext().setAuthentication(bureauJwtAuthentication);
    }
}