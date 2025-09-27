package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.model.Roles;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AccessManager {
    private final Datastore datastore;

    @Getter
    private final List<String> saGroup;

    @Getter
    private final List<String> employeeGroup;

    public AccessManager(Datastore datastore) {
        this.datastore = datastore;
        this.saGroup = List.of(Roles.ADMIN, Roles.SUPPORT_STAFF);
        this.employeeGroup = List.of(Roles.ADMIN, Roles.SUPPORT_STAFF, Roles.DOCTOR);
    }

    public void enforceRoleBasedAccess(List<String> accessGroup, String token) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public String enforceOwnershipBasedAccess(String token) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
