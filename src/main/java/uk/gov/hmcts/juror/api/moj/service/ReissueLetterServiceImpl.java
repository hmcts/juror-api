package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReissueLetterServiceImpl implements ReissueLetterService {

    private final JurorPoolRepository jurorPoolRepository;
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final PrintDataService printDataService;
    private final JurorStatusRepository jurorStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public ReissueLetterListResponseDto reissueLetterList(ReissueLetterListRequestDto request) {
        String login = SecurityUtil.getActiveUsersBureauPayload().getLogin();
        log.debug("Reissue letter list request received from bureau user %s", login);

        LetterType letterType = request.getLetterType();

        final List<String> headings = letterType.getReissueDataTypes().stream()
            .map(DataType::getDisplayText)
            .toList();
        final List<String> dataTypes = letterType.getReissueDataTypes().stream()
            .map(DataType::getDataType)
            .toList();

        List<Tuple> letters = bulkPrintDataRepository.findLetters(request, letterType.getLetterQueryConsumer());

        if (letters.isEmpty()) {
            throw new MojException.NotFound("No letters found for the given criteria", null);
        }

        final List<List<Object>> data = letters.stream()
            .map(tuple -> letterType.getReissueDataTypes().stream()
                .map(dataType -> dataType.transform(tuple.get(dataType.getExpression())))
                .toList()
            ).toList();

        return new ReissueLetterListResponseDto(headings, dataTypes, data);
    }

    @Override
    @Transactional
    public void reissueLetter(ReissueLetterRequestDto request) {
        String login = SecurityUtil.getActiveUsersBureauPayload().getLogin();
        log.debug("Reissue letters request received from Bureau user %s", login);

        request.getLetters().stream().forEach(letter -> {
            // verify the user is reprinting the same letter type with bulk print data table
            bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted(letter.getJurorNumber(),
                    letter.getFormCode(), letter.getDatePrinted())
                .orElseThrow(() -> new MojException.NotFound(
                    "Bulk print data not found for juror %s " + letter.getJurorNumber(), null));

            FormCode formCode = FormCode.getFormCode(letter.getFormCode());

            JurorStatus jurorStatus = RepositoryUtils.retrieveFromDatabase(
                formCode.getJurorStatus(), jurorStatusRepository);

            formCode.getLetterPrinter().accept(printDataService,
                jurorPoolRepository.findByJurorJurorNumberAndStatus(letter.getJurorNumber(), jurorStatus));
        });
    }

    @Override
    @Transactional
    public void deletePendingLetter(ReissueLetterRequestDto request) {
        String login = SecurityUtil.getActiveUsersBureauPayload().getLogin();
        log.debug("Delete pending letter request received from Bureau user %s", login);

        ReissueLetterRequestDto.ReissueLetterRequestData letter = request.getLetters().get(0);

        BulkPrintData bulkPrintData =
            bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted(letter.getJurorNumber(),
                    letter.getFormCode(), letter.getDatePrinted())
                .orElseThrow(() -> new MojException.NotFound(
                    "Bulk print data not found for juror %s " + letter.getJurorNumber(), null));

        bulkPrintDataRepository.delete(bulkPrintData);

    }

}
