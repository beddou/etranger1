package com.drag.foreignnationals.etranger.security.repository;

import com.drag.foreignnationals.etranger.security.entity.RefreshToken;
import com.drag.foreignnationals.etranger.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}