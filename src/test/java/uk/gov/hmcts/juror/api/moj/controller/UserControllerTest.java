package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CreateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UpdateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        UserController.class,
        RestResponseEntityExceptionHandler.class
    }
)
@DisplayName("Controller: " + UserControllerTest.BASE_URL)
@SuppressWarnings({"PMD.LawOfDemeter","PMD.ExcessiveImports"})
class UserControllerTest {

    public static final String BASE_URL = "/api/v1/moj/users";
    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private UserService userService;
    @InjectMocks
    private UserController userController;


    @Nested
    @DisplayName("POST " + ViewAllUsers.URL)
    class ViewAllUsers {
        public static final String URL = BASE_URL;

        @Test
        void positiveTypical() throws Exception {
            PaginatedList<UserDetailsDto> response = new PaginatedList<>();
            response.setData(List.of(
                UserDetailsDto.builder().username("name1").build(),
                UserDetailsDto.builder().username("name2").build()
            ));

            doReturn(response).when(userService).getUsers(any());

            UserSearchDto request = UserSearchDto.builder()
                .pageLimit(25)
                .pageNumber(1)
                .build();

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(response)));

            verify(userService, times(1))
                .getUsers(request);
            verifyNoMoreInteractions(userService);
        }

        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void negativeInvalidPayload() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(new UserSearchDto())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST " + CreateUser.URL)
    class CreateUser {
        public static final String URL = BASE_URL + "/create";

        @Test
        void positiveTypical() throws Exception {
            CreateUserDto request = CreateUserDto.builder()
                .userType(UserType.BUREAU)
                .email("email")
                .name("name")
                .build();

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

            verify(userService, times(1))
                .createUser(request);
            verifyNoMoreInteractions(userService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(CreateUserDto.builder()
                        .userType(UserType.BUREAU)
                        .name("name")
                        .build())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(userService);
        }
    }

    @Nested
    @DisplayName("GET " + GetUser.URL)
    class GetUser {
        public static final String URL = BASE_URL + "/{username}";


        private String toUrl(String username) {
            return URL.replace("{username}", username);
        }

        @Test
        void positiveTypical() throws Exception {
            UserDetailsDto response = UserDetailsDto.builder()
                .username("Username")
                .build();
            when(userService.getUser("Username"))
                .thenReturn(response);

            mockMvc.perform(get(toUrl("Username")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(response)));

            verify(userService, times(1))
                .getUser("Username");
            verifyNoMoreInteractions(userService);
        }
    }

    @Nested
    @DisplayName("PUT " + UpdateUser.URL)
    class UpdateUser {
        public static final String URL = BASE_URL + "/{username}";


        private String toUrl(String username) {
            return URL.replace("{username}", username);
        }

        @Test
        void positiveTypical() throws Exception {
            UpdateUserDto request = UpdateUserDto.builder()
                .email("email")
                .name("name")
                .isActive(true)
                .build();

            mockMvc.perform(put(toUrl("Username"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

            verify(userService, times(1))
                .updateUser("Username", request);
            verifyNoMoreInteractions(userService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            mockMvc.perform(put(toUrl("Username"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(UpdateUserDto.builder()
                        .email("email")
                        .name("name")
                        .build())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(userService);
        }
    }

    @Nested
    @DisplayName("PATCH " + UpdateUser.URL)
    class AddCourt {
        public static final String URL = BASE_URL + "/{username}/courts";


        private String toUrl(String username) {
            return URL.replace("{username}", username);
        }

        @Test
        void positiveTypical() throws Exception {
            List<String> courts = List.of("401", "402", "403");

            mockMvc.perform(patch(toUrl("Username"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(courts)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

            verify(userService, times(1))
                .addCourt("Username", courts);
            verifyNoMoreInteractions(userService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            List<String> courts = List.of("401", "4042", "403");

            mockMvc.perform(patch(toUrl("Username"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(courts)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(userService);
        }
    }

    @Nested
    @DisplayName("DELETE " + RemoveCourt.URL)
    class RemoveCourt {
        public static final String URL = BASE_URL + "/{username}/courts";


        private String toUrl(String username) {
            return URL.replace("{username}", username);
        }

        @Test
        void positiveTypical() throws Exception {
            List<String> courts = List.of("401", "402", "403");

            mockMvc.perform(delete(toUrl("Username"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(courts)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

            verify(userService, times(1))
                .removeCourt("Username", courts);
            verifyNoMoreInteractions(userService);
        }

        @Test
        void negativeInvalidPayload() throws Exception {
            List<String> courts = List.of("401", "4042", "403");
            mockMvc.perform(delete(toUrl("Username"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(courts)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(userService);
        }
    }

    @Nested
    @DisplayName("PATCH " + UpdateUserType.URL)
    class UpdateUserType {
        public static final String URL = BASE_URL + "/{username}/type/{type}";


        private String toUrl(String username, String type) {
            return URL.replace("{username}", username)
                .replace("{type}", type);
        }

        @Test
        void positiveTypical() throws Exception {
            mockMvc.perform(patch(toUrl("Username", UserType.BUREAU.name())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

            verify(userService, times(1))
                .changeUserType("Username", UserType.BUREAU);
            verifyNoMoreInteractions(userService);
        }

        @Test
        void negativeInvalidType() throws Exception {
            mockMvc.perform(patch(toUrl("Username", "INVALID")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
            verifyNoMoreInteractions(userService);
        }
    }
}
