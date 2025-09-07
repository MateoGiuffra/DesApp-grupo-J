package com.desapp.football_api.security;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        byte[] key = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        System.out.println("la contra es: " + Base64.getEncoder().encodeToString(key));
    }
}
