package uk.gov.hmcts.juror.api.bureau.service;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.config.WelshDayMonthTranslationConfig;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateFieldMod;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.NotifyTemplateFieldRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.PoolRequestService;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation class for {@link JurorCommsNotifyPayLoadService}.
 */
@Slf4j
@Service
public class JurorCommsNotifyPayLoadServiceImpl implements JurorCommsNotifyPayLoadService {

    private static final String BULK_PRINT_DATA_DETAIL_REC = "bulk_print_data.detail_rec";
    private static final String SERVICE_START_DATE = "SERVICESTARTDATE";
    private static final String SERVICE_START_TIME = "SERVICESTARTTIME";
    private static final String DATE_FORMAT = "EEEE dd MMMM, yyyy";

    Locale langLocale = new Locale("en", "GB");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    DateTimeFormatter formatterWelsh = DateTimeFormatter.ofPattern(DATE_FORMAT, langLocale);


    private final NotifyTemplateFieldRepositoryMod notifyTemplateFieldRepositoryMod;


    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepositoryMod;
    private final WelshDayMonthTranslationConfig welshDayMonthTranslationConfig;

    private final PoolRequestService poolRequestService;

    private final WelshCourtLocationRepository welshCourtLocationRepository;


    @Autowired
    public JurorCommsNotifyPayLoadServiceImpl(NotifyTemplateFieldRepositoryMod notifyTemplateFieldRepositoryMod,

                                              JurorDigitalResponseRepositoryMod jurorDigitalResponseRepositoryMod,
                                              WelshDayMonthTranslationConfig welshDayMonthTranslationConfig,
                                              PoolRequestService poolRequestService,
                                              WelshCourtLocationRepository welshCourtLocationRepository) {
        Assert.notNull(notifyTemplateFieldRepositoryMod, "NotifyTemplateFieldRepositoryMod must not be null");

        Assert.notNull(jurorDigitalResponseRepositoryMod, "JurorDigitalResponseRepositoryMod must not be null");
        Assert.notNull(welshDayMonthTranslationConfig, "welshDayMonthTranslationConfig must not be null");
        Assert.notNull(poolRequestService, "PoolRequestService must not be null");
        Assert.notNull(welshCourtLocationRepository, "WelshCourtLocationRepository must not be null");
        this.notifyTemplateFieldRepositoryMod = notifyTemplateFieldRepositoryMod;

        this.jurorDigitalResponseRepositoryMod = jurorDigitalResponseRepositoryMod;
        this.welshDayMonthTranslationConfig = welshDayMonthTranslationConfig;
        this.poolRequestService = poolRequestService;
        this.welshCourtLocationRepository = welshCourtLocationRepository;
    }

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
        PoolRequest poolRequest = juror.getPool();
        final CourtLocation court = poolRequest.getCourtLocation();

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Letter Comms getting PAYLOAD STUFF : Started - {}", dateFormat.format(new Date()));

        List<NotifyTemplateFieldMod> fields = getPayLoadFieldsForTemplate(templateId);

        log.trace("payloadService. generating payloadMap. fields {}", fields.size());
        final Map<String, String> map = new HashMap<>();
        Object fieldValue;
        try {
            for (NotifyTemplateFieldMod field : fields) {
                if (field.getDatabaseField().equals(BULK_PRINT_DATA_DETAIL_REC)) {
                    log.trace("is bulk_print_data.detail_rec field: {} ", field.getTemplateField());
                    map.put(
                        field.getTemplateField(),
                        detailData.substring(field.getPositionFrom() - 1, field.getPositionTo()).trim()
                    );
                } else if (field.getTemplateField().equals(SERVICE_START_DATE)) {
                    log.trace("is service start date : {} ", field.getJdClassProperty());
                    fieldValue = invokeGetter(juror, field.getJdClassProperty());
                    String formattedDate = dateTimeFormatter.format((LocalDate) fieldValue);
                    log.trace("class of fieldValue : {}", fieldValue.getClass().getCanonicalName());
                    map.put(field.getTemplateField(), formattedDate);
                } else if (field.getTemplateField().equals(SERVICE_START_TIME)) {
                    log.trace("is service start time : {} ", field.getJdClassProperty());
                    final String attendTime = getAttendTime(juror);
                    map.put(field.getTemplateField(), attendTime);
                } else if (field.getDatabaseField().equals("POOL.LOC_CODE")) {
                    log.info("is other fields : {} ", field.getJdClassProperty());
                    fieldValue = invokeGetter(juror, field.getJdClassProperty());
                    map.put(field.getTemplateField(), fieldValue.toString().substring(22, 25));
                } else if (field.getJdClassName().equals("court")) {
                    fieldValue = invokeGetter(court, field.getJdClassProperty());
                    String value = fieldValue == null ? "" : fieldValue.toString();
                    value = value.replace(",", "\r\n");
                    map.put(field.getTemplateField(), value);
                } else {
                    log.trace("is other fields : {} ", field.getJdClassProperty());
                    fieldValue = invokeGetterJurorPool(juror, field);
                    map.put(field.getTemplateField(), fieldValue.toString());
                    log.debug("fieldValue: {} ", fieldValue);
                }
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
        final DigitalResponse digitalResponse =
            jurorDigitalResponseRepositoryMod.findByJurorNumber(jurorPool.getJurorNumber());

        final CourtLocation court = jurorPool.getPool().getCourtLocation();
        final WelshCourtLocation welshCourtLocation = getWelshCourtLocation(court.getLocCode());
        Boolean isWelshCourt = isWelshCourtAndComms(jurorPool.getJuror().getWelsh(), welshCourtLocation);
        log.trace("payloadService-reflection generating payloadMap. fields {}", fields.size());

        final Map<String, String> map = new HashMap<>();
        try {
            Object fieldValue;
            String value;
            for (NotifyTemplateFieldMod field : fields) {
                if (field.getJdClassName().equalsIgnoreCase("pool")
                    && field.getJdClassProperty().equals("attendTime")) {
                    log.trace("is pool {} ", field.getJdClassProperty());
                    final String attendTime = getAttendTime(jurorPool);
                    map.put(field.getTemplateField(), attendTime);
                } else if (field.getJdClassName().equals("juror")) {
                    //meta java reflection
                    log.trace("is juror {} ", field.getJdClassProperty());
                    fieldValue = invokeGetterJurorPool(jurorPool, field);
                    if (field.getTemplateField().equalsIgnoreCase(SERVICE_START_DATE)) {
                        String formattedDate = dateTimeFormatter.format((LocalDate) fieldValue);
                        String formattedDateWelsh = formatterWelsh.format((LocalDate) fieldValue);
                        String str = null;

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

                    } else if (field.getDatabaseField().equalsIgnoreCase("pool.loc_code")) {
                        log.trace("is other fields : {} ", field.getJdClassProperty());
                        fieldValue = invokeGetterJurorPool(jurorPool, field);
                        map.put(field.getTemplateField(), fieldValue.toString().substring(22, 25));
                    } else {
                        map.put(field.getTemplateField(), fieldValue.toString());
                    }

                } else if (field.getJdClassName().equals("court")) {
                    fieldValue = getFieldValueForCourt(isWelshCourt, field, court, welshCourtLocation);
                    value = fieldValue == null ? "" : fieldValue.toString();
                    if (!value.isEmpty() && field.getJdClassProperty().equals("locationAddress")) {
                        value = value.replace(",", "\r\n");
                    }
                    log.trace("court. fieldvalue is : {} {} ", field.getJdClassProperty(), value);
                    map.put(field.getTemplateField(), value);
                } else if (field.getJdClassName().equals("jurorResponse")) {
                    fieldValue = invokeGetter(digitalResponse, field.getJdClassProperty());
                    value = fieldValue == null ? "" : fieldValue.toString();
                    log.trace("jurorResponse. fieldvalue is : {} {} ", field.getJdClassProperty(), value);
                    map.put(field.getTemplateField(), value);
                }
            }

        } catch (Exception e) {
            log.error("Failed to generate the template field map.", e);
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }

        return map;
    }

    @Override
    public Boolean isWelshCourtAndComms(Boolean welsh, WelshCourtLocation welshCourtLocation) {
        log.debug("inside isWelshComms");
        if (welshCourtLocation == null || welsh == null) {
            log.trace("not welsh court");
            return false;
        } else {
            log.trace("welsh court");
            return welsh;
        }
    }

    @Override
    public WelshCourtLocation getWelshCourtLocation(String locationCode) {
        return welshCourtLocationRepository.findByLocCode(locationCode);
    }


    private Object getFieldValueForCourt(Boolean isWelshCourt, NotifyTemplateFieldMod field, CourtLocation court,
                                         WelshCourtLocation welshCourtLocation) {
        log.trace("getFieldValueForCourt - field : {} iswelsh {} ", field.getJdClassProperty(), isWelshCourt);
        if (field.getJdClassProperty().equals("juryOfficerPhone") || field.getJdClassProperty().equals("postcode")) {
            return invokeGetter(court, field.getJdClassProperty());
        } else if (isWelshCourt) {
            return invokeGetter(welshCourtLocation, field.getJdClassProperty());
        } else {
            return invokeGetter(court, field.getJdClassProperty());
        }
    }

    /**
     * Strip out unwanted template fields.
     * For LETTER_COMMS, only interested in data coming from juror.print_files.detail_rec
     *
     * @return List of required fields for template.
     */
    private List<NotifyTemplateFieldMod> getPayLoadFieldsForTemplate(String templateId) {
        log.debug("Inside PayLoadService.getPayLoadFieldsForTemplate ........");
        return queryNotifyTemplateField(templateId);
    }

    /**
     * Strip out unwanted template fields.
     *
     * @return List of required fields for template.
     */
    private List<NotifyTemplateFieldMod> getPayLoadFields(String templateId) {

        List<NotifyTemplateFieldMod> payload = queryNotifyTemplateField(templateId);

        log.trace("after findByTemplateId() call - fields found : {} ", payload.size());
        return payload
            .stream()
            .filter(f -> f.getJdClassName() != null && f.getJdClassName().length() > 0)
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
            if (dayOfWeek != null && !"".equals(dayOfWeek)) {
                daysWeek.add(dayOfWeek);
            }
        }
        return daysWeek;
    }

    public List<String> setUpEnglishMonths() {
        List<String> monthNames = new ArrayList<>();
        for (String nameOfMonth : new DateFormatSymbols().getMonths()) {
            if (nameOfMonth != null && !"".equals(nameOfMonth)) {
                monthNames.add(nameOfMonth);
            }
        }
        return monthNames;
    }

    public List<String> setUpEnglishDaysMonth() {

        ArrayList<String> daysMonths = new ArrayList<>();

        for (int i = 0;
             i < setUpEnglishDaysWeek().size();
             i++) {
            daysMonths.add(setUpEnglishDaysWeek().get(i));
        }
        for (int ip = 0;
             ip < setUpEnglishMonths().size();
             ip++) {
            daysMonths.add(setUpEnglishMonths().get(ip));
        }
        return daysMonths;
    }

    public Map<String, String> setUpTranslationMap() {

        Map<String, String> myWelshTranslationMap = new HashMap<>();

        for (int i = 0;
             i < setUpWelshMonthDays().size();
             i++) {
            myWelshTranslationMap.put(setUpEnglishDaysMonth().get(i), setUpWelshMonthDays().get(i));
        }
        return myWelshTranslationMap;

    }

    public Object invokeGetterJurorPool(JurorPool obj, NotifyTemplateFieldMod field) {
        Object invokeObject = obj;
        if (field.getDatabaseField().startsWith("juror.")) {
            invokeObject = obj.getJuror();
        }
        if (field.getDatabaseField().startsWith("pool.")) {
            invokeObject = obj.getPool();
        }
        return invokeGetter(invokeObject, field.getJdClassProperty());
    }

    public Object invokeGetter(Object obj, String variableName) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(variableName, obj.getClass());
            Method getter = pd.getReadMethod();
            Object f = getter.invoke(obj);
            if (f != null) {
                log.info(f.toString());
            }
            return f;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | IntrospectionException e) {
            log.error(" reflection invokeGetter failed {} ", Arrays.toString(e.getStackTrace()));
        }
        return null;
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
}
