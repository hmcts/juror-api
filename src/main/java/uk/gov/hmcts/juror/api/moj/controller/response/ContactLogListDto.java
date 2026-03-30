package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Response DTO for listing contact logs relating to a specific juror record.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "List of contact logs relating to a specific juror record")
public class ContactLogListDto {

    @JsonProperty("contactLogs")
    @Schema(description = "List of contact logs")
    private List<ContactLogDataDto> data;

    @Schema(description = "Common details for every Juror record")
    private JurorDetailsCommonResponseDto commonDetails;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Contact log data")
    @ToString
    public static class ContactLogDataDto {

        @JsonProperty("username")
        @Schema(name = "Username", description = "System user identifier")
        private String username;

        @JsonProperty("logDate")
        @Schema(name = "Date of log", description = "Date the contact log was originally recorded")
        private String logDate;

        @JsonProperty("enquiryType")
        @Schema(name = "Enquiry type", description = "Enquiry type description")
        private String enquiryType;

        @JsonProperty("notes")
        @Schema(name = "Notes", description = "Contact log notes/content")
        private String notes;

        /**
         * Initialise an instance of this DTO class using a ContactLog object to populate its properties.
         *
         * @param contactLog an object representation of a ContactLog record from the database
         */
        public ContactLogDataDto(ContactLog contactLog) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy KK:mm a", Locale.ENGLISH);
            this.logDate = contactLog.getStartCall().format(formatter);

            this.username = contactLog.getUsername();
            this.enquiryType = contactLog.getEnquiryType().getDescription();
            this.notes = contactLog.getNotes();
        }

    }

}
