package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorHistoryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class HistoryTemplateServiceImpl implements HistoryTemplateService {
    @Override
    public JurorHistoryResponseDto.JurorHistoryEntryDto toJurorHistoryEntryDto(JurorHistory item) {
        return JurorHistoryResponseDto.JurorHistoryEntryDto.builder()
            .description(item.getHistory().getDescription())
            .dateCreated(item.getDateCreated())
            .username(item.getCreatedBy())
            .poolNumber(item.getPoolNumber())
            .details(getHistoryDetails(item))
            .build();
    }

    private List<String> getHistoryDetails(JurorHistory item) {
        if (item.isMigrated() || item.getHistory().getTemplate() == null) {
            return getMigratedDetails(item);
        } else {
            String historyText = buildHistoryText(item);
            System.out.println(historyText);
            System.out.println(Arrays.toString(historyText.split("\\R")));
            System.out.println(Arrays.toString(historyText.split("\\n")));
            System.out.println(Arrays.toString(historyText.split("\n")));
            System.out.println(Arrays.toString(historyText.split("\\n")));
            System.out.println(Arrays.toString(historyText.split(System.lineSeparator())));
            return List.of(historyText.split("\\n"));
        }
    }

    private String buildHistoryText(JurorHistory item) {
        String template = item.getHistory().getTemplate();
        template = replace(template, "other_information", item.getOtherInformation());
        template = replace(template, "other_info_reference", item.getOtherInformationRef());

        template = replaceRegex(template, "\\{other_info_date:(.*?)\\}", matcher -> {
            if (item.getOtherInformationDate() != null) {
                final String dateFormat = matcher.group(1);
                return DateTimeFormatter.ofPattern(dateFormat).format(item.getOtherInformationDate());
            }
            return null;
        });

        return template;
    }

    private String replace(String template, String key, String value) {
        return template.replace("{" + key + "}", Optional.ofNullable(value).orElse(""));
    }

    private String replaceRegex(String template, String pattern,
                                Function<Matcher, String> valueProvider) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(template);

        String result = null;
        if (matcher.find()) {
            result = valueProvider.apply(matcher);
        }
        return template.replaceAll(pattern, Optional.ofNullable(result).orElse(""));
    }

    @Deprecated
    private List<String> getMigratedDetails(JurorHistory item) {
        List<String> details = new ArrayList<>();
        if (item.getOtherInformation() != null) {
            details.add(item.getOtherInformation());
        }
        if (item.getOtherInformationDate() != null) {
            details.add(DateTimeFormatter.ISO_DATE.format(item.getOtherInformationDate()));
        }
        if (item.getOtherInformationRef() != null) {
            details.add(item.getOtherInformationRef());
        }
        return details;
    }


}
