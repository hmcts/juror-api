package uk.gov.hmcts.juror.api.moj.domain;

import java.math.BigDecimal;

public interface HasTotals {
    BigDecimal getTotalPaid();

    BigDecimal getTotalDue();
}
