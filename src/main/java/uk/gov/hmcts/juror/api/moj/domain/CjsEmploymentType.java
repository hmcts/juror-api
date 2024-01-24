package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;

@Getter
public enum CjsEmploymentType {
    POLICE("Police Force"),
    PRISON_SERVICE("HM Prison Service"),
    NCA("National Crime Agency"),
    JUDICIARY("Judiciary"),
    HMCTS("HMCTS"),
    OTHER("Other");

    private final String employer;

    CjsEmploymentType(String employer) {
        this.employer = employer;
    }

}
