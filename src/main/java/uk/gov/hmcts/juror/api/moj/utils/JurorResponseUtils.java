package uk.gov.hmcts.juror.api.moj.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public final class JurorResponseUtils {
    private JurorResponseUtils() {
        // an empty private constructor
    }

    public static JurorResponse getDigitalResponseForJurorDisqualification(
        String jurorNumber,
        JurorResponseRepository jurorResponseRepository) {
        log.trace("Juror {}: Enter getDigitalResponseForJuror", jurorNumber);
        Optional<JurorResponse> jurorResponse = jurorResponseRepository.findById(jurorNumber);

        if (!jurorResponse.isPresent()) {
            String customErrorMessage = "Juror {}: Cannot find associated response record for " + jurorNumber;
            throw new MojException.NotFound(customErrorMessage, null);
        }

        log.trace("Juror {}: Exit getDigitalResponseForJuror", jurorNumber);
        return jurorResponse.get();
    }

    /**
     * Creates a minimal paper response for age disqualification in cases where no response has
     * been commenced.
     *
     * @param juror               A juror object
     * @param disqualifiedComment Disqualified comment
     * @return Return a paper response object
     */
    public static PaperResponse createMinimalPaperSummonsRecord(Juror juror,
                                                                String disqualifiedComment) {
        PaperResponse jurorPaperResponse = new PaperResponse();
        jurorPaperResponse.setJurorNumber(juror.getJurorNumber());
        // setting the received date to now
        jurorPaperResponse.setDateReceived(LocalDateTime.now());
        // set up Juror personal details
        jurorPaperResponse.setTitle(juror.getTitle());
        jurorPaperResponse.setFirstName(juror.getFirstName());
        jurorPaperResponse.setLastName(juror.getLastName());
        jurorPaperResponse.setDateOfBirth(juror.getDateOfBirth());

        //set address
        setUpAddress(jurorPaperResponse, juror);
        jurorPaperResponse.setThirdPartyReason(disqualifiedComment);
        jurorPaperResponse.setProcessingStatus(ProcessingStatus.CLOSED);
        jurorPaperResponse.setProcessingComplete(true);
        jurorPaperResponse.setCompletedAt(LocalDateTime.now());
        return jurorPaperResponse;
    }

    private static void setUpAddress(PaperResponse jurorPaperResponse,
                                     Juror juror) {
        jurorPaperResponse.setAddressLine1(juror.getAddressLine1());
        jurorPaperResponse.setAddressLine2(juror.getAddressLine2());
        jurorPaperResponse.setAddressLine3(juror.getAddressLine3());
        jurorPaperResponse.setAddressLine4(juror.getAddressLine4());
        jurorPaperResponse.setAddressLine5(juror.getAddressLine5());
        jurorPaperResponse.setPostcode(juror.getPostcode());
    }
}