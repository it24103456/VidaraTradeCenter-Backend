package com.vidara.tradecenter.user.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import com.vidara.tradecenter.user.model.enums.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private UserRole name;

    // CONSTRUCTORS

    public Role() {
    }

    public Role(UserRole name) {
        this.name = name;
    }


    // GETTERS AND SETTERS
    public UserRole getName() {
        return name;
    }

    public void setName(UserRole name) {
        this.name = name;
    }


    // TOSTRING

    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name=" + name +
                '}';
    }
}