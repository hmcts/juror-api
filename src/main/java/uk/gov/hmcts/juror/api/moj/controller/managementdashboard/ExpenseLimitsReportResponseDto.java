package uk.gov.hmcts.juror.api.moj.controller.managementdashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class ExpenseLimitsReportResponseDto {

    private List<ExpenseLimitsRecord> records;

    public ExpenseLimitsReportResponseDto() {
        records = new ArrayList<>();
    }

    public ExpenseLimitsReportResponseDto(List<ExpenseLimitsRecord> records) {
        this.records = records;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class ExpenseLimitsRecord {
        private String courtLocationNameAndCode;
        private String type;
        private Double oldLimit;
        private Double newLimit;
        private String changedBy;
    }

}
