package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryType;

import java.util.List;

/**
 * Response DTO to list available enquiry types.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Enquiry type list response")
public class ContactEnquiryTypeListDto {

    @JsonProperty("enquiryTypes")
    @Schema(description = "List of available enquiry types")
    private List<ContactEnquiryType> data;

}
