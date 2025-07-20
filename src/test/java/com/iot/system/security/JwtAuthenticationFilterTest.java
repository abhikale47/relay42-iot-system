package com.iot.system.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private UserDetailsService userDetailsService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void testDoFilterInternalWithValidToken() throws ServletException, IOException {
    String token = "validToken";
    String username = "testuser";
    
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);
    
    UserDetails userDetails = new User(username, "password", 
        Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider).validateToken(token);
    verify(jwtTokenProvider).getUsernameFromToken(token);
    verify(userDetailsService).loadUserByUsername(username);
  }

  @Test
  void testDoFilterInternalWithInvalidToken() throws ServletException, IOException {
    String token = "invalidToken";
    
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider).validateToken(token);
    verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  void testDoFilterInternalWithNoAuthHeader() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  void testDoFilterInternalWithInvalidAuthHeader() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  void testDoFilterInternalWithException() throws ServletException, IOException {
    String token = "validToken";
    
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(token)).thenThrow(new RuntimeException("Token error"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider).validateToken(token);
    verify(jwtTokenProvider).getUsernameFromToken(token);
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }
}