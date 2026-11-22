package com.minerva.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.minerva.api.User.Roles;
import com.minerva.api.User.User;
public final class RolAutorizacion {

    private RolAutorizacion() {
    }

    public static Roles obtenerRol(User usuario) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String nombre = authority.getAuthority();
                if (nombre == null) {
                    continue;
                }
                if (nombre.startsWith("ROLE_")) {
                    nombre = nombre.substring("ROLE_".length());
                }
                try {
                    return Roles.valueOf(nombre);
                } catch (IllegalArgumentException ignored) {
                    // Continue with the next authority.
                }
            }
        }

        return usuario != null && usuario.getPersona() != null
                ? usuario.getPersona().getRol()
                : null;
    }
}
