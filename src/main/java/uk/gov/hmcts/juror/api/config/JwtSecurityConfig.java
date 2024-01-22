package uk.gov.hmcts.juror.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public class JwtSecurityConfig {
    private static final String PERMISSIONS_KEY = "permissions";

    @NotNull
    private long tokenValidity;

    @NotBlank
    private String secret;

    @NotBlank
    private String subject;

    private String issuer;

    @SuppressWarnings("java:S1450")//False positive as lombok creates getter
    private Map<String, Object> claims;


    @SuppressWarnings("java:S3740")//Have to use non parametrized type for map
    public void setClaims(Map<String, Object> claims) {
        if (claims.containsKey(PERMISSIONS_KEY) && claims.get(
            PERMISSIONS_KEY) instanceof Map claimsMap) {
            claims.put(PERMISSIONS_KEY, claimsMap.values());
        }
        this.claims = Collections.unmodifiableMap(claims);
    }
}
