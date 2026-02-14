package com.drag.foreignnationals.etranger.security.repository;

import com.drag.foreignnationals.etranger.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> , RevisionRepository<User, Long, Integer> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    @Query(value = "SELECT * FROM users WHERE username = :username",
            nativeQuery = true)
    Optional<User> findByUsernameIncludeDeleted(@Param("username") String username);

    @Query(value = "SELECT * FROM users WHERE id = ?", nativeQuery = true)
    Optional<User> findByIdIncludeDeleted(Long id);

    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE id = ? AND deleted = true", nativeQuery = true)
    boolean isActuallyDeleted(Long id);

}
