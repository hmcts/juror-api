package uk.gov.hmcts.juror.api.moj.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.config.RemoteConfig;
import uk.gov.hmcts.juror.api.moj.client.contracts.PncCheckServiceClient;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.JwtService;

import java.time.format.DateTimeFormatter;


@Slf4j
@Component
public class PncCheckServiceClientImpl extends AbstractRemoteRestClient implements
    PncCheckServiceClient {


    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final JurorRepository jurorRepository;

    private final String singleUpdateUrl;

    @Autowired
    public PncCheckServiceClientImpl(final JwtService jwtService, JurorRepository jurorRepository,
                                     final RemoteConfig config) {
        super(config.pncCheckServiceRestTemplateBuilder(jwtService));
        this.jurorRepository = jurorRepository;
        this.singleUpdateUrl = config.getPncCheckService().getUrl();
    }

    @Override
    public void checkJuror(String jurorNumber) {
        log.info("Manual police check requested for juror number: " + jurorNumber);
        JurorCheckRequest jurorCheckRequest = createJurorCheckRequest(jurorNumber);
        try {
            HttpEntity<JurorCheckRequest>
                requestUpdate = new HttpEntity<>(jurorCheckRequest);


            ResponseEntity<Void> response =
                restTemplate.exchange(singleUpdateUrl, HttpMethod.POST, requestUpdate, Void.class);

            final HttpStatusCode statusCode = response.getStatusCode();
            if (!statusCode.equals(HttpStatus.OK)) {
                throw new MojException.RemoteGatewayException(
                    "Call to PncCheckServiceClientImpl check juror failed ",
                    null);
            }
        } catch (Throwable throwable) {
            String message = "Failed to trigger juror pnc check";
            log.error(message, throwable);
            throw new MojException.InternalServerError(message, throwable);
        }
    }

    private JurorCheckRequest createJurorCheckRequest(String jurorNumber) {
        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);

        if (juror == null) {
            throw new MojException.NotFound("No juror found with juror number: " + jurorNumber, null);
        }
        if (StringUtils.isBlank(juror.getPostcode()) || juror.getDateOfBirth() == null) {
            throw new MojException.BadRequest("Juror postcode and date of birth cannot be null", null);
        }

        return JurorCheckRequest.builder()
            .postCode(juror.getPostcode().replaceAll(" ", ""))
            .dateOfBirth(dateFormatter.format(juror.getDateOfBirth()))
            .jurorNumber(juror.getJurorNumber())
            .name(NameDetails.builder()
                .firstName(juror.getFirstName().replaceAll("\\s.*", ""))
                .middleName(juror.getFirstName().contains(" ") ?
                    juror.getFirstName().replaceAll(".*?\\s", "") : null)
                .lastName(juror.getLastName())
                .build())
            .build();
    }

}
