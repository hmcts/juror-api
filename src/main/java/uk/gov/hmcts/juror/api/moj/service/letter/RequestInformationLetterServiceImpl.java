package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.letter.RequestLetter;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.letter.RequestLetterRepository;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyStatusUpdateService;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;

import java.util.List;

/**
 * Service methods related to request information letters.
 */
@Slf4j
@Service
public class RequestInformationLetterServiceImpl extends LetterServiceImpl<RequestLetter, RequestLetterRepository>
    implements RequestInformationLetterService {

    @Autowired
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Autowired
    private final JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Autowired
    private final JurorPoolRepository jurorPoolRepository;
    @Autowired
    private final SummonsReplyStatusUpdateService summonsReplyStatusUpdateService;

    public RequestInformationLetterServiceImpl(
        @NonNull final RequestLetterRepository letterRepository,
        @NonNull final JurorPaperResponseRepositoryMod jurorPaperResponseRepository,
        @NonNull final JurorPoolRepository jurorPoolRepository,
        @NonNull final JurorDigitalResponseRepositoryMod jurorResponseRepository,
        @NonNull final SummonsReplyStatusUpdateService summonsReplyStatusUpdateService) {
        super(letterRepository);
        this.jurorPaperResponseRepository = jurorPaperResponseRepository;
        this.jurorPoolRepository = jurorPoolRepository;
        this.jurorResponseRepository = jurorResponseRepository;
        this.summonsReplyStatusUpdateService = summonsReplyStatusUpdateService;
    }

    @Override
    public RequestLetter createNewLetter(String owner, String jurorNumber) {
        RequestLetter requestLetter = new RequestLetter();
        requestLetter.setOwner(owner);
        requestLetter.setJurorNumber(jurorNumber);
        return requestLetter;
    }

    @Override
    @Transactional
    public void requestInformation(BureauJWTPayload payload, AdditionalInformationDto additionalInformationDto) {

        final String jurorNumber = additionalInformationDto.getJurorNumber();
        final String owner = payload.getOwner();
        log.trace("Preparing to queue Request Letter for juror {}", jurorNumber);

        final ReplyMethod replyMethod = additionalInformationDto.getReplyMethod();

        if (replyMethod.equals(ReplyMethod.PAPER)) {
            //Basic validation to ensure additional information can be requested for paper responses
            checkIfSignatureMissing(additionalInformationDto.getMissingInformation(), jurorNumber);
        }

        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber, owner);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

        //Basic validation passed, now map the information required and set the data for the Request Letter
        Juror juror = jurorPool.getJuror();

        boolean welshMapping = juror.getWelsh() != null && !juror.getWelsh().equals(Boolean.FALSE);
        String missingInformation =
            MissingInformation.buildMissingInformationString(additionalInformationDto.getMissingInformation(),
                welshMapping);

        RequestLetter letterToEnqueue = getLetterToEnqueue(owner, jurorNumber);
        letterToEnqueue.setRequiredInformation(missingInformation);

        //Mapping complete, now queue the Letter
        enqueueLetter(letterToEnqueue);

        // update the response status
        if (replyMethod.equals(ReplyMethod.PAPER)) {
            summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber,
                ProcessingStatus.AWAITING_CONTACT, payload);
        } else {
            summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber,
                ProcessingStatus.AWAITING_CONTACT, payload);
        }

        log.trace("Finished queueing request letter for juror {}", jurorNumber);
    }

    private void checkIfSignatureMissing(List<MissingInformation> missingInformation,
                                         String jurorNumber) {
        if (missingInformation.contains(MissingInformation.SIGNATURE)) {
            String customErrorMessage = String.format(
                "Cannot request additional information via the bulk print process for juror number %s "
                    + "because the signature is missing",
                jurorNumber
            );
            throw new MojException.BadRequest(customErrorMessage, null);
        }
    }
}

