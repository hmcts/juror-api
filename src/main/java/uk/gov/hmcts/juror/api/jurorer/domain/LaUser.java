package uk.gov.hmcts.juror.api.jurorer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.envers.NotAudited;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user", schema = "juror_er")
@Data
@Builder
@AllArgsConstructor
public class LaUser implements Serializable {

    @Id
    @Column(name = "username", unique = true, length = 200)
    @NotEmpty
    @Size(min = 1, max = 200)
    private String username;

    @JoinColumn(name = "la_code", nullable = false)
    @ManyToOne
    @NotEmpty
    private LocalAuthority laCode;

    @NotNull
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @NotAudited
    @JsonProperty("last_logged_in")
    private LocalDateTime lastLoggedIn;

}
