package com.nie.mentalserver.service;

public interface TokenBlacklistService {

    void blacklist(String jti, long remainingMillis);

    boolean isBlacklisted(String jti);
}
