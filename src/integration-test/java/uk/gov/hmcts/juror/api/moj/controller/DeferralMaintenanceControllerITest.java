
package uk.gov.hmcts.juror.api.moj.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.ProcessJurorPostponementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.CurrentlyDeferred;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.CurrentlyDeferredRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription.CONFIRMED;
import static uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription.NEEDED;
import static uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription.SURPLUS;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.DAY_ALREADY_EXISTS;
import static uk.gov.hmcts.juror.api.testvalidation.DeferralMaintenanceValidation.validateDeferralMaintenanceOptions;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: /api/v1/moj/deferral-maintenance/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeferralMaintenanceControllerITest extends AbstractIntegrationTest {

    static final String JUROR_000000000 = "000000000";
    static final String JUROR_123456789 = "123456789";
    static final String JUROR_555555551 = "555555551";
    static final String JUROR_555555552 = "555555552";
    static final String JUROR_555555557 = "555555557";
    static final String JUROR_555555558 = "555555558";
    static final String JUROR_555555559 = "555555559";
    static final String JUROR_555555560 = "555555560";
    static final String JUROR_555555561 = "555555561";
    static final String JUROR_555555562 = "555555562";
    static final String JUROR_090909090 = "090909090";

    static final String POOL_222222222 = "222222222";
    static final String POOL_415220502 = "415220502";
    static final String POOL_415220503 = "415220503";

    static final String OPTIC_REF_12345678 = "12345678";

    static final String OWNER_400 = "400"; // bureau owner
    static final String OWNER_415 = "415";
    static final String BUREAU_USER = "BUREAU_USER";
    static final String COURT_USER = "COURT_USER";

    static final String RESPONDED = "Responded";

    static final String EXPECT_POOL_UTILISATION = "Expect Pool Utilisation stats to be calculated for the given pool "
        + "request";

    private final TestRestTemplate template;
    private final CurrentlyDeferredRepository currentlyDeferredRepository;
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorRepository jurorRepository;
    private final PoolRequestRepository poolRequestRepository;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("POST Get deferral options for dates")
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
    class GetDeferralOptionsForDates {
        static final String URL_PREFIX = "/api/v1/moj/deferral-maintenance/available-pools/";

        @Test
        void testGDeferralOptionsForDatesBureauUserHappyPath() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
            deferralDatesRequestDto.setDeferralDates(Arrays.asList(
                LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555551));

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
                .as("Preferred deferral date of 30-05-2023 is a Tuesday, expect the deferral options to "
                    + "check for week commencing 2023-05-29 (the previous Monday)")
                .isNotNull();

            List<DeferralOptionsDto.DeferralOptionDto> deferralOptions = firstOption.getDeferralOptions();
            assertThat(deferralOptions.size())
                .as("Expect two available active pool options to be returned for the given week")
                .isEqualTo(2);
            // get first deferral option for w/c 2023-05-29, pool number: 415220401
            DeferralOptionsDto.DeferralOptionDto availablePool1 = deferralOptions.stream()
                .filter(pool -> "415220401".equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool1)
                .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral "
                    + "option")
                .isNotNull();
            assertThat(availablePool1.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 5, 30));
            assertThat(availablePool1.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(2);
            assertThat(availablePool1.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(SURPLUS);
            // get second deferral option for w/c 2023-05-29, pool number: 415220502
            DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
                .filter(pool -> POOL_415220502.equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool2)
                .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral "
                    + "option")
                .isNotNull();
            assertThat(availablePool2.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 1));
            assertThat(availablePool2.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(2);
            assertThat(availablePool2.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(NEEDED);

            /*
             * second option - requested deferral date is 2023-06-12 (Monday)
             * expect 1 active pool to be returned as an available deferral option
             */
            DeferralOptionsDto.OptionSummaryDto secondOption = optionsSummary.stream()
                .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
                .findFirst()
                .orElse(null);
            assertThat(secondOption)
                .as("Preferred deferral date of 2023-06-12 is a Monday, expect the deferral options to check"
                    + " for week commencing 2023-06-12 (the same day)")
                .isNotNull();
            List<DeferralOptionsDto.DeferralOptionDto> deferralOptions2 = secondOption.getDeferralOptions();
            assertThat(deferralOptions2.size())
                .as("Expect one available active pool option to be returned for the given week")
                .isEqualTo(1);
            // get first deferral option for w/c 2023-06-12, pool number: 415220503
            DeferralOptionsDto.DeferralOptionDto availablePool3 = deferralOptions2.stream()
                .filter(pool -> POOL_415220503.equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool3)
                .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral "
                    + "option")
                .isNotNull();
            assertThat(availablePool3.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 12));
            assertThat(availablePool3.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(4);
            assertThat(availablePool3.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(NEEDED);

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
            assertThat(deferralOptions3.size()).as(EXPECT_POOL_UTILISATION).isEqualTo(1);
            // get first deferral option for w/c 2023-06-26, no available pools - deferral maintenance
            DeferralOptionsDto.DeferralOptionDto deferralMaintenance = deferralOptions3.stream()
                .findFirst()
                .orElse(null);
            validateDeferralMaintenanceOptions(deferralMaintenance, 2);
        }

        @Test
        void testGDeferralOptionsForDatesCourtUserHappyPath() {
            final String bureauJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
            deferralDatesRequestDto.setDeferralDates(Arrays.asList(
                LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555557));

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
                .filter(pool -> "415220401".equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool1)
                .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool1.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 5, 30));
            assertThat(availablePool1.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(0);
            assertThat(availablePool1.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(CONFIRMED);
            // get second deferral option for w/c 2023-05-29, pool number: 415220502
            DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
                .filter(pool -> POOL_415220502.equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool2)
                .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool2.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 1));
            assertThat(availablePool2.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(1);
            assertThat(availablePool2.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(CONFIRMED);

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
                .filter(pool -> POOL_415220503.equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool3)
                .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool3.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 12));
            assertThat(availablePool3.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(0);
            assertThat(availablePool3.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(CONFIRMED);
            // get second deferral option for w/c 2023-06-12, pool number: 415220504
            DeferralOptionsDto.DeferralOptionDto availablePool4 = deferralOptions2.stream()
                .filter(pool -> "415220504".equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool4)
                .as("Expect a valid active pool with pool number 415220504 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool4.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 12));
            assertThat(availablePool4.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(1);
            assertThat(availablePool4.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(CONFIRMED);
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
        void testGDeferralOptionsForDatesBureauUserInvalidAccess() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
            deferralDatesRequestDto.setDeferralDates(Arrays.asList(LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555557));

            ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void testGDeferralOptionsForDatesCourtUserInvalidAccess() {
            final String bureauJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
            deferralDatesRequestDto.setDeferralDates(Arrays.asList(LocalDate.of(2023, 6, 19),
                LocalDate.of(2023, 6, 26),
                LocalDate.of(2023, 7, 9)));

            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555551));

            ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void testGDeferralOptionsForDatesNoDates() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
            deferralDatesRequestDto.setDeferralDates(Arrays.asList(LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26),
                LocalDate.of(2023, 7, 9)));
            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555551));

            ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
            assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initDeferralOptions.sql"})
        void testGDeferralOptionsForDatesTooManyDates() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();

            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555551));

            ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
            assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        }
    }

    @SuppressWarnings("GrazieInspection")
    @Nested
    @DisplayName("POST Get deferral options for dates and court location")
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_getDeferralOptionsForDatesLocCode.sql"})
    class GetDeferralOptionsForDatesAndCourtLocation {
        static final String URL_PREFIX = "/api/v1/moj/deferral-maintenance/available-pools/";
        static final String URL_POSTFIX = "/deferral_dates";

        @Test
        void testGDeferralOptionsForDatesAndCourtLocationBureauUserHappyPath() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
            deferralDatesRequestDto.setDeferralDates(Arrays.asList(
                LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + OWNER_415 + "/" + JUROR_555555551 + URL_POSTFIX));

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
                .filter(pool -> "415220401".equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool1)
                .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool1.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 5, 30));
            assertThat(availablePool1.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(2);
            assertThat(availablePool1.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(SURPLUS);
            // get second deferral option for w/c 2023-05-29, pool number: 415220502
            DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
                .filter(pool -> POOL_415220502.equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool2)
                .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool2.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 1));
            assertThat(availablePool2.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(2);
            assertThat(availablePool2.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(NEEDED);

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
                .filter(pool -> POOL_415220503.equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool3)
                .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool3.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 12));
            assertThat(availablePool3.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(4);
            assertThat(availablePool3.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(NEEDED);

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
        void testGDeferralOptionsForDatesAndCourtLocationCourtUserHappyPath() {
            final String bureauJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
            deferralDatesRequestDto.setDeferralDates(Arrays.asList(
                LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26)));

            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + OWNER_415 + "/" + JUROR_555555557 + URL_POSTFIX));

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
                .filter(pool -> "415220401".equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool1)
                .as("Expect a valid active pool with pool number 415220401 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool1.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 5, 30));
            assertThat(availablePool1.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(0);
            assertThat(availablePool1.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(CONFIRMED);
            // get second deferral option for w/c 2023-05-29, pool number: 415220502
            DeferralOptionsDto.DeferralOptionDto availablePool2 = deferralOptions.stream()
                .filter(pool -> POOL_415220502.equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool2)
                .as("Expect a valid active pool with pool number 415220502 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool2.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 1));
            assertThat(availablePool2.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(1);
            assertThat(availablePool2.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(CONFIRMED);

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
                .filter(pool -> POOL_415220503.equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool3)
                .as("Expect a valid active pool with pool number 415220503 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool3.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 12));
            assertThat(availablePool3.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(0);
            assertThat(availablePool3.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(CONFIRMED);
            // get second deferral option for w/c 2023-06-12, pool number: 415220504
            DeferralOptionsDto.DeferralOptionDto availablePool4 = deferralOptions2.stream()
                .filter(pool -> "415220504".equals(pool.getPoolNumber()))
                .findFirst()
                .orElse(null);
            assertThat(availablePool4)
                .as("Expect a valid active pool with pool number 415220504 to be returned as a deferral option")
                .isNotNull();
            assertThat(availablePool4.getServiceStartDate())
                .as("Expect correct Pool Request data to be mapped in to the DTO")
                .isEqualTo(LocalDate.of(2023, 6, 12));
            assertThat(availablePool4.getUtilisation()).as(EXPECT_POOL_UTILISATION).isEqualTo(1);
            assertThat(availablePool4.getUtilisationDescription()).as(EXPECT_POOL_UTILISATION).isEqualTo(CONFIRMED);
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
        void testGDeferralOptionsForDatesAndCourtLocationNoDates() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
            deferralDatesRequestDto.setDeferralDates(Arrays.asList(LocalDate.of(2023, 5, 30),
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 26),
                LocalDate.of(2023, 7, 9)));
            RequestEntity<DeferralDatesRequestDto> requestEntity = new RequestEntity<>(deferralDatesRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + OWNER_400 + "/" + JUROR_555555551 + URL_POSTFIX));

            ResponseEntity<DeferralOptionsDto> response = template.exchange(requestEntity, DeferralOptionsDto.class);
            assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("POST Get preferred dates")
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_initPreferredDates.sql"})
    class GetPreferredDates {
        static final String URL_PREFIX = "/api/v1/moj/deferral-maintenance/deferral-dates/";

        @Test
        void testGPreferredDatesBureauUserHappyPath() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET,
                URI.create(URL_PREFIX + JUROR_555555551));

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
        void testGPreferredDatesCourtUserHappyPath() {
            final String bureauJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET,
                URI.create(URL_PREFIX + JUROR_555555557));

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
        void testGPreferredDatesCourtUserInvalidAccess() {
            final String bureauJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET,
                URI.create(URL_PREFIX + JUROR_555555551));

            ResponseEntity<Object> response = template.exchange(requestEntity, Object.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void testGPreferredDatesBureauUserNoDigitalResponse() {
            final String jurorNumber = "555555550";
            final String login = "BUREAU_USER";
            final String bureauJwt = createJwt(login, "400");
            final URI uri = URI.create("/api/v1/moj/deferral-maintenance/deferral-dates/" + jurorNumber);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);

            ResponseEntity<Object> response = template.exchange(requestEntity, Object.class);

            assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        }

        @Test
        void testGPreferredDatesBureauUserNoDates() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<List<String>> requestEntity = new RequestEntity<>(httpHeaders, GET,
                URI.create(URL_PREFIX + JUROR_555555552));

            ResponseEntity<Object[]> response = template.exchange(requestEntity, Object[].class);


            Object[] responseBody = response.getBody();
            assertThat(responseBody).isNotNull();

            List<String> preferredDates =
                Arrays.stream(response.getBody()).map(String::valueOf).collect(Collectors.toList());
            assertThat(preferredDates.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("POST Process juror deferral")
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceProcessJurorTest.sql"})
    class ProcessJurorDeferral {
        static final String URL_PREFIX = "/api/v1/moj/deferral-maintenance/juror/defer/";

        @Test
        void bureauProcessJurorActivePoolPaper() {

            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            DeferralReasonRequestDto deferralReasonRequestDto =
                createDeferralReasonRequestDtoToActivePool(ReplyMethod.PAPER);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555561));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // grab old record to verify the properties have been updated correctly updated
            List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_555555561,
                false);

            assertThat(jurorPools.size()).isGreaterThan(0);
            verifyActivePoolOldRecord(jurorPools.get(0));

            // grab new record to verify it has been created and the properties have been updated correctly
            Juror newJurorRecord = JurorPoolUtils.getActiveJurorRecord(jurorPoolRepository, JUROR_555555561);

            verifyActiveJurorNewRecord(newJurorRecord,
                deferralReasonRequestDto.getPoolNumber(), deferralReasonRequestDto.getDeferralDate());
            assertThat(newJurorRecord.getOpticRef())
                .as(String.format("Expected optic ref to be %s", OPTIC_REF_12345678)).isEqualTo(OPTIC_REF_12345678);

            // check to make sure no record was created for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555561);
            assertThat(deferral.isPresent()).isFalse();
        }

        @Test
        void bureauProcessJurorAlreadyInBulkPrintForGivenDate() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ResponseEntity<String> response = template.exchange(new RequestEntity<>(
                createDeferralReasonRequestDtoToActivePool(ReplyMethod.PAPER),
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555552)), String.class);

            assertThat(response.getStatusCode()).as("HTTP status unprocessable entity expected")
                .isEqualTo(UNPROCESSABLE_ENTITY);

            assertBusinessRuleViolation(response, "Letter already exists in bulk print queue for the same day",
                DAY_ALREADY_EXISTS);
        }


        @Test
        void bureauOwnedCourtUserProcessJurorActivePoolPaper() {
            final String bureauJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            DeferralReasonRequestDto deferralReasonRequestDto =
                createDeferralReasonRequestDtoToActivePool(ReplyMethod.PAPER);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555551));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void courtProcessJurorActivePoolDigital() {
            final String bureauJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            DeferralReasonRequestDto deferralReasonRequestDto =
                createDeferralReasonRequestDtoToActivePool(ReplyMethod.DIGITAL);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555558));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // grab old record to verify the properties have been updated correctly
            List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_555555558,
                false);
            assertThat(jurorPools.size()).isGreaterThan(0);
            verifyActivePoolOldRecord(jurorPools.get(0));

            // grab new record to verify it has been created and the properties have been updated correctly
            Juror newJurorRecord = JurorPoolUtils.getActiveJurorRecord(jurorPoolRepository, JUROR_555555558);
            verifyActiveJurorNewRecord(newJurorRecord,
                deferralReasonRequestDto.getPoolNumber(), deferralReasonRequestDto.getDeferralDate());

            // check to make sure no record was created for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555558);
            assertThat(deferral.isPresent()).isFalse();

            assertThat(newJurorRecord.getOpticRef())
                .as(String.format("Expected optic ref to be %s", OPTIC_REF_12345678))
                .isEqualTo(OPTIC_REF_12345678);
        }

        @Test
        void bureauProcessJurorDeferralMaintenancePaper() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            DeferralReasonRequestDto deferralReasonRequestDto =
                createDeferralReasonDtoToDeferralMaintenance(ReplyMethod.PAPER);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555562));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check to make sure no record was created for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555562);
            assertThat(deferral.isPresent()).isTrue();
        }

        @Test
        void courtProcessJurorDeferralMaintenanceDigital() {
            final String bureauJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            DeferralReasonRequestDto deferralReasonRequestDto =
                createDeferralReasonDtoToDeferralMaintenance(ReplyMethod.DIGITAL);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555559));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check to make sure no record was created for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555559);
            assertThat(deferral.isPresent()).isTrue();
        }

        private void verifyActivePoolOldRecord(JurorPool jurorPool) {
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
            assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo(RESPONDED);
            assertThat(jurorPool.getDeferralDate()).isNull();
            assertThat(jurorPool.getNextDate()).isEqualTo(startDate);
            assertThat(juror.isResponded()).isTrue();
            assertThat(jurorPool.getDeferralCode()).isNull();
            assertThat(jurorPool.getIsActive()).isTrue();
            assertThat(juror.getNoDefPos()).isEqualTo(1);
            assertThat(jurorPool.getWasDeferred()).isTrue();
        }
    }

    @Nested
    @DisplayName("POST Change jurors deferral date")
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceChangeDateTest.sql"})
    class ChangeJurorsDeferralDate {
        static final String URL_PREFIX = "/api/v1/moj/deferral-maintenance/deferrals/change-deferral-date/";

        @Test
        void changeDeferralDateActivePoolNotFound() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonRequestDtoToActivePool(null);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_090909090));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);

            assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        }

        @Test
        void changeDeferralDateActivePoolForbidden() {
            final String courtJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
            DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonRequestDtoToActivePool(null);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555551));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void changeDateDeferralMaintenanceHappyPath() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonDtoToDeferralMaintenance(null);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555552));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check to make sure record was created for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555552);
            assertThat(deferral.isPresent()).isTrue();

            assertThat(deferral.get().getDeferredTo()).isEqualTo(
                deferralReasonRequestDto.getDeferralDate());
        }

        @Test
        void changeDeferralDateActivePoolBureauUser() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonRequestDtoToActivePool(null);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555551));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        void courtUserChangeDateDeferralMaintenance() {
            final String courtJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
            DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonDtoToDeferralMaintenance(null);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555558));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check to make sure record was created for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555558);
            assertThat(deferral.isPresent()).isTrue();

            assertThat(deferral.get().getDeferredTo()).isEqualTo(deferralReasonRequestDto.getDeferralDate());
        }

        @Test
        void courtUserChangeDateActivePool() {
            final String courtJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
            DeferralReasonRequestDto deferralReasonRequestDto = createDeferralReasonRequestDtoToActivePool(null);
            RequestEntity<DeferralReasonRequestDto> requestEntity = new RequestEntity<>(deferralReasonRequestDto,
                httpHeaders, POST, URI.create(URL_PREFIX + JUROR_555555558));
            ResponseEntity<DeferralReasonRequestDto> response = template.exchange(requestEntity,
                DeferralReasonRequestDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // grab old record to verify the properties have been updated correctly
            List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_555555558, false);
            assertThat(jurorPools.size()).isGreaterThan(0);

            verifyActivePoolOldRecordChangeDate(jurorPools.get(0), deferralReasonRequestDto.getPoolNumber());

            // grab new record to verify it has been created and the properties have been updated correctly
            jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_555555558, true);

            verifyActivePoolNewRecordChangeDate(jurorPools.get(0),
                deferralReasonRequestDto.getPoolNumber(), deferralReasonRequestDto.getDeferralDate());

            // check to make sure no record was created for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555558);
            assertThat(deferral.isPresent()).isFalse();
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
            assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo(RESPONDED);
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
    }

    @Nested
    @DisplayName("POST Move jurors to active pool")
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJurorTest.sql"})
    class MoveJurorsToActivePool {
        static final String URL = "/api/v1/moj/deferral-maintenance/deferrals/allocate-jurors-to-pool";

        @Test
        void allocateJurorFromDeferralMaintenanceSingleJuror() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            DeferralAllocateRequestDto deferralAllocateRequestDto = createDeferralAllocateRequestDto(
                POOL_415220503, Collections.singletonList(JUROR_555555557));

            RequestEntity<DeferralAllocateRequestDto> requestEntity = new RequestEntity<>(deferralAllocateRequestDto,
                httpHeaders, POST, URI.create(URL));
            ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check to make sure the juror has been removed from maintenance
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555557);
            assertThat(deferral.isPresent())
                .as("Expected juror to be removed from deferral maintenance").isFalse();

            // add logic to check to see the content of the new pool member
            List<JurorPool> jurorPools = jurorPoolRepository.findByPoolPoolNumberAndWasDeferredAndIsActive(
                POOL_415220503, true, true);

            assertThat(jurorPools.size()).as("Expected size to be one for the new pool member record")
                .isEqualTo(1);

            // check to make sure the new pool members record has been updated correctly
            JurorPool jurorPool = jurorPools.get(0);
            Juror juror = jurorPool.getJuror();
            assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo(RESPONDED);
            assertThat(jurorPool.getDeferralDate()).isNull();
            assertThat(jurorPool.getIsActive()).isTrue();
            assertThat(juror.isResponded()).isTrue();
            assertThat(juror.getNoDefPos()).isEqualTo(1);
            LocalDate expectedStartDate = LocalDate.now().plusWeeks(1);
            assertThat(jurorPool.getNextDate()).isEqualTo(expectedStartDate);
            assertThat(juror.getOpticRef())
                .as(String.format("Expected optic ref to be %s", OPTIC_REF_12345678)).isEqualTo(OPTIC_REF_12345678);
        }

        @Test
        void allocateJurorFromDeferralMaintenanceMultipleJurors() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            List<String> jurorNumbers = Arrays.asList(JUROR_555555557, JUROR_555555560, JUROR_555555559);
            DeferralAllocateRequestDto deferralAllocateRequestDto = createDeferralAllocateRequestDto(
                POOL_415220503, jurorNumbers);

            RequestEntity<DeferralAllocateRequestDto> requestEntity = new RequestEntity<>(deferralAllocateRequestDto,
                httpHeaders, POST, URI.create(URL));
            ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check to make sure the jurors has been removed from maintenance
            for (String jurorNumber : jurorNumbers) {
                Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(jurorNumber);
                assertThat(deferral.isPresent())
                    .as("Expected juror to be removed from deferral maintenance").isFalse();
            }

            // add logic to check to see the content of the new pool member
            List<JurorPool> jurorPools = jurorPoolRepository.findByPoolPoolNumberAndWasDeferredAndIsActive(
                POOL_415220503, true, true);

            assertThat(jurorPools.size()).as("Expected size to be three for the juror pool records").isEqualTo(3);

            // check to make sure the new pool members record has been updated correctly
            for (JurorPool jurorPool : jurorPools) {
                Juror juror = jurorPool.getJuror();
                assertThat(jurorPool.getStatus().getStatusDesc()).isEqualTo(RESPONDED);
                assertThat(jurorPool.getDeferralDate()).isNull();
                assertThat(jurorPool.getIsActive()).isTrue();
                assertThat(juror.isResponded()).isTrue();
                assertThat(juror.getNoDefPos()).isEqualTo(1);
                LocalDate expectedStartDate = LocalDate.now().plusWeeks(1);
                assertThat(jurorPool.getNextDate()).isEqualTo(expectedStartDate);
            }
        }

        @Test
        void allocateJurorFromDeferralMaintenanceSingleJurorCourtUserPoolRequestNotFound() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            List<String> jurorNumbers = Collections.singletonList(JUROR_555555557);
            DeferralAllocateRequestDto deferralAllocateRequestDto = createDeferralAllocateRequestDto(
                POOL_222222222, jurorNumbers);

            RequestEntity<DeferralAllocateRequestDto> requestEntity = new RequestEntity<>(deferralAllocateRequestDto,
                httpHeaders, POST, URI.create(URL));
            ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
            assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        }

        @Test
        void allocateJurorFromDeferralMaintenanceSingleJurorCourtUserPoolMemberInvalidAccess() {
            final String courtJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            List<String> jurorNumbers = Collections.singletonList(JUROR_555555557);
            DeferralAllocateRequestDto deferralAllocateRequestDto = createDeferralAllocateRequestDto(
                POOL_415220503, jurorNumbers);

            RequestEntity<DeferralAllocateRequestDto> requestEntity = new RequestEntity<>(deferralAllocateRequestDto,
                httpHeaders, POST, URI.create(URL));
            ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        private DeferralAllocateRequestDto createDeferralAllocateRequestDto(String poolNumber,
                                                                            List<String> jurorNumbers) {
            DeferralAllocateRequestDto dto = new DeferralAllocateRequestDto();
            dto.setJurors(jurorNumbers);
            dto.setPoolNumber(poolNumber);
            return dto;
        }
    }

    @Nested
    @DisplayName("GET Get deferrals by court location code")
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJuror_courtLocationCode.sql"})
    class GetDeferralsByCourtLocationCode {
        static final String URL = "/api/v1/moj/deferral-maintenance/deferrals/415";

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")//False positive
        void testGDeferralsByCourtLocationCodeBureauUser() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, GET, URI.create(URL));
            ResponseEntity<DeferralListDto> response = template.exchange(requestEntity, DeferralListDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            DeferralListDto dto = response.getBody();
            assertThat(dto.getDeferrals().size()).as("Expected size to be 2").isEqualTo(2);
            //Todo may want to make the following test more determinate as order is not guaranteed
            assertThat(dto.getDeferrals().get(0).getJurorNumber()).containsAnyOf("555555557", "555555559");
            assertThat(dto.getDeferrals().get(1).getJurorNumber()).containsAnyOf("555555557", "555555559");
        }

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")//False positive
        void testGDeferralsByCourtLocationCodeCourtUser() {
            final String courtJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, GET, URI.create(URL));
            ResponseEntity<DeferralListDto> response = template.exchange(requestEntity, DeferralListDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            DeferralListDto dto = response.getBody();
            assertThat(dto.getDeferrals().size()).as("Expected size to be 2").isEqualTo(2);
            //Todo may want to make the following test more determinate as order is not guaranteed
            assertThat(dto.getDeferrals().get(0).getJurorNumber()).containsAnyOf("123456789", "444444444");
            assertThat(dto.getDeferrals().get(1).getJurorNumber()).containsAnyOf("123456789", "444444444");
        }
    }

    @Nested
    @DisplayName("GET Get deferral options for court location")
    @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceAllocateJurorTest.sql"})
    class GetDeferralOptionsForCourtLocation {
        static final String URL = "/api/v1/moj/deferral-maintenance/available-pools/415";

        @Test
        void findActivePoolsForCourtLocationNullMaxDate() {
            final String courtJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, GET, URI.create(URL));
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
    }

    @Nested
    @DisplayName("DELETE Delete deferral")
    class DeleteDeferral {
        static final String URL_PREFIX = "/api/v1/moj/deferral-maintenance/delete-deferral/";

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenance_DeleteDeferralTest.sql"})
        void deleteDeferredRecordHappyPath() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, DELETE,
                URI.create(URL_PREFIX + JUROR_123456789));
            ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // check to make sure no record was deleted for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_123456789);
            assertThat(deferral.isPresent()).isFalse();

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(
                JUROR_123456789, POOL_415220502, true).get();
            assertThat(jurorPool).isNotNull();

            Juror juror = jurorPool.getJuror();

            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(IJurorStatus.RESPONDED);
            assertThat(juror.getNoDefPos()).isEqualTo(0);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenance_DeleteDeferralTest.sql"})
        void deleteDeferredRecordJurorNotFound() {
            final String bureauJwt = createJwt(BUREAU_USER, OWNER_400);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, DELETE,
                URI.create(URL_PREFIX + JUROR_000000000));
            ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);

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
        void deleteDeferredRecordWrongAccess() {
            final String courtJwt = createJwt(COURT_USER, OWNER_415);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, DELETE,
                URI.create(URL_PREFIX + JUROR_123456789));
            ResponseEntity<Void> response = template.exchange(requestEntity, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            // check to make sure no record was not deleted for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_123456789);
            assertThat(deferral.isPresent()).isTrue();

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(
                JUROR_123456789, "415220502", true).get();
            assertThat(jurorPool).isNotNull();
            Juror juror = jurorPool.getJuror();
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(IJurorStatus.DEFERRED);
            assertThat(juror.getNoDefPos()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("POST Process juror postponement")
    class ProcessJurorPostponement {
        static final String URL = "/api/v1/moj/deferral-maintenance/juror/postpone";

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_postponeJuror.sql"})
        void bureauPostponeJurorActivePool() {
            setHeaders(BUREAU_USER, OWNER_400, UserType.BUREAU);

            ProcessJurorPostponementRequestDto request =
                createProcessJurorPostponementRequestDto(Collections.singletonList(JUROR_555555551));

            RequestEntity<ProcessJurorPostponementRequestDto> requestEntity = new RequestEntity<>(request,
                httpHeaders, POST, URI.create(URL));

            ResponseEntity<DeferralResponseDto> response = template.exchange(requestEntity, DeferralResponseDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(requireNonNull(response.getBody()).getCountJurorsPostponed()).isEqualTo(1);

            // check to ensure pool member was postponed and current record logically deleted
            List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_555555551,
                false);

            assertThat(jurorPools.size()).as("Expected size to be one for the postponed pool member record")
                .isEqualTo(1);
            JurorPool jurorPool = jurorPools.get(0);
            assertThat(jurorPool.getStatus().getStatus()).as("Expect Status to be deferred (7)")
                .isEqualTo(7);
            assertThat(jurorPool.getDeferralCode()).as("Expect reason code to be postponement (P)")
                .isEqualTo("P");
            assertThat(jurorPool.getPostpone()).as("Expect pool member to be postponed").isTrue();

            // check to ensure postponed pool member was created in new pool
            jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_555555551, true);

            assertThat(jurorPools.size()).as("Expected size to be one for the new pool member record")
                .isEqualTo(1);
            assertThat(jurorPools.get(0).getJurorNumber()).isEqualTo(JUROR_555555551);

            // check to make sure the juror has not been added to deferral maintenance
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555551);
            assertThat(deferral.isPresent()).as("Expect juror not to be in deferral maintenance").isFalse();

            // verify postpone letter has been queued for bulk print
            List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findAll();
            assertThat(bulkPrintData.size()).isEqualTo(1);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_postponeJuror.sql"})
        void courtPostponeJurorActivePool() {
            setHeaders(COURT_USER, OWNER_415, UserType.COURT);

            ProcessJurorPostponementRequestDto request =
                createProcessJurorPostponementRequestDto(Collections.singletonList(JUROR_555555559));

            RequestEntity<ProcessJurorPostponementRequestDto> requestEntity = new RequestEntity<>(request,
                httpHeaders, POST, URI.create(URL));

            ResponseEntity<DeferralResponseDto> response = template.exchange(requestEntity, DeferralResponseDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(requireNonNull(response.getBody()).getCountJurorsPostponed()).isEqualTo(1);

            // check to ensure pool member was postponed and current record logically deleted
            List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_555555559,
                false);

            assertThat(jurorPools.size()).as("Expected size to be one for the postponed pool member record")
                .isEqualTo(1);
            JurorPool jurorPool = jurorPools.get(0);
            assertThat(jurorPool.getStatus().getStatus()).as("Expect Status to be deferred (7)")
                .isEqualTo(7);
            assertThat(jurorPool.getDeferralCode()).as("Expect reason code to be postponement (P)")
                .isEqualTo("P");
            assertThat(jurorPool.getPostpone()).as("Expect pool member to be postponed").isTrue();

            // check to ensure postponed pool member was created in new pool
            jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(JUROR_555555559, true);

            assertThat(jurorPools.size()).as("Expected size to be one for the new pool member record")
                .isEqualTo(1);
            assertThat(jurorPools.get(0).getJurorNumber()).isEqualTo(JUROR_555555559);

            // check to make sure the juror has not been added to deferral maintenance
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555559);
            assertThat(deferral.isPresent()).as("Expect juror not to be in deferral maintenance").isFalse();

            // verify no postpone letter has been queued for bulk print
            List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findAll();
            assertThat(bulkPrintData.size()).isEqualTo(0);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_postponeJuror.sql"})
        void bureauPostponeJurorDeferralMaintenance() {
            setHeaders(BUREAU_USER, OWNER_400, UserType.BUREAU);

            ProcessJurorPostponementRequestDto request = new ProcessJurorPostponementRequestDto();
            request.setDeferralDate(LocalDate.now().plusDays(20));
            request.setExcusalReasonCode("P");
            request.setJurorNumbers(Collections.singletonList(JUROR_555555552));

            RequestEntity<ProcessJurorPostponementRequestDto> requestEntity = new RequestEntity<>(request,
                httpHeaders, POST, URI.create(URL));

            ResponseEntity<DeferralResponseDto> response = template.exchange(requestEntity, DeferralResponseDto.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(requireNonNull(response.getBody()).getCountJurorsPostponed()).isEqualTo(1);

            // check to ensure pool member was postponed but still active
            List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(
                JUROR_555555552, true);

            assertThat(jurorPools.size()).as("Expected size to be one for the postponed pool member record")
                .isEqualTo(1);
            JurorPool jurorPool = jurorPools.get(0);

            assertThat(jurorPool.getStatus().getStatus()).as("Expect Status to be deferred (7)")
                .isEqualTo(7);
            assertThat(jurorPool.getDeferralCode()).as("Expect reason code to be postponement (P)")
                .isEqualTo("P");
            assertThat(jurorPool.getPostpone()).as("Expect pool member to be postponed").isTrue();

            // check to make sure a record was created for the deferral maintenance table
            Optional<CurrentlyDeferred> deferral = currentlyDeferredRepository.findById(JUROR_555555552);
            assertThat(deferral.isPresent()).isTrue();

            // verify postpone letter has been queued for bulk print
            List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findAll();
            assertThat(bulkPrintData.size()).isEqualTo(1);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/DeferralMaintenanceController_postponeJuror.sql"})
        void bureauPostponeJurorToTheSamePool() {
            setHeaders(BUREAU_USER, OWNER_400, UserType.BUREAU);

            ResponseEntity<MojException.BadRequest> response = template.exchange(new RequestEntity<>(
                createProcessJurorPostponementRequestDto(Collections.singletonList(JUROR_555555557)),
                httpHeaders, POST, URI.create(URL)), MojException.BadRequest.class);

            assertThat(response.getStatusCode()).as("Expect the status to be a bad request")
                .isEqualTo(BAD_REQUEST);
        }

        @Test
        void bureauPostponeJurorWithAnInvalidCode() {
            setHeaders(BUREAU_USER, OWNER_400, UserType.BUREAU);

            ProcessJurorPostponementRequestDto request =
                createProcessJurorPostponementRequestDto(Collections.singletonList(JUROR_555555551));
            request.setExcusalReasonCode("C");

            ResponseEntity<MojException.BadRequest> response = template.exchange(new RequestEntity<>(
                request, httpHeaders, POST, URI.create(URL)), MojException.BadRequest.class);

            assertThat(response.getStatusCode()).as("Expect the status to be a bad request")
                .isEqualTo(BAD_REQUEST);
        }

        @Test
        void bureauPostponeJurorForNonExistingJuror() {
            setHeaders(BUREAU_USER, OWNER_400, UserType.BUREAU);

            ProcessJurorPostponementRequestDto request =
                createProcessJurorPostponementRequestDto(Collections.singletonList("999999999"));

            ResponseEntity<MojException.BadRequest> response = template.exchange(new RequestEntity<>(
                request, httpHeaders, POST, URI.create(URL)), MojException.BadRequest.class);

            assertThat(response.getStatusCode()).as("Expect the status to be a bad request")
                .isEqualTo(NOT_FOUND);
        }

        @Test
        void bureauPostponeJurorPoolNumberDoesNotExist() {
            setHeaders(BUREAU_USER, OWNER_400, UserType.BUREAU);

            ProcessJurorPostponementRequestDto request =
                createProcessJurorPostponementRequestDto(Collections.singletonList(JUROR_555555551));
            request.setPoolNumber(POOL_415220502);

            ResponseEntity<MojException.NotFound> response = template.exchange(new RequestEntity<>(
                request, httpHeaders, POST, URI.create(URL)), MojException.NotFound.class);

            assertThat(response.getStatusCode()).as("Expect the status to be a bad request")
                .isEqualTo(NOT_FOUND);
        }

        @Test
        void courtPostponeJurorNoJurorsInRequest() {
            setHeaders(COURT_USER, OWNER_415, UserType.COURT);

            ProcessJurorPostponementRequestDto request = createProcessJurorPostponementRequestDto(new ArrayList<>());
            request.setJurorNumbers(new ArrayList<>());

            ResponseEntity<MojException.BadRequest> response = template.exchange(new RequestEntity<>(
                request, httpHeaders, POST, URI.create(URL)), MojException.BadRequest.class);

            assertThat(response.getStatusCode()).as("Expect the status to be a bad request")
                .isEqualTo(BAD_REQUEST);
        }

        @Test
        void courtPostponeJurorNullJurorsInRequest() {
            setHeaders(COURT_USER, OWNER_415, UserType.COURT);

            ProcessJurorPostponementRequestDto request = createProcessJurorPostponementRequestDto(new ArrayList<>());
            request.setJurorNumbers(null);

            ResponseEntity<MojException.BadRequest> response = template.exchange(new RequestEntity<>(
                request, httpHeaders, POST, URI.create(URL)), MojException.BadRequest.class);

            assertThat(response.getStatusCode()).as("Expect the status to be a bad request")
                .isEqualTo(BAD_REQUEST);
        }

        private void setHeaders(String user, String owner, UserType userType) {
            final String bureauJwt = createJwt(user, owner, userType);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        }

        private ProcessJurorPostponementRequestDto createProcessJurorPostponementRequestDto(List<String> jurorNumbers) {
            ProcessJurorPostponementRequestDto request = new ProcessJurorPostponementRequestDto();
            request.setDeferralDate(LocalDate.now().plusDays(10));
            request.setPoolNumber(POOL_415220503);
            request.setExcusalReasonCode("P");
            request.setJurorNumbers(jurorNumbers);

            return request;
        }
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

    @Override
    protected String createJwt(String login, String owner, String... courts) {
        return mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .login(login)
            .staff(BureauJwtPayload.Staff.builder()
                .name("Test User")
                .active(1)
                .rank(1)
                .courts(List.of("415", "400"))
                .build())
            .owner(owner)
            .build());
    }

    protected String createJwt(String login, String owner, UserType userType) {
        return mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .login(login)
            .userType(userType)
            .staff(BureauJwtPayload.Staff.builder()
                .name("Test User")
                .active(1)
                .rank(1)
                .courts(List.of("415", "400"))
                .build())
            .owner(owner)
            .build());
    }
}