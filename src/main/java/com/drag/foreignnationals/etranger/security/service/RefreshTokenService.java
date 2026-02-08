package com.drag.foreignnationals.etranger.security.service;

import com.drag.foreignnationals.etranger.security.entity.RefreshToken;

public interface RefreshTokenService {

    public RefreshToken createRefreshToken(Long userId);
    public RefreshToken verifyExpiration(RefreshToken token);
}
