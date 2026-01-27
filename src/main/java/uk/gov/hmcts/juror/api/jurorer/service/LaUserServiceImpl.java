package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaJwtDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaUserDetailsDto;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.repository.LaUserRepository;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.JwtService;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LaUserServiceImpl implements LaUserService {

    private final LaUserRepository userRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public LaJwtDto createJwt(String email) {
        LaUser user = findUserByUsername(email);

        throw new MojException.Forbidden("User not found", null);
    }


    @Override
    @Transactional(readOnly = true)
    public LaUserDetailsDto getUser(String username) {
        LaUser user = findUserByUsername(username);
        return new LaUserDetailsDto();
    }


    @Override
    @Transactional(readOnly = true)
    public LaUser findUserByUsername(String username) {
        return userRepository.findById(username).orElseThrow(
            () -> new MojException.NotFound("User not found", null)
        );
    }

}
