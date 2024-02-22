package uk.gov.hmcts.juror.api.moj.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.type.YesNoConverter;
import uk.gov.hmcts.juror.api.bureau.domain.Team;
import uk.gov.hmcts.juror.api.moj.domain.lisener.UserListener;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users", schema = "juror_mod")
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"team", "courts"})
@EntityListeners(UserListener.class)
public class User implements Serializable {

    @Column(name = "owner")
    private String owner;
    @Id
    @Column(name = "username", unique = true, length = 20)
    @NotEmpty
    @Size(min = 1, max = 20)
    private String username;

    @Column(name = "name", length = 50, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 50)
    private String name;

    @NotNull
    @Min(0)
    @Max(9)
    @Column(name = "level", nullable = false)
    @Builder.Default
    //Level 0 = Standard User
    //Level 1 = Team Leader
    //Level 9 = Senior Juror Officer
    private Integer level = 0;

    @NotNull
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Transient
    private List<String> courts;

    @JsonProperty("last_logged_in")
    private LocalDateTime lastLoggedIn;

    @ManyToOne
    private Team team;

    @Version
    private Integer version;

    //Temp fields until we migrate over to active directory
    @Column(name = "password")
    @JsonIgnore
    @Deprecated(forRemoval = true)
    private String password;
    @Transient
    @Column(name = "password_warning")
    @Deprecated(forRemoval = true)
    private Boolean passwordWarning;

    @Transient
    @Column(name = "days_to_expire")
    @Deprecated(forRemoval = true)
    private Integer daysToExpire;

    @Column(name = "password_changed_date")
    @Deprecated(forRemoval = true)
    private Date passwordChangedDate;

    @Column(name = "failed_login_attempts")
    @Deprecated(forRemoval = true)
    private int failedLoginAttempts;

    @Column(name = "LOGIN_ENABLED_YN")
    @Convert(converter = YesNoConverter.class)
    @Deprecated(forRemoval = true)
    private Boolean loginEnabledYn;

    @Column(name = "approval_limit")
    private BigDecimal approvalLimit;
    @Column(name = "can_approve")
    private boolean canApprove;

    public Boolean isTeamLeader() {
        return level == SecurityUtil.TEAM_LEADER_LEVEL;
    }

    public Boolean isSeniorJurorOfficer() {
        return level == SecurityUtil.SENIOR_JUROR_OFFICER_LEVEL;
    }

    public User() {

    }

    @Deprecated(forRemoval = true)
    public void incrementLoginAttempt() {
        this.failedLoginAttempts += 1;
    }

    public String getCourtAtIndex(int index, String defaultValue) {
        if (this.getCourts() == null || index >= this.getCourts().size()) {
            return defaultValue;
        }
        return this.getCourts().get(index);
    }

    public void setCourtLocation(List<String> courts) {
        this.courts = Collections.unmodifiableList(courts);
    }
}
