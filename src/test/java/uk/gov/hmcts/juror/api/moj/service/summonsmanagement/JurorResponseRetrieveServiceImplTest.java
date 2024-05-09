package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import com.querydsl.core.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.JurorResponseRetrieveRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.JurorResponseRetrieveResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.juror.api.moj.utils.converters.ConversionUtils.toProperCase;

@ExtendWith(SpringExtension.class)
class JurorResponseRetrieveServiceImplTest {
    private static final String JUROR_X_8 = "11111111";

    private static final String POOL_X_8 = "11111111";
    private static final String FIRST_NAME = "Firstname";
    private static final String LAST_NAME = "Lastname";
    private static final String POSTCODE = "ZZ1 5ZZ";
    private static final String OFFICER_ASSIGNED = "Officer";
    private static final String COURT_NAME = "CARDIFF";

    private static final String BUREAU_OWNER = "400";
    private static final String COURT_OWNER = "415";

    private static final String BUREAU_USER = "BUREAU_USER";
    private static final String COURT_USER = "COURT_USER";

    private static final String BUREAU_STAFF_NAME = "Bureau User";
    private static final String COURT_STAFF_NAME = "Court User";

    private static final LocalDateTime DATE_RECEIVED = LocalDateTime.now();

    @Mock
    private JurorResponseCommonRepositoryMod jurorResponseCommonRepository;

    @Mock
    private AppSettingService appSettingService;

    @InjectMocks
    private JurorResponseRetrieveServiceImpl jurorResponseRetrieveService;

    @DisplayName("Retrieve juror responses based on search criteria")
    @Nested
    class RetrieveJurorResponses {

        @Test
        @DisplayName("Bureau officer - basic search with single criteria is okay")
        void bureauOfficerBasicSearchSingleCriteriaOkay() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setJurorNumber(JUROR_X_8 + "1");

            // mock the database response
            Tuple tuple = createMockedDbResponse(1, ProcessingStatus.TODO);
            List<Tuple> tupleList = new ArrayList<>();
            tupleList.add(tuple);

            doReturn(100).when(appSettingService).getBureauOfficerSearchResultLimit();
            doReturn(tupleList).when(jurorResponseCommonRepository).retrieveJurorResponseDetails(
                request, false, 100);

            // mock jwt payload
            BureauJwtPayload payload = mockJwt(BUREAU_OWNER, BUREAU_USER, BUREAU_STAFF_NAME, UserType.BUREAU);
            TestUtils.mockSecurityUtil(payload);
            // invoke service
            JurorResponseRetrieveResponseDto response =
                jurorResponseRetrieveService.retrieveJurorResponse(request);

            // verify response
            assertThat(response).isNotNull();
            verifyResponse(0, 1, 1, response);
            verify(jurorResponseCommonRepository, times(1))
                .retrieveJurorResponseDetails(request, false, 100);
        }

        @Test
        @DisplayName("Bureau officer - advanced search with single criteria is forbidden")
        void bureauOfficerAdvancedSearchSingleCriteriaForbidden() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setProcessingStatus(Collections.singletonList(ProcessingStatus.TODO));

            // mock jwt payload
            BureauJwtPayload payload = mockJwt(BUREAU_OWNER, BUREAU_USER, BUREAU_STAFF_NAME, UserType.BUREAU);
            TestUtils.mockSecurityUtil(payload);

            MojException.Forbidden exception =
                assertThrows(MojException.Forbidden.class, () ->
                        jurorResponseRetrieveService.retrieveJurorResponse(request),
                    "Should throw an exception");
            assertEquals("Advanced search is only available to team leaders", exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "There should be no cause");

            verify(jurorResponseCommonRepository, never())
                .retrieveJurorResponseDetails(request, false, 100);
        }

        @Test
        @DisplayName("Bureau officer - basic and advanced search (isUrgent criteria is false) is okay")
        void bureauOfficerBasicAndAdvancedSearchIsUrgentFlagFalseIsOkay() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setJurorNumber(JUROR_X_8 + 1);
            request.setIsUrgent(Boolean.FALSE);

            // mock the database response
            Tuple tuple = createMockedDbResponse(1, ProcessingStatus.TODO);
            List<Tuple> tupleList = new ArrayList<>();
            tupleList.add(tuple);
            doReturn(100).when(appSettingService).getBureauOfficerSearchResultLimit();
            doReturn(tupleList).when(jurorResponseCommonRepository).retrieveJurorResponseDetails(
                request, false, 100);

            // mock jwt payload
            BureauJwtPayload payload = mockJwt(BUREAU_OWNER, BUREAU_USER, BUREAU_STAFF_NAME, UserType.BUREAU);
            TestUtils.mockSecurityUtil(payload);

            // invoke service
            JurorResponseRetrieveResponseDto response =
                jurorResponseRetrieveService.retrieveJurorResponse(request);

            // verify response
            assertThat(response).isNotNull();
            verifyResponse(0, 1, 1, response);
            verify(jurorResponseCommonRepository, times(1))
                .retrieveJurorResponseDetails(request, false, 100);
        }

        @Test
        @DisplayName("Bureau officer - advanced search with single criteria - isUrgent is false, is bad request.  "
            + "isUrgent = false, is not considered a search criteria.")
        void bureauOfficerAdvancedSearchSingleCriteriaIsUrgentIsFalseIsBadRequest() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setIsUrgent(Boolean.FALSE);

            BureauJwtPayload payload = mockJwt(BUREAU_OWNER, BUREAU_USER, BUREAU_STAFF_NAME, UserType.BUREAU);
            TestUtils.mockSecurityUtil(payload);

            MojException.BadRequest exception =
                assertThrows(MojException.BadRequest.class, () ->
                        jurorResponseRetrieveService.retrieveJurorResponse(request),
                    "Should throw an exception");
            assertEquals("No search filters supplied", exception.getMessage(),
                "Message should match");

            verify(jurorResponseCommonRepository, never())
                .retrieveJurorResponseDetails(request, false, 100);
        }

        @Test
        @DisplayName("Court officer - search is forbidden")
        void courtOfficerSearchForbidden() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setProcessingStatus(Collections.singletonList(ProcessingStatus.TODO));

            // mock jwt payload
            BureauJwtPayload payload = mockJwt(COURT_OWNER, COURT_USER, COURT_STAFF_NAME, UserType.COURT);
            TestUtils.mockSecurityUtil(payload);

            MojException.Forbidden exception =
                assertThrows(MojException.Forbidden.class, () ->
                        jurorResponseRetrieveService.retrieveJurorResponse(request),
                    "Should throw an exception");
            assertEquals("This service is for Bureau users only", exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "There should be no cause");

            verify(jurorResponseCommonRepository, never())
                .retrieveJurorResponseDetails(request, false, 100);
        }

        @Test
        @DisplayName("Bureau officer - no search criteria provided is bad request")
        void bureauOfficerNoSearchCriteriaBadRequest() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();

            // mock jwt payload
            BureauJwtPayload payload = mockJwt(BUREAU_OWNER, BUREAU_USER, BUREAU_STAFF_NAME, UserType.BUREAU);
            TestUtils.mockSecurityUtil(payload);

            MojException.BadRequest exception =
                assertThrows(MojException.BadRequest.class, () ->
                        jurorResponseRetrieveService.retrieveJurorResponse(request),
                    "Should throw an exception");
            assertEquals("No search filters supplied", exception.getMessage(),
                "Message should match");

            verify(jurorResponseCommonRepository, never())
                .retrieveJurorResponseDetails(request, false, 100);
        }

        private void verifyResponse(int listIndex, int listSize, int postfixId,
                                    JurorResponseRetrieveResponseDto response) {

            String id = String.valueOf(postfixId);

            List<JurorResponseRetrieveResponseDto.JurorResponseDetails> records = response.getRecords();
            assertThat(records).hasSize(listSize);
            assertThat(records.get(listIndex).getJurorNumber()).isEqualTo(JUROR_X_8 + id);
            assertThat(records.get(listIndex).getPoolNumber()).isEqualTo(POOL_X_8 + id);
            assertThat(records.get(listIndex).getFirstName()).isEqualTo(FIRST_NAME + id);
            assertThat(records.get(listIndex).getLastName()).isEqualTo(LAST_NAME + id);
            assertThat(records.get(listIndex).getPostcode()).isEqualTo(POSTCODE);
            assertThat(records.get(listIndex).getCourtName()).isEqualTo(toProperCase(COURT_NAME));
            assertThat(records.get(listIndex).getOfficerAssigned()).isEqualTo(OFFICER_ASSIGNED + id);
            assertThat(records.get(listIndex).getDateReceived()).isEqualTo(DATE_RECEIVED.plusDays(postfixId));
        }

        private BureauJwtPayload mockJwt(String owner, String username, String staffName, UserType userType,
                                         Role... roles) {
            BureauJwtPayload payload = TestUtils.createJwt(owner, username, userType, List.of(roles));
            payload.setStaff(TestUtils.staffBuilder(staffName,  null));
            return payload;
        }

        private Tuple createMockedDbResponse(int postfixId,
                                             ProcessingStatus processingStatus) {
            String id = String.valueOf(postfixId);

            Tuple tuple = mock(Tuple.class);
            doReturn(JUROR_X_8 + id).when(tuple).get(0, String.class);
            doReturn(FIRST_NAME + id).when(tuple).get(1, String.class);
            doReturn(LAST_NAME + id).when(tuple).get(2, String.class);
            doReturn(POSTCODE).when(tuple).get(3, String.class);
            doReturn(processingStatus).when(tuple).get(4, ProcessingStatus.class);
            doReturn(DATE_RECEIVED.plusDays(postfixId)).when(tuple).get(5, LocalDateTime.class);
            doReturn(OFFICER_ASSIGNED + id).when(tuple).get(6, String.class);
            doReturn(POOL_X_8 + id).when(tuple).get(7, String.class);
            doReturn(COURT_NAME).when(tuple).get(8, String.class);

            return tuple;
        }
    }
}