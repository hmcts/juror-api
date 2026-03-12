package uk.gov.hmcts.juror.api.jurorer.controller;

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
import uk.gov.hmcts.juror.api.config.jurorer.JurorErJwtPayload;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaUserDetailsDto;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + LaUserControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
@Sql({"/db/jurorer/teardownUsers.sql", "/db/jurorer/createUsers.sql"})
public class LaUserControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/juror-er/users";

    private final TestRestTemplate restTemplate;
    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeadersLaUsers("001");
    }


    @Nested
    @DisplayName("GET Juror ER local authority users")
    class GetLaUsers  {
        private static final String URL = BASE_URL + "/001";

        @DisplayName("Positive")
        @Nested
        class Positive {

            @Test
            void laUsersHappyPath() {

                ResponseEntity<LaUserDetailsDto> response =
                    restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(URL)),
                                                                            LaUserDetailsDto.class);
                assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

                LaUserDetailsDto laUserDetailsDto = response.getBody();
                assertThat(laUserDetailsDto).isNotNull();
                assertThat(laUserDetailsDto.getLaUserDetails()).hasSize(2);

                List<LaUserDetailsDto.LaUserDetails> laUsers = laUserDetailsDto.getLaUserDetails();
                assertThat(laUsers).extracting("username")
                    .containsExactlyInAnyOrder("test_user1@localauthority1.council.uk",
                                                        "test_user2@localauthority1.council.uk");

            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void laUsersInvalidLaAndTooLong() {
                ResponseEntity<LaUserDetailsDto> response =
                        restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
                                        URI.create(BASE_URL + "/13323456789")),
                                LaUserDetailsDto.class);
                assertThat(response.getStatusCode()).as("Expect the status to be bad request")
                        .isEqualTo(HttpStatus.BAD_REQUEST);
            }

            @Test
            void laUsersInvalidLaAndWrongType() {
                ResponseEntity<LaUserDetailsDto> response =
                        restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(BASE_URL + "/bad")),
                                LaUserDetailsDto.class);
                assertThat(response.getStatusCode()).as("Expect the status to be bad request")
                        .isEqualTo(HttpStatus.BAD_REQUEST);
            }

            @Test
            void noLaUsersFoundAsNoneExist() {
                initHeadersLaUsers("003");
                ResponseEntity<LaUserDetailsDto> response =
                        restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(BASE_URL + "/003")),
                                LaUserDetailsDto.class);
                assertThat(response.getStatusCode()).as("Expect the status to be ok")
                        .isEqualTo(HttpStatus.OK);
                LaUserDetailsDto laUserDetailsDto = response.getBody();
                assertThat(laUserDetailsDto).isNotNull();
                assertThat(laUserDetailsDto.getLaUserDetails()).isEmpty();
            }

            @Test
            void laUsersNotActive() {
                initHeadersLaUsers("004");
                ResponseEntity<LaUserDetailsDto> response =
                        restTemplate.exchange(new RequestEntity<>(httpHeaders, GET, URI.create(BASE_URL + "/004")),
                                LaUserDetailsDto.class);
                assertThat(response.getStatusCode()).as("Expect the status to be forbidden")
                        .isEqualTo(HttpStatus.FORBIDDEN);
            }

        }
    }

    private void initHeadersLaUsers(String laCode) {

        JurorErJwtPayload payload = JurorErJwtPayload.builder()
            .username("la_user_1")
            .laCode(laCode)
            .laName("Local Authority X")
            .roles(Collections.singletonList("LA_USER"))
            .build();

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintJurorErJwt(payload));
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    }
}
