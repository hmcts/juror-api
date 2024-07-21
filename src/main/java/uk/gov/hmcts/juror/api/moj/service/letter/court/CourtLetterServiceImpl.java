package uk.gov.hmcts.juror.api.moj.service.letter.court;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.CertificateOfAttendanceLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.DeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.ExcusalLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.FailedToAttendLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.NonDeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.WithdrawalLetterData;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.CertificateOfAttendanceLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralDeniedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ShowCauseLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.WithdrawalLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCode;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.MojExcusalCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.CertificateOfAttendanceListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.DeferralDeniedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.DeferralGrantedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.ExcusalGrantedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.ShowCauseLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.WithdrawalLetterListRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
@Primary
public class CourtLetterServiceImpl implements CourtLetterService {

    private final CertificateOfAttendanceListRepository certificateOfAttendanceListRepository;
    private final DeferralGrantedLetterListRepository deferralGrantedLetterListRepository;
    private final ExcusalGrantedLetterListRepository excusalGrantedLetterListRepository;
    private final DeferralDeniedLetterListRepository deferralDeniedLetterListRepository;
    private final WithdrawalLetterListRepository withdrawalLetterListRepository;
    private final MojExcusalCodeRepository excusalCodeRepository;
    private final ShowCauseLetterListRepository showCauseLetterListRepository;

    private final CourtPostponementLetterServiceImpl courtPostponementLetterService;
    private final CourtExcusalRefusedLetterServiceImpl courtExcusalRefusedLetterService;
    private final CourtFailedToAttendLetterServiceImpl courtFailedToAttendLetterService;

    // String constants for response headings
    private static final String DATE_REFUSED = "Date refused";
    private static final String DEFERRED_TO = "Deferred to";
    private static final String DATE_EXCUSED = "Date excused";
    private static final String DATE_DISQUALIFIED = "Date disqualified";
    private static final String ABSENT_DATE = "Absent date";
    private static final String RECORDS_FOUND = "{} records found";

    @Override
    @Transactional(readOnly = true)
    public LetterListResponseDto getEligibleList(CourtLetterListRequestDto request) {

        CourtLetterType letterType = request.getLetterType();

        return switch (letterType) {
            case DEFERRAL_GRANTED -> getEligibleDeferralGrantedList(request);
            case DEFERRAL_REFUSED -> getEligibleDeferralDeniedList(request);
            case WITHDRAWAL -> getEligibleWithdrawalList(request);
            case POSTPONED -> courtPostponementLetterService.getEligibleList(request);
            case SHOW_CAUSE -> getEligibleShowCauseList(request);
            case EXCUSAL_GRANTED -> getEligibleExcusalGrantedList(request);
            case CERTIFICATE_OF_ATTENDANCE -> getEligibleCertificateOfAttendanceList(request);
            case EXCUSAL_REFUSED -> courtExcusalRefusedLetterService.getEligibleList(request);
            case FAILED_TO_ATTEND -> courtFailedToAttendLetterService.getEligibleList(request);
            default -> throw new MojException.InternalServerError("Letter type not yet implemented", null);
        };
    }

    private LetterListResponseDto getEligibleDeferralGrantedList(CourtLetterListRequestDto
                                                                     courtLetterListRequestDto) {
        log.trace("Enter getEligibleDeferralGrantedList");

        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(courtLetterListRequestDto);

        log.debug("Find jurors eligible for a court deferral letter for the primary court: {}", owner);
        List<DeferralGrantedLetterList> eligibleJurorRecords =
            deferralGrantedLetterListRepository.findJurorsEligibleForDeferralGrantedLetter(searchCriteria, owner);
        log.debug("{} records found", eligibleJurorRecords.size());

        List<String> headings = List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, POSTCODE, STATUS, DEFERRED_TO, REASON,
            DATE_PRINTED, POOL_NUMBER);

        List<String> dataTypes = List.of(STRING, STRING, STRING, STRING, STRING, DATE, STRING, DATE, HIDDEN);

        List<DeferralLetterData> deferralLetterDataList =
            serialiseDeferralLetterData(eligibleJurorRecords, courtLetterListRequestDto.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(deferralLetterDataList)
            .build();

        log.trace("Exit getEligibleDeferralGrantedList");
        return responseDto;
    }

    private LetterListResponseDto getEligibleCertificateOfAttendanceList(CourtLetterListRequestDto
                                                                             courtLetterListRequestDto) {
        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(courtLetterListRequestDto);

        log.debug("Find jurors eligible for a court certificate of attendance letter for the primary court: {}",
            owner);
        List<CertificateOfAttendanceLetterList> eligibleJurorRecords =
            certificateOfAttendanceListRepository.findJurorsEligibleForCertificateOfAcceptanceLetter(searchCriteria,
                owner);
        log.debug("{} records found", eligibleJurorRecords.size());

        List<String> headings =
            List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, POOL_NUMBER, START_DATE, COMPLETION_DATE,
                DATE_PRINTED);

        List<String> dataTypes =
            List.of(STRING, STRING, STRING, STRING, DATE, DATE, DATE); //

        List<CertificateOfAttendanceLetterData> certificateOfAttendanceLetterData =
            serialiseCertificateOfAttendanceLetterData(eligibleJurorRecords,
                courtLetterListRequestDto.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(certificateOfAttendanceLetterData)
            .build();

        log.trace("Exit getEligibleCertificateOfAttendanceList");
        return responseDto;

    }

    private LetterListResponseDto getEligibleDeferralDeniedList(CourtLetterListRequestDto courtLetterListRequestDto) {
        log.trace("Enter getEligibleDeferralDeniedList");

        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(courtLetterListRequestDto);

        log.debug("Find jurors eligible for a court deferral denied letter for the primary court: {}", owner);
        List<DeferralDeniedLetterList> eligibleJurorRecords =
            deferralDeniedLetterListRepository.findJurorsEligibleForDeferralDeniedLetter(searchCriteria, owner);
        log.debug("{} records found", eligibleJurorRecords.size());

        List<String> headings = List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, POSTCODE, STATUS, DATE_REFUSED, REASON,
            DATE_PRINTED, POOL_NUMBER);

        List<String> dataTypes = List.of(STRING, STRING, STRING, STRING, STRING, DATE, STRING, DATE, HIDDEN);

        List<NonDeferralLetterData> deferralLetterDataList =
            serialiseNonDeferralLetterData(eligibleJurorRecords, courtLetterListRequestDto.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(deferralLetterDataList)
            .build();

        log.trace("Exit getEligibleDeferralDeniedList");
        return responseDto;
    }

    private LetterListResponseDto getEligibleShowCauseList(CourtLetterListRequestDto request) {
        log.trace("Enter getEligibleShowCauseList method");

        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(request);

        log.debug("Find jurors eligible for a 'show case' letter for the primary court: {}", owner);
        List<ShowCauseLetterList> eligibleJurorRecords =
            showCauseLetterListRepository.findJurorsEligibleForShowCauseLetter(searchCriteria, owner);
        log.debug(RECORDS_FOUND, eligibleJurorRecords.size());

        List<String> headings = List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, ABSENT_DATE, DATE_PRINTED);

        List<String> dataTypes = List.of(STRING, STRING, STRING, DATE, DATE);

        List<FailedToAttendLetterData> showCauseLetterDataList =
            serialiseShowCauseLetterData(eligibleJurorRecords, request.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(showCauseLetterDataList)
            .build();

        log.trace("Exit getEligibleShowCauseList method");
        return responseDto;
    }

    private List<DeferralLetterData> serialiseDeferralLetterData(List<DeferralGrantedLetterList> eligibleJurorRecords,
                                                                 boolean isIncludePrinted) {
        List<DeferralLetterData> deferralLetterDataList = new ArrayList<>();
        for (DeferralGrantedLetterList result : eligibleJurorRecords) {

            DeferralLetterData.DeferralLetterDataBuilder deferralLetterData = DeferralLetterData.builder()
                .jurorNumber(result.getJurorNumber())
                .firstName(result.getFirstName())
                .lastName(result.getLastName())
                .postcode(result.getPostcode())
                .status(result.getStatus())
                .deferredTo(result.getDeferralDate())
                .reason(WordUtils.capitalizeFully(result.getDeferralReason()))
                .poolNumber(result.getPoolNumber());

            if (isIncludePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                deferralLetterData.datePrinted(datePrinted != null
                    ? datePrinted.toLocalDate()
                    : null);
            }

            deferralLetterDataList.add(deferralLetterData.build());
        }
        return deferralLetterDataList;
    }

    private List<NonDeferralLetterData> serialiseNonDeferralLetterData(
        List<DeferralDeniedLetterList> eligibleJurorRecords, boolean isIncludePrinted) {

        List<NonDeferralLetterData> deferralLetterDataList = new ArrayList<>();
        for (DeferralDeniedLetterList result : eligibleJurorRecords) {

            String deferralReason = getDeferralReasonDescription(result.getOtherInformation());

            NonDeferralLetterData.NonDeferralLetterDataBuilder nonDeferralLetterData =
                NonDeferralLetterData.builder()
                    .jurorNumber(result.getJurorNumber())
                    .firstName(result.getFirstName())
                    .lastName(result.getLastName())
                    .postcode(result.getPostcode())
                    .status(result.getStatus())
                    .dateRefused(result.getRefusalDate())
                    .reason(deferralReason)
                    .poolNumber(result.getPoolNumber());

            if (isIncludePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                nonDeferralLetterData.datePrinted(datePrinted != null
                    ? datePrinted.toLocalDate()
                    : null);
            }

            deferralLetterDataList.add(nonDeferralLetterData.build());
        }
        return deferralLetterDataList;
    }

    private List<FailedToAttendLetterData> serialiseShowCauseLetterData(List<ShowCauseLetterList> eligibleJurorRecords,
                                                                        boolean isIncludePrinted) {
        List<FailedToAttendLetterData> showCauseLetterDataList = new ArrayList<>();
        for (ShowCauseLetterList result : eligibleJurorRecords) {
            FailedToAttendLetterData.FailedToAttendLetterDataBuilder showCauseLetterData =
                FailedToAttendLetterData.builder()
                    .jurorNumber(result.getJurorNumber())
                    .firstName(result.getFirstName())
                    .lastName(result.getLastName())
                    .absentDate(result.getAbsentDate())
                    .poolNumber(result.getPoolNumber());

            if (isIncludePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                showCauseLetterData.datePrinted(datePrinted != null ? datePrinted.toLocalDate() : null);
            }

            showCauseLetterDataList.add(showCauseLetterData.build());
        }
        return showCauseLetterDataList;
    }

    private LetterListResponseDto getEligibleWithdrawalList(CourtLetterListRequestDto courtLetterListRequestDto) {
        log.trace("Enter getEligibleWithdrawalList");

        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(courtLetterListRequestDto);

        log.debug("Find jurors eligible for a court withdrawal letter for the primary court: {}", owner);
        List<WithdrawalLetterList> eligibleJurorRecords =
            withdrawalLetterListRepository.findJurorsEligibleForWithdrawalLetter(searchCriteria, owner);
        log.debug("{} records found", eligibleJurorRecords.size());

        List<String> headings =
            List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, POSTCODE, STATUS, DATE_DISQUALIFIED, REASON,
                DATE_PRINTED, POOL_NUMBER);

        List<String> dataTypes = List.of(STRING, STRING, STRING, STRING, STRING, DATE, STRING, DATE, HIDDEN);

        List<WithdrawalLetterData> withdrawalLetterDataList =
            serialiseWithdrawalLetterData(eligibleJurorRecords, courtLetterListRequestDto.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(withdrawalLetterDataList)
            .build();

        log.trace("Exit getEligibleWithdrawalGrantedList");
        return responseDto;

    }

    private List<WithdrawalLetterData> serialiseWithdrawalLetterData(List<WithdrawalLetterList> eligibleJurorRecords,
                                                                     boolean isIncludePrinted) {
        List<WithdrawalLetterData> withdrawalLetterDataList = new ArrayList<>();
        for (WithdrawalLetterList result : eligibleJurorRecords) {

            WithdrawalLetterData.WithdrawalLetterDataBuilder withdrawalLetterData = WithdrawalLetterData.builder()
                .jurorNumber(result.getJurorNumber())
                .firstName(result.getFirstName())
                .lastName(result.getLastName())
                .postcode(result.getPostcode())
                .status(result.getStatus())
                .dateDisqualified(result.getDateDisqualified())
                .reason(WordUtils.capitalizeFully(DisqualifyCode.getDisqualifyCode(
                    result.getDisqualifiedCode()).getDescription()))
                .poolNumber(result.getPoolNumber());

            if (isIncludePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                withdrawalLetterData.datePrinted(datePrinted != null
                    ? datePrinted.toLocalDate()
                    : null);
            }

            withdrawalLetterDataList.add(withdrawalLetterData.build());
        }
        return withdrawalLetterDataList;
    }

    private String getDeferralReasonDescription(String otherInfoText) {

        // deferral reason code will always be the last character in the other information text for the deferral
        // denied  status history event
        String deferralReasonCode = otherInfoText.substring(otherInfoText.length() - 1).toUpperCase();
        log.debug("Extracted deferral reason code: {} from other information text: {}", deferralReasonCode,
            otherInfoText);

        Optional<ExcusalCode> excusalCodeOpt = excusalCodeRepository.findById(deferralReasonCode);

        return excusalCodeOpt.map(excusalCode -> WordUtils.capitalizeFully(excusalCode.getDescription()))
            .orElse(null);
    }

    private LetterListResponseDto getEligibleExcusalGrantedList(CourtLetterListRequestDto courtLetterListRequestDto) {
        log.trace("Enter getEligibleExcusalGrantedList");

        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(courtLetterListRequestDto);

        log.debug("Find jurors eligible for a court excusal granted letter for the primary court: {}", owner);
        List<ExcusalGrantedLetterList> eligibleJurorRecords =
            excusalGrantedLetterListRepository.findJurorsEligibleForExcusalGrantedLetter(searchCriteria, owner);
        log.debug("{} records found", eligibleJurorRecords.size());

        List<String> headings = List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, POSTCODE, STATUS, DATE_EXCUSED, REASON,
            DATE_PRINTED, POOL_NUMBER);

        List<String> dataTypes = List.of(STRING, STRING, STRING, STRING, STRING, DATE, STRING, DATE, HIDDEN);

        List<ExcusalLetterData> excusalLetterDataList =
            serialiseExcusalGrantedLetterData(eligibleJurorRecords, courtLetterListRequestDto.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(excusalLetterDataList)
            .build();

        log.trace("Exit getEligibleExcusalGrantedList");
        return responseDto;
    }

    private List<ExcusalLetterData> serialiseExcusalGrantedLetterData(
        List<ExcusalGrantedLetterList> eligibleJurorRecords,
        boolean isIncludePrinted) {
        List<ExcusalLetterData> excusalLetterDataList = new ArrayList<>();
        for (ExcusalGrantedLetterList result : eligibleJurorRecords) {

            ExcusalLetterData.ExcusalLetterDataBuilder excusalLetterData = ExcusalLetterData.builder()
                .jurorNumber(result.getJurorNumber())
                .firstName(result.getFirstName())
                .lastName(result.getLastName())
                .postcode(result.getPostcode())
                .status(result.getStatus())
                .dateExcused(result.getDateExcused())
                .reason(WordUtils.capitalizeFully(result.getReason()))
                .poolNumber(result.getPoolNumber());

            if (isIncludePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                excusalLetterData.datePrinted(datePrinted != null
                    ? datePrinted.toLocalDate()
                    : null);
            }
            excusalLetterDataList.add(excusalLetterData.build());
        }
        return excusalLetterDataList;
    }

    private List<CertificateOfAttendanceLetterData> serialiseCertificateOfAttendanceLetterData(
        List<CertificateOfAttendanceLetterList> eligibleJurorRecords,
        boolean isIncludePrinted) {
        List<CertificateOfAttendanceLetterData> dataList = new ArrayList<>();
        for (CertificateOfAttendanceLetterList result : eligibleJurorRecords) {

            CertificateOfAttendanceLetterData.CertificateOfAttendanceLetterDataBuilder
                certificateOfAttendanceLetterDataBuilder =
                CertificateOfAttendanceLetterData.builder()
                    .jurorNumber(result.getJurorNumber())
                    .firstName(result.getFirstName())
                    .lastName(result.getLastName())
                    .poolNumber(result.getPoolNumber())
                    .startDate(result.getStartDate())
                    .completionDate(result.getCompletionDate());


            if (isIncludePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                certificateOfAttendanceLetterDataBuilder.datePrinted(datePrinted != null
                    ? datePrinted.toLocalDate()
                    : null);
            }

            dataList.add(certificateOfAttendanceLetterDataBuilder.build());
        }
        return dataList;
    }
}



