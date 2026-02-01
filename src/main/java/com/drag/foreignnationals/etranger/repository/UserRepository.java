package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String name);
}
