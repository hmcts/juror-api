package uk.gov.hmcts.juror.api.moj.controller;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.letter.LetterId;
import uk.gov.hmcts.juror.api.moj.domain.letter.RequestLetter;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.letter.RequestLetterRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the API endpoints defined in LetterController.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LetterControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Autowired
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Autowired
    private RequestLetterRepository requestLetterRepository;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationBureauPaperUserHappyPath() throws Exception {
        final String jurorNumber = "222222222";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Part 2 Section D, Part 2 Section A";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        assertThat(requestLetterOpt.isPresent()).isTrue();
        assertThat(requestLetterOpt.get().getRequiredInformation())
            .as("Expect the request letter string to be queued for all info")
            .isEqualTo(expectedRequestString);

        verifyPaperResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationCourtUser_forbidden() throws Exception {
        final String jurorNumber = "222222222";
        final String courtOwner = "415";
        final String courtJwt = createBureauJwt("COURT_USER", courtOwner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        verifyPaperResponse(jurorNumber, ProcessingStatus.TODO);
    }

    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationBureauUserPaper_AllCategoriesMissing() throws Exception {
        final String jurorNumber = "222222222";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Part 1 Date of Birth, Part 1 Telephone No., Part 2 Section B, "
            + "Part 2 Section C, Part 2 Section C, Part 2 Section E, Part 2 Section A, Part 3 Section A/B/C, Part 2 "
            + "Section D, Part 4";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.DATE_OF_BIRTH,
                MissingInformation.TELEPHONE_NO,
                MissingInformation.CONVICTIONS,
                MissingInformation.MENTAL_CAPACITY_ACT,
                MissingInformation.MENTAL_HEALTH_ACT,
                MissingInformation.RESIDENCY,
                MissingInformation.BAIL,
                MissingInformation.SERVE_ON_DATE,
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.DISABILITY_OR_IMPAIRMENT
            ))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        if (requestLetterOpt.isPresent()) {
            assertThat(requestLetterOpt.get().getRequiredInformation())
                .as("Expect the request letter string to be queued for all info")
                .isEqualTo(expectedRequestString);
        }

        verifyPaperResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationBureauUserPaper_AllCategoriesMissingWelsh() throws Exception {
        final String jurorNumber = "222222223";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Rhan 1 Dyddiad Geni, Rhan 1 Rhif FFon., Rhan 2 Adran B, "
            + "Rhan 2 Adran C, Rhan 2 Adran C, Rhan 2 Adran E, Rhan 2 Adran A, Rhan 3 Adran A/B/C, Rhan 2 Adran D, "
            + "Rhan"
            + " 4";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(Arrays.asList(
                MissingInformation.DATE_OF_BIRTH,
                MissingInformation.TELEPHONE_NO,
                MissingInformation.CONVICTIONS,
                MissingInformation.MENTAL_CAPACITY_ACT,
                MissingInformation.MENTAL_HEALTH_ACT,
                MissingInformation.RESIDENCY,
                MissingInformation.BAIL,
                MissingInformation.SERVE_ON_DATE,
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.DISABILITY_OR_IMPAIRMENT
            ))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        if (requestLetterOpt.isPresent()) {
            assertThat(requestLetterOpt.get().getRequiredInformation())
                .as("Expect the request letter string to be queued for all info")
                .isEqualTo(expectedRequestString);
        }

        verifyPaperResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationBureauUserPaper_EmptyMissingInformation() throws Exception {
        final String jurorNumber = "222222222";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(new ArrayList<>())
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
            .as("Expect HTTP Response to be BAD_REQUEST, cannot process when no missing info")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyPaperResponse(jurorNumber, ProcessingStatus.TODO);
    }

    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationBureauUserPaper_MissingSignature() throws Exception {
        final String jurorNumber = "222222222";
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/letter/request-information");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber("222222222")
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(List.of(
                MissingInformation.SIGNATURE))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
            .as("Expect HTTP Response to be BAD_REQUEST, cannot process when Signature is missing")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyPaperResponse(jurorNumber, ProcessingStatus.TODO);
    }


    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationBureauUserDigital_HappyPath() throws Exception {
        final String jurorNumber = "111111000";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Part 2 Section D, Part 2 Section A";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.DIGITAL)
            .missingInformation(Arrays.asList(
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.BAIL))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        requestLetterOpt.ifPresent(requestLetter -> assertThat(requestLetter.getRequiredInformation())
            .as("Expect the request letter string to be queued for all info")
            .isEqualTo(expectedRequestString));

        verifyDigitalResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }


    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationBureauUserDigital_AllCategoriesMissing() throws Exception {
        final String jurorNumber = "111111000";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Part 1 Date of Birth, Part 1 Telephone No., Part 2 Section B, "
            + "Part 2 Section C, Part 2 Section C, Part 2 Section E, Part 2 Section A, Part 3 Section A/B/C, Part 2 "
            + "Section D, Part 4";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.DIGITAL)
            .missingInformation(Arrays.asList(
                MissingInformation.DATE_OF_BIRTH,
                MissingInformation.TELEPHONE_NO,
                MissingInformation.CONVICTIONS,
                MissingInformation.MENTAL_CAPACITY_ACT,
                MissingInformation.MENTAL_HEALTH_ACT,
                MissingInformation.RESIDENCY,
                MissingInformation.BAIL,
                MissingInformation.SERVE_ON_DATE,
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.DISABILITY_OR_IMPAIRMENT
            ))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        if (requestLetterOpt.isPresent()) {
            assertThat(requestLetterOpt.get().getRequiredInformation())
                .as("Expect the request letter string to be queued for all info")
                .isEqualTo(expectedRequestString);
        }

        verifyDigitalResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Sql({"/db/mod/truncate.sql","/db/LetterController_initPoolMemberAndResponse.sql"})
    @Test
    public void requestInformationBureauUserDigital_AllCategoriesMissingWelsh() throws Exception {
        final String jurorNumber = "111111001";
        final String owner = "400";
        final String bureauJwt = createBureauJwt("BUREAU_USER", owner);
        final URI uri = URI.create("/api/v1/moj/letter/request-information");
        final String expectedRequestString = "Rhan 1 Dyddiad Geni, Rhan 1 Rhif FFon., Rhan 2 Adran B, "
            + "Rhan 2 Adran C, Rhan 2 Adran C, Rhan 2 Adran E, Rhan 2 Adran A, Rhan 3 Adran A/B/C, Rhan 2 Adran D, "
            + "Rhan" + " 4";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.DIGITAL)
            .missingInformation(Arrays.asList(
                MissingInformation.DATE_OF_BIRTH,
                MissingInformation.TELEPHONE_NO,
                MissingInformation.CONVICTIONS,
                MissingInformation.MENTAL_CAPACITY_ACT,
                MissingInformation.MENTAL_HEALTH_ACT,
                MissingInformation.RESIDENCY,
                MissingInformation.BAIL,
                MissingInformation.SERVE_ON_DATE,
                MissingInformation.CJS_EMPLOYMENT_5_YEARS,
                MissingInformation.DISABILITY_OR_IMPAIRMENT
            ))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Request Letter queued for juror number " + jurorNumber);

        // try to read back the saved request letter entry, there could be a race problem in saving/reading
        LetterId letterId = new LetterId(owner, jurorNumber);
        Optional<RequestLetter> requestLetterOpt = requestLetterRepository.findById(letterId);
        if (requestLetterOpt.isPresent()) {
            assertThat(requestLetterOpt.get().getRequiredInformation())
                .as("Expect the request letter string to be queued for all info")
                .isEqualTo(expectedRequestString);
        }

        verifyDigitalResponse(jurorNumber, ProcessingStatus.AWAITING_CONTACT);
    }

    @Test
    public void requestInformationBureauUserPaper_MissingReplyMethod() throws Exception {
        final String jurorNumber = "222222222";
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/letter/request-information");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<AdditionalInformationDto> request = new RequestEntity<>(AdditionalInformationDto.builder()
            .jurorNumber("222222222")
            .missingInformation(List.of(
                MissingInformation.BAIL))
            .build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(request, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode())
            .as("Expect HTTP Response to be BAD_REQUEST, cannot process when Reply Method is unspecified")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyPaperResponse(jurorNumber, ProcessingStatus.TODO);
    }



    private void verifyPaperResponse(String jurorNumber, ProcessingStatus status) {
        PaperResponse paperResponse =
            jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        assert paperResponse != null;
        Assertions.assertThat(paperResponse.getProcessingStatus()).isEqualTo(status);
    }

    private void verifyDigitalResponse(String jurorNumber, ProcessingStatus status) {
        DigitalResponse response = jurorResponseRepository.findByJurorNumber(jurorNumber);
        assertThat(response).isNotNull();
        Assertions.assertThat(response.getProcessingStatus()).isEqualTo(status);
    }
}