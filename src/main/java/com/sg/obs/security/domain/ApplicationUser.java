package com.sg.obs.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sg.obs.base.domain.BaseEntity;
import com.sg.obs.security.AppUserInfo;
import com.sg.obs.security.AppUserPrincipal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "APPLICATION_USERS")
public class ApplicationUser extends BaseEntity implements AppUserPrincipal, UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_users_id_seq")
    @SequenceGenerator(name = "application_users_id_seq", sequenceName = "application_users_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "USERNAME", unique = true)
    private String username;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @JsonIgnore
    @Column(name = "HASHED_PASSWORD")
    private String hashedPassword;

    @Column(name = "ENABLED")
    private Boolean enabled = true;

    @Column(name = "EMAIL", unique = true)
    private String email;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "PROFILE_PICTURE")
    private String profilePicture;

    @Override
    public AppUserInfo getAppUser() {
        return new AppUserInfo() {
            @Override
            public UserId getUserId() {
                return UserId.of(String.valueOf(id));
            }

            @Override
            public String getPreferredUsername() {
                return StringUtils.capitalize(firstName + " " + lastName);
            }

            @Override
            public String getPictureUrl() {
                return profilePicture;
            }
        };
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(Role::toSimpleGrantedAuthority).toList();
    }

    @Override
    public String getPassword() {
        return this.hashedPassword;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
