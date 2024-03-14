package uk.gov.hmcts.juror.api.bureau.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauResponseStatusUpdateDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.bureau.service.ResponseAlreadyMergedException;
import uk.gov.hmcts.juror.api.bureau.service.ResponseUpdateService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.validation.DateOfBirth;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_PRIMARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_SECONDARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * Controller endpoints for updating a juror response.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/juror/{jurorId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Response Edit API", description = "Bureau operations relating to editing a juror response.")
public class ResponseUpdateController {
    private final ResponseUpdateService responseUpdateService;

    @Autowired
    public ResponseUpdateController(final ResponseUpdateService responseUpdateService) {
        Assert.notNull(responseUpdateService, "ResponseUpdateService cannot be null");
        this.responseUpdateService = responseUpdateService;
    }

    @GetMapping("/notes")
    @Operation(summary = "Get notes for a specific juror response",
        description = "Retrieve notes of a single juror response by their juror number")
    public ResponseEntity<JurorNoteDto> jurorNoteByJurorNumber(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId) {
        validateJurorNumberPathVariable(jurorId);

        final JurorNoteDto notesDto = responseUpdateService.notesByJurorNumber(jurorId);
        return ResponseEntity.ok().body(notesDto);
    }

    @PutMapping("/notes")
    @Operation(summary = "notes for a specific juror response",
        description = "Update notes of a single juror response by their juror number")
    public ResponseEntity<Void> updateNoteByJurorNumber(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        @Parameter(hidden = true) BureauJwtAuthentication jwt,
        @Validated @RequestBody JurorNoteDto noteDto) {
        validateJurorNumberPathVariable(jurorId);

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) jwt.getPrincipal();
        responseUpdateService.updateNote(noteDto, jurorId, jwtPayload.getLogin());
        log.info("Updated notes for juror {}", jurorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/phone")
    @Operation(summary = "phone log entry for specific juror response",
        description = "Insert new phone log to a single juror response by their juror number")
    public ResponseEntity<Void> updatePhoneLogByJurorNumber(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Validated @RequestBody JurorPhoneLogDto phoneLogDto) {
        validateJurorNumberPathVariable(jurorId);
        responseUpdateService.updatePhoneLog(phoneLogDto, jurorId, payload.getLogin());
        log.info("Updated phone log for juror {}", jurorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Response update for a first person response - juror details section.
     *
     * @param jurorId                    Juror response to update
     * @param jwt                        Spring supplied security principal
     * @param firstPersonJurorDetailsDto Updated content
     * @return {@link org.springframework.http.HttpStatus#NO_CONTENT}
     * @throws BureauOptimisticLockingException Response was updated previously and the data in this request is stale.
     * @throws ResponseAlreadyMergedException   Response has been merged to Juror previously - not allowed to edit.
     */
    @PostMapping("/details/first-person")
    @Operation(summary = "changes to juror response - first "
        + "person juror details",
        description = "Juror Details (first person) edit")
    public ResponseEntity<Void> updateJurorDetailsFirstPerson(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        @Parameter(hidden = true) BureauJwtAuthentication jwt,
        @Validated @RequestBody FirstPersonJurorDetailsDto firstPersonJurorDetailsDto)
        throws ResponseAlreadyMergedException {
        validateJurorNumberPathVariable(jurorId);

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) jwt.getPrincipal();
        responseUpdateService.updateJurorDetailsFirstPerson(firstPersonJurorDetailsDto, jurorId, jwtPayload
            .getLogin());
        log.info("Updated first person juror details section for juror {}", jurorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Response update for a third party response - juror details section.
     *
     * @param jurorId                   Juror response to update
     * @param jwt                       Spring supplied security principal
     * @param thirdPartyJurorDetailsDto Updated content
     * @return {@link org.springframework.http.HttpStatus#NO_CONTENT}
     * @throws BureauOptimisticLockingException Response was updated previously and the data in this request is stale.
     * @throws ResponseAlreadyMergedException   Response has been merged to Juror previously - not allowed to edit.
     */
    @PostMapping("/details/third-party")
    @Operation(summary = "changes to juror response - third party "
        + "juror details",
        description = "Juror Details (third party) edit")
    public ResponseEntity<Void> updateJurorDetailsThirdParty(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        @Parameter(hidden = true) BureauJwtAuthentication jwt,
        @Validated @RequestBody ThirdPartyJurorDetailsDto thirdPartyJurorDetailsDto)
        throws ResponseAlreadyMergedException {
        validateJurorNumberPathVariable(jurorId);

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) jwt.getPrincipal();
        responseUpdateService.updateJurorDetailsThirdParty(thirdPartyJurorDetailsDto, jurorId, jwtPayload.getLogin());
        log.info("Updated third party juror details section for juror {}", jurorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Response update for response - juror eligibility details section.
     *
     * @param jurorId             Juror response to update
     * @param jwt                 Spring supplied security principal
     * @param jurorEligibilityDto Updated content
     * @return {@link org.springframework.http.HttpStatus#NO_CONTENT}
     * @throws BureauOptimisticLockingException Response was updated previously and the data in this request is stale.
     * @throws ResponseAlreadyMergedException   Response has been merged to Juror previously - not allowed to edit.
     */
    @PostMapping("/details/eligibility")
    @Operation(summary = "changes to juror response - juror "
        + "eligibility",
        description = "Juror Eligibility edit")
    public ResponseEntity<Void> updateJurorEligibility(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        @Parameter(hidden = true) BureauJwtAuthentication jwt,
        @Validated @RequestBody JurorEligibilityDto jurorEligibilityDto) {
        validateJurorNumberPathVariable(jurorId);

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) jwt.getPrincipal();
        responseUpdateService.updateJurorEligibility(jurorEligibilityDto, jurorId, jwtPayload.getLogin());
        log.info("Updated third party juror eligibility for juror {}", jurorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/details/excusal")
    @Operation(summary = "changes to juror response - excusal/deferral",
        description = "Excusal/Deferral edit")
    public ResponseEntity<Void> updateDeferralExcusal(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        @Parameter(hidden = true) BureauJwtAuthentication jwt,
        @Validated @RequestBody DeferralExcusalDto deferralExcusalDto)
        throws ResponseAlreadyMergedException {
        validateJurorNumberPathVariable(jurorId);

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) jwt.getPrincipal();
        responseUpdateService.updateExcusalDeferral(deferralExcusalDto, jurorId, jwtPayload.getLogin());
        log.info("Updated update excusal/deferral section for juror {}", jurorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/details/special-needs")
    @Operation(summary = "changes to juror response - reasonable"
        + " adjustments (special needs)",
        description = "Reasonable adjustments (special needs) edit")
    public ResponseEntity<Void> updateSpecialNeeds(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        @Parameter(hidden = true) BureauJwtAuthentication jwt,
        @Validated @RequestBody ReasonableAdjustmentsDto reasonableAdjustmentsDto)
        throws ResponseAlreadyMergedException {
        validateJurorNumberPathVariable(jurorId);

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) jwt.getPrincipal();
        responseUpdateService.updateSpecialNeeds(reasonableAdjustmentsDto, jurorId, jwtPayload.getLogin());
        log.info("Updated update special needs section for juror {}", jurorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/details/cjs")
    @Operation(summary = "changes to juror response - CJS employee details"
        + " edit",
        description = "CJS employee details edit")
    public ResponseEntity<Void> updateCjs(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        @Parameter(hidden = true) BureauJwtAuthentication jwt,
        @Validated @RequestBody CJSEmploymentDetailsDto cjsEmploymentDetailsDto)
        throws ResponseAlreadyMergedException {
        validateJurorNumberPathVariable(jurorId);

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) jwt.getPrincipal();
        responseUpdateService.updateCjs(cjsEmploymentDetailsDto, jurorId, jwtPayload.getLogin());
        log.info("Updated CJS employee section for juror {}", jurorId);
        return ResponseEntity.noContent().build();
    }


    /**
     * Update the juror response, if the Pool status has already been updated in legacy and response is not perocessed.
     *
     * @param jurorId           Juror number of response to process
     * @param updateResponseDto Update information
     * @param principal         Currently authenticated bureau officer details
     * @return HTTP 202 accepted - no body content
     * @throws BureauOptimisticLockingException Response data from the UI is outdated. Version mismatch with DB.
     */
    @PostMapping("/response/status")
    @Operation(summary = "Update juror response if legacy status changed",
        description = "Update and process juror response")
    public ResponseEntity<Object> updateResponseStatus(@Parameter(description = "Valid juror number",
        required = true) @PathVariable String jurorId,
                                                       @Parameter(description = "Status update details") @RequestBody BureauResponseStatusUpdateDto updateResponseDto,
                                                       @Parameter(hidden = true) BureauJwtAuthentication principal)
        throws BureauOptimisticLockingException {

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) principal.getPrincipal();

        try {
            responseUpdateService.updateResponseStatus(jurorId, updateResponseDto.getStatus(),
                updateResponseDto.getVersion(), jwtPayload.getLogin()
            );
        } catch (OptimisticLockingFailureException olfe) {
            log.warn("Juror {} response was updated by another user!", jurorId);
            throw new BureauOptimisticLockingException(olfe);
        }

        log.info("Status updated successfully");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .build();
    }


    /**
     * Validate a juror number path variable matches {@link ValidationConstants#JUROR_NUMBER}.
     *
     * @param jurorNumber Path variable supplied juror number
     */
    static void validateJurorNumberPathVariable(final String jurorNumber) {
        if (!jurorNumber.matches(ValidationConstants.JUROR_NUMBER)) {
            log.warn("Juror number {} in path invalid", jurorNumber);
            throw new ValidationException("Juror number must be exactly 9 digits");
        }
        log.trace("Juror number valid");
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Request body for updating the notes field of a juror response")
    public static class JurorNoteDto implements Serializable {

        @Schema(description = "Juror response notes", example = "Some free form text", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @Length(max = 2000)
        @NotEmpty
        private String notes;

        @Schema(description = "Juror response Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        @Pattern(regexp = ValidationConstants.MD5_HASHCODE)
        private String version;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Request body for adding a phone log to a juror response")
    public static class JurorPhoneLogDto implements Serializable {

        @Schema(description = "Phone log notes", example = "Call related to something", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @Length(max = 2000)
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AbstractJurorDetailsDto implements Serializable {

        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        @Schema(description = "Notes regarding update", requiredMode = Schema.RequiredMode.REQUIRED)
        private String notes;

        @Length(max = 10)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror title")
        private String title;

        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Jurors first name")
        private String firstName;

        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Jurors last name")
        private String lastName;

        @NotEmpty
        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 1")
        @JsonProperty("address1")
        private String address;

        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 2")
        private String address2;

        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 3")
        private String address3;

        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 4")
        private String address4;

        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 5")
        private String address5;

        @Pattern.List({
            @Pattern(regexp = NO_PIPES_REGEX),
            @Pattern(regexp = POSTCODE_REGEX)
        })
        @Length(max = 10)
        @Schema(description = "Juror postcode")
        private String postcode;

        @LocalDateOfBirth
        @Past
        @Schema(description = "Juror date of birth")
        private LocalDate dob;

        @Pattern(regexp = PHONE_PRIMARY_REGEX)
        @Schema(description = "Juror main phone number")
        private String mainPhone;

        @Pattern(regexp = PHONE_SECONDARY_REGEX)
        @Schema(description = "Juror secondary phone number")
        private String altPhone;

        @Pattern(regexp = EMAIL_ADDRESS_REGEX)
        @Length(max = 254)
        @Schema(description = "Juror email address")
        private String emailAddress;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Request body for editing the juror details section of a first person juror response")
    public static class FirstPersonJurorDetailsDto extends AbstractJurorDetailsDto {
        @Builder
        private FirstPersonJurorDetailsDto(Integer version, String notes, String title, String firstName,
                                           String lastName, String address, String address2, String address3,
                                           String address4, String address5, String postcode, LocalDate dob,
                                           String mainPhone, String altPhone, String emailAddress) {
            super(
                version,
                notes,
                title,
                firstName,
                lastName,
                address,
                address2,
                address3,
                address4,
                address5,
                postcode,
                dob,
                mainPhone,
                altPhone,
                emailAddress
            );
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Request body for editing the juror details section of a third party juror response")
    public static class ThirdPartyJurorDetailsDto extends AbstractJurorDetailsDto {

        @Schema(description = "Flag for using Jurors phone as contact")
        @JsonProperty("useJurorPhoneDetails")
        private Boolean useJurorPhone;

        @Schema(description = "Flag for using Jurors email as contact")
        @JsonProperty("useJurorEmailDetails")
        private Boolean useJurorEmail;

        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Third party respondents first name")
        private String thirdPartyFirstName;

        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Third party respondents last name")
        private String thirdPartyLastName;

        @NotEmpty
        @Length(max = 50)
        @Schema(description = "Third party respondents relationship to Juror")
        private String relationship;

        @NotEmpty
        @Length(max = 1000)
        @Schema(description = "Reason response is a third party response")
        private String thirdPartyReason;

        @Pattern(regexp = NO_PIPES_REGEX)
        @Length(max = 1000)
        @Schema(description = "Details of why other was selected as the thirdPartyReason")
        private String thirdPartyOtherReason;

        @Pattern(regexp = PHONE_PRIMARY_REGEX)
        @Schema(description = "Third party respondents main phone number")
        private String thirdPartyMainPhone;

        @Pattern(regexp = PHONE_SECONDARY_REGEX)
        @Schema(description = "Third party respondents secondary phone number")
        private String thirdPartyAltPhone;

        @Length(max = 254)
        @Pattern(regexp = EMAIL_ADDRESS_REGEX)
        @Schema(description = "Third party respondents email address")
        private String thirdPartyEmail;

        @Builder
        private ThirdPartyJurorDetailsDto(Integer version, String notes, String title, String firstName,
                                          String lastName, String address, String address2, String address3,
                                          String address4, String address5, String postcode, LocalDate dob,
                                          String mainPhone, String altPhone, String emailAddress,
                                          Boolean useJurorPhone, Boolean useJurorEmail, String thirdPartyFirstName,
                                          String thirdPartyLastName, String relationship, String thirdPartyReason,
                                          String thirdPartyOtherReason, String thirdPartyMainPhone,
                                          String thirdPartyAltPhone, String thirdPartyEmail) {
            super(
                version,
                notes,
                title,
                firstName,
                lastName,
                address,
                address2,
                address3,
                address4,
                address5,
                postcode,
                dob,
                mainPhone,
                altPhone,
                emailAddress
            );
            this.useJurorPhone = useJurorPhone;
            this.useJurorEmail = useJurorEmail;
            this.thirdPartyFirstName = thirdPartyFirstName;
            this.thirdPartyLastName = thirdPartyLastName;
            this.relationship = relationship;
            this.thirdPartyReason = thirdPartyReason;
            this.thirdPartyOtherReason = thirdPartyOtherReason;
            this.thirdPartyMainPhone = thirdPartyMainPhone;
            this.thirdPartyAltPhone = thirdPartyAltPhone;
            this.thirdPartyEmail = thirdPartyEmail;
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeferralExcusalDto implements Serializable {

        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        @Schema(description = "Notes regarding the change to details", requiredMode = Schema.RequiredMode.REQUIRED)
        private String notes;

        /**
         * Is this an excusal = true, or deferral = false.
         */
        @NotNull
        @Schema(description = "Flag whether this is an excusal (true) or deferral (false)", requiredMode =
            Schema.RequiredMode.REQUIRED)
        private DeferralExcusalUpdateType excusal;

        @Length(max = 1000)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "List of nominated deferral dates")
        private String deferralDates;

        /**
         * Excusal / deferral reason string.
         */
        @Length(max = 1000)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Reason for excusal/deferral")
        private String reason;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReasonableAdjustmentsDto implements Serializable {

        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        @Schema(description = "Notes regarding the change to details", requiredMode = Schema.RequiredMode.REQUIRED)
        private String notes;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors limited mobility")
        private String limitedMobility;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors hearing impairment")
        private String hearingImpairment;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors diabetes")
        private String diabetes;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors sight impairment")
        private String sightImpairment;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors learning disability")
        private String learningDisability;

        @Length(max = 1000)
        @Schema(description = "Details about any other special needs")
        private String other;

        /**
         * Maps to {@link uk.gov.hmcts.juror.api.juror.domain.JurorResponse# specialNeedsArrangements}.
         */
        @Length(max = 1000)
        @Schema(description = "Details about required special arrangements")
        private String specialArrangements;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CJSEmploymentDetailsDto implements Serializable {
        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        @Schema(description = "Notes regarding the change to details", requiredMode = Schema.RequiredMode.REQUIRED)
        private String notes;

        @NotEmpty
        @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
        private String jurorNumber;

        @Length(max = 1000)
        @Schema(description = "Details on police force employment")
        private String policeForceDetails;

        @Length(max = 1000)
        @Schema(description = "Details on HM Prison Service employment")
        private String prisonServiceDetails;

        @Schema(description = "Whether juror has had NCA employment")
        private Boolean ncaEmployment;

        @Schema(description = "Whether juror has had Judiciary employment")
        private Boolean judiciaryEmployment;

        @Schema(description = "Whether juror has had HMCTS employment")
        private Boolean hmctsEmployment;

        @Length(max = 1000)
        @Schema(description = "Details on other CJS employment")
        private String otherDetails;

    }

    public enum DeferralExcusalUpdateType {
        CONFIRMATION,
        DEFERRAL,
        EXCUSAL
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request body for editing the juror eligibility section of a response")
    public static class JurorEligibilityDto {
        @NotNull
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        private String notes;

        @Schema(description = "Whether the Juror has lived in the UK for the required period", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean residency;

        @Schema(description = "Textual description of the residency criteria")
        private String residencyDetails;

        @Schema(description = "Whether the Juror has been sectioned under the Mental Health Act", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean mentalHealthAct;

        @Schema(description = "Textual description of the mental health act criteria")
        private String mentalHealthActDetails;

        @Schema(description = "Whether the Juror is on bail", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean bail;

        @Schema(description = "Textual description of the bail criteria")
        private String bailDetails;

        @Schema(description = "Whether the Juror has a criminal conviction", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean convictions;

        @Schema(description = "Textual description of the convictions criteria")
        private String convictionsDetails;
    }

}
