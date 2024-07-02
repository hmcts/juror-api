package uk.gov.hmcts.juror.api.juror.controller;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.TestUtil;
import uk.gov.hmcts.juror.api.config.public1.PublicJwtPayload;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.service.JurorPersistenceService;
import uk.gov.hmcts.juror.api.juror.service.JurorServiceImpl.JurorResponseAlreadyExistsException;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.ExcessiveImports")
public class PublicEndpointControllerTest extends AbstractIntegrationTest {

    @MockBean
    private JurorPersistenceService mockJurorPersistenceService;

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;

    @Value("${jwt.secret.public}")
    private String publicSecret;

    private LocalDate dob40YearsOld;
    private JurorResponseDto.Qualify validQualify;

    @SuppressWarnings("Duplicates")
    @Before
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        //create a valid DOB
        dob40YearsOld = LocalDate.now().minusYears(40L);

        //create a valid Qualify
        validQualify = JurorResponseDto.Qualify.builder()
            .convicted(JurorResponseDto.Answerable.builder().answer(false).build())
            .livedConsecutive(JurorResponseDto.Answerable.builder().answer(true).build())
            .mentalHealthAct(JurorResponseDto.Answerable.builder().answer(false).build())
            .onBail(JurorResponseDto.Answerable.builder().answer(false).build())
            .build();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicEndpointControllerTest.respondToSummons_happy.sql")
    public void respondToSummons_constraintViolationException() throws Exception {
        final URI uri = URI.create("/api/v1/public/juror/respond");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintPublicJwt(PublicJwtPayload.builder()
            .jurorNumber("789456123")
            .postcode("G6 1AB")
            .surname("SMITH")
            .roles(new String[]{"juror"})
            .id("")
            .build())
        );

        final JurorResponseDto dto = JurorResponseDto.builder(
                "789456123", "Joe", "Smith", "123456 Pleasant Walk",
                "Grimsby",
                "Town Centre",
                "G6 1AB", dob40YearsOld,
                "012341234567", "joe@smith.dev", validQualify, null, ReplyMethod.DIGITAL)
            .title("Mr")
            .build();

        final JurorResponseAlreadyExistsException mockException = new JurorResponseAlreadyExistsException("",
            new DataIntegrityViolationException(""));
        given(mockJurorPersistenceService.persistJurorResponse(any(JurorResponseDto.class))).willThrow(
            new RuntimeException(mockException));

        RequestEntity<JurorResponseDto> requestEntity = new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);

        verify(mockJurorPersistenceService).persistJurorResponse(any(JurorResponseDto.class));
    }

    public String mintPublicJwt(final PublicJwtPayload payload) {
        return TestUtil.mintPublicJwt(payload, SignatureAlgorithm.HS256, publicSecret,
            Instant.now().plus(100L * 365L, ChronoUnit.DAYS));
    }
}
