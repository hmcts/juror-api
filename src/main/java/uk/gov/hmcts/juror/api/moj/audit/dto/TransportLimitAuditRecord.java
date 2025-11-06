package uk.gov.hmcts.juror.api.moj.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


    @Data
    @Builder
    public class TransportLimitAuditRecord implements Serializable {

        private String locCode;
        private String courtName;
        private Long revisionNumber;
        private String changedBy;
        private LocalDateTime changeDateTime;

        private BigDecimal publicTransportPreviousValue;
        private BigDecimal publicTransportCurrentValue;

        private BigDecimal taxiPreviousValue;
        private BigDecimal taxiCurrentValue;

        public boolean hasPublicTransportChanged() {
            if (publicTransportPreviousValue == null && publicTransportCurrentValue == null) {
                return false;
            }
            if (publicTransportPreviousValue == null || publicTransportCurrentValue == null) {
                return true;
            }
            return publicTransportPreviousValue.compareTo(publicTransportCurrentValue) != 0;
        }

        public boolean hasTaxiChanged() {
            if (taxiPreviousValue == null && taxiCurrentValue == null) {
                return false;
            }
            if (taxiPreviousValue == null || taxiCurrentValue == null) {
                return true;
            }
            return taxiPreviousValue.compareTo(taxiCurrentValue) != 0;
        }

}
