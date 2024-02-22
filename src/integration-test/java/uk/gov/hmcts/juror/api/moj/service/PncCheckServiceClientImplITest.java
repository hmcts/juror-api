package uk.gov.hmcts.juror.api.moj.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestUtil;
import uk.gov.hmcts.juror.api.config.RemoteConfig;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.client.contracts.PncCheckServiceClient;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest(httpPort = 8090)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Police National Computer Manual Check")
@SuppressWarnings("PMD.LawOfDemeter")
public class PncCheckServiceClientImplITest extends AbstractIntegrationTest {

    @Autowired
    private RemoteConfig config;

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;

    @Value("${jwt.secret.bureau}")
    protected String bureauSecret;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    private void initHeaders(String... userLevel) throws Exception {
        String level = "99";
        if (userLevel.length == 1) {
            level = userLevel[0];
        }

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel(level)
            .passwordWarning(false)
            .login("BUREAU_USER")
            .daysToExpire(89)
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    protected void stubResponse(
        PncCheckServiceClient.JurorCheckRequest jurorCheckRequest) throws JsonProcessingException {

        WireMock.stubFor(WireMock.post(config.getPncCheckService().getUrl())
            .withHeader("Content-Type", WireMock.containing(ContentType.APPLICATION_JSON.getMimeType()))
            .withRequestBody(WireMock.equalToJson(TestUtil.parseToJsonString(jurorCheckRequest)))
            .willReturn(WireMock.ok()
                .withHeader(HttpHeaders.CONNECTION, "close")));
    }

    @Test
    @DisplayName("Happy path for typical check")
    @Sql({"/db/mod/truncate.sql","/db/PncCheck_CreateJurors.sql"})
    public void pncCheckHappyPath() throws JsonProcessingException {
        stubResponse(PncCheckServiceClient.JurorCheckRequest.builder()
            .name(PncCheckServiceClient.NameDetails.builder()
                .firstName("Liam")
                .middleName("random")
                .lastName("Smith")
                .build())
            .dateOfBirth("03-10-1987")
            .postCode("M244BP")
            .jurorNumber("111111111")
            .build());

        final URI uri = URI.create("/api/v1/moj/pnc/manual?juror_number=111111111");

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PATCH, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Unhappy path for typical check with no postcode")
    @Sql({"/db/mod/truncate.sql", "/db/PncCheck_CreateJurors.sql"})
    public void pncCheckUnhappyPathNoPostcode() throws JsonProcessingException {
        stubResponse(PncCheckServiceClient.JurorCheckRequest.builder()
            .name(PncCheckServiceClient.NameDetails.builder()
                .firstName("Liam")
                .middleName("random")
                .lastName("Smith")
                .build())
            .dateOfBirth("03-10-1987")
            .postCode("")
            .jurorNumber("121212121")
            .build());

        final URI uri = URI.create("/api/v1/moj/pnc/manual?juror_number=121212121");

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PATCH, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Unhappy path for typical check with no date of birth")
    @Sql({"/db/mod/truncate.sql", "/db/PncCheck_CreateJurors.sql"})
    public void pncCheckUnhappyPathNoDateOfBirth() throws JsonProcessingException {
        stubResponse(PncCheckServiceClient.JurorCheckRequest.builder()
            .name(PncCheckServiceClient.NameDetails.builder()
                .firstName("Liam")
                .middleName("random")
                .lastName("Smith")
                .build())
            .dateOfBirth(null)
            .postCode("M244BP")
            .jurorNumber("333333333")
            .build());

        final URI uri = URI.create("/api/v1/moj/pnc/manual?juror_number=333333333");

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PATCH, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Unhappy path for typical check no juror number found matching")
    @Sql({"/db/mod/truncate.sql", "/db/PncCheck_CreateJurors.sql"})
    public void pncCheckUnhappyPathNoJurorNumber() throws JsonProcessingException {
        stubResponse(PncCheckServiceClient.JurorCheckRequest.builder()
            .name(PncCheckServiceClient.NameDetails.builder()
                .firstName("Liam")
                .middleName("random")
                .lastName("Smith")
                .build())
            .dateOfBirth("03-10-1987")
            .postCode("M244BP")
            .jurorNumber("111111112")
            .build());

        final URI uri = URI.create("/api/v1/moj/pnc/manual?juror_number=111111112");

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.PATCH, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Override
    protected String mintBureauJwt(final BureauJWTPayload payload) throws Exception {
        return TestUtil.mintBureauJwt(payload, SignatureAlgorithm.HS256, bureauSecret,
            Instant.now().plus(100L * 365L, ChronoUnit.DAYS));
    }
}
