package com.sg.obs.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sg.obs.base.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;


@Getter
@Entity
@Table(name = "ROLES")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roles_id_seq")
    @SequenceGenerator(name = "roles_id_seq", sequenceName = "roles_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Integer id;


    @Column(name = "NAME", unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<ApplicationUser> users = new HashSet<>();

    @JsonIgnore
    public SimpleGrantedAuthority toSimpleGrantedAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + name);
    }
}
