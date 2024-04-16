package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterReponseDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.LawOfDemeter"})
public class ReissueLetterServiceImpl implements ReissueLetterService {

    private final JurorPoolRepository jurorPoolRepository;
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final PrintDataService printDataService;
    private final JurorStatusRepository jurorStatusRepository;
    private final JurorHistoryService jurorHistoryService;

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
                .toList()).toList();
        final List<String> headings = letterType.getReissueDataTypes().stream()
            .map(DataType::getDisplayText)
            .toList();
        final List<String> dataTypes = letterType.getReissueDataTypes().stream()
            .map(DataType::getDataType)
            .toList();

        data = setStatusDesc(data, headings);
        data = setFormCode(data, headings, request);
        return new ReissueLetterListResponseDto(headings, dataTypes, data);
    }

    @Override
    @Transactional
    public ReissueLetterReponseDto reissueLetter(ReissueLetterRequestDto request) {
        String login = SecurityUtil.getActiveUsersBureauPayload().getLogin();
        log.debug("Reissue letters request received from Bureau user {}", login);

        ReissueLetterReponseDto response = new ReissueLetterReponseDto();
        response.setJurors(new ArrayList<>());

        validateReissueRequest(request, response);

        // if no jurors with a modified status are found, print the requested letters
        if (response.getJurors().isEmpty()) {

            request.getLetters().stream().forEach(letter -> {
                // ensure the request to reprint the letter meets criteria
                validateRequestedLetter(letter);

                FormCode formCode = FormCode.getFormCode(letter.getFormCode());

                log.debug("Printing letter for juror number {} with form code {}", letter.getJurorNumber(),
                    letter.getFormCode());

                List<JurorPool> jurorPools = jurorPoolRepository
                    .findByJurorJurorNumberOrderByDateCreatedDesc(letter.getJurorNumber());

                if (jurorPools.isEmpty()) {
                    throw new MojException.NotFound("Juror not found for juror number "
                        + letter.getJurorNumber(), null);
                }

                BiConsumer<PrintDataService, JurorPool> letterPrinter = formCode.getLetterPrinter();
                if (letterPrinter == null) {
                    throw new MojException.InternalServerError(
                        "Attempting to send a letter without a resend letter function", null);
                }

                letterPrinter.accept(printDataService, jurorPools.get(0));

                // create letter history
                createLetterHistory(letter);

            });
        }

        return response;
    }

    private void validateReissueRequest(ReissueLetterRequestDto request, ReissueLetterReponseDto response) {
        request.getLetters().stream().forEach(letter -> {
            FormCode formCode = FormCode.getFormCode(letter.getFormCode());

            List<JurorPool> jurorPools = jurorPoolRepository
                .findByJurorJurorNumberOrderByDateCreatedDesc(letter.getJurorNumber());

            if (jurorPools.isEmpty()) {
                throw new MojException.NotFound("Juror not found for juror number "
                    + letter.getJurorNumber(), null);
            }

            JurorStatus jurorStatus = RepositoryUtils.retrieveFromDatabase(
                formCode.getJurorStatus(), jurorStatusRepository);

            if (!jurorStatus.equals(jurorPools.get(0).getStatus())) {
                JurorPool jurorPool = jurorPools.get(0);
                ReissueLetterReponseDto.ReissueLetterResponseData jurorData =
                    ReissueLetterReponseDto.ReissueLetterResponseData.builder()
                        .jurorNumber(letter.getJurorNumber())
                        .firstName(jurorPool.getJuror().getFirstName())
                        .lastName(jurorPool.getJuror().getLastName())
                        .jurorStatus(jurorPool.getStatus())
                        .build();
                response.getJurors().add(jurorData);

            }
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
    @SuppressWarnings("PMD.LinguisticNaming")
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

    // If the letter has not been printed previously, the form code will be null (no record in bulk print table),
    // therefore need to set this value before returning the response
    @SuppressWarnings("PMD.LinguisticNaming")
    private List<List<Object>> setFormCode(List<List<Object>> data, List<String> headings,
                                           ReissueLetterListRequestDto request) {

        // only need to set default form code for SUMMOND_REMINDER letter if the code is missing
        if (!LetterType.SUMMONED_REMINDER.equals(request.getLetterType())) {
            return data;
        }

        // ensure the heading exists before adding the missing form code
        final int formCodeIndex = headings.indexOf("hidden_form_code");
        if (formCodeIndex < 0) {
            return data;
        }

        final int jurorNumberIndex = headings.indexOf("Juror number");
        List<List<Object>> newLetterData = new ArrayList<>();
        for (List<Object> datum : data) {
            ArrayList<Object> newData = new ArrayList<>(datum);

            if (datum.get(formCodeIndex) == null) {
                newData.remove(formCodeIndex);

                // determine form code based on Welsh flag
                String jurorNumber = datum.get(jurorNumberIndex).toString();
                Juror juror = JurorPoolUtils.getActiveJurorRecord(jurorPoolRepository, jurorNumber);

                if (Boolean.TRUE.equals(juror.getWelsh())) {
                    newData.add(formCodeIndex, FormCode.BI_SUMMONS_REMINDER.getCode());
                } else {
                    newData.add(formCodeIndex, FormCode.ENG_SUMMONS_REMINDER.getCode());
                }
            }
            newLetterData.add(newData);
        }
        return newLetterData;
    }

    private void validateRequestedLetter(ReissueLetterRequestDto.@NotNull ReissueLetterRequestData letter) {
        // verify the letter exists to reprint
        Optional<BulkPrintData> printedLetter =
            bulkPrintDataRepository.findByJurorNumberFormCodeDatePrinted(letter.getJurorNumber(),
                letter.getFormCode(), letter.getDatePrinted());

        if (printedLetter.isEmpty()) {
            // for some letters do not throw exception as need to print the initial letter (after validation)
            List<String> createLetterIfNotExist = Arrays.asList("5228", "5228C");
            boolean createLetter = createLetterIfNotExist
                .stream().anyMatch(code -> code.contains(letter.getFormCode()));

            if (!createLetter) {
                throw new MojException.NotFound(String.format("Bulk print data not found for juror %s ",
                    letter.getJurorNumber()), null);
            }
        } else {
            // verify the same letter is not already pending a reprint
            bulkPrintDataRepository.findByJurorNumberFormCodeAndPending(letter.getJurorNumber(),
                    letter.getFormCode())
                .ifPresent(bulkPrintData -> {
                    throw new MojException.BadRequest(String.format("Letter already pending reprint for juror %s",
                        letter.getJurorNumber()), null);
                });
        }
    }

    @SuppressWarnings("PMD.TooFewBranchesForASwitchStatement")
    private void createLetterHistory(ReissueLetterRequestDto.ReissueLetterRequestData letter) {
        // TODO: implemement for other letter types and remove outer if statement
        if (FormCode.ENG_SUMMONS_REMINDER.getCode().equals(letter.getFormCode())
            || FormCode.BI_SUMMONS_REMINDER.getCode().equals(letter.getFormCode())) {

            JurorPool jurorPool = JurorPoolUtils.getLatestActiveJurorPoolRecord(jurorPoolRepository,
                letter.getJurorNumber());

            // add additional switch-case statements for other letter types
            switch (letter.getFormCode()) {
                case "5228", "5228C" -> jurorHistoryService.createSummonsReminderLetterHistory(jurorPool);
                default -> throw new MojException.NotImplemented("Letter type not implemented", null);
            }
        }
    }
}
