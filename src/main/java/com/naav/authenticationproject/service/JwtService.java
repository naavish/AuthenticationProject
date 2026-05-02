package com.naav.authenticationproject.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Base64;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// This CLASS IS A Jwt Service and is responsible for Creating JWT tokens , Reading Data from Tokens , Validating data from tokens.
/*
From Application.properties we have secret key used to sign tokens
jwtExpiration = how long tokens live
 */
@Service
public class JwtService  {

    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // Extracting data from token ,
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    //Generic Extractor , this is a reusable engine , take token , decode claims and apply any function
    public <T> T extractClaim(String token , Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //token creation , create token with no extra data
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }


    public String generateToken(Map<String,Object> extraClaims, UserDetails userDetails){
        return buildToken(extraClaims, userDetails , jwtExpiration);
    }

    public long getExpirationTime(){
        return jwtExpiration;
    }

    //building the secure token

    private String buildToken(Map<String, Object> extractClaims, UserDetails userDetails , long expiration ){
        return Jwts.builder().setClaims(extractClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
    }

    // this checks if username matches and token not expired
    public boolean isTokenValid(String token, UserDetails userDetails ) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Checks if token is older than now
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
// :: is a method reference operator , a shortcut lambda expression , meaning when a claims object is given , call getExpiration on it
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    // signing the token
    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);

    }


}
