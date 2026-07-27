package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CreatePoolController when digital by default is enabled.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "feature-flags.flags.digital-by-default=true")
public class CreatePoolDigitalByDefaultControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;

    @Autowired
    private JurorRepository jurorRepository;

    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    private HttpHeaders httpHeaders;

    @Before
    public void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql(
        scripts = {
            "/db/mod/truncate.sql",
            "/db/CreatePoolController_loadVoters.sql"
        },
        statements = {
            "INSERT INTO juror_mod.court_catchment_area (postcode, loc_code) VALUES ('CH1', '419')",
            "INSERT INTO juror_mod.court_catchment_area (postcode, loc_code) VALUES ('CH2', '419')",
            "INSERT INTO juror_mod.court_catchment_area (postcode, loc_code) VALUES ('CH3', '419')",
            "INSERT INTO juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, "
                + "loc_code, new_request, last_update, additional_summons, attend_time) VALUES ('400', '419221201', "
                + "TIMESTAMP'2022-12-04 00:00:00.0', 5, 5, 'CRO', '419', 'N', "
                + "TIMESTAMP'2022-10-02 09:22:09.0', NULL, NULL)"
        })
    public void createPool_digitalByDefaultCourt_createsLightSummonsLetter() {
        PoolCreateRequestDto poolCreateRequest = setUpDigitalByDefaultPoolCreateRequestDto();

        ResponseEntity<String> response = createPool(poolCreateRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {
            List<Juror> jurors = jurorRepository.findAll();
            assertDigitalByDefaultLightSummons(jurors, "419221201", "5222");
        });
    }

    @Test
    @Sql(
        scripts = {
            "/db/mod/truncate.sql",
            "/db/CreatePoolController_loadVoters.sql"
        },
        statements = {
            "UPDATE juror_mod.court_location SET digital_by_default = TRUE WHERE loc_code = '774'",
            "DELETE FROM juror_mod.court_catchment_area WHERE postcode = 'SY2' AND loc_code = '774'",
            "INSERT INTO juror_mod.court_catchment_area (postcode, loc_code) VALUES ('SY2', '774')",
            "INSERT INTO juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, "
                + "loc_code, new_request, last_update, additional_summons, attend_time) VALUES ('400', '774221201', "
                + "TIMESTAMP'2022-12-04 00:00:00.0', 5, 5, 'CRO', '774', 'N', "
                + "TIMESTAMP'2022-10-02 09:22:09.0', NULL, NULL)"
        })
    public void createPool_digitalByDefaultWelshCourt_createsWelshLightSummonsLetter() {
        PoolCreateRequestDto poolCreateRequest = setUpWelshDigitalByDefaultPoolCreateRequestDto();

        ResponseEntity<String> response = createPool(poolCreateRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {
            List<Juror> jurors = jurorRepository.findAll();
            assertDigitalByDefaultLightSummons(jurors, "774221201", "5222C");
        });
    }

    private ResponseEntity<String> createPool(PoolCreateRequestDto poolCreateRequest) {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .roles(Set.of(Role.MANAGER))
            .login("BUREAU_USER")
            .staff(BureauJwtPayload.Staff.builder().name("Bureau User").active(1).build())
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        return template.exchange(requestEntity, String.class);
    }

    private PoolCreateRequestDto setUpPoolCreateRequestDto() {
        PoolCreateRequestDto poolCreateRequestDto = new PoolCreateRequestDto();
        poolCreateRequestDto.setPoolNumber("415221201");
        poolCreateRequestDto.setStartDate(LocalDate.of(2022, 12, 4));
        poolCreateRequestDto.setAttendTime(LocalDateTime.of(2022, 12, 4, 9, 0, 0));
        poolCreateRequestDto.setNoRequested(5);
        poolCreateRequestDto.setBureauDeferrals(0);
        poolCreateRequestDto.setNumberRequired(4);
        poolCreateRequestDto.setCitizensToSummon(8);
        poolCreateRequestDto.setCatchmentArea("415");
        List<String> postcodes = new ArrayList<>();
        postcodes.add("CH1");
        postcodes.add("CH2");
        postcodes.add("CH3");
        poolCreateRequestDto.setPostcodes(postcodes);

        return poolCreateRequestDto;
    }

    private PoolCreateRequestDto setUpDigitalByDefaultPoolCreateRequestDto() {
        PoolCreateRequestDto poolCreateRequestDto = setUpPoolCreateRequestDto();
        poolCreateRequestDto.setPoolNumber("419221201");
        poolCreateRequestDto.setCatchmentArea("419");
        return poolCreateRequestDto;
    }

    private PoolCreateRequestDto setUpWelshDigitalByDefaultPoolCreateRequestDto() {
        PoolCreateRequestDto poolCreateRequestDto = setUpPoolCreateRequestDto();
        poolCreateRequestDto.setPoolNumber("774221201");
        poolCreateRequestDto.setCatchmentArea("774");
        poolCreateRequestDto.setPostcodes(List.of("SY2"));
        return poolCreateRequestDto;
    }

    private void assertDigitalByDefaultLightSummons(List<Juror> jurors, String poolNumber, String formType) {
        assertThat(jurors).hasSize(8);
        assertThat(jurors).allSatisfy(juror -> {
            assertThat(juror.isDigitalByDefault()).isTrue();
            assertThat(juror.getDbdPreference()).isEqualTo(ReplyMethod.DIGITAL.getDescription());
        });

        List<BulkPrintData> lightSummonsLetters = bulkPrintDataRepository.findAll();
        assertThat(lightSummonsLetters).hasSize(8);
        assertThat(lightSummonsLetters).allSatisfy(letter ->
            assertThat(letter.getFormAttribute().getFormType()).isEqualTo(formType));

        jurors.forEach(juror -> {
            List<JurorHistory> jurorHistories =
                jurorHistoryRepository.findByJurorNumberOrderById(juror.getJurorNumber());
            assertThat(jurorHistories).singleElement().satisfies(history -> {
                assertThat(history.getPoolNumber()).isEqualTo(poolNumber);
                assertThat(history.getCreatedBy()).isEqualTo("BUREAU_USER");
                assertThat(history.getHistoryCode()).isEqualTo(HistoryCodeMod.PRINT_SUMMONS);
                assertThat(history.getOtherInformation()).isEqualTo("Summons letter only");
            });
        });
    }
}
