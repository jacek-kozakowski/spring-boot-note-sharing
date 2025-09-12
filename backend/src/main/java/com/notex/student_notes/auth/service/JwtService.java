package com.notex.student_notes.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpirationTime;

    public long getExpirationTime(){return jwtExpirationTime;}

    public String generateToken(UserDetails userDetails, long expirationTime){
        log.info("Generating token for user {}", userDetails.getUsername());
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }
    public String generateToken(UserDetails userDetails){
        return generateToken(userDetails, jwtExpirationTime);
    }
    private Claims extractClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extractClaims(token);
        return claimResolver.apply(claims);
    }

    private SecretKey getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        if (keyBytes.length < 32){
            throw new IllegalArgumentException("Secret key must be at least 256 bites (32 characters).");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && isTokenNonExpired(token) && verifySignature(token));
    }

    private boolean verifySignature(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        }catch (JwtException e){
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenNonExpired(String token){
        return extractClaim(token, Claims::getExpiration).after(new Date());
    }
}
