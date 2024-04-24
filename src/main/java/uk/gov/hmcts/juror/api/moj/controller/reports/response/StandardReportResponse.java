package uk.gov.hmcts.juror.api.moj.controller.reports.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StandardReportResponse
    extends AbstractReportResponse<List<LinkedHashMap<String, Object>>> {
}
