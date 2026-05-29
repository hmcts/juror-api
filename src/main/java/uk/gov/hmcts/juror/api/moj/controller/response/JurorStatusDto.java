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

    @SuppressWarnings({"PMD.ShortMethodName"})
    public static JurorStatusDto of(JurorStatus status) {
        return JurorStatusDto.builder()
            .status(status.getStatus())
            .statusDesc(status.getStatusDesc())
            .active(status.isActive())
            .build();
    }

    public Integer getCode() {
        return this.status;
    }

    public String getDescription() {
        return this.statusDesc;
    }

    public Boolean isActive() {
        return this.active;
    }
}
