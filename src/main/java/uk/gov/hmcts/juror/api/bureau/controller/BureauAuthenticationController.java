package uk.gov.hmcts.juror.api.bureau.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.service.BureauAuthenticationService;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Authentication endpoints for Bureau authentication.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Tag(name = "Auth (Bureau)", description = "Bureau Authentication API")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Deprecated(forRemoval = true)
public class BureauAuthenticationController {
    private final BureauAuthenticationService authenticationService;
    private final CourtLocationRepository courtLocationRepository;
    private final EntityManager entityManager;


    @PostMapping(path = "/auth/bureau")
    @Operation(summary = "Authenticate Bureau Login",
        description = "Authenticate Bureau Officer credentials to allow creation of a JWT")
    public ResponseEntity<BureauAuthenticationResponseDto> authenticationEndpoint(
        @RequestBody BureauAuthenticationRequestDto requestDto) {
        return ResponseEntity.ok().body(authenticationService.authenticateBureauOfficer(requestDto));
    }


    /**
     * Login credentials for Bureau authentication.
     * Created by jonny on 24/03/17.
     */
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @ToString(exclude = "password")
    @Schema(description = "Bureau officer login credentials")
    public static class BureauAuthenticationRequestDto implements Serializable {
        @Schema(description = "Bureau login username", requiredMode = Schema.RequiredMode.REQUIRED)
        private String userId;

        @Schema(description = "Bureau login password", requiredMode = Schema.RequiredMode.REQUIRED)
        private transient String password;
    }

    /**
     * Response dto for a successful Bureau authentication request.
     * Created by jonny on 24/03/17.
     */
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(description = "Bureau officer authentication response. Used to mint a valid JWT")
    public static class BureauAuthenticationResponseDto implements Serializable {
        @Schema(description = "Owner number for current user", requiredMode = Schema.RequiredMode.REQUIRED)
        private String owner;

        @Schema(description = "Bureau login username", requiredMode = Schema.RequiredMode.REQUIRED)
        private String login;

        @Schema(description = "Bureau user access level", requiredMode = Schema.RequiredMode.REQUIRED)
        private String userLevel;

        @Schema(description = "Days left before user credentials expire", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Integer daysToExpire;

        @Schema(description = "Flag. Imminent expiry of user credentials", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean passwordWarning;

        private UserType userType;


        private Set<Role> roles;

        @Schema(description = "Staff information")
        private UserDto staff;
    }

    /**
     * Response dto for the staff details.
     *
     * @since Sprint 13
     */
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(description = "Bureau staff member information")
    public static class UserDto implements Serializable {
        /**
         * Staff member name. (E.g. "Joe Bloggs")
         */
        @Schema(description = "Staff member display fullname")
        private String name;

        /**
         * Staff rank. Default 0 is staff member. 1 is team leader.
         *
         * @implNote Integer value as the requirements for this status are fluid.
         */
        @Schema(description = "Staff rank.", example = "0 = Worker, 1 = Team Lead")
        private Integer rank;

        /**
         * Is this staff member active.
         *
         * @implNote Integer value as the requirements for this status are fluid.
         */
        @Schema(description = "Is the staff member active?", example = "0 = no, 1 = yes")
        private Integer active;

        /**
         * Court locations staff member covers.
         */
        @Schema(description = "Court location codes the staff member administrates.")
        private List<String> courts;

        public static UserDto from(final CourtLocationRepository courtLocationRepository, EntityManager entityManager,
                                   final User user) {
            if (null == user) {
                log.debug("User record was null!");
                return null;
            }

            return UserDto.builder()
                .active(user.isActive() ? 1 : 0)
                .name(user.getUsername())
                .rank(user.getLevel())
                .courts(courtLocationRepository.findLocCodeByOwner(entityManager, user.getOwner()))
                .build();
        }
    }


    public UserDto from(final User user) {
        return UserDto.from(courtLocationRepository, entityManager, user);
    }
}
