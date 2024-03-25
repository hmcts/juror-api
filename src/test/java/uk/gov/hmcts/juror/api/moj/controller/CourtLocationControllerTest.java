package uk.gov.hmcts.juror.api.moj.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.RestfulAuthenticationEntryPoint;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("PMD.ExcessiveImports")
@ExtendWith(SpringExtension.class)
@WebMvcTest(CourtLocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourtLocationControllerTest {

    private static final String BASE_URL = "/api/v1/moj/court-location";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourtLocationService courtLocationService;

    @MockBean
    private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    //Tests related to operation: getAllCourtLocationsByPostcode
    @Test
    void retrieveAllCourtLocationsByPostcodeBureauHappy() throws Exception {
        final ArgumentCaptor<String> postcodeCaptor = ArgumentCaptor.forClass(String.class);
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getAllCourtLocationsByPostcode()).when(courtLocationService)
            .getCourtLocationsByPostcode(any(String.class));

        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .queryParam("postcode", "SE15")
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
    void retrieveAllCourtLocationsByPostcodeCourtHappy() throws Exception {
        final ArgumentCaptor<String> postcodeCaptor = ArgumentCaptor.forClass(String.class);
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");

        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

        doReturn(getAllCourtLocationsByPostcode()).when(courtLocationService)
            .getCourtLocationsByPostcode(any(String.class));

        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .queryParam("postcode", "SE16")
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
    void retrieveAllCourtLocationsByPostcodeInvalidPath() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("415", "COURT_USER");
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
    void retrieveAllCourtLocationsByPostcodeIncorrectHttpOperation() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    void retrieveAllCourtLocationsByPostcodeLongPostcode() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas?postcode=SE1235AJ")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }

    @Test
    void retrieveAllCourtLocationsByPostcodeNoPostcode() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }

    @Test
    void retrieveAllCourtLocationsByPostcodeBlankPostcode() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .queryParam("postcode", "")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(courtLocationService, never()).getCourtLocationsByPostcode(any(String.class));
    }

    @Test
    void retrieveAllCourtLocationsByPostcodeInvalidPostcodeRegExValidationFailureFormat() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
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
    void retrieveAllCourtLocationsByPostcodeInvalidPostcodeSpace() throws Exception {
        BureauJwtPayload jwtPayload = TestUtils.createJwt("400", "BUREAU_USER");
        BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
        when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);


        mockMvc.perform(get("/api/v1/moj/court-location/catchment-areas")
                .principal(mockPrincipal)
                .queryParam("postcode", "A BC")
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

    @Nested
    class GetCourtRates
        extends AbstractControllerTest<Void, GetCourtRates> {
        private static final String URL = BASE_URL + "/{loc_code}/rates";
        private static final BureauJwtAuthentication MOCK_PRINCIPAL = mock(BureauJwtAuthentication.class);

        protected GetCourtRates() {
            super(HttpMethod.GET, URL, MOCK_PRINCIPAL);
            BureauJwtPayload bureauJwtPayload = TestUtils.createJwt("415", "COURT_USER");
            when(MOCK_PRINCIPAL.getPrincipal()).thenReturn(bureauJwtPayload);
        }

        @BeforeEach
        void beforeEach() {
            this.setMockMvc(mockMvc);
        }

        @Test
        void positiveTypical() throws Exception {
            send(null, HttpStatus.OK, "415");
            verify(courtLocationService, times(1))
                .getCourtRates("415");
        }

        @Test
        void negativeInvalidLocCode() throws Exception {
            send(null, HttpStatus.BAD_REQUEST, "INVALID");
            verifyNoInteractions(courtLocationService);
        }
    }
}
