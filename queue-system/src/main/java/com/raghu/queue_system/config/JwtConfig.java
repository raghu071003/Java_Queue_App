package com.raghu.queue_system.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class JwtConfig {

    public static final Key SECRET_KEY =
            Keys.secretKeyFor(SignatureAlgorithm.HS256);
}