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
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauBacklogAllocateRequestDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for the API endpoints defined in. {@link BureauBacklogAllocateControllerTest}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.LawOfDemeter")
public class BureauBacklogAllocateControllerTest extends AbstractIntegrationTest {

    //@Rule
    //public ExpectedException thrown = ExpectedException.none();

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauBacklogAllocateController_allocateRepliesPost.sql")
    public void backlogAllocateReplies_post_happyPath() throws Exception {

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJwtPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/backlogAllocate/replies");

        RequestEntity<BureauBacklogAllocateRequestDto> request =
            new RequestEntity<>(BureauBacklogAllocateRequestDto.builder()
                .officerAllocations(Arrays.asList(
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2).urgentCount(1)
                        .userId("carneson").build(),
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2).urgentCount(1)
                        .userId("sgomez").build(),
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2).urgentCount(1)
                        .userId("mruby").build()
                )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauBacklogAllocateController_allocateRepliesPost.sql")
    public void backlogAllocateReplies_post_errorPath_noRequestingUser() throws Exception {

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login(null)
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJwtPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());


        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/backlogAllocate/replies");

        RequestEntity<BureauBacklogAllocateRequestDto> request =
            new RequestEntity<>(BureauBacklogAllocateRequestDto.builder()
                .officerAllocations(Arrays.asList(
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2).urgentCount(1)
                        .userId("carneson").build(),
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2).urgentCount(1)
                        .userId("sgomez").build(),
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2).urgentCount(1)
                        .userId("mruby").build()
                )).build(), httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauBacklogAllocateController_allocateRepliesPost.sql")
    public void backlogAllocateReplies_post_missingAllocations() throws Exception {

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJwtPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/backlogAllocate/replies");

        RequestEntity<BureauBacklogAllocateRequestDto> request =
            new RequestEntity<>(BureauBacklogAllocateRequestDto.builder()
                .officerAllocations(Arrays.asList(
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2)
                        .userId("carneson").build(),
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2).urgentCount(1)
                        .userId("sgomez").build(),
                    BureauBacklogAllocateRequestDto.StaffAllocation.builder().nonUrgentCount(2).urgentCount(1).userId(
                        "mruby").build()
                )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }
}
