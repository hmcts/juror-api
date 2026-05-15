package uk.gov.hmcts.juror.api.jurorer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaUserDetailsDto;
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;

@RestController
@RequestMapping(value = "/api/v1/juror-er/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LaUserController {

    private final LaUserService userService;

    @GetMapping("/{lacode}")
    @Operation(summary = "Get a list of users at local authority")
    public ResponseEntity<LaUserDetailsDto> getUserDetails(
        @PathVariable(name = "lacode") String laCode
    ) {
        return ResponseEntity.ok(userService.getLaUserDetails(laCode));
    }
}
