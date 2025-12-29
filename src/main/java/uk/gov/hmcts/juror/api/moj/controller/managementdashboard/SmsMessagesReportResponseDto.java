package uk.gov.hmcts.juror.api.moj.controller.managementdashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SmsMessagesReportResponseDto {

    private List<SmsMessagesRecord> records;
    private Long totalMessagesSent;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class SmsMessagesRecord {
        private String courtLocationNameAndCode;
        private Long messagesSent;
    }

}
