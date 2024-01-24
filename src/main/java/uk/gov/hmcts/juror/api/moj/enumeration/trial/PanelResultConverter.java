package uk.gov.hmcts.juror.api.moj.enumeration.trial;

import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class PanelResultConverter implements AttributeConverter<PanelResult, String> {
    @Override
    public String convertToDatabaseColumn(PanelResult attribute) {
        if (attribute != null) {
            return attribute.getCode();
        }
        return null;
    }

    @Override
    public PanelResult convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        return Arrays.stream(PanelResult.values()).filter(panelResult ->
            panelResult.getCode().equalsIgnoreCase(dbData)).findFirst().orElse(null);
    }
}
