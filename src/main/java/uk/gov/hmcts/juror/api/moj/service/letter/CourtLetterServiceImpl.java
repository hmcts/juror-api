package uk.gov.hmcts.juror.api.moj.service.letter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.DeferralLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.NonDeferralLetterData;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralDeniedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralGrantedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.DeferralDeniedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.DeferralGrantedLetterListRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtLetterServiceImpl implements CourtLetterService {

    private final DeferralGrantedLetterListRepository deferralGrantedLetterListRepository;
    private final DeferralDeniedLetterListRepository deferralDeniedLetterListRepository;
    private final ExcusalCodeRepository excusalCodeRepository;

    // String constants for response headings
    private static final String JUROR_NUMBER = "Juror number";
    private static final String FIRST_NAME = "First name";
    private static final String LAST_NAME = "Last name";
    private static final String POSTCODE = "Postcode";
    private static final String STATUS = "Status";
    private static final String DEFERRED_TO = "Deferred to";
    private static final String DATE_REFUSED = "Date refused";
    private static final String REASON = "Reason";
    private static final String DATE_PRINTED = "Date printed";
    private static final String POOL_NUMBER = "Pool number";

    // String constants for data type descriptions
    private static final String STRING = "string";
    private static final String DATE = "date";
    private static final String HIDDEN = "hidden";

    @Override
    @Transactional(readOnly = true)
    public LetterListResponseDto getEligibleList(CourtLetterListRequestDto courtLetterListRequestDto) {

        CourtLetterType letterType = courtLetterListRequestDto.getLetterType();

        return switch (letterType) {
            case DEFERRAL_GRANTED -> getEligibleDeferralGrantedList(courtLetterListRequestDto);
            case DEFERRAL_REFUSED -> getEligibleDeferralDeniedList(courtLetterListRequestDto);
            default -> throw new MojException.InternalServerError("Letter type not yet implemented", null);
        };

    }

    private LetterListResponseDto getEligibleDeferralGrantedList(CourtLetterListRequestDto courtLetterListRequestDto) {
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


    private CourtLetterSearchCriteria buildSearchCriteria(CourtLetterListRequestDto courtLetterListRequestDto) {
        return CourtLetterSearchCriteria.builder()
            .jurorNumber(courtLetterListRequestDto.getJurorNumber())
            .jurorName(courtLetterListRequestDto.getJurorName())
            .postcode(courtLetterListRequestDto.getJurorPostcode())
            .poolNumber(courtLetterListRequestDto.getPoolNumber())
            .includePrinted(courtLetterListRequestDto.isIncludePrinted())
            .build();
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
                deferralLetterData.datePrinted(datePrinted != null ? datePrinted.toLocalDate() : null);
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

            NonDeferralLetterData.NonDeferralLetterDataBuilder nonDeferralLetterData = NonDeferralLetterData.builder()
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
                nonDeferralLetterData.datePrinted(datePrinted != null ? datePrinted.toLocalDate() : null);
            }

            deferralLetterDataList.add(nonDeferralLetterData.build());
        }
        return deferralLetterDataList;
    }

    private String getDeferralReasonDescription(String otherInfoText) {

        // deferral reason code will always be the last character in the other information text for the deferral
        // denied  status history event
        String deferralReasonCode = otherInfoText.substring(otherInfoText.length() - 1).toUpperCase();
        log.debug("Extracted deferral reason code: {} from other information text: {}", deferralReasonCode,
            otherInfoText);

        Optional<ExcusalCodeEntity> excusalCodeOpt = excusalCodeRepository.findById(deferralReasonCode);

        return excusalCodeOpt.map(excusalCodeEntity -> WordUtils.capitalizeFully(excusalCodeEntity.getDescription()))
            .orElse(null);
    }

}
