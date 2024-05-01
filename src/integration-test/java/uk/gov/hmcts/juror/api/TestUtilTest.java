package uk.gov.hmcts.juror.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.public1.PublicJwtPayload;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for testing utility methods.
 */
public class TestUtilTest {
    private static final String SECRET_KEY_HMAC =
        "U2VjcmV0S2V5SE1BQ1NlY3JldEtleUhNQUNTZWNyZXRLZXlITUFDU2VjcmV0S2V5SE1BQw==";//
    // SecretKeyHMACSecretKeyHMACSecretKeyHMACSecretKeyHMAC
    private static final String SECRET_KEY_PUBLIC = "U2VjcmV0S2V5U2VjcmV0S2V5U2VjcmV0S2V5U2VjcmV0S2V5";//
    // SecretKeySecretKeySecretKeySecretKey
    private static final String SECRET_KEY_BUREAU = "U2VjcmV0S2V5MlNlY3JldEtleTJTZWNyZXRLZXkyU2VjcmV0S2V5Mg==";//
    // SecretKey2SecretKey2SecretKey2SecretKey2

    private static final String TEST_ID = "123";
    private static final String TEST_JUROR_NUMBER = "987654321";
    private static final String[] TEST_ROLES = {"juror", "test"};
    private static final Instant HUNDRED_YEARS = Instant.now().plusSeconds(60L * 60L * 24L * 365L * 100L);
    private static final String TEST_BUREAU_LOGIN = "testuser";
    private static final Integer TEST_BUREAU_DAYS_TO_EXPIRE = 888;
    private static final String TEST_BUREAU_OWNER = JurorDigitalApplication.JUROR_OWNER;
    private static final Boolean TEST_BUREAU_PASSWORD_WARNING = Boolean.FALSE;
    private static final String TEST_BUREAU_USER_LEVEL = "3";

    @Test
    public void testMintPublicJwt() throws Exception {
        PublicJwtPayload jwtPayload = new PublicJwtPayload();
        jwtPayload.setId(TEST_ID);
        jwtPayload.setJurorNumber(TEST_JUROR_NUMBER);
        jwtPayload.setRoles(TEST_ROLES);

        // encode a token
        final String jwt = TestUtil.mintPublicJwt(jwtPayload, SignatureAlgorithm.HS256, SECRET_KEY_PUBLIC,
            HUNDRED_YEARS);
        assertThat(jwt).isNotEmpty();

        //decode the token and test validity
        Jws<Claims> claimsJws = Jwts.parser()
            .setSigningKey(SECRET_KEY_PUBLIC)
            .build()
            .parseClaimsJws(jwt);

        ObjectMapper objectMapper = new ObjectMapper();
        PublicJwtPayload data = objectMapper.convertValue(claimsJws.getBody().get("data"), PublicJwtPayload.class);


        assertThat(data).isNotNull();
        assertThat(data.getId()).isEqualTo(TEST_ID);
        assertThat(data.getJurorNumber()).isEqualTo(TEST_JUROR_NUMBER);
        assertThat(data.getSurname()).isNull();
        assertThat(data.getRoles())
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyInAnyOrder(TEST_ROLES)
        ;
    }


    @Test
    public void testMintBureauJwt() throws Exception {
        BureauJwtPayload jwtPayload = new BureauJwtPayload(TEST_BUREAU_OWNER, TEST_BUREAU_LOGIN, TEST_BUREAU_USER_LEVEL, null);

        // encode a token
        final String jwt = TestUtil.mintBureauJwt(jwtPayload, SignatureAlgorithm.HS256, SECRET_KEY_BUREAU,
            HUNDRED_YEARS);
        assertThat(jwt).isNotEmpty();

        //decode the token and test validity
        Jws<Claims> claimsJws = Jwts.parser()
            .setSigningKey(SECRET_KEY_BUREAU)
            .build()
            .parseClaimsJws(jwt);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        BureauJwtPayload data = objectMapper.convertValue(claimsJws.getBody(), BureauJwtPayload.class);


        assertThat(data).isNotNull();
        assertThat(data.getLogin()).isEqualTo(TEST_BUREAU_LOGIN);
        assertThat(data.getOwner()).isEqualTo(TEST_BUREAU_OWNER);
        assertThat(data.getUserLevel()).isEqualTo(TEST_BUREAU_USER_LEVEL);
    }

    @Test
    public void testMintHmacJwt() throws Exception {
        // encode a token
        final String jwt = TestUtil.mintHmacJwt(SignatureAlgorithm.HS256, SECRET_KEY_HMAC, HUNDRED_YEARS);
        assertThat(jwt).isNotEmpty();

        //decode the token and test validity
        Jws<Claims> claimsJws = Jwts.parser()
            .setSigningKey(SECRET_KEY_HMAC)
            .build()
            .parseClaimsJws(jwt);

        assertThat(claimsJws).isNotNull();
    }

}