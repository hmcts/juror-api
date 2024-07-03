package uk.gov.hmcts.juror.api.moj.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.response.jurorresponse.IJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Slf4j
public final class JurorResponseUtils {
    private JurorResponseUtils() {
        // an empty private constructor
    }


    public static void updateCurrentOwnerInResponseDto(JurorPoolRepository jurorPoolRepository,
                                                       IJurorResponse responseDto) {

        // set the current owner.  Need to ensure the current owner is returned as the owner can change if, for
        // example, the juror is transferred to a different pool
        List<JurorPool> jurorPools =
            JurorPoolUtils.getActiveJurorPoolRecords(jurorPoolRepository, responseDto.getJurorNumber());

        Optional<JurorPool> jurorPool = jurorPools.stream()
            .filter(not(jp -> jp.getStatus().getCode().equals(IJurorStatus.TRANSFERRED)))
            .sorted(Comparator.comparing(JurorPool::getDateCreated).reversed())
            .toList().stream().findFirst();

        jurorPool.ifPresent(pool -> responseDto.setCurrentOwner(pool.getOwner()));
    }

    /**
     * Creates a minimal paper response for age disqualification in cases where no response has
     * been commenced.
     *
     * @param juror               A juror object
     * @param disqualifiedComment Disqualified comment
     * @return Return a paper response object
     */
    public static PaperResponse createMinimalPaperSummonsRecord(
        JurorPaperResponseRepositoryMod jurorPaperResponseRepository,
        JurorResponseAuditRepositoryMod jurorResponseAuditRepositoryMod,
        Juror juror, String disqualifiedComment) {
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
        JurorResponseUtils.setUpAddress(jurorPaperResponse, juror);
        jurorPaperResponse.setThirdPartyReason(disqualifiedComment);
        jurorPaperResponse.setProcessingComplete(true);
        jurorPaperResponse.setCompletedAt(LocalDateTime.now());
        jurorPaperResponse = jurorPaperResponseRepository.save(jurorPaperResponse);
        jurorPaperResponse.setProcessingStatus(jurorResponseAuditRepositoryMod, ProcessingStatus.CLOSED);
        return jurorPaperResponseRepository.save(jurorPaperResponse);
    }

    private static void setUpAddress(PaperResponse jurorPaperResponse, Juror juror) {
        jurorPaperResponse.setAddressLine1(juror.getAddressLine1());
        jurorPaperResponse.setAddressLine2(juror.getAddressLine2());
        jurorPaperResponse.setAddressLine3(juror.getAddressLine3());
        jurorPaperResponse.setAddressLine4(juror.getAddressLine4());
        jurorPaperResponse.setAddressLine5(juror.getAddressLine5());
        jurorPaperResponse.setPostcode(juror.getPostcode());
    }
}