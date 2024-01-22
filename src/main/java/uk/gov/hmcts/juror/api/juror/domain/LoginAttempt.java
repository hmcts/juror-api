package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Login attempt entity.  Tracks failed login attempts by jurors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LOGIN_ATTEMPTS", schema = "JUROR_DIGITAL_USER")
@Deprecated(forRemoval = true)
public class LoginAttempt implements Serializable {
    @Id
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "LOGINATTEMPTS")
    private Integer loginattempts;
}
