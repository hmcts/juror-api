package uk.gov.hmcts.juror.api.moj.utils;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;

import java.time.LocalDateTime;

public class JurorHistoryUtils {

    private JurorHistoryUtils() {
    }

    public static void saveJurorHistory(HistoryCodeMod code, String jurorNumber, String poolNumber,
                                         BureauJWTPayload payload, JurorHistoryRepository jurorHistoryRepository) {
        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorNumber)
            .dateCreated(LocalDateTime.now())
            .historyCode(code)
            .createdBy(payload.getLogin())
            .poolNumber(poolNumber)
            .build();

        jurorHistoryRepository.save(jurorHistory);
    }
}
