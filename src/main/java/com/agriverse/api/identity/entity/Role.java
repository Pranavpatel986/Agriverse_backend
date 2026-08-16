package com.agriverse.api.identity.entity;

import com.agriverse.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** RBAC role: READER | AUTHOR | EDITOR | ADMIN, per SAD Section 10 / DB spec Identity domain. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static final String READER = "READER";
    public static final String AUTHOR = "AUTHOR";
    public static final String EDITOR = "EDITOR";
    public static final String ADMIN = "ADMIN";
}
