package uk.gov.hmcts.juror.api.moj.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestSearchListDto;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql({"/db/mod/truncate.sql"})
public class PoolRequestSearchServiceITest extends ContainerTest {

    @Autowired
    private PoolRequestRepository poolRequestRepository;
    @Autowired
    private PoolRequestSearchService poolRequestSearchService;

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_wholePoolNumber_exactMatch() {
        String poolNumber = "415230101";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setPoolNumber(poolNumber);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        PoolRequestSearchListDto.PoolRequestSearchDataDto data = results.getData().get(0);
        assertThat(data.getPoolNumber()).isEqualTo(poolNumber);
        assertThat(data.getPoolType()).isEqualTo("Crown court");
        assertThat(data.getPoolStatus()).isEqualTo("Requested");
        assertThat(data.getPoolStage()).isEqualTo("At court");
        assertThat(data.getServiceStartDate()).isEqualTo("2023-01-16");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_wholePoolNumber_noMatch() {
        String poolNumber = "015230101";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setPoolNumber(poolNumber);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(0);
        assertThat(results.getData().size()).isEqualTo(0);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_partialPoolNumber_multipleMatch() {
        String poolNumber = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setPoolNumber(poolNumber);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).startsWith(poolNumber);
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_partialPoolNumber_noMatch() {
        String poolNumber = "016";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setPoolNumber(poolNumber);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(0);
        assertThat(results.getData().size()).isEqualTo(0);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_courtLocation_multipleMatch() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).startsWith(locCode);
            assertThat(data.getCourtName()).isEqualToIgnoringCase("Lewes Sitting At Chichester");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_courtLocationAndPoolNumber_oneMatch() {
        String poolNumber = "416230101";
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setPoolNumber(poolNumber);
        poolSearchRequestDto.setLocCode(locCode);
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        PoolRequestSearchListDto.PoolRequestSearchDataDto data = results.getData().get(0);
        assertThat(data.getPoolNumber()).isEqualTo(poolNumber);
        assertThat(data.getCourtName()).isEqualToIgnoringCase("Lewes Sitting At Chichester");
        assertThat(data.getPoolType()).isEqualToIgnoringCase("Civil court");
        assertThat(data.getPoolStatus()).isEqualToIgnoringCase("Nil");
        assertThat(data.getPoolStage()).isEqualToIgnoringCase("With the Bureau");
        assertThat(data.getServiceStartDate()).isEqualTo("2023-01-09");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_serviceStartDate_multipleMatch() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getServiceStartDate()).isEqualTo(startDate.toString());
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_serviceStartDate_courtUser_oneMatch() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            Collections.singletonList("416"));

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        PoolRequestSearchListDto.PoolRequestSearchDataDto data = results.getData().get(0);
        assertThat(data.getPoolNumber()).isEqualTo("416230102");
        assertThat(data.getCourtName()).isEqualToIgnoringCase("Lewes Sitting At Chichester");
        assertThat(data.getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.getPoolStatus()).isEqualToIgnoringCase("Completed");
        assertThat(data.getPoolStage()).isEqualToIgnoringCase("At court");
        assertThat(data.getServiceStartDate()).isEqualTo("2023-01-16");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_allBasicSearchCriteria_oneMatch() {
        String poolNumber = "416230103";
        String locCode = "416";
        LocalDate startDate = LocalDate.of(2023, 1, 23);

        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setPoolNumber(poolNumber);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setServiceStartDate(startDate);
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            Collections.singletonList("416"));

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        PoolRequestSearchListDto.PoolRequestSearchDataDto data = results.getData().get(0);
        assertThat(data.getPoolNumber()).isEqualTo(poolNumber);
        assertThat(data.getCourtName()).isEqualToIgnoringCase("Lewes Sitting At Chichester");
        assertThat(data.getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.getPoolStatus()).isEqualToIgnoringCase("Active");
        assertThat(data.getPoolStage()).isEqualToIgnoringCase("At court");
        assertThat(data.getServiceStartDate()).isEqualTo("2023-01-23");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_noBasicSearchCriteria() {
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto, new ArrayList<>()));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByPoolNoAsc() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.POOL_NO);
        poolSearchRequestDto.setSortDirection(SortDirection.ASC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getPoolNumber()).isEqualTo("416230101");
        assertThat(data.get(1).getPoolNumber()).isEqualTo("416230102");
        assertThat(data.get(2).getPoolNumber()).isEqualTo("416230103");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByPoolNoDesc() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.POOL_NO);
        poolSearchRequestDto.setSortDirection(SortDirection.DESC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getPoolNumber()).isEqualTo("416230103");
        assertThat(data.get(1).getPoolNumber()).isEqualTo("416230102");
        assertThat(data.get(2).getPoolNumber()).isEqualTo("416230101");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByCourtNameAsc() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.COURT_NAME);
        poolSearchRequestDto.setSortDirection(SortDirection.ASC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getCourtName()).isEqualToIgnoringCase("Chester");
        assertThat(data.get(1).getCourtName()).isEqualToIgnoringCase("Coventry");
        assertThat(data.get(2).getCourtName()).isEqualToIgnoringCase("Lewes Sitting At Chichester");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByCourtNameDesc() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.COURT_NAME);
        poolSearchRequestDto.setSortDirection(SortDirection.DESC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getCourtName()).isEqualToIgnoringCase("Lewes Sitting At Chichester");
        assertThat(data.get(1).getCourtName()).isEqualToIgnoringCase("Coventry");
        assertThat(data.get(2).getCourtName()).isEqualToIgnoringCase("Chester");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByPoolStageAsc() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.POOL_STAGE);
        poolSearchRequestDto.setSortDirection(SortDirection.ASC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getPoolStage()).isEqualToIgnoringCase("With the Bureau");
        assertThat(data.get(1).getPoolStage()).isEqualToIgnoringCase("At court");
        assertThat(data.get(2).getPoolStage()).isEqualToIgnoringCase("At court");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByPoolStageDesc() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.POOL_STAGE);
        poolSearchRequestDto.setSortDirection(SortDirection.DESC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getPoolStage()).isEqualToIgnoringCase("At court");
        assertThat(data.get(1).getPoolStage()).isEqualToIgnoringCase("At court");
        assertThat(data.get(2).getPoolStage()).isEqualToIgnoringCase("With the Bureau");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByPoolStatusAsc() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.POOL_STATUS);
        poolSearchRequestDto.setSortDirection(SortDirection.ASC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(7);
        assertThat(results.getData().size()).isEqualTo(7);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getPoolStatus()).isEqualToIgnoringCase("Requested");
        assertThat(data.get(1).getPoolStatus()).isEqualToIgnoringCase("Nil");
        assertThat(data.get(2).getPoolStatus()).isEqualToIgnoringCase("Active");
        assertThat(data.get(3).getPoolStatus()).isEqualToIgnoringCase("Active");
        assertThat(data.get(4).getPoolStatus()).isEqualToIgnoringCase("Active");
        assertThat(data.get(5).getPoolStatus()).isEqualToIgnoringCase("Completed");
        assertThat(data.get(6).getPoolStatus()).isEqualToIgnoringCase("Completed");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByPoolStatusDesc() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.POOL_STATUS);
        poolSearchRequestDto.setSortDirection(SortDirection.DESC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(7);
        assertThat(results.getData().size()).isEqualTo(7);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getPoolStatus()).isEqualToIgnoringCase("Completed");
        assertThat(data.get(1).getPoolStatus()).isEqualToIgnoringCase("Completed");
        assertThat(data.get(2).getPoolStatus()).isEqualToIgnoringCase("Active");
        assertThat(data.get(3).getPoolStatus()).isEqualToIgnoringCase("Active");
        assertThat(data.get(4).getPoolStatus()).isEqualToIgnoringCase("Active");
        assertThat(data.get(5).getPoolStatus()).isEqualToIgnoringCase("Nil");
        assertThat(data.get(6).getPoolStatus()).isEqualToIgnoringCase("Requested");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByPoolTypeAsc() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.POOL_TYPE);
        poolSearchRequestDto.setSortDirection(SortDirection.ASC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(7);
        assertThat(results.getData().size()).isEqualTo(7);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getPoolType()).isEqualToIgnoringCase("Civil court");
        assertThat(data.get(1).getPoolType()).isEqualToIgnoringCase("Civil court");
        assertThat(data.get(2).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(3).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(4).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(5).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(6).getPoolType()).isEqualToIgnoringCase("Crown court");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByPoolTypeDesc() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.POOL_TYPE);
        poolSearchRequestDto.setSortDirection(SortDirection.DESC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(7);
        assertThat(results.getData().size()).isEqualTo(7);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        assertThat(data.get(0).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(1).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(2).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(3).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(4).getPoolType()).isEqualToIgnoringCase("Crown court");
        assertThat(data.get(5).getPoolType()).isEqualToIgnoringCase("Civil court");
        assertThat(data.get(6).getPoolType()).isEqualToIgnoringCase("Civil court");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByStartDateAsc() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.START_DATE);
        poolSearchRequestDto.setSortDirection(SortDirection.ASC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(7);
        assertThat(results.getData().size()).isEqualTo(7);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        String nextWeek = LocalDate.now().plusWeeks(1).toString();
        String lastWeek = LocalDate.now().plusWeeks(-1).toString();

        assertThat(data.get(0).getServiceStartDate()).isEqualTo("2023-01-09");
        assertThat(data.get(1).getServiceStartDate()).isEqualTo("2023-01-16");
        assertThat(data.get(2).getServiceStartDate()).isEqualTo("2023-01-23");
        assertThat(data.get(3).getServiceStartDate()).isEqualTo("2023-01-30");
        assertThat(data.get(4).getServiceStartDate()).isEqualTo(lastWeek);
        assertThat(data.get(5).getServiceStartDate()).isEqualTo(lastWeek);
        assertThat(data.get(6).getServiceStartDate()).isEqualTo(nextWeek);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_sortByStartDateDesc() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setSortColumn(PoolSearchRequestDto.SortColumn.START_DATE);
        poolSearchRequestDto.setSortDirection(SortDirection.DESC);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(7);
        assertThat(results.getData().size()).isEqualTo(7);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        String nextWeek = LocalDate.now().plusWeeks(1).toString();
        String lastWeek = LocalDate.now().plusWeeks(-1).toString();

        assertThat(data.get(0).getServiceStartDate()).isEqualTo(nextWeek);
        assertThat(data.get(1).getServiceStartDate()).isEqualTo(lastWeek);
        assertThat(data.get(2).getServiceStartDate()).isEqualTo(lastWeek);
        assertThat(data.get(3).getServiceStartDate()).isEqualTo("2023-01-30");
        assertThat(data.get(4).getServiceStartDate()).isEqualTo("2023-01-23");
        assertThat(data.get(5).getServiceStartDate()).isEqualTo("2023-01-16");
        assertThat(data.get(6).getServiceStartDate()).isEqualTo("2023-01-09");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchForPoolRequest_multipleResults_defaultSortOrder() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(7);
        assertThat(results.getData().size()).isEqualTo(7);

        List<PoolRequestSearchListDto.PoolRequestSearchDataDto> data = results.getData();

        String nextWeek = LocalDate.now().plusWeeks(1).toString();
        String lastWeek = LocalDate.now().plusWeeks(-1).toString();

        assertThat(data.get(0).getServiceStartDate()).isEqualTo(nextWeek);
        assertThat(data.get(1).getServiceStartDate()).isEqualTo(lastWeek);
        assertThat(data.get(2).getServiceStartDate()).isEqualTo(lastWeek);
        assertThat(data.get(3).getServiceStartDate()).isEqualTo("2023-01-30");
        assertThat(data.get(4).getServiceStartDate()).isEqualTo("2023-01-23");
        assertThat(data.get(5).getServiceStartDate()).isEqualTo("2023-01-16");
        assertThat(data.get(6).getServiceStartDate()).isEqualTo("2023-01-09");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPaginatedPoolRequests.sql"})
    public void test_searchForPoolRequest_multiplePages_firstPage() {
        String locCode = "415";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(27);
        assertThat(results.getData().size()).isEqualTo(25);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPaginatedPoolRequests.sql"})
    public void test_searchForPoolRequest_multiplePages_secondPage() {
        String locCode = "415";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(1);
        poolSearchRequestDto.setLocCode(locCode);

        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(27);
        assertThat(results.getData().size()).isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_requested() {
        String locCode = "415";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Collections.singletonList(PoolSearchRequestDto.PoolStatus.REQUESTED));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        assertThat(results.getData().get(0).getPoolNumber()).isEqualTo("415230101");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_active() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Collections.singletonList(PoolSearchRequestDto.PoolStatus.ACTIVE));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(2);
        assertThat(results.getData().size()).isEqualTo(2);


        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("416230101", "416230102");
        }
    }

    /**
     * Additional test scenario to cover the Active state of a pool when it has been requested,
     * transferred to the Bureau and created (citizens summonsed) but no responses are received yet so no
     * pool members have an "active" status (RESPONDED, JURY, PANEL).
     */
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_active2() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Collections.singletonList(PoolSearchRequestDto.PoolStatus.ACTIVE));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        assertThat(results.getData().get(0).getPoolNumber()).isEqualTo("417230102");
    }

    /**
     * Additional test scenario to cover the Active state of a pool when it has been created via the transfer journey.
     * When a court officer wishes to transfer jurors to another court, a new pool is created with a NO_REQUESTED value
     * of null. The pool will initially contain active pool members, but it may also be used as a "staging" pool
     * which would still show as ACTIVE if the start date is in the future, even if all pool members are transferred
     * out again
     */
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_active3() {
        String locCode = "418";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Collections.singletonList(PoolSearchRequestDto.PoolStatus.ACTIVE));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("418230101", "418230102", "418230105");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_completed() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Collections.singletonList(PoolSearchRequestDto.PoolStatus.COMPLETED));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(2);
        assertThat(results.getData().size()).isEqualTo(2);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("416230103", "416230104");
        }
    }

    /**
     * Additional test scenario to cover the Completed state of a pool when it has been created via the transfer
     * journey. When a court officer wishes to transfer jurors to another court, a new pool is created with a
     * NO_REQUESTED value of null. These pools may also be used as a "staging" pool which would show as COMPLETED if:
     * <ul>
     *     <li>All pool members are inactive (have completed their service) and the service start date is in the
     *     past</li>
     *     <li>There are no pool members left in the pool and the service start date is in the past</li>
     * </ul>
     */
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_completed2() {
        String locCode = "418";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Collections.singletonList(PoolSearchRequestDto.PoolStatus.COMPLETED));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("418230103", "418230104", "418230106");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_activeAndCompleted() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Arrays.asList(PoolSearchRequestDto.PoolStatus.ACTIVE,
            PoolSearchRequestDto.PoolStatus.COMPLETED));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(4);
        assertThat(results.getData().size()).isEqualTo(4);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).startsWith("4162301");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_requestedAndActive() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Arrays.asList(PoolSearchRequestDto.PoolStatus.REQUESTED,
            PoolSearchRequestDto.PoolStatus.ACTIVE));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("416230101", "416230102", "416230105");
        }
    }

    /**
     * Additional test scenario to cover the Active state of a pool when it has been requested,
     * transferred to the Bureau and created (citizens summonsed) but no responses are received yet so no
     * pool members have an "active" status (RESPONDED, JURY, PANEL).
     */
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_requestedAndActive2() {
        String locCode = "417";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Arrays.asList(PoolSearchRequestDto.PoolStatus.REQUESTED,
            PoolSearchRequestDto.PoolStatus.ACTIVE));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(2);
        assertThat(results.getData().size()).isEqualTo(2);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("417230102", "417230104");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_requestedAndCompleted() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Arrays.asList(PoolSearchRequestDto.PoolStatus.REQUESTED,
            PoolSearchRequestDto.PoolStatus.COMPLETED));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("416230103", "416230104", "416230105");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStatus_all() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Arrays.asList(PoolSearchRequestDto.PoolStatus.REQUESTED,
            PoolSearchRequestDto.PoolStatus.ACTIVE, PoolSearchRequestDto.PoolStatus.COMPLETED));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(5);
        assertThat(results.getData().size()).isEqualTo(5);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStage_bureau() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStage(Collections.singletonList(PoolSearchRequestDto.PoolStage.BUREAU));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        assertThat(results.getData().get(0).getPoolNumber()).isEqualTo("416230101");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStage_court() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStage(Collections.singletonList(PoolSearchRequestDto.PoolStage.COURT));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(4);
        assertThat(results.getData().size()).isEqualTo(4);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("416230102", "416230103", "416230104", "416230105");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolStage_both() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStage(Arrays.asList(PoolSearchRequestDto.PoolStage.COURT,
            PoolSearchRequestDto.PoolStage.BUREAU));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(5);
        assertThat(results.getData().size()).isEqualTo(5);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_activeAtCourt() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Collections.singletonList(PoolSearchRequestDto.PoolStatus.ACTIVE));
        poolSearchRequestDto.setPoolStage(Collections.singletonList(PoolSearchRequestDto.PoolStage.COURT));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        assertThat(results.getData().get(0).getPoolNumber()).isIn("416230102");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_activeWithBureau() {
        String locCode = "416";
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setLocCode(locCode);
        poolSearchRequestDto.setPoolStatus(Collections.singletonList(PoolSearchRequestDto.PoolStatus.ACTIVE));
        poolSearchRequestDto.setPoolStage(Collections.singletonList(PoolSearchRequestDto.PoolStage.BUREAU));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        assertThat(results.getData().get(0).getPoolNumber()).isIn("416230101");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolType_CrownCourt() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setPoolType(Collections.singletonList("CRO"));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        assertThat(results.getData().get(0).getPoolNumber()).isEqualTo("415230101");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolType_CivilCourt() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setPoolType(Collections.singletonList("CIV"));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        assertThat(results.getData().get(0).getPoolNumber()).isEqualTo("417230102");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolType_HighCourt() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setPoolType(Collections.singletonList("HGH"));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(1);
        assertThat(results.getData().size()).isEqualTo(1);

        assertThat(results.getData().get(0).getPoolNumber()).isEqualTo("416230105");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolType_all() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setPoolType(Arrays.asList("CRO", "CIV", "HGH"));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(3);
        assertThat(results.getData().size()).isEqualTo(3);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolType_crownAndCivil() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setPoolType(Arrays.asList("CRO", "CIV"));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(2);
        assertThat(results.getData().size()).isEqualTo(2);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("415230101", "417230102");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolType_crownAndHigh() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setPoolType(Arrays.asList("CRO", "HGH"));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(2);
        assertThat(results.getData().size()).isEqualTo(2);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("415230101", "416230105");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initAdvancedSearchRequests.sql"})
    public void test_searchForPoolRequest_advancedSearch_poolType_civilAndHigh() {
        LocalDate startDate = LocalDate.of(2023, 1, 16);
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setServiceStartDate(startDate);
        poolSearchRequestDto.setPoolType(Arrays.asList("CIV", "HGH"));
        PoolRequestSearchListDto results = poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto,
            new ArrayList<>());

        assertThat(results.getResultsCount()).isEqualTo(2);
        assertThat(results.getData().size()).isEqualTo(2);

        for (PoolRequestSearchListDto.PoolRequestSearchDataDto data : results.getData()) {
            assertThat(data.getPoolNumber()).isIn("417230102", "416230105");
        }
    }

}
