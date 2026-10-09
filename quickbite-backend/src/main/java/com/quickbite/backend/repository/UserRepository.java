package com.quickbite.backend.repository;

import com.quickbite.backend.entity.User;
import com.quickbite.backend.enums.AccountStatus;
import com.quickbite.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleAndAccountStatus(Role role, AccountStatus status);

    // Admin user search by name or email (null q = no text filter)
    @Query("""
        select u from User u
        where (:q is null
               or lower(u.name) like lower(concat('%', :q, '%'))
               or lower(u.email) like lower(concat('%', :q, '%')))
        """)
    Page<User> search(@Param("q") String q, Pageable pageable);

    @Query("""
        select u from User u
        where u.role = :role
          and (:q is null
               or lower(u.name) like lower(concat('%', :q, '%'))
               or lower(u.email) like lower(concat('%', :q, '%')))
        """)
    Page<User> searchByRole(@Param("q") String q, @Param("role") Role role, Pageable pageable);
}