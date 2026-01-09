package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.SmsMessagesReportResponseDto;

import java.util.List;

@Repository
public interface IMessageRepository {

    List<SmsMessagesReportResponseDto.SmsMessagesRecord> getSmsMessageCounts();
}

