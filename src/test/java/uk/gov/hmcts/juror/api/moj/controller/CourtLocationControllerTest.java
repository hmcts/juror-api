package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CourtLocationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CourtLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourtLocationService courtLocationService;

    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    @Before
    public void setupMocks() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    //Tests related to operation: getAllCourtLocationsByPostcode
    @Test
    public void getAllCourtLocationsByPostcode_bureau_happy() throws Exception {
        final ArgumentCaptor<String> postcodeCaptor = ArgumentCaptor.forClass(String.class);
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getAllCourtLocationsByPostcode()).when(courtLocationService)
            .getCourtLocationsByPostcode(any(String.class));

        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .queryParam("postcode","SE15")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(2)))
            .andExpect(jsonPath("$.[0].locationCode").value("440"))
            .andExpect(jsonPath("$.[0].locationName").value("Inner London"))
            .andExpect(jsonPath("$.[0].attendanceTime").doesNotExist())
            .andExpect(jsonPath("$.[1].locationCode").value("400"))
            .andExpect(jsonPath("$.[1].locationName").value("Jury Central Summoning Bureau"))
            .andExpect(jsonPath("$.[1].attendanceTime").doesNotExist());

        verify(courtLocationService, times(1)).getCourtLocationsByPostcode(postcodeCaptor.capture());
        assertThat(postcodeCaptor.getValue()).isEqualTo("SE15");
    }

    @Test
    public void getAllCourtLocationsByPostcode_court_happy() throws Exception {
        final ArgumentCaptor<String> postcodeCaptor = ArgumentCaptor.forClass(String.class);
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getAllCourtLocationsByPostcode()).when(courtLocationService)
            .getCourtLocationsByPostcode(any(String.class));

        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .queryParam("postcode","SE16")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(2)))
            .andExpect(jsonPath("$.[0].locationCode").value("440"))
            .andExpect(jsonPath("$.[0].locationName").value("Inner London"))
            .andExpect(jsonPath("$.[0].attendanceTime").doesNotExist())
            .andExpect(jsonPath("$.[1].locationCode").value("400"))
            .andExpect(jsonPath("$.[1].locationName").value("Jury Central Summoning Bureau"))
            .andExpect(jsonPath("$.[1].attendanceTime").doesNotExist());

        verify(courtLocationService, times(1)).getCourtLocationsByPostcode(postcodeCaptor.capture());
        assertThat(postcodeCaptor.getValue()).isEqualTo("SE16");
    }

    @Test
    public void getAllCourtLocationsByPostcode_invalidPath() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getAllCourtLocationsByPostcode()).when(courtLocationService)
            .getCourtLocationsByPostcode(any(String.class));

        mockMvc.perform(get("/api/v1/moj/court-location/all-court-locations/postc0de/SE16LA")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }

    @Test
    public void getAllCourtLocationsByPostcode_incorrectHttpOperation() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getAllCourtLocationsByPostcode()).when(courtLocationService)
            .getCourtLocationsByPostcode(any(String.class));

        mockMvc.perform(post("/api/v1/moj/court-location/catchment-areas?postcode=SE16LA")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isMethodNotAllowed());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }
    @Test
    public void getAllCourtLocationsByPostcode_longPostcode() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas?postcode=SE1235AJ")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }

    @Test
    public void getAllCourtLocationsByPostcode_noPostcode() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }
    @Test
    public void getAllCourtLocationsByPostcode_blankPostcode() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .queryParam("postcode","")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }

    @Test
    public void getAllCourtLocationsByPostcode_invalidPostcode_regExValidationFailure_format() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doThrow(MojException.BadRequest.class).when(courtLocationService)
            .getCourtLocationsByPostcode(any(String.class));

        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas?postcode=6LASE1")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }

    @Test
    public void getAllCourtLocationsByPostcode_invalidPostcode_space() throws Exception {
        BureauJWTPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .queryParam("postcode","A BC")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }

    private List<CourtLocationDataDto> getAllCourtLocationsByPostcode() {
        return List.of(
            new CourtLocationDataDto(
                "440",
                "Inner London",
                null),
            new CourtLocationDataDto(
                "400",
                "Jury Central Summoning Bureau",
                null)
        );
    }
}
