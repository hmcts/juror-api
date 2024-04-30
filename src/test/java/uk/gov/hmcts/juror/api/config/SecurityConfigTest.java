package uk.gov.hmcts.juror.api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthenticationProvider;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.hmac.HmacJwtAuthenticationProvider;
import uk.gov.hmcts.juror.api.config.public1.PublicJwtAuthenticationProvider;
import uk.gov.hmcts.juror.api.config.public1.PublicJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.service.JwtServiceImpl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SecurityConfigTest.SecurityConfigControllerTest.class)
@ContextConfiguration(classes = {
    SecurityConfigTest.SecurityConfigControllerTest.class,
    SecurityConfig.class,
    SecurityConfigEndpoints.class,
    RestfulAuthenticationEntryPoint.class,
    PublicJwtAuthenticationProvider.class,
    BureauJwtAuthenticationProvider.class,
    HmacJwtAuthenticationProvider.class,
    JwtServiceImpl.class,
    ApplicationBeans.class
})
@ActiveProfiles({"development", "test"})
class SecurityConfigTest {

    @MockBean
    private CourtLocationRepository courtLocationRepository;

    protected static String bureauSecret;
    protected static String publicSecret;
    protected static String hmacSecret;

    @Value("${jwt.secret.bureau}")
    public void setBureauSecret(String secret) {
        bureauSecret = secret;
    }

    @Value("${jwt.secret.public}")
    public void setPublicSecret(String secret) {
        publicSecret = secret;
    }

    @Value("${jwt.secret.hmac}")
    public void setHmacSecret(String secret) {
        hmacSecret = secret;
    }

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "[{index}] Authorised request using JWT Type: {0} for url {1}")
    @MethodSource("authorisedRequests")
    void authorised(String jwtType, String url, String jwt) throws Exception {
        mockMvc.perform(get(url)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }


    @ParameterizedTest(name = "[{index}] Unauthorised request expected JWT Type: {0} but got JWT Type {1} for url {2}")
    @MethodSource("unauthorisedRequests")
    void unauthorised(String expectedJwtType, String actualJwtType, String url, String jwt) throws Exception {
        assertThrows(InvalidJwtAuthenticationException.class, () -> {
            mockMvc.perform(get(url)
                    .header(HttpHeaders.AUTHORIZATION, jwt)
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnauthorized());
        });
    }

    private Stream<Arguments> unauthorisedRequests() {
        Stream.Builder<Arguments> builder = Stream.builder();

        for (UrLs urls : UrLs.values()) {
            for (UrLs.JwtType jwtType : UrLs.JwtType.values()) {
                final String token;
                if (jwtType == urls.jwtType) {
                    //If the url expects this token type provide the invalid secret
                    token = jwtType.getInvalidToken();
                } else {
                    //If the url does not expect this token type provide a valid secret
                    token = jwtType.getValidToken();
                }
                builder.add(Arguments.arguments(urls.jwtType.name(), jwtType.name(), urls.url, token));

            }
        }
        return builder.build();
    }

    private Stream<Arguments> authorisedRequests() {
        Stream.Builder<Arguments> builder = Stream.builder();
        for (UrLs urls : UrLs.values()) {
            builder.add(Arguments.arguments(urls.jwtType.name(), urls.url, urls.jwtType.getValidToken()));
        }
        return builder.build();
    }

    private enum UrLs {
        hmacAuth1(JwtType.HMAC, "/api/v1/auth/juror/test"),
        hmacAuth2(JwtType.HMAC, "/api/v1/auth/bureau/test"),
        hmacAuth3(JwtType.HMAC, "/api/v1/auth/public/test"),
        hmacAuth4(JwtType.HMAC, "/api/v1/auth/settings/test"),
        publicAuth(JwtType.PUBLIC, "/api/v1/public/test"),
        bureauAuth(JwtType.BUREAU, "/api/v1/bureau/test"),
        bureauAuth2(JwtType.BUREAU, "/api/v1/moj/test");

        private final String url;
        private final JwtType jwtType;

        UrLs(JwtType jwtType, String url) {
            this.url = url;
            this.jwtType = jwtType;
        }

        private enum JwtType {
            HMAC(SecurityConfigTest.hmacSecret, JwtType::getHmacClaimMap),
            PUBLIC(SecurityConfigTest.publicSecret, JwtType::getPublicClaimMap),
            BUREAU(SecurityConfigTest.bureauSecret, JwtType::getBureauClaimMap);

            private final String secret;
            private final Supplier<Map<String, Object>> jwtClaimMapSupplier;

            JwtType(String secret, Supplier<Map<String, Object>> jwtClaimMapSupplier) {
                this.secret = secret;
                this.jwtClaimMapSupplier = jwtClaimMapSupplier;
            }

            public Map<String, Object> getClaimMap() {
                return jwtClaimMapSupplier.get();
            }

            public String getValidToken() {
                return getJwt(secret, getClaimMap());
            }

            public String getInvalidToken() {
                return getJwt(secret + "Invalid", jwtClaimMapSupplier.get());
            }

            private static Map<String, Object> getPublicClaimMap() {
                PublicJwtPayload payload = PublicJwtPayload.builder()
                    .jurorNumber("209092530")
                    .postcode("AB3 9RY")
                    .surname("CASTILLO")
                    .roles(new String[]{"juror"})
                    .id("")
                    .build();
                final Map<String, Object> claimsMap = new HashMap<>();
                claimsMap.put("data", payload);
                return claimsMap;
            }

            private static Map<String, Object> getHmacClaimMap() {
                return new HashMap<>();
            }

            private static Map<String, Object> getBureauClaimMap() {
                BureauJwtPayload payload = BureauJwtPayload.builder()
                    .userType(UserType.BUREAU)
                    .login("COURT_USER")
                    .owner("400")
                    .staff(BureauJwtPayload.Staff.builder().courts(Collections.singletonList("415")).build())
                    .build();
                final Map<String, Object> claimsMap = new HashMap<>();
                claimsMap.put("daysToExpire", payload.getDaysToExpire());
                claimsMap.put("login", payload.getLogin());
                claimsMap.put("owner", payload.getOwner());
                claimsMap.put("passwordWarning", payload.getPasswordWarning());
                claimsMap.put("userLevel", payload.getUserLevel());
                claimsMap.put("staff", payload.getStaff());
                return claimsMap;
            }

            public static String getJwt(String secret, Map<String, Object> claimsMap) {
                claimsMap.put(Claims.EXPIRATION, Date.from(Instant.now().plus(100L * 365L, ChronoUnit.DAYS)));
                claimsMap.put(Claims.ISSUED_AT, Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant()));
                return Jwts.builder()
                    .claims(claimsMap)
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                    .compact();
            }

            public Object getJwtWithClaimMap(Map<String, Object> claimMap) {
                return getJwt(secret, claimMap);
            }
        }
    }

    @RestController
    public static class SecurityConfigControllerTest {
        @GetMapping({
            "/api/v1/public/test",
            "/api/v1/bureau/test",
            "/api/v1/moj/test",
            "/api/v1/auth/juror/test",
            "/api/v1/auth/bureau/test",
            "/api/v1/auth/public/test",
            "/api/v1/auth/settings/test"
        })
        public ResponseEntity<Map<String, Boolean>> validResponse() {
            return ResponseEntity.ok(Collections.singletonMap("isValid", true));
        }
    }
}