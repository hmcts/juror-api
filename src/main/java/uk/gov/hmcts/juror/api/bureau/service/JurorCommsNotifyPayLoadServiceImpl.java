package uk.gov.hmcts.juror.api.bureau.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.config.WelshDayMonthTranslationConfig;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.ICourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateFieldMod;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateMapperMod;
import uk.gov.hmcts.juror.api.moj.domain.TemporaryCourtAddress;
import uk.gov.hmcts.juror.api.moj.domain.TemporaryCourtName;
import uk.gov.hmcts.juror.api.moj.repository.NotifyTemplateFieldRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorCommonResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.PoolRequestService;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation class for {@link JurorCommsNotifyPayLoadService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JurorCommsNotifyPayLoadServiceImpl implements JurorCommsNotifyPayLoadService {

    private static final String SERVICE_START_DATE = "SERVICESTARTDATE";
    private static final String SERVICE_START_TIME = "SERVICESTARTTIME";
    private static final String DATE_FORMAT = "EEEE dd MMMM, yyyy";

    private static final DateTimeFormatter ENGLISH_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter WELSH_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern(DATE_FORMAT, new Locale("en", "GB"));

    private final NotifyTemplateFieldRepositoryMod notifyTemplateFieldRepositoryMod;
    private final JurorCommonResponseRepositoryMod commonResponseRepositoryMod;
    private final WelshDayMonthTranslationConfig welshDayMonthTranslationConfig;
    private final PoolRequestService poolRequestService;
    private final WelshCourtLocationRepository welshCourtLocationRepository;

    /**
     * Establishes the mapping for the required fields required for the given templateId
     * and their corresponding values, obtained from the given detailData parameter (letter comms).
     *
     * @param templateId template for which the payload is to be assembled for.
     * @param detailData source of data.
     * @return Map pairing for each template placeholder:value.
     */
    @Override
    public Map<String, String> generatePayLoadData(String templateId, String detailData, JurorPool juror) {

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Letter Comms getting payload details : "
                + "Template {},"
                + " juror {},"
                + " juror pool {},"
                + " Started - {}", templateId,
            juror.getJurorNumber(),
            juror.getPoolNumber(),
            dateFormat.format(new Date()));

        List<NotifyTemplateFieldMod> fields = getPayLoadFieldsForTemplate(templateId);
        NotifyTemplateMapperMod.Context context = NotifyTemplateMapperMod.Context.from(juror,"Temporary Court Name",
            "Temporary Court Address");

        context.setDetailData(detailData);
        context.setTemporaryCourtName(TemporaryCourtName.TAUNTON.getTemporaryCourtName());
        context.setTemporaryCourtName(TemporaryCourtName.HARROW.getTemporaryCourtName());
        context.setTemporaryCourtAddress(TemporaryCourtAddress.TAUNTON.getTemporaryCourtAddress());
        context.setTemporaryCourtAddress(TemporaryCourtAddress.HARROW.getTemporaryCourtAddress());

        context.setAbstractResponse(commonResponseRepositoryMod.findByJurorNumber(juror.getJurorNumber()));
        context.setActualCourtLocation(context.getCourtLocation());
        final WelshCourtLocation welshCourtLocation = getWelshCourtLocation(context.getCourtLocation().getLocCode());
        context.setWelshCourtLocation(welshCourtLocation);
        Boolean isWelshCourt = isWelshCourtAndComms(juror.getJuror().getWelsh(), welshCourtLocation);



        final Map<String, String> map = new HashMap<>();
        Object fieldValue = null;
        try {
            for (NotifyTemplateFieldMod field : fields) {
                NotifyTemplateMapperMod mapperObject = field.getMapperObject();
                log.trace("processing field: {} using mapper {}", field.getTemplateField(), mapperObject);
                context.setPositionFrom(field.getPositionFrom());
                context.setPositionTo(field.getPositionTo());
                context.setDetailData(detailData);
                if (field.getTemplateField().equals(SERVICE_START_DATE)) {
                    fieldValue = invokeGetter(context, mapperObject);
                    String formattedDate;

                    if (mapperObject == NotifyTemplateMapperMod.BULK_PRINT_DATA) {
                        formattedDate = fieldValue.toString();
                    } else {
                        formattedDate = ENGLISH_DATE_TIME_FORMATTER.format((LocalDate) fieldValue);
                        if (isWelshCourt) {
                            String formattedDateWelsh = WELSH_DATE_TIME_FORMATTER.format((LocalDate) fieldValue);
                            String str;
                            Map<String, String> myWelshTranslationMap;
                            myWelshTranslationMap = setUpTranslationMap();

                            for (Map.Entry<String, String> entry : myWelshTranslationMap.entrySet()) {
                                str = formattedDateWelsh.replace(entry.getKey(), entry.getValue());
                                formattedDateWelsh = str;
                            }


                            formattedDate = formattedDateWelsh;
                        }
                    }
                    map.put(field.getTemplateField(), formattedDate);
                } else if (field.getTemplateField().equals(SERVICE_START_TIME)) {
                    final String attendTime = getAttendTime(context.getJurorPool());
                    map.put(field.getTemplateField(), attendTime);
                } else if (mapperObject.getType() == NotifyTemplateMapperMod.Type.COURT) {
                    fieldValue = invokeGetter(context, mapperObject, "");
                    String value = String.valueOf(fieldValue);
                    if (!value.isEmpty() && mapperObject == NotifyTemplateMapperMod.COURT_LOC_ADDRESS) {
                        value = value.replace(",", "\r\n");
                    }
                    map.put(field.getTemplateField(), value);
                    fieldValue = value;
                } else {
                    fieldValue = invokeGetter(context, mapperObject);
                    map.put(field.getTemplateField(), fieldValue.toString());
                }
                log.trace("fieldValue: {} ", fieldValue);

            }


        } catch (StringIndexOutOfBoundsException stre) {
            log.error(
                "Failed to establish data needed for notify template fields to send comms (missing template fields "
                    + "data)", stre);
            throw new StringIndexOutOfBoundsException();
        } catch (Exception e) {
            log.error(
                "Failed to establish data needed for notify template fields to send comms (missing template fields "
                    + "data)", e);
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }
        return map;
    }

    /**
     * Establishes the mapping for the required fields required for the given templateId.
     *
     * @param templateId template for which the payload is to be assembled for.
     * @return Map pairing for each template placeholder:value.
     */
    @Override
    public Map<String, String> generatePayLoadData(String templateId, JurorPool jurorPool) {

        List<NotifyTemplateFieldMod> fields = getPayLoadFields(templateId);
        log.trace("payloadService-reflection generating payloadMap. fields {}", fields.size());

        NotifyTemplateMapperMod.Context context = NotifyTemplateMapperMod.Context.from(jurorPool,"Temporary Court Name",
            "Temporary Court Address");
        context.setTemporaryCourtName(TemporaryCourtName.TAUNTON.getTemporaryCourtName());
        context.setTemporaryCourtName(TemporaryCourtName.HARROW.getTemporaryCourtName());
        context.setTemporaryCourtAddress(TemporaryCourtAddress.TAUNTON.getTemporaryCourtAddress());
        context.setTemporaryCourtAddress(TemporaryCourtAddress.HARROW.getTemporaryCourtAddress());
        context.setAbstractResponse(commonResponseRepositoryMod.findByJurorNumber(jurorPool.getJurorNumber()));
        context.setActualCourtLocation(context.getCourtLocation());
        final WelshCourtLocation welshCourtLocation = getWelshCourtLocation(context.getCourtLocation().getLocCode());
        context.setWelshCourtLocation(welshCourtLocation);
        Boolean isWelshCourt = isWelshCourtAndComms(jurorPool.getJuror().getWelsh(), welshCourtLocation);


        final Map<String, String> map = new HashMap<>();
        try {
            Object fieldValue = null;
            String value;
            for (NotifyTemplateFieldMod field : fields) {
                NotifyTemplateMapperMod mapperObject = field.getMapperObject();
                log.trace("processing field: {}", mapperObject);
                if (mapperObject == NotifyTemplateMapperMod.POOL_ATTEND_TIME) {
                    final String attendTime = getAttendTime(context.getJurorPool());
                    map.put(field.getTemplateField(), attendTime);
                } else if (mapperObject.getType() == NotifyTemplateMapperMod.Type.JUROR) {
                    fieldValue = invokeGetter(context, mapperObject);

                    if (field.getTemplateField().equalsIgnoreCase(SERVICE_START_DATE)) {
                        String formattedDate = ENGLISH_DATE_TIME_FORMATTER.format((LocalDate) fieldValue);
                        String formattedDateWelsh = WELSH_DATE_TIME_FORMATTER.format((LocalDate) fieldValue);
                        String str;

                        if (isWelshCourt) {
                            Map<String, String> myWelshTranslationMap;
                            myWelshTranslationMap = setUpTranslationMap();

                            for (Map.Entry<String, String> entry : myWelshTranslationMap.entrySet()) {
                                str = formattedDateWelsh.replace(entry.getKey(), entry.getValue());
                                formattedDateWelsh = str;
                            }
                        }

                        if (isWelshCourt) {
                            formattedDate = formattedDateWelsh;
                        }
                        map.put(field.getTemplateField(), formattedDate);

                    } else {
                        map.put(field.getTemplateField(), String.valueOf(fieldValue));
                    }

                } else if (mapperObject.getType() == NotifyTemplateMapperMod.Type.COURT) {
                    fieldValue = getFieldValueForCourt(isWelshCourt, mapperObject, context);
                    value = fieldValue == null ? "" : fieldValue.toString();
                    if (!value.isEmpty() && mapperObject == NotifyTemplateMapperMod.COURT_LOC_ADDRESS) {
                        value = value.replace(",", "\r\n");
                    }
                    map.put(field.getTemplateField(), value);
                } else if (mapperObject.getType() == NotifyTemplateMapperMod.Type.RESPONSE) {
                    map.put(field.getTemplateField(), invokeGetter(context, mapperObject, "").toString());
                }
                log.trace("fieldValue: {} ", fieldValue);
            }

        } catch (Exception e) {
            log.error("Failed to generate the template field map.", e);
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }

        return map;
    }

    @Override
    public Boolean isWelshCourtAndComms(Boolean welsh, WelshCourtLocation welshCourtLocation) {
        if (welshCourtLocation == null || welsh == null) {
            return false;
        } else {
            return welsh;
        }
    }

    @Override
    public WelshCourtLocation getWelshCourtLocation(String locationCode) {
        return welshCourtLocationRepository.findByLocCode(locationCode);
    }


    private Object getFieldValueForCourt(boolean isWelshCourt, NotifyTemplateMapperMod mapperMod,
                                         NotifyTemplateMapperMod.Context context) {
        final ICourtLocation actualCourtLocation = context.getActualCourtLocation();
        if (isWelshCourt
            && !Set.of(NotifyTemplateMapperMod.COURT_JURY_OFFICER_PHONE,
            NotifyTemplateMapperMod.COURT_LOC_POSTCODE).contains(mapperMod)) {
            context.setActualCourtLocation(context.getWelshCourtLocation());
        } else {
            context.setActualCourtLocation(context.getCourtLocation());
        }
        Object returnValue = invokeGetter(context, mapperMod);
        context.setActualCourtLocation(actualCourtLocation);
        return returnValue;
    }

    /**
     * Strip out unwanted template fields.
     * For LETTER_COMMS, only interested in data coming from juror.print_files.detail_rec
     *
     * @return List of required fields for template.
     */
    private List<NotifyTemplateFieldMod> getPayLoadFieldsForTemplate(String templateId) {
        return queryNotifyTemplateField(templateId);
    }

    /**
     * Strip out unwanted template fields.
     *
     * @return List of required fields for template.
     */
    private List<NotifyTemplateFieldMod> getPayLoadFields(String templateId) {
        List<NotifyTemplateFieldMod> payload = queryNotifyTemplateField(templateId);
        return payload
            .stream()
            .filter(f -> f.getMapperObject() != null)
            .collect(Collectors.toList());
    }

    private List<NotifyTemplateFieldMod> queryNotifyTemplateField(String templateId) {
        return notifyTemplateFieldRepositoryMod.findByTemplateId(templateId);
    }

    public List<String> setUpWelshMonthDays() {
        List<String> welshMonthsDays;
        welshMonthsDays = welshDayMonthTranslationConfig.getWelshDaysMonths();
        return welshMonthsDays;
    }

    public List<String> setUpEnglishDaysWeek() {
        List<String> daysWeek = new ArrayList<>();
        for (String dayOfWeek : new DateFormatSymbols().getWeekdays()) {
            if (dayOfWeek != null && !dayOfWeek.isEmpty()) {
                daysWeek.add(dayOfWeek);
            }
        }
        return daysWeek;
    }

    public List<String> setUpEnglishMonths() {
        List<String> monthNames = new ArrayList<>();
        for (String nameOfMonth : new DateFormatSymbols().getMonths()) {
            if (nameOfMonth != null && !nameOfMonth.isEmpty()) {
                monthNames.add(nameOfMonth);
            }
        }
        return monthNames;
    }

    public List<String> setUpEnglishDaysMonth() {
        ArrayList<String> daysMonths = new ArrayList<>();
        daysMonths.addAll(setUpEnglishDaysWeek());
        daysMonths.addAll(setUpEnglishMonths());
        return daysMonths;
    }

    public Map<String, String> setUpTranslationMap() {
        Map<String, String> myWelshTranslationMap = new HashMap<>();
        for (int i = 0; i < setUpWelshMonthDays().size(); i++) {
            myWelshTranslationMap.put(setUpEnglishDaysMonth().get(i), setUpWelshMonthDays().get(i));
        }
        return myWelshTranslationMap;
    }

    /**
     * Gets the attendance time for a summons
     * If the attend time in JUROR.UNIQUE_POOL is populated, this value will be returned. Otherwise the 'default' attend
     * time for the court will be used.
     *
     * @param jurorDetails juror details to transform, not null
     * @return attendance time, nullable
     */
    private String getAttendTime(JurorPool jurorDetails) {
        LocalDateTime poolAttendTime = poolRequestService.getPoolAttendanceTime(jurorDetails.getPoolNumber());
        if (poolAttendTime != null) {
            if (log.isTraceEnabled()) {
                log.trace("Attend time is set in unique pool, using pool attend time of {}", poolAttendTime);
            }
            return DateUtils.TIME_FORMAT.format(poolAttendTime);

        } else {
            final String courtAttendTime = DateUtils.TIME_FORMAT.format(jurorDetails.getCourt().getCourtAttendTime());
            if (log.isTraceEnabled()) {
                log.trace("Attend time is not set in pool, using court attend time of {}", courtAttendTime);
            }
            return courtAttendTime;
        }
    }

    private Object invokeGetter(NotifyTemplateMapperMod.Context context, NotifyTemplateMapperMod mapperObject,
                                Object defaultValue) {
        return Optional.ofNullable(mapperObject.getMapper().apply(context)).orElse(defaultValue);
    }

    private Object invokeGetter(NotifyTemplateMapperMod.Context context, NotifyTemplateMapperMod mapperObject) {
        return invokeGetter(context, mapperObject, null);
    }
}
