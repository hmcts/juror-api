package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.audit.dto.TransportLimitAuditRecord;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit record showing changes to transport soft limits")
public class TransportLimitAuditResponseDto implements Serializable {

    @JsonProperty("loc_code")
    @Schema(description = "Court location code", example = "415")
    private String locCode;

    @JsonProperty("court_name")
    @Schema(description = "Court location name", example = "CHESTER")
    private String courtName;

    @JsonProperty("revision_number")
    @Schema(description = "Audit revision number", example = "12345")
    private Long revisionNumber;

    @JsonProperty("changed_by")
    @Schema(description = "Username of person who made the change", example = "john.smith@hmcts.net")
    private String changedBy;

    @JsonProperty("change_date_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date and time when change occurred", example = "2025-11-03T14:30:00")
    private LocalDateTime changeDateTime;

    @JsonProperty("public_transport_previous_value")
    @Schema(description = "Previous public transport soft limit value", example = "25.00000")
    private BigDecimal publicTransportPreviousValue;

    @JsonProperty("public_transport_current_value")
    @Schema(description = "Current public transport soft limit value", example = "30.00000")
    private BigDecimal publicTransportCurrentValue;

    @JsonProperty("public_transport_changed")
    @Schema(description = "Whether public transport limit changed in this revision", example = "true")
    private Boolean publicTransportChanged;

    @JsonProperty("public_transport_change_description")
    @Schema(description = "Human-readable description of public transport limit change",
        example = "£25.00 → £30.00")
    private String publicTransportChangeDescription;

    @JsonProperty("taxi_previous_value")
    @Schema(description = "Previous taxi soft limit value", example = "50.00000")
    private BigDecimal taxiPreviousValue;

    @JsonProperty("taxi_current_value")
    @Schema(description = "Current taxi soft limit value", example = "60.00000")
    private BigDecimal taxiCurrentValue;

    @JsonProperty("taxi_changed")
    @Schema(description = "Whether taxi limit changed in this revision", example = "true")
    private Boolean taxiChanged;

    @JsonProperty("taxi_change_description")
    @Schema(description = "Human-readable description of taxi limit change",
        example = "£50.00 → £60.00")
    private String taxiChangeDescription;

    /**
     * Convert from internal service DTO to API response DTO.
     */
    public static TransportLimitAuditResponseDto from(TransportLimitAuditRecord record) {
        return TransportLimitAuditResponseDto.builder()
            .locCode(record.getLocCode())
            .courtName(record.getCourtName())
            .revisionNumber(record.getRevisionNumber())
            .changedBy(record.getChangedBy())
            .changeDateTime(record.getChangeDateTime())
            .publicTransportPreviousValue(record.getPublicTransportPreviousValue())
            .publicTransportCurrentValue(record.getPublicTransportCurrentValue())
            .publicTransportChanged(record.hasPublicTransportChanged())
            .publicTransportChangeDescription(formatPublicTransportChange(record))
            .taxiPreviousValue(record.getTaxiPreviousValue())
            .taxiCurrentValue(record.getTaxiCurrentValue())
            .taxiChanged(record.hasTaxiChanged())
            .taxiChangeDescription(formatTaxiChange(record))
            .build();
    }

    private static String formatPublicTransportChange(TransportLimitAuditRecord record) {
        BigDecimal previous = record.getPublicTransportPreviousValue();
        BigDecimal current = record.getPublicTransportCurrentValue();

        if (previous == null && current == null) {
            return "No change";
        }
        if (previous == null) {
            return "Initially set to " + formatValue(current);
        }
        if (current == null) {
            return "Cleared (was " + formatValue(previous) + ")";
        }
        if (record.hasPublicTransportChanged()) {
            return formatValue(previous) + " → " + formatValue(current);
        }
        return "No change";
    }

    private static String formatTaxiChange(TransportLimitAuditRecord record) {
        BigDecimal previous = record.getTaxiPreviousValue();
        BigDecimal current = record.getTaxiCurrentValue();

        if (previous == null && current == null) {
            return "No change";
        }
        if (previous == null) {
            return "Initially set to " + formatValue(current);
        }
        if (current == null) {
            return "Cleared (was " + formatValue(previous) + ")";
        }
        if (record.hasTaxiChanged()) {
            return formatValue(previous) + " → " + formatValue(current);
        }
        return "No change";
    }

    private static String formatValue(BigDecimal value) {
        if (value == null) {
            return "null";
        }
        return String.format("£%.2f", value);
    }


}
