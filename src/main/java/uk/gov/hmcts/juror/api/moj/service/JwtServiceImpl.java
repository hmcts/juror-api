package uk.gov.hmcts.juror.api.moj.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {
    private final Clock clock;

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Value("${jwt.expiry.bureau}")
    private String bureauExpiry;

    @Autowired
    public JwtServiceImpl(Clock clock) {
        this.clock = clock;
    }

    private <T> T extractClaim(String jwt, String secret, Function<Claims, T> claimResolver) {
        return claimResolver.apply(extractClaims(jwt, secret));
    }

    @Override
    public String extractSubject(String jwt, String secret) {
        return extractClaim(jwt, secret, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String jwt, String secret) {
        return extractClaim(jwt, secret, Claims::getExpiration);
    }

    @Override
    public Claims extractClaims(String jwt, String secret) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
        } catch (Exception exception) {
            throw new InvalidJwtAuthenticationException("Failed to parse JWT", exception);
        }
    }

    @Override
    public boolean isJwtExpired(String jwt, String secret) {
        return extractExpiration(jwt, secret).before(new Date(clock.millis()));
    }

    SecretKey getSigningKey(String jwtSecret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @Override
    public String generateJwtToken(String id, String issuer, String subject, long tokenValidity, Key secretKey,
                                   Map<String, Object> claims) {
        try {
            Date issuedAtDate = new Date(clock.millis());
            return Jwts.builder()
                .id(id)
                .issuer(issuer)
                .claims(claims)
                .subject(subject)
                .issuedAt(issuedAtDate)
                .expiration(new Date(issuedAtDate.getTime() + tokenValidity))
                .signWith(secretKey)
                .compact();
        } catch (Exception exception) {
            throw new InvalidJwtAuthenticationException("Failed to parse JWT", exception);
        }
    }

    @Override
    public String generateBureauJwtToken(String id, BureauJwtPayload payload) {
        return generateJwtToken(
            id,
            "juror",
            null,
            timeUnitToMilliseconds(bureauExpiry),
            getSigningKey(bureauSecret),
            payload.toClaims()
        );
    }

    private long timeUnitToMilliseconds(String value) {
        String lastDigit = value.substring(value.length() - 1).toLowerCase();
        long number = Long.parseLong(value.substring(0, value.length() - 1));
        return switch (lastDigit) {
            case "h" -> number * 3600000;
            case "m" -> number * 60000;
            case "s" -> number * 1000;
            default -> throw new MojException.InternalServerError("Unknown number format:  '" + value + "'", null);
        };
    }
}
