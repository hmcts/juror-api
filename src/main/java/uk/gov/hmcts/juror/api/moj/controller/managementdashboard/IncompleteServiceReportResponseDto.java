package uk.gov.hmcts.juror.api.moj.controller.managementdashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class IncompleteServiceReportResponseDto {

    private List<IncompleteServiceRecord> records;

    public IncompleteServiceReportResponseDto() {
        records = new ArrayList<>();
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class IncompleteServiceRecord {
        private String court;
        private Integer numberOfIncompleteServices;
    }

}
