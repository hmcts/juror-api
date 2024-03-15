package uk.gov.hmcts.juror.api.testsupport;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisableIfWeekendCondition.class)
public @interface DisableIfWeekend {
}
