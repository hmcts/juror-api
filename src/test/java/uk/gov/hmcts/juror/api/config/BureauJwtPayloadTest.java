package uk.gov.hmcts.juror.api.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.LawOfDemeter")
class BureauJwtPayloadTest {

    private CourtLocation mockCourtLocation(String owner, String locCode, CourtType courtType) {
        CourtLocation location = mock(CourtLocation.class);
        when(location.getOwner()).thenReturn(owner);
        when(location.getLocCode()).thenReturn(locCode);
        when(location.getType()).thenReturn(courtType);
        return location;
    }

    @Test
    void positiveUserConstructor() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("name1");
        when(user.getUsername()).thenReturn("username");
        when(user.getLevel()).thenReturn(3);
        when(user.getUserType()).thenReturn(UserType.COURT);
        when(user.getRoles()).thenReturn(Set.of(Role.MANAGER));
        when(user.isActive()).thenReturn(true);
        BureauJwtPayload payload = new BureauJwtPayload(
            user,
            "401",
            List.of(
                mockCourtLocation("400", "401", CourtType.MAIN),
                mockCourtLocation("400", "402", CourtType.SATELLITE),
                mockCourtLocation("400", "403", CourtType.SATELLITE)
            )
        );

        assertThat(payload.getOwner()).isEqualTo("400");
        assertThat(payload.getLocCode()).isEqualTo("401");
        assertThat(payload.getLogin()).isEqualTo("username");
        assertThat(payload.getUserLevel()).isEqualTo("3");
        assertThat(payload.getPasswordWarning()).isFalse();
        assertThat(payload.getDaysToExpire()).isEqualTo(999);
        assertThat(payload.getUserType()).isEqualTo(UserType.COURT);
        assertThat(payload.getRoles()).containsExactly(Role.MANAGER);
        assertThat(payload.getStaff()).isEqualTo(
            new BureauJwtPayload.Staff(
                "name1",
                3,
                1,
                List.of("401", "402", "403")
            )
        );
    }

    @Test
    void positiveUserConstructorIsAdmin() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("name1");
        when(user.getUsername()).thenReturn("username");
        when(user.getLevel()).thenReturn(3);
        when(user.getUserType()).thenReturn(UserType.ADMINISTRATOR);
        when(user.getRoles()).thenReturn(Set.of(Role.MANAGER));
        when(user.isActive()).thenReturn(true);
        BureauJwtPayload payload = new BureauJwtPayload(
            user,
            "401",
            List.of(
                mockCourtLocation("400", "401", CourtType.MAIN),
                mockCourtLocation("400", "402", CourtType.SATELLITE),
                mockCourtLocation("400", "403", CourtType.SATELLITE)
            )
        );

        assertThat(payload.getOwner()).isEqualTo("400");
        assertThat(payload.getLocCode()).isEqualTo("401");
        assertThat(payload.getLogin()).isEqualTo("username");
        assertThat(payload.getUserLevel()).isEqualTo("3");
        assertThat(payload.getPasswordWarning()).isFalse();
        assertThat(payload.getDaysToExpire()).isEqualTo(999);
        assertThat(payload.getUserType()).isEqualTo(UserType.ADMINISTRATOR);
        assertThat(payload.getRoles()).containsExactly(Role.values());
        assertThat(payload.getStaff()).isEqualTo(
            new BureauJwtPayload.Staff(
                "name1",
                3,
                1,
                List.of("401", "402", "403")
            )
        );
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void positiveToClaims() {
        BureauJwtPayload payload = new BureauJwtPayload(
            "owner",
            "locCode",
            "login",
            "userLevel",
            false,
            999,
            new BureauJwtPayload.Staff(
                "staffName",
                1,
                2,
                List.of("400", "415")
            ),
            UserType.COURT,
            Set.of(Role.MANAGER)
        );
        assertThat(payload.toClaims())
            .hasSize(9)
            .containsEntry("owner", "owner")
            .containsEntry("locCode", "locCode")
            .containsEntry("login", "login")
            .containsEntry("userLevel", "userLevel")
            .containsEntry("passwordWarning", false)
            .containsEntry("daysToExpire", 999)
            .containsEntry("userType", UserType.COURT)
            .containsEntry("roles", Set.of(Role.MANAGER))
            .containsEntry("staff", Map.of(
                "name", "staffName",
                "rank", 1,
                "active", 2,
                "courts", List.of("400", "415")
            ));
    }

    @Test
    void positiveFromClaims() {
        Claims claims = mock(Claims.class);
        when(claims.get("staff", Map.class)).thenReturn(Map.of(
            "name", "staffName",
            "rank", 1,
            "active", 2,
            "courts", List.of("400", "415")
        ));
        when(claims.containsKey("roles")).thenReturn(true);
        when(claims.get("roles", List.class)).thenReturn(List.of(Role.MANAGER.name()));
        when(claims.containsKey("userType")).thenReturn(true);
        when(claims.get("userType", String.class)).thenReturn(UserType.COURT.name());

        when(claims.get("daysToExpire", Integer.class)).thenReturn(999);


        when(claims.get("login", String.class)).thenReturn("login");
        when(claims.get("owner", String.class)).thenReturn("owner");
        when(claims.get("locCode", String.class)).thenReturn("locCode");
        when(claims.get("passwordWarning", Boolean.class)).thenReturn(false);
        when(claims.containsKey("userLevel")).thenReturn(true);
        when(claims.get("userLevel", String.class)).thenReturn("userLevel");


        assertThat(BureauJwtPayload.fromClaims(claims))
            .isEqualTo(
                new BureauJwtPayload(
                    "owner",
                    "locCode",
                    "login",
                    "userLevel",
                    false,
                    999,
                    new BureauJwtPayload.Staff(
                        "staffName",
                        1,
                        2,
                        List.of("400", "415")
                    ),
                    UserType.COURT,
                    List.of(Role.MANAGER)
                )
            );
    }

    @Test
    void positiveFromClaimsMissingKeys() {
        Claims claims = mock(Claims.class);
        when(claims.get("staff", Map.class)).thenReturn(Map.of(
            "name", "staffName",
            "rank", 1,
            "active", 2,
            "courts", List.of("400", "415")
        ));
        when(claims.containsKey("roles")).thenReturn(false);
        when(claims.containsKey("userType")).thenReturn(false);

        when(claims.get("daysToExpire", Integer.class)).thenReturn(999);


        when(claims.get("login", String.class)).thenReturn("login");
        when(claims.get("owner", String.class)).thenReturn("owner");
        when(claims.get("locCode", String.class)).thenReturn("locCode");
        when(claims.get("passwordWarning", Boolean.class)).thenReturn(false);
        when(claims.containsKey("userLevel")).thenReturn(true);
        when(claims.get("userLevel", String.class)).thenReturn("userLevel");


        assertThat(BureauJwtPayload.fromClaims(claims))
            .isEqualTo(
                new BureauJwtPayload(
                    "owner",
                    "locCode",
                    "login",
                    "userLevel",
                    false,
                    999,
                    new BureauJwtPayload.Staff(
                        "staffName",
                        1,
                        2,
                        List.of("400", "415")
                    ),
                    null,
                    Collections.emptyList()
                )
            );
    }

    @Nested
    class StaffTest {
        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveToClaims() {
            BureauJwtPayload.Staff staff = new BureauJwtPayload.Staff(
                "StaffName",
                1,
                2,
                List.of("400", "415")
            );
            assertThat(staff.toClaims())
                .hasSize(4)
                .containsEntry("name", "StaffName")
                .containsEntry("rank", 1)
                .containsEntry("active", 2)
                .containsEntry("courts", List.of("400", "415"));
        }

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveFromClaimsHasValues() {
            assertThat(BureauJwtPayload.Staff.fromClaims(Map.of(
                "name", "StaffName",
                "rank", 1,
                "active", 2,
                "courts", List.of("400", "415")
            ))).isEqualTo(
                new BureauJwtPayload.Staff(
                    "StaffName",
                    1,
                    2,
                    List.of("400", "415")
                )
            );
        }

        @Test
        void positiveFromClaimsDefaultValues() {
            assertThat(BureauJwtPayload.Staff.fromClaims(Map.of("someKey", "someValue")))
                .isEqualTo(
                    new BureauJwtPayload.Staff(
                        "",
                        Integer.MIN_VALUE,
                        0,
                        Collections.emptyList()
                    )
                );
        }

        @Test
        void positiveFromClaimsNull() {
            assertThat(BureauJwtPayload.Staff.fromClaims(null))
                .isEqualTo(
                    new BureauJwtPayload.Staff(
                        null,
                        null,
                        null,
                        Collections.emptyList()
                    )
                );
        }

        @Test
        void positiveFromClaimsEmptyMap() {
            assertThat(BureauJwtPayload.Staff.fromClaims(Map.of()))
                .isEqualTo(
                    new BureauJwtPayload.Staff(
                        null,
                        null,
                        null,
                        Collections.emptyList()
                    )
                );
        }
    }
}
