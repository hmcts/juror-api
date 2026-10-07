package uk.gov.hmcts.juror.api.moj.controller.request.messages;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identifies which Notify template id (from APP_SETTING) to resolve and use")
public enum EmailTemplateName {

    @Schema(description = "Maps to WE_ARE_GROUP_CONTACT_INFORMATION_TEMPLATE_ID")
    CONTACT_INFORMATION,

    @Schema(description = "Maps to WE_ARE_GROUP_REFERRAL_CONFIRMED_TEMPLATE_ID")
    REFERRAL_CONFIRMED
}
