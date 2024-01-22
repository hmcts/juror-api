package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.bureau.domain.PhoneLog;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "All Details available on phone calls between Juror and Bureau officer")
public class PhoneLogDto implements Serializable {

    @Schema(description = "Juror number")
    private String jurorNumber;

    @Schema(description = "Phone log notes")
    private String notes;

    @Schema(description = "Bureau officer username")
    private String username;

    @Schema(description = "Phone code")
    private String phoneCode;

    @Schema(description = "Phone code description")
    private String phoneCodeDescription;

    @Schema(description = "Date/time of last update")
    private Date lastUpdate;

    @Schema(description = "Date/time of start of call")
    private Date startCall;

    @Schema(description = "Date/time of end of call")
    private Date endCall;

    public PhoneLogDto(final PhoneLog phoneLog) {
        this.jurorNumber = phoneLog.getJurorNumber();
        this.notes = phoneLog.getNotes();
        this.username = phoneLog.getUsername();
        this.phoneCode = phoneLog.getPhoneCode();
        this.lastUpdate = phoneLog.getLastUpdate();
        this.startCall = phoneLog.getStartCall();
        this.endCall = phoneLog.getEndCall();
    }
}
