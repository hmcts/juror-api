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
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void createPool_digitalByDefaultCourt_doesNotCreateSummonsLetter() {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .roles(Set.of(Role.MANAGER))
            .login("BUREAU_USER")
            .staff(BureauJwtPayload.Staff.builder().name("Bureau User").active(1).build())
            .owner("400")
            .build());

        PoolCreateRequestDto poolCreateRequest = setUpDigitalByDefaultPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        executeInTransaction(() -> {
            List<Juror> jurors = jurorRepository.findAll();
            assertThat(jurors).hasSize(8);
            assertThat(jurors).allSatisfy(juror -> {
                assertThat(juror.isDigitalByDefault()).isTrue();
                assertThat(juror.getDbdPreference()).isEqualTo(ReplyMethod.DIGITAL.getDescription());
            });

            assertThat(bulkPrintDataRepository.count()).isZero();
        });
    }

    private PoolCreateRequestDto setUpDigitalByDefaultPoolCreateRequestDto() {
        PoolCreateRequestDto poolCreateRequestDto = new PoolCreateRequestDto();
        poolCreateRequestDto.setPoolNumber("419221201");
        poolCreateRequestDto.setStartDate(LocalDate.of(2022, 12, 4));
        poolCreateRequestDto.setAttendTime(LocalDateTime.of(2022, 12, 4, 9, 0, 0));
        poolCreateRequestDto.setNoRequested(5);
        poolCreateRequestDto.setBureauDeferrals(0);
        poolCreateRequestDto.setNumberRequired(4);
        poolCreateRequestDto.setCitizensToSummon(8);
        poolCreateRequestDto.setCatchmentArea("419");
        List<String> postcodes = new ArrayList<>();
        postcodes.add("CH1");
        postcodes.add("CH2");
        postcodes.add("CH3");
        poolCreateRequestDto.setPostcodes(postcodes);

        return poolCreateRequestDto;
    }
}
