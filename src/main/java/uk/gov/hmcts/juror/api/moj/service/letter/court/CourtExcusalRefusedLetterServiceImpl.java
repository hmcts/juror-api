package uk.gov.hmcts.juror.api.moj.service.letter.court;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.NonDeferralLetterData;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalRefusedLetterList;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.ExcusalRefusalLetterListRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtExcusalRefusedLetterServiceImpl implements CourtLetterService {
    private static final String DATE_REFUSED = "Date Refused";
    private final List<String> headings =
        List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, POSTCODE, STATUS, DATE_REFUSED, REASON,
            DATE_PRINTED, POOL_NUMBER);

    private final List<String> dataTypes = List.of(STRING, STRING, STRING, STRING, STRING, DATE, STRING, DATE, HIDDEN);

    @NonNull
    private final ExcusalRefusalLetterListRepository excusalRefusalLetterListRepository;


    @Override
    public LetterListResponseDto getEligibleList(CourtLetterListRequestDto courtLetterListRequestDto) {
        log.trace("Enter getEligibleDeferralGrantedList");

        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(courtLetterListRequestDto);

        log.debug("Find jurors eligible for a court deferral letter for the primary court: {}", owner);

        List<ExcusalRefusedLetterList> eligibleJurorRecords =
            excusalRefusalLetterListRepository.findJurorsEligibleForExcusalRefusalLetter(searchCriteria, owner);
        log.debug("{} records found", eligibleJurorRecords.size());

        List<NonDeferralLetterData> excusalRefusedLetterData =
            serialiseExcusalRefusedLetterData(eligibleJurorRecords, courtLetterListRequestDto.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(excusalRefusedLetterData)
            .build();

        log.trace("Exit getEligibleDeferralGrantedList");
        return responseDto;
    }

    private List<NonDeferralLetterData> serialiseExcusalRefusedLetterData(
        List<ExcusalRefusedLetterList> eligibleJurorRecords,
        boolean includePrinted) {
        List<NonDeferralLetterData> excusalRefusedLetterDataList = new ArrayList<>();
        for (ExcusalRefusedLetterList result : eligibleJurorRecords) {
            NonDeferralLetterData.NonDeferralLetterDataBuilder excusalRefusedLetterData =
                NonDeferralLetterData.builder()
                    .jurorNumber(result.getJurorNumber())
                    .firstName(result.getFirstName())
                    .lastName(result.getLastName())
                    .postcode(result.getPostcode())
                    .status(result.getStatus())
                    .dateRefused(result.getDateExcused())
                    .reason(result.getReason())
                    .poolNumber(result.getPoolNumber());

            if (includePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                excusalRefusedLetterData.datePrinted(datePrinted != null
                    ? datePrinted.toLocalDate()
                    : null);
            }
            excusalRefusedLetterDataList.add(excusalRefusedLetterData.build());
        }
        return excusalRefusedLetterDataList;
    }
}
