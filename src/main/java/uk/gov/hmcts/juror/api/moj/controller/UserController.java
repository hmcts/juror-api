package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CreateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UpdateUserDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserSearchDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UsernameDto;
import uk.gov.hmcts.juror.api.moj.service.UserService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "View all users")
    @PreAuthorize(SecurityUtil.USER_TYPE_ADMINISTRATOR + " or " + SecurityUtil.IS_MANAGER)
    public ResponseEntity<PaginatedList<UserDetailsDto>> viewAllUsers(
        @RequestBody @Valid UserSearchDto userSearchDto
    ) {
        return ResponseEntity.ok(userService.getUsers(userSearchDto));
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new user")
    @PreAuthorize(SecurityUtil.USER_TYPE_ADMINISTRATOR)
    public ResponseEntity<UsernameDto> createUser(
        @RequestBody @Valid CreateUserDto createUserDto
    ) {
        return ResponseEntity.accepted().body(userService.createUser(createUserDto));
    }

    @GetMapping("/{username}")
    @Operation(summary = "View user")
    @PreAuthorize(SecurityUtil.USER_TYPE_ADMINISTRATOR + " or " + SecurityUtil.IS_MANAGER)
    public ResponseEntity<UserDetailsDto> getUser(
        @PathVariable("username")
        @Parameter(description = "username", required = true)
        @Valid @NotBlank String username) {
        return ResponseEntity.ok(userService.getUser(username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update a new user")
    @PreAuthorize(SecurityUtil.USER_TYPE_ADMINISTRATOR + " or " + SecurityUtil.IS_MANAGER)
    public ResponseEntity<Void> updateUser(
        @PathVariable("username")
        @Parameter(description = "username", required = true)
        @Valid String username,
        @RequestBody @Valid UpdateUserDto updateUserDto
    ) {
        userService.updateUser(username, updateUserDto);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{username}/courts")
    @Operation(summary = "Add courts to user")
    @PreAuthorize(SecurityUtil.USER_TYPE_ADMINISTRATOR)
    public ResponseEntity<Void> addCourt(
        @PathVariable("username")
        @Parameter(description = "username", required = true)
        @Valid @NotBlank String username,
        @RequestBody @Valid @NotEmpty List<@CourtLocationCode String> courts
    ) {
        userService.addCourt(username, courts);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{username}/courts")
    @Operation(summary = "Remove courts from user")
    @PreAuthorize(SecurityUtil.USER_TYPE_ADMINISTRATOR)
    public ResponseEntity<Void> removeCourt(
        @PathVariable("username")
        @Parameter(description = "username", required = true)
        @Valid @NotBlank String username,
        @RequestBody @Valid @NotEmpty List<@CourtLocationCode String> courts
    ) {
        userService.removeCourt(username, courts);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{username}/type/{type}")
    @Operation(summary = "Update the type of user")
    @PreAuthorize(SecurityUtil.USER_TYPE_ADMINISTRATOR)
    public ResponseEntity<Void> updateUserType(
        @PathVariable("username")
        @Parameter(description = "username", required = true)
        @Valid @NotBlank String username,
        @PathVariable("type")
        @Parameter(description = "type", required = true)
        @Valid UserType userType
    ) {
        userService.changeUserType(username, userType);
        return ResponseEntity.accepted().build();
    }
}
