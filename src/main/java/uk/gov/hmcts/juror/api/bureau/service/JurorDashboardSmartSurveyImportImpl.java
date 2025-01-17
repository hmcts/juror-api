package uk.gov.hmcts.juror.api.bureau.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.juror.api.bureau.domain.SurveyResponse;
import uk.gov.hmcts.juror.api.bureau.domain.SurveyResponseKey;
import uk.gov.hmcts.juror.api.bureau.domain.SurveyResponseRepository;
import uk.gov.hmcts.juror.api.config.SmartSurveyConfigurationProperties;
import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JurorDashboardSmartSurveyImportImpl implements BureauProcessService {

    private final AppSettingService appSetting;
    private final SmartSurveyConfigurationProperties smartSurveyConfigurationProperties;
    private final SurveyResponseRepository surveyResponseRepository;
    private Proxy proxy;
    private Boolean proxyEnabled = false;

    /**
     * Implements a specific job execution.
     * Process retrieval of satisfaction survey responses via the smart survey rest api.
     */
    @Override
    @Transactional
    public SchedulerServiceClient.Result process() {

        SimpleDateFormat dateFormatSurvey = new SimpleDateFormat();
        log.info("Smart Survey Processing : STARTED- {}", dateFormatSurvey.format(new Date()));

        //Get app settings from database
        int surveyDays = this.appSetting.getSmartSurveySummonsResponseDays();
        String surveyId = this.appSetting.getSmartSurveySummonsResponseSurveyId();
        String exportName = this.appSetting.getSmartSurveySummonsResponseExportName();

        //Set smart survey config parameters
        Boolean smartSurveyEnabled = smartSurveyConfigurationProperties.getEnabled();
        String smartSurveyExportsUrl = smartSurveyConfigurationProperties.getExportsUrl();
        String smartSurveyTokenSecret = smartSurveyConfigurationProperties.getSecret();
        String smartSurveyToken = smartSurveyConfigurationProperties.getToken();

        String smartSurveyCredentials = "?api_token={apiToken}&api_token_secret={apiTokenSecret}";
        String exportUrl;

        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusDays(surveyDays);

        List<SurveyResponse> surveyResponseList;
        int dbInsertCount = 0;
        int dbSkipCount = 0;
        int errorCount = 0;

        SmartSurveyConfigurationProperties.Proxy proxyProperties = smartSurveyConfigurationProperties.getProxy();

        // Log the settings retrieved from application.yml
        // these settings are required for the process to continue
        log.info("Smart Survey config enabled: {}", smartSurveyEnabled);
        log.info("Smart Survey config exports url: {}", smartSurveyExportsUrl);

        if (!smartSurveyEnabled) {
            log.info("Smart Survey data import disabled in application settings");
        } else {

            if (smartSurveyExportsUrl == null || smartSurveyExportsUrl.isEmpty()) {
                log.error("Smart Survey URL not set in config, unable to process survey responses");
                throw new IllegalStateException("smartsurvey exportsurl null or empty");
            }

            // Setup proxy using notify proxy settings in application.yml
            try {
                proxyEnabled = proxyProperties.getEnabled();
                log.info("Smart Survey proxy enabled: {}", proxyEnabled);

                String proxyHost;
                Proxy.Type proxyType;
                String proxyPort;
                if (proxyEnabled) {
                    proxyHost = proxyProperties.getHost();
                    proxyPort = proxyProperties.getPort();
                    proxyType = proxyProperties.getType();

                    log.info("Smart Survey proxy host: {}", proxyHost);
                    log.info("Smart Survey proxy port: {}", proxyPort);
                    log.info("Smart Survey proxy type: {}", proxyType);

                    proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                } else {
                    proxy = null;
                    log.info("Smart Survey proxy settings ignored");
                }

            } catch (Exception e) {
                log.error("Smart Survey unable to create proxy using application settings");
                proxy = null;
                errorCount++;
            }

            // Output settings retrieved from APP_SETTINGS table
            log.info("Smart Survey config survey Id: {}", surveyId);
            log.info("Smart Survey config export name: {}", exportName);
            log.info("Smart Survey config days: {}", surveyDays);
            log.info("Smart Survey system date: {}", currentDate);
            log.info("Smart Survey calculated start date: {}", startDate);

            Map<String, String> vars = new HashMap<String, String>();
            vars.put("surveyId", surveyId);
            vars.put("apiToken", smartSurveyToken);
            vars.put("apiTokenSecret", smartSurveyTokenSecret);

            exportUrl = getExportDownloadUrl(smartSurveyExportsUrl, vars);

            if (exportUrl == null || exportUrl.isEmpty()) {
                log.error("Unable to obtain export download url from smart survey api");
                throw new IllegalStateException("unable to obtain export download url from smart survey api");
            } else {
                exportUrl = exportUrl + smartSurveyCredentials;
                surveyResponseList = getExportData(exportUrl, vars, startDate, surveyId);
            }

            // Add records to table SURVEY_RESPONSE if not existing
            if (!surveyResponseList.isEmpty()) {

                log.info("Smart Survey records parsed (excluding header): {}", surveyResponseList.size());

                for (SurveyResponse objSurveyResponse : surveyResponseList) {

                    SurveyResponseKey objSurveyResponseKey = new SurveyResponseKey();
                    objSurveyResponseKey.setId(objSurveyResponse.getId());
                    objSurveyResponseKey.setSurveyId(objSurveyResponse.getSurveyId());

                    if (!surveyResponseRepository.existsById(objSurveyResponseKey)) {
                        try {
                            this.surveyResponseRepository.save(objSurveyResponse);
                            dbInsertCount++;
                        } catch (Exception e) {
                            errorCount++;
                            log.error("Error inserting survey record: {} - {}", e.getMessage(), objSurveyResponse);
                        }
                    } else {
                        // record already exists
                        dbSkipCount++;
                    }

                }

                log.info("Records inserted: {}", dbInsertCount);
                log.info("Records skipped: {}", dbSkipCount);
                log.info("Records with error: {}", errorCount);

            }

        }

        SchedulerServiceClient.Result.Status status = errorCount == 0
            ? SchedulerServiceClient.Result.Status.SUCCESS
            : SchedulerServiceClient.Result.Status.PARTIAL_SUCCESS;

        // log the results for Dynatrace
        log.info(
            "[JobKey: CRONBATCH_SMART_SURVEY_IMPORT]\n[{}]\nresult={},\nmetadata={records_inserted={},records_skipped={},error_count={}}",
            DATE_TIME_FORMATTER.format(LocalDateTime.now()),
            status,
            dbInsertCount,
            dbSkipCount,
            errorCount
        );

        log.info("Smart Survey Processing : FINISHED- {}", dateFormatSurvey.format(new Date()));

        return new SchedulerServiceClient.Result(status,
            errorCount == 0
                ? "Successfully loaded survey records"
                : "Error loading some survey records",
            Map.of(
                "RECORDS_INSERTED", String.valueOf(dbInsertCount),
                "RECORDS_SKIPPED", String.valueOf(dbSkipCount),
                "ERROR_COUNT", String.valueOf(errorCount)
            ));
    }


    /**
     * Obtain list of available exports from Smart Survey API.
     * Return url of the export matching the export name specificed in APP_SETTINGS.
     */
    private String getExportDownloadUrl(String smartSurveyUrl, Map<String, String> vars) {

        // Get the latest survey export URL from the smart survey API

        final String configExportName = this.appSetting.getSmartSurveySummonsResponseExportName();
        String smartSurveyExportList;
        RestTemplate restTemplate;
        HttpHeaders headers;

        log.info("Smart Survey request url: {}", smartSurveyUrl);
        log.info("Smart Survey config export name: {}", configExportName);

        try {
            if (proxyEnabled) {
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setProxy(proxy);
                restTemplate = new RestTemplate(requestFactory);
            } else {
                restTemplate = new RestTemplate();
            }

            headers = createHttpHeaders(vars.get("apiToken"), vars.get("apiTokenSecret"));
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>("body", headers);
            ResponseEntity<String> response = restTemplate.exchange(
                smartSurveyUrl,
                HttpMethod.GET,
                entity,
                String.class,
                vars
            );

            smartSurveyExportList = response.getBody();

        } catch (Exception e) {
            throw new IllegalStateException("call to smart survey API failed: " + e.getMessage());
        }

        smartSurveyExportList = "{surveyexportlist:" + smartSurveyExportList + "}";

        // Parse the returned list of exports
        JSONObject jsonObj = new JSONObject(smartSurveyExportList);
        JSONArray jsonArr = jsonObj.getJSONArray("surveyexportlist");

        // Find the latest survey export matching the name set in the config
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.length(); i++) {

            JSONObject obj = jsonArr.getJSONObject(i);
            String exportName = obj.getString("name");

            if (exportName.equals(configExportName)) {
                jsonList.add(obj);
            }
        }
        String exportUrl = null;
        //Get the Url for the latest export record - first item in list
        if (!jsonList.isEmpty()) {
            JSONObject obj = jsonList.get(0);
            log.debug("Smart Survey export details: {}", obj);
            exportUrl = obj.getString("href_download");
        }

        return exportUrl;

    }

    /**
     * Obtain survey export CSV data from smart survey API.
     * Parse the data and return records from the specified start date to current date.
     */
    private List<SurveyResponse> getExportData(String smartSurveyUrl, Map<String, String> vars,
                                               LocalDate extractStartDate, String surveyId) {

        List<SurveyResponse> surveyResponseList = new ArrayList<SurveyResponse>();
        String smartSurveyExportData;
        RestTemplate restTemplate;
        HttpHeaders headers;

        // get export data from Smart Survey API
        log.info("Smart Survey request url: {}", smartSurveyUrl);
        try {
            if (proxyEnabled) {
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setProxy(proxy);
                restTemplate = new RestTemplate(requestFactory);
            } else {
                restTemplate = new RestTemplate();
            }

            headers = createHttpHeaders(vars.get("apiToken"), vars.get("apiTokenSecret"));
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>("body", headers);
            ResponseEntity<String> response = restTemplate.exchange(
                smartSurveyUrl,
                HttpMethod.GET,
                entity,
                String.class,
                vars
            );

            smartSurveyExportData = response.getBody();

        } catch (Exception e) {
            throw new IllegalStateException("call to smart survey API failed: " + e.getMessage());
        }

        if (smartSurveyExportData == null || smartSurveyExportData.isEmpty()) {
            log.error("Unable to retrieve export data");
            throw new IllegalStateException("smartSurveyExportData null or empty");
        } else {

            // Parse the CSV data
            log.info("Smart Survey export data - length: {}", smartSurveyExportData.length());

            // Split the string data block into rows delimited by the CRLF characters
            String[] arrCsvRows = smartSurveyExportData.trim().split("\r\n");
            log.info("Smart Survey export data - records (including header): {}", arrCsvRows.length);

            // Process each data row
            // Note: the fist row (index 0) contains column headings
            // processing starts from the second row (index 1)
            if (arrCsvRows.length > 1) {
                for (int i = 1; i < arrCsvRows.length; i++) {

                    String csvRow = arrCsvRows[i];
                    String[] csvCols = csvRow.split(",");

                    // User Id
                    String surveyUserId = csvCols[0].trim().replaceAll("\"", "");

                    // User No
                    String surveyUserNoString = csvCols[1].trim().replaceAll("\"", "");
                    int surveyUserNo = Integer.parseInt(surveyUserNoString);

                    // Ended Date
                    String surveyEndDateString = csvCols[8].trim().replaceAll("\"", "");
                    LocalDate ldSurveyEndDate = null;
                    Date surveyEndDate = null;
                    try {
                        ldSurveyEndDate = LocalDate.parse(surveyEndDateString);
                        surveyEndDate = Date.from(ldSurveyEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    } catch (Exception e) {
                        log.error("Error parsing date value: {}", surveyEndDateString);
                    }

                    // Q4 - Satisfaction Desc
                    String surveySatisfactionDesc = csvCols[csvCols.length - 1].trim().replaceAll("\"", "");

                    // Populate list of survey response records
                    if (!ldSurveyEndDate.isBefore(extractStartDate)) {
                        SurveyResponseKey objSurveyResponseKey = new SurveyResponseKey();
                        objSurveyResponseKey.setId(surveyUserId);
                        objSurveyResponseKey.setSurveyId(surveyId);

                        SurveyResponse objSurveyResponse = new SurveyResponse();
                        objSurveyResponse.setId(surveyUserId);
                        objSurveyResponse.setSurveyId(surveyId);
                        objSurveyResponse.setUserNo(surveyUserNo);
                        objSurveyResponse.setSurveyResponseDate(surveyEndDate);
                        objSurveyResponse.setSatisfactionDesc(surveySatisfactionDesc);

                        surveyResponseList.add(objSurveyResponse);
                    }

                } // end process loop

            } else {
                log.info("Smart Survey export data - no data rows returned");
            }

        }

        return surveyResponseList;

    }

    private HttpHeaders createHttpHeaders(String user, String password) {
        // Create base64 encoded Basic Auth header

        String authString = user + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + encodedAuth);
        return headers;
    }

}







