package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.audit.dto.TransportLimitAuditRecord;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Response DTO wrapping a list of transport limit audit records.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "List of audit records for transport limit changes")
public class TransportLimitAuditListResponseDto implements Serializable {

    @Schema(description = "List of audit records")
    private List<TransportLimitAuditResponseDto> auditRecords;

    @Schema(description = "Total number of audit records", example = "5")
    private Integer totalRecords;

    /**
     * Convert from list of internal service DTOs to API response DTO.
     */
    public static TransportLimitAuditListResponseDto from(List<TransportLimitAuditRecord> records) {
        List<TransportLimitAuditResponseDto> responses = records.stream()
            .map(TransportLimitAuditResponseDto::from)
            .collect(Collectors.toList());

        return TransportLimitAuditListResponseDto.builder()
            .auditRecords(responses)
            .totalRecords(responses.size())
            .build();
    }

}
