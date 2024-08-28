package uk.gov.hmcts.juror.api.moj.controller.response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class JurorStatusDto {

    private int status;
    private String statusDesc;
    private boolean active;

    public static JurorStatusDto of(JurorStatus status) {
        return JurorStatusDto.builder()
            .status(status.getStatus())
            .statusDesc(status.getStatusDesc())
            .active(status.getActive())
            .build();
    }

    public Integer getCode() {
        return this.status;
    }

    public String getDescription() {
        return this.statusDesc;
    }

    public Boolean getActive() {
        return this.active;
    }
}
