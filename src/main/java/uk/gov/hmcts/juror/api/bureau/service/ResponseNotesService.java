package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController;

public interface ResponseNotesService {
    /**
     * Retrieve the notes for a specific juror response.
     *
     * @param jurorId Juror number
     * @return Notes dto
     */
    ResponseUpdateController.JurorNoteDto notesByJurorNumber(String jurorId);

    /**
     * Update notes for specific juror response.
     *
     * @param noteDto   Dto containing updated notes
     * @param jurorId   Juror number to update
     * @param auditUser User performing the update
     */
    void updateNote(ResponseUpdateController.JurorNoteDto noteDto, String jurorId, String auditUser);

    @ResponseStatus(HttpStatus.CONFLICT)
    class NoteComparisonFailureException extends RuntimeException {
        public NoteComparisonFailureException() {
            super("Version field did not match computed value. Out of sync with DB value.");
        }
    }
}
