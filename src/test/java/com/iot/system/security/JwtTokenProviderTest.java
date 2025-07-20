package com.iot.system.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;

class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
    
    // Set test values using reflection
    ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "testSecretKey123456789012345678901234567890");
    ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 86400); // 24 hours in seconds

    User userDetails = new User("testuser", "password", 
        Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
  }

  @Test
  void testGenerateToken() {
    String token = jwtTokenProvider.generateToken(authentication);
    
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains("."));
  }

  @Test
  void testGetUsernameFromToken() {
    String token = jwtTokenProvider.generateToken(authentication);
    String username = jwtTokenProvider.getUsernameFromToken(token);
    
    assertEquals("testuser", username);
  }

  @Test
  void testValidateTokenValid() {
    String token = jwtTokenProvider.generateToken(authentication);
    
    assertTrue(jwtTokenProvider.validateToken(token));
  }

  @Test
  void testValidateTokenInvalid() {
    String invalidToken = "invalid.token.here";
    
    assertFalse(jwtTokenProvider.validateToken(invalidToken));
  }

  @Test
  void testValidateTokenExpired() {
    // Set expiration to -1 second (already expired)
    ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", -1);
    String token = jwtTokenProvider.generateToken(authentication);
    
    assertFalse(jwtTokenProvider.validateToken(token));
  }

  @Test
  void testValidateTokenNull() {
    assertFalse(jwtTokenProvider.validateToken(null));
  }

  @Test
  void testValidateTokenEmpty() {
    assertFalse(jwtTokenProvider.validateToken(""));
  }

  @Test
  void testGetUsernameFromInvalidToken() {
    String invalidToken = "invalid.token.here";
    
    assertThrows(Exception.class, () -> {
      jwtTokenProvider.getUsernameFromToken(invalidToken);
    });
  }
}