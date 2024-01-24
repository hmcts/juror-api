package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.bureau.domain.THistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

public interface PartHistoryService {

    void createPoliceCheckDisqualifyPartHistory(JurorPool jurorPool);

    void createPoliceCheckQualifyPartHistory(JurorPool jurorPool, boolean isChecked);

    void createPoliceCheckInProgressPartHistory(JurorPool jurorPool);

    void createPoliceCheckInsufficientInformationPartHistory(JurorPool jurorPool);

    //TODO replace with actual PartHistoryCode enum once Part history migration has occurred
    enum PartHistoryCode {
        DISQUALIFY_POOL_MEMBER(THistoryCode.DISQUALIFY_POOL_MEMBER),
        ELECTRONIC_POLICE_CHECK_QUALIFY("POLG"),
        ELECTRONIC_POLICE_CHECK_DISQUALIFY("POLF"),
        INSUFFICIENT_INFORMATION("POLI"),
        ELECTRONIC_POLICE_CHECK_REQUEST("POLE");

        private final String partHistoryCode;

        PartHistoryCode(String partHistoryCode) {
            this.partHistoryCode = partHistoryCode;
        }

        public String getCode() {
            return this.partHistoryCode;
        }
    }
}
