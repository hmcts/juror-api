package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.audit.dto.TransportLimitAuditRecord;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "List of audit records for transport limit changes")
public class TransportLimitAuditListResponseDto implements Serializable {

    @JsonProperty("audit_records")
    @Schema(description = "List of audit records")
    private List<TransportLimitAuditResponseDto> auditRecords;

    @JsonProperty("total_records")
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
