package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    CASH("Cash"),
    BACS("BACS");

    final String name;

    PaymentMethod(String name) {
        this.name = name;
    }

}
