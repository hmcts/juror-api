package uk.gov.hmcts.juror.api.moj.service.letter.court;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.FailedToAttendLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.FailedToAttendLetterList;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.FailedToAttendLetterListRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtFailedToAttendLetterServiceImpl implements CourtLetterService {

    // String constants for response headings
    private static final String ABSENT_DATE = "Absent date";

    @NonNull
    private final FailedToAttendLetterListRepository failedToAttendLetterListRepository;

    @Override
    @Transactional(readOnly = true)
    public LetterListResponseDto getEligibleList(CourtLetterListRequestDto request) {
        log.trace("Enter getEligibleList - Failed to attend");

        String owner = SecurityUtil.getActiveOwner();

        CourtLetterSearchCriteria searchCriteria = buildSearchCriteria(request);

        log.debug("Find jurors eligible for a court failed to attend letter for the primary court: {}", owner);
        List<FailedToAttendLetterList> eligibleJurorRecords =
            failedToAttendLetterListRepository.findJurorsEligibleForFailedToAttendLetter(searchCriteria, owner);
        log.debug("{} records found", eligibleJurorRecords.size());

        List<String> headings = List.of(JUROR_NUMBER, FIRST_NAME, LAST_NAME, ABSENT_DATE, DATE_PRINTED);

        List<String> dataTypes = List.of(STRING, STRING, STRING, DATE, DATE);

        List<FailedToAttendLetterData> failedToAttendLetterDataList =
            serialiseFailedToAttendLetterData(eligibleJurorRecords, request.isIncludePrinted());

        LetterListResponseDto responseDto = LetterListResponseDto.builder()
            .headings(headings)
            .dataTypes(dataTypes)
            .data(failedToAttendLetterDataList)
            .build();

        log.trace("Exit getEligibleList (Failed To Attend letter)");
        return responseDto;
    }

    private List<FailedToAttendLetterData> serialiseFailedToAttendLetterData(
        List<FailedToAttendLetterList> eligibleJurorRecords,
        boolean isIncludePrinted) {
        List<FailedToAttendLetterData> failedToAttendLetterDataList = new ArrayList<>();
        for (FailedToAttendLetterList result : eligibleJurorRecords) {
            FailedToAttendLetterData.FailedToAttendLetterDataBuilder failedToAttendLetterData =
                FailedToAttendLetterData.builder()
                    .jurorNumber(result.getJurorNumber())
                    .firstName(result.getFirstName())
                    .lastName(result.getLastName())
                    .absentDate(result.getAbsentDate())
                    .poolNumber(result.getPoolNumber());

            if (isIncludePrinted) {
                LocalDateTime datePrinted = result.getDatePrinted();
                failedToAttendLetterData.datePrinted(datePrinted != null ? datePrinted.toLocalDate() : null);
            }

            failedToAttendLetterDataList.add(failedToAttendLetterData.build());
        }
        return failedToAttendLetterDataList;
    }
}
