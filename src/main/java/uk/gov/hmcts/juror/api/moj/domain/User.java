package uk.gov.hmcts.juror.api.moj.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.juror.api.bureau.domain.Team;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", schema = "juror_mod")
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"team", "courts"})
public class User implements Serializable {

    @Column(name = "owner")
    @Deprecated(forRemoval = true)
    private String owner;

    @Id
    @Column(name = "username", unique = true, length = 20)
    @NotEmpty
    @Size(min = 1, max = 20)
    private String username;


    @Column(name = "email", unique = true)
    @Size(min = 1, max = 200)
    private String email;

    @Column(name = "name", length = 50, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 50)
    private String name;

    @NotNull
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @JsonProperty("last_logged_in")
    private LocalDateTime lastLoggedIn;

    @ManyToOne
    @Deprecated(forRemoval = true)//TODO confirm
    private Team team;

    @Version
    private Integer version;

    @Column(name = "approval_limit")
    private BigDecimal approvalLimit;

    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @ElementCollection
    @CollectionTable(schema = "juror_mod", name = "user_roles", joinColumns = @JoinColumn(name = "username",
        referencedColumnName = "username"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles;


    @JoinTable(
        schema = "juror_mod", name = "user_courts",
        joinColumns = @JoinColumn(name = "username", referencedColumnName = "username"),
        inverseJoinColumns = @JoinColumn(name = "loc_code", referencedColumnName = "loc_code")
    )
    @ManyToMany
    private Set<CourtLocation> courts;


    public User() {

    }

    @Deprecated(forRemoval = true)
    public Boolean isTeamLeader() {
        return hasRole(Role.TEAM_LEADER);
    }

    public boolean hasRole(Role role) {
        return getRoles().contains(role);
    }

    @Deprecated(forRemoval = true)
    public String getOwner() {
        return owner;
    }

    @Deprecated(forRemoval = true)
    public Integer getLevel() {
        if (hasRole(Role.SENIOR_JUROR_OFFICER)) {
            return 9;
        } else if (hasRole(Role.TEAM_LEADER) || UserType.COURT.equals(this.getUserType())) {
            return 1;
        } else {
            return 0;
        }
    }

    @Deprecated(forRemoval = true)
    public Team getTeam() {
        return team;
    }


    public Set<Role> getRoles() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        return roles;
    }

    public void addRole(Role role) {
        this.getRoles().add(role);
    }

    public Set<CourtLocation> getCourts() {
        if (courts == null) {
            courts = new HashSet<>();
        }
        return courts;
    }

    public void addCourt(CourtLocation courtLocation) {
        this.getCourts().add(courtLocation);
    }

    public void removeCourt(CourtLocation courtLocation) {
        this.getCourts().remove(courtLocation);
    }

    public boolean hasCourtByOwner(String owner) {
        return this.getCourts().stream().anyMatch(courtLocation -> courtLocation.getOwner().equals(owner));
    }

    public void clearCourts() {
        this.getCourts().clear();
    }

    public void clearRoles() {
        this.getRoles().clear();
    }
}
