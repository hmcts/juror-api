package uk.gov.hmcts.juror.api.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Notify client regional service keys.
 */
@Validated
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "notifyregion")

public class NotifyRegionsConfigurationProperties {

    private List<String> regionkeys = new ArrayList<>();

    public List<String> getRegionKeys() {
        return regionkeys;
    }
}

