package uk.gov.hmcts.juror.api.moj.controller.managementdashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SmsMessagesReportResponseDto {

    private List<SmsMessagesRecord> records;
    private int totalMessagesSent;

    public SmsMessagesReportResponseDto() {
        this.records = List.of();
        this.totalMessagesSent = 0;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class SmsMessagesRecord {
        private String courtLocationNameAndCode;
        private int messagesSent;
    }

}
