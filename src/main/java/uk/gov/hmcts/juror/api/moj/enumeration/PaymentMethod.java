package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;

import java.util.function.Function;

@Getter
public enum PaymentMethod {

    CASH("Cash", Appearance::isPayCash),
    BACS("BACS", appearance -> !appearance.isPayCash());

    final String name;
    final Function<Appearance, Boolean> isApplicableFunction;

    PaymentMethod(String name, Function<Appearance, Boolean> isApplicableFunction) {
        this.name = name;
        this.isApplicableFunction = isApplicableFunction;
    }

    public boolean isApplicable(Appearance appearance) {
        return isApplicableFunction.apply(appearance);
    }
}
