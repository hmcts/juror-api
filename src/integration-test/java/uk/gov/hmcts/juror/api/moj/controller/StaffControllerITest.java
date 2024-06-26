package uk.gov.hmcts.juror.api.moj.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + StaffControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Sql(scripts = "/db/mod/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@SuppressWarnings("PMD.TooManyMethods")
public class StaffControllerITest extends AbstractIntegrationTest {
    static final String BASE_URL = "/api/v1/moj/staff";

    private HttpHeaders httpHeaders;
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JurorPaperResponseRepositoryMod paperResponseRepositoryMod;

    @Autowired
    private JurorDigitalResponseRepositoryMod digitalResponseRepositoryMod;


    @BeforeEach
    public void setUp() throws Exception {
        initHeadersTeamLeader();
    }


    private void initHeadersTeamLeader() {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .roles(Set.of(Role.MANAGER))
            .login("smcbob")
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private void initHeadersNormalUser() {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .login("jmcbob")
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private void initHeadersBadUsername() {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .roles(Set.of(Role.MANAGER))
            .login("jmccbob")
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private void initHeadersCourtUser() {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.COURT)
            .roles(Set.of(Role.MANAGER))
            .login("jmcbob")
            .owner("415")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    @Nested
    class Paper {
        @Nested
        class Positive {
            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void assignToUser() {
                String jurorNumber = "586856852";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Expect the HTTP POST request to be OK")
                    .isEqualTo(HttpStatus.OK);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("jmcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void nullStaffAssignment() {
                String jurorNumber = "586856851";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo(null)
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Expect the HTTP POST request to be OK")
                    .isEqualTo(HttpStatus.OK);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff()).isNull();

            }
        }

        @Nested
        class Negative {
            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void nullStaffAssignmentNotTeamLeader() {
                initHeadersNormalUser();
                String jurorNumber = "586856851";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo(null)
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.FORBIDDEN);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void noStaffRecord() {
                initHeadersBadUsername();
                String jurorNumber = "586856852";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.NOT_FOUND);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void noJurorResponse() {
                String jurorNumber = "999999999";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void noAssignmentTargetStaffRecord() {
                String jurorNumber = "586856852";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmccbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.NOT_FOUND);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");

            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void refuseAutoUser() {
                String jurorNumber = "586856852";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("AUTO")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void urgentJurorResponse() {
                String jurorNumber = "586856853";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo(null)
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");

            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void incorrectProcessingStatus() {
                String jurorNumber = "586856855";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo(null)
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void processingStatusComplete() {
                String jurorNumber = "586856856";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");

            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void processingStatusClosed() {
                String jurorNumber = "586856857";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void courtUser() {
                initHeadersCourtUser();
                String jurorNumber = "586856857";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.FORBIDDEN);

                PaperResponse paperResponse = paperResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(paperResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }
        }
    }

    @Nested
    class Digital {
        @Nested
        class Positive {
            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void assignToUser() {
                String jurorNumber = "686856852";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Expect the HTTP POST request to be OK")
                    .isEqualTo(HttpStatus.OK);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("jmcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void nullStaffAssignment() {
                String jurorNumber = "686856851";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo(null)
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Expect the HTTP POST request to be OK")
                    .isEqualTo(HttpStatus.OK);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff()).isNull();

            }
        }

        @Nested
        class Negative {
            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void nullStaffAssignmentNotTeamLeader() {
                initHeadersNormalUser();
                String jurorNumber = "686856851";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo(null)
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.FORBIDDEN);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void noStaffRecord() {
                initHeadersBadUsername();
                String jurorNumber = "686856852";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.NOT_FOUND);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void noJurorResponse() {
                String jurorNumber = "999999999";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void noAssignmentTargetStaffRecord() {
                String jurorNumber = "686856852";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmccbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.NOT_FOUND);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");

            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void refuseAutoUser() {
                String jurorNumber = "686856852";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("AUTO")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void urgentJurorResponse() {
                String jurorNumber = "686856853";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo(null)
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");

            }

            @Test
            @SneakyThrows
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void incorrectProcessingStatus() {
                String jurorNumber = "686856855";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo(null)
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void processingStatusComplete() {
                String jurorNumber = "686856856";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");

            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void processingStatusClosed() {
                String jurorNumber = "686856857";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }

            @Test
            @Sql({"/db/mod/truncate.sql", "/db/staff/changeAssignment.sql"})
            void courtUser() {
                initHeadersCourtUser();
                String jurorNumber = "686856857";
                StaffAssignmentRequestDto requestDto = StaffAssignmentRequestDto.builder()
                    .assignTo("jmcbob")
                    .responseJurorNumber(jurorNumber)
                    .version(0)
                    .build();

                ResponseEntity<StaffAssignmentResponseDto> response =
                    restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                        URI.create(BASE_URL + "/assign")), StaffAssignmentResponseDto.class);

                assertThat(response.getStatusCode())
                    .as("Http Status")
                    .isEqualTo(HttpStatus.FORBIDDEN);

                DigitalResponse digitalResponse = digitalResponseRepositoryMod.findByJurorNumber(jurorNumber);
                assertThat(digitalResponse.getStaff().getUsername()).as("Username").isEqualTo("smcbob");
            }
        }
    }
}
