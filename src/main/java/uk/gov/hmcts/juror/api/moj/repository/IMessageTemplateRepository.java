package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessagePlaceholders;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;

public interface IMessageTemplateRepository {
    PaginatedList<? extends JurorToSendMessageBase> messageSearch(MessageSearch search, String locCode,
                                                        boolean simpleResponse, Long maxItems);

    String getDefaultValue(MessagePlaceholders messagePlaceholder, String locCode);
}
