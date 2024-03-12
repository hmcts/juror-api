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
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
        log.debug("Reissue letter list request received from bureau user {}", login);

        LetterType letterType = request.getLetterType();

        List<Tuple> letters = bulkPrintDataRepository.findLetters(request, letterType.getLetterQueryConsumer());

        if (letters.isEmpty()) {
            throw new MojException.NotFound("No letters found for the given criteria",
                null);
        }

        List<List<Object>> data = letters.stream()
            .map(tuple -> letterType.getReissueDataTypes().stream()
                .map(dataType -> dataType.transform(tuple.get(dataType.getExpression())))
                .toList()
            ).toList();
        final List<String> headings = letterType.getReissueDataTypes().stream()
            .map(DataType::getDisplayText)
            .toList();
        final List<String> dataTypes = letterType.getReissueDataTypes().stream()
            .map(DataType::getDataType)
            .toList();

        data = setStatusDesc(data, headings);
        return new ReissueLetterListResponseDto(headings, dataTypes, data);
    }

    @Override
    @Transactional
    public void reissueLetter(ReissueLetterRequestDto request) {
        String login = SecurityUtil.getActiveUsersBureauPayload().getLogin();
        log.debug("Reissue letters request received from Bureau user {}", login);

        request.getLetters().stream().forEach(letter -> {
            // verify the user is reprinting the same letter type with bulk print data table
            bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted(letter.getJurorNumber(),
                    letter.getFormCode(), letter.getDatePrinted())
                .orElseThrow(() -> new MojException.NotFound(
                    String.format("Bulk print data not found for juror %s ", letter.getJurorNumber()),
                    null));

            // verify the same letter is not already pending a reprint
            bulkPrintDataRepository.findByJurorNumberFormCodeAndPending(letter.getJurorNumber(), letter.getFormCode())
                .ifPresent(bulkPrintData -> {
                    throw new MojException.BadRequest(String.format("Letter already pending reprint for juror %s ",
                        letter.getJurorNumber()), null);
                });

            FormCode formCode = FormCode.getFormCode(letter.getFormCode());

            // TODO: history to show letter requested by bureau user
            log.debug("Printing letter for juror number {} with form code {}", letter.getJurorNumber(),
                letter.getFormCode());

            JurorStatus jurorStatus = RepositoryUtils.retrieveFromDatabase(
                formCode.getJurorStatus(), jurorStatusRepository);

            List<JurorPool> jurorPools = jurorPoolRepository
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(letter.getJurorNumber(),
                    jurorStatus);

            if (jurorPools.isEmpty()) {
                throw new MojException.NotFound("Juror not found for juror number "
                    + letter.getJurorNumber(), null);
            }

            BiConsumer<PrintDataService, JurorPool> letterPrinter = formCode.getLetterPrinter();
            if (letterPrinter.equals(null)) {
                throw new MojException.InternalServerError(
                    "Attempting to send a letter without a resend letter function", null);
            }

            letterPrinter.accept(printDataService, jurorPools.get(0));
        });
    }

    @Override
    @Transactional
    public void deletePendingLetter(ReissueLetterRequestDto request) {
        String login = SecurityUtil.getActiveUsersBureauPayload().getLogin();
        log.debug("Delete pending letter request received from Bureau user {}", login);

        ReissueLetterRequestDto.ReissueLetterRequestData letter = request.getLetters().get(0);

        BulkPrintData bulkPrintData =
            bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted(letter.getJurorNumber(),
                    letter.getFormCode(), letter.getDatePrinted())
                .orElseThrow(() -> new MojException.NotFound(
                    "Bulk print data not found for juror %s " + letter.getJurorNumber(),
                    null));
        // TODO: history to show letter pending deleted by bureau user
        log.debug("Deleting pending letter for juror number {} with form code {}", letter.getJurorNumber(),
            letter.getFormCode());

        bulkPrintDataRepository.delete(bulkPrintData);
    }

    /**
     * Set status description to be in line with certain statuses combined with status code.
     * @param data - Letter list data.
     * @param headings - Letter list headings
     * @return data with status updated to correspond to code.
     */
    private List<List<Object>> setStatusDesc(List<List<Object>> data, List<String> headings) {
        int statusIndex = headings.indexOf("Status");
        int reasonIndex = headings.indexOf("Reason");

        if (statusIndex < 0 || reasonIndex < 0) {
            return data;
        }

        List<List<Object>> newLetterData = new ArrayList<>();

        for (List<Object> datum : data) {
            ArrayList<Object> newData = new ArrayList<>();
            for (Object object : datum) {
                newData.add(object);
            }

            if (newData.get(statusIndex).equals("Deferred")
                && newData.get(reasonIndex).equals("Postponement of service")) {
                newData.remove(statusIndex);
                newData.add(statusIndex, "Postponed");
            }
            newLetterData.add(newData);
        }
        return newLetterData;
    }
}
