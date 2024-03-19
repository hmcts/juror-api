package uk.gov.hmcts.juror.api.juror.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.config.public_.PublicJWTPayload;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorHolidaysRequestDto;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorDetailDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorHolidaysResponseDto;
import uk.gov.hmcts.juror.api.juror.service.HolidaysService;
import uk.gov.hmcts.juror.api.juror.service.JurorPersistenceService;
import uk.gov.hmcts.juror.api.juror.service.JurorService;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;

/**
 * API endpoints controller for Public Juror Endpoints.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/public", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Public API", description = "Public Interface API")
@SuppressWarnings("PMD")
public class PublicEndpointController {
    private final JurorPersistenceService jurorPersistenceService;
    private final JurorService jurorService;
    private final SpringValidatorAdapter validator;
    private final HolidaysService holidaysService;

    private final JurorResponseService jurorResponseService ;

    @Autowired
    public PublicEndpointController(final JurorPersistenceService jurorPersistenceService,
                                    final JurorService jurorService,
                                    final HolidaysService holidaysService,
                                    final JurorResponseService jurorResponseService,
                                    @SuppressWarnings("SpringJavaAutowiringInspection") final SpringValidatorAdapter validator) {
        Assert.notNull(jurorPersistenceService, "JurorPersistenceService cannot be null.");
        Assert.notNull(jurorService, "JurorService cannot be null.");
        Assert.notNull(holidaysService, "HolidaysService cannot be null");
        Assert.notNull(jurorResponseService, "JurorResponseService cannot be null");
        Assert.notNull(validator, "Validator cannot be null.");
        this.jurorPersistenceService = jurorPersistenceService;
        this.jurorService = jurorService;
        this.holidaysService = holidaysService;
        this.jurorResponseService = jurorResponseService;
        this.validator = validator;
    }

    /**
     * Return the existing juror summons details for confirmation by the juror.
     *
     * @param principal   The authenticated juror
     * @param jurorNumber The juror number to show the details of
     * @return Juror details
     */
    @GetMapping(path = "/juror/{jurorNumber}")
    @Operation(summary = "Find juror details by Juror Number",
        description = "The existing juror summons details for confirmation by the juror")
    public ResponseEntity<JurorDetailDto> retrieveJurorById(
        @Parameter(hidden = true) @AuthenticationPrincipal PublicJWTPayload principal,
        @Parameter(description = "Juror number", required = true) @PathVariable String jurorNumber) {
        if (ObjectUtils.isEmpty(jurorNumber) || !principal.getJurorNumber().equals(jurorNumber)) {
            log.warn(
                "Rejected juror details request. Auth juror {}, expected {}",
                principal.getJurorNumber(),
                jurorNumber
            );
            throw new InvalidJwtAuthenticationException("Invalid Authentication token - juror number mismatch");
        }

        final JurorDetailDto jurorDetailDto = jurorService.getJurorByJurorNumber(jurorNumber);
        if (jurorDetailDto == null) {
            log.warn("No result for juror number {}", jurorNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(jurorDetailDto);
    }

    /**
     * Get Dates Chosen by Juror.
     * Response if date is Bank Holiday.
     *
     * @param jurorHolidaysRequestDto
     * @param principal
     */
    @PostMapping("/deferral-dates")
    @Operation(summary = "/confirm_date/deferral-dates",
        description = "Retrieve dates if Bank Holidays")
    public ResponseEntity<JurorHolidaysResponseDto> jurorHolidayDates(
        @Parameter(hidden = true) @AuthenticationPrincipal PublicJWTPayload principal,
        @Validated @RequestBody JurorHolidaysRequestDto jurorHolidaysRequestDto) {

        final JurorHolidaysResponseDto jurorHolidaysResponseDto = new JurorHolidaysResponseDto();
        jurorHolidaysResponseDto.setMatchingHolidayDates(
            holidaysService.getMatchingHolidayDates(jurorHolidaysRequestDto));
        return ResponseEntity.ok().body(jurorHolidaysResponseDto);
    }


    /**
     * Process the Juror Response from the Juror.
     *
     * @param principal   Authenticated juror context
     * @param responseDto Response information to persist
     * @return Success message
     */
    @PostMapping(path = "/juror/respond")
    @Operation(summary = "Save a Juror response", description = "Process the Juror Response from the"
        + " Juror and save")
    public ResponseEntity<String> respondToSummons(
        @Parameter(hidden = true) @AuthenticationPrincipal PublicJWTPayload principal,
        @RequestBody JurorResponseDto responseDto)
        throws NoSuchMethodException, MethodArgumentNotValidException {

        // BEGIN - custom validation
        Class<?> validationGroup = null;
        responseDto.setReplyMethod(ReplyMethod.DIGITAL);
        if (!ObjectUtils.isEmpty(responseDto.getThirdParty())
            && !ObjectUtils.isEmpty(responseDto.getThirdParty().getThirdPartyReason())) {
            if ("deceased".equalsIgnoreCase(responseDto.getThirdParty().getThirdPartyReason().trim())) {
                validationGroup = JurorResponseDto.ThirdPartyDeceasedValidationGroup.class;
                log.debug("Third party DECEASED validation rules applied: {}", validationGroup);
            } else {
                if (!ObjectUtils.isEmpty(responseDto.getDateOfBirth())) {
                    validationGroup = JurorResponseDto.ThirdPartyValidationGroup.class;
                    log.debug("Third party validation rules applied: {}", validationGroup);
                    log.debug("Manually validating third party phone details");
                    validateThirdPartyPhoneNumberSettings(responseDto);
                    log.debug("Manually validating third party email details");
                    validateThirdPartyEmailSettings(responseDto);
                } else {
                    log.error("A third party response for a living juror must contain a date of birth");
                    throw new IllegalStateException("Invalid response");
                }
            }
        } else if (!ObjectUtils.isEmpty(responseDto.getDateOfBirth())) {

            if (responseDto.getQualify() != null) {
                validationGroup = JurorResponseDto.FirstPersonValidationGroup.class;
                log.debug("First person validation rules applied: {}", validationGroup);
            } else {
                validationGroup = JurorResponseDto.IneligibleAgeValidationGroup.class;
                log.debug("Ineligible age validation rules applied: {}", validationGroup);
            }

        } else {
            validationGroup = Default.class;
            log.debug("Validation rules applied: {}", validationGroup);
        }

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(responseDto, "responseDto");
        validator.validate(responseDto, errors, validationGroup);
        if (errors.hasErrors()) {
            log.warn("Validation failed on {} constraints", errors.getErrorCount());
            if (log.isTraceEnabled()) {
                log.trace("Failed constraints: {}", errors);
            }

            final MethodParameter methodParameter = new MethodParameter(this.getClass().getDeclaredMethod(
                "respondToSummons",
                PublicJWTPayload.class,
                JurorResponseDto.class
            ), 1);
            throw new MethodArgumentNotValidException(methodParameter, errors);
        }
        //END - custom validation

        if (!principal.getJurorNumber().equals(responseDto.getJurorNumber())) {
            log.warn("Rejected juror details request. Auth juror {}, expected {}", principal.getJurorNumber(),
                responseDto.getJurorNumber()
            );
            throw new InvalidJwtAuthenticationException("Invalid Authentication token - juror number mismatch");
        }
        jurorPersistenceService.persistJurorResponse(responseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Saved");
    }


    private void validateThirdPartyPhoneNumberSettings(JurorResponseDto responseDto) {
        if (null != responseDto.getThirdParty() && null != responseDto.getThirdParty().getUseJurorPhoneDetails()
            && responseDto.getThirdParty().getUseJurorPhoneDetails().equals(Boolean.TRUE)) {
            log.trace(
                "Response for Juror {} is set to use the jurors phone, beginning manual validation.",
                responseDto.getJurorNumber()
            );
            if (null == responseDto.getPrimaryPhone()) {
                // they haven't provided a jurors phone

                if (null == responseDto.getQualify()) {
                    // Juror is age ineligible, so they didn't get to provide a juror number
                    // but frontend posts useJurorPhone as true by default, so set to false and ignore
                    log.trace("Response for Juror {} is age-ineligible and haven't provided a Juror phone number, "
                        + "setting useJurorPhoneDetails to false");
                    responseDto.getThirdParty().setUseJurorPhoneDetails(Boolean.FALSE);
                    return;
                }

                log.debug(
                    "Juror response {} failed as Juror phone was not provided but set as main contact",
                    responseDto.getJurorNumber()
                );
                throw new InvalidPhoneNumberSettingsProvided();
            }
        }
    }

    private void validateThirdPartyEmailSettings(JurorResponseDto responseDto) {
        if (null != responseDto.getThirdParty() && null != responseDto.getThirdParty().getUseJurorEmailDetails()
            && responseDto.getThirdParty().getUseJurorEmailDetails().equals(Boolean.TRUE)) {
            log.trace(
                "Response for Juror {} is set to use the jurors email, beginning manual validation.",
                responseDto.getJurorNumber()
            );
            if (null == responseDto.getEmailAddress()) {
                // they haven't provided a jurors email

                if (null == responseDto.getQualify()) {
                    // Juror is age ineligible, so they didn't get to provide a juror email
                    // but frontend posts useJurorEmail as true by default, so set to false and ignore
                    log.trace("Response for Juror {} is age-ineligible and haven't provided a Juror email, "
                        + "setting useJurorPhoneDetails to false");
                    responseDto.getThirdParty().setUseJurorEmailDetails(Boolean.FALSE);
                    return;
                }

                log.debug(
                    "Juror response {} failed as Juror email was not provided but set as main contact",
                    responseDto.getJurorNumber()
                );
                throw new InvalidEmailSettingsProvided("You have requested to use the Juror's email details as"
                    + " the point of contact but have not provided an email for the Juror.");
            }
        } else if (null != responseDto.getThirdParty() && null != responseDto.getThirdParty().getUseJurorEmailDetails()
            && responseDto.getThirdParty().getUseJurorEmailDetails().equals(Boolean.FALSE)
            && null == responseDto.getThirdParty().getEmailAddress()) {
            // we are using the third party email as default but no third party email provided
            log.debug(
                "Juror response {} failed as Third Party email was not provided but set as main contact",
                responseDto.getJurorNumber()
            );
            throw new InvalidEmailSettingsProvided("You have requested to use the Third Party email details"
                + " as the point of contact but have not provided an email for the Third Party.");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidPhoneNumberSettingsProvided extends RuntimeException {
        public InvalidPhoneNumberSettingsProvided() {
            super("You have requested to use the Juror's phone details as the point of contact but have not "
                + "provided a number for the Juror.");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidEmailSettingsProvided extends RuntimeException {
        public InvalidEmailSettingsProvided(String msg) {
            super(msg);
        }
    }
}
