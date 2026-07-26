package com.accessiq.repository;

import com.accessiq.model.Role;
import com.accessiq.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    List<Role> findByNameIn(List<RoleName> names);

    boolean existsByName(RoleName name);
}