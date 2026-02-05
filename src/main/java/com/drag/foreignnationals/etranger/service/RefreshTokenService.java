package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.entity.RefreshToken;
import com.drag.foreignnationals.etranger.repository.RefreshTokenRepository;
import com.drag.foreignnationals.etranger.repository.UserRepository;

public interface RefreshTokenService {

    public RefreshToken createRefreshToken(Long userId);
    public RefreshToken verifyExpiration(RefreshToken token);
}
