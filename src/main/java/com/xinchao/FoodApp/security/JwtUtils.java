package com.xinchao.FoodApp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service // logic class
@Slf4j // log help
public class JwtUtils { // login manager
    private static final long EXPIRATION_TIME = 30L * 24* 60 * 60 *1000; // token will expire in ... miliseconds
    private SecretKey key; // secret stamp use to check jwt, secretKey la inteface con secretkexspec la class implement

    @Value("${secretJwtString}")
    private String secretJwtString;


    @PostConstruct // after spring create object and inject, run this method
    private void init(){
        byte[] keyByte = secretJwtString.getBytes(StandardCharsets.UTF_8); // chuyen secretString thanh array of byte
        // save key into class
        this.key = new SecretKeySpec(keyByte, "HmacSHA256"); // object dai dien cho khoa, kexbyte la cac byte cua khoa, con Hmac la ten thuattoan ky
    }

    public String generateToken(String email){ // tao JWT
        return Jwts.builder()
                .subject(email) // info to point who Jwt is about
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact(); // make jwt to string
    }

    public String getUsernameFromToken(String token){ // doc email tu jwt
        return extractClaims(token, Claims::getSubject); // Claims tap hop thong tin trong jwt
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction){
        return claimsTFunction.apply((Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()));
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private  boolean isTokenExpired(String token){
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
}
