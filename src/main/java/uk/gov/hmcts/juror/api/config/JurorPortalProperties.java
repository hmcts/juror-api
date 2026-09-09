package uk.gov.hmcts.juror.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "uk.gov.hmcts.juror.portal")
@Getter
@Setter
public class JurorPortalProperties {
    private String scheme;
    private String host;
    private String documentPath;
    private String mapPath;

    public String getDocumentBaseUrl() {
        return scheme + "://" + host + documentPath;
    }

    public String getMapBaseUrl() {
        return scheme + "://" + host + mapPath;
    }
}
