package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {
    @Test
    void givenBuilderWithDefaultValueThanDefaultValueIsPresent() {
        User user = User.builder().build();

        assertEquals(0, user.getLevel());
        assertEquals(Boolean.TRUE, user.isActive());
    }
}