package uk.gov.hmcts.juror.api.bureau.service;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateField;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateFieldRepository;
import uk.gov.hmcts.juror.api.bureau.exception.JurorCommsNotificationServiceException;
import uk.gov.hmcts.juror.api.config.WelshDayMonthTranslationConfig;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private static final String PRINT_FILES_DETAIL_REC = "PRINT_FILES.DETAIL_REC";
    private static final String SERVICE_START_DATE = "SERVICESTARTDATE";
    private static final String SERVICE_START_TIME = "SERVICESTARTTIME";
    private static final String DATE_FORMAT = "EEEE dd MMMMM, yyyy";
    private static final String TIME_FORMAT = "HH:mm";

    Locale langLocale = new Locale("en", "GB");

    DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);


    DateFormat timeformatter = new SimpleDateFormat(TIME_FORMAT);
    DateFormat timeformatterunique = new SimpleDateFormat(TIME_FORMAT);
    DateFormat formatterWelsh = new SimpleDateFormat(DATE_FORMAT, langLocale);


    private final NotifyTemplateFieldRepository notifyTemplateFieldRepository;
    private final JurorResponseRepository jurorResponseRepository;
    private final WelshDayMonthTranslationConfig welshDayMonthTranslationConfig;
    private final UniquePoolService uniquePoolService;
    private final WelshCourtLocationRepository welshCourtLocationRepository;



    @Autowired
    public JurorCommsNotifyPayLoadServiceImpl(NotifyTemplateFieldRepository notifyTemplateFieldRepository,
                                              JurorResponseRepository jurorResponseRepository,
                                              WelshDayMonthTranslationConfig welshDayMonthTranslationConfig,
                                              UniquePoolService uniquePoolService,
                                              WelshCourtLocationRepository welshCourtLocationRepository) {
        Assert.notNull(notifyTemplateFieldRepository, "NotifyTemplateFieldRepository must not be null");
        Assert.notNull(jurorResponseRepository, "jurorResponseRepository must not be null");
        Assert.notNull(welshDayMonthTranslationConfig, "welshDayMonthTranslationConfig must not be null");
        Assert.notNull(uniquePoolService, "UniquePoolService must not be null");
        Assert.notNull(welshCourtLocationRepository, "WelshCourtLocationRepository must not be null");
        this.notifyTemplateFieldRepository = notifyTemplateFieldRepository;
        this.jurorResponseRepository = jurorResponseRepository;
        this.welshDayMonthTranslationConfig = welshDayMonthTranslationConfig;
        this.uniquePoolService = uniquePoolService;
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


    public Map<String, String> generatePayLoadData(String templateId, String detailData, Pool pool) {


        final CourtLocation court = pool.getCourt();
        final WelshCourtLocation welshCourtLocation = (court != null && court.getLocCode() !=null ? getWelshCourtLocation(court.getLocCode()) :null);
        Boolean isWelshCourt = isWelshCourtAndComms(pool.getWelsh(), welshCourtLocation);


        SimpleDateFormat dateFormat = new SimpleDateFormat();
        log.info("Letter Comms getting PAYLOAD STUFF : Started - {}", dateFormat.format(new Date()));

        List<NotifyTemplateField> fields = getPayLoadFieldsForTemplate(templateId);

        log.trace("payloadService. generating payloadMap. fields {}", fields.size());
        final Map<String, String> map = new HashMap<>();
        Object fieldValue;
        try {
            for (NotifyTemplateField field : fields) {

                if (field.getDatabaseField().equals(PRINT_FILES_DETAIL_REC)) {
                    log.trace("is print_files.detail_rec field: {} ", field.getTemplateField());
                    map.put(
                        field.getTemplateField(),
                        detailData.substring(field.getPositionFrom() - 1, field.getPositionTo()).trim()
                    );
                } else if (field.getTemplateField().equals(SERVICE_START_DATE)) {
                    log.trace("is service start date : {} ", field.getJdClassProperty());
                    fieldValue = invokeGetter(pool, field.getJdClassProperty());
                    String formattedDate = formatter.format((Date) fieldValue);
                    log.trace("class of fieldValue : {}", fieldValue.getClass().getCanonicalName());
                    map.put(field.getTemplateField(), formattedDate);
                } else if (field.getTemplateField().equals(SERVICE_START_TIME)) {
                    log.trace("is service start time : {} ", field.getJdClassProperty());
                    final String attendTime = getAttendTime(pool);
                    map.put(field.getTemplateField(), attendTime);
                } else if (field.getDatabaseField().equals("POOL.LOC_CODE")) {
                    log.info("is other fields : {} ", field.getJdClassProperty());
                    fieldValue = invokeGetter(pool, field.getJdClassProperty());
                    map.put(field.getTemplateField(), fieldValue.toString().substring(22, 25));
                } else if (field.getJdClassName().equals("court")) {
                    fieldValue = invokeGetter(court, field.getJdClassProperty());
                    String value = fieldValue == null
                        ?
                        ""
                        :
                            fieldValue.toString();
                    value = value.replace(",", "\r\n");
                    map.put(field.getTemplateField(), value);
                } else {
                    log.trace("is other fields : {} ", field.getJdClassProperty());
                    fieldValue = invokeGetter(pool, field.getJdClassProperty());
                    map.put(field.getTemplateField(), fieldValue.toString());
                    log.info("fieldValue: {} ", fieldValue);
                }
            }

        } catch (StringIndexOutOfBoundsException stre) {
            log.error(
                "Failed to establish data needed for notify template fields to send comms (missing template fields "
                    + "data) : "
                    + stre);
            throw new StringIndexOutOfBoundsException();
        } catch (Exception e) {
            log.error(
                "Failed to establish data needed for notify template fields to send comms (missing template fields "
                    + "data)  : " + e);
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
    public Map<String, String> generatePayLoadData(String templateId, Pool pool) {

        List<NotifyTemplateField> fields = getPayLoadFields(templateId);

        final JurorResponse jurorResponse = jurorResponseRepository.findByJurorNumber(pool.getJurorNumber());

        final CourtLocation court = pool.getCourt();
        final WelshCourtLocation welshCourtLocation = getWelshCourtLocation(court.getLocCode());

        Boolean isWelshCourt = isWelshCourtAndComms(pool.getWelsh(), welshCourtLocation);
        log.trace("payloadService-reflection generating payloadMap. fields {}", fields.size());

        final Map<String, String> map = new HashMap<>();
        try {
            Object fieldValue;
            String value;
            for (NotifyTemplateField field : fields) {
                if (field.getJdClassName().equals("uniquePool") && field.getJdClassProperty().equals("attendTime")) {
                    log.trace("is uniquePool {} ", field.getJdClassProperty());
                    final String attendTime = getAttendTime(pool);
                    map.put(field.getTemplateField(), attendTime);
                } else if (field.getJdClassName().equals("pool")) {
                    //meta java reflection
                    log.trace("is pool {} ", field.getJdClassProperty());
                    fieldValue = invokeGetter(pool, field.getJdClassProperty());
                    if (field.getTemplateField().equals(SERVICE_START_DATE)) {
                        String formattedDate = formatter.format((Date) fieldValue);
                        String formattedDateWelsh = formatterWelsh.format((Date) fieldValue);
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

                    } else if (field.getDatabaseField().equals("POOL.LOC_CODE")) {
                        log.info("is other fields : {} ", field.getJdClassProperty());
                        fieldValue = invokeGetter(pool, field.getJdClassProperty());
                        map.put(field.getTemplateField(), fieldValue.toString().substring(22, 25));
                    } else {
                        map.put(field.getTemplateField(), fieldValue.toString());
                    }

                } else if (field.getJdClassName().equals("court")) {
                    fieldValue = getFieldValueForCourt(isWelshCourt, field, court, welshCourtLocation);
                    value = fieldValue == null
                        ?
                        ""
                        :
                            fieldValue.toString();
                    if (!value.isEmpty() && field.getJdClassProperty().equals("locationAddress")) {
                        value = value.replace(",", "\r\n");
                    }
                    log.trace("court. fieldvalue is : {} {} ", field.getJdClassProperty(), value);
                    log.info("court. fieldvalue is : {} {} ", field.getJdClassProperty(), value);
                    map.put(field.getTemplateField(), value);
                } else if (field.getJdClassName().equals("jurorResponse")) {
                    fieldValue = invokeGetter(jurorResponse, field.getJdClassProperty());
                    value = fieldValue == null
                        ?
                        ""
                        :
                            fieldValue.toString();
                    log.trace("jurorResponse. fieldvalue is : {} {} ", field.getJdClassProperty(), value);
                    map.put(field.getTemplateField(), value);
                }

            }

        } catch (Exception e) {
            log.error("Failed to generate the template field map." + e);
            throw new JurorCommsNotificationServiceException(e.getMessage(), e);
        }

        return map;
    }

    @Override
    public Boolean isWelshCourtAndComms(Boolean welsh, WelshCourtLocation welshCourtLocation) {
        log.info("inside isWelshComms");
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


    private Object getFieldValueForCourt(Boolean isWelshCourt, NotifyTemplateField field, CourtLocation court,
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
     * @param templateId
     * @return List of required fields for template.
     */
    private List<NotifyTemplateField> getPayLoadFieldsForTemplate(String templateId) {
        log.debug("Inside PayLoadService.getPayLoadFieldsForTemplate ........");
        return queryNotifyTemplateField(templateId);
    }

    /**
     * Strip out unwanted template fields.
     *
     * @param templateId
     * @return List of required fields for template.
     */
    private List<NotifyTemplateField> getPayLoadFields(String templateId) {

        List<NotifyTemplateField> payload = queryNotifyTemplateField(templateId);

        log.trace("after findByTemplateId() call - fields found : {} ", payload.size());
        return payload
            .stream()
            .filter(f -> f.getJdClassName() != null && f.getJdClassName().length() > 0)
            .collect(Collectors.toList());

    }

    private List<NotifyTemplateField> queryNotifyTemplateField(String templateId) {
        return notifyTemplateFieldRepository.findByTemplateId(templateId);
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
     * @param poolDetails pool details to transform, not null
     * @return attendance time, nullable
     */
    private String getAttendTime(Pool poolDetails) throws ParseException {
        String uniquePoolAttendTime = uniquePoolService.getPoolAttendanceTime(poolDetails.getPoolNumber());

        if (uniquePoolAttendTime != null) {
            if (log.isTraceEnabled()) {
                log.trace("Attend time is set in unique pool, using pool attend time of {}", uniquePoolAttendTime);
            }

            Date attendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(uniquePoolAttendTime);
            uniquePoolAttendTime = timeformatterunique.format(attendTime);

            return uniquePoolAttendTime;
        } else {
            final String courtAttendTime = poolDetails.getCourt().getCourtAttendTime();
            if (log.isTraceEnabled()) {
                log.trace("Attend time is not set in unique pool, using court attend time of {}", courtAttendTime);
            }
            return courtAttendTime;
        }
    }
}
