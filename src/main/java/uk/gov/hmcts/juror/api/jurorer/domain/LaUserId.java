package uk.gov.hmcts.juror.api.jurorer.domain;

import java.io.Serializable;
import java.util.Objects;

public class LaUserId implements Serializable {

    private String username;
    private String localAuthority; // Must match the PK type of LocalAuthority (la_code)

    public LaUserId() {
        // Intentionally empty constructor.
    }

    public LaUserId(String username, String localAuthority) {
        this.username = username;
        this.localAuthority = localAuthority;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof LaUserId that)) {
            return false;
        }

        return Objects.equals(username, that.username)
            && Objects.equals(localAuthority, that.localAuthority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, localAuthority);
    }
}
