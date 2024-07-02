package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "All Details available on phone calls between Juror and Bureau officer")
public class ContactLogDto implements Serializable {

    @Schema(description = "Juror number")
    private String jurorNumber;

    @Schema(description = "Phone log notes")
    private String notes;

    @Schema(description = "Bureau officer username")
    private String username;

    @Schema(description = "Phone code description")
    private String phoneCodeDescription;

    @Schema(description = "Date/time of last update")
    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    private LocalDateTime lastUpdate;

    @Schema(description = "Date/time of start of call")
    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    private LocalDateTime startCall;

    @Schema(description = "Date/time of end of call")
    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    private LocalDateTime endCall;

    public ContactLogDto(final ContactLog phoneLog) {
        this.jurorNumber = phoneLog.getJurorNumber();
        this.notes = phoneLog.getNotes();
        this.username = phoneLog.getUsername();
        this.lastUpdate = phoneLog.getLastUpdate();
        this.startCall = phoneLog.getStartCall();
        this.endCall = phoneLog.getEndCall();
    }
}
