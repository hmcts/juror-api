
package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.domain.CurrentlyDeferred;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.CurrentlyDeferredRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static uk.gov.hmcts.juror.api.testvalidation.DeferralMaintenanceValidation.validateDeferralMaintenanceOptions;

@SuppressWarnings("PMD.LawOfDemeter")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeferralMaintenanceControllerITest extends AbstractIntegrationTest {

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private CurrentlyDeferredRepository currentlyDeferredRepository;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @Autowired
    private JurorRepository jurorRepository;

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
    public void getDeferralOptionsForDates_bureauUser_happyPath() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(
            LocalDate.of(2023, 5, 30),
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 26)));

        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DeferralOptionsDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<DeferralOptionsDto.OptionSummaryDto> optionsSummary = responseBody.getDeferralPoolsSummary();

        /*
         * first option - requested deferral date is 2023-05-30 (Tuesday)
         * expect 2 active pools to be returned as available deferral options
         */
        DeferralOptionsDto.OptionSummaryDto firstOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst()
            .orElse(null);
        assertThat(firstOption)
            .as("Preferred deferral date of 30-05-2023 is a Tuesday, expect the deferral options to check "
                + "for week commencing 2023-05-29 (the previous Monday)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions = firstOption.getDeferralOptions();
        assertThat(deferralOptions.size())
            .as("Expect two available active pool options to be returned for the given week")
            .isEqualTo(2);
        // get first deferral option for w/c 2023-05-29, pool number: 415220401
        DeferralOptionsDto.DeferralOptionDto availablePool1 = deferralOptions.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220401"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool1)
            .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool1.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 5, 30));
        assertThat(availablePool1.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(2);
        assertThat(availablePool1.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.SURPLUS);
        // get second deferral option for w/c 2023-05-29, pool number: 415220502
        DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220502"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool2)
            .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool2.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(availablePool2.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(2);
        assertThat(availablePool2.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        /*
         * second option - requested deferral date is 2023-06-12 (Monday)
         * expect 1 active pool to be returned as an available deferral option
         */
        DeferralOptionsDto.OptionSummaryDto secondOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst()
            .orElse(null);
        assertThat(secondOption)
            .as("Preferred deferral date of 2023-06-12 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-12 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions2 = secondOption.getDeferralOptions();
        assertThat(deferralOptions2.size())
            .as("Expect one available active pool option to be returned for the given week")
            .isEqualTo(1);
        // get first deferral option for w/c 2023-06-12, pool number: 415220503
        DeferralOptionsDto.DeferralOptionDto availablePool3 = deferralOptions2.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220503"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool3)
            .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool3.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(availablePool3.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(4);
        assertThat(availablePool3.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        /*
         * third option - requested deferral date is 2023-06-26 (Monday)
         * expect no active pools to be available as deferral options - use deferral maintenance
         */
        DeferralOptionsDto.OptionSummaryDto thirdOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 26)))
            .findFirst()
            .orElse(null);
        assertThat(thirdOption)
            .as("Preferred deferral date of 2023-06-26 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-26 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions3 = thirdOption.getDeferralOptions();
        assertThat(deferralOptions3.size())
            .as("Expect one available deferral option to be returned for the given week")
            .isEqualTo(1);
        // get first deferral option for w/c 2023-06-26, no available pools - deferral maintenance
        DeferralOptionsDto.DeferralOptionDto deferralMaintenance = deferralOptions3.stream()
            .findFirst()
            .orElse(null);
        validateDeferralMaintenanceOptions(deferralMaintenance, 2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
    public void getDeferralOptionsForDates_courtUser_happyPath() throws Exception {
        final String jurorNumber = "555555557";
        final String login = "COURT_USER";
        final String bureauJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(
            LocalDate.of(2023, 5, 30),
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 26)));

        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DeferralOptionsDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<DeferralOptionsDto.OptionSummaryDto> optionsSummary = responseBody.getDeferralPoolsSummary();

        /*
         * first option - requested deferral date is 2023-05-30 (Tuesday)
         * expect 2 active pools to be returned as an available deferral option (both bureau owned)
         */
        DeferralOptionsDto.OptionSummaryDto firstOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst()
            .orElse(null);
        assertThat(firstOption)
            .as("Preferred deferral date of 30-05-2023 is a Tuesday, expect the deferral options to check "
                + "for week commencing 2023-05-29 (the previous Monday)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions = firstOption.getDeferralOptions();
        assertThat(deferralOptions.size())
            .as("Expect two available active pool options to be returned for the given week")
            .isEqualTo(2);
        // get first deferral option for w/c 2023-05-29, pool number: 415220401
        DeferralOptionsDto.DeferralOptionDto availablePool1 = deferralOptions.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220401"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool1)
            .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool1.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 5, 30));
        assertThat(availablePool1.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(0);
        assertThat(availablePool1.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.CONFIRMED);
        // get second deferral option for w/c 2023-05-29, pool number: 415220502
        DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220502"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool2)
            .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool2.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(availablePool2.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(1);
        assertThat(availablePool2.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.CONFIRMED);

        /*
         * second option - requested deferral date is 2023-06-12 (Monday)
         * expect 2 active pools to be returned as available deferral options (one bureau owned, one court owned)
         */
        DeferralOptionsDto.OptionSummaryDto secondOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst()
            .orElse(null);
        assertThat(secondOption)
            .as("Preferred deferral date of 2023-06-12 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-12 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions2 = secondOption.getDeferralOptions();
        assertThat(deferralOptions2.size())
            .as("Expect two available active pool options to be returned for the given week")
            .isEqualTo(2);
        // get first deferral option for w/c 2023-06-12, pool number: 415220503
        DeferralOptionsDto.DeferralOptionDto availablePool3 = deferralOptions2.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220503"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool3)
            .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool3.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(availablePool3.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(0);
        assertThat(availablePool3.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.CONFIRMED);
        // get second deferral option for w/c 2023-06-12, pool number: 415220504
        DeferralOptionsDto.DeferralOptionDto availablePool4 = deferralOptions2.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220504"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool4)
            .as("Expect a valid active pool with pool number 415220504 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool4.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(availablePool4.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(1);
        assertThat(availablePool4.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.CONFIRMED);
        /*
         * third option - requested deferral date is 2023-06-26 (Monday)
         * expect no active pools to be available as deferral options - use deferral maintenance
         */
        DeferralOptionsDto.OptionSummaryDto thirdOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 26)))
            .findFirst()
            .orElse(null);
        assertThat(thirdOption)
            .as("Preferred deferral date of 2023-06-26 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-26 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions3 = thirdOption.getDeferralOptions();
        assertThat(deferralOptions3.size())
            .as("Expect one available deferral option to be returned for the given week")
            .isEqualTo(1);
        // get first deferral option for w/c 2023-06-26, no available pools - deferral maintenance
        DeferralOptionsDto.DeferralOptionDto deferralMaintenance = deferralOptions3.stream()
            .findFirst()
            .orElse(null);
        validateDeferralMaintenanceOptions(deferralMaintenance, 1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
    public void getDeferralOptionsForDates_bureauUser_invalidAccess() throws Exception {
        final String jurorNumber = "555555557";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(LocalDate.of(2023, 5, 30),
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 26)));

        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
    public void getDeferralOptionsForDates_courtUser_invalidAccess() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "COURT_USER";
        final String bureauJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(LocalDate.of(2023, 6, 19),
            LocalDate.of(2023, 6, 26),
            LocalDate.of(2023, 7, 9)));

        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
    public void getDeferralOptionsForDates_noDates() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(LocalDate.of(2023, 5, 30),
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 26),
            LocalDate.of(2023, 7, 9)));
        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_getDeferralOptionsForDatesLocCode.sql"})
    public void getDeferralOptionsForDatesAndCourtLocationBureauUserHappyPath() throws Exception {
        final String jurorNumber = "555555551";
        final String locationCode = "415";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri =
            URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + locationCode + "/" + jurorNumber
                + "/deferral_dates");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(
            LocalDate.of(2023, 5, 30),
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 26)));

        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DeferralOptionsDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<DeferralOptionsDto.OptionSummaryDto> optionsSummary = responseBody.getDeferralPoolsSummary();

        /*
         * first option - requested deferral date is 2023-05-30 (Tuesday)
         * expect 2 active pools to be returned as available deferral options
         */
        DeferralOptionsDto.OptionSummaryDto firstOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst()
            .orElse(null);
        assertThat(firstOption)
            .as("Preferred deferral date of 30-05-2023 is a Tuesday, expect the deferral options to check "
                + "for week commencing 2023-05-29 (the previous Monday)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions = firstOption.getDeferralOptions();
        assertThat(deferralOptions.size())
            .as("Expect two available active pool options to be returned for the given week")
            .isEqualTo(2);
        // get first deferral option for w/c 2023-05-29, pool number: 415220401
        DeferralOptionsDto.DeferralOptionDto availablePool1 = deferralOptions.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220401"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool1)
            .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool1.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 5, 30));
        assertThat(availablePool1.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(2);
        assertThat(availablePool1.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.SURPLUS);
        // get second deferral option for w/c 2023-05-29, pool number: 415220502
        DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220502"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool2)
            .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool2.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(availablePool2.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(2);
        assertThat(availablePool2.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        /*
         * second option - requested deferral date is 2023-06-12 (Monday)
         * expect 1 active pool to be returned as an available deferral option
         */
        DeferralOptionsDto.OptionSummaryDto secondOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst()
            .orElse(null);
        assertThat(secondOption)
            .as("Preferred deferral date of 2023-06-12 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-12 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions2 = secondOption.getDeferralOptions();
        assertThat(deferralOptions2.size())
            .as("Expect one available active pool option to be returned for the given week")
            .isEqualTo(1);
        // get first deferral option for w/c 2023-06-12, pool number: 415220503
        DeferralOptionsDto.DeferralOptionDto availablePool3 = deferralOptions2.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220503"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool3)
            .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool3.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(availablePool3.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(4);
        assertThat(availablePool3.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        /*
         * third option - requested deferral date is 2023-06-26 (Monday)
         * expect no active pools to be available as deferral options - use deferral maintenance
         */
        DeferralOptionsDto.OptionSummaryDto thirdOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 26)))
            .findFirst()
            .orElse(null);
        assertThat(thirdOption)
            .as("Preferred deferral date of 2023-06-26 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-26 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions3 = thirdOption.getDeferralOptions();
        assertThat(deferralOptions3.size())
            .as("Expect one available deferral option to be returned for the given week")
            .isEqualTo(1);
        // get first deferral option for w/c 2023-06-26, no available pools - deferral maintenance
        DeferralOptionsDto.DeferralOptionDto deferralMaintenance = deferralOptions3.stream()
            .findFirst()
            .orElse(null);
        validateDeferralMaintenanceOptions(deferralMaintenance, 2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_getDeferralOptionsForDatesLocCode.sql"})
    public void getDeferralOptionsForDatesAndCourtLocationCourtUserHappyPath() throws Exception {
        final String jurorNumber = "555555557";
        final String locationCode = "415";
        final String login = "COURT_USER";
        final String bureauJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + locationCode + "/"
            + jurorNumber + "/deferral_dates");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(
            LocalDate.of(2023, 5, 30),
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 26)));

        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DeferralOptionsDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<DeferralOptionsDto.OptionSummaryDto> optionsSummary = responseBody.getDeferralPoolsSummary();

        /*
         * first option - requested deferral date is 2023-05-30 (Tuesday)
         * expect 2 active pools to be returned as an available deferral option (both bureau owned)
         */
        DeferralOptionsDto.OptionSummaryDto firstOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst()
            .orElse(null);
        assertThat(firstOption)
            .as("Preferred deferral date of 30-05-2023 is a Tuesday, expect the deferral options to check "
                + "for week commencing 2023-05-29 (the previous Monday)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions = firstOption.getDeferralOptions();
        assertThat(deferralOptions.size())
            .as("Expect two available active pool options to be returned for the given week")
            .isEqualTo(2);
        // get first deferral option for w/c 2023-05-29, pool number: 415220401
        DeferralOptionsDto.DeferralOptionDto availablePool1 = deferralOptions.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220401"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool1)
            .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool1.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 5, 30));
        assertThat(availablePool1.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(0);
        assertThat(availablePool1.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.CONFIRMED);
        // get second deferral option for w/c 2023-05-29, pool number: 415220502
        DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220502"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool2)
            .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool2.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(availablePool2.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(1);
        assertThat(availablePool2.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.CONFIRMED);

        /*
         * second option - requested deferral date is 2023-06-12 (Monday)
         * expect 2 active pools to be returned as available deferral options (one bureau owned, one court owned)
         */
        DeferralOptionsDto.OptionSummaryDto secondOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst()
            .orElse(null);
        assertThat(secondOption)
            .as("Preferred deferral date of 2023-06-12 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-12 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions2 = secondOption.getDeferralOptions();
        assertThat(deferralOptions2.size())
            .as("Expect two available active pool options to be returned for the given week")
            .isEqualTo(2);
        // get first deferral option for w/c 2023-06-12, pool number: 415220503
        DeferralOptionsDto.DeferralOptionDto availablePool3 = deferralOptions2.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220503"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool3)
            .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool3.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(availablePool3.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(0);
        assertThat(availablePool3.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.CONFIRMED);
        // get second deferral option for w/c 2023-06-12, pool number: 415220504
        DeferralOptionsDto.DeferralOptionDto availablePool4 = deferralOptions2.stream()
            .filter(pool -> pool.getPoolNumber().equals("415220504"))
            .findFirst()
            .orElse(null);
        assertThat(availablePool4)
            .as("Expect a valid active pool with pool number 415220504 to be returned as a deferral option")
            .isNotNull();
        assertThat(availablePool4.getServiceStartDate())
            .as("Expect correct Pool Request data to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(availablePool4.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(1);
        assertThat(availablePool4.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(PoolUtilisationDescription.CONFIRMED);
        /*
         * third option - requested deferral date is 2023-06-26 (Monday)
         * expect no active pools to be available as deferral options - use deferral maintenance
         */
        DeferralOptionsDto.OptionSummaryDto thirdOption = optionsSummary.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 26)))
            .findFirst()
            .orElse(null);
        assertThat(thirdOption)
            .as("Preferred deferral date of 2023-06-26 is a Monday, expect the deferral options to check "
                + "for week commencing 2023-06-26 (the same day)")
            .isNotNull();
        List<DeferralOptionsDto.DeferralOptionDto> deferralOptions3 = thirdOption.getDeferralOptions();
        assertThat(deferralOptions3.size())
            .as("Expect one available deferral option to be returned for the given week")
            .isEqualTo(1);
        // get first deferral option for w/c 2023-06-26, no available pools - deferral maintenance
        DeferralOptionsDto.DeferralOptionDto deferralMaintenance = deferralOptions3.stream()
            .findFirst()
            .orElse(null);
        validateDeferralMaintenanceOptions(deferralMaintenance, 1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
    public void getDeferralOptionsForDatesAndCourtLocationNoDates() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "BUREAU_USER";
        final String locationCode = "400";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + locationCode + "/"
            + jurorNumber + "/deferral_dates");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(LocalDate.of(2023, 5, 30),
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 26),
            LocalDate.of(2023, 7, 9)));
        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
    public void getDeferralOptionsForDates_tooManyDates() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();

        RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initPreferredDates.sql"})
    public void getPreferredDates_bureauUser_happyPath() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferral-dates/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);

        ResponseEntity<Object[]> response = template.exchange(requestEntity, Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Object[] responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<String> preferredDates =
            Arrays.stream(response.getBody()).map(String::valueOf).collect(Collectors.toList());

        assertThat(preferredDates).contains("2023-05-29");
        assertThat(preferredDates).contains("2023-06-12");
        assertThat(preferredDates).contains("2023-07-03");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initPreferredDates.sql"})
    public void getPreferredDates_courtUser_happyPath() throws Exception {
        final String jurorNumber = "555555557";
        final String login = "COURT_USER";
        final String bureauJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferral-dates/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);

        ResponseEntity<Object[]> response = template.exchange(requestEntity, Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Object[] responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<String> preferredDates =
            Arrays.stream(response.getBody()).map(String::valueOf).collect(Collectors.toList());

        assertThat(preferredDates).contains("2023-06-07");
        assertThat(preferredDates).contains("2023-07-03");
        assertThat(preferredDates).contains("2023-08-09");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initPreferredDates.sql"})
    public void getPreferredDates_courtUser_invalidAccess() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "COURT_USER";
        final String bureauJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferral-dates/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);

        ResponseEntity<Object> response = template.exchange(requestEntity, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initPreferredDates.sql"})
    public void getPreferredDates_bureauUser_noDigitalResponse() throws Exception {
        final String jurorNumber = "555555550";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferral-dates/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);

        ResponseEntity<Object> response = template.exchange(requestEntity, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initPreferredDates.sql"})
    public void getPreferredDates_bureauUser_noDates() throws Exception {
        final String jurorNumber = "555555552";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferral-dates/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);

        ResponseEntity<Object[]> response = template.exchange(requestEntity, Object[].class);


        Object[] responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        List<String> preferredDates =
            Arrays.stream(response.getBody()).map(String::valueOf).collect(Collectors.toList());
        assertThat(preferredDates.size()).isEqualTo(0);
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceProcessJurorTest.sql"})
    public void bureau_processJuror_activePool_paper() throws Exception {
        final String jurorNumber = "555555561";
        final String login = "BUREAU_USER";
        final String opticRef = "12345678";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/juror/defer/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto =
            createDeferralReasonRequestDtoToActivePool(ReplyMethod.PAPER);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // grab old record to verify the properties have been updated correctly updated
        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, false);

        assertThat(jurorPools.size()).isGreaterThan(0);
        verifyActivePoolOldRecord(jurorPools.get(0), deferralReasonRequestDto.getPoolNumber());

        // grab new record to verify it has been created and the properties have been updated correctly
        Juror newJurorRecord = JurorPoolUtils.getActiveJurorRecord(jurorPoolRepository, jurorNumber);

        verifyActiveJurorNewRecord(newJurorRecord,
            deferralReasonRequestDto.getPoolNumber(), deferralReasonRequestDto.getDeferralDate());
        assertThat(newJurorRecord.getOpticRef())
            .as(String.format("Expected optic ref to be %s", opticRef)).isEqualTo(opticRef);

        // check to make sure no record was created for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isFalse();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceChangeDateTest.sql"})
    public void changeDeferralDate_activePool_NotFound() throws Exception {
        final String jurorNumber = "090909090";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/change-deferral-date/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonRequestDtoToActivePool(null);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceChangeDateTest.sql"})
    public void changeDeferralDate_activePool_Forbidden() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "COURT_USER";
        final String courtJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/change-deferral-date/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
        DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonRequestDtoToActivePool(null);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceChangeDateTest.sql"})
    public void changeDate_deferralMaintenance_happyPath() throws Exception {
        final String jurorNumber = "555555552";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/change-deferral-date/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonDtoToDeferralMaintenance(null);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check to make sure record was created for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isTrue();

        assertThat(deferral.get().getDeferredTo()).isEqualTo(
            deferralReasonRequestDto.getDeferralDate());
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceChangeDateTest.sql"})
    public void changeDeferralDate_activePool_bureauUser() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/change-deferral-date/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonRequestDtoToActivePool(null);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceChangeDateTest.sql"})
    public void courtUser_changeDate_deferralMaintenance() throws Exception {
        final String jurorNumber = "555555558";
        final String login = "COURT_USER";
        final String courtJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/change-deferral-date/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
        DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonDtoToDeferralMaintenance(null);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check to make sure record was created for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isTrue();

        assertThat(deferral.get().getDeferredTo()).isEqualTo(
            deferralReasonRequestDto.getDeferralDate());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceChangeDateTest.sql"})
    public void courtUser_changeDate_activePool() throws Exception {
        final String jurorNumber = "555555558";
        final String login = "COURT_USER";
        final String courtJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/change-deferral-date/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
        DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonRequestDtoToActivePool(null);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // grab old record to verify the properties have been updated correctly
        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, false);
        assertThat(jurorPools.size()).isGreaterThan(0);

        verifyActivePoolOldRecordChangeDate(jurorPools.get(0), deferralReasonRequestDto.getPoolNumber());

        // grab new record to verify it has been created and the properties have been updated correctly
        jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);

        verifyActivePoolNewRecordChangeDate(jurorPools.get(0),
            deferralReasonRequestDto.getPoolNumber(), deferralReasonRequestDto.getDeferralDate());

        // check to make sure no record was created for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isFalse();
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceProcessJurorTest.sql"})
    public void bureauOwned_CourtUser_processJuror_activePool_paper() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "COURT_USER";
        final String bureauJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/juror/defer/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto =
            createDeferralReasonRequestDtoToActivePool(ReplyMethod.PAPER);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);


    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceProcessJurorTest.sql"})
    public void court_processJuror_activePool_digital() throws Exception {
        final String jurorNumber = "555555558";
        final String login = "COURT_USER";
        final String bureauJwt = createBureauJwt(login, "415");
        final String opticRef = "12345678";
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/juror/defer/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto =
            createDeferralReasonRequestDtoToActivePool(ReplyMethod.DIGITAL);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // grab old record to verify the properties have been updated correctly
        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, false);
        assertThat(jurorPools.size()).isGreaterThan(0);
        verifyActivePoolOldRecord(jurorPools.get(0), deferralReasonRequestDto.getPoolNumber());

        // grab new record to verify it has been created and the properties have been updated correctly
        Juror newJurorRecord = JurorPoolUtils.getActiveJurorRecord(jurorPoolRepository, jurorNumber);
        verifyActiveJurorNewRecord(newJurorRecord,
            deferralReasonRequestDto.getPoolNumber(), deferralReasonRequestDto.getDeferralDate());

        // check to make sure no record was created for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isFalse();

        assertThat(newJurorRecord.getOpticRef())
            .as(String.format("Expected optic ref to be %s", opticRef))
            .isEqualTo(opticRef);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceProcessJurorTest.sql"})
    public void bureau_processJuror_deferralMaintenance_paper() throws Exception {
        final String jurorNumber = "555555562";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/juror/defer/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto =
            createDeferralReasonDtoToDeferralMaintenance(ReplyMethod.PAPER);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check to make sure no record was created for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isTrue();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceProcessJurorTest.sql"})
    public void court_processJuror_deferralMaintenance_digital() throws Exception {
        final String jurorNumber = "555555559";
        final String login = "COURT_USER";
        final String bureauJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/juror/defer/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto =
            createDeferralReasonDtoToDeferralMaintenance(ReplyMethod.DIGITAL);
        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check to make sure no record was created for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isTrue();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJurorTest.sql"})
    public void allocateJurorFromDeferralMaintenance_singleJuror() throws Exception {
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/allocate-jurors-to-pool");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        String poolNumber = "415220503";
        String opticRef = "12345678";
        List<String> jurorNumbers = Collections.singletonList("555555557");
        DeferralAllocateRequestDto deferralAllocateRequestDto = createDeferralAllocateRequestDto(
            poolNumber, jurorNumbers);

        RequestEntity<DeferralAllocateRequestDto> requestEntity = new RequestEntity<>(deferralAllocateRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check to make sure the juror has been removed from maintenance
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById("555555557");
        assertThat(deferral.isPresent())
            .as("Expected juror to be removed from deferral maintenance").isFalse();

        // add logic to check to see the content of the new pool member
        List<JurorPool> jurorPools = jurorPoolRepository.findByPoolPoolNumberAndWasDeferredAndIsActive(poolNumber,
            true, true);

        assertThat(jurorPools.size()).as("Expected size to be one for the new pool member record").isEqualTo(1);

        // check to make sure the new pool members record has been updated correctly
        JurorPool jurorPool = jurorPools.get(0);
        Juror juror = jurorPool.getJuror();
        assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo("Responded");
        assertThat(jurorPool.getDeferralDate()).isNull();
        assertThat(jurorPool.getIsActive()).isTrue();
        assertThat(juror.isResponded()).isTrue();
        assertThat(juror.getNoDefPos()).isEqualTo(1);
        LocalDate expectedStartDate = LocalDate.now().plusWeeks(1);
        assertThat(jurorPool.getNextDate()).isEqualTo(expectedStartDate);
        assertThat(juror.getOpticRef())
            .as(String.format("Expected optic ref to be %s", opticRef)).isEqualTo(opticRef);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJurorTest.sql"})
    public void allocateJurorFromDeferralMaintenance_multipleJurors() throws Exception {
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/allocate-jurors-to-pool");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        String poolNumber = "415220503";
        List<String> jurorNumbers = Arrays.asList("555555557", "555555560", "555555559");
        DeferralAllocateRequestDto deferralAllocateRequestDto = createDeferralAllocateRequestDto(
            poolNumber, jurorNumbers);

        RequestEntity<DeferralAllocateRequestDto> requestEntity = new RequestEntity<>(deferralAllocateRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check to make sure the jurors has been removed from maintenance
        for (String jurorNumber : jurorNumbers) {
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
            assertThat(deferral.isPresent())
                .as("Expected juror to be removed from deferral maintenance").isFalse();
        }

        // add logic to check to see the content of the new pool member
        List<JurorPool> jurorPools = jurorPoolRepository.findByPoolPoolNumberAndWasDeferredAndIsActive(poolNumber,
            true, true);

        assertThat(jurorPools.size()).as("Expected size to be three for the juror pool records").isEqualTo(3);

        // check to make sure the new pool members record has been updated correctly
        for (JurorPool jurorPool : jurorPools) {
            Juror juror = jurorPool.getJuror();
            assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo("Responded");
            assertThat(jurorPool.getDeferralDate()).isNull();
            assertThat(jurorPool.getIsActive()).isTrue();
            assertThat(juror.isResponded()).isTrue();
            assertThat(juror.getNoDefPos()).isEqualTo(1);
            LocalDate expectedStartDate = LocalDate.now().plusWeeks(1);
            assertThat(jurorPool.getNextDate()).isEqualTo(expectedStartDate);
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJurorTest.sql"})
    public void allocateJurorFromDeferralMaintenance_singleJuror_courtUser_poolRequestNotFound() throws Exception {
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/allocate-jurors-to-pool");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        String poolNumber = "222222222";
        List<String> jurorNumbers = Collections.singletonList("555555557");
        DeferralAllocateRequestDto deferralAllocateRequestDto = createDeferralAllocateRequestDto(
            poolNumber, jurorNumbers);

        RequestEntity<DeferralAllocateRequestDto> requestEntity = new RequestEntity<>(deferralAllocateRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJurorTest.sql"})
    public void allocateJurorFromDeferralMaintenance_singleJuror_courtUser_poolMemberInvalidAccess() throws Exception {
        final String login = "COURT_USER";
        final String courtJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/allocate-jurors-to-pool");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
        String poolNumber = "415220503";
        List<String> jurorNumbers = Collections.singletonList("555555557");
        DeferralAllocateRequestDto deferralAllocateRequestDto = createDeferralAllocateRequestDto(
            poolNumber, jurorNumbers);

        RequestEntity<DeferralAllocateRequestDto> requestEntity = new RequestEntity<>(deferralAllocateRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJuror_courtLocationCode.sql"})
    public void getDeferralsByCourtLocationCode_bureauUser() throws Exception {
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/415");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);
        ResponseEntity<DeferralListDto> response = template.exchange(requestEntity, DeferralListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DeferralListDto dto = response.getBody();
        assertThat(dto.getDeferrals().size()).as("Expected size to be 2").isEqualTo(2);
        //Todo may want to make the following test more determinate as order is not guaranteed
        assertThat(dto.getDeferrals().get(0).getJurorNumber()).containsAnyOf("555555557", "555555559");
        assertThat(dto.getDeferrals().get(1).getJurorNumber()).containsAnyOf("555555557", "555555559");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJuror_courtLocationCode.sql"})
    public void getDeferralsByCourtLocationCode_courtUser() throws Exception {
        final String login = "COURT_USER";
        final String courtJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferrals/415");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);
        ResponseEntity<DeferralListDto> response = template.exchange(requestEntity, DeferralListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DeferralListDto dto = response.getBody();
        assertThat(dto.getDeferrals().size()).as("Expected size to be 2").isEqualTo(2);
        //Todo may want to make the following test more determinate as order is not guaranteed
        assertThat(dto.getDeferrals().get(0).getJurorNumber()).containsAnyOf("123456789", "444444444");
        assertThat(dto.getDeferrals().get(1).getJurorNumber()).containsAnyOf("123456789", "444444444");
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJurorTest.sql"})
    public void findActivePoolsForCourtLocation_nullMaxDate() throws Exception {
        final String login = "BUREAU_USER";
        final String courtJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/available-pools/415");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);
        ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DeferralOptionsDto dto = response.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getDeferralPoolsSummary().size()).isEqualTo(1);
        assertThat(dto.getDeferralPoolsSummary().get(0).getDeferralOptions().size()).isEqualTo(1);
        assertThat(dto.getDeferralPoolsSummary().get(0).getWeekCommencing())
            .isEqualTo(DateUtils.getStartOfWeekFromDate(LocalDate.now().plusWeeks(1)));
        assertThat(dto.getDeferralPoolsSummary().get(0).getDeferralOptions().get(0).getUtilisation())
            .isEqualTo(4);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenance_DeleteDeferralTest.sql"})
    public void deleteDeferredRecord_happyPath() throws Exception {
        String jurorNumber = "123456789";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/delete-deferral/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.DELETE, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // check to make sure no record was deleted for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isFalse();

        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(
            "123456789", "415220502", true).get();
        assertThat(jurorPool).isNotNull();

        Juror juror = jurorPool.getJuror();

        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(IJurorStatus.RESPONDED);
        assertThat(juror.getNoDefPos()).isEqualTo(0);
    }

    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_postponeJuror.sql"})
    public void bureau_postponeJuror_activePool() throws Exception {
        final String jurorNumber = "555555551";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/juror/postpone/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto = new DeferralReasonRequestDto();

        String newPool = "415220503";
        deferralReasonRequestDto.setDeferralDate(LocalDate.now().plusDays(10));
        deferralReasonRequestDto.setPoolNumber(newPool);
        deferralReasonRequestDto.setExcusalReasonCode("P");

        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check to ensure pool member was postponed and current record logically deleted
        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(
            jurorNumber, false);

        assertThat(jurorPools.size()).as("Expected size to be one for the postponed pool member record").isEqualTo(1);
        JurorPool jurorPool = jurorPools.get(0);
        assertThat(jurorPool.getStatus().getStatus()).as("Expect Status to be deferred (7)").isEqualTo(7);
        assertThat(jurorPool.getDeferralCode()).as("Expect reason code to be postponement (P)").isEqualTo("P");
        assertThat(jurorPool.getPostpone()).as("Expect pool member to be postponed").isTrue();

        // check to ensure postponed pool member was created in new pool
        jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThat(jurorPools.size()).as("Expected size to be one for the new pool member record").isEqualTo(1);
        assertThat(jurorPools.get(0).getJurorNumber()).isEqualTo(jurorNumber);

        // check to make sure the juror has not been added to deferral maintenance
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent())
            .as("Expect juror not to be in deferral maintenance").isFalse();

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenance_DeleteDeferralTest.sql"})
    public void deleteDeferredRecord_jurorNotFound() throws Exception {
        String jurorNumber = "000000000";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/delete-deferral/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.DELETE, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // check to make sure no record was not deleted for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById("123456789");
        assertThat(deferral.isPresent()).isTrue();

        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(
            "123456789", "415220502", true).get();
        assertThat(jurorPool).isNotNull();

        Juror juror = jurorPool.getJuror();
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(IJurorStatus.DEFERRED);
        assertThat(juror.getNoDefPos()).isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenance_DeleteDeferralTest.sql"})
    public void deleteDeferredRecord_wrongAccess() throws Exception {
        String jurorNumber = "123456789";
        final String login = "COURT_USER";
        final String courtJwt = createBureauJwt(login, "415");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/delete-deferral/" + jurorNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.DELETE, uri);
        ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // check to make sure no record was not deleted for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isTrue();

        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(
            jurorNumber, "415220502", true).get();
        assertThat(jurorPool).isNotNull();
        Juror juror = jurorPool.getJuror();
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(IJurorStatus.DEFERRED);
        assertThat(juror.getNoDefPos()).isEqualTo(1);
    }


    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_postponeJuror.sql"})
    public void bureau_postponeJuror_deferralMaintenance() throws Exception {
        final String jurorNumber = "555555552";
        final String login = "BUREAU_USER";
        final String bureauJwt = createBureauJwt(login, "400");
        final URI uri = URI.create("/api/v1/moj/deferral-maintenance/juror/postpone/" + jurorNumber);
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(7); // deferred status

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        DeferralReasonRequestDto deferralReasonRequestDto = new DeferralReasonRequestDto();

        deferralReasonRequestDto.setDeferralDate(LocalDate.now().plusDays(20));
        deferralReasonRequestDto.setExcusalReasonCode("P");

        RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
            DeferralReasonRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check to ensure pool member was postponed but still active
        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThat(jurorPools.size()).as("Expected size to be one for the postponed pool member record").isEqualTo(1);
        JurorPool jurorPool = jurorPools.get(0);

        assertThat(jurorPool.getStatus().getStatus()).as("Expect Status to be deferred (7)").isEqualTo(7);
        assertThat(jurorPool.getDeferralCode()).as("Expect reason code to be postponement (P)").isEqualTo("P");
        assertThat(jurorPool.getPostpone()).as("Expect pool member to be postponed").isTrue();

        // check to make sure a record was created for the deferral maintenance table
        Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
        assertThat(deferral.isPresent()).isTrue();
    }

    private DeferralReasonRequestDto createDeferralReasonRequestDtoToActivePool(ReplyMethod replyMethod) {
        DeferralReasonRequestDto dto = new DeferralReasonRequestDto();
        dto.setReplyMethod(replyMethod);
        dto.setExcusalReasonCode("A");
        dto.setPoolNumber("415220504");
        dto.setDeferralDate(LocalDate.of(2023, 6, 12));
        return dto;
    }

    private DeferralReasonRequestDto createDeferralReasonDtoToDeferralMaintenance(ReplyMethod replyMethod) {
        DeferralReasonRequestDto dto = new DeferralReasonRequestDto();
        dto.setReplyMethod(replyMethod);
        dto.setExcusalReasonCode("A");
        dto.setDeferralDate(LocalDate.of(2023, 8, 1));
        return dto;
    }

    private DeferralAllocateRequestDto createDeferralAllocateRequestDto(String poolNumber,
                                                                        List<String> jurorNumbers) {
        DeferralAllocateRequestDto dto = new DeferralAllocateRequestDto();
        dto.setJurors(jurorNumbers);
        dto.setPoolNumber(poolNumber);
        return dto;
    }

    private void verifyActivePoolOldRecord(JurorPool jurorPool, String poolNumber) {
        Juror juror = jurorPool.getJuror();
        assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo("Deferred");
        assertThat(jurorPool.getDeferralDate()).isNotNull();
        assertThat(jurorPool.getNextDate()).isNull();
        assertThat(juror.isResponded()).isTrue();
        assertThat(jurorPool.getDeferralCode()).isEqualTo("A");
        assertThat(jurorPool.getIsActive()).isFalse();
        assertThat(juror.getNoDefPos()).isEqualTo(1);
        assertThat(jurorPool.getWasDeferred()).isNull();
    }

    private void verifyActiveJurorNewRecord(Juror juror, String poolNumber, LocalDate startDate) {

        List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(juror.getJurorNumber(),
            true);
        assertThat(jurorPools.size()).isEqualTo(1);
        JurorPool jurorPool = jurorPools.get(0);
        assertThat(jurorPool.getPoolNumber()).isEqualTo(poolNumber);
        assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo("Responded");
        assertThat(jurorPool.getDeferralDate()).isNull();
        assertThat(jurorPool.getNextDate()).isEqualTo(startDate);
        assertThat(juror.isResponded()).isTrue();
        assertThat(jurorPool.getDeferralCode()).isNull();
        assertThat(jurorPool.getIsActive()).isTrue();
        assertThat(juror.getNoDefPos()).isEqualTo(1);
        assertThat(jurorPool.getWasDeferred()).isTrue();

    }

    private void verifyActivePoolOldRecordChangeDate(JurorPool jurorPool, String poolNumber) {
        assertThat(jurorPool.getPoolNumber()).isNotEqualTo(poolNumber);
        assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo("Deferred");
        assertThat(jurorPool.getDeferralDate()).isNotNull();
        assertThat(jurorPool.getNextDate()).isNull();
        Juror juror = jurorPool.getJuror();
        assertThat(juror.isResponded()).isTrue();
        assertThat(jurorPool.getDeferralCode()).isEqualTo("A");
        assertThat(jurorPool.getIsActive()).isFalse();

        //This is not possible to verify now as there is one juror record
        //assertThat(juror.getNoDefPos()).isEqualTo(0);

        assertThat(jurorPool.getWasDeferred()).isNull();
    }

    private void verifyActivePoolNewRecordChangeDate(JurorPool jurorPool, String poolNumber, LocalDate startDate) {
        assertThat(jurorPool.getPoolNumber()).isEqualTo(poolNumber);
        assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo("Responded");
        assertThat(jurorPool.getDeferralDate()).isNull();
        assertThat(jurorPool.getNextDate()).isEqualTo(startDate);
        Juror juror = jurorPool.getJuror();
        assertThat(juror.isResponded()).isTrue();
        assertThat(jurorPool.getDeferralCode()).isNull();
        assertThat(jurorPool.getIsActive()).isTrue();
        assertThat(juror.getNoDefPos()).isEqualTo(0);
        assertThat(juror.getExcusalDate()).isNull();
        assertThat(jurorPool.getWasDeferred()).isTrue();
    }

    @Override
    protected String createBureauJwt(String login, String owner) throws Exception {
        return mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login(login)
            .staff(BureauJWTPayload.Staff.builder()
                .name("Test User")
                .active(1)
                .rank(1)
                .courts(List.of("415", "400"))
                .build())
            .daysToExpire(89)
            .owner(owner)
            .build());
    }
}