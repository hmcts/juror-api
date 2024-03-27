package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.ExportContactDetailsRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.MessageSendRequest;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.ViewMessageTemplateDto;
import uk.gov.hmcts.juror.api.moj.domain.CsvBuilder;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.messages.Message;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessagePlaceholders;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageTemplate;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.MessageRepository;
import uk.gov.hmcts.juror.api.moj.repository.MessageTemplateRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.INVALID_SEND_TYPE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_MUST_HAVE_EMAIL;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_MUST_HAVE_PHONE_NUMBER;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_NOT_APART_OF_TRIAL;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.PLACEHOLDER_MUST_HAVE_VALUE;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.GodClass",
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
public class MessagingServiceImpl implements MessagingService {

    private final MessageTemplateRepository messageTemplateRepository;
    private final TrialRepository trialRepository;
    private final PanelRepository panelRepository;

    private final MessageRepository messagesRepository;
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final Clock clock;
    private final JurorRepository jurorRepository;
    private final JurorHistoryService historyService;

    MessageTemplate getMessageTemplate(MessageType messageType, boolean english) {
        Optional<MessageTemplate> messageTemplateOptional =
            messageTemplateRepository.findById(
                english ? messageType.getEnglishMessageId() : messageType.getWelshMessageId());
        return messageTemplateOptional.orElseThrow(() -> new MojException.NotFound((english ? "English" : "Welsh")
            + " message template not found for " + messageType, null));
    }

    @Override
    @Transactional(readOnly = true)
    public ViewMessageTemplateDto getViewMessageTemplateDto(MessageType messageType, String locCode) {
        ViewMessageTemplateDto viewMessageTemplateDto = new ViewMessageTemplateDto();

        MessageTemplate englishMessageTemplate = getMessageTemplate(messageType, true);
        viewMessageTemplateDto.setMessageTemplateEnglish(englishMessageTemplate.getText());
        viewMessageTemplateDto.setSendType(messageType.getSendType());

        Set<ViewMessageTemplateDto.Placeholder> placeholdersSet =
            new HashSet<>(getRequiredPlaceholders(englishMessageTemplate, locCode));


        if (isWelshLocation(locCode)) {
            MessageTemplate welshMessageTemplate = getMessageTemplate(messageType, false);
            viewMessageTemplateDto.setMessageTemplateWelsh(welshMessageTemplate.getText());
            placeholdersSet.addAll(getRequiredPlaceholders(welshMessageTemplate, locCode));
        }

        viewMessageTemplateDto.setPlaceholders(
            placeholdersSet.stream()
                .filter(ViewMessageTemplateDto.Placeholder::isEditable)
                .toList()
        );
        return viewMessageTemplateDto;
    }

    @Override
    @Transactional(readOnly = true)
    public ViewMessageTemplateDto getViewMessageTemplateDtoPopulated(MessageType messageType, String locCode,
                                                                     Map<String, String> overridePlaceholders) {
        ViewMessageTemplateDto viewMessageTemplateDto = new ViewMessageTemplateDto();
        viewMessageTemplateDto.setSendType(messageType.getSendType());

        MessageTemplate englishMessageTemplate = getMessageTemplate(messageType, true);
        viewMessageTemplateDto.setMessageTemplateEnglish(
            getMessageTemplatePopulated(englishMessageTemplate, locCode, false, overridePlaceholders)
        );

        if (isWelshLocation(locCode)) {
            MessageTemplate welshMessageTemplate = getMessageTemplate(messageType, false);
            viewMessageTemplateDto.setMessageTemplateWelsh(
                getMessageTemplatePopulated(welshMessageTemplate, locCode, true, overridePlaceholders)
            );
        }
        return viewMessageTemplateDto;
    }

    String getMessageTemplatePopulated(MessageTemplate messageTemplate,
                                       String locCode,
                                       boolean isWelsh,
                                       Map<String, String> overridePlaceholders) {
        return getMessageTemplatePopulated(messageTemplate,
            getPopulatedPlaceholders(getRequiredPlaceholders(messageTemplate, locCode), overridePlaceholders, isWelsh));
    }

    String getMessageTemplatePopulated(MessageTemplate messageTemplate,
                                       Map<String, String> placeholders) {
        String populatedMessageTemplate = messageTemplate.getText();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            populatedMessageTemplate = populatedMessageTemplate.replace(entry.getKey(), entry.getValue());
        }
        return populatedMessageTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedList<? extends JurorToSendMessageBase> search(MessageSearch messageSearch, String locCode,
                                                                  boolean simpleResponse) {
        PaginatedList<? extends JurorToSendMessageBase> jurorToSendMessages =
            messageTemplateRepository.messageSearch(messageSearch, locCode, simpleResponse, 500L);
        if (jurorToSendMessages.isEmpty()) {
            throw new MojException.NotFound("No jurors found that meet the search criteria", null);
        }
        return jurorToSendMessages;
    }

    void validateTrialNumber(String trialNumber, String locCode,
                             List<MessageSendRequest.JurorAndSendType> jurors) {
        if (trialNumber != null) {
            if (!doesTrialExist(trialNumber, locCode)) {
                throw new MojException.NotFound("Trial does not exist for trial number: " + trialNumber, null);
            }
            if (jurors.stream()
                .anyMatch(jurorNumberAndSendType -> !isJurorOnTrial(jurorNumberAndSendType.getJurorNumber(),
                    jurorNumberAndSendType.getPoolNumber(),
                    trialNumber, locCode))) {
                throw new MojException.BusinessRuleViolation("Juror is not apart of trial", JUROR_NOT_APART_OF_TRIAL);
            }
        }
    }

    @Override
    @Transactional
    public void send(MessageType messageType, String locCode, MessageSendRequest messageSendRequest) {
        validateTrialNumber(
            messageSendRequest.getPlaceholderValues().getOrDefault("<trial_no>", null),
            locCode, messageSendRequest.getJurors());

        MessageTemplate englishMessageTemplate = getMessageTemplate(messageType, true);

        CourtLocation courtLocation = courtLocationRepository.findByLocCode(locCode)
            .orElseThrow(() -> new MojException.NotFound("Court location '" + locCode + "' not found", null));

        final String englishTemplate = getMessageTemplatePopulated(englishMessageTemplate, locCode, false,
            messageSendRequest.getPlaceholderValues());

        final String welshTemplate;
        if (isWelshLocation(locCode)) {
            MessageTemplate welshMessageTemplate = getMessageTemplate(messageType, false);
            welshTemplate = getMessageTemplatePopulated(welshMessageTemplate, locCode, true,
                messageSendRequest.getPlaceholderValues());
        } else {
            welshTemplate = null;
        }

        messagesRepository.saveAll(
            messageSendRequest.getJurors().stream()
                .map(jurorNumberAndSendType -> createMessage(jurorNumberAndSendType, messageType, courtLocation,
                    englishTemplate, welshTemplate))
                .toList()
        );
    }

    @Override
    public String exportContactDetails(String locCode, ExportContactDetailsRequest exportContactDetailsRequest) {

        List<List<String>> exportItems = messageTemplateRepository
            .exportDetails(exportContactDetailsRequest, locCode);
        CsvBuilder csvBuilder =
            new CsvBuilder(
                exportContactDetailsRequest.getExportItems()
                    .stream()
                    .map(ExportContactDetailsRequest.ExportItems::getTitle)
                    .toList());

        exportItems.forEach(csvBuilder::addRow);
        return csvBuilder.build();
    }

    boolean doesTrialExist(String trialNumber, String locCode) {
        return trialRepository.existsByTrialNumberAndCourtLocationLocCode(trialNumber, locCode);
    }


    boolean isJurorOnTrial(String jurorNumber, String poolNumber, String trialNumber, String locCode) {
        return panelRepository
            .existsByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorPoolPoolPoolNumberAndJurorPoolJurorJurorNumber(
                trialNumber, locCode, poolNumber, jurorNumber
            );
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    Message createMessage(MessageSendRequest.JurorAndSendType jurorAndSendType,
                          MessageType messageType,
                          CourtLocation courtLocation,
                          String englishTemplate,
                          String welshTemplate) {
        if (!messageType.getSendType().supports(jurorAndSendType.getType())) {
            throw new MojException.BusinessRuleViolation("This message type only supports: "
                + messageType.getSendType(), INVALID_SEND_TYPE);
        }
        final String username = SecurityUtil.getActiveLogin();
        log.debug(String.format("User %s created a %s message of type %s for juror %s in pool %s",
            username,
            jurorAndSendType.getType(),
            messageType,
            jurorAndSendType.getJurorNumber(),
            jurorAndSendType.getPoolNumber()));

        Juror juror = getJuror(jurorAndSendType.getJurorNumber());
        Message message = Message.builder()
            .jurorNumber(juror.getJurorNumber())
            .fileDatetime(LocalDateTime.now(clock))
            .userName(username)
            .locationCode(courtLocation)
            .poolNumber(jurorAndSendType.getPoolNumber())
            .build();

        boolean both = MessageType.SendType.EMAIL_AND_SMS.equals(jurorAndSendType.getType());
        if (both || MessageType.SendType.EMAIL.equals(jurorAndSendType.getType())) {
            if (juror.getEmail() == null) {
                throw new MojException.BusinessRuleViolation(
                    "Email is required for juror: " + juror.getJurorNumber(),
                    JUROR_MUST_HAVE_EMAIL);
            }
            message.setEmail(juror.getEmail());
        }
        if (both || MessageType.SendType.SMS.equals(jurorAndSendType.getType())) {
            if (juror.getPhoneNumber() == null) {
                throw new MojException.BusinessRuleViolation(
                    "Phone number is required for juror: " + juror.getJurorNumber(),
                    JUROR_MUST_HAVE_PHONE_NUMBER);
            }
            message.setPhone(juror.getPhoneNumber());
        }

        String otherInfo;
        if (welshTemplate != null && juror.getWelsh()) {
            message.setSubject("Eich Gwasanaeth Rheithgor");
            message.setMessageText(welshTemplate);
            message.setMessageId(messageType.getWelshMessageId());
            otherInfo = getMessageTemplate(messageType, false).getTitle();
        } else {
            message.setSubject("Your Jury Service");
            message.setMessageText(englishTemplate);
            message.setMessageId(messageType.getEnglishMessageId());
            otherInfo = getMessageTemplate(messageType, true).getTitle();
        }

        historyService.createSendMessageHistory(jurorAndSendType.getJurorNumber(),
            jurorAndSendType.getPoolNumber(), otherInfo);
        return message;
    }

    Map<String, String> getPopulatedPlaceholders(List<ViewMessageTemplateDto.Placeholder> requiredPlaceholders,
                                                 Map<String, String> overridePlaceholderValues,
                                                 boolean isWelsh) {

        Map<String, String> placeholders = new ConcurrentHashMap<>();
        for (ViewMessageTemplateDto.Placeholder placeholder : requiredPlaceholders) {
            placeholders.put(placeholder.getPlaceholderName(),
                getPlaceholderValue(placeholder, overridePlaceholderValues, isWelsh));
        }
        return placeholders;
    }

    String getPlaceholderValue(ViewMessageTemplateDto.Placeholder placeholder,
                               Map<String, String> overridePlaceholderValues,
                               boolean isWelsh) {
        boolean isOverridable = isOverridable(placeholder.getPlaceholderName());
        String value;
        if ((placeholder.isEditable() || isOverridable)
            && overridePlaceholderValues.containsKey(placeholder.getPlaceholderName())) {
            value = overridePlaceholderValues.get(placeholder.getPlaceholderName());
        } else {
            value = placeholder.getDefaultValue();
        }

        if (value == null) {
            throw new MojException.BusinessRuleViolation(
                "Placeholder " + placeholder.getPlaceholderName() + " must have a value",
                PLACEHOLDER_MUST_HAVE_VALUE
            );
        }
        return placeholder.getDataType().convertData(value, isWelsh);
    }

    boolean isOverridable(String placeholderName) {
        return "<trial_no>".equals(placeholderName);
    }


    Juror getJuror(String jurorNumber) {
        Juror juror = jurorRepository.findByJurorNumber(jurorNumber);
        if (juror == null) {
            throw new MojException.NotFound(
                "Juror not found: JurorNumber: " + jurorNumber, null);
        }
        return juror;
    }

    List<ViewMessageTemplateDto.Placeholder> getRequiredPlaceholders(MessageTemplate messageTemplate,
                                                                     String locCode) {
        return messageTemplate.getPlaceholders().stream()
            .map(messagePlaceholder -> mapPlaceholder(messagePlaceholder, locCode))
            .toList();
    }

    ViewMessageTemplateDto.Placeholder mapPlaceholder(MessagePlaceholders messagePlaceholder, String locCode) {
        String defaultValue = null;
        if (messagePlaceholder.getSourceTableName() != null
            && messagePlaceholder.getSourceColumnName() != null) {
            defaultValue = messageTemplateRepository.getDefaultValue(messagePlaceholder, locCode);
        }
        return ViewMessageTemplateDto.Placeholder.builder()
            .displayName(messagePlaceholder.getDisplayName())
            .editable(messagePlaceholder.isEditable())
            .placeholderName(messagePlaceholder.getPlaceholderName())
            .dataType(messagePlaceholder.getType())
            .defaultValue(defaultValue)
            .build();

    }

    boolean isWelshLocation(String locCode) {
        return CourtLocationUtils.isWelshCourtLocation(
            welshCourtLocationRepository, locCode);
    }
}
