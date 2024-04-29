package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.MarkAsDeceasedDto;
import uk.gov.hmcts.juror.api.moj.domain.ContactCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeceasedResponseServiceImpl implements DeceasedResponseService {

    private static final String DECEASED_CODE = "D";
    private static final String PAPER_RESPONSE_EXISTS_TEXT = "A Paper summons reply exists.";

    @NonNull
    private final ContactCodeRepository contactCodeRepository;
    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    @NonNull
    private final JurorPoolRepository jurorPoolRepository;
    @NonNull
    private final JurorRepository jurorRepository;
    @NonNull
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @NonNull
    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    @NonNull
    private final ContactLogRepository contactLogRepository;

    @Transactional
    @Override
    public void markAsDeceased(BureauJwtPayload payload, MarkAsDeceasedDto markAsDeceasedDto) {

        final String jurorNumber = markAsDeceasedDto.getJurorNumber();
        final String owner = payload.getOwner();
        final String login = payload.getLogin();

        log.debug("Begin processing mark as deceased for juror {} by user {}", jurorNumber, payload.getLogin());

        // update juror record for each active entry
        JurorPool jurorPool = JurorPoolUtils.getActiveJurorPoolForUser(jurorPoolRepository, jurorNumber, owner);
        JurorPoolUtils.checkOwnershipForCurrentUser(jurorPool, owner);

        Juror juror = jurorPool.getJuror();

        updateJuror(login, jurorPool, juror);
        createJurorHistory(jurorNumber, login, jurorPool);

        String deceasedComment = markAsDeceasedDto.getDeceasedComment();

        if (Boolean.TRUE.equals(markAsDeceasedDto.getPaperResponseExists())) {
            // get the comment for the juror and append with the text PAPER_RESPONSE_EXISTS_TEXT
            deceasedComment = deceasedComment + " \n" + PAPER_RESPONSE_EXISTS_TEXT;

            // create a simple paper summons record with minimal info for the juror with completed status
            createMinimalPaperSummonsRecord(juror, deceasedComment);
        } else {
            // Update any response record to closed to prevent further processing

            // get the digital response
            AbstractJurorResponse jurorResponse = jurorDigitalResponseRepository.findByJurorNumber(jurorNumber);

            if (jurorResponse != null) {
                setResponseToClosed(jurorResponse);
                jurorDigitalResponseRepository.save((DigitalResponse)jurorResponse);
            } else {
                jurorResponse = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
                if (jurorResponse != null) {
                    setResponseToClosed(jurorResponse);
                    jurorPaperResponseRepository.save((PaperResponse) jurorResponse);
                }
            }
        }

        createContactLog(payload, jurorNumber, deceasedComment);
        jurorRepository.save(juror);
        jurorPoolRepository.save(jurorPool);
        log.info("Processed juror {} as deceased", jurorNumber);
    }

    private static void updateJuror(String login, JurorPool jurorPool, Juror juror) {
        juror.setResponded(true);
        juror.setExcusalDate(LocalDate.now(ZoneId.systemDefault()));
        juror.setExcusalCode(DECEASED_CODE);
        juror.setUserEdtq(login);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.EXCUSED);
        jurorPool.setStatus(jurorStatus);

        jurorPool.setNextDate(null);
    }

    private void createJurorHistory(String jurorNumber, String login, JurorPool jurorPool) {
        // audit pool member
        JurorHistory history = JurorHistory.builder()
            .jurorNumber(jurorNumber)
            .createdBy(login)
            .dateCreated(LocalDateTime.now())
            .poolNumber(jurorPool.getPoolNumber())
            .historyCode(HistoryCodeMod.EXCUSE_POOL_MEMBER)
            .otherInformation("Deceased")
            .build();
        jurorHistoryRepository.save(history);
    }

    private void createContactLog(BureauJwtPayload payload, String jurorNumber, String deceasedComment) {
        ContactCode enquiryType = RepositoryUtils.retrieveFromDatabase(
            IContactCode.UNABLE_TO_ATTEND.getCode(), contactCodeRepository);

        ContactLog contactLog = ContactLog.builder()
            .username(payload.getLogin())
            .jurorNumber(jurorNumber)
            .startCall(LocalDateTime.now())
            .enquiryType(enquiryType)
            .notes(deceasedComment)
            .repeatEnquiry(false)
            .build();

        contactLogRepository.saveAndFlush(contactLog);
    }

    private static void setResponseToClosed(AbstractJurorResponse jurorResponse) {
        jurorResponse.setProcessingStatus(ProcessingStatus.CLOSED);
        jurorResponse.setProcessingComplete(true);
        jurorResponse.setCompletedAt(LocalDateTime.now());
    }

    private void createMinimalPaperSummonsRecord(Juror juror, String deceasedComment) {
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

        jurorPaperResponse.setThirdPartyReason(deceasedComment);

        jurorPaperResponse.setProcessingComplete(true);
        jurorPaperResponse.setProcessingStatus(ProcessingStatus.CLOSED);
        jurorPaperResponse.setCompletedAt(LocalDateTime.now());

        jurorPaperResponseRepository.save(jurorPaperResponse);
    }

    private void setUpAddress(PaperResponse jurorPaperResponse, Juror juror) {
        jurorPaperResponse.setAddressLine1(juror.getAddressLine1());
        jurorPaperResponse.setAddressLine2(juror.getAddressLine2());
        jurorPaperResponse.setAddressLine3(juror.getAddressLine3());
        jurorPaperResponse.setAddressLine4(juror.getAddressLine4());
        jurorPaperResponse.setAddressLine5(juror.getAddressLine5());
        jurorPaperResponse.setPostcode(juror.getPostcode());
    }
}

