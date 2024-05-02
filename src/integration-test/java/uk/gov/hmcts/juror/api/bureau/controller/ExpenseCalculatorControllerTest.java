package uk.gov.hmcts.juror.api.bureau.controller;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcTravelModeData;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorExpensesCalcResults;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for the API endpoints defined in {@link ExpenseCalculatorControllerTest}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExpenseCalculatorControllerTest extends AbstractIntegrationTest {

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;
    private String bureauJwt;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("ksalazar")
            .owner("400")
            .build());
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ExpenseCalculatorData_setup.sql"
    })
    public void expensesCalculatorLooseIncomeAboveMaxIncomeLimit_happyPath() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/expenseCalculator/estimate");

        // based on BureauBacklogAllocateRequestController
        RequestEntity<JurorExpensesCalcRequestDto> request = new RequestEntity<>(JurorExpensesCalcRequestDto.builder()
            .looseIncome(true).incomeExceedsThreshold(true).dailyEarnings(0f)
            .extraCosts(true).extraCostsAmount(25f).parking(true)
            .travellingModes(Arrays.asList(
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("car").dailyMiles(2.5f).dailyCost(0f).build(),
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("public").dailyCost(10f).build()
            )).build(), httpHeaders, HttpMethod.POST, uri);

        // based on BereauBacklogCountControllerTest.
        ResponseEntity<JurorExpensesCalcResults> response = template.exchange(request, JurorExpensesCalcResults.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDailyTravelTotal().equals(10.785f));
        assertThat(response.getBody().getDailyLossOfEarningsClaim().equals(64.95f));
        assertThat(response.getBody().getDailyLossOfEarningsTotal().equals(89.95f));
        assertThat(response.getBody().getDailyTotal().equals(81.445f));
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ExpenseCalculatorData_setup.sql"
    })
    public void expensesCalculatorLooseIncomeBelowMaxIncomeLimit_happyPath() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/expenseCalculator/estimate");

        RequestEntity<JurorExpensesCalcRequestDto> request = new RequestEntity<>(JurorExpensesCalcRequestDto.builder()
            .looseIncome(true).incomeExceedsThreshold(false).dailyEarnings(30.50f)
            .extraCosts(true).extraCostsAmount(25f).parking(true)
            .travellingModes(Arrays.asList(
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("car").dailyMiles(2.5f).dailyCost(0f).build(),
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("public").dailyCost(10f).build()
            )).build(), httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<JurorExpensesCalcResults> response = template.exchange(request, JurorExpensesCalcResults.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDailyTravelTotal().equals(10.785f));
        assertThat(response.getBody().getDailyLossOfEarningsClaim().equals(55.5f));
        assertThat(response.getBody().getDailyLossOfEarningsTotal().equals(55.5f));
        assertThat(response.getBody().getDailyTotal().equals(71.995f));
    }


    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ExpenseCalculatorData_setup.sql"
    })
    public void expensesCalculatorNotLooseIncome_happyPath() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/expenseCalculator/estimate");

        RequestEntity<JurorExpensesCalcRequestDto> request = new RequestEntity<>(JurorExpensesCalcRequestDto.builder()
            .looseIncome(false)
            .extraCosts(true).extraCostsAmount(25f).parking(true)
            .travellingModes(Arrays.asList(
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("car").dailyMiles(2.5f).dailyCost(0f).build(),
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("public").dailyCost(10f).build()
            )).build(), httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<JurorExpensesCalcResults> response = template.exchange(request, JurorExpensesCalcResults.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDailyTravelTotal().equals(10.785f));
        assertThat(response.getBody().getDailyLossOfEarningsClaim().equals(25f));
        assertThat(response.getBody().getDailyLossOfEarningsTotal().equals(25f));
        assertThat(response.getBody().getDailyTotal().equals(41.495f));
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    //@Sql("/db/ExpenseCalculatorData_setup.sql")
    public void expensesCalculatorNoRateData() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/expenseCalculator/estimate");

        // based on BureauBacklogAllocateRequestController
        RequestEntity<JurorExpensesCalcRequestDto> request = new RequestEntity<>(JurorExpensesCalcRequestDto.builder()
            .looseIncome(false)
            .extraCosts(true).extraCostsAmount(25f).parking(true)
            .travellingModes(Arrays.asList(
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("car").dailyMiles(2.5f).dailyCost(0f).build(),
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("public").dailyCost(10f).build()
            )).build(), httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<JurorExpensesCalcResults> response = template.exchange(request, JurorExpensesCalcResults.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/ExpenseCalculatorData_setup.sql"
    })
    public void expensesCalculatorMissingUserParameter() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/expenseCalculator/estimate");

        RequestEntity<JurorExpensesCalcRequestDto> request = new RequestEntity<>(JurorExpensesCalcRequestDto.builder()
            .extraCosts(true).extraCostsAmount(25f).parking(true)
            .travellingModes(Arrays.asList(
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("car").dailyMiles(2.5f).dailyCost(0f).build(),
                JurorExpensesCalcTravelModeData.builder().modeOfTravel("public").dailyCost(10f).build()
            )).build(), httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<JurorExpensesCalcResults> response = template.exchange(request, JurorExpensesCalcResults.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

}

