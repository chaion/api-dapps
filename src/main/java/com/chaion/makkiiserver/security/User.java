package com.chaion.makkiiserver.security;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Set;

@Data
public class User {

    @Id
    private String id;
    private String username;
    private String password;
    private Set<Role> roles;
}

@Data
class Role {
    String name;
}
