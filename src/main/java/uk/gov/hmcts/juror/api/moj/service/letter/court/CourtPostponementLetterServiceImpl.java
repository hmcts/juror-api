package uk.gov.hmcts.juror.api.moj.service.letter.court;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PostponeLetterData;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.PostponedLetterList;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.PostponementLetterListRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtPostponementLetterServiceImpl implements CourtLetterService {

    // String constants for response headings
    private static final String POSTPONED_TO = "Postponed to";
    private static final String POSTPONED_STATUS = "Postponed";

    @NotNull
    private final PostponementLetterListRepository postponementLetterListRepository;

    @Override
    @Transactional(readOnly = true)
    public LetterListResponseDto getEligibleList(CourtLetterListRequestDto request) {
        log.trace("Enter getEligiblePostponedList");

        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(request);

        log.debug("Find jurors eligible for a court postpone letter for the primary court: {}", owner);
        List<PostponedLetterList> eligibleJurorRecords =
            postponementLetterListRepository.findJurorsEligibleForPostponementLetter(searchCriteria, owner);
        log.debug("{} records found", eligibleJurorRecords.size());

        List<String> headings = List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, POSTCODE, STATUS, POSTPONED_TO, REASON,
            DATE_PRINTED, POOL_NUMBER);

        List<String> dataTypes = List.of(STRING, STRING, STRING, STRING, STRING, DATE, STRING, DATE, HIDDEN);

        List<PostponeLetterData> postponeLetterDataList =
            serialisePostponementLetterData(eligibleJurorRecords, request.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(postponeLetterDataList)
            .build();

        log.trace("Exit getEligibleList (Postponement letter");
        return responseDto;
    }

    private List<PostponeLetterData> serialisePostponementLetterData(List<PostponedLetterList> eligibleJurorRecords,
                                                                     boolean isIncludePrinted) {
        List<PostponeLetterData> postponeLetterDataList = new ArrayList<>();
        for (PostponedLetterList result : eligibleJurorRecords) {
            PostponeLetterData.PostponeLetterDataBuilder postponeLetterData = PostponeLetterData.builder()
                .jurorNumber(result.getJurorNumber())
                .firstName(result.getFirstName())
                .lastName(result.getLastName())
                .postcode(result.getPostcode())
                .status(POSTPONED_STATUS) // a postponement is a type of deferral.
                .postponedTo(result.getDeferralDate())
                .reason(WordUtils.capitalizeFully(result.getDeferralReason()))
                .poolNumber(result.getPoolNumber());

            if (isIncludePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                postponeLetterData.datePrinted(datePrinted != null
                    ? datePrinted.toLocalDate()
                    : null);
            }

            postponeLetterDataList.add(postponeLetterData.build());
        }
        return postponeLetterDataList;
    }
}
