package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.messages.MessageSendRequest;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.ViewMessageTemplateDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;

import java.util.Map;

public interface MessagingService {
    ViewMessageTemplateDto getViewMessageTemplateDto(MessageType messageType, String locCode);

    ViewMessageTemplateDto getViewMessageTemplateDtoPopulated(MessageType messageType, String locCode,
                                                       Map<String, String> placeholders);

    PaginatedList<? extends JurorToSendMessageBase> search(MessageSearch messageSearch, String locCode,
                                                    boolean simpleResponse);

    void send(MessageType messageType, String locCode, MessageSendRequest messageSendRequest);
}
