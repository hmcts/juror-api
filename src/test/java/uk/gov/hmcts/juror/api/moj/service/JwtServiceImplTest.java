package uk.gov.hmcts.juror.api.moj.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.utils.TestConstants;

import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        JwtServiceImpl.class
    }
)
@DisplayName("JwtServiceImpl")
@SuppressWarnings("unchecked")
class JwtServiceImplTest {

    @Autowired
    private JwtServiceImpl jwtService;

    @MockBean
    private Clock clock;

    private Date currentDate;

    private MockedStatic<Jwts> jwtsMockedStatic;

    @BeforeEach
    void beforeEach() {
        jwtsMockedStatic = Mockito.mockStatic(Jwts.class);
        currentDate = new Date(System.currentTimeMillis());
        when(clock.millis()).thenReturn(currentDate.getTime());
    }

    @AfterEach
    void afterEach() {
        if (jwtsMockedStatic != null) {
            jwtsMockedStatic.close();
        }
    }

    private Claims setupValidJwtMock() {
        JwtParserBuilder jwtParserBuilder = mock(JwtParserBuilder.class);
        JwtParser jwtParser = mock(JwtParser.class);
        Jws<Claims> jwtClaims = mock(Jws.class);
        Claims claims = mock(Claims.class);
        jwtsMockedStatic.when(Jwts::parser).thenReturn(jwtParserBuilder);
        when(jwtParserBuilder.verifyWith(any(SecretKey.class))).thenReturn(jwtParserBuilder);
        when(jwtParserBuilder.build()).thenReturn(jwtParser);
        when(jwtParser.parseSignedClaims(TestConstants.JWT)).thenReturn(jwtClaims);
        when(jwtClaims.getPayload()).thenReturn(claims);
        return claims;
    }

    private JwtBuilder setupJwtTokenGenerator() {
        JwtBuilder jwtBuilder = mock(JwtBuilder.class);
        jwtsMockedStatic.when(Jwts::builder).thenReturn(jwtBuilder);

        when(jwtBuilder.id(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.issuer(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.claims(anyMap())).thenReturn(jwtBuilder);
        when(jwtBuilder.subject(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.issuedAt(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.expiration(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.signWith(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.compact()).thenReturn(TestConstants.JWT);
        return jwtBuilder;
    }

    @DisplayName("public String extractSubject(String jwt, String secret)")
    @Nested
    class ExtractSubject {

        @Test
        void positive_username_found() {
            String subject = "schedular@cgi.com";
            Claims claims = setupValidJwtMock();
            when(claims.getSubject()).thenReturn(subject);
            assertEquals(subject, jwtService.extractSubject(TestConstants.JWT, TestConstants.JWT_SECRET));
            verify(claims, times(1)).getSubject();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negative_username_not_found() {
            Claims claims = setupValidJwtMock();
            when(claims.getSubject()).thenReturn(null);
            assertNull(jwtService.extractSubject(TestConstants.JWT, TestConstants.JWT_SECRET));
            verify(claims, times(1)).getSubject();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negative_invalid_jwt() {
            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::parser).thenThrow(exception);

            InvalidJwtAuthenticationException unauthorisedException = assertThrows(
                InvalidJwtAuthenticationException.class,
                () -> jwtService.extractSubject(TestConstants.JWT, TestConstants.JWT_SECRET)
            );
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage());
            assertEquals(exception, unauthorisedException.getCause());
        }
    }

    @DisplayName("public Date extractExpiration(String jwt, String secret)")
    @Nested
    class ExtractExpiration {

        @Test
        void positive_expiration_date_found() {
            Date expirationDate = new Date();
            Claims claims = setupValidJwtMock();
            when(claims.getExpiration()).thenReturn(expirationDate);
            assertEquals(expirationDate, jwtService.extractExpiration(TestConstants.JWT, TestConstants.JWT_SECRET));
            verify(claims, times(1)).getExpiration();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negative_expiration_date_not_found() {
            Claims claims = setupValidJwtMock();
            when(claims.getSubject()).thenReturn(null);
            assertNull(jwtService.extractExpiration(TestConstants.JWT, TestConstants.JWT_SECRET));
            verify(claims, times(1)).getExpiration();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negative_invalid_jwt() {
            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::parser).thenThrow(exception);

            InvalidJwtAuthenticationException unauthorisedException = assertThrows(
                InvalidJwtAuthenticationException.class,
                () -> jwtService.extractExpiration(TestConstants.JWT, TestConstants.JWT_SECRET)
            );
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage());
            assertEquals(exception, unauthorisedException.getCause());
        }
    }

    @DisplayName(
        "public String generateJwtToken(String id, String issuer, String subject, long tokenValidity, Key secretKey, "
            + "Map<String, Object> claims)")
    @Nested
    class GenerateJwtTokenFull {
        private Map<String, Object> getClaims() {
            return Map.of(
                "Key", "Value",
                "Key 2", "Value 2",
                "Key 3", "Value 3");
        }

        @Test
        void positive_token_generated() {
            String id = "MyId";
            String issuer = "MyIssuer";
            String subject = "MySubject";
            long tokenValidity = 500L;
            Key secretKey = mock(Key.class);
            Map<String, Object> claims = getClaims();

            JwtBuilder jwtBuilder = setupJwtTokenGenerator();

            assertEquals(TestConstants.JWT,
                jwtService.generateJwtToken(id, issuer, subject, tokenValidity, secretKey, claims));

            verify(jwtBuilder, times(1)).id(id);
            verify(jwtBuilder, times(1)).issuer(issuer);
            verify(jwtBuilder, times(1)).claims(claims);
            verify(jwtBuilder, times(1)).subject(subject);
            verify(jwtBuilder, times(1)).issuedAt(currentDate);
            verify(jwtBuilder, times(1)).expiration(new Date(currentDate.getTime() + tokenValidity));
            verify(jwtBuilder, times(1)).signWith(secretKey);
            verify(jwtBuilder, times(1)).compact();
        }

        @Test
        void negative_exception_raised() {
            String id = "MyId";
            String issuer = "MyIssuer";
            String subject = "MySubject";
            long tokenValidity = 500L;
            Key secretKey = mock(Key.class);
            Map<String, Object> claims = getClaims();

            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::builder).thenThrow(exception);

            InvalidJwtAuthenticationException unauthorisedException = assertThrows(
                InvalidJwtAuthenticationException.class,
                () -> jwtService.generateJwtToken(id, issuer, subject, tokenValidity, secretKey, claims));
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage());
            assertEquals(exception, unauthorisedException.getCause());
        }
    }
}
