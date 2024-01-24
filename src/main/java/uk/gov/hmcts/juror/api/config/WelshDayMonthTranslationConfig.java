package uk.gov.hmcts.juror.api.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "welshtranslations")

public class WelshDayMonthTranslationConfig {

    private List<String> welshDaysMonths = new ArrayList<>();

    public List<String> getWelshDaysMonths() {
        return welshDaysMonths;
    }
}
