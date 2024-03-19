package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.service.PoolCreateService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CreatePoolController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        CreatePoolController.class,
        RestResponseEntityExceptionHandler.class
    }
)
class CreatePoolControllerTest {

    private static final String BASE_URL = "/api/v1/moj/pool-create";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PoolCreateService poolCreateService;

    @InjectMocks
    private CreatePoolController createPoolController;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;


    @Nested
    class GetThinPoolMembers {
        private static final String URL = BASE_URL + "/members/{poolNumber}";

        GetThinPoolMembers() {
        }

        @BeforeEach
        void beforeEach() {
            securityUtilMockedStatic = Mockito.mockStatic(SecurityUtil.class);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        void positiveTypical() throws Exception {
            Mockito.doReturn(new ArrayList<String>()).when(poolCreateService).getThinJurorPoolsList("415240501", "415");

            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn("415");

            mockMvc.perform(get(URL.replace("{poolNumber}", "415240501")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

            Mockito.verify(poolCreateService, Mockito.times(1))
                .getThinJurorPoolsList("415240501", "415");
        }

        @Test
        void invalidLocCode() throws Exception {
            mockMvc.perform(get(URL.replace("{poolNumber}", "0000")))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is4xxClientError());

            Mockito.verifyNoInteractions(poolCreateService);
        }
    }
}
