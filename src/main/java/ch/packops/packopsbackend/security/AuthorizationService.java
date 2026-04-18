package ch.packops.packopsbackend.security;

import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.domain.Role;
import ch.packops.packopsbackend.domain.User;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean canManageUsers(User user) {
        return hasRole(user, Role.ADMIN);
    }

    public boolean canManageProductConfigurations(User user) {
        return hasRole(user, Role.ADMIN);
    }

    public boolean canUpdateConfiguration(User user) {
        return hasRole(user, Role.ADMIN) || hasRole(user, Role.OPERATOR);
    }

    public boolean canStartProcess(User user) {
        return hasRole(user, Role.ADMIN) || hasRole(user, Role.OPERATOR);
    }

    public boolean canStopProcess(User user, Process process) {
        if (user == null || process == null) {
            return false;
        }

        if (hasRole(user, Role.ADMIN)) {
            return true;
        }

        if (hasRole(user, Role.OPERATOR)) {
            return process.getUser() != null
                    && process.getUser().getId() != null
                    && process.getUser().getId().equals(user.getId());
        }

        return false;
    }

    public boolean canViewProcess(User user, Process process) {
        if (user == null || process == null) {
            return false;
        }

        if (hasRole(user, Role.ADMIN)) {
            return true;
        }

        if (hasRole(user, Role.OPERATOR)) {
            return process.getUser() != null
                    && process.getUser().getId() != null
                    && process.getUser().getId().equals(user.getId());
        }

        if (hasRole(user, Role.VIEWER)) {
            return false;
        }

        return false;
    }

    private boolean hasRole(User user, String role) {
        return user != null
                && user.getRole() != null
                && user.getRole().equalsIgnoreCase(role);
    }
}